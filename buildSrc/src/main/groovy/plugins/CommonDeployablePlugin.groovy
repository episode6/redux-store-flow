package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

class CommonDeployablePlugin implements Plugin<Project> {
  @Override void apply(Project target) {
    target.with {
      plugins.with {
        apply("org.jetbrains.dokka")
        apply("maven-publish")
        apply("signing")
      }

      task("deploy", dependsOn: tasks.publish)
      task("install", dependsOn: tasks.publishToMavenLocal)

      task("javadocJar", type: Jar, dependsOn: tasks.dokkaHtml) {
        archiveClassifier.set('javadoc')
        from tasks.dokkaHtml
      }

      signing {
        def signingKey = findProperty("signingKey")
        def signingPassword = findProperty("signingPassword")
        if (signingKey != null && signingPassword != null) {
          useInMemoryPgpKeys(signingKey, signingPassword)
        }
        sign publishing.publications
      }

      publishing {
        repositories {
          maven {
            url Config.Maven.getRepoUrl(target)
            credentials {
              username findProperty("nexusUsername")
              password findProperty("nexusPassword")
            }
          }
        }
      }
    }
  }
}
