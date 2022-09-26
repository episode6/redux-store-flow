package com.episode6.redux.testsupport

import com.episode6.redux.Action
import com.episode6.redux.sideeffects.SideEffect
import com.episode6.redux.sideeffects.SideEffectContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Utility function for testing an individual [SideEffect]. Returns the [Flow] of [Action] output using the
 * given [context] as input.
 */
public fun <T> SideEffect<T>.testOutput(context: SideEffectTestContext<T>): Flow<Action> = with(context) { act() }

/**
 * Utility class for testing individual [SideEffect]s.
 */
public class SideEffectTestContext<T>(defaultState: T) : SideEffectContext<T> {
  public val actionsFlow: MutableSharedFlow<Action> = MutableSharedFlow()
  public var activeState: T = defaultState

  override val actions: Flow<Action> get() = actionsFlow
  override suspend fun currentState(): T = activeState
}
