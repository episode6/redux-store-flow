package com.episode6.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

fun <T, R> StoreFlow<T>.mapStore(mapper: (T)->R): StoreFlow<R> {
  val newInitialValue = mapper(initialValue)
  val flow = map { mapper(it) }.stateIn(scope, SharingStarted.WhileSubscribed(), newInitialValue)
  return object : StoreFlow<R>, StateFlow<R> by flow {
    override val scope: CoroutineScope get() = this@mapStore.scope
    override val initialValue: R get() = newInitialValue
    override fun dispatch(action: Action) = this@mapStore.dispatch(action)
  }
}
