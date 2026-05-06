#!/usr/bin/env python3
"""Generate and slice a one-off image-1 POI marker board.

Secrets are read from environment variables only:
- PHASE44_IMAGE_API_KEY
- PHASE44_IMAGE_BASE_URL, defaulting to the user's compatible image endpoint
- PHASE44_IMAGE_MODEL, defaulting to gpt-image-1
"""

from __future__ import annotations

import argparse
import base64
import json
import os
import re
import sys
import urllib.request
import urllib.error
from collections import deque
from pathlib import Path
from urllib.parse import urlparse

from PIL import Image


DEFAULT_BASE_URL = "https://api.suqis.com/v1/images"
DEFAULT_MODEL = "gpt-image-1"
OUT_DIR = Path("local-content/phase44/poi-icons-image1")
BOARD_PATH = OUT_DIR / "phase44-poi-icon-board.png"
TRANSPARENT_THRESHOLD = 248
BACKGROUND_DISTANCE_THRESHOLD = 38

ICONS = [
    {
        "code": "ama_temple",
        "filename": "ama-temple-2_5d-image1.png",
        "description": "A-Ma Temple with layered red tiled roofs, incense smoke, stone steps, and small coastal shrine details",
    },
    {
        "code": "lilau_square",
        "filename": "lilau-square-2_5d-image1.png",
        "description": "Lilau Square with a Portuguese well, warm paving waves, pastel houses, and old street charm",
    },
    {
        "code": "dom_pedro_v_theatre",
        "filename": "hill-watch-2_5d-image1.png",
        "description": "Dom Pedro V Theatre / St. Augustine hill lookout with cream facade, arched windows, theatre roof, and subtle lookout motif",
    },
    {
        "code": "monte_fort",
        "filename": "monte-fort-2_5d-image1.png",
        "description": "Monte Fort with stone ramparts, cannon silhouette, bronze muzzle, and fortress platform",
    },
    {
        "code": "senado_square",
        "filename": "senado-square-2_5d-image1.png",
        "description": "Senado Square with Portuguese wave paving, civic square buildings, fountain, and warm plaza colors",
    },
]


def image_generation_endpoint() -> str:
    endpoint = (os.environ.get("PHASE44_IMAGE_GENERATIONS_URL")
                or os.environ.get("PHASE44_IMAGE_BASE_URL")
                or DEFAULT_BASE_URL).strip().rstrip("/")
    if endpoint.endswith("/images"):
        return endpoint + "/generations"
    if endpoint.endswith("/images/generations"):
        return endpoint
    parsed = urlparse(endpoint)
    if parsed.path in ("", "/"):
        return endpoint + "/v1/images/generations"
    return endpoint


def image_model() -> str:
    return os.environ.get("PHASE44_IMAGE_MODEL") or os.environ.get("PHASE36_IMAGE_MODEL") or DEFAULT_MODEL


def image_api_key() -> str:
    key = os.environ.get("PHASE44_IMAGE_API_KEY") or os.environ.get("PHASE36_IMAGE_API_KEY") or os.environ.get("OPENAI_API_KEY")
    if not key:
        raise RuntimeError("Set PHASE44_IMAGE_API_KEY before generating image assets.")
    return key


def extract_image_payload(data: dict) -> tuple[str | None, str | None]:
    candidates = data.get("data") or data.get("images") or data.get("result") or data.get("output")
    if isinstance(candidates, dict):
        candidates = [candidates]
    if not isinstance(candidates, list):
        candidates = [data]
    for candidate in candidates:
        if not isinstance(candidate, dict):
            continue
        b64 = candidate.get("b64_json") or candidate.get("base64") or candidate.get("image_base64") or candidate.get("image")
        url = candidate.get("url") or candidate.get("image_url")
        if b64 or url:
            return b64, url
    return None, None


def prompt_text() -> str:
    descriptions = "\n".join(f"{index + 1}. {item['description']}" for index, item in enumerate(ICONS))
    return f"""
Create a single sprite sheet containing five separate 2.5D cartoon landmark map icons for a Macau exploration mini-program.

Layout:
- One row, five equal cells.
- Each icon centered in its own cell with generous transparent padding.
- Keep each object separate and not touching neighboring icons.
- Transparent background only.
- No text, no letters, no labels, no numbers, no watermark, no UI frame.

Style:
- 2.5D isometric cartoon tourist-map icon style.
- Clean rounded shapes, polished game asset look, warm Macau heritage palette.
- Consistent lighting, perspective, scale, and outline thickness across all icons.
- Suitable for WeChat mini-program native map markers.

Icons from left to right:
{descriptions}
""".strip()


