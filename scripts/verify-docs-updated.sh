#!/usr/bin/env bash
set -euo pipefail

# Usage: scripts/verify-docs-updated.sh [base-ref]
# Default base-ref: origin/main

BASE_REF=${1:-origin/main}

# If the specified ref doesn't exist locally, try resolving it as origin/<ref>
if ! git rev-parse --verify "$BASE_REF" >/dev/null 2>&1; then
  if git rev-parse --verify "origin/$BASE_REF" >/dev/null 2>&1; then
    BASE_REF="origin/$BASE_REF"
  fi
fi

# Ensure we have the base for comparison
git fetch origin --no-tags --prune || true

# Compute changed files between base and HEAD
CHANGED_FILES=$(git diff --name-only "$BASE_REF"...HEAD)

if [ -z "$CHANGED_FILES" ]; then
  echo "No code changes detected."
  exit 0
fi

echo "Changed files:\n$CHANGED_FILES"

# Define what constitutes a docs change
is_doc_change() {
  grep -E '^(docs/|CHANGELOG\.md$|README\.md$|.*\.md$|.github/workflows/)' <<< "$CHANGED_FILES" >/dev/null 2>&1
}

# Define what constitutes a code change (anything not a doc/markdown or workflow)
has_code_change() {
  # Files that are considered docs or metadata are excluded from "code"
  grep -vE '^(docs/|CHANGELOG\.md$|README\.md$|.*\.md$|.github/workflows/)' <<< "$CHANGED_FILES" | grep -q . || return 1
}

DOCS_UPDATED=false
CODE_CHANGED=false

if is_doc_change; then
  DOCS_UPDATED=true
fi

if has_code_change; then
  CODE_CHANGED=true
fi

if [ "$CODE_CHANGED" = true ] && [ "$DOCS_UPDATED" = false ]; then
  echo "\nERROR: Code changes detected but no documentation or changelog updates found."
  echo "Please update docs/CHANGELOG.md or other relevant docs (docs/, README.md, *.md) to describe user-facing changes."
  echo "Changed files were:\n$CHANGED_FILES"
  exit 1
fi

if [ "$DOCS_UPDATED" = true ]; then
  echo "Docs updated."
else
  echo "No code changes detected."
fi
exit 0
