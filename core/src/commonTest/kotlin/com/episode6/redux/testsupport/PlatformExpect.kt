package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope

expect fun runTest(testBody: suspend CoroutineScope.() -> Unit)
