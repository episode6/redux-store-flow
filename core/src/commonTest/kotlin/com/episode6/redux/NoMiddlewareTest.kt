package com.episode6.redux

import assertk.assertThat
import com.episode6.redux.stoplight.*
import com.episode6.redux.testsupport.runTest
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test

class NoMiddlewareTest {

  @Test fun testValue_default() = runTest {
    val store = stopLightStore()

    assertThat(store.value).hasLights(red = true)
  }

  @Test fun testValue_switchLight() = runTest {
    val store = stopLightStore()

    store.dispatch(SetGreenLightOn(true))
    store.dispatch(SetRedLightOn(false))

    assertThat(store.value).hasLights(green = true)
  }
}

fun CoroutineScope.stopLightStore(): StoreFlow<StopLightState> = StoreFlow(
  initialValue = StopLightState(),
  reducer = StopLightState::reduce,
  scope = this
)
