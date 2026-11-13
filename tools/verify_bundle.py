#!/usr/bin/env python3
"""Strict, offline patch-application check; optionally combine with an official GmsCore bundle."""
import argparse
import json
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("bundle", type=Path)
    parser.add_argument("apk", type=Path)
    parser.add_argument("--desktop-jar", type=Path, required=True)
    parser.add_argument("--java", default="/usr/lib/jvm/java-21-openjdk-amd64/bin/java")
    parser.add_argument("--gms-bundle", type=Path)
    parser.add_argument("--out", type=Path, required=True, help="New output directory; existing evidence is never overwritten")
    args = parser.parse_args()
    for path in (args.bundle, args.apk, args.desktop_jar, args.gms_bundle):
        if path is not None and not path.is_file():
            parser.error(f"Missing input: {path}")
    names = []
    for source in sorted((ROOT / "patches/src/main/kotlin").rglob("*.kt")):
        names += re.findall(r'\b(?:bytecodePatch|resourcePatch|rawResourcePatch)\(\s*name\s*=\s*"([^"\n]+)"', source.read_text())
    if not names or len(names) != len(set(names)):
        parser.error("Expected a nonempty, unique set of named patches")
    if args.out.exists():
        parser.error(f"Output directory already exists: {args.out}; choose a new run directory")
    args.out.mkdir(parents=True)
    command = [args.java, "-Xmx6g", "-jar", str(args.desktop_jar.resolve()), "patch",
               "--exclusive", "--validate", "--unsigned"]
    expected = set(names)
    if args.gms_bundle:
        command += ["-p", str(args.gms_bundle.resolve()), "-e", "GmsCore support"]
        expected.add("GmsCore support")
    command += ["-p", str(args.bundle.resolve())]
    for name in names:
        command += ["-e", name]
    result_file = args.out.resolve() / "result.json"
    command += ["-o", str(args.out.resolve() / "patched-unsigned.apk"),
                "-t", str(args.out.resolve() / "scratch"), "-r", str(result_file), str(args.apk.resolve())]
    (args.out / "command.json").write_text(json.dumps(command, indent=2) + "\n")
    log_path = args.out / "patch.log"
    print(f"Validating {len(expected)} selected patches; log: {log_path}", flush=True)
    with log_path.open("w") as log:
        patched = subprocess.run(command, stdout=log, stderr=subprocess.STDOUT)
    if patched.returncode != 0 or not result_file.exists():
        print(log_path.read_text()[-6000:])
        raise SystemExit(patched.returncode or 1)
    result = json.loads(result_file.read_text())
    missing = expected - set(result.get("appliedPatches", []))
    if not result.get("success") or result.get("failedPatches") or missing:
        raise SystemExit(f"Patch validation failed: missing={sorted(missing)}; see {result_file}")
    print(f"PASS {len(expected)} selected patches applied to YouTube {result.get('packageVersion')}")
    print(args.out / "patched-unsigned.apk")


if __name__ == "__main__":
    main()
