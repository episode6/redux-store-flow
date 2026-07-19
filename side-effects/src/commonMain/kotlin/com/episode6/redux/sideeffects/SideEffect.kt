package com.episode6.redux.sideeffects

import com.episode6.redux.Action
import kotlinx.coroutines.flow.Flow

/**
 * SideEffects offer a way to include managed async operations in a [com.episode6.redux.StoreFlow].
 * The primary input is [SideEffectContext.actions], which will receive an emission
 * for every [Action] dispatched to the Store. The SideEffect returns a new [Flow]
 * of [Action] which will subsequently be dispatched back into the Store.
 *
 * [act] is invoked synchronously while the store is being set up, before the store processes
 * its first action. It should build and return a cold [Flow] without performing any work itself.
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
   * A [Flow] of all the actions dispatched to the [com.episode6.redux.StoreFlow]. A well-behaved
   * SideEffect will usually filterIsInstance<SpecificAction>() then transformLatest
   * to emit new actions back into the [com.episode6.redux.StoreFlow]
   *
   * Each access of this property returns an independently-buffered [Flow] (backed by its own
   * unlimited buffer, registered at access-time). This means:
   * - Actions are delivered to each buffer in dispatch order and consumed in FIFO order, but no
   *   delivery order is guaranteed *across* different side-effects (each consumes its own queue
   *   at its own pace).
   * - A SideEffect that suspends while processing an action only delays its own queue; other
   *   side-effects are unaffected.
   * - A SideEffect that never accesses this property simply opts out of receiving actions
   *   (e.g. an effect that only observes an external Flow); this has no impact on other
   *   side-effects.
   * - Access this property while building the flow chain (the normal case) to ensure the buffer
   *   is registered before the store processes its first action. A buffer registered later (i.e.
   *   accessing this property lazily from inside an operator) will only receive actions dispatched
   *   after registration.
   * - Each accessed [Flow] is intended for a single collector; access this property once per
   *   independent collection (e.g. `merge(actions.filterIsInstance<A>(), actions.filterIsInstance<B>())`).
   */
  public val actions: Flow<Action>

  /**
   * Returns the current state of the [com.episode6.redux.StoreFlow] at function call-time.
   */
  public suspend fun currentState(): State
}
