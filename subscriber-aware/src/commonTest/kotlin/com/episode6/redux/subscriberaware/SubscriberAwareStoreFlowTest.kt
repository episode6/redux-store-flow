@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.subscriberaware

import app.cash.turbine.test
import app.cash.turbine.testIn
import assertk.all
import assertk.assertThat
import assertk.assertions.*
import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.StoreFlow
import com.episode6.redux.testsupport.FlowTestScope
import com.episode6.redux.testsupport.awaitItems
import com.episode6.redux.testsupport.lastElement
import com.episode6.redux.testsupport.runUnconfinedStoreTest
import com.episode6.redux.testsupport.stoplight.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test

class SubscriberAwareStoreFlowTest {

  val actions = mutableListOf<Action>()
  val middleware = Middleware<StopLightState> { _, next ->
    {
      actions += it
      next(it)
    }
  }

  private fun storeTest(testBody: suspend TestScope.(StoreFlow<StopLightState>) -> Unit) = runUnconfinedStoreTest(
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
      assertThat(awaitItem()).hasDefaultLights()

      ensureAllEventsConsumed()
    }
  }

  @Test fun testUnSubscribe() = storeTest { store ->

    store.testIn(this).cancelAndIgnoreRemainingEvents()

    assertThat(actions).all {
      hasSize(2)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
      index(1).isEqualTo(SubscriberStatusChanged(subscribersActive = false))
    }
  }

  @Test fun testMultipleSubscribers() = storeTest { store ->

    val collector1 = store.testIn(this)
    val collector2 = store.testIn(this)

    assertThat(actions).all {
      hasSize(1)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
    }
    assertThat(collector1.awaitItem()).hasDefaultLights()
    assertThat(collector2.awaitItem()).hasDefaultLights()

    collector1.cancel()
    collector2.cancel()
  }

  @Test fun testDanglingSubscriber() = storeTest { store ->

    val collector1 = store.testIn(this)
    val collector2 = store.testIn(this)
    val collector1Events = collector1.cancelAndConsumeRemainingEvents()

    assertThat(actions).all {
      hasSize(1)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
    }
    assertThat(collector1Events).isNotEmpty()
    assertThat(collector2.awaitItem()).hasDefaultLights()
    collector2.ensureAllEventsConsumed()

    collector1.cancel()
    collector2.cancel()
  }

  @Test fun testMultipleSubscribers_unsubscribe() = storeTest { store ->

    val collector1 = store.testIn(this)
    val collector2 = store.testIn(this)
    val events1 = collector1.cancelAndConsumeRemainingEvents()
    val events2 = collector2.cancelAndConsumeRemainingEvents()

    assertThat(actions).all {
      hasSize(2)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
      index(1).isEqualTo(SubscriberStatusChanged(subscribersActive = false))
    }
    assertThat(events1).isNotEmpty()
    assertThat(events2).isNotEmpty()
  }

  @Test fun testValue_switchLight() = storeTest { store ->

    store.dispatch(SetRedLightOn(false))
    store.dispatch(SetGreenLightOn(true))

    assertThat(store.state).hasLights(green = true)
  }

  @Test fun testDoesNotEmitStaleState() = storeTest { store ->

    var collector = store.testIn(this)
    store.dispatch(SetRedLightOn(false))
    assertThat(collector.awaitItems(2)).all {
      index(0).hasDefaultLights()
      index(1).hasLights()
    }
    collector.expectNoEvents()

    collector.cancel()
    store.dispatch(SetGreenLightOn(true))
//    store.test { assertThat(awaitItems(2)).lastElement().hasLights(green = true) }
    assertThat(store.state).hasLights(green = true)

    collector = store.testIn(this)
    assertThat(collector.awaitItem()).hasLights(green = true)
    collector.expectNoEvents()

    collector.cancel()
  }
}
