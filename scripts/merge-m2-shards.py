#!/usr/bin/env python3
import json
import os
import shutil
import argparse
import traceback
import sys
import zipfile
import tempfile
import filecmp

def merge_module_files(base_file, new_file):
    print(f"DEBUG: Merging module files: {base_file} and {new_file}")
    try:
        with open(base_file, 'r') as f:
            base_content = f.read()
            if not base_content.strip():
                print(f"WARNING: {base_file} is empty, skipping merge and using {new_file}")
                shutil.copy2(new_file, base_file)
                return
            base_data = json.loads(base_content)

        with open(new_file, 'r') as f:
            new_content = f.read()
            if not new_content.strip():
                print(f"WARNING: {new_file} is empty, skipping merge")
                return
            new_data = json.loads(new_content)

        if not isinstance(base_data, dict) or not isinstance(new_data, dict):
            print(f"WARNING: .module files are not dictionaries, skipping merge for {base_file}")
            return

        # Use a dict for variants keyed by name to merge them
        base_variants_list = base_data.get('variants', [])
        new_variants_list = new_data.get('variants', [])

        if not isinstance(base_variants_list, list) or not isinstance(new_variants_list, list):
            print(f"WARNING: 'variants' is not a list in {base_file} or {new_file}, skipping variants merge")
        else:
            base_variants = {v['name']: v for v in base_variants_list if isinstance(v, dict) and 'name' in v}
            new_variants = {v['name']: v for v in new_variants_list if isinstance(v, dict) and 'name' in v}

            print(f"DEBUG: base_variants count: {len(base_variants)}")
            print(f"DEBUG: new_variants names: {list(new_variants.keys())}")

            base_variants.update(new_variants)
            base_data['variants'] = list(base_variants.values())
            print(f"DEBUG: resulting variants count: {len(base_data['variants'])}")

        with open(base_file, 'w') as f:
            json.dump(base_data, f, indent=2)

    except Exception as e:
        print(f"ERROR: Exception merging {base_file} and {new_file}: {e}", file=sys.stderr)
        traceback.print_exc()
        raise

def process_shard_dir(shard_path, output_dir):
    excluded_extensions = {".md5", ".sha1", ".sha256", ".sha512", ".lastUpdated", ".asc", ".effective"}
    excluded_files = {
        "maven-metadata.xml",
        "maven-metadata-local.xml",
        "resolver-status.properties",
        "_remote.repositories",
    }

    for root, dirs, files in os.walk(shard_path):
        if "__MACOSX" in root:
            continue

        for file in files:
            # Normalize path separators in case we're on a system that extracted backslashes as part of the filename
            # This can happen if a ZIP entry name literally contained backslashes.
            normalized_file_path = file.replace('\\', '/')
            if normalized_file_path != file:
                 print(f"DEBUG: Normalizing backslashes in filename: {file} -> {normalized_file_path}")

            ext = os.path.splitext(normalized_file_path)[1]
            if normalized_file_path in excluded_files or ext in excluded_extensions or "maven-metadata.xml" in normalized_file_path or normalized_file_path.startswith("._"):
                continue

            rel_root = os.path.relpath(root, shard_path).replace('\\', '/')
            rel_path = os.path.normpath(os.path.join(rel_root, normalized_file_path)).replace('\\', '/')

            dest_path = os.path.join(output_dir, rel_path)

            if normalized_file_path.endswith(".module"):
                if os.path.exists(dest_path):
                    merge_module_files(dest_path, os.path.join(root, file))
                else:
                    print(f"DEBUG: First .module for {rel_path}, copying from {shard_path}")
                    os.makedirs(os.path.dirname(dest_path), exist_ok=True)
                    shutil.copy2(os.path.join(root, file), dest_path)
            elif not os.path.exists(dest_path):
                os.makedirs(os.path.dirname(dest_path), exist_ok=True)
                shutil.copy2(os.path.join(root, file), dest_path)
            else:
                # File already exists.
                # If it's not a checksum/metadata, check if it's identical.
                if not filecmp.cmp(os.path.join(root, file), dest_path, shallow=False):
                    if normalized_file_path.endswith(".pom"):
                        print(f"WARNING: POM collision for {rel_path}. Files differ! Keeping the existing one.")
                    else:
                        print(f"DEBUG: File collision for {rel_path}. Files differ! Keeping the existing one.")
                pass

def main():
    try:
        parser = argparse.ArgumentParser(description="Merge multiple Maven Local shards into one bundle.")
        parser.add_argument("--input", required=True, help="Directory containing shard zip files or subdirectories")
        parser.add_argument("--output", required=True, help="Output directory for the merged bundle")
        args = parser.parse_args()

        input_dir = args.input
        output_dir = args.output

        if os.path.exists(output_dir):
            shutil.rmtree(output_dir)
        os.makedirs(output_dir)

        if not os.path.exists(input_dir):
            print(f"ERROR: input_dir {input_dir} does not exist", file=sys.stderr)
            sys.exit(1)

        # Walk through the input directory and find all zip files recursively
        all_zips = []
        for root, dirs, files in os.walk(input_dir):
            for file in files:
                if file.endswith(".zip") and "__MACOSX" not in root:
                    all_zips.append(os.path.join(root, file))

        all_zips.sort()
        print(f"Found zip shards: {all_zips}")

        if not all_zips:
            print(f"WARNING: No zip shards found in {input_dir}, checking for direct subdirs")
            subdirs = sorted([d for d in os.listdir(input_dir) if os.path.isdir(os.path.join(input_dir, d))])
            for d in subdirs:
                print(f"DEBUG: Processing subdir shard: {d}")
                process_shard_dir(os.path.join(input_dir, d), output_dir)
        else:
            for zip_path in all_zips:
                print(f"DEBUG: Processing zip shard: {zip_path}")
                with tempfile.TemporaryDirectory() as tmpdir:
                    try:
                        with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                            # We manually extract to handle backslashes in ZIP entry names
                            for member in zip_ref.infolist():
                                # ZIP standard uses forward slashes, but Windows sometimes uses backslashes
                                normalized_name = member.filename.replace('\\', '/')
                                # Skip directories or entries that would escape tmpdir (security)
                                if normalized_name.startswith('/') or '..' in normalized_name:
                                    continue

                                target_path = os.path.normpath(os.path.join(tmpdir, normalized_name))
                                if normalized_name.endswith('/') or member.is_dir():
                                    os.makedirs(target_path, exist_ok=True)
                                else:
                                    os.makedirs(os.path.dirname(target_path), exist_ok=True)
                                    with zip_ref.open(member) as source, open(target_path, "wb") as target:
                                        shutil.copyfileobj(source, target)

                        process_shard_dir(tmpdir, output_dir)
                    except zipfile.BadZipFile:
                        print(f"ERROR: {zip_path} is not a valid zip file", file=sys.stderr)
                        sys.exit(1)

        print("DEBUG: Merge completed successfully")
    except Exception as e:
        print(f"FATAL ERROR in merge script: {e}", file=sys.stderr)
        traceback.print_exc()
        sys.exit(1)

if __name__ == "__main__":
    main()
