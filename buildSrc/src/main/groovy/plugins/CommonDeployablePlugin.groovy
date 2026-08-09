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
      }

      kotlin {
        explicitApi()
      }

      dokka {
        dokkaSourceSets.configureEach {
          externalDocumentationLinks.register("coroutines") {
            url.set(new URI("https://kotlinlang.org/api/kotlinx.coroutines/"))
          }
        }
      }

      tasks.register("deploy") {
        dependsOn(tasks.named("publish"))
      }
      tasks.register("install") {
        dependsOn(tasks.named("publishToMavenLocal"))
      }

      tasks.register("javadocJar", Jar) {
        dependsOn("dokkaGeneratePublicationHtml")
        archiveClassifier.set('javadoc')
        from tasks.named("dokkaGeneratePublicationHtml")
      }

      publishing {
        repositories {
          maven {
            name = "sonatype"
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
