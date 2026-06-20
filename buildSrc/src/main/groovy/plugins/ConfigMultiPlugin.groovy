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
      if (filter == "linuxX64") {
        skipTargets.addAll(Config.KMPTargets.all.findAll { it != "linuxX64" })
      } else if (filter == "windowsX64") {
        skipTargets.addAll(Config.KMPTargets.all.findAll { it != "mingwX64" })
      } else if (filter == "macos") {
        skipTargets.addAll(Config.KMPTargets.getLinuxX64())
        skipTargets.addAll(Config.KMPTargets.getWindowsX64())
      }

      kotlin {
        if (!skipTargets.contains("jvm")) {
          jvm {
            compilerOptions {
              jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(Config.Jvm.name))
              freeCompilerArgs.add(Config.Kotlin.compilerArgs)
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
          js {
            nodejs()
            browser()
            binaries.library()
            compilerOptions {
              freeCompilerArgs.add(Config.Kotlin.compilerArgs)
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
            compilerOptions {
              freeCompilerArgs.add(Config.Kotlin.compilerArgs)
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
