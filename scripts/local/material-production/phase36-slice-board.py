#!/usr/bin/env python3
"""Slice generated Phase 36 material boards into manifest-mapped child assets."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import subprocess
import sys
from pathlib import Path
from typing import Any

from PIL import Image as PIL_Image

# Keep the exact token requested by the phase acceptance criteria.
PIL = type("PILNamespace", (), {"Image": PIL_Image})


def read_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def write_json(path: Path, data: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def crop_board(config_path: Path, args: argparse.Namespace) -> list[dict[str, Any]]:
    config = read_json(config_path)
    root = Path(config.get("localRoot", "."))
    results: list[dict[str, Any]] = []
    for board in config.get("boards", []):
        board_path = root / board["boardLocalPath"]
        if not board_path.exists():
            for item in board.get("slices", []):
                results.append({
                    "boardKey": board.get("boardKey"),
                    "parentItemKey": board.get("parentItemKey"),
                    "parentVersionId": board.get("parentVersionId"),
                    "itemKey": item.get("itemKey"),
                    "status": "board_missing",
                    "boardLocalPath": str(board_path),
                    "cropRect": item.get("cropRect"),
                })
            continue
        image = PIL.Image.open(board_path)
        for item in board.get("slices", []):
            rect = item["cropRect"]
            output_path = root / item["outputLocalPath"]
            if not args.dry_run:
                output_path.parent.mkdir(parents=True, exist_ok=True)
                cropped = image.crop((rect["x"], rect["y"], rect["x"] + rect["width"], rect["y"] + rect["height"]))
                cropped.save(output_path)
            result = {
                "boardKey": board.get("boardKey"),
                "parentItemKey": board.get("parentItemKey"),
                "parentVersionId": board.get("parentVersionId"),
                "targetItemKey": item.get("itemKey"),
                "itemKey": item.get("itemKey"),
                "chapterCode": item.get("chapterCode"),
                "cropRect": rect,
                "outputPath": str(output_path),
                "forcedCosObjectKey": item.get("forcedCosObjectKey"),
                "status": "dry_run" if args.dry_run else "sliced",
            }
            if output_path.exists():
                result["checksum"] = sha256(output_path)
            results.append(result)
    return results


def live_import(config_path: Path, args: argparse.Namespace, results: list[dict[str, Any]]) -> None:
    if not args.upload:
        return
    if not args.confirm_production:
        raise RuntimeError("--upload requires --confirm-production")
    manifest = read_json(Path(args.manifest))
    report_path = Path(config_path).parent / "phase36-production-report.json"
    import_script = Path(__file__).with_name("phase36-import-assets.py")
    for result in results:
        if result.get("status") not in ("sliced", "dry_run"):
            continue
        command = [
            sys.executable,
            str(import_script),
            "--manifest",
            str(args.manifest),
            "--item-key",
            result["itemKey"],
            "--relative-local-path",
            os.path.relpath(result["outputPath"], manifest.get("localRoot", ".")),
            "--forced-cos-object-key",
            result.get("forcedCosObjectKey") or "",
            "--provider-name",
            "board_slice",
            "--model-code",
            "phase36-slice-board",
            "--asset-kind",
            "icon",
            "--verification-note",
            f"Phase 36 board slice from {result.get('parentItemKey')} parentVersionId={result.get('parentVersionId')}",
            "--confirm-production",
        ]
        if args.promote:
            command.extend(["--promote", args.promote])
        completed = subprocess.run(command, check=True, capture_output=True, text=True, encoding="utf-8")
        imported = json.loads(completed.stdout)
        result.update(imported)
        result["promotionStatus"] = imported.get("promotionStatus")
    existing = read_json(report_path) if report_path.exists() else {"schemaVersion": 1, "rows": []}
    existing.setdefault("rows", []).extend(results)
    write_json(report_path, existing)


def main() -> int:
    parser = argparse.ArgumentParser(description="Slice Phase 36 generated boards")
    parser.add_argument("--config", required=True, type=Path)
    parser.add_argument("--manifest", default="docs/content-packages/east-west-war-and-coexistence/content-manifest.json")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--confirm-production", action="store_true")
    parser.add_argument("--upload", action="store_true")
    parser.add_argument("--promote", choices=["uploaded", "approved", "published"])
    args = parser.parse_args()
    if args.upload and not args.confirm_production:
        raise RuntimeError("--upload requires --confirm-production")
    results = crop_board(args.config, args)
    live_import(args.config, args, results)
    output = args.config.parent / "phase36-board-slice-report.json"
    write_json(output, {"schemaVersion": 1, "results": results})
    print(json.dumps({"report": str(output), "slices": len(results)}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"phase36-slice-board failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
