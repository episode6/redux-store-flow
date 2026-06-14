#!/usr/bin/env bash
set -euo pipefail

# Usage: scripts/assert-architecture.sh [os]
# Example: scripts/assert-architecture.sh ubuntu-latest

OS_NAME=${1:-"unknown"}

case "$OS_NAME" in
  macos-latest)
    if [ "$(uname -m)" != "arm64" ]; then
      echo "ERROR: Not running on arm64 architecture. Found: $(uname -m)"
      exit 1
    fi
    ;;
  ubuntu-latest|windows-latest)
    if [[ ! "$(uname -m)" =~ (x86_64|x64|AMD64) ]]; then
      echo "ERROR: Not running on x64 architecture. Found: $(uname -m)"
      exit 1
    fi
    ;;
  *)
    echo "WARNING: Unknown OS '$OS_NAME'. Skipping architecture assertion."
    ;;
esac

echo "Architecture assertion passed for $OS_NAME ($(uname -m))"
