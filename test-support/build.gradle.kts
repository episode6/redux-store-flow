description = "Test support utilities for Redux StoreFlow"

plugins {
  id("config-multi-deploy")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.test)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(project(":core"))
      }
    }
  }
}

