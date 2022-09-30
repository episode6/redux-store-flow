plugins {
  id("org.jetbrains.kotlin.multiplatform") version (libs.versions.kotlin.core.get()) apply (false)
  id("org.jetbrains.dokka") version (libs.versions.dokka.core.get())
  id("org.jetbrains.compose") version (libs.versions.compose.core.get()) apply (false)
}

allprojects {
  group = "com.episode6.redux"
  version = "1.0.0-SNAPSHOT"
}

tasks.wrapper {
  gradleVersion = libs.versions.gradle.core.get()
  distributionType = Wrapper.DistributionType.ALL
}

val dokkaDir = "build/dokka/html"

tasks.create<Delete>("clearDocsDir") {
  delete(dokkaDir)
  doLast { file("$rootDir/$dokkaDir").mkdirs() }
}

tasks.dokkaHtmlMultiModule {
  dependsOn("clearDocsDir")
  outputDirectory.set(file("$rootDir/$dokkaDir"))
}

tasks.create("configDocs") {
  doLast {
    file("$rootDir/docs/_config.yml").writeText(
      """
        theme: jekyll-theme-cayman
        title: Redux StoreFlow
        description: A kotlin implementation of Redux, backed by StateFlows and Coroutines
        version: $version
        docsDir: https://episode6.github.io/redux-store-flow/docs/${ if (Config.Maven.isReleaseBuild(project)) version else "main" }
      """.trimIndent()
    )
  }
}

tasks.create("syncDocs") {
  dependsOn("dokkaHtmlMultiModule", "configDocs")
}
