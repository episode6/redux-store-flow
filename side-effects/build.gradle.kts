description = "Side Effects for Redux StoreFlow"

plugins {
  id("config-multi-deploy")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":core"))
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(project(":test-support"))
      }
    }
  }
}
