package com.episode6.redux.subscriberaware

import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.Reducer
import com.episode6.redux.StoreFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

data class SubscriberStatusChanged(val subscribersActive: Boolean = false) : Action

fun <State: Any?> SubscriberAwareStoreFlow(
  scope: CoroutineScope,
  initialValue: State,
  reducer: Reducer<State>,
  middlewares: List<Middleware<State>> = emptyList(),
): StoreFlow<State> {
  val store = StoreFlow(scope = scope, initialValue = initialValue, reducer = reducer, middlewares = middlewares)
  val flow = store
    .onStart { store.dispatch(SubscriberStatusChanged(true)) }
    .onCompletion { store.dispatch(SubscriberStatusChanged(false))  }
    .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 1)

  return object : StoreFlow<State>, Flow<State> by flow {
    override val initialState: State get() = store.initialState
    override val state: State get() = store.state
    override fun dispatch(action: Action) = store.dispatch(action)
  }
}
