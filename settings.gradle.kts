enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
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
  ":compose",

  ":test-support",
  ":test-support:internal",
)
