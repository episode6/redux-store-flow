@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux

import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.index
import com.episode6.redux.testsupport.awaitItems
import com.episode6.redux.testsupport.stoplight.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test

class NoMiddlewareTest {

  @Test fun testValue_default() = stopLightStoreTest { store ->
    assertThat(store.state).hasDefaultLights()
  }

  @Test fun testValue_switchLight() = stopLightStoreTest { store ->

    store.dispatch(SetGreenLightOn(true))
    store.dispatch(SetRedLightOn(false))

    assertThat(store.state).hasLights(green = true)
  }

  @Test fun testFlow_default() = stopLightStoreTest { store ->

    store.test {
      assertThat(awaitItem()).hasDefaultLights()
      ensureAllEventsConsumed()
    }
  }

  @Test fun testFlow_switchLight() = stopLightStoreTest { store ->

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
