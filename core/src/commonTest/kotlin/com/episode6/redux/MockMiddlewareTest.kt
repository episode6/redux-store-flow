package com.episode6.redux

import assertk.assertThat
import com.episode6.redux.stoplight.SetRedLightOn
import com.episode6.redux.stoplight.StopLightState
import com.episode6.redux.stoplight.createStopLightStore
import com.episode6.redux.stoplight.hasLights
import com.episode6.redux.testsupport.runTest
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineScope
import kotlin.test.Test

class MockMiddlewareTest {
  private val dispatch1: Dispatch = mockk(relaxed = true)
  private val dispatch2: Dispatch = mockk(relaxed = true)

  private fun CoroutineScope.wellBehavedStore(): StoreFlow<StopLightState> =
    createStopLightStore(WellBehavedMiddleware(dispatch1), WellBehavedMiddleware(dispatch2))

  private fun CoroutineScope.badlyBehavedStore(): StoreFlow<StopLightState> =
    createStopLightStore(BadlyBehavedMiddleware(dispatch1), BadlyBehavedMiddleware(dispatch2))

  @Test fun testWellBehaved() = runTest {
    val store = wellBehavedStore()
    val action = SetRedLightOn(false)

    store.dispatch(action)

    verifyOrder {
      dispatch2.invoke(action)
      dispatch1.invoke(action)
    }
    assertThat(store.value).hasLights()
  }

  @Test fun testBadlyBehaved() = runTest {
    val store = badlyBehavedStore()
    val action = SetRedLightOn(false)

    store.dispatch(action)

    verifyOrder {
      dispatch1.invoke(action)
      dispatch2.invoke(action)
    }
    assertThat(store.value).hasLights()
  }
}

private fun <T> WellBehavedMiddleware(dispatch: Dispatch): Middleware<T> = Middleware { _, next ->
  {
    next(it)
    dispatch(it)
  }
}

private fun <T> BadlyBehavedMiddleware(dispatch: Dispatch): Middleware<T> = Middleware { _, next ->
  {
    dispatch(it)
    next(it)
  }
}
