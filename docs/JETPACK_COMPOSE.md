{% include readme_index.html %}

### Jetpack Compose Support
<sup>Module: [`com.episode6.redux:compose:{{ site.version }}`]({{ site.docsDir }}/compose/com.episode6.redux.compose/index.html)</sup>

The `:compose` module includes a `StoreFlow.collectAsState()` convenience method for use in Jetpack Compose (both android and kotlin multi-platform). This removes the need for the caller to specify a default value, since a StoreFlow includes a stable initialValue that is used. We also include a version of the method that takes a transform lambda as a parameter, to collect only part of the State.

Example:
```kotlin
@Composable fun storeFlowExample(store: StoreFlow<TrafficLightState>) {
  // collect the entire state
  val entireState: TrafficLightState by store.collectAsState()
  
  // only collect a part of the state
  val greenLight: Boolean by store.collectAsState { it.green }
}
```
