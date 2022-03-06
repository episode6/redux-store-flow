package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class ConfigMultiDeployablePlugin implements Plugin<Project> {
  @Override
  void apply(Project target) {
    target.with {
      plugins.with {
        apply(ConfigMultiPlugin)
        apply(CommonDeployablePlugin)
      }

      // mitigate gradle warning
      tasks.publishKotlinMultiplatformPublicationToMavenLocal {
        dependsOn tasks.signJvmPublication
      }
      tasks.publishJvmPublicationToMavenLocal {
        dependsOn tasks.signKotlinMultiplatformPublication
      }
      tasks.publishKotlinMultiplatformPublicationToMavenRepository {
        dependsOn tasks.signJvmPublication
      }
      tasks.publishJvmPublicationToMavenRepository {
        dependsOn tasks.signKotlinMultiplatformPublication
      }

      publishing {
        publications.withType(MavenPublication) {
          Config.Maven.applyPomConfig(target, pom)
          artifact javadocJar
        }
      }
    }
  }
}
