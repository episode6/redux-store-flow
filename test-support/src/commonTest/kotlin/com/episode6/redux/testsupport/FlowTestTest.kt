package com.episode6.redux.testsupport

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlin.test.Test

class FlowTestTest {

  @Test fun testFlowTestCollector_static() = runFlowTest {
    val flow = flowOf(1, 2, 3)

    val collector = flow.testCollector()

    assertThat(collector.values).containsExactly(1, 2, 3)
  }

  @Test fun testFlowTest_static() = runFlowTest {
    val flow = flowOf(1, 2, 3)

    flow.test {
      assertThat(values).containsExactly(1, 2, 3)
    }
  }

  @Test fun testFlowTestCollector_live() = runFlowTest {
    val flow = MutableSharedFlow<Int>()

    val collector = flow.testCollector()

    assertThat(collector.values).isEmpty()

    flow.emit(1)
    assertThat(collector.values).containsExactly(1)

    flow.emit(2)
    assertThat(collector.values).containsExactly(1, 2)

    flow.emit(3)
    assertThat(collector.values).containsExactly(1, 2, 3)
  }

  @Test fun testFlowTest_live() = runFlowTest {
    val flow = MutableSharedFlow<Int>()

    flow.test {
      assertThat(values).isEmpty()

      flow.emit(1)
      assertThat(values).containsExactly(1)

      flow.emit(2)
      assertThat(values).containsExactly(1, 2)

      flow.emit(3)
      assertThat(values).containsExactly(1, 2, 3)
    }
  }

  @Test fun testFlowCollectorStop() = runFlowTest {
    val flow = MutableSharedFlow<Int>()
    var hasSubscribed = false
    var isComplete = false
    val collector = flow
      .onSubscription { hasSubscribed = true }
      .onCompletion { isComplete = true }
      .testCollector(start = false)

    assertThat(hasSubscribed).isFalse()
    assertThat(isComplete).isFalse()

    collector.startCollecting()

    assertThat(hasSubscribed).isTrue()
    assertThat(isComplete).isFalse()

    collector.stopCollecting()

    assertThat(hasSubscribed).isTrue()
    assertThat(isComplete).isTrue()
  }

  @Test fun testFlowCollectionFail() = runFlowTest {
    val flow = MutableSharedFlow<Int>()

    val collector = flow.testCollector(start = true)

    assertThat { collector.startCollecting() }
      .isFailure().hasClass(AssertionError::class)
  }
}
