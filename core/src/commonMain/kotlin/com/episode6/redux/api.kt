package com.episode6.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a redux store backed by a [StateFlow]
 */
interface StoreFlow<State : Any?> : StateFlow<State> {
  val initialValue: State
  val scope: CoroutineScope
  fun dispatch(action: Action)
}

/**
 * Identifies a redux action that can be dispatched/interpreted by a [StoreFlow]
 */
interface Action

/**
 * Function that dispatches an action to a [StoreFlow]
 */
typealias Dispatch = (Action) -> Unit

/**
 * Reduces a state + action to a new state
 */
typealias Reducer<State> = State.(Action) -> State

/**
 * Gets the chance to interfere with a [StoreFlow]'s dispatch of actions
 */
fun interface Middleware<State : Any?> {
  fun CoroutineScope.interfere(store: StoreFlow<State>, next: Dispatch): Dispatch
}
