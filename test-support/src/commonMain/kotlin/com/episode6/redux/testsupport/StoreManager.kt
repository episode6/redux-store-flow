package com.episode6.redux.testsupport

import com.episode6.redux.StoreFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.coroutines.CoroutineContext

/**
 * A wrapper around a [StoreFlow] that allows it to be shut down at the end of a test
 */
public interface StoreManager<T> {
  public suspend fun store(): StoreFlow<T>
  public fun shutdown()
}

/**
 * Returns a [StoreManager] that wraps an instance of the [StoreFlow] created in [storeBuilder]
 */
@Suppress("FunctionName") // creator method for [StoreManager]
@ExperimentalCoroutinesApi
public fun <T> TestScope.StoreManager(
  context: CoroutineContext = UnconfinedTestDispatcher(),
  storeBuilder: CoroutineScope.() -> StoreFlow<T>
): StoreManager<T> = StoreManagerImpl(this + context, storeBuilder)

private class StoreManagerImpl<T>(
  scope: CoroutineScope,
  builder: CoroutineScope.() -> StoreFlow<T>
) : StoreManager<T> {
  private val state = MutableStateFlow<StoreFlow<T>?>(null)
  private val job = scope.launch { state.value = builder() }

  override suspend fun store() = state.filterNotNull().first()
  override fun shutdown() = job.cancel()
}
