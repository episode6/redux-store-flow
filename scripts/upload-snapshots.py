#!/usr/bin/env python3
"""Uploads a merged mavenLocal-layout bundle of -SNAPSHOT artifacts to a Maven
snapshot repository using the timestamped unique-snapshot protocol.

Plain PUTs of non-unique snapshot filenames (foo-1.0-SNAPSHOT.jar) only register on
the FIRST publish of a version: the repo's maven-metadata.xml keeps pointing at that
first build, so later re-publishes are accepted but never served (observed on
sonatype central's maven-snapshots repo, July 2026). Resolvers follow the metadata,
so each publish must upload NEW timestamped filenames (foo-1.0-20260719.123456-2.jar)
and re-PUT a maven-metadata.xml with an incremented buildNumber — which is exactly
what mvn/gradle do when deploying a snapshot to a remote repo, and what this script
recreates for a bundle assembled from per-OS mavenLocal shards.

Auth comes from the NEXUS_USERNAME / NEXUS_PASSWORD env vars. Signature and checksum
files are not uploaded (matching the repo's snapshot behavior); mavenLocal's
maven-metadata-local.xml files are skipped in favor of the generated metadata.
"""
import argparse
import base64
import os
import re
import sys
import urllib.error
import urllib.request
from datetime import datetime, timezone

SKIP_SUFFIXES = (".asc", ".md5", ".sha1", ".sha256", ".sha512")


def auth_header():
    username = os.environ.get("NEXUS_USERNAME")
    password = os.environ.get("NEXUS_PASSWORD")
    if not username or not password:
        sys.exit("NEXUS_USERNAME and NEXUS_PASSWORD must be set")
    token = base64.b64encode(f"{username}:{password}".encode()).decode()
    return f"Basic {token}"


def http(method, url, auth, data=None):
    request = urllib.request.Request(url, data=data, method=method)
    request.add_header("Authorization", auth)
    request.add_header("User-Agent", "tacita-snapshot-upload")
    if data is not None:
        request.add_header("Content-Type", "application/octet-stream")
    return urllib.request.urlopen(request)


def next_build_number(repo_url, module_rel, auth):
    """Reads the module's existing remote metadata so the new build supersedes it."""
    try:
        with http("GET", f"{repo_url}/{module_rel}/maven-metadata.xml", auth) as response:
            existing = response.read().decode()
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return 1
        raise
    builds = [int(m) for m in re.findall(r"<buildNumber>(\d+)</buildNumber>", existing)]
    return max(builds, default=0) + 1


def classifier_and_extension(filename, artifact_id, version):
    """Splits foo-1.0-SNAPSHOT[-classifier].ext into (classifier, ext)."""
    prefix = f"{artifact_id}-{version}"
    if not filename.startswith(prefix):
        return None
    rest = filename[len(prefix):]
    left, dot, ext = rest.partition(".")
    if not dot or (left and not left.startswith("-")):
        return None
    return (left[1:] if left else None), ext


def metadata_xml(group_id, artifact_id, version, timestamp, build, entries, updated):
    lines = [
        '<?xml version="1.0" encoding="UTF-8"?>',
        '<metadata modelVersion="1.1.0">',
        f"  <groupId>{group_id}</groupId>",
        f"  <artifactId>{artifact_id}</artifactId>",
        f"  <version>{version}</version>",
        "  <versioning>",
        "    <snapshot>",
        f"      <timestamp>{timestamp}</timestamp>",
        f"      <buildNumber>{build}</buildNumber>",
        "    </snapshot>",
        f"    <lastUpdated>{updated}</lastUpdated>",
        "    <snapshotVersions>",
    ]
    for classifier, ext, value in entries:
        lines.append("      <snapshotVersion>")
        if classifier:
            lines.append(f"        <classifier>{classifier}</classifier>")
        lines.extend([
            f"        <extension>{ext}</extension>",
            f"        <value>{value}</value>",
            f"        <updated>{updated}</updated>",
            "      </snapshotVersion>",
        ])
    lines.extend(["    </snapshotVersions>", "  </versioning>", "</metadata>", ""])
    return "\n".join(lines)


def upload_module(bundle, module_rel, repo_url, auth, now):
    artifact_id = os.path.basename(os.path.dirname(module_rel))
    version = os.path.basename(module_rel)
    group_id = os.path.dirname(os.path.dirname(module_rel)).replace("/", ".")
    base_version = version[: -len("-SNAPSHOT")]
    timestamp = now.strftime("%Y%m%d.%H%M%S")
    updated = now.strftime("%Y%m%d%H%M%S")
    build = next_build_number(repo_url, module_rel, auth)
    value = f"{base_version}-{timestamp}-{build}"

    entries = []
    for filename in sorted(os.listdir(os.path.join(bundle, module_rel))):
        if filename.endswith(SKIP_SUFFIXES) or filename.startswith("maven-metadata"):
            continue
        parsed = classifier_and_extension(filename, artifact_id, version)
        if parsed is None:
            sys.exit(f"Unexpected file in {module_rel}: {filename}")
        classifier, ext = parsed
        remote_name = f"{artifact_id}-{value}" + (f"-{classifier}" if classifier else "") + f".{ext}"
        print(f"Uploading {module_rel}/{remote_name}")
        with open(os.path.join(bundle, module_rel, filename), "rb") as f:
            http("PUT", f"{repo_url}/{module_rel}/{remote_name}", auth, data=f.read())
        entries.append((classifier, ext, value))

    if not entries:
        return
    print(f"Uploading {module_rel}/maven-metadata.xml (build {build})")
    xml = metadata_xml(group_id, artifact_id, version, timestamp, build, entries, updated)
    http("PUT", f"{repo_url}/{module_rel}/maven-metadata.xml", auth, data=xml.encode())


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bundle", required=True, help="root of the mavenLocal-layout bundle")
    parser.add_argument("--repo-url", required=True, help="snapshot repository base url")
    args = parser.parse_args()

    repo_url = args.repo_url.rstrip("/")
    auth = auth_header()
    now = datetime.now(timezone.utc)

    modules = sorted(
        os.path.relpath(dirpath, args.bundle).replace(os.sep, "/")
        for dirpath, _, filenames in os.walk(args.bundle)
        if os.path.basename(dirpath).endswith("-SNAPSHOT") and filenames
    )
    if not modules:
        sys.exit(f"No -SNAPSHOT module directories found under {args.bundle}")
    for module_rel in modules:
        upload_module(args.bundle, module_rel, repo_url, auth, now)
    print(f"Uploaded {len(modules)} snapshot modules")


if __name__ == "__main__":
    main()
