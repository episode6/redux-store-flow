description = "Internal test support utilities for Redux StoreFlow"

plugins {
  id("config-multi")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.coroutines.test)
        api(libs.assertk.core)
        api(libs.turbine)
        api(project(":test-support"))
        implementation(project(":core"))
      }
    }
  }
}

