@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext

public fun <T> runStoreTest(storeBuilder: CoroutineScope.() -> T, testBody: suspend TestScope.(T) -> Unit): TestResult =
  runTest {
    val manager = storeManager(storeBuilder = storeBuilder)
    testBody(manager.store())
    manager.shutdown()
  }

public fun <T> TestScope.storeManager(
  context: CoroutineContext = UnconfinedTestDispatcher(),
  storeBuilder: CoroutineScope.() -> T
): StoreManager<T> = StoreManagerImpl(this + context, storeBuilder)

public interface StoreManager<T> {
  public suspend fun store(): T
  public fun shutdown()
}

private class StoreManagerImpl<T>(scope: CoroutineScope, builder: CoroutineScope.() -> T) : StoreManager<T> {
  private val state = MutableStateFlow<T?>(null)
  private val job = scope.launch { state.value = builder() }

  override suspend fun store() = state.filterNotNull().first()
  override fun shutdown() = job.cancel()
}
