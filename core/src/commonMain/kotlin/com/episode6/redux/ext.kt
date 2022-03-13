package com.episode6.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

fun <T, R> StoreFlow<T>.mapStore(mapper: (T)->R): StoreFlow<R> {
  val newInitialValue = mapper(initialValue)
  val newFlow = map { mapper(it) }.distinctUntilChanged()
  return object : StoreFlow<R>, Flow<R> by newFlow {
    override val scope: CoroutineScope get() = this@mapStore.scope
    override val initialValue: R get() = newInitialValue
    override fun dispatch(action: Action) = this@mapStore.dispatch(action)
    override val replayCache: List<R> get() = this@mapStore.replayCache.map(mapper)
    override val value: R get() = mapper(this@mapStore.value)
  }
}
