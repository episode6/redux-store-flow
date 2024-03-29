package com.episode6.redux.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.episode6.redux.StoreFlow
import com.episode6.redux.mapStore
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Collects values from this [StoreFlow] and represents its latest value via [State].
 */
@Composable public fun <T> StoreFlow<T>.collectAsState(
  context: CoroutineContext = EmptyCoroutineContext
): State<T> = collectAsState(initial = initialState, context = context)

/**
 * Map this [StoreFlow] before collecting values from it.
 */
@Composable public fun <IN, OUT> StoreFlow<IN>.collectAsState(
  context: CoroutineContext = EmptyCoroutineContext,
  mapper: (IN) -> OUT
): State<OUT> {
  val store = remember { mapStore(mapper) }
  return store.collectAsState(context = context)
}
