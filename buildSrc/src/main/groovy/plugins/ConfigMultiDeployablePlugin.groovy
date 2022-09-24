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

      // mitigate gradle warnings by ensuring all pub tasks depend on all sign tasks
      def signTasks = tasks.withType(Sign)
      tasks.withType(AbstractPublishToMaven).forEach { pubTask ->
        signTasks.forEach {
          pubTask.dependsOn(it)
        }
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
