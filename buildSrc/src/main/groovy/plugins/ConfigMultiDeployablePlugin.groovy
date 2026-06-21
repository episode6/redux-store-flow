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
      tasks.withType(AbstractPublishToMaven).configureEach { pubTask ->
        pubTask.dependsOn(signTasks)
      }

      publishing {
        publications.withType(MavenPublication).configureEach { pub ->
          Config.Maven.applyPomConfig(target, pub.pom)
          pub.artifact tasks.named("javadocJar")
        }
      }
    }
  }
}
