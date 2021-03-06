enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
  versionCatalogs {
    create("libs") { from(files("libs.versions.toml")) }
  }
}

rootProject.name = "redux-store-flow"

include(
  ":core",
  ":side-effects",
  ":subscriber-aware",
  ":test-support",
)
