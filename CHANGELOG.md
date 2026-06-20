# ChangeLog

## v1.1.6 - Unreleased

## v1.1.5 - Released 06/18/2026

- Attempting to fix site deployment issue.

## v1.1.4 - Unreleased

- CI: Fix snapshot artifact detection and upload in `publish-artifacts` workflow.

## v1.1.3 - Unreleased

## v1.1.2 - Unreleased

- Fix release publication workflow to include missing linux-x64 and windows-x64 artifacts.

## v1.1.1 - Released 06/15/2026

- Fix publication failure from v1.1.0 ([711a9c2](https://github.com/episode6/redux-store-flow/commit/711a9c2803e40f5aa6feaef99193a0f473c92d45))

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
