{% include readme_index.html %}

### Unit Test Support
<sup>Module: [`com.episode6.redux:test-support:{{ site.version }}`]({{ site.docsDir }}/test-support/com.episode6.redux.testsupport/index.html)</sup>

Because StoreFlow launches a coroutine on init, unit-testing one with kotlin's new-ish `runTest` method is not entirely straightforward (creating a StoreFlow with a TestScope results in a UncompletedCoroutinesError since StoreFlow will intentionally never complete its Job until its CoroutineScope is explicitly cancelled). 

To deal with this, we supply a `StoreManager` in the `:test-support` module. The StoreManager is used to create and shutdown the StoreFlow that is being tested. 

Example:
```kotlin
private fun createStoreFlow(scope: CoroutineScope): StoreFlow<TrafficLightState> { /*  */ }

@Test fun testGreenLight() = runTest {
  val manager = StoreManager { createStoreFlow(this) }
  val store: StoreFlow<TrafficLightState> = manager.store()
  
  // test code
  store.dispatch(SetGreenLight(true))
  assertTrue(store.state.green)
  
  // we must shutdown the StoreManager before the end of the test body
  manager.shutdown()
}
```

We also offer a convenience method `runStoreTest` (that replaces `runTest`) that automatically shuts down the store at the end of the test...
```kotlin
@Test fun testGreenLight() = runStoreTest(::createStoreFlow) { store ->
  // test code
  store.dispatch(SetGreenLight(true))
  assertTrue(store.state.green)
}
```

#### Testing individual SideEffects
While integration tests of a fully-formed StoreFlow offer the most value, you may want to test SideEffects individually to validate their output. To assist with this, we offer the `SideEffectTestContext` class and `SideEffect.testOutput(SideEffectTestContext): Flow<Action>` method. The SideEffectTestContext lets you emit `Action`s and control the `currentState()` that is available to the SideEffect. We can then test the emissions of the resulting Flow using [Turbine](https://github.com/cashapp/turbine) (or by manually collecting from it).

Example:
```kotlin
class SideEffectTest {
  val context = SideEffectTestContext(defaultState = TrafficLightState())
  
  @Test fun testGreenLightEffect() = runTest {
    val sideEffect = greenLightSideEffect() // from our SideEffects example code
    val testOutput: Flow<Action> = sideEffect.testOutput(context)
    
    testOutput.test { // Flow.test function is part of the Turbine library
      
      // use context to send actions through the SideEffect
      context.actionsFlow.emit(SetGreenLight(true))

      advanceTimeBy(30000L)
      
      // awaitItem is part of the Turbine library
      assertEquals(awaitItem(), SetGreenLight(false))
      assertEquals(awaitItem(), SetYellowLight(true))
    }
  }
}
```
