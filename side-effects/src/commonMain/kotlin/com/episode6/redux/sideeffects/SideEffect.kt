package com.episode6.redux.sideeffects

import com.episode6.redux.Action
import kotlinx.coroutines.flow.Flow

public fun interface SideEffect<State : Any?> {
  public fun SideEffectContext<State>.act(): Flow<Action>
}

public interface SideEffectContext<State: Any?> {
  public val actions: Flow<Action>
  public suspend fun currentState(): State
}
