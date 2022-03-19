package com.episode6.redux.subscriberaware

import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.Reducer
import com.episode6.redux.StoreFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

/**
 * An [Action] that is dispatched to a [StoreFlow] that is created with [SubscriberAwareStoreFlow]
 */
data class SubscriberStatusChanged(val subscribersActive: Boolean = false) : Action

/**
 * Creates a [StoreFlow] that dispatches [SubscriberStatusChanged] when subscribers start or stop
 * collecting from it. The actions will fire when the first subscriber starts and the last subscriber
 * stops collecting.
 */
@Suppress("FunctionName") fun <State : Any?> SubscriberAwareStoreFlow(
  scope: CoroutineScope,
  initialValue: State,
  reducer: Reducer<State>,
  middlewares: List<Middleware<State>> = emptyList(),
): StoreFlow<State> {

  val store = StoreFlow(scope = scope, initialValue = initialValue, reducer = reducer, middlewares = middlewares)

  val flow = store
    .drop(1) // since we have to emit onStart, drop the store's first emission
    .onStart { store.dispatch(SubscriberStatusChanged(true)) }
    .onCompletion { store.dispatch(SubscriberStatusChanged(false)) }
    .shareIn(scope, SharingStarted.WhileSubscribed(), replay = 0) // replay = stale emissions when SharingStarted.WhileSubscribed is used
    .onStart { emit(store.state) } // replacement for replay

  return object : StoreFlow<State>, Flow<State> by flow {
    override val initialState: State get() = store.initialState
    override val state: State get() = store.state
    override fun dispatch(action: Action) = store.dispatch(action)
  }
}
