StoreFlow is a kotlin multiplatform "port" of [Redux for Javascript](https://redux.js.org/) / [ReduxKotlin](https://reduxkotlin.org/) that is backed by kotlin's coroutines and Flows. While we use much of the same terminology as the aforementioned projects, the api and implementation differ significantly.

### Installation
Redux StoreFlow artifacts are published to Maven Central with gradle metadata. We currently ship the following modules...
```groovy
def reduxVersion = "{{ site.version }}"
dependencies {
  // core api & implementation
  implementation "com.episode6.redux:store-flow:$reduxVersion"
  // Support for Side Effects / SideEffectMiddleware
  implementation "com.episode6.redux:side-effects:$reduxVersion"
  // A StoreFlow that dispatches a SubscriberStatusChanged Action
  implementation "com.episode6.redux:subscriber-aware:$reduxVersion"
  // Jetpack Compose Multiplatform support
  implementation "com.episode6.redux:compose:$reduxVersion"
  // Unit-testing support module
  testImplementation "com.episode6.redux:test-support:$reduxVersion"
}
```

[Snapshot KDocs](docs/main/)
