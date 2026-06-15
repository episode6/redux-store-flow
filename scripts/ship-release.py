#!/usr/bin/env python3
import argparse
import json
import os
import re
import subprocess
import sys
import tempfile

def get_current_branch():
    try:
        result = subprocess.run(
            ["git", "rev-parse", "--abbrev-ref", "HEAD"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=True
        )
        return result.stdout.strip()
    except subprocess.CalledProcessError as e:
        print(f"Error getting current git branch: {e.stderr.strip()}", file=sys.stderr)
        sys.exit(1)

def get_version():
    gradle_file = "build.gradle.kts"
    if not os.path.exists(gradle_file):
        print(f"Error: {gradle_file} not found in the current directory.", file=sys.stderr)
        sys.exit(1)
        
    with open(gradle_file, "r") as f:
        content = f.read()
        
    # Search for version = "..."
    match = re.search(r'version\s*=\s*"([^"]+)"', content)
    if not match:
        print("Error: Could not find version pattern in build.gradle.kts", file=sys.stderr)
        sys.exit(1)
        
    version = match.group(1)
    if version.endswith("-SNAPSHOT"):
        print(f"Error: Version '{version}' ends with -SNAPSHOT. Release version must be resolved/bumped first.", file=sys.stderr)
        sys.exit(1)
        
    return version

def get_changelog_notes(version):
    changelog_file = "docs/CHANGELOG.md"
    if not os.path.exists(changelog_file):
        print(f"Error: {changelog_file} not found.", file=sys.stderr)
        sys.exit(1)
        
    with open(changelog_file, "r") as f:
        lines = f.readlines()
        
    notes = []
    found_section = False
    
    # We look for a line starting with '## v<version>'
    header_pattern = re.compile(rf"^##\s+v{re.escape(version)}(\s+|$|-)")
    any_header_pattern = re.compile(r"^##\s+v\d+")
    
    for line in lines:
        if found_section:
            if any_header_pattern.match(line):
                break
            notes.append(line)
        elif header_pattern.match(line):
            found_section = True
            
    if not found_section:
        print(f"Error: Could not find changelog section for v{version} in docs/CHANGELOG.md", file=sys.stderr)
        sys.exit(1)
        
    content = "".join(notes).strip()
    if not content:
        print(f"Warning: Changelog notes for v{version} are empty.", file=sys.stderr)
        
    return content

def run_gh_release(version, notes, target_branch, dry_run=False):
    tag_name = f"v{version}"
    release_name = f"v{version}"
    
    # Create temp file for release notes
    with tempfile.NamedTemporaryFile(mode='w+', suffix='.md', delete=False) as temp_notes:
        temp_notes.write(notes)
        temp_notes_path = temp_notes.name
        
    try:
        cmd = [
            "gh", "release", "create", tag_name,
            "--title", release_name,
            "--notes-file", temp_notes_path,
            "--target", target_branch
        ]
        
        if dry_run:
            print("[DRY-RUN] Would execute command:")
            print(" ".join(cmd))
            print("\n[DRY-RUN] Release Notes:")
            print("-" * 40)
            print(notes)
            print("-" * 40)
            return {
                "success": True,
                "dry_run": True,
                "tag": tag_name,
                "title": release_name,
                "branch": target_branch,
                "command": " ".join(cmd),
                "notes": notes
            }
        else:
            print(f"Running: {' '.join(cmd)}")
            result = subprocess.run(
                cmd,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                check=True
            )
            release_url = result.stdout.strip()
            print(f"Success! Created release: {release_url}")
            return {
                "success": True,
                "dry_run": False,
                "tag": tag_name,
                "title": release_name,
                "branch": target_branch,
                "url": release_url,
                "notes": notes
            }
    except subprocess.CalledProcessError as e:
        print(f"Error executing gh release: {e.stderr.strip()}", file=sys.stderr)
        sys.exit(1)
    finally:
        if os.path.exists(temp_notes_path):
            os.remove(temp_notes_path)

def main():
    parser = argparse.ArgumentParser(description="Ship a release branch by publishing it on GitHub.")
    parser.add_argument("--branch", help="Target branch/ref to point the release to (defaults to current branch)")
    parser.add_argument("--dry-run", action="store_true", help="Print details of the release without publishing")
    parser.add_argument("--output", help="Optional path to write a JSON report of the release results")
    
    args = parser.parse_args()
    
    # Require --output argument to comply with Rule 3 (CLI Script Pattern)
    if not args.output:
        print("Error: --output <file_path> is required to capture the execution results.", file=sys.stderr)
        sys.exit(1)
        
    branch = args.branch if args.branch else get_current_branch()
    version = get_version()
    notes = get_changelog_notes(version)
    
    result = run_gh_release(version, notes, branch, dry_run=args.dry_run)
    
    # Write JSON output report
    with open(args.output, "w") as f:
        json.dump(result, f, indent=2)
        
    print(f"Execution results written to: {args.output}")

if __name__ == "__main__":
    main()
