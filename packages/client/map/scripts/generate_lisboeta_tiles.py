from __future__ import annotations

import csv
import json
import math
import random
import shutil
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

import cv2
import numpy as np
from opencc import OpenCC
from PIL import Image


ROOT = Path(r"D:\Archive\gemini")
MAP_ROOT = ROOT / "map"
SOURCE_IMAGE = ROOT / "Mall-Directory_2022_TC-scaled.jpg"
SEED_CSV = MAP_ROOT / "data" / "poi_seed.csv"
ASSET_DIR = MAP_ROOT / "assets"
FLOOR_DIR = ASSET_DIR / "floors"
TILE_DIR = MAP_ROOT / "tiles"
MANIFEST_PATH = ASSET_DIR / "manifest.json"
MANIFEST_JS_PATH = ASSET_DIR / "manifest.js"
POI_CSV_PATH = MAP_ROOT / "pois.csv"
POI_JSON_PATH = ASSET_DIR / "pois.json"
TILE_SIZE = 256
T2S = OpenCC("t2s")


@dataclass(frozen=True)
class FloorConfig:
    floor_id: str
    floor_label_en: str
    floor_label_tc: str
    crop_box: tuple[int, int, int, int]
    canvas_size: tuple[int, int]
    paper_seed: int

    @property
    def crop_width(self) -> int:
        return self.crop_box[2] - self.crop_box[0]

    @property
    def crop_height(self) -> int:
        return self.crop_box[3] - self.crop_box[1]


FLOORS: dict[str, FloorConfig] = {
    "1f": FloorConfig(
        floor_id="1f",
        floor_label_en="1st Floor",
        floor_label_tc="一樓",
        crop_box=(250, 80, 1605, 1100),
        canvas_size=(1792, 1280),
        paper_seed=11,
    ),
    "2f": FloorConfig(
        floor_id="2f",
        floor_label_en="2nd Floor",
        floor_label_tc="二樓",
        crop_box=(1620, 80, 2515, 830),
        canvas_size=(1280, 1024),
        paper_seed=23,
    ),
    "g": FloorConfig(
        floor_id="g",
        floor_label_en="Ground Floor",
        floor_label_tc="地下",
        crop_box=(1580, 720, 2520, 1510),
        canvas_size=(1280, 1024),
        paper_seed=37,
    ),
}


CLASS_SPECS = [
    {"name": "purple", "fill": (183, 157, 206), "min_area": 200, "mask": lambda h, s, v: (h >= 136) & (h <= 145) & (s > 25) & (v > 100)},
    {"name": "green", "fill": (199, 217, 108), "min_area": 160, "mask": lambda h, s, v: (h >= 32) & (h <= 40) & (s > 25) & (v > 100)},
    {"name": "yellow", "fill": (245, 213, 94), "min_area": 140, "mask": lambda h, s, v: (h >= 22) & (h <= 32) & (s > 25) & (v > 100)},
    {"name": "orange", "fill": (232, 174, 119), "min_area": 140, "mask": lambda h, s, v: (h >= 10) & (h < 22) & (s > 25) & (v > 100)},
    {"name": "pink", "fill": (223, 120, 169), "min_area": 90, "mask": lambda h, s, v: (h >= 165) & (s > 30) & (v > 100)},
    {"name": "red", "fill": (232, 122, 108), "min_area": 90, "mask": lambda h, s, v: (h <= 8) & (s > 40) & (v > 100)},
    {"name": "cyan", "fill": (175, 214, 231), "min_area": 1200, "mask": lambda h, s, v: (h >= 80) & (h <= 110) & (s > 15) & (v > 100)},
]


def ensure_clean_dir(path: Path) -> None:
    if path.exists():
        shutil.rmtree(path)
    path.mkdir(parents=True, exist_ok=True)


def paper_texture(size: tuple[int, int], seed: int) -> np.ndarray:
    width, height = size
    rng = np.random.default_rng(seed)
    base = np.full((height, width, 3), [244, 238, 224], dtype=np.uint8)
    noise = rng.normal(0, 5.5, (height, width, 1)).astype(np.int16)
    paper = np.clip(base.astype(np.int16) + noise, 0, 255).astype(np.uint8)

    # Add a broad wash so the paper is not perfectly flat.
    wash = np.zeros((height, width), dtype=np.float32)
    cv2.circle(wash, (int(width * 0.18), int(height * 0.18)), int(min(width, height) * 0.35), 0.8, -1)
    cv2.GaussianBlur(wash, (0, 0), sigmaX=width * 0.06, sigmaY=height * 0.06, dst=wash)
    paper = np.clip(paper.astype(np.float32) + wash[:, :, None] * np.array([8, 6, 3], dtype=np.float32), 0, 255).astype(np.uint8)
    return paper


