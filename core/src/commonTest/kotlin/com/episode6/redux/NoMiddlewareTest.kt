package com.episode6.redux

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import com.episode6.redux.stoplight.*
import com.episode6.redux.testsupport.runFlowTest
import com.episode6.redux.testsupport.runTest
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test

class NoMiddlewareTest {

  @Test fun testValue_default() = runTest {
    val store = stopLightStore()

    assertThat(store.value).hasDefaultLights()
  }

  @Test fun testValue_switchLight() = runTest {
    val store = stopLightStore()

    store.dispatch(SetGreenLightOn(true))
    store.dispatch(SetRedLightOn(false))

    assertThat(store.value).hasLights(green = true)
  }

  @Test fun testFlow_default() = runFlowTest {
    val store = stopLightStore()

    store.test {
      assertThat(values).all {
        hasSize(1)
        index(0).hasDefaultLights()
      }
    }
  }

  @Test fun testFlow_switchLight() = runFlowTest {
    val store = stopLightStore()

    store.test {
      store.dispatch(SetYellowLightOn(true))
      store.dispatch(SetRedLightOn(false))

      assertThat(values).all {
        hasSize(3)
        index(0).hasDefaultLights()
        index(1).hasLights(red = true, yellow = true)
        index(2).hasLights(yellow = true)
      }
    }
  }
}

fun CoroutineScope.stopLightStore(): StoreFlow<StopLightState> = StoreFlow(
  initialValue = StopLightState(),
  reducer = StopLightState::reduce,
  scope = this
)
