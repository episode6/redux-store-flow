@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.sideeffects

import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import com.episode6.redux.Action
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.internal.TimingController
import com.episode6.redux.testsupport.internal.awaitItems
import com.episode6.redux.testsupport.internal.lastElement
import com.episode6.redux.testsupport.internal.stoplight.*
import com.episode6.redux.testsupport.runStoreTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.transform
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

  // regression: an observe-only side-effect (one that never touches `actions`) must not
  // prevent other side-effects from receiving actions
  @Test fun testObserveOnlyEffect_doesNotStarveOtherEffects() {
    val externalActions = MutableSharedFlow<Action>()
    runStoreTest({
      createStopLightStore(
        SideEffectMiddleware(
          SideEffect { externalActions }, // observe-only: ignores actions entirely
          SideEffect {
            actions.filterIsInstance<SwitchToGreen>().transform { emitLights(green = true) }
          },
        )
      )
    }) { store ->
      store.dispatch(SwitchToGreen)

      assertThat(store.state).hasLights(green = true)
    }
  }

  // regression: an observe-only side-effect's output is still dispatched back into the store
  @Test fun testObserveOnlyEffect_outputIsDispatched() {
    val externalActions = MutableSharedFlow<Action>()
    runStoreTest({
      createStopLightStore(
        SideEffectMiddleware(
          SideEffect { externalActions },
        )
      )
    }) { store ->
      externalActions.emit(SetGreenLightOn(true))

      assertThat(store.state).hasLights(green = true, red = true)
    }
  }

  // regression: a side-effect that suspends inline in its collect path must not
  // block delivery of subsequent actions to other side-effects
  @Test fun testSuspendingEffect_doesNotStallOtherEffects() = runStoreTest({
    createStopLightStore(
      SideEffectMiddleware(
        SideEffect {
          actions.filterIsInstance<SwitchToYellow>().transform {
            timing.await(SLOW_EFFECT_DELAY) // suspends inline while processing
            emitLights(yellow = true)
          }
        },
        SideEffect {
          actions.filterIsInstance<SwitchToGreen>().transform { emitLights(green = true) }
        },
      )
    )
  }) { store ->
    store.dispatch(SwitchToYellow) // slow effect is now suspended mid-processing
    store.dispatch(SwitchToGreen) // fast effect should receive this immediately

    assertThat(store.state).hasLights(green = true)

    timing.advanceBy(SLOW_EFFECT_DELAY)
    assertThat(store.state).hasLights(yellow = true)
  }

  // regression: actions dispatched while a side-effect is busy are buffered and
  // delivered to it in dispatch order once it resumes
  @Test fun testSuspendingEffect_processesItsOwnQueueInOrder() = runStoreTest({
    createStopLightStore(
      SideEffectMiddleware(
        SideEffect {
          actions.transform { action ->
            when (action) {
              SwitchToGreen -> { timing.await(SLOW_EFFECT_DELAY); emitLights(green = true) }
              SwitchToYellow -> { timing.await(SLOW_EFFECT_DELAY); emitLights(yellow = true) }
              SwitchToRed -> { timing.await(SLOW_EFFECT_DELAY); emitLights(red = true) }
              else -> Unit
            }
          }
        },
      )
    )
  }) { store ->
    store.test {
      assertThat(awaitItem()).hasDefaultLights()

      store.dispatch(SwitchToGreen)
      store.dispatch(SwitchToYellow)
      store.dispatch(SwitchToRed)

      timing.advanceBy(SLOW_EFFECT_DELAY)
      assertThat(awaitItem()).hasLights(green = true)

      timing.advanceBy(SLOW_EFFECT_DELAY)
      assertThat(awaitItem()).hasLights(yellow = true)

      timing.advanceBy(SLOW_EFFECT_DELAY)
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
private const val SLOW_EFFECT_DELAY = 100L

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
