plugins {
  `java-gradle-plugin`
}

dependencies {
  runtimeOnly(libs.bundles.gradle.plugins)
}

gradlePlugin {
  plugins {
    create("ConfigureMultiPlugin") {
      id = "config-multi"
      implementationClass = "plugins.ConfigMultiPlugin"
    }
    create("ConfigureMultiDeployable") {
      id = "config-multi-deploy"
      implementationClass = "plugins.ConfigMultiDeployablePlugin"
    }
  }
}
