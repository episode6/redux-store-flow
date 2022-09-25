@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

fun <T> runUnconfinedStoreTest(storeBuilder: CoroutineScope.() -> T, testBody: suspend TestScope.(T) -> Unit) =
  runUnconfinedTest {
    val manager = storeManager(storeBuilder)
    testBody(manager.store())
    manager.shutdown()
  }

fun <T> TestScope.storeManager(builder: CoroutineScope.() -> T): StoreManager<T> = StoreManagerImpl(this, builder)

interface StoreManager<T> {
  suspend fun store(): T
  fun shutdown()
}

private class StoreManagerImpl<T>(scope: CoroutineScope, builder: CoroutineScope.() -> T) : StoreManager<T> {
  private val state = MutableStateFlow<T?>(null)
  private val job = scope.launch(UnconfinedTestDispatcher()) { state.value = builder() }

  override suspend fun store() = state.filterNotNull().first()
  override fun shutdown() = job.cancel()
}
