package com.episode6.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a redux store backed by a [StateFlow]
 */
interface StoreFlow<T : Any> : StateFlow<T> {
  val initialValue: T
  val dispatch: Dispatch
  val scope: CoroutineScope
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
typealias Reducer<T> = T.(Action) -> T

/**
 * Gets the chance to interfere with a [StoreFlow]'s dispatch of actions
 */
fun interface Middleware<T : Any> {
  fun CoroutineScope.interfere(store: StoreFlow<T>, next: Dispatch): Dispatch
}
