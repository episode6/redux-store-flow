### SideEffects [`com.episode6.redux:side-effects`]({{ site.docsDir }}/side-effects/com.episode6.redux.sideeffects/index.html)

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
