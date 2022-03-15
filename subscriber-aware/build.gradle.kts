description = "Subscriber-Aware version of StoreFlow"

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
