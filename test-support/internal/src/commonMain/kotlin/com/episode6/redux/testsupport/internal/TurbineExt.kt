package com.episode6.redux.testsupport.internal

import app.cash.turbine.ReceiveTurbine

suspend fun <T> ReceiveTurbine<T>.awaitItems(count: Int): List<T> {
  val list = mutableListOf<T>()
  repeat(count) { list += awaitItem() }
  return list
}
