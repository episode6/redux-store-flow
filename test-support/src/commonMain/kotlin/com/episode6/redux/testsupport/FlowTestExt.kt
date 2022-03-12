package com.episode6.redux.testsupport

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

fun runFlowTest(testBody: suspend FlowTestScope.() -> Unit) = runTest { FlowTestScopeImpl(this).testBody() }

interface FlowTestScope : CoroutineScope {
  fun <T> Flow<T>.testCollector(): FlowValueCollector<T> = FlowValueCollectorImpl(this, this@FlowTestScope)
  suspend fun <T> Flow<T>.test(testBody: suspend FlowValueCollector<T>.() -> Unit) = testCollector().testBody()
}

interface FlowValueCollector<T> {
  val values: List<T>
}

private class FlowTestScopeImpl(private val delegate: CoroutineScope) : FlowTestScope, CoroutineScope by delegate
private class FlowValueCollectorImpl<T>(flow: Flow<T>, scope: CoroutineScope) : FlowValueCollector<T> {
  private val _values = mutableListOf<T>()
  override val values: List<T> get() = _values

  init {
    scope.launch { flow.collect { _values.add(it) } }
  }
}
