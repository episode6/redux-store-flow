{% include readme_index.html %}

### SideEffects

<sup>Module: [`com.episode6.redux:side-effects:{{ site.version }}`]({{ site.docsDir }}/side-effects/com.episode6.redux.sideeffects/index.html)</sup>

SideEffects offer a way to include managed async operations in a StoreFlow. A `SideEffect` is a functional interface
that runs with a receiver of `SideEffectContext`. The primary input is `actions: Flow<Action>` which represents every
action dispatched to the StoreFlow, and the output is a new `Flow<Action>` where each emission will subsequently be
dispatched back into the StoreFlow

```kotlin
fun interface SideEffect<State : Any?> {
  // using a receiver (with named member) helps limit the verbosity of side-effects
  // while forcing usage of actions and currentState() to be explicit
  fun SideEffectContext<State>.act(): Flow<Action>
}

interface SideEffectContext<State : Any?> {
  val actions: Flow<Action> // primary input for a side
  suspend fun currentState(): State // returns the current state of the StoreFlow at the time its called
}
```

### Action delivery contract

The `SideEffectMiddleware` guarantees the following...

- Each SideEffect's `act()` is invoked synchronously while the StoreFlow is being set up, before the
  store processes its first action. `act()` should build and return a cold flow without performing
  any work itself; only the collection of the returned flow happens asynchronously.
- Each access of `actions` is backed by its own unlimited buffer, registered at access-time. Actions
  are delivered to each buffer in dispatch order and consumed in FIFO order. Access `actions` while
  building your flow chain (the normal case) and the buffer is guaranteed to receive every action
  dispatched to the store.
- A SideEffect that suspends while processing an action only delays its own queue of actions; other
  side-effects are unaffected.
- A SideEffect that never reads `actions` (e.g. one that only observes an external flow) simply opts
  out of receiving actions; this has no impact on any other side-effect.
- Actions are always reduced (and the resulting state published) *before* being relayed to
  side-effects, and the store's reducer is never blocked by a slow side-effect.
- No delivery order is guaranteed *across* different side-effects; each consumes its own queue at its
  own pace. Side-effects should never depend on when another side-effect observes an action (if they
  do, they're probably observing the wrong actions).

In our traffic light example, we can add a few side-effects to turn it into a state-machine that runs indefinitely...

```kotlin
fun setGreenEffect() = SideEffect<TrafficLightState> {
  actions.filterInstanceOf<SetGreenLight>() // it's good practice for a side-effect to only responds to a single action type
    .filter { it.value } // we only care when turning the green light on
    .transformLatest {
      delay(30.seconds)
      emit(SetGreenLight(false))
      emit(SetYellowLight(true))
    }
}

fun setYellowEffect() = SideEffect<TrafficLightState> {
  actions.filterInstanceOf<SetYellowLight>()
    .filter { it.value }
    .transformLatest {
      delay(10.seconds)
      emit(SetYellowLight(false))
      emit(SetRedLight(true))
    }
}

fun setRedEffect() = SideEffect<TrafficLightState> {
  actions.filterInstanceOf<SetRedLight>()
    .filter { it.value }
    .transformLatest {
      delay(40.seconds)
      emit(SetRedLight(false))
      emit(SetGreenLight(true))
    }
}
```

We then update our previous creator function to include a `SideEffectMiddleware` with these new side effects

```kotlin
fun trafficLightStore(scope: CoroutineScope) = StoreFlow(
  scope = scope,
  initialState = TrafficLightState(),
  reducer = TrafficLightState::reduce,
  middlewares = listOf(
    SideEffectMiddleware(
      setGreenEffect(),
      setYellowEffect(),
      setRedEffect(),
    )
  )
).also { it.dispatch(SetRedLight(true)) } // fire off the first action to kick off the side-effects
```

### SideEffects in DI

Using a dependency injection framework with multi-binding support can help limit the verbosity of side effects, while
still allowing them to maintain their own dependencies and be individually testable. For example,
with [dagger2](https://dagger.dev/) we can define our SideEffects directly in a `@Module` (or split across several
modules).

```kotlin
@Module object SideEffectModule {
  
  @Provides @IntoSet fun sideEffect1(someDependency: SomeDependency) = SideEffect<TrafficLightState> {
    actions.filterIsInstance<SomeAction>().transformLatest { /* do work */ }
  }

  @Provides @IntoSet fun sideEffect2(someDependency: SomeDependency2) = SideEffect<TrafficLightState> {
    actions.filterIsInstance<SomeAction2>().transformLatest { /* do work */ }
  }

  // etc
}

// We then define a factory for our StoreFlow and inject the Set<SideEffect>
class StoreFactory @Inject constructor(
  val sideEffects: Set<@JvmSuppressWildcards SideEffect<TrafficLightState>>,
) {
  fun create(scope: CoroutineScope): StoreFlow<TrafficLightState> = StoreFlow(
    scope = scope,
    initialValue = TrafficLightState(),
    reducer = TrafficLightState::reduce,
    middlewares = listOf(SideEffectMiddleware(sideEffects))
  )
}
```
In the above example we no longer need to pass each SideEffect into the SideEffectMiddleware manually, but we can still write unit tests for each SideEffect individually by directly calling the `SideEffectModule.sideEffect*` methods.
