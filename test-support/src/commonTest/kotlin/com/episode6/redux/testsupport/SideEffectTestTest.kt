@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.testsupport

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.episode6.redux.Action
import com.episode6.redux.sideeffects.SideEffect
import com.episode6.redux.testsupport.internal.stoplight.SetGreenLightOn
import com.episode6.redux.testsupport.internal.stoplight.SetRedLightOn
import com.episode6.redux.testsupport.internal.stoplight.SetYellowLightOn
import com.episode6.redux.testsupport.internal.stoplight.StopLightState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SideEffectTestTest {

  val context = SideEffectTestContext(StopLightState())

  @Test fun testSampleSideEffect_noOpWhenLightsOff() = runTest {
    val output: Flow<Action> = sampleSideEffect().testOutput(context)

    output.test {
      context.activeState = StopLightState(redLight = false)
      context.actionsFlow.emit(SetGreenLightOn(true))

      ensureAllEventsConsumed() // no events
    }
  }

  @Test fun testSampleSideEffect_turnOffRedLight() = runTest {
    val output: Flow<Action> = sampleSideEffect().testOutput(context)

    output.test {
      context.activeState = StopLightState(redLight = true)
      context.actionsFlow.emit(SetGreenLightOn(true))

      assertThat(awaitItem()).isEqualTo(SetRedLightOn(false))
      ensureAllEventsConsumed()
    }
  }

  @Test fun testSampleSideEffect_turnOffRedAndYellowLight() = runTest {
    val output: Flow<Action> = sampleSideEffect().testOutput(context)

    output.test {
      context.activeState = StopLightState(redLight = true, yellowLight = true)
      context.actionsFlow.emit(SetGreenLightOn(true))

      assertThat(awaitItem()).isEqualTo(SetRedLightOn(false))
      assertThat(awaitItem()).isEqualTo(SetYellowLightOn(false))
      ensureAllEventsConsumed()
    }
  }
}

private fun sampleSideEffect() = SideEffect<StopLightState> {
  actions.filterIsInstance<SetGreenLightOn>().transformLatest {
    val state = currentState()
    if (it.on) {
      if (state.redLight) emit(SetRedLightOn(false))
      if (state.yellowLight) emit(SetYellowLightOn(false))
    }
  }
}
