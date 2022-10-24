plugins {
  id("org.jetbrains.kotlin.multiplatform") version (libs.versions.kotlin.core.get()) apply (false)
  id("org.jetbrains.dokka") version (libs.versions.dokka.core.get())
  id("org.jetbrains.compose") version (libs.versions.compose.core.get()) apply (false)
}

allprojects {
  group = "com.episode6.redux"
  version = "1.0.1-SNAPSHOT"
}
description = "Yet another kotlin implementation of Redux, backed by StateFlows and Coroutines"

tasks.wrapper {
  gradleVersion = libs.versions.gradle.core.get()
  distributionType = Wrapper.DistributionType.ALL
}

val dokkaDir = "${rootProject.buildDir}/dokka/html"
val siteDir = "${rootProject.buildDir}/site"

tasks.create<Delete>("clearDokkaDir") {
  delete(dokkaDir)
  doLast { file(dokkaDir).mkdirs() }
}

tasks.dokkaHtmlMultiModule {
  dependsOn("clearDokkaDir")
  outputDirectory.set(file(dokkaDir))
}

tasks.create<Copy>("copyReadmes") {
  from(file("docs/"))
  destinationDir = file(siteDir)
}

tasks.create("configDocs") {
  dependsOn("copyReadmes")
  doLast {
    file("$siteDir/_config.yml").writeText(
      """
        theme: jekyll-theme-cayman
        title: Redux StoreFlow
        description: ${rootProject.description}
        version: $version
        docsDir: https://episode6.github.io/redux-store-flow/docs/${ if (Config.Maven.isReleaseBuild(project)) "v$version" else "main" }
        kotlinVersion: ${libs.versions.kotlin.core.get()}
        coroutineVersion: ${libs.versions.kotlinx.coroutines.get()}
      """.trimIndent()
    )
  }
}

tasks.create("syncDocs") {
  dependsOn("dokkaHtmlMultiModule", "configDocs")
}
