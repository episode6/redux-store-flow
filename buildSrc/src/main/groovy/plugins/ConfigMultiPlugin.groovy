package plugins

import org.gradle.api.GradleException
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

        def hostOs = System.getProperty("os.name")
        def isMingwX64 = hostOs.startsWith("Windows")
        def nativeTarget
        if (hostOs == "Mac OS X") nativeTarget = macosX64('native')
        else if (hostOs == "Linux") nativeTarget = linuxX64("native")
        else if (isMingwX64) nativeTarget = mingwX64("native")
        else throw new GradleException("Host OS is not supported in Kotlin/Native.")

        configure(nativeTarget) {
          compilations.all {
            kotlinOptions {
              freeCompilerArgs += Config.Kotlin.compilerArgs

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
        }

      }
    }
  }
}
