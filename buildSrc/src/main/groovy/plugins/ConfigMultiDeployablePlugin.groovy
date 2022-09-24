package plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.plugins.signing.Sign

class ConfigMultiDeployablePlugin implements Plugin<Project> {
  @Override
  void apply(Project target) {
    target.with {
      plugins.with {
        apply(ConfigMultiPlugin)
        apply(CommonDeployablePlugin)
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
