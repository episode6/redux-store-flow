package plugins


import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete

class ConfigSitePlugin implements Plugin<Project> {
  @Override
  void apply(Project target) {

    if (target != target.rootProject) throw new GradleException("Can only apply ConfigSitePlugin to root project")

    target.with {
      def dokkaDir = layout.buildDirectory.dir("dokka/html")
      def siteDir = layout.buildDirectory.dir("site")

      tasks.register("clearDokkaDir", Delete) {
        delete(dokkaDir)
        doLast { dokkaDir.get().asFile.mkdirs() }
      }

      tasks.register("clearSiteDir", Delete) {
        delete(siteDir)
        doLast { siteDir.get().asFile.mkdirs() }
      }

      tasks.named("dokkaGeneratePublicationHtml") {
        outputDirectory.set(dokkaDir)
      }

      dependencies {
        subprojects.each { sub ->
          // In Dokka 2, aggregation is done by adding subprojects as 'dokka' dependencies
          if (sub.path != ":test-support:internal") {
            dokka(sub)
          }
        }
      }

      tasks.register("copyReadmes", Copy) {
        from(file("docs/"))
        exclude(".gitignore", "_site/", "_config.yml")
        into(siteDir)
      }

      tasks.register("configSite") {
        dependsOn("copyReadmes")
        doLast {
          siteDir.get().file("_config.yml").asFile.write(Config.Site.generateJekyllConfig(target))
        }
      }
    }
  }
}
