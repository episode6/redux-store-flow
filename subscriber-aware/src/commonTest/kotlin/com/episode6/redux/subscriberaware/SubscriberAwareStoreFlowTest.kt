package com.episode6.redux.subscriberaware

import assertk.all
import assertk.assertThat
import assertk.assertions.*
import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.FlowTestScope
import com.episode6.redux.testsupport.lastElement
import com.episode6.redux.testsupport.runUnconfinedStoreTest
import com.episode6.redux.testsupport.stoplight.*
import kotlin.test.Test

class SubscriberAwareStoreFlowTest {

  val actions = mutableListOf<Action>()
  val middleware = Middleware<StopLightState> { _, next ->
    {
      actions += it
      next(it)
    }
  }

  private fun storeTest(testBody: suspend FlowTestScope.(StoreFlow<StopLightState>) -> Unit) = runUnconfinedStoreTest(
    storeBuilder = {
      SubscriberAwareStoreFlow(
        scope = this,
        initialValue = StopLightState(),
        reducer = StopLightState::reduce,
        middlewares = listOf(middleware)
      )
    },
    testBody = testBody
  )

  @Suppress("UNUSED_VARIABLE")
  @Test fun testNoInitialAction() = storeTest {
    assertThat(actions).isEmpty()
  }

  @Test fun testSubscribe() = storeTest { store ->

    store.test {
      assertThat(actions).containsExactly(SubscriberStatusChanged(subscribersActive = true))
      assertThat(values).isNotEmpty()
    }
  }

  @Test fun testUnSubscribe() = storeTest { store ->

    store.testCollector().stopCollecting()

    assertThat(actions).all {
      hasSize(2)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
      index(1).isEqualTo(SubscriberStatusChanged(subscribersActive = false))
    }
  }

  @Test fun testMultipleSubscribers() = storeTest { store ->

    val collector1 = store.testCollector()
    val collector2 = store.testCollector()

    assertThat(actions).all {
      hasSize(1)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
    }
    assertThat(collector1.values).isNotEmpty()
    assertThat(collector2.values).isNotEmpty()

    collector1.stopCollecting()
    collector2.stopCollecting()
  }

  @Test fun testDanglingSubscriber() = storeTest { store ->

    val collector1 = store.testCollector()
    val collector2 = store.testCollector()
    collector1.stopCollecting()

    assertThat(actions).all {
      hasSize(1)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
    }
    assertThat(collector1.values).isNotEmpty()
    assertThat(collector2.values).isNotEmpty()

    collector1.stopCollecting()
    collector2.stopCollecting()
  }

  @Test fun testMultipleSubscribers_unsubscribe() = storeTest { store ->

    val collector1 = store.testCollector()
    val collector2 = store.testCollector()
    collector1.stopCollecting()
    collector2.stopCollecting()

    assertThat(actions).all {
      hasSize(2)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
      index(1).isEqualTo(SubscriberStatusChanged(subscribersActive = false))
    }
    assertThat(collector1.values).isNotEmpty()
    assertThat(collector2.values).isNotEmpty()
  }

  @Test fun testDoesNotEmitStaleState() = storeTest { store ->

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

    collector.stopCollecting()
  }
}
