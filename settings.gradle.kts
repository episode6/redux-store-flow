dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
  versionCatalogs {
    create("libs") { from(files("libs.versions.toml")) }
  }
}

rootProject.name = "redux"

include(
  ":store-flow",
  ":side-effects",
  ":subscriber-aware",
  ":compose",

  ":test-support",
  ":test-support:internal",
)
