package com.episode6.redux.sideeffects

import com.episode6.redux.Action
import com.episode6.redux.Middleware
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Returns a [Middleware] that passes actions to the supplied [sideEffects], then dispatches
 * their returned [Action]s back into the [com.episode6.redux.StoreFlow]
 */
@Suppress("FunctionName")
public fun <State : Any?> SideEffectMiddleware(vararg sideEffects: SideEffect<State>): Middleware<State> =
  SideEffectMiddleware(sideEffects.toList())


/**
 * Returns a [Middleware] that passes actions to the supplied [sideEffects], then dispatches
 * their returned [Action]s back into the [com.episode6.redux.StoreFlow]
 *
 * Each [SideEffect]'s [SideEffect.act] is invoked synchronously while the store is being set up,
 * before the store processes its first action; only the collection of the returned [Flow] is
 * launched asynchronously. Each access of [SideEffectContext.actions] registers its own unlimited
 * buffer of dispatched actions, so a side-effect that suspends while processing an action only
 * delays its own queue, and a side-effect that never reads [SideEffectContext.actions] simply
 * opts out of receiving actions without affecting the other side-effects.
 */
@Suppress("FunctionName")
public fun <State : Any?> SideEffectMiddleware(sideEffects: Collection<SideEffect<State>>): Middleware<State> =
  Middleware { store, next ->
    val buffers = MutableStateFlow<List<Channel<Action>>>(emptyList())
    val context = SideEffectContextImpl(buffers, store)
    sideEffects.forEach { sideEffect ->
      val actions = sideEffect.actWith(context)
      launch { actions.collect(store::dispatch) }
    }

    return@Middleware { action ->
      next(action)
      buffers.value.forEach { it.trySend(action) }
    }
  }

private class SideEffectContextImpl<State>(
  private val buffers: MutableStateFlow<List<Channel<Action>>>,
  private val state: Flow<State>,
) : SideEffectContext<State> {

  override val actions: Flow<Action>
    get() {
      val buffer = Channel<Action>(Channel.UNLIMITED)
      buffers.update { it + buffer }
      return buffer.receiveAsFlow().onCompletion {
        buffers.update { it - buffer }
        buffer.cancel()
      }
    }

  override suspend fun currentState(): State = state.first()
}

private fun <State> SideEffect<State>.actWith(context: SideEffectContext<State>): Flow<Action> = with(context) { act() }
