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
        dependsOn tasks.signJvmPublication, tasks.signJsPublication
      }
      tasks.publishKotlinMultiplatformPublicationToMavenRepository {
        dependsOn tasks.signJvmPublication, tasks.signJsPublication
      }

      tasks.publishJvmPublicationToMavenLocal {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJsPublication
      }
      tasks.publishJvmPublicationToMavenRepository {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJsPublication
      }

      tasks.publishJsPublicationToMavenLocal {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJvmPublication
      }
      tasks.publishJsPublicationToMavenRepository {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJvmPublication
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
