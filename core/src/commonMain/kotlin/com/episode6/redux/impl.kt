package com.episode6.redux

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Creates a new [StoreFlow], a redux store backed by a [StateFlow]
 */
@Suppress("FunctionName") fun <State : Any?> StoreFlow(
  initialValue: State,
  reducer: Reducer<State>,
  middlewares: List<Middleware<State>> = emptyList(),
  scope: CoroutineScope = MainScope() + Dispatchers.Default,
): StoreFlow<State> = StoreFlowImpl(initialValue, reducer, middlewares, scope)

private class StoreFlowImpl<T : Any?>(
  override val initialValue: T,
  reducer: Reducer<T>,
  middlewares: List<Middleware<T>>,
  override val scope: CoroutineScope,
  private val delegate: MutableStateFlow<T> = MutableStateFlow(initialValue)
) : StoreFlow<T>, StateFlow<T> by delegate {

  private val actionChannel: Channel<Action> = Channel()

  init {
    scope.launch {
      val reduce: Dispatch = middlewares.foldRight(
        initial = { action -> delegate.value = delegate.value.reducer(action) },
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

  override val dispatch: Dispatch = {
    scope.launch { actionChannel.send(it) }
  }
}
