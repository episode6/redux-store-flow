description = "Jetpack Compose support for Redux StoreFlow"

plugins {
  id("com.android.library")
  id("config-multi-deploy")
  id("org.jetbrains.compose")
}

kotlin {
  android()

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(project(":core"))
        api(compose.runtime)
      }
    }
    val androidMain by getting {
      dependsOn(commonMain)
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.jvmTarget = Config.Jvm.name
}

android {
  compileSdk = 31

  defaultConfig {
    minSdk = 21
    targetSdk = 31
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

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }
}
