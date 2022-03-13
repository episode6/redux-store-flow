package com.episode6.redux.testsupport

import assertk.all
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.hasMessage
import assertk.assertions.isFailure
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class JvmPlatformActualTest {

  @Test fun testRunTest_respectSleep() {
    var isSet: Boolean = false

    runTest {
      Thread.sleep(1000)
      isSet = true
    }

    assertThat(isSet).isTrue()
  }

  @Test fun testRunTest_propagateFail() {
    assertThat {
      runTest {
        Thread.sleep(1000)
        throw RuntimeException("test exception")
      }
    }.isFailure().all {
      hasClass(RuntimeException::class)
      hasMessage("test exception")
    }
  }
}
