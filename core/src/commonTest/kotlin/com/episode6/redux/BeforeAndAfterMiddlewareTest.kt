package com.episode6.redux

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.episode6.redux.testsupport.runTest
import com.episode6.redux.testsupport.stoplight.*
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test


class BeforeAndAfterMiddlewareTest {
  private fun CoroutineScope.stopLightStore(vararg middlewares: Middleware<StopLightState>): StoreFlow<StopLightState> =
    createStopLightStore(*middlewares)

  @Test fun testProcessAction() = runTest {
    val middleware = TestMiddleware()
    val store = stopLightStore(middleware)
    val action = SetRedLightOn(false)

    store.dispatch(action)

    verifyOrder {
      middleware.before.captureState(any())
      middleware.after.captureState(any())
    }
    confirmVerified(middleware.before, middleware.after)
    val (beforeState, beforeAction) = middleware.beforeSlot.first()
    val (afterState, afterAction) = middleware.afterSlot.first()
    assertAll {
      assertThat(beforeState).hasDefaultLights()
      assertThat(afterState).hasLights()
      assertThat(beforeAction).isEqualTo(action)
      assertThat(afterAction).isEqualTo(action)
    }
  }

  @Test fun testProcessAction_twoMiddlewares() = runTest {
    val middleware1 = TestMiddleware()
    val middleware2 = TestMiddleware()
    val store = stopLightStore(middleware1, middleware2)
    val action = SetRedLightOn(false)

    store.dispatch(action)

    verifyOrder {
      middleware1.before.captureState(any())
      middleware2.before.captureState(any())
      middleware2.after.captureState(any())
      middleware1.after.captureState(any())
    }
    confirmVerified(middleware1.before, middleware1.after, middleware2.before, middleware2.after)
    val (beforeState1, beforeAction1) = middleware1.beforeSlot.first()
    val (afterState1, afterAction1) = middleware1.afterSlot.first()
    val (beforeState2, beforeAction2) = middleware2.beforeSlot.first()
    val (afterState2, afterAction2) = middleware2.afterSlot.first()
    assertAll {
      assertThat(beforeState1).hasDefaultLights()
      assertThat(afterState1).hasLights()
      assertThat(beforeAction1).isEqualTo(action)
      assertThat(afterAction1).isEqualTo(action)
      assertThat(beforeState2).hasDefaultLights()
      assertThat(afterState2).hasLights()
      assertThat(beforeAction2).isEqualTo(action)
      assertThat(afterAction2).isEqualTo(action)
    }
  }
}

private data class StateAndAction(val state: StopLightState, val action: Action)

private interface CaptureMiddlewareState {
  fun captureState(stateAndAction: StateAndAction)
}

private class TestMiddleware : Middleware<StopLightState> {
  val beforeSlot = mutableListOf<StateAndAction>()
  val afterSlot = mutableListOf<StateAndAction>()
  val before = mockk<CaptureMiddlewareState> {
    every { captureState(capture(beforeSlot)) } answers {}
  }
  val after = mockk<CaptureMiddlewareState> {
    every { captureState(capture(afterSlot)) } answers {}
  }

  override fun CoroutineScope.interfere(store: StoreFlow<StopLightState>, next: Dispatch): Dispatch {
    return {
      before.captureState(StateAndAction(store.value, it))
      next(it)
      after.captureState(StateAndAction(store.value, it))
    }
  }
}
