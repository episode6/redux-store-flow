plugins {
  `java-gradle-plugin`
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
