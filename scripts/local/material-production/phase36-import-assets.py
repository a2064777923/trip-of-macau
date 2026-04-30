#!/usr/bin/env python3
"""Import an already-produced Phase 36 material file through admin backend APIs."""

from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any


DEFAULT_BACKEND = "http://localhost:8081"


def read_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def api_request(method: str, backend: str, path: str, token: str, payload: dict[str, Any] | None = None) -> Any:
    body = None if payload is None else json.dumps(payload, ensure_ascii=False).encode("utf-8")
    request = urllib.request.Request(
        backend.rstrip("/") + path,
        data=body,
        method=method,
        headers={
            "Accept": "application/json",
            "Content-Type": "application/json; charset=utf-8",
            "Authorization": f"Bearer {token}",
        },
    )
    try:
        with urllib.request.urlopen(request, timeout=60) as response:
            envelope = json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {exc.code} {method} {path}: {detail}") from exc
    if envelope.get("code") not in (0, 200, None) and envelope.get("success") is not True:
        raise RuntimeError(f"API failed for {method} {path}: {envelope}")
    return envelope.get("data", envelope)


def manifest_index(manifest: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {item["itemKey"]: item for item in manifest.get("materials", [])}


def resolve_package(backend: str, token: str, package_code: str) -> dict[str, Any]:
    query = urllib.parse.urlencode({"pageNum": 1, "pageSize": 50, "keyword": package_code})
    page = api_request("GET", backend, f"/api/admin/v1/content/material-packages?{query}", token)
    for item in page.get("list", []):
        if item.get("code") == package_code:
            return item
    raise RuntimeError(f"Package code not found: {package_code}")


def resolve_item(backend: str, token: str, package_id: int, item_key: str) -> dict[str, Any]:
    detail = api_request("GET", backend, f"/api/admin/v1/content/material-packages/{package_id}", token)
    for item in detail.get("items", []):
        if item.get("itemKey") == item_key:
            return item
    raise RuntimeError(f"Package item not found: {item_key}")


def import_item(args: argparse.Namespace) -> dict[str, Any]:
    manifest = read_json(args.manifest)
    by_key = manifest_index(manifest)
    manifest_item = by_key.get(args.item_key)
    if not manifest_item:
        raise RuntimeError(f"itemKey is not in manifest: {args.item_key}")
    package_code = args.package_code or manifest.get("packageCode")
    package_id = args.package_id
    token = args.token or os.environ.get("PHASE36_ADMIN_BEARER_TOKEN")
    backend = args.backend.rstrip("/")
    if not args.confirm_production:
        return {
            "dryRun": True,
            "packageCode": package_code,
            "itemKey": args.item_key,
            "relativeLocalPath": args.relative_local_path or manifest_item.get("localPath"),
            "forcedCosObjectKey": args.forced_cos_object_key or manifest_item.get("cosObjectKey"),
            "assetKind": args.asset_kind or manifest_item.get("assetKind"),
            "subtitleMetadataJson": args.subtitle_metadata_json,
            "posterFallbackItemKey": args.poster_fallback_item_key or manifest_item.get("fallbackItemKey"),
            "targetStatus": args.promote,
        }
    if not token:
        raise RuntimeError("PHASE36_ADMIN_BEARER_TOKEN is required for live import")
    if package_id is None:
        package_id = int(resolve_package(backend, token, package_code)["id"])
    item = resolve_item(backend, token, package_id, args.item_key)
    payload = {
        "relativeLocalPath": args.relative_local_path or manifest_item.get("localPath"),
        "forcedCosObjectKey": args.forced_cos_object_key or manifest_item.get("cosObjectKey"),
        "promptText": args.prompt_text,
        "scriptText": args.script_text,
        "providerName": args.provider_name,
        "modelCode": args.model_code,
        "estimatedCost": args.estimated_cost,
        "assetKind": args.asset_kind or manifest_item.get("assetKind"),
        "subtitleMetadataJson": args.subtitle_metadata_json,
        "posterFallbackItemKey": args.poster_fallback_item_key or manifest_item.get("fallbackItemKey"),
        "verificationNote": args.verification_note,
        "localeCode": args.locale_code,
    }
    version = api_request(
        "POST",
        backend,
        f"/api/admin/v1/content/material-packages/{package_id}/items/{item['id']}/production/import",
        token,
        payload,
    )
    if args.promote:
        version = api_request(
            "POST",
            backend,
            f"/api/admin/v1/content/material-packages/{package_id}/items/{item['id']}/production/promote",
            token,
            {
                "versionId": version.get("id"),
                "targetStatus": args.promote,
                "verificationNote": args.verification_note,
                "superAdminConfirmation": True,
            },
        ).get("version", version)
    return {
        "dryRun": False,
        "packageId": package_id,
        "itemKey": args.item_key,
        "versionId": version.get("id"),
        "assetId": version.get("contentAssetId"),
        "promotionStatus": version.get("promotionStatus"),
        "localPath": version.get("localPath"),
        "cosObjectKey": version.get("cosObjectKey"),
        "canonicalUrl": version.get("canonicalUrl"),
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Import Phase 36 produced material through backend production/import")
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--item-key", required=True)
    parser.add_argument("--package-code")
    parser.add_argument("--package-id", type=int)
    parser.add_argument("--backend", default=os.environ.get("PHASE36_ADMIN_BASE_URL", DEFAULT_BACKEND))
    parser.add_argument("--token", default=os.environ.get("PHASE36_ADMIN_BEARER_TOKEN"))
    parser.add_argument("--relative-local-path")
    parser.add_argument("--forced-cos-object-key")
    parser.add_argument("--prompt-text")
    parser.add_argument("--script-text")
    parser.add_argument("--provider-name", default="local")
    parser.add_argument("--model-code", default="manual-import")
    parser.add_argument("--estimated-cost", default="0")
    parser.add_argument("--asset-kind")
    parser.add_argument("--subtitle-metadata-json")
    parser.add_argument("--poster-fallback-item-key")
    parser.add_argument("--verification-note", default="Phase 36 local import")
    parser.add_argument("--locale-code", default="zh-Hant")
    parser.add_argument("--confirm-production", action="store_true")
    parser.add_argument("--promote", choices=["uploaded", "approved", "published"])
    args = parser.parse_args()

    result = import_item(args)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"phase36-import-assets failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