def generate_board(output_path: Path, overwrite: bool) -> str:
    if output_path.exists() and not overwrite:
        return "existing"
    payload = {
        "model": image_model(),
        "prompt": prompt_text(),
        "size": os.environ.get("PHASE44_IMAGE_SIZE", "1536x1024"),
        "n": 1,
    }
    quality = os.environ.get("PHASE44_IMAGE_QUALITY", "auto").strip()
    if quality:
        payload["quality"] = quality
    background = os.environ.get("PHASE44_IMAGE_BACKGROUND", "").strip()
    if background:
        payload["background"] = background
    request = urllib.request.Request(
        image_generation_endpoint(),
        data=json.dumps(payload, ensure_ascii=False).encode("utf-8"),
        method="POST",
        headers={
            "Authorization": f"Bearer {image_api_key()}",
            "Content-Type": "application/json; charset=utf-8",
        },
    )
    try:
        with urllib.request.urlopen(request, timeout=240) as response:
            data = json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", errors="replace")
        raise RuntimeError(f"Image endpoint HTTP {exc.code}: {detail[:600]}") from exc
    b64, url = extract_image_payload(data)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    if b64:
        output_path.write_bytes(base64.b64decode(b64))
        return "generated_b64"
    if url:
        download_request = urllib.request.Request(url, method="GET", headers={"Accept": "image/png,image/*,*/*"})
        with urllib.request.urlopen(download_request, timeout=240) as response:
            output_path.write_bytes(response.read())
        return "generated_url"
    raise RuntimeError(f"Image endpoint returned no image payload: {data}")


def trim_transparent(image: Image.Image) -> Image.Image:
    alpha = image.getchannel("A")
    bbox = alpha.getbbox()
    if not bbox:
        return image
    left, top, right, bottom = bbox
    pad = 28
    left = max(0, left - pad)
    top = max(0, top - pad)
    right = min(image.width, right + pad)
    bottom = min(image.height, bottom + pad)
    return image.crop((left, top, right, bottom))


def detect_icon_components(image: Image.Image) -> list[tuple[int, int, int, int]]:
    alpha = image.getchannel("A")
    width, height = image.size
    pixels = alpha.load()
    visited = bytearray(width * height)
    boxes: list[tuple[int, int, int, int, int]] = []

    for y in range(height):
        for x in range(width):
            index = y * width + x
            if visited[index] or pixels[x, y] < 16:
                continue
            queue: deque[tuple[int, int]] = deque([(x, y)])
            visited[index] = 1
            min_x = max_x = x
            min_y = max_y = y
            area = 0
            while queue:
                cx, cy = queue.popleft()
                area += 1
                min_x = min(min_x, cx)
                max_x = max(max_x, cx)
                min_y = min(min_y, cy)
                max_y = max(max_y, cy)
                for nx, ny in ((cx + 1, cy), (cx - 1, cy), (cx, cy + 1), (cx, cy - 1)):
                    if nx < 0 or ny < 0 or nx >= width or ny >= height:
                        continue
                    next_index = ny * width + nx
                    if visited[next_index] or pixels[nx, ny] < 16:
                        continue
                    visited[next_index] = 1
                    queue.append((nx, ny))
            if area >= 700:
                boxes.append((min_x, min_y, max_x + 1, max_y + 1, area))

    boxes.sort(key=lambda box: box[4], reverse=True)
    selected = boxes[:len(ICONS)]
    selected.sort(key=lambda box: (box[0] + box[2]) / 2)
    return [(left, top, right, bottom) for left, top, right, bottom, _ in selected]


