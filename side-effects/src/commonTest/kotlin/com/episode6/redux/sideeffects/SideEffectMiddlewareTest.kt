@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.sideeffects

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import com.episode6.redux.Action
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.TimingController
import com.episode6.redux.testsupport.runFlowTest
import com.episode6.redux.testsupport.runTest
import com.episode6.redux.testsupport.stoplight.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.transformLatest
import kotlin.test.Test

class SideEffectMiddlewareTest {

  private val timing = TimingController()
  private fun CoroutineScope.stopLightStore() = stopLightStore(timing)

  @Test fun testInitialValue() = runTest {
    val store = stopLightStore()

    assertThat(store.value).hasDefaultLights()
  }

  @Test fun testInitialValue_flow() = runFlowTest {
    val store = stopLightStore()

    store.test {
      assertThat(values).all {
        hasSize(1)
        index(0).hasDefaultLights()
      }
    }
  }

  @Test fun testInitWithoutTime() = runTest {
    val store = stopLightStore()

    store.dispatch(SwitchToGreen)

    assertThat(store.value).hasLights(green = true)
  }

  @Test fun testInitWithoutTime_flow() = runFlowTest {
    val store = stopLightStore()

    store.test {
      store.dispatch(SwitchToGreen)

      assertThat(values).all {
        hasSize(3)
        index(2).hasLights(green = true)
      }
    }
  }
}

private object SwitchToGreen : Action
private object SwitchToYellow : Action
private object SwitchToRed : Action

private const val RED_TO_GREEN_DELAY = 75L
private const val GREEN_TO_YELLOW_DELAY = 60L
private const val YELLOW_TO_RED_DELAY = 15L

private fun CoroutineScope.stopLightStore(timing: TimingController): StoreFlow<StopLightState> = createStopLightStore(
  SideEffectMiddleware(
    SideEffect {
      actions.filterIsInstance<SwitchToGreen>().transformLatest {
        emitLights(green = true)
        timing.await(GREEN_TO_YELLOW_DELAY)
        emit(SwitchToYellow)
      }
    },
    SideEffect {
      actions.filterIsInstance<SwitchToYellow>().transformLatest {
        emitLights(yellow = true)
        timing.await(YELLOW_TO_RED_DELAY)
        emit(SwitchToRed)
      }
    },
    SideEffect {
      actions.filterIsInstance<SwitchToRed>().transformLatest {
        emitLights(red = true)
        timing.await(RED_TO_GREEN_DELAY)
        emit(SwitchToGreen)
      }
    },
  )
)

private suspend fun FlowCollector<Action>.emitLights(
  red: Boolean = false,
  yellow: Boolean = false,
  green: Boolean = false
) {
  emit(SetRedLightOn(red))
  emit(SetYellowLightOn(yellow))
  emit(SetGreenLightOn(green))
}
