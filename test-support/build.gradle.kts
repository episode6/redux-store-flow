description = "Core implementation of Redux StoreFlow"

plugins {
  id("config-multi")
}

// assertK doesn't support windows builds yet, simply disabling the task results doesn't solve the issue
rootProject.gradle.startParameter.excludedTaskNames.add(":test-support:compileKotlinMingwX64")

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(libs.kotlinx.coroutines.test)
        api(libs.assertk.core)
        api(libs.turbine)
        implementation(project(":core"))
      }
    }
  }
}

