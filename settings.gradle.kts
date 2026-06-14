dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
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
