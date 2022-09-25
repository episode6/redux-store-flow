@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux

import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.index
import com.episode6.redux.testsupport.awaitItems
import com.episode6.redux.testsupport.runStoreTest
import com.episode6.redux.testsupport.stoplight.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test

class NoMiddlewareTest {

  private fun storeTest(testBody: suspend TestScope.(StoreFlow<StopLightState>) -> Unit) = runStoreTest(
    storeBuilder = CoroutineScope::createStopLightStore,
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
      assertThat(awaitItem()).hasDefaultLights()
      ensureAllEventsConsumed()
    }
  }

  @Test fun testFlow_switchLight() = storeTest { store ->

    store.test {
      store.dispatch(SetYellowLightOn(true))
      store.dispatch(SetRedLightOn(false))

      assertThat(awaitItems(3)).all {
        index(0).hasDefaultLights()
        index(1).hasLights(red = true, yellow = true)
        index(2).hasLights(yellow = true)
      }
      ensureAllEventsConsumed()
    }
  }
}
