@file:OptIn(ExperimentalCoroutinesApi::class)

package com.episode6.redux.subscriberaware

import app.cash.turbine.test
import app.cash.turbine.testIn
import assertk.all
import assertk.assertThat
import assertk.assertions.*
import com.episode6.redux.Action
import com.episode6.redux.Middleware
import com.episode6.redux.testsupport.internal.awaitItems
import com.episode6.redux.testsupport.internal.stoplight.*
import com.episode6.redux.testsupport.runStoreTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test

class SubscriberAwareStoreFlowTest {

  private val actions = mutableListOf<Action>()
  private val middleware = Middleware<StopLightState> { _, next ->
    {
      actions += it
      next(it)
    }
  }

  private fun subscriberAwareStore(scope: CoroutineScope) = SubscriberAwareStoreFlow(
    scope = scope,
    initialValue = StopLightState(),
    reducer = StopLightState::reduce,
    middlewares = listOf(middleware)
  )

  @Suppress("UNUSED_VARIABLE")
  @Test fun testNoInitialAction() = runStoreTest(::subscriberAwareStore) {
    assertThat(actions).isEmpty()
  }

  @Test fun testSubscribe() = runStoreTest(::subscriberAwareStore) { store ->

    store.test {
      assertThat(actions).containsExactly(SubscriberStatusChanged(subscribersActive = true))
      assertThat(awaitItem()).hasDefaultLights()

      ensureAllEventsConsumed()
    }
  }

  @Test fun testUnSubscribe() = runStoreTest(::subscriberAwareStore) { store ->

    store.testIn(this).cancelAndIgnoreRemainingEvents()

    assertThat(actions).all {
      hasSize(2)
      index(0).isEqualTo(SubscriberStatusChanged(subscribersActive = true))
      index(1).isEqualTo(SubscriberStatusChanged(subscribersActive = false))
    }
  }

  @Test fun testMultipleSubscribers() = runStoreTest(::subscriberAwareStore) { store ->

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

  @Test fun testDanglingSubscriber() = runStoreTest(::subscriberAwareStore) { store ->

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

  @Test fun testMultipleSubscribers_unsubscribe() = runStoreTest(::subscriberAwareStore) { store ->

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

  @Test fun testValue_switchLight() = runStoreTest(::subscriberAwareStore) { store ->

    store.dispatch(SetRedLightOn(false))
    store.dispatch(SetGreenLightOn(true))

    assertThat(store.state).hasLights(green = true)
  }

  @Test fun testDoesNotEmitStaleState() = runStoreTest(::subscriberAwareStore) { store ->

    val collector = store.testIn(this)
    store.dispatch(SetRedLightOn(false))
    assertThat(collector.awaitItems(2)).all {
      index(0).hasDefaultLights()
      index(1).hasLights()
    }
    collector.ensureAllEventsConsumed()
    collector.cancel()

    store.dispatch(SetGreenLightOn(true))

    assertThat(store.state).hasLights(green = true)

    val collector2 = store.testIn(this)

    assertThat(collector2.awaitItem()).hasLights(green = true)

    collector2.ensureAllEventsConsumed()
    collector2.cancel()
  }
}
