description = "Core implementation of Redux StoreFlow"

plugins {
  id("config-multi-deploy")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":api"))
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
