package com.episode6.redux.sideeffects

import com.episode6.redux.Action
import kotlinx.coroutines.flow.Flow

fun interface SideEffect<State : Any?> {
  fun SideEffectContext<State>.act(): Flow<Action>
}

interface SideEffectContext<State: Any?> {
  val actions: Flow<Action>
  suspend fun currentState(): State
}
