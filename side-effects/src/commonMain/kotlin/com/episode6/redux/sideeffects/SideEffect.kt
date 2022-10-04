package com.episode6.redux.sideeffects

import com.episode6.redux.Action
import kotlinx.coroutines.flow.Flow

/**
 * SideEffects offer a way to include managed async operations in a [StoreFlow].
 * The primary input is [SideEffectContext.actions], which will receive an emission
 * for every [Action] dispatched to the Store. The SideEffect returns a new [Flow]
 * of [Action] which will subsequently be dispatched back into the Store.
 */
public fun interface SideEffect<State : Any?> {
  public fun SideEffectContext<State>.act(): Flow<Action>
}

/**
 * The receiver passed to a [SideEffect]. The primary input is [actions], however
 * we the [currentState] can also be captured at any time inside the SideEffect.
 */
public interface SideEffectContext<State: Any?> {

  /**
   * A [Flow] of all the actions dispatched to the [StoreFlow]. A well-behaved
   * SideEffect will usually filterIsInstance<SpecificAction>() then transformLatest
   * to emit new actions back into the [StoreFlow]
   */
  public val actions: Flow<Action>

  /**
   * Returns the current state of the [StoreFlow] at function call-time.
   */
  public suspend fun currentState(): State
}