def make_near_white_transparent(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    pixels = rgba.load()
    for y in range(rgba.height):
        for x in range(rgba.width):
            r, g, b, a = pixels[x, y]
            if a and r >= TRANSPARENT_THRESHOLD and g >= TRANSPARENT_THRESHOLD and b >= TRANSPARENT_THRESHOLD:
                pixels[x, y] = (255, 255, 255, 0)
    return rgba


def is_background_pixel(pixel: tuple[int, int, int, int]) -> bool:
    r, g, b, a = pixel
    if a == 0:
        return True
    bright = min(r, g, b) >= 214
    low_saturation = max(r, g, b) - min(r, g, b) <= BACKGROUND_DISTANCE_THRESHOLD
    return bright and low_saturation


def flood_remove_generated_checkerboard(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    pixels = rgba.load()
    width, height = rgba.size
    visited = bytearray(width * height)
    queue: deque[tuple[int, int]] = deque()

    def enqueue(x: int, y: int) -> None:
        if x < 0 or y < 0 or x >= width or y >= height:
            return
        index = y * width + x
        if visited[index]:
            return
        visited[index] = 1
        if is_background_pixel(pixels[x, y]):
            queue.append((x, y))

    for x in range(width):
        enqueue(x, 0)
        enqueue(x, height - 1)
    for y in range(height):
        enqueue(0, y)
        enqueue(width - 1, y)

    while queue:
        x, y = queue.popleft()
        pixels[x, y] = (255, 255, 255, 0)
        enqueue(x + 1, y)
        enqueue(x - 1, y)
        enqueue(x, y + 1)
        enqueue(x, y - 1)

    return rgba


def remove_text_like_marks(image: Image.Image) -> Image.Image:
    # Safety cleanup for accidental tiny dark marks near cell bottoms. It avoids
    # touching the main landmark body by only clearing thin horizontal bands.
    rgba = image.convert("RGBA")
    pixels = rgba.load()
    for y in range(int(rgba.height * 0.78), rgba.height):
        dark_count = 0
        for x in range(rgba.width):
            r, g, b, a = pixels[x, y]
            if a > 180 and r < 75 and g < 75 and b < 75:
                dark_count += 1
        if 2 <= dark_count <= 80:
            for x in range(rgba.width):
                r, g, b, a = pixels[x, y]
                if a > 120 and r < 90 and g < 90 and b < 90:
                    pixels[x, y] = (r, g, b, 0)
    return rgba


def slice_board(board_path: Path, output_dir: Path) -> list[dict]:
    board = Image.open(board_path).convert("RGBA")
    board = flood_remove_generated_checkerboard(make_near_white_transparent(board))
    transparent_board_path = output_dir / "phase44-poi-icon-board-transparent.png"
    board.save(transparent_board_path)
    component_boxes = detect_icon_components(board)
    cell_width = board.width // len(ICONS)
    rows = []
    for index, item in enumerate(ICONS):
        if len(component_boxes) >= len(ICONS):
            left, top, right, bottom = component_boxes[index]
            x_pad = 14
            y_pad = 36
            if index > 0:
                previous_right = component_boxes[index - 1][2]
                x_pad_left = max(0, min(x_pad, (left - previous_right) // 2 - 1))
            else:
                x_pad_left = x_pad
            if index < len(component_boxes) - 1:
                next_left = component_boxes[index + 1][0]
                x_pad_right = max(0, min(x_pad, (next_left - right) // 2 - 1))
            else:
                x_pad_right = x_pad
            cell = board.crop((
                max(0, left - x_pad_left),
                max(0, top - y_pad),
                min(board.width, right + x_pad_right),
                min(board.height, bottom + y_pad),
            ))
        else:
            left = index * cell_width
            right = board.width if index == len(ICONS) - 1 else (index + 1) * cell_width
            cell = board.crop((left, 0, right, board.height))
        cell = flood_remove_generated_checkerboard(remove_text_like_marks(make_near_white_transparent(cell)))
        trimmed = trim_transparent(cell)
        icon = Image.new("RGBA", (512, 512), (255, 255, 255, 0))
        scale = min(430 / max(trimmed.width, 1), 430 / max(trimmed.height, 1))
        resized = trimmed.resize((max(1, round(trimmed.width * scale)), max(1, round(trimmed.height * scale))), Image.Resampling.LANCZOS)
        icon.alpha_composite(resized, ((512 - resized.width) // 2, (512 - resized.height) // 2))
        output_path = output_dir / item["filename"]
        icon.save(output_path)
        rows.append({
            "code": item["code"],
            "filename": item["filename"],
            "path": str(output_path),
            "bytes": output_path.stat().st_size,
            "alphaBBox": icon.getchannel("A").getbbox(),
            "componentBox": component_boxes[index] if len(component_boxes) >= len(ICONS) else None,
        })
    return rows


def validate_no_text_filenames(rows: list[dict]) -> None:
    for row in rows:
        if re.search(r"\s", row["filename"]):
            raise RuntimeError(f"Unexpected whitespace in filename: {row['filename']}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--overwrite", action="store_true")
    parser.add_argument("--board-only", action="store_true")
    parser.add_argument("--size", default=os.environ.get("PHASE44_IMAGE_SIZE", "1536x1024"))
    args = parser.parse_args()

    os.environ["PHASE44_IMAGE_SIZE"] = args.size
    status = generate_board(BOARD_PATH, args.overwrite)
    rows = [] if args.board_only else slice_board(BOARD_PATH, OUT_DIR)
    validate_no_text_filenames(rows)
    report = {
        "schemaVersion": 1,
        "provider": "suqis-compatible",
        "model": image_model(),
        "boardPath": str(BOARD_PATH),
        "generationStatus": status,
        "requirements": {
            "background": "transparent",
            "noText": True,
            "singleBoardMultiIcon": True,
        },
        "icons": rows,
    }
    report_path = OUT_DIR / "phase44-poi-icon-board-report.json"
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"report": str(report_path), "status": status, "icons": len(rows)}, ensure_ascii=False))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
