package com.episode6.redux

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isTrue
import com.episode6.redux.testsupport.runFlowTest
import com.episode6.redux.testsupport.runTest
import com.episode6.redux.testsupport.stoplight.*
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test

class TestMapStore {
  private fun CoroutineScope.stopLightStore(): StoreFlow<StopLightState> = createStopLightStore()

  @Test fun testMapValueRead() = runTest {
    val store: StoreFlow<Boolean> = stopLightStore().mapStore { it.redLight }

    assertThat(store.value).isTrue()
  }

  @Test fun testMapValueRead_flow() = runFlowTest {
    val store: StoreFlow<Boolean> = stopLightStore().mapStore { it.redLight }

    store.test {
      assertThat(values).containsExactly(true)
    }
  }

  @Test fun testDispatchValueChanged() = runFlowTest {
    val backingStore = stopLightStore()
    val store: StoreFlow<Boolean> = backingStore.mapStore { it.redLight }

    store.test {
      store.dispatch(SetRedLightOn(false))
      store.dispatch(SetRedLightOn(false)) // dupes all ignored
      backingStore.dispatch(SetRedLightOn(false))

      assertThat(values).containsExactly(true, false)
    }
  }

  @Test fun testDispatchValueChanged_testCollector() = runFlowTest {
    val backingStore = stopLightStore()
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
  }
}
