package com.episode6.redux

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.index
import com.episode6.redux.testsupport.FlowTestScope
import com.episode6.redux.testsupport.runUnconfinedStoreTest
import com.episode6.redux.testsupport.stoplight.*
import kotlin.test.Test

class NoMiddlewareTest {

  private fun storeTest(testBody: suspend FlowTestScope.(StoreFlow<StopLightState>) -> Unit) = runUnconfinedStoreTest(
    storeBuilder = { createStopLightStore() },
    testBody = testBody
  )

  @Test fun testValue_default() = storeTest { store ->

    assertThat(store.state).hasDefaultLights()
  }

  @Test fun testValue_switchLight() = storeTest { store ->

    store.dispatch(SetGreenLightOn(true))
    store.dispatch(SetRedLightOn(false))

    assertThat(store.state).hasLights(green = true)
  }

  @Test fun testFlow_default() = storeTest { store ->

    store.test {
      assertThat(values).all {
        hasSize(1)
        index(0).hasDefaultLights()
      }
    }
  }

  @Test fun testFlow_switchLight() = storeTest { store ->

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
