plugins {
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.jetbrains.compose) apply false
  id("config-site")
}

allprojects {
  group = "com.episode6.redux"
  version = "1.1.6-SNAPSHOT"
}
description = "Yet another kotlin implementation of Redux, backed by StateFlows and Coroutines"

tasks.wrapper {
  gradleVersion = libs.versions.gradle.core.get()
  distributionType = Wrapper.DistributionType.ALL
}
