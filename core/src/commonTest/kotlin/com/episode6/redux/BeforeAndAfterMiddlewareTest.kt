package com.episode6.redux

import assertk.all
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.episode6.redux.stoplight.*
import com.episode6.redux.testsupport.runTest
import io.mockk.*
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test

private typealias StateAndAction = Pair<StopLightState, Action>
private interface CaptureState {
  fun capture(stateAndAction: StateAndAction)
}

class BeforeAndAfterMiddlewareTest {
  private val beforeSlot = slot<StateAndAction>()
  private val afterSlot = slot<StateAndAction>()
  private val before: CaptureState = mockk {
    every { capture(capture(beforeSlot)) } answers { }
  }
  private val after: CaptureState = mockk {
    every { capture(capture(afterSlot)) } answers { }
  }
  private val middleware: Middleware<StopLightState> = Middleware { store, next ->
    { action ->
      before.capture(store.value to action)
      next(action)
      after.capture(store.value to action)
    }
  }

  private fun CoroutineScope.stopLightStore(): StoreFlow<StopLightState> = createStopLightStore(listOf(middleware))

  @Test fun testProcessAction() = runTest {
    val store = stopLightStore()
    val action = SetRedLightOn(false)

    store.dispatch(action)

    verifyOrder {
      before.capture(any())
      after.capture(any())
    }
    confirmVerified(before, after)
    val (beforeState, beforeAction) = beforeSlot.captured
    val (afterState, afterAction) = afterSlot.captured
    assertAll {
      assertThat(beforeState).hasDefaultLights()
      assertThat(afterState).hasLights()
      assertThat(beforeAction).isEqualTo(action)
      assertThat(afterAction).isEqualTo(action)
    }
  }
}
