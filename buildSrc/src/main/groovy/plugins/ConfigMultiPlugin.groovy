package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class ConfigMultiPlugin implements Plugin<Project> {
  @Override
  void apply(Project target) {
    target.with {
      plugins.with {
        apply("org.jetbrains.kotlin.multiplatform")
      }

      def skipTargets = findProperty("skipTargets")?.split(",") ?: []

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


        for (t in Config.KMPTargets.natives - skipTargets) {
          targets.add(presets.getByName(t).createTarget(t)) {
            compilations.all {
              kotlinOptions {
                freeCompilerArgs += Config.Kotlin.compilerArgs
              }
            }
          }
        }

        sourceSets {
          commonMain {}
          commonTest {
            dependencies {
              implementation(kotlin("test"))
            }
          }
          
          for (sourceSet in Config.KMPTargets.all - skipTargets) {
            getByName("${sourceSet}Main") {
              dependsOn(commonMain)
            }
          }
        }
      }
    }
  }
}
