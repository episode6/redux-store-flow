#!/usr/bin/env python3
import json
import os
import shutil
import argparse

def merge_module_files(base_file, new_file):
    print(f"DEBUG: Merging module files: {base_file} and {new_file}")
    try:
        if not os.path.exists(base_file):
            print(f"ERROR: base_file does not exist: {base_file}")
            return
        if not os.path.exists(new_file):
            print(f"ERROR: new_file does not exist: {new_file}")
            return

        with open(base_file, 'r') as f:
            base_data = json.load(f)
        with open(new_file, 'r') as f:
            new_data = json.load(f)

        # Use a dict for variants keyed by name to merge them
        base_variants = {v['name']: v for v in base_data.get('variants', [])}
        new_variants = {v['name']: v for v in new_data.get('variants', [])}

        print(f"DEBUG: base_variants count: {len(base_variants)}")
        print(f"DEBUG: new_variants names: {list(new_variants.keys())}")

        base_variants.update(new_variants)
        base_data['variants'] = list(base_variants.values())

        print(f"DEBUG: resulting variants count: {len(base_data['variants'])}")

        with open(base_file, 'w') as f:
            json.dump(base_data, f, indent=2)
    except Exception as e:
        print(f"ERROR: Exception merging {base_file} and {new_file}: {e}")
        import traceback
        traceback.print_exc()
        raise

def main():
    parser = argparse.ArgumentParser(description="Merge multiple Maven Local shards into one bundle.")
    parser.add_argument("--input", required=True, help="Directory containing shard subdirectories")
    parser.add_argument("--output", required=True, help="Output directory for the merged bundle")
    args = parser.parse_args()

    input_dir = args.input
    output_dir = args.output

    if os.path.exists(output_dir):
        shutil.rmtree(output_dir)
    os.makedirs(output_dir)

    shards = sorted([d for d in os.listdir(input_dir) if os.path.isdir(os.path.join(input_dir, d))])
    print(f"Found shards: {shards}")

    # We want to skip checksums because we'll regenerate them
    excluded_extensions = {".md5", ".sha1", ".sha256", ".sha512", ".lastUpdated"}
    excluded_files = {
        "maven-metadata.xml",
        "resolver-status.properties",
        "_remote.repositories",
    }

    for shard in shards:
        print(f"DEBUG: Processing shard: {shard}")
        shard_path = os.path.join(input_dir, shard)
        for root, dirs, files in os.walk(shard_path):
            for file in files:
                ext = os.path.splitext(file)[1]
                if file in excluded_files or ext in excluded_extensions or "maven-metadata.xml" in file:
                    # print(f"DEBUG: Skipping excluded file: {file}")
                    continue

                rel_path = os.path.relpath(os.path.join(root, file), shard_path)
                dest_path = os.path.join(output_dir, rel_path)

                if file.endswith(".module"):
                    if os.path.exists(dest_path):
                        merge_module_files(dest_path, os.path.join(root, file))
                    else:
                        print(f"DEBUG: First .module for {rel_path}, copying from {shard}")
                        os.makedirs(os.path.dirname(dest_path), exist_ok=True)
                        shutil.copy2(os.path.join(root, file), dest_path)
                elif not os.path.exists(dest_path):
                    # print(f"DEBUG: Copying new file {rel_path} from {shard}")
                    os.makedirs(os.path.dirname(dest_path), exist_ok=True)
                    shutil.copy2(os.path.join(root, file), dest_path)
                else:
                    # File already exists, assume identical
                    # print(f"DEBUG: File already exists in bundle: {rel_path}")
                    pass

if __name__ == "__main__":
    main()
