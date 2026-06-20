description = "Jetpack Compose support for Redux StoreFlow"

plugins {
  id("config-multi-deploy")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.compose.runtime)
        api(project(":store-flow"))
      }
    }
  }
}
