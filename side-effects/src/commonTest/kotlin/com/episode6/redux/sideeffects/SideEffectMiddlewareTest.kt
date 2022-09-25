@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.sideeffects

import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import com.episode6.redux.Action
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.*
import com.episode6.redux.testsupport.stoplight.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.transformLatest
import kotlin.test.Test

class SideEffectMiddlewareTest {

  private val timing = TimingController()

  private fun timedStore(scope: CoroutineScope) = scope.stopLightStore(timing)

  @Test fun testInitialValue() = runStoreTest(::timedStore) { store ->
    assertThat(store.state).hasDefaultLights()
  }

  @Test fun testInitialValue_flow() = runStoreTest(::timedStore) { store ->
    store.test {
      assertThat(awaitItem()).hasDefaultLights()
      ensureAllEventsConsumed()
    }
  }

  @Test fun testInitWithoutTime() = runStoreTest(::timedStore) { store ->
    store.dispatch(SwitchToGreen)

    assertThat(store.state).hasLights(green = true)
  }

  @Test fun testInitWithoutTime_flow() = runStoreTest(::timedStore) { store ->
    store.test {
      store.dispatch(SwitchToGreen)

      assertThat(awaitItems(2)).all {
        lastElement().hasLights(green = true)
      }
      ensureAllEventsConsumed()
    }
  }

  @Test fun testInitWithTime_flow() = runStoreTest(::timedStore) { store ->
    store.test {
      store.dispatch(SwitchToGreen)
      timing.advanceBy(GREEN_TO_YELLOW_DELAY)

      assertThat(awaitItems(3)).all {
        lastElement().hasLights(yellow = true)
      }

      timing.advanceBy(YELLOW_TO_RED_DELAY)
      assertThat(awaitItem()).hasLights(red = true)

      ensureAllEventsConsumed()
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
