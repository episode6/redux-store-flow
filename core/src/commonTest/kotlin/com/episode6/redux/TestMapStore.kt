package com.episode6.redux

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isTrue
import com.episode6.redux.testsupport.FlowTestScope
import com.episode6.redux.testsupport.runUnconfinedStoreTest
import com.episode6.redux.testsupport.stoplight.*
import kotlin.test.Test

class TestMapStore {
  private fun storeTest(testBody: suspend FlowTestScope.(StoreFlow<StopLightState>) -> Unit) = runUnconfinedStoreTest(
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
      assertThat(values).containsExactly(true)
    }
  }

  @Test fun testDispatchValueChanged() = storeTest { backingStore ->
    val store: StoreFlow<Boolean> = backingStore.mapStore { it.redLight }

    store.test {
      store.dispatch(SetRedLightOn(false))
      store.dispatch(SetRedLightOn(false)) // dupes all ignored
      backingStore.dispatch(SetRedLightOn(false))

      assertThat(values).containsExactly(true, false)
    }
  }

  @Test fun testDispatchValueChanged_testCollector() = storeTest { backingStore ->
    val store: StoreFlow<Boolean> = backingStore.mapStore { it.redLight }
    val backingStoreCollector = backingStore.testCollector()
    val storeCollector = store.testCollector()

    store.dispatch(SetRedLightOn(false))
    store.dispatch(SetRedLightOn(false)) // dupes all ignored
    backingStore.dispatch(SetRedLightOn(false))

    // verify both stores have same values and same number of values
    assertThat(storeCollector.values).containsExactly(true, false)
    assertThat(backingStoreCollector.values).all {
      hasSize(2)
      index(0).hasDefaultLights()
      index(1).hasLights()
    }

    storeCollector.stopCollecting()
    backingStoreCollector.stopCollecting()
  }
}