def simplify_mask(mask: np.ndarray, open_size: int = 3, close_size: int = 5) -> np.ndarray:
    opened = cv2.morphologyEx(mask.astype(np.uint8), cv2.MORPH_OPEN, np.ones((open_size, open_size), np.uint8))
    closed = cv2.morphologyEx(opened, cv2.MORPH_CLOSE, np.ones((close_size, close_size), np.uint8))
    return closed


def draw_components(canvas: np.ndarray, mask: np.ndarray, fill: tuple[int, int, int], min_area: int, outline: tuple[int, int, int], width: int) -> None:
    count, labels, stats, _ = cv2.connectedComponentsWithStats(mask.astype(np.uint8), 8)
    fill_color = tuple(int(v) for v in fill)
    outline_color = tuple(int(v) for v in outline)
    for idx in range(1, count):
        x, y, w, h, area = stats[idx]
        if area < min_area:
            continue
        component = (labels == idx).astype(np.uint8)
        contours, _ = cv2.findContours(component, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        if not contours:
            continue
        cv2.drawContours(canvas, contours, -1, fill_color, -1)
        cv2.drawContours(canvas, contours, -1, outline_color, width)


def overlay_dots(canvas: np.ndarray, mask: np.ndarray, seed: int) -> None:
    rng = random.Random(seed)
    height, width = mask.shape
    dot_color = np.array([156, 131, 107], dtype=np.uint8)
    step = 12
    for y in range(6, height, step):
        for x in range(6, width, step):
            jitter_x = x + rng.randint(-2, 2)
            jitter_y = y + rng.randint(-2, 2)
            if 0 <= jitter_x < width and 0 <= jitter_y < height and mask[jitter_y, jitter_x]:
                cv2.circle(canvas, (jitter_x, jitter_y), 1, dot_color.tolist(), -1)


def render_floor(config: FloorConfig, source: Image.Image) -> Image.Image:
    crop = source.crop(config.crop_box).resize(config.canvas_size, Image.Resampling.LANCZOS)
    rgb = np.array(crop.convert("RGB"))
    hsv = cv2.cvtColor(rgb, cv2.COLOR_RGB2HSV)
    h, s, v = [hsv[:, :, index] for index in range(3)]

    canvas = paper_texture(config.canvas_size, config.paper_seed)
    outline = (104, 78, 57)

    base_mask = (s < 20) & (v > 175) & (v < 242)
    base_mask = simplify_mask(base_mask, open_size=3, close_size=5)
    draw_components(canvas, base_mask, fill=(227, 225, 221), min_area=1000, outline=outline, width=3)

    for spec in CLASS_SPECS:
        class_mask = simplify_mask(spec["mask"](h, s, v), open_size=2, close_size=3)
        draw_components(canvas, class_mask, fill=spec["fill"], min_area=spec["min_area"], outline=outline, width=2)

    colored_mask = (s > 22) & (v > 95)
    overlay_dots(canvas, colored_mask, seed=config.paper_seed * 17)

    # A final light blur softens sharp raster corners and helps the hand-drawn look.
    softened = cv2.GaussianBlur(canvas, (0, 0), sigmaX=0.35, sigmaY=0.35)

    # Floor-specific cleanup for source-image bleed near crop boundaries.
    if config.floor_id == "1f":
        softened[-18:, :, :] = paper_texture((config.canvas_size[0], 18), config.paper_seed + 101)
    if config.floor_id == "g":
        softened[:, :88, :] = paper_texture((88, config.canvas_size[1]), config.paper_seed + 211)
        softened[:96, :, :] = paper_texture((config.canvas_size[0], 96), config.paper_seed + 307)

    return Image.fromarray(softened)


def save_tiles(image: Image.Image, floor_id: str) -> dict[str, int]:
    floor_tile_dir = TILE_DIR / floor_id
    ensure_clean_dir(floor_tile_dir)
    width, height = image.size
    cols = math.ceil(width / TILE_SIZE)
    rows = math.ceil(height / TILE_SIZE)
    for row in range(rows):
        for col in range(cols):
            box = (
                col * TILE_SIZE,
                row * TILE_SIZE,
                min((col + 1) * TILE_SIZE, width),
                min((row + 1) * TILE_SIZE, height),
            )
            tile = Image.new("RGB", (TILE_SIZE, TILE_SIZE), (244, 238, 224))
            tile_crop = image.crop(box)
            tile.paste(tile_crop, (0, 0))
            tile.save(floor_tile_dir / f"{col}_{row}.png")
    return {"cols": cols, "rows": rows}


def load_seed_rows() -> list[dict[str, str]]:
    with SEED_CSV.open("r", encoding="utf-8-sig", newline="") as handle:
        return list(csv.DictReader(handle))


def derive_name_sc(name_tc: str, name_en: str) -> str:
    return T2S.convert(name_tc) if name_tc.strip() else name_en


def project_point(config: FloorConfig, src_x: float, src_y: float) -> tuple[int, int]:
    scale_x = config.canvas_size[0] / config.crop_width
    scale_y = config.canvas_size[1] / config.crop_height
    return round(src_x * scale_x), round(src_y * scale_y)


def build_poi_rows(seed_rows: Iterable[dict[str, str]], floor_tiles: dict[str, dict[str, int]]) -> list[dict[str, object]]:
    pois: list[dict[str, object]] = []
    for row in seed_rows:
        config = FLOORS[row["floor_id"]]
        center_x, center_y = project_point(config, float(row["source_x"]), float(row["source_y"]))
        tile_col = center_x // TILE_SIZE
        tile_row = center_y // TILE_SIZE
        name_tc = row["name_tc"]
        name_sc = derive_name_sc(name_tc, row["name_en"])
        pois.append(
            {
                "floor_id": config.floor_id,
                "floor_label_en": config.floor_label_en,
                "floor_label_tc": config.floor_label_tc,
                "floor_label_sc": T2S.convert(config.floor_label_tc),
                "code": row["code"],
                "name_en": row["name_en"],
                "name_tc": name_tc,
                "name_sc": name_sc,
                "category": row["category"],
                "source_type": row["source_type"],
                "center_x": center_x,
                "center_y": center_y,
                "tile_id": f"{config.floor_id}_{tile_col}_{tile_row}",
                "tile_col": tile_col,
                "tile_row": tile_row,
                "tile_local_x": center_x - tile_col * TILE_SIZE,
                "tile_local_y": center_y - tile_row * TILE_SIZE,
                "tile_path": f"../tiles/{config.floor_id}/{tile_col}_{tile_row}.png",
                "notes": row["notes"],
                "sort_order": int(row["sort_order"]),
            }
        )

    pois.sort(key=lambda item: (item["floor_id"], item["sort_order"], item["code"]))
    return pois


def write_csv(rows: list[dict[str, object]], path: Path) -> None:
    fieldnames = [
        "floor_id",
        "floor_label_en",
        "floor_label_tc",
        "floor_label_sc",
        "code",
        "name_en",
        "name_tc",
        "name_sc",
        "category",
        "source_type",
        "center_x",
        "center_y",
        "tile_id",
        "tile_col",
        "tile_row",
        "tile_local_x",
        "tile_local_y",
        "tile_path",
        "notes",
    ]
    with path.open("w", encoding="utf-8-sig", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=fieldnames)
        writer.writeheader()
        for row in rows:
            writer.writerow({key: row[key] for key in fieldnames})


def build_manifest(floor_tiles: dict[str, dict[str, int]], poi_rows: list[dict[str, object]]) -> dict[str, object]:
    floors = []
    for floor_id, config in FLOORS.items():
        tile_meta = floor_tiles[floor_id]
        floors.append(
            {
                "floor_id": floor_id,
                "labels": {
                    "en": config.floor_label_en,
                    "tc": config.floor_label_tc,
                    "sc": T2S.convert(config.floor_label_tc),
                },
                "width": config.canvas_size[0],
                "height": config.canvas_size[1],
                "tile_size": TILE_SIZE,
                "tile_cols": tile_meta["cols"],
                "tile_rows": tile_meta["rows"],
                "image_path": f"../assets/floors/{floor_id}.png",
                "tile_path_template": f"../tiles/{floor_id}/{{col}}_{{row}}.png",
            }
        )

    return {"floors": floors, "pois": poi_rows}


def main() -> None:
    ASSET_DIR.mkdir(parents=True, exist_ok=True)
    FLOOR_DIR.mkdir(parents=True, exist_ok=True)
    TILE_DIR.mkdir(parents=True, exist_ok=True)

    source = Image.open(SOURCE_IMAGE)
    floor_tiles: dict[str, dict[str, int]] = {}

    for floor_id, config in FLOORS.items():
        rendered = render_floor(config, source)
        rendered.save(FLOOR_DIR / f"{floor_id}.png")
        floor_tiles[floor_id] = save_tiles(rendered, floor_id)

    seed_rows = load_seed_rows()
    poi_rows = build_poi_rows(seed_rows, floor_tiles)

    write_csv(poi_rows, POI_CSV_PATH)
    POI_JSON_PATH.write_text(json.dumps(poi_rows, ensure_ascii=False, indent=2), encoding="utf-8")
    manifest = build_manifest(floor_tiles, poi_rows)
    manifest_json = json.dumps(manifest, ensure_ascii=False, indent=2)
    MANIFEST_PATH.write_text(manifest_json, encoding="utf-8")
    MANIFEST_JS_PATH.write_text(f"window.__MAP_MANIFEST__ = {manifest_json};\n", encoding="utf-8")

    print(f"Generated floors: {', '.join(FLOORS)}")
    print(f"POI CSV: {POI_CSV_PATH}")
    print(f"Manifest: {MANIFEST_PATH}")


if __name__ == "__main__":
    main()
