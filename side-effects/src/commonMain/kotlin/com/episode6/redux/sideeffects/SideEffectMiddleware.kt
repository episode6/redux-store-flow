package com.episode6.redux.sideeffects

import com.episode6.redux.Action
import com.episode6.redux.Middleware
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Suppress("FunctionName")
fun <State : Any?> SideEffectMiddleware(vararg sideEffects: SideEffect<State>): Middleware<State> =
  SideEffectMiddleware(sideEffects.toList())

@Suppress("FunctionName")
fun <State : Any?> SideEffectMiddleware(sideEffects: Collection<SideEffect<State>>): Middleware<State> =
  Middleware { store, next ->
    val actionRelay = MutableSharedFlow<Action>()
    val context = SideEffectContextImpl(actionRelay.asSharedFlow(), store)
    sideEffects.map { it.actWith(context) }
      .forEach {
        launch { it.collect(store::dispatch) }
      }

    return@Middleware { action ->
      next(action)
      launch { actionRelay.emit(action) }
    }
  }

private class SideEffectContextImpl<State>(
  override val actions: Flow<Action>,
  private val state: Flow<State>,
) : SideEffectContext<State> {
  override suspend fun currentState(): State = state.first()
}

private fun <State> SideEffect<State>.actWith(context: SideEffectContext<State>): Flow<Action> = with(context) { act() }
