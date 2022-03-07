package com.episode6.redux

import assertk.assertThat
import com.episode6.redux.stoplight.StopLightState
import com.episode6.redux.stoplight.hasOnlyRedLightOn
import com.episode6.redux.stoplight.reduce
import com.episode6.redux.testsupport.runTest
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test

class NoMiddlewareTest {

  @Test fun testDefault() = runTest {
    val store = stopLightStore()

    assertThat(store.value).hasOnlyRedLightOn()
  }
}

fun CoroutineScope.stopLightStore(): StoreFlow<StopLightState> = StoreFlow(
  initialValue = StopLightState(),
  reducer = StopLightState::reduce,
  scope = this
)
