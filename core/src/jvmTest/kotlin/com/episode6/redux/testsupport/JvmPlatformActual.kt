package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest

@OptIn(ExperimentalCoroutinesApi::class)
actual fun runTest(testBody: suspend CoroutineScope.() -> Unit) = runBlockingTest {
  val job = launch(block = testBody)
  job.cancelAndJoin()
}
