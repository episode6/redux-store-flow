@file:OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)

package com.episode6.redux.sideeffects

import com.episode6.redux.Action
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.TimingController
import com.episode6.redux.testsupport.stoplight.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.test.Test

class SideEffectMiddlewareTest {

  private val timing = TimingController()
  private fun CoroutineScope.stopLightStore() = stopLightStore(timing)

  @Test fun testSomething() {

  }
}

private object ShutOffAllLights : Action
private object SwitchToGreen : Action
private object SwitchToYellow : Action
private object SwitchToRed : Action

private fun CoroutineScope.stopLightStore(timing: TimingController): StoreFlow<StopLightState> = createStopLightStore(
  SideEffectMiddleware(
    SideEffect {
      actions.filterIsInstance<ShutOffAllLights>()
        .map { flowOf(SetRedLightOn(false), SetGreenLightOn(false), SetYellowLightOn(false)) }
        .flattenConcat()
    },
    SideEffect {
      actions.filterIsInstance<SwitchToGreen>().transformLatest {
        emit(ShutOffAllLights)
        emit(SetGreenLightOn(true))
        timing.await(60)
        emit(SwitchToYellow)
      }
    },
    SideEffect {
      actions.filterIsInstance<SwitchToYellow>().transformLatest {
        emit(ShutOffAllLights)
        emit(SetYellowLightOn(true))
        timing.await(15)
        emit(SwitchToRed)
      }
    },
    SideEffect {
      actions.filterIsInstance<SwitchToRed>().transformLatest {
        emit(ShutOffAllLights)
        emit(SetRedLightOn(true))
        timing.await(75)
        emit(SwitchToGreen)
      }
    },
  )
)
