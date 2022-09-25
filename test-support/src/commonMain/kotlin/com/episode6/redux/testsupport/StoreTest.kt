@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

public fun <T> runStoreTest(
  storeBuilder: CoroutineScope.() -> T,
  testBody: suspend TestScope.(T) -> Unit,
): TestResult =
  runTest {
    val manager = storeManager(storeBuilder = storeBuilder)
    testBody(manager.store())
    manager.shutdown()
  }

