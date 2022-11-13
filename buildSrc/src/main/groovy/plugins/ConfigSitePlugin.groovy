package plugins


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete

class ConfigSitePlugin implements Plugin<Project> {
  @Override
  void apply(Project target) {

    if (target != target.rootProject) throw GradleException("Can only apply ConfigSitePlugin to root project")

    target.with {
      def dokkaDir = "${rootProject.buildDir}/dokka/html"
      def siteDir = "${rootProject.buildDir}/site"

      if (tasks.findByName("clean") == null) {
        tasks.create("clean", Delete) {
          delete(rootProject.buildDir)
        }
      }

      tasks.create("clearDokkaDir", Delete) {
        delete(dokkaDir)
        doLast { file(dokkaDir).mkdirs() }
      }

      tasks.create("clearSiteDir", Delete) {
        delete(siteDir)
        doLast { file(siteDir).mkdirs() }
      }

      tasks.dokkaHtmlMultiModule {
        outputDirectory.set(file(dokkaDir))
      }

      tasks.create("copyReadmes", Copy) {
        from(file("docs/"))
        exclude(".gitignore", "_site/", "_config.yml")
        into(file(siteDir))
      }

      tasks.create("configSite") {
        dependsOn("copyReadmes")
        doLast {
          file("$siteDir/_config.yml").write(Config.Site.generateJekyllConfig(target))
        }
      }

      tasks.create("syncDocs") {
        dependsOn("dokkaHtmlMultiModule", "configSite")
      }
    }
  }
}
