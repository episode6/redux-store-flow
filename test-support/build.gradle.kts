description = "Core implementation of Redux StoreFlow"

plugins {
  id("config-multi")
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

