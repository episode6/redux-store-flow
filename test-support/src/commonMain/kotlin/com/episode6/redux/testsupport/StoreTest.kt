@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.testsupport

import com.episode6.redux.StoreFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.coroutines.CoroutineContext

/**
 * Creates the [StoreFlow] defined in [storeBuilder] using the given [context]. Then
 * executes the [testBody], shutting down the [StoreFlow] when finished.
 */
public fun <T> runStoreTest(
  context: CoroutineContext = UnconfinedTestDispatcher(),
  storeBuilder: CoroutineScope.() -> StoreFlow<T>,
  testBody: suspend TestScope.(StoreFlow<T>) -> Unit,
): TestResult =
  runTest {
    val manager = StoreManager(context = context, storeBuilder = storeBuilder)
    testBody(manager.store())
    manager.shutdown()
  }

