package com.episode6.redux.testsupport.stoplight

import assertk.Assert
import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.runStoreTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope

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

fun CoroutineScope.createStopLightStore(vararg middlewares: Middleware<StopLightState>): StoreFlow<StopLightState> =
  StoreFlow(
    scope = this,
    initialValue = StopLightState(),
    reducer = StopLightState::reduce,
    middlewares = middlewares.toList(),
  )

@OptIn(ExperimentalCoroutinesApi::class) fun stopLightStoreTest(
  middlewares: List<Middleware<StopLightState>> = emptyList(),
  testBody: suspend TestScope.(StoreFlow<StopLightState>) -> Unit
) = runStoreTest(
  storeBuilder = { createStopLightStore(*middlewares.toTypedArray()) },
  testBody = testBody
)

fun Assert<StopLightState>.hasDefaultLights() = hasLights(red = true)
fun Assert<StopLightState>.hasLights(
  green: Boolean = false,
  yellow: Boolean = false,
  red: Boolean = false,
) = all {
  prop(StopLightState::greenLight).isEqualTo(green)
  prop(StopLightState::yellowLight).isEqualTo(yellow)
  prop(StopLightState::redLight).isEqualTo(red)
}
