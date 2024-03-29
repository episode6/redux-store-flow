import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPom

class Config {
  class Jvm {
    static String name = "1.8"
    static JavaVersion targetCompat = JavaVersion.VERSION_1_8
    static JavaVersion sourceCompat = JavaVersion.VERSION_1_8
  }

  class Kotlin {
    static String compilerArgs = "-opt-in=kotlin.RequiresOptIn"
  }

  public class KMPTargets {
    static String[] linux = [
        "linuxX64",
    ]
    static String[] apple = [
        "iosArm32",
        "iosArm64",
        "iosX64",
        "iosSimulatorArm64",
        "macosX64",
        "macosArm64",
        "tvosArm64",
        "tvosX64",
        "tvosSimulatorArm64",
        "watchosArm32",
        "watchosArm64",
        "watchosX86",
        "watchosX64",
        "watchosSimulatorArm64",
    ]
    static String[] windows = [
        "mingwX64",
    ]
    public static String[] natives = linux + apple + windows
    public static String[] all = natives + ["jvm", "js"]
  }

  class Site {
    static String generateJekyllConfig(Project project) {
      return """
        theme: jekyll-theme-cayman
        title: Redux StoreFlow
        description: ${project.rootProject.description}
        version: ${project.version}
        docsDir: https://episode6.github.io/redux-store-flow/docs/${ if (Config.Maven.isReleaseBuild(project)) "v${project.version}" else "main" }
        kotlinVersion: ${project.libs.versions.kotlin.core.get()}
        coroutineVersion: ${project.libs.versions.kotlinx.coroutines.get()}
""".stripIndent()
    }
  }

  class Maven {
    static void applyPomConfig(Project project, MavenPom pom) {
      pom.with {
        name = project.rootProject.name + "-" + project.name
        url = "https://github.com/episode6/redux-store-flow"
        licenses {
          license {
            name = "The MIT License (MIT)"
            url = "https://github.com/episode6/redux-store-flow/blob/main/LICENSE"
            distribution = "repo"
          }
        }
        developers {
          developer {
            id = "episode6"
            name = "episode6, Inc."
          }
        }
        scm {
          url = "extensible"
          connection = "scm:https://github.com/episode6/redux-store-flow.git"
          developerConnection = "scm:https://github.com/episode6/redux-store-flow.git"
        }
      }
      project.afterEvaluate {
        pom.description = project.description ?: project.rootProject.description
      }
    }

    public static boolean isReleaseBuild(Project project) {
      return project.version.contains("SNAPSHOT") == false
    }

    static String getRepoUrl(Project project) {
      if (isReleaseBuild(project)) {
        return "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
      } else {
        return "https://oss.sonatype.org/content/repositories/snapshots/"
      }
    }
  }
}
