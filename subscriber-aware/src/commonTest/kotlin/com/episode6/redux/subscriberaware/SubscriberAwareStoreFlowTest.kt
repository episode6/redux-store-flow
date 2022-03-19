package com.episode6.redux.subscriberaware

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.lastElement
import com.episode6.redux.testsupport.runFlowTest
import com.episode6.redux.testsupport.stoplight.*
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

  @Test fun testMultipleSubscribers() = runFlowTest {
    val store = stopLightStore()

    val collector1 = store.testCollector()
    val collector2 = store.testCollector()

    assertThat(actions).all {
      hasSize(1)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
    }
  }

  @Test fun testDanglingSubscriber() = runFlowTest {
    val store = stopLightStore()

    val collector1 = store.testCollector()
    val collector2 = store.testCollector()
    collector1.stopCollecting()

    assertThat(actions).all {
      hasSize(1)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
    }
  }

  @Test fun testMultipleSubscribers_unsubscribe() = runFlowTest {
    val store = stopLightStore()

    val collector1 = store.testCollector()
    val collector2 = store.testCollector()
    collector1.stopCollecting()
    collector2.stopCollecting()

    assertThat(actions).all {
      hasSize(2)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
      index(1).isEqualTo(SubscriberStatusChanged(subscribersActive = false))
    }
  }

  @Test fun testDoesNotEmitStaleState() = runFlowTest {
    val store = stopLightStore()

    val collector = store.testCollector()
    store.dispatch(SetRedLightOn(false))
    assertThat(collector.values).all {
      hasSize(2)
      index(0).hasDefaultLights()
      index(1).hasLights()
    }

    collector.stopCollecting()
    store.dispatch(SetGreenLightOn(true))
    assertThat(collector.values).hasSize(2) // no change because not collecting
    assertThat(store.state).hasLights(green = true)

    collector.startCollecting()
    assertThat(collector.values).all {
      hasSize(3)
      lastElement().hasLights(green = true)
    }
  }
}
