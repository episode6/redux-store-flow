package com.episode6.redux

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a redux store backed by a [StateFlow]
 */
interface StoreFlow<T : Any> : StateFlow<T> {
  val scope: CoroutineScope
  val initialValue: T
  fun dispatch(action: Action)
}

/**
 * Identifies a redux action that can be dispatched/interpreted by a [StoreFlow]
 */
interface Action
