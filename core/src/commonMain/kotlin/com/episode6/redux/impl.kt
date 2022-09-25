package com.episode6.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Creates a new [StoreFlow], a redux store backed by a [kotlinx.coroutines.flow.StateFlow]
 */
@Suppress("FunctionName") public fun <State : Any?> StoreFlow(
  scope: CoroutineScope,
  initialValue: State,
  reducer: Reducer<State>,
  middlewares: List<Middleware<State>> = emptyList(),
): StoreFlow<State> = StoreFlowImpl(
  scope = scope,
  initialState = initialValue,
  reducer = reducer,
  middlewares = middlewares,
)

private class StoreFlowImpl<T : Any?>(
  scope: CoroutineScope,
  override val initialState: T,
  reducer: Reducer<T>,
  middlewares: List<Middleware<T>>,
  private val stateFlow: MutableStateFlow<T> = MutableStateFlow(initialState)
) : StoreFlow<T>, Flow<T> by stateFlow {

  private val actionChannel: Channel<Action> = Channel(capacity = UNLIMITED)

  init {
    scope.launch {
      val reduce: Dispatch = middlewares.foldRight(
        initial = { action -> stateFlow.value = stateFlow.value.reducer(action) },
        operation = { middleware, next -> with(middleware) { interfere(this@StoreFlowImpl, next) } }
      )
      try {
        for (action in actionChannel) {
          reduce(action)
        }
      } finally {
        actionChannel.close()
      }
    }
  }

  override val state: T get() = stateFlow.value

  override fun dispatch(action: Action) {
    actionChannel.trySend(action)
  }
}
