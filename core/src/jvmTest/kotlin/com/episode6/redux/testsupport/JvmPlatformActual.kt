package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UncompletedCoroutinesError
import kotlinx.coroutines.test.runBlockingTest

@OptIn(ExperimentalCoroutinesApi::class)
actual fun runTest(testBody: suspend CoroutineScope.() -> Unit) {
  try {
    runBlockingTest { testBody() }
  } catch (e: UncompletedCoroutinesError) {
    // no-op, store launches a coroutine on init
  }
}
