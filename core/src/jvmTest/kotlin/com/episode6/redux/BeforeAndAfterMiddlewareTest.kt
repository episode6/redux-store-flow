package com.episode6.redux

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.episode6.redux.testsupport.FlowTestScope
import com.episode6.redux.testsupport.runUnconfinedStoreTest
import com.episode6.redux.testsupport.stoplight.*
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test


class BeforeAndAfterMiddlewareTest {

  private fun storeTest(
    middlewares: List<Middleware<StopLightState>>,
    testBody: suspend TestScope.(StoreFlow<StopLightState>) -> Unit
  ) = runUnconfinedStoreTest(
    storeBuilder = { createStopLightStore(*middlewares.toTypedArray()) },
    testBody = testBody
  )

  @Test fun testProcessAction() {
    val middleware = TestMiddleware()
    storeTest(listOf(middleware)) { store ->
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
  }

  @Test fun testProcessAction_twoMiddlewares() {
    val middlewares = listOf(TestMiddleware(), TestMiddleware())
    storeTest(middlewares) { store ->
      val action = SetRedLightOn(false)

      store.dispatch(action)

      verifyOrder {
        middlewares[0].before.captureState(any())
        middlewares[1].before.captureState(any())
        middlewares[1].after.captureState(any())
        middlewares[0].after.captureState(any())
      }
      confirmVerified(middlewares[0].before, middlewares[0].after, middlewares[1].before, middlewares[1].after)
      val (beforeState1, beforeAction1) = middlewares[0].beforeSlot.first()
      val (afterState1, afterAction1) = middlewares[0].afterSlot.first()
      val (beforeState2, beforeAction2) = middlewares[1].beforeSlot.first()
      val (afterState2, afterAction2) = middlewares[1].afterSlot.first()
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
      before.captureState(StateAndAction(store.state, it))
      next(it)
      after.captureState(StateAndAction(store.state, it))
    }
  }
}
