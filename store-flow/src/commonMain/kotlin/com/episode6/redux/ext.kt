package com.episode6.redux

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Maps a [StoreFlow] that emits a state of type [T] to one that
 * emits a state of type [R], by applying the given [mapper] to
 * its emissions.
 *
 * The only difference will be the state that is emitted. Any actions
 * dispatched to the resulting [StoreFlow] will be passed directly back
 * to the receiver.
 */
public fun <T, R> StoreFlow<T>.mapStore(mapper: (T) -> R): StoreFlow<R> {
  val newFlow = map { mapper(it) }.distinctUntilChanged()
  return object : StoreFlow<R>, Flow<R> by newFlow {
    override val initialState: R get() = mapper(this@mapStore.initialState)
    override val state: R get() = mapper(this@mapStore.state)
    override fun dispatch(action: Action) = this@mapStore.dispatch(action)
  }
}
