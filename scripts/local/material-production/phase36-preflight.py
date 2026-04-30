#!/usr/bin/env python3
"""Phase 36 material package production preflight.

All project copy is read from UTF-8 manifest/prompt/script files. This script
does not embed story copy or secrets.
"""

from __future__ import annotations

import argparse
import json
from collections import Counter, defaultdict
from decimal import Decimal
from pathlib import Path
from typing import Any


def read_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def read_text_if_exists(path: Path) -> str:
    if not path.exists():
        return ""
    return path.read_text(encoding="utf-8")


def money(value: Any) -> Decimal:
    if value is None or value == "":
        return Decimal("0")
    return Decimal(str(value))


def material_index(manifest: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {item["itemKey"]: item for item in manifest.get("materials", [])}


def selected_work_items(manifest: dict[str, Any], batch: dict[str, Any]) -> list[dict[str, Any]]:
    by_key = material_index(manifest)
    items: list[dict[str, Any]] = []
    for entry in batch.get("workItems", []):
        item_key = entry.get("itemKey")
        manifest_item = by_key.get(item_key, {})
        merged = {**manifest_item, **entry}
        merged["itemKey"] = item_key
        items.append(merged)
    return items


def build_report(manifest_path: Path, batch_path: Path, args: argparse.Namespace) -> dict[str, Any]:
    manifest = read_json(manifest_path)
    batch = read_json(batch_path)
    docs_dir = manifest_path.parent
    # Explicit UTF-8 reads catch encoding regressions before live production.
    image_prompt_chars = len(read_text_if_exists(docs_dir / "image-prompts.md"))
    audio_script_chars = len(read_text_if_exists(docs_dir / "audio-scripts.md"))
    items = selected_work_items(manifest, batch)
    asset_counter = Counter((item.get("assetKind") or "other") for item in items)
    chapter_counter = Counter((item.get("chapterCode") or "global") for item in items)
    usage_counter = Counter((item.get("usageTarget") or "unknown") for item in items)
    grouped: dict[str, list[str]] = defaultdict(list)
    for item in items:
        grouped[f"{item.get('assetKind') or 'other'}::{item.get('chapterCode') or 'global'}::{item.get('usageTarget') or 'unknown'}"].append(item["itemKey"])

    estimated_total = sum((money(item.get("estimatedCost")) for item in items), Decimal("0"))
    batch_ceiling = money(args.batch_cost_ceiling if args.batch_cost_ceiling is not None else batch.get("batchCostCeiling"))
    daily_ceiling = money(args.daily_cost_ceiling if args.daily_cost_ceiling is not None else batch.get("dailyCostCeiling"))
    requires_super_admin = bool((batch_ceiling and estimated_total > batch_ceiling) or (daily_ceiling and estimated_total > daily_ceiling))
    risk_summary = []
    if requires_super_admin:
        risk_summary.append(
            {
                "riskCode": "cost_ceiling_exceeded",
                "message": "EstimatedTotalCost exceeds at least one configured ceiling.",
                "requiresSuperAdminConfirmation": True,
            }
        )
    missing_refs = [item["itemKey"] for item in items if item["itemKey"] not in material_index(manifest)]
    if missing_refs:
        risk_summary.append(
            {
                "riskCode": "manifest_item_missing",
                "message": "One or more batch item keys are not present in content-manifest.json.",
                "itemKeys": missing_refs,
                "requiresSuperAdminConfirmation": False,
            }
        )

    report = {
        "schemaVersion": 1,
        "packageCode": batch.get("packageCode") or manifest.get("packageCode"),
        "manifestPath": str(manifest_path),
        "batchPath": str(batch_path),
        "itemCount": len(items),
        "targetAssetKinds": sorted(asset_counter),
        "assetKindCounts": dict(asset_counter),
        "chapterCounts": dict(chapter_counter),
        "usageTargetCounts": dict(usage_counter),
        "groups": [{"groupKey": key, "itemKeys": value} for key, value in sorted(grouped.items())],
        "estimatedTotalCost": str(estimated_total),
        "currencyCode": batch.get("currencyCode", "CNY"),
        "dailyCostCeiling": str(daily_ceiling),
        "batchCostCeiling": str(batch_ceiling),
        "requiresSuperAdminConfirmation": requires_super_admin,
        "riskSummary": risk_summary,
        "utf8SourceStats": {
            "imagePromptsChars": image_prompt_chars,
            "audioScriptsChars": audio_script_chars,
        },
        "dryRun": bool(args.dry_run),
    }
    return report


def main() -> int:
    parser = argparse.ArgumentParser(description="Phase 36 production preflight")
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--batch", required=True, type=Path)
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--batch-cost-ceiling")
    parser.add_argument("--daily-cost-ceiling")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()

    report = build_report(args.manifest, args.batch, args)
    output = args.output or args.batch.parent / "phase36-preflight-report.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({
        "output": str(output),
        "itemCount": report["itemCount"],
        "estimatedTotalCost": report["estimatedTotalCost"],
        "requiresSuperAdminConfirmation": report["requiresSuperAdminConfirmation"],
    }, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
