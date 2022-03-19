package com.episode6.redux.subscriberaware

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.runFlowTest
import com.episode6.redux.testsupport.stoplight.StopLightState
import com.episode6.redux.testsupport.stoplight.reduce
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test

class SubscriberAwareStoreFlowTest {

  val actions = mutableListOf<Action>()
  val middleware = Middleware<StopLightState> { _, next ->
    {
      actions += it
      next(it)
    }
  }

  private fun CoroutineScope.stopLightStore(): StoreFlow<StopLightState> = SubscriberAwareStoreFlow(
    scope = this,
    initialValue = StopLightState(),
    reducer = StopLightState::reduce,
    middlewares = listOf(middleware)
  )

  @Suppress("UNUSED_VARIABLE")
  @Test fun testNoInitialAction() = runFlowTest {
    val store = stopLightStore()

    assertThat(actions).isEmpty()
  }

  @Test fun testSubscribe() = runFlowTest {
    val store = stopLightStore()

    store.test {
      assertThat(actions).containsExactly(SubscriberStatusChanged(subscribersActive = true))
    }
  }

  @Test fun testUnSubscribe() = runFlowTest {
    val store = stopLightStore()

    store.testCollector().stopCollecting()

    assertThat(actions).all {
      hasSize(2)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
      index(1).isEqualTo(SubscriberStatusChanged(subscribersActive = false))
    }
  }
}
