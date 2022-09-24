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
        api(libs.assertk.core)
        implementation(project(":core"))
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(libs.assertk.core)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}
