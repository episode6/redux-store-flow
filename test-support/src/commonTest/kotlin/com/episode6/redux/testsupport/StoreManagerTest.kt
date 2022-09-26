@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.testsupport

import assertk.assertThat
import com.episode6.redux.testsupport.internal.stoplight.SetGreenLightOn
import com.episode6.redux.testsupport.internal.stoplight.SetRedLightOn
import com.episode6.redux.testsupport.internal.stoplight.createStopLightStore
import com.episode6.redux.testsupport.internal.stoplight.hasLights
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StoreManagerTest {

  @Test fun testSimpleStore_manager() = runTest {
    val storeManager = StoreManager { createStopLightStore() }
    val store = storeManager.store()

    store.dispatch(SetRedLightOn(false))
    store.dispatch(SetGreenLightOn(true))

    assertThat(store.state).hasLights(green = true)

    storeManager.shutdown()
  }

  @Test fun testSimpleStore_runStoreTest() = runStoreTest(CoroutineScope::createStopLightStore) { store ->
    store.dispatch(SetRedLightOn(false))
    store.dispatch(SetGreenLightOn(true))

    assertThat(store.state).hasLights(green = true)
  }
}
