---
name: ship-release-skill
description: >-
  Ship a release branch by publishing a GitHub release using the gh CLI. Parses
  version from build.gradle.kts and gets release notes from docs/CHANGELOG.md.
---

# Ship Release Branch Skill

## Overview
This skill guides the agent in shipping a release branch by creating and publishing a GitHub release pointing to the tip of the release branch. It ensures consistency by:
1. Resolving the release version directly from `build.gradle.kts` on the target release branch (never assuming the branch name matches the version name, as hotfixes are appended to existing release branches).
2. Extracting release notes from the corresponding section in `docs/CHANGELOG.md`.
3. Publishing the GitHub release using the `gh` CLI with matching tag name and release name (format: `v<VERSION>`).

## Dependencies
- `release-branch-skill`: Typically run after a release branch is cut and hardened.

## Quick Start
To perform a dry-run and verify release notes/version before shipping:
```bash
./scripts/ship-release.py --dry-run --output /tmp/release-dry-run.json
```

To ship the current release branch:
```bash
./scripts/ship-release.py --output /tmp/release-result.json
```

## Utility Scripts
The skill uses the `./scripts/ship-release.py` script.

### Arguments
- `--branch <branch>`: Target branch/ref to point the release to (defaults to current branch).
- `--dry-run`: Prints release details and the `gh` command without executing them.
- `--output <file_path>`: (Required) Path to write a JSON report of the release results.

### Example JSON output (`/tmp/release-result.json`):
```json
{
  "success": true,
  "dry_run": false,
  "tag": "v1.1.0",
  "title": "v1.1.0",
  "branch": "release/v1.1.0",
  "url": "https://github.com/episode6/redux-store-flow/releases/tag/v1.1.0",
  "notes": "- CI: Use gradle/actions/setup-gradle@v6...\n- Upgraded Kotlin to 2.4.0..."
}
```

## Common Mistakes
1. **Shipping a Snapshot**: Trying to ship when the version in `build.gradle.kts` still contains `-SNAPSHOT`. The script will detect this and fail.
2. **Missing Changelog Section**: Forgetting to update `docs/CHANGELOG.md` with the release version and date. The script will fail if the section matching `v<VERSION>` cannot be found.
3. **Mismatched release notes**: Assuming the release notes can be typed manually. Always extract them directly from `docs/CHANGELOG.md` using the script to avoid discrepancies.
