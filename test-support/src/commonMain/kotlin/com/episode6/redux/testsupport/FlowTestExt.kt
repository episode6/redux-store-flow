@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

suspend fun CoroutineScope.flowTestScope(body: suspend FlowTestScope.() -> Unit) = FlowTestScopeImpl(this).body()
fun runUnconfinedFlowTest(testBody: suspend FlowTestScope.() -> Unit) = runUnconfinedTest { flowTestScope(testBody) }

interface FlowTestScope : CoroutineScope {
  fun <T> Flow<T>.testCollector(start: Boolean = true): FlowValueCollector<T>
  suspend fun <T> Flow<T>.test(testBody: suspend FlowValueCollector<T>.() -> Unit)
}

interface FlowValueCollector<T> {
  fun startCollecting()
  fun stopCollecting()
  val values: List<T>
}

private class FlowTestScopeImpl(private val delegate: CoroutineScope) : FlowTestScope, CoroutineScope by delegate {
  override fun <T> Flow<T>.testCollector(start: Boolean): FlowValueCollector<T> =
    FlowValueCollectorImpl(this@testCollector, this@FlowTestScopeImpl).apply { if (start) startCollecting() }

  override suspend fun <T> Flow<T>.test(testBody: suspend FlowValueCollector<T>.() -> Unit) {
    testCollector(start = true).apply {
      testBody()
      stopCollecting()
    }
  }
}

private class FlowValueCollectorImpl<T>(
  private val flow: Flow<T>,
  private val scope: CoroutineScope
) : FlowValueCollector<T> {

  private val _values = mutableListOf<T>()
  private var job: Job? = null

  override val values: List<T> get() = _values
  override fun startCollecting() {
    if (job != null) throw AssertionError("Already started collecting from flow: $flow")
    job = scope.launch { flow.toList(_values) }
  }

  override fun stopCollecting() {
    job?.cancel()
    job = null
  }
}
