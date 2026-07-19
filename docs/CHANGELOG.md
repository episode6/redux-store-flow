# ChangeLog

## v1.2.0 - Unreleased

- Reworked `SideEffectMiddleware`'s action relay to be safe by default ([issue 116](https://github.com/episode6/redux-store-flow/issues/116), problem P1): each side-effect now gets its own unlimited action buffer instead of sharing a single zero-buffer relay gated on every side-effect subscribing.
  - A side-effect that never reads `actions` (e.g. one that only observes an external flow) no longer starves all other side-effects; it simply opts out of receiving actions. The `merge(actions.filter { false }, ...)` workaround is no longer necessary (but remains harmless).
  - A side-effect that suspends inline while processing an action no longer stalls action delivery to other side-effects; it only delays its own queue.
  - `SideEffect.act()` is now invoked synchronously during store setup, before the store processes its first action; only the collection of the returned flow is launched asynchronously.
  - Actions are delivered to each side-effect in dispatch order and consumed in FIFO order; delivery order *across* different side-effects remains intentionally unspecified.
- Remove legacy Jenkinsfile (CI runs entirely on GitHub Actions)

## v1.1.7 - Released 07/11/2026

- Fix composable `StoreFlow.collectAsState` rendering its first frame from stale state: it now seeds compose with the store's current `state` instead of the construction-time `initialState`
- Move version name source of truth into `self.versions.toml` (build.gradle.kts, `ship-release.py` and release skills now read it from there)

## v1.1.6 - Released 06/20/2026

- Modernized Gradle configuration: replaced deprecated function calls and patterns with modern equivalents (Task Configuration Avoidance, Kotlin 2.0 `compilerOptions` DSL, and `layout.buildDirectory`).

## v1.1.5 - Released 06/19/2026

- Attempting to fix site deployment issue.

## v1.1.4 - Released 06/19/2026

- CI: Fix snapshot artifact detection and upload in `publish-artifacts` workflow.

## v1.1.3 - Released 06/18/2026

## v1.1.2 - Released 06/16/2026

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
