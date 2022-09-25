package com.episode6.redux.testsupport.internal

import assertk.Assert
import assertk.assertions.support.appendName
import assertk.assertions.support.expected

fun <T> Assert<List<T>>.lastElement() = transform(appendName("lastElement")) {
  when {
    it.isEmpty() -> expected("list to not be empty")
    else         -> it.last()
  }
}
