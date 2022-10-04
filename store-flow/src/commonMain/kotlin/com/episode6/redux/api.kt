package com.episode6.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Represents a redux store backed by a [kotlinx.coroutines.flow.StateFlow]
 */
public interface StoreFlow<State : Any?> : Flow<State> {
  public val initialState: State
  public val state: State
  public fun dispatch(action: Action)
}

/**
 * Identifies a redux action that can be dispatched/interpreted by a [StoreFlow]
 */
public interface Action

/**
 * Reduces a state + action to a new state
 */
public typealias Reducer<State> = State.(Action) -> State

/**
 * Gets the chance to interfere with a [StoreFlow]'s dispatch/processing of actions
 */
public fun interface Middleware<State : Any?> {
  public fun CoroutineScope.interfere(store: StoreFlow<State>, next: Dispatch): Dispatch
}

/**
 * Function that handles an incoming [Action]
 */
public typealias Dispatch = (Action) -> Unit
