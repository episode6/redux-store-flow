package com.episode6.redux.sideeffects

import com.episode6.redux.Action
import com.episode6.redux.Middleware
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Returns a [Middleware] that passes actions to the supplied [sideEffects], then dispatches
 * their returned [Action]s back into the [StoreFlow]
 */
@Suppress("FunctionName")
public fun <State : Any?> SideEffectMiddleware(vararg sideEffects: SideEffect<State>): Middleware<State> =
  SideEffectMiddleware(sideEffects.toList())


/**
 * Returns a [Middleware] that passes actions to the supplied [sideEffects], then dispatches
 * their returned [Action]s back into the [StoreFlow]
 */
@Suppress("FunctionName")
public fun <State : Any?> SideEffectMiddleware(sideEffects: Collection<SideEffect<State>>): Middleware<State> =
  Middleware { store, next ->
    val sideEffectCount = sideEffects.size
    val actionRelay = MutableSharedFlow<Action>()
    val context = SideEffectContextImpl(actionRelay.asSharedFlow(), store)
    sideEffects.forEach {
      launch { it.actWith(context).collect(store::dispatch) }
    }

    return@Middleware { action ->
      next(action)
      launch {
        actionRelay.awaitSubscriptionCount(sideEffectCount)
        actionRelay.emit(action)
      }
    }
  }

private class SideEffectContextImpl<State>(
  override val actions: Flow<Action>,
  private val state: Flow<State>,
) : SideEffectContext<State> {
  override suspend fun currentState(): State = state.first()
}

private fun <State> SideEffect<State>.actWith(context: SideEffectContext<State>): Flow<Action> = with(context) { act() }
private suspend fun MutableSharedFlow<*>.awaitSubscriptionCount(count: Int) {
  subscriptionCount.filter { it == count }.first()
}
