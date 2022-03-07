package com.episode6.redux.stoplight

import assertk.Assert
import assertk.all
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.StoreFlow
import kotlinx.coroutines.CoroutineScope

data class StopLightState(
  val greenLight: Boolean = false,
  val yellowLight: Boolean = false,
  val redLight: Boolean = true,
)

sealed class ReduceStopLight(internal val reducer: StopLightState.() -> StopLightState) : Action
data class SetGreenLightOn(val on: Boolean) : ReduceStopLight({ copy(greenLight = on) })
data class SetYellowLightOn(val on: Boolean) : ReduceStopLight({ copy(yellowLight = on) })
data class SetRedLightOn(val on: Boolean) : ReduceStopLight({ copy(redLight = on) })

fun StopLightState.reduce(action: Action): StopLightState = (action as? ReduceStopLight)?.reducer?.invoke(this) ?: this

fun CoroutineScope.createStopLightStore(middlewares: List<Middleware<StopLightState>> = emptyList()): StoreFlow<StopLightState> =
  StoreFlow(
    initialValue = StopLightState(),
    reducer = StopLightState::reduce,
    middlewares = middlewares,
    scope = this
  )

fun Assert<StopLightState>.hasGreenLightOn() = prop(StopLightState::greenLight).isTrue()
fun Assert<StopLightState>.hasGreenLightOff() = prop(StopLightState::greenLight).isFalse()
fun Assert<StopLightState>.hasYellowLightOn() = prop(StopLightState::yellowLight).isTrue()
fun Assert<StopLightState>.hasYellowLightOff() = prop(StopLightState::yellowLight).isFalse()
fun Assert<StopLightState>.hasRedLightOn() = prop(StopLightState::redLight).isTrue()
fun Assert<StopLightState>.hasRedLightOff() = prop(StopLightState::redLight).isFalse()

fun Assert<StopLightState>.hasOnlyGreenLightOn() = all {
  hasGreenLightOn()
  hasYellowLightOff()
  hasRedLightOff()
}

fun Assert<StopLightState>.hasOnlyYellowLightOn() = all {
  hasGreenLightOff()
  hasYellowLightOn()
  hasRedLightOff()
}

fun Assert<StopLightState>.hasOnlyRedLightOn() = all {
  hasGreenLightOff()
  hasYellowLightOff()
  hasRedLightOn()
}
