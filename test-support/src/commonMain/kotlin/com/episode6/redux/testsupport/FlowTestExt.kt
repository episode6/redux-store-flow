package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

interface FlowTestScope : CoroutineScope {
  fun <T> Flow<T>.test(testBody: FlowValueCollector<T>.() -> Unit)
}

interface FlowValueCollector<T> {
  val values: List<T>
}

fun runFlowTest(testBody: suspend FlowTestScope.() -> Unit) = runTest { FlowTestScopeImpl(this).testBody() }

private class FlowTestScopeImpl(private val delegate: CoroutineScope) : FlowTestScope, CoroutineScope by delegate {
  override fun <T> Flow<T>.test(testBody: FlowValueCollector<T>.() -> Unit) {
    val collector = FlowValueCollectorImpl<T>()
    val job = launch { collect { collector._values.add(it) } }
    collector.testBody()
    job.cancel()
  }
}

private class FlowValueCollectorImpl<T> : FlowValueCollector<T> {
  val _values = mutableListOf<T>()
  override val values: List<T> get() = _values
}
