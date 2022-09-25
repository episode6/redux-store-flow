package com.episode6.redux

import app.cash.turbine.test
import app.cash.turbine.testIn
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.index
import assertk.assertions.isTrue
import com.episode6.redux.testsupport.awaitItems
import com.episode6.redux.testsupport.runUnconfinedStoreTest
import com.episode6.redux.testsupport.stoplight.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test

class TestMapStore {
  private fun storeTest(testBody: suspend TestScope.(StoreFlow<StopLightState>) -> Unit) = runUnconfinedStoreTest(
    storeBuilder = { createStopLightStore() },
    testBody = testBody
  )

  @Test fun testMapValueRead() = storeTest { backingStore ->
    val store: StoreFlow<Boolean> = backingStore.mapStore { it.redLight }

    assertThat(store.state).isTrue()
  }

  @Test fun testMapValueRead_flow() = storeTest { backingStore ->
    val store: StoreFlow<Boolean> = backingStore.mapStore { it.redLight }

    store.test {
      assertThat(awaitItem()).isTrue()
      expectNoEvents()
    }
  }

  @Test fun testDispatchValueChanged() = storeTest { backingStore ->
    val store: StoreFlow<Boolean> = backingStore.mapStore { it.redLight }

    store.test {
      store.dispatch(SetRedLightOn(false))
      store.dispatch(SetRedLightOn(false)) // dupes all ignored
      backingStore.dispatch(SetRedLightOn(false))

      assertThat(awaitItems(2)).containsExactly(true, false)
      expectNoEvents()
    }
  }

  @Test fun testDispatchValueChanged_testCollector() = storeTest { backingStore ->
    val store: StoreFlow<Boolean> = backingStore.mapStore { it.redLight }
    val backingStoreCollector = backingStore.testIn(this)
    val storeCollector = store.testIn(this)

    store.dispatch(SetRedLightOn(false))
    store.dispatch(SetRedLightOn(false)) // dupes all ignored
    backingStore.dispatch(SetRedLightOn(false))

    // verify both stores have same values and same number of values
    assertThat(storeCollector.awaitItems(2)).containsExactly(true, false)
    assertThat(backingStoreCollector.awaitItems(2)).all {
      index(0).hasDefaultLights()
      index(1).hasLights()
    }
    storeCollector.expectNoEvents()
    backingStoreCollector.expectNoEvents()

    storeCollector.cancel()
    backingStoreCollector.cancel()
  }
}
