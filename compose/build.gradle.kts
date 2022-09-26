description = "Jetpack Compose support for Redux StoreFlow"

plugins {
  id("com.android.library")
  kotlin("multiplatform")
  id("org.jetbrains.compose")
}

kotlin {
  android()
  jvm("desktop")

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines.core)
//        api(project(":core"))
        api(compose.runtime)
      }
    }
    val androidMain by getting {
      dependsOn(commonMain)
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = "11"
}

android {
  compileSdkVersion(31)

  defaultConfig {
    minSdkVersion(21)
    targetSdkVersion(31)
  }

  compileOptions {
    sourceCompatibility = Config.Jvm.sourceCompat
    targetCompatibility = Config.Jvm.targetCompat
  }

  sourceSets {
    named("main") {
      manifest.srcFile("src/androidMain/AndroidManifest.xml")
    }
  }
}
