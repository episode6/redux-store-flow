# Enforce Docs & Changelog Updates Skill

Purpose

This skill defines a policy and automation for agents making code changes: any change that modifies production/source files must also update the changelog and any relevant documentation.

How it enforces

- A lightweight verification script is provided at scripts/verify-docs-updated.sh. It checks the diff between the current branch and a base ref (default origin/main) and fails if code changes exist without accompanying docs/CHANGELOG/README or other .md updates.
- A GitHub Actions workflow (.github/workflows/verify-docs.yml) runs the verification on pull requests to block merges that don't update docs.

Agent responsibilities

- Run scripts/verify-docs-updated.sh before creating a PR: bash scripts/verify-docs-updated.sh origin/main
- When making changes that affect behavior, APIs, public interfaces, or user-visible output, update docs/CHANGELOG.md and any affected docs/ files.
- If the change is purely cosmetic or docs-only, indicate this in the PR and ensure the changelog reflects the change if release-worthy.

Customization

- The script accepts a base-ref argument. CI will pass the PR base ref automatically.
- Edit the script patterns to match project-specific docs locations or filename conventions.

Notes for maintainers

- This mechanism enforces documentation discipline but is intentionally permissive: it looks for any Markdown/docs changes. For stricter rules (e.g., mapping touched modules to specific docs), extend the script.
- Consider running the script as a local pre-push hook or integrating it into pre-commit if desired.
