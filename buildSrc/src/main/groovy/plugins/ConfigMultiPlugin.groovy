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

      kotlin {
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


        for (t in Config.KMPTargets.filterNatives(path)) {
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
          
          for (sourceSet in Config.KMPTargets.filterAll(path)) {
            getByName("${sourceSet}Main") {
              dependsOn(commonMain)
            }
          }
        }
      }
    }
  }
}
