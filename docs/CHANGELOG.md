# ChangeLog

## v1.1.0-SNAPSHOT - Unreleased

- CI: Use gradle/actions/setup-gradle@v6 and actions/setup-java@v5 (Azul Zulu); enable basic Gradle caching in GitHub workflows
- Upgraded Kotlin to 2.4.0
- Upgraded Gradle to 9.5.1
- Upgraded Coroutines to 1.11.0
- Upgraded Compose Multiplatform to 1.11.1
- Upgraded Turbine to 1.2.1 and fixed tests related to `turbineScope` requirement
- Upgraded JVM target and source compatibility to Java 17
- Added support for new Kotlin Multiplatform targets: `linuxArm64` and `wasmJs`
- Removed support for Apple Intel (x64) targets: `iosX64`, `macosX64`, `tvosX64`, and `watchosX64`
- Fixed "Default Kotlin Hierarchy Template Not Applied Correctly" build warnings

## v1.0.1 - Released 11/13/2022

- Fix for composable StoreFlow.collectAsState - remember mapped flows

## v1.0.0 - Released 10/04/2022

- First release
