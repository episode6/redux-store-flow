plugins {
  id("org.jetbrains.kotlin.multiplatform") version (libs.versions.kotlin.core.get()) apply (false)
  id("org.jetbrains.dokka") version (libs.versions.dokka.core.get())
  id("org.jetbrains.compose") version (libs.versions.compose.core.get()) apply (false)
  id("config-site")
}

allprojects {
  group = "com.episode6.redux"
  version = "1.0.2-SNAPSHOT"
}
description = "Yet another kotlin implementation of Redux, backed by StateFlows and Coroutines"

tasks.wrapper {
  gradleVersion = libs.versions.gradle.core.get()
  distributionType = Wrapper.DistributionType.ALL
}
