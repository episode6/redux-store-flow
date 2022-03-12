description = "Core implementation of Redux StoreFlow"

plugins {
  id("config-multi")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.coroutines.test)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.mockk.core)
        implementation(libs.assertk.core)
      }
    }
  }
}
