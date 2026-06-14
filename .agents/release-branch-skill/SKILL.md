# Cut Release Branch Skill

This skill automates and describes the process of cutting a new release branch and preparing the version bumps, as defined in `RELEASE_CHECKLIST.md`.

## Steps to Execute

### 1. Pre-check
- Ensure the `main` branch is passing all CI checks (is "green").

### 2. Cut new Release Branch
- Checkout the `main` branch and pull the latest changes.
- Create a new branch: `git checkout -b release/v<VERSION>`
- Push the empty branch and set it to be tracked: `git push -u origin release/v<VERSION>`

### 3. Version Bump PRs
Create two separate Pull Requests to update versions.

#### PR 1: Snapshot Version on `main`
- **Target Branch:** `main`
- **PR Title:** `[VERSION] Snapshot v<NEXT_VERSION>-SNAPSHOT`
- **Changes:**
    - Update `version` in `build.gradle.kts`.
    - Update `docs/CHANGELOG.md` if necessary.

#### PR 2: Release Version on Release Branch
- **Target Branch:** `release/v<VERSION>`
- **PR Title:** `[VERSION] Release v<VERSION>`
- **Changes:**
    - Update `version` in `build.gradle.kts` (remove `-SNAPSHOT` if present).
    - Update `docs/CHANGELOG.md` with release date and final version.
    - Run `./gradlew syncDocs` and include any resulting changes in the commit.

## Verification
- After these steps, the project is ready for the "Harden Release Branch" phase, which requires manual verification and cherry-picking of bug fixes.
