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

      def linuxTargets = ["linuxX64"]
      def appleTargets = ["macosX64"]
      def windowsTargets = ["mingwX64"]
      def nativeTargets = linuxTargets + appleTargets + windowsTargets
      def noopTargets = nativeTargets + ["js"]

      kotlin {
        jvm  {
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
        js {
          browser()
          nodejs()
          compilations.all {
            kotlinOptions {
              freeCompilerArgs += Config.Kotlin.compilerArgs
            }
          }
        }
        for (t in nativeTargets) {
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
          noopMain {
            dependsOn commonMain
          }
          for (sourceSet in noopTargets) {
            getByName("${sourceSet}Main") {
              dependsOn(noopMain)
            }
          }
        }
      }

      task("assembleApple", dependsOn: tasks.macosX64MainKlibrary)
      task("testApple", dependsOn: tasks.macosX64Test)
      task("assembleWindows", dependsOn: tasks.mingwX64MainKlibrary)
      task("testWindows") // assertK not supported on windows yet
    }
  }
}
