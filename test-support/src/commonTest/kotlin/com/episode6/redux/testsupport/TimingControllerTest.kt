package com.episode6.redux.testsupport

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.launch
import kotlin.test.Test

class TimingControllerTest {
  private val timing = TimingController()
  private var isDone = false

  @Test fun testStartTime() {
    assertThat(timing.time).isEqualTo(0L)
  }

  @Test fun testAwaitTme_noTimePassed() = runUnconfinedTest {
    val job = launch {
      timing.await(25)
      isDone = true
    }

    assertThat(isDone).isFalse()

    job.cancel()
  }

  @Test fun testAwaitTme_notEnoughTimePassed() = runUnconfinedTest {
    val job = launch {
      timing.await(25)
      isDone = true
    }

    timing.advanceBy(20)
    assertThat(isDone).isFalse()

    job.cancel()
  }

  @Test fun testAwaitTme_done() = runUnconfinedTest {
    val job = launch {
      timing.await(25)
      isDone = true
    }

    timing.advanceBy(25)
    assertThat(isDone).isTrue()

    job.cancel()
  }
}
