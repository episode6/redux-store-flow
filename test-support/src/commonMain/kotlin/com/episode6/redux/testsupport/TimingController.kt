package com.episode6.redux.testsupport

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

// im probably reinventing the wheel here
class TimingController(startTime: Long = 0L) {
  private val timeState = MutableStateFlow(startTime)
  val time: Long get() = timeState.value

  suspend fun await(duration: Long) {
    val start = time
    timeState.filter { it >= (start + duration) }.first()
  }

  fun advanceBy(duration: Long) {
    if (duration > 0) {
      timeState.value = time + duration
    }
  }

  fun advanceTo(time: Long) {
    if (time > this.time) {
      timeState.value = time
    }
  }

}
