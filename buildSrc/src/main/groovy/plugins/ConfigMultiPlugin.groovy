package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class ConfigMultiPlugin implements Plugin<Project> {
  @Override
  void apply(Project target) {
    target.with {
      plugins.apply("org.jetbrains.kotlin.multiplatform")

      def skipTargets = (findProperty("skipTargets")?.split(",") ?: []) as List
      def filter = findProperty("filter")
      if (filter == "x64") {
        skipTargets.addAll(Config.KMPTargets.getNonX64())
      } else if (filter == "nonX64") {
        skipTargets.addAll(Config.KMPTargets.getX64())
      }

      kotlin {
        if (!skipTargets.contains("jvm")) {
          jvm {
            compilations.all {
              kotlinOptions {
                jvmTarget = Config.Jvm.name
                freeCompilerArgs += Config.Kotlin.compilerArgs
              }
            }
            java {
              sourceCompatibility = Config.Jvm.sourceCompat
              targetCompatibility = Config.Jvm.targetCompat
            }
            jvmTest {
              useJUnitPlatform()
              testLogging {
                events "passed", "skipped", "failed"
              }
            }
          }
        }
        if (!skipTargets.contains("js")) {
          js(IR) {
            nodejs()
            browser()
            binaries.library()
            compilations.all {
              kotlinOptions {
                freeCompilerArgs += Config.Kotlin.compilerArgs
              }
            }
          }
        }
        if (!skipTargets.contains("wasmJs")) {
          wasmJs {
            nodejs()
            browser()
            binaries.library()
          }
        }
        if (!skipTargets.contains("wasmWasi")) {
          wasmWasi {
            nodejs()
            binaries.library()
          }
        }


        for (t in Config.KMPTargets.natives - skipTargets) {
          invokeMethod(t, {
            compilations.all {
              kotlinOptions {
                freeCompilerArgs += Config.Kotlin.compilerArgs
              }
            }
          })
        }

        sourceSets {
          commonMain {}
          commonTest {
            dependencies {
              implementation(kotlin("test"))
            }
          }
        }
      }
    }
  }
}
