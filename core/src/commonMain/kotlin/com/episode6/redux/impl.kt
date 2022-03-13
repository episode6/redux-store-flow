package com.episode6.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Creates a new [StoreFlow], a redux store backed by a [StateFlow]
 */
@Suppress("FunctionName") fun <State : Any?> StoreFlow(
  scope: CoroutineScope,
  initialValue: State,
  reducer: Reducer<State>,
  middlewares: List<Middleware<State>> = emptyList(),
): StoreFlow<State> = StoreFlowImpl(
  scope = scope,
  initialValue = initialValue,
  reducer = reducer,
  middlewares = middlewares,
)

private class StoreFlowImpl<T : Any?>(
  private val scope: CoroutineScope,
  override val initialValue: T,
  reducer: Reducer<T>,
  middlewares: List<Middleware<T>>,
  private val state: MutableStateFlow<T> = MutableStateFlow(initialValue)
) : StoreFlow<T>, StateFlow<T> by state {

  private val actionChannel: Channel<Action> = Channel()

  init {
    scope.launch {
      val reduce: Dispatch = middlewares.foldRight(
        initial = { action -> state.value = state.value.reducer(action) },
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

  override fun dispatch(action: Action) {
    scope.launch { actionChannel.send(action) }
  }
}
