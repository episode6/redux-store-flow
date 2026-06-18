{% include readme_index.html %}

### Subscriber Aware StoreFlow

<sup>Module: [`com.episode6.redux:subscriber-aware:{{ site.version }}`]({{ site.docsDir }}/subscriber-aware/com.episode6.redux.subscriberaware/-subscriber-aware-store-flow.html)</sup>

A `SubscriberAwareStoreFlow` acts just like a normal `StoreFlow` except that it dispatches a special
action (`data class SubscriberStatusChanged(val subscribersActive: Boolean) : Action`) whenever the first subscriber
begins collecting from the StoreFlow and whenever the last subscriber stops collecting from the StoreFlow. If the
StoreFlow is being used to control the state of your UI, then this maps approximately to the UI's lifecycle.
The `SubscriberStatusChanged` action can be handled by your Reducer, Middleware and/or SideEffects.

Example: Lets assume our traffic light StoreFlow is controlled by a server somewhere, instead of the local timing we
implemented in the [SideEffects Example](SIDE_EFFECTS.md#sideeffects). We'd likely only want to maintain our connection
to the server when there is actually a UI observing our state (otherwise we'd just be wasting cycles).

```kotlin
// assume our server gives us updates via a Flow
interface TrafficServer {
  fun lightStateUpdates(): Flow<LightStateUpdates>
}

fun trafficServerListenSideEffect(server: TrafficServer) = SideEffect<TrafficLightState> {
  actions.filterIsInstance<SubscriberStatusChanged>().transformLatest { action ->
    // we use an if-statement here instead of filtering because we want 
    // SubscriberStatusChanged(false) to cancel any ongoing transform, 
    // so instead we just no-op when false
    if (action.subscribersActive) { 
      server.lightStateUpdates().collect { serverUpdate ->
        emit(SetGreenLight(serverUpdate.green))
        emit(SetYellowLight(serverUpdate.yellow))
        emit(SetRedLight(serverUpdate.red))
      }
    }
  }
}

// then we update our creator function to use SubscriberAwareStoreFlow 
// and apply our new SideEffect (which is the only one we'll need)
fun trafficLightStore(scope: CoroutineScope, server: TrafficServer) = SubscriberAwareStoreFlow(
  scope = scope,
  initialState = TrafficLightState(),
  reducer = TrafficLightState::reduce,
  middlewares = listOf(
    SideEffectMiddleware(trafficServerListenSideEffect(server))
  )
) // no need to dispatch an initial action because SubscriberStatusChanged 
  // will be automatically dispatched when the UI starts collecting.
```
