package com.episode6.redux.compose

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.episode6.redux.testsupport.internal.stoplight.SetRedLightOn
import com.episode6.redux.testsupport.internal.stoplight.hasLights
import com.episode6.redux.testsupport.internal.stoplight.stopLightStoreTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoreComposeExtTest {

  @Test fun firstComposition_reflectsCurrentState_notInitialState() = stopLightStoreTest { store ->
    store.dispatch(SetRedLightOn(false))
    check(!store.state.redLight) { "dispatch did not apply synchronously" }

    composeTest(content = { recordFrame(store.collectAsState().value) }) {
      assertThat(frames.first()).hasLights()
    }
  }

  @Test fun firstComposition_reflectsCurrentState_mapped() = stopLightStoreTest { store ->
    store.dispatch(SetRedLightOn(false))

    composeTest(content = { recordFrame(store.collectAsState { it.redLight }.value) }) {
      assertThat(frames.first()).isEqualTo(false)
    }
  }

  @Test fun recomposes_whenStateChanges() = stopLightStoreTest { store ->
    composeTest(content = { recordFrame(store.collectAsState().value) }) {
      assertThat(frames.first()).hasLights(red = true)

      store.dispatch(SetRedLightOn(false))
      awaitFrame()

      assertThat(frames.last()).hasLights()
    }
  }
}

private object UnitApplier : AbstractApplier<Unit>(Unit) {
  override fun insertTopDown(index: Int, instance: Unit) {}
  override fun insertBottomUp(index: Int, instance: Unit) {}
  override fun remove(index: Int, count: Int) {}
  override fun move(from: Int, to: Int, count: Int) {}
  override fun onClear() {}
}

/**
 * Runs [content] in a real composition (headless, no ui) so we can observe the value
 * composed on each frame, including the very first one.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private class ComposeTestScope<T>(private val testScope: TestScope, private val clock: BroadcastFrameClock) {
  val frames = mutableListOf<T>()

  fun recordFrame(value: T) { frames += value }

  fun awaitFrame() = with(testScope) {
    advanceUntilIdle() // let collection coroutines observe the change
    clock.sendFrame(0) // recompose
    advanceUntilIdle() // apply the recomposition
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
private suspend fun <T> TestScope.composeTest(
  content: @Composable ComposeTestScope<T>.() -> Unit,
  testBody: suspend ComposeTestScope<T>.() -> Unit,
) {
  val clock = BroadcastFrameClock()
  val scope = ComposeTestScope<T>(this, clock)
  val recomposer = Recomposer(coroutineContext + clock)
  val runner = launch(clock) { recomposer.runRecomposeAndApplyChanges() }
  val composition = Composition(UnitApplier, recomposer)
  try {
    composition.setContent { scope.content() }
    advanceUntilIdle() // let launched effects start collecting
    testBody(scope)
  } finally {
    composition.dispose()
    recomposer.close()
    runner.cancelAndJoin()
  }
}
