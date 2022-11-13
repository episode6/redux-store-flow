plugins {
  `java-gradle-plugin`
}

gradlePlugin {
  plugins {
    create("ConfigureSitePlugin") {
      id = "config-site"
      implementationClass = "plugins.ConfigSitePlugin"
    }
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
