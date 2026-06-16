<p align="center">
    <a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.episode6.redux%22"><img src="https://img.shields.io/maven-central/v/com.episode6.redux/store-flow.svg?style=flat-square"></a>
</p>

StoreFlow is yet another kotlin multiplatform "port" of [Redux for Javascript](https://redux.js.org/)
/ [ReduxKotlin](https://reduxkotlin.org/). What sets it apart is that instead of trying to faithfully re-create the
Redux api, we leverage kotlin's coroutines and Flows to handle the heavy lifting. StoreFlow uses much of the same
terminology as the aforementioned projects but our creation and usage patterns differ. We will not attempt to explain
the core concepts, theory or motivation of Redux here;
the [ReduxKotlin intro docs](https://reduxkotlin.org/introduction/core-concepts) are recommended reading if unfamiliar
with the Redux pattern.

{% include readme_index.html %}

### Installation

Redux StoreFlow artifacts are published to Maven Central with gradle metadata. We currently ship the following
modules...

<sub>StoreFlow v{{ site.version }} is compiled against Kotlin v{{ site.kotlinVersion }} and Coroutines v{{ site.coroutineVersion }}</sub>

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

<sup>[Snapshots](docs/main/) are available in the sonatype snapshot repo
at https://oss.sonatype.org/content/repositories/snapshots/</sup>
