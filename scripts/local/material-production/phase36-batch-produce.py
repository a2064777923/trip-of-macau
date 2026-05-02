#!/usr/bin/env python3
"""One-off Phase 36 batch production runner.

Live mode is intentionally explicit:
python scripts/local/material-production/phase36-batch-produce.py --manifest docs/content-packages/east-west-war-and-coexistence/content-manifest.json --batch docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-batch.json --confirm-production --upload --promote published
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import sys
import time
import urllib.error
import urllib.request
from urllib.parse import urlparse
from pathlib import Path
from typing import Any


AI_JOB_ROUTE = "/api/admin/v1/ai/generation-jobs"
REWARD_CUE_ITEM_KEY = "sfx_reward_unlock"
DEFAULT_BACKEND = "http://localhost:8081"


def image_generation_endpoint(item: dict[str, Any]) -> str:
    endpoint = (
        os.environ.get("PHASE36_IMAGE_GENERATIONS_URL")
        or os.environ.get("PHASE36_IMAGE_BASE_URL")
        or item.get("openaiImagesEndpoint")
        or "https://api.openai.com/v1/images/generations"
    ).strip()
    if endpoint.endswith("/"):
        endpoint = endpoint[:-1]
    if endpoint.endswith("/images"):
        return endpoint + "/generations"
    if endpoint.endswith("/images/generations"):
        return endpoint
    parsed = urlparse(endpoint)
    if parsed.path in ("", "/"):
        return endpoint + "/v1/images/generations"
    return endpoint


def image_api_key() -> str | None:
    return os.environ.get("PHASE36_IMAGE_API_KEY") or os.environ.get("OPENAI_API_KEY")


def image_model(item: dict[str, Any]) -> str:
    return os.environ.get("PHASE36_IMAGE_MODEL") or item.get("modelCode") or "gpt-image-1"


def extract_image_payload(data: dict[str, Any]) -> tuple[str | None, str | None]:
    candidates = data.get("data")
    if candidates is None:
        candidates = data.get("images") or data.get("result") or data.get("output")
    if isinstance(candidates, dict):
        candidates = [candidates]
    if not isinstance(candidates, list):
        candidates = [data]
    for candidate in candidates:
        if not isinstance(candidate, dict):
            continue
        b64 = (
            candidate.get("b64_json")
            or candidate.get("base64")
            or candidate.get("image_base64")
            or candidate.get("image")
        )
        url = candidate.get("url") or candidate.get("image_url")
        if b64 or url:
            return b64, url
    return None, None


def download_binary(url: str) -> bytes:
    request = urllib.request.Request(url, method="GET", headers={"Accept": "image/*,*/*"})
    with urllib.request.urlopen(request, timeout=180) as response:
        return response.read()


def read_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8") if path.exists() else ""


def write_json(path: Path, data: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def section_text(markdown: str, ref: str | None) -> str:
    if not ref or "#" not in ref:
        return ""
    anchor = ref.split("#", 1)[1].strip().lower()
    lines = markdown.splitlines()
    capturing = False
    captured: list[str] = []
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("## "):
            heading = stripped[3:].strip().lower().replace(" ", "-")
            if capturing:
                break
            capturing = heading == anchor
        elif capturing:
            captured.append(line)
    return "\n".join(captured).strip()


def manifest_index(manifest: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {item["itemKey"]: item for item in manifest.get("materials", [])}


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
        with urllib.request.urlopen(request, timeout=120) as response:
            envelope = json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"HTTP {exc.code} {method} {path}: {detail}") from exc
    if envelope.get("code") not in (0, 200, None) and envelope.get("success") is not True:
        raise RuntimeError(f"API failed for {method} {path}: {envelope}")
    return envelope.get("data", envelope)


def maybe_generate_openai_image(item: dict[str, Any], prompt_text: str, output_path: Path, args: argparse.Namespace) -> str:
    api_key = image_api_key()
    if not api_key:
        return "blocked_missing_image_api_key"
    if output_path.exists() and not args.overwrite:
        return "ready_existing"
    payload = {
        "model": image_model(item),
        "prompt": prompt_text,
        "size": item.get("size", "1024x1024"),
        "quality": item.get("quality", "auto"),
        "n": 1,
    }
    request = urllib.request.Request(
        image_generation_endpoint(item),
        data=json.dumps(payload, ensure_ascii=False).encode("utf-8"),
        method="POST",
        headers={
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json; charset=utf-8",
        },
    )
    with urllib.request.urlopen(request, timeout=180) as response:
        data = json.loads(response.read().decode("utf-8"))
    b64, url = extract_image_payload(data)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    if b64:
        output_path.write_bytes(base64.b64decode(b64))
        return "generated"
    if url:
        output_path.write_bytes(download_binary(url))
        return "generated"
    if not b64:
        return "retry_required"


def import_and_promote(backend: str, token: str, package_id: int, item_id: int, payload: dict[str, Any], promote: str | None) -> dict[str, Any]:
    version = api_request(
        "POST",
        backend,
        f"/api/admin/v1/content/material-packages/{package_id}/items/{item_id}/production/import",
        token,
        payload,
    )
    if promote:
        promoted = api_request(
            "POST",
            backend,
            f"/api/admin/v1/content/material-packages/{package_id}/items/{item_id}/production/promote",
            token,
            {
                "versionId": version.get("id"),
                "targetStatus": promote,
                "verificationNote": "Phase 36 live production verification",
                "superAdminConfirmation": True,
            },
        )
        version = promoted.get("version", version)
    return version


def package_detail(backend: str, token: str, package_code: str) -> dict[str, Any]:
    page = api_request("GET", backend, f"/api/admin/v1/content/material-packages?pageNum=1&pageSize=50&keyword={package_code}", token)
    package_id = None
    for row in page.get("list", []):
        if row.get("code") == package_code:
            package_id = row.get("id")
            break
    if not package_id:
        raise RuntimeError(f"Package not found: {package_code}")
    return api_request("GET", backend, f"/api/admin/v1/content/material-packages/{package_id}", token)


def run_audio_job(backend: str, token: str, item: dict[str, Any], script_text: str) -> dict[str, Any]:
    # Uses existing admin AI endpoints; provider credentials stay backend-side.
    voices = api_request("GET", backend, "/api/admin/v1/ai/voices?languageCode=zh-Hans", token)
    voice = next((row for row in voices if row.get("availabilityStatus") in (None, "available")), None)
    if not voice:
        return {"audioGenerationStatus": "manual_import_required", "manualImportRequired": True, "errorMessage": "No available Mandarin voice"}
    job = api_request(
        "POST",
        backend,
        AI_JOB_ROUTE,
        token,
        {
            "capabilityCode": "admin_voice_synthesis",
            "providerId": voice.get("providerId"),
            "inventoryId": voice.get("inventoryId"),
            "generationType": "audio",
            "sourceScope": "story_material_package",
            "promptTitle": item.get("itemKey"),
            "promptText": script_text,
            "promptVariablesJson": json.dumps({"voiceCode": voice.get("voiceCode"), "languageCode": "zh-Hans"}, ensure_ascii=False),
        },
    )
    for _ in range(5):
        time.sleep(2)
        job = api_request("POST", backend, f"{AI_JOB_ROUTE}/{job['id']}/refresh", token)
        if job.get("jobStatus") in ("completed", "failed"):
            break
    candidates = job.get("candidates") or []
    candidate = next((row for row in candidates if row.get("isFinalized") or row.get("storageUrl")), None)
    if not candidate:
        status = "retry_required" if job.get("jobStatus") == "failed" else "manual_import_required"
        return {"audioGenerationStatus": status, "manualImportRequired": status == "manual_import_required", "errorMessage": job.get("errorMessage")}
    return {"audioGenerationStatus": "ready_to_bind", "candidateId": candidate.get("id"), "voice": voice, "job": job}


def main() -> int:
    parser = argparse.ArgumentParser(description="Phase 36 batch still-image/audio production")
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--batch", required=True, type=Path)
    parser.add_argument("--preflight-report", type=Path)
    parser.add_argument("--backend", default=os.environ.get("PHASE36_ADMIN_BASE_URL", DEFAULT_BACKEND))
    parser.add_argument("--token", default=os.environ.get("PHASE36_ADMIN_BEARER_TOKEN"))
    parser.add_argument("--confirm-production", action="store_true")
    parser.add_argument("--upload", action="store_true")
    parser.add_argument("--promote", choices=["uploaded", "approved", "published"])
    parser.add_argument("--overwrite", action="store_true")
    args = parser.parse_args()

    manifest = read_json(args.manifest)
    batch = read_json(args.batch)
    docs_dir = args.manifest.parent
    local_root = Path(manifest.get("localRoot", "local-content/phase33/east-west-war-and-coexistence"))
    image_prompts = read_text(docs_dir / "image-prompts.md")
    audio_scripts = read_text(docs_dir / "audio-scripts.md")
    by_key = manifest_index(manifest)
    report_path = args.batch.parent / "phase36-production-report.json"
    rows: list[dict[str, Any]] = []
    package = None
    if args.upload:
        if not args.confirm_production:
            raise RuntimeError("--upload requires --confirm-production")
        if not args.token:
            raise RuntimeError("PHASE36_ADMIN_BEARER_TOKEN is required for --upload")
        package = package_detail(args.backend, args.token, batch.get("packageCode", manifest.get("packageCode")))
    elif args.confirm_production:
        print("confirm-production supplied without --upload: local generation/import report only")

    for entry in batch.get("workItems", []):
        item_key = entry["itemKey"]
        manifest_item = {**by_key.get(item_key, {}), **entry}
        if not manifest_item:
            rows.append({"itemKey": item_key, "status": "manifest_missing"})
            continue
        output_path = local_root / manifest_item.get("localPath", f"generated/{item_key}")
        asset_kind = manifest_item.get("assetKind", "other")
        prompt_text = section_text(image_prompts, manifest_item.get("promptRef")) or manifest_item.get("promptText", "")
        script_text = section_text(audio_scripts, manifest_item.get("scriptRef")) or manifest_item.get("scriptText", "")
        row = {
            "itemKey": item_key,
            "localPath": manifest_item.get("localPath"),
            "providerName": entry.get("providerName"),
            "modelCode": entry.get("modelCode"),
            "estimatedCost": entry.get("estimatedCost", "0"),
            "actualCost": "0",
            "assetKind": asset_kind,
            "status": "planned",
            "audioGenerationStatus": None,
            "manualImportRequired": False,
            "errorMessage": None,
        }
        if asset_kind in ("image", "icon"):
            if args.confirm_production:
                try:
                    status = maybe_generate_openai_image(manifest_item, prompt_text, output_path, args)
                except Exception as exc:
                    status = "retry_required"
                    row["errorMessage"] = str(exc)
            else:
                status = "dry_run"
            row["status"] = status
        elif asset_kind == "audio":
            if args.confirm_production and args.upload:
                audio_result = run_audio_job(args.backend, args.token, manifest_item, script_text)
                row.update(audio_result)
                row["status"] = audio_result.get("audioGenerationStatus", "retry_required")
            else:
                row["audioGenerationStatus"] = "dry_run"
                row["status"] = "dry_run"
        else:
            row["status"] = "skipped_unsupported_kind"

        if args.upload and package and asset_kind in ("image", "icon") and output_path.exists():
            item_row = next((item for item in package.get("items", []) if item.get("itemKey") == item_key), None)
            if item_row:
                version = import_and_promote(
                    args.backend,
                    args.token,
                    package["id"],
                    item_row["id"],
                    {
                        "relativeLocalPath": manifest_item.get("localPath"),
                        "forcedCosObjectKey": manifest_item.get("cosObjectKey"),
                        "promptText": prompt_text,
                        "scriptText": script_text,
                        "providerName": entry.get("providerName"),
                        "modelCode": entry.get("modelCode"),
                        "estimatedCost": entry.get("estimatedCost", "0"),
                        "assetKind": asset_kind,
                        "subtitleMetadataJson": entry.get("subtitleMetadataJson"),
                        "posterFallbackItemKey": manifest_item.get("fallbackItemKey"),
                        "verificationNote": "Phase 36 generated still-image import",
                        "localeCode": "zh-Hant",
                    },
                    args.promote,
                )
                row.update({
                    "versionId": version.get("id"),
                    "assetId": version.get("contentAssetId"),
                    "promotionStatus": version.get("promotionStatus"),
                    "cosObjectKey": version.get("cosObjectKey"),
                    "canonicalUrl": version.get("canonicalUrl"),
                    "status": version.get("promotionStatus") or row["status"],
                })
        if item_key == REWARD_CUE_ITEM_KEY and row.get("audioGenerationStatus") not in ("ready_to_bind", "dry_run"):
            row["manualImportRequired"] = row.get("audioGenerationStatus") == "manual_import_required"
        rows.append(row)

    required_verify = [
        "story_cover_copper_mirror",
        "hero_ch01_ama_coast",
        "pickup_ming_coastal_token",
        "title_harbour_witness_final",
        "audio_ch01_narration",
        REWARD_CUE_ITEM_KEY,
    ]
    report = {
        "schemaVersion": 1,
        "packageCode": batch.get("packageCode", manifest.get("packageCode")),
        "liveUpload": bool(args.upload),
        "promote": args.promote,
        "rows": rows,
        "verificationItems": [
            {k: row.get(k) for k in ("itemKey", "versionId", "assetId", "promotionStatus", "localPath", "cosObjectKey", "canonicalUrl")}
            for row in rows
            if row.get("itemKey") in required_verify
        ],
    }
    write_json(report_path, report)
    print(json.dumps({"report": str(report_path), "items": len(rows)}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"phase36-batch-produce failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
