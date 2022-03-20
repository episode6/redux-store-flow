package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope

actual fun runTest(testBody: suspend CoroutineScope.() -> Unit) {
  // no-op, coroutine tests are not supported on most platforms in kotlin 1.5
}
