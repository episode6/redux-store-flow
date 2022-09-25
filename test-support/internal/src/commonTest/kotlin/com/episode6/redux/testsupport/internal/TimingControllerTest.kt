@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.testsupport.internal

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TimingControllerTest {
  private val timing = TimingController()
  private var isDone = false

  @Test fun testStartTime() {
    assertThat(timing.time).isEqualTo(0L)
  }

  @Test fun testAwaitTme_noTimePassed() = runTest {
    val job = launch(UnconfinedTestDispatcher()) {
      timing.await(25)
      isDone = true
    }

    assertThat(isDone).isFalse()

    job.cancel()
  }

  @Test fun testAwaitTme_notEnoughTimePassed() = runTest {
    val job = launch(UnconfinedTestDispatcher()) {
      timing.await(25)
      isDone = true
    }

    timing.advanceBy(20)
    assertThat(isDone).isFalse()

    job.cancel()
  }

  @Test fun testAwaitTme_done() = runTest {
    val job = launch(UnconfinedTestDispatcher()) {
      timing.await(25)
      isDone = true
    }

    timing.advanceBy(25)
    assertThat(isDone).isTrue()

    job.cancel()
  }
}
