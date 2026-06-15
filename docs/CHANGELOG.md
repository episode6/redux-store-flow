# ChangeLog

## v1.1.1 - Unreleased

## v1.1.0 - Released 06/15/2026

- CI: Use gradle/actions/setup-gradle@v6 and actions/setup-java@v5 (Azul Zulu); enable enhanced Gradle caching in GitHub workflows
- Added GitHub Actions workflow and local script (`scripts/verify-docs-updated.sh`) for verifying documentation updates on pull requests.
- Upgraded Kotlin to 2.4.0
- Added Compose Compiler plugin to the build for Kotlin 2.x support.
- Upgraded Gradle to 9.5.1
- Upgraded Coroutines to 1.11.0
- Upgraded Compose Multiplatform to 1.11.1
- Upgraded Turbine to 1.2.1 and fixed tests related to `turbineScope` requirement
- Upgraded JVM target and source compatibility to Java 17
- Added support for new Kotlin Multiplatform targets: `linuxArm64`, `wasmJs`, and `wasmWasi`
- Removed support for Apple Intel (x64) targets: `iosX64`, `macosX64`, `tvosX64`, and `watchosX64`
- Fixed "Default Kotlin Hierarchy Template Not Applied Correctly" build warnings

## v1.0.1 - Released 11/13/2022

- Fix for composable StoreFlow.collectAsState - remember mapped flows

## v1.0.0 - Released 10/04/2022

- First release
