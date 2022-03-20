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
        dependsOn tasks.signJvmPublication, tasks.signJsPublication, tasks.signNativePublication
      }
      tasks.publishKotlinMultiplatformPublicationToMavenRepository {
        dependsOn tasks.signJvmPublication, tasks.signJsPublication, tasks.signNativePublication
      }

      tasks.publishJvmPublicationToMavenLocal {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJsPublication, tasks.signNativePublication
      }
      tasks.publishJvmPublicationToMavenRepository {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJsPublication, tasks.signNativePublication
      }

      tasks.publishJsPublicationToMavenLocal {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJvmPublication, tasks.signNativePublication
      }
      tasks.publishJsPublicationToMavenRepository {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJvmPublication, tasks.signNativePublication
      }

      tasks.publishNativePublicationToMavenLocal {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJvmPublication, tasks.signJsPublication
      }
      tasks.publishNativePublicationToMavenRepository {
        dependsOn tasks.signKotlinMultiplatformPublication, tasks.signJvmPublication, tasks.signJsPublication
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
