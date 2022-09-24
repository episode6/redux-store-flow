package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> runUnconfinedStoreTest(storeBuilder: CoroutineScope.() -> T, testBody: suspend FlowTestScope.(T) -> Unit) =
  runUnconfinedTest {
    val manager = storeManager(storeBuilder)
    flowTestScope { testBody(manager.store()) }
    manager.shutdown()
  }

fun <T> CoroutineScope.storeManager(builder: CoroutineScope.() -> T): StoreManager<T> = StoreManagerImpl(this, builder)

interface StoreManager<T> {
  suspend fun store(): T
  fun shutdown()
}

private class StoreManagerImpl<T>(scope: CoroutineScope, builder: CoroutineScope.() -> T) : StoreManager<T> {
  private val state = MutableStateFlow<T?>(null)
  private val job = scope.launch { state.value = builder() }

  override suspend fun store() = state.filterNotNull().first()
  override fun shutdown() = job.cancel()
}
