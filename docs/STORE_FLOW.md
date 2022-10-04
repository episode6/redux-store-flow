{% include readme_index.html %}

### StoreFlow Core Api 
<sup>Module: [`com.episode6.redux:store-flow:{{ site.version }}`]({{ site.docsDir }}/store-flow/com.episode6.redux/index.html)</sup>

By implementing `Flow`, the `StoreFlow` api remains relatively simple...

```kotlin
interface StoreFlow<State : Any?> : Flow<State> {
  val initialState: State // the initial state used to create the StoreFlow
  val state: State // the current state
  fun dispatch(action: Action) // dispatches an action to the Store to be reduced / processed by middleware
}

interface Action
```

In addition to an initialState and Reducer, a `CoroutineScope` is required to create a StoreFlow

```kotlin
fun <State : Any?> StoreFlow(
  scope: CoroutineScope,
  initialValue: State,
  reducer: Reducer<State>,
  middlewares: List<Middleware<State>> = emptyList(),
): StoreFlow<State>
```

<sup>Every StoreFlow is thread-safe. Actions are processed in-order in a single coroutine in the supplied
CoroutineScope.</sup>

The Reducer is a pure function that takes a State and Action as input and outputs a new State

```kotlin
typealias Reducer<State> = State.(Action) -> State
```

### Sample StoreFlow

Here we demo a sample StoreFlow that manages the state of a traffic light...

```kotlin
data class TrafficLightState(
  val green: Boolean = false,
  val yellow: Boolean = false,
  val red: Boolean = true,
)

data class SetGreenLight(val value: Boolean) : Action
data class SetYellowLight(val value: Boolean) : Action
data class SetRedLight(val value: Boolean) : Action

// See the ReduceAction pattern below for a trick to eliminate this additional verbosity
private fun TrafficLightState.reduce(action: Action): TrafficLightState = when (action) {
  is SetGreenLight  -> copy(green = action.value)
  is SetYellowLight -> copy(yellow = action.value)
  is SetRedLight    -> copy(red = action.value)
  else              -> this
}

fun trafficLightStore(scope: CoroutineScope) = StoreFlow(
  scope = scope,
  initialState = TrafficLightState(),
  reducer = TrafficLightState::reduce,
)
```

We can see the store working by collecting state updates...
```kotlin
fun main() {
  coroutineContext {
    val store = trafficLightStore(this)
    
    launch { store.collect { println(it) } }
    
    store.dispatch(SetGreenLight(true))
    store.dispatch(SetYellowLight(true))
  }
}
```

### Middleware

A Middleware is a functional interface that has the opportunity to interfere with the processing of an action. It accepts a dispatch function `next`, which allows the action to continue being processed. The middleware then returns its own dispatch function in which `next` should be called.

```kotlin
fun interface Middleware<State : Any?> {
  fun CoroutineScope.interfere(store: StoreFlow<State>, next: Dispatch): Dispatch
}

typealias Dispatch = (Action) -> Unit
```

A simple logging middleware could look something like this...

```kotlin
fun loggingMiddleware() = Middleware { store, next ->
  return@Middleware { action ->
    println("before $action; ${store.state}")
    next(action) // let the action be reduced
    println("after $action; ${store.state}")
  }
}
```

Since a Middleware is executed with a `CoroutineScope`, it can safely launch async work in response to actions, however it's bad practice to defer execution of the `next` dispatch function.  

Currently, the only Middleware we ship is `SideEffectMiddleware`, which you can read more about in the [SideEffect Readme](SIDE_EFFECTS.md#sideeffects). If you're new to redux, this should be the only Middleware you need to worry about (besides simple logging).

### ReduceAction Pattern

A common complaint about the Redux pattern is it adds redundant boilerplate due to the addition of Actions and the Reducer. Once way we can limit this verbosity is with the "ReduceAction" pattern...
```kotlin
// Only actions that extend our ReduceAction will make changes to the state. 
// Because we're using a sealed class, we still have complete control of 
// reducer as changes can only be introduced to this file.
sealed class ReduceAction(reduce: TrafficLightState.()->TrafficLightState) : Action

// we update our actions so they can reduce themselves
data class SetGreenLight(val value: Boolean) : ReduceAction({ copy(green = value) })
data class SetYellowLight(val value: Boolean) : ReduceAction({ copy(yellow = value) })
data class SetRedLight(val value: Boolean) : ReduceAction({ copy(red = value) })

// instead of writing a manual reducer function, we replace it with a simple lambda
fun trafficLightStore(scope: CoroutineScope) = StoreFlow(
  scope = scope,
  initialState = TrafficLightState(),
  reducer = { (it as? ReduceAction)?.reduce?.invoke(this) ?: this },
)
```

