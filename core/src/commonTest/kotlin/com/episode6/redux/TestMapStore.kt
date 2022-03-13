package com.episode6.redux

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.index
import assertk.assertions.isTrue
import com.episode6.redux.testsupport.runFlowTest
import com.episode6.redux.testsupport.runTest
import com.episode6.redux.testsupport.stoplight.SetRedLightOn
import com.episode6.redux.testsupport.stoplight.StopLightState
import com.episode6.redux.testsupport.stoplight.createStopLightStore
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
}
