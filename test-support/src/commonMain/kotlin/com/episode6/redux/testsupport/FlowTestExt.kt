package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun runFlowTest(testBody: suspend FlowTestScope.() -> Unit) = runTest { FlowTestScopeImpl(this).testBody() }

interface FlowTestScope : CoroutineScope {
  fun <T> Flow<T>.testCollector(start: Boolean = true): FlowValueCollector<T> =
    FlowValueCollectorImpl(this, this@FlowTestScope)
      .apply { if (start) startCollecting() }

  suspend fun <T> Flow<T>.test(testBody: suspend FlowValueCollector<T>.() -> Unit) = testCollector(start = true).apply {
    testBody()
    stopCollecting()
  }
}

interface FlowValueCollector<T> {
  fun startCollecting()
  fun stopCollecting()
  val values: List<T>
}

private class FlowTestScopeImpl(private val delegate: CoroutineScope) : FlowTestScope, CoroutineScope by delegate
private class FlowValueCollectorImpl<T>(
  private val flow: Flow<T>,
  private val scope: CoroutineScope
) : FlowValueCollector<T> {

  private val _values = mutableListOf<T>()
  private var job: Job? = null

  override val values: List<T> get() = _values
  override fun startCollecting() {
    if (job != null) throw AssertionError("Already started collecting from flow: $flow")
    job = scope.launch { flow.collect { _values.add(it) } }
  }

  override fun stopCollecting() {
    job?.cancel()
    job = null
  }
}
