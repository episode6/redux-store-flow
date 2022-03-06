description = "Public api of Redux StoreFlow"

plugins {
  id("config-multi-deploy")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines.core)
      }
    }
  }
}
