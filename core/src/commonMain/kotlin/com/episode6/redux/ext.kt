package com.episode6.redux

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

fun <T, R> StoreFlow<T>.mapStore(mapper: (T) -> R): StoreFlow<R> {
  val newFlow = map { mapper(it) }.distinctUntilChanged()
  return object : StoreFlow<R>, Flow<R> by newFlow {
    override val initialState: R get() = mapper(this@mapStore.initialState)
    override val state: R get() = mapper(this@mapStore.state)
    override fun dispatch(action: Action) = this@mapStore.dispatch(action)
  }
}
