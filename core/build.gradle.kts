description = "Core implementation of Redux StoreFlow"

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
    val commonTest by getting {
      dependencies {
        implementation(project(":test-support:internal"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.mockk.core)
      }
    }
  }
}
