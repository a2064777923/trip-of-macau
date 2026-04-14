# Lisboeta 室內手繪瓦片地圖

這個目錄現在包含三層室內地圖的可重複生成流程：

- `scripts/generate_lisboeta_tiles.py`
  從 `Mall-Directory_2022_TC-scaled.jpg` 裁出三層，重繪成無文字的手繪風底圖，切成 `256x256` 瓦片，並輸出 POI CSV / JSON。
- `data/poi_seed.csv`
  商舖與 POI 的種子資料。這裡保存原始裁切座標系下的定位點，腳本會轉成最終瓦片座標。
- `assets/floors/*.png`
  每層完整手繪圖。
- `tiles/<floor_id>/*.png`
  每層的瓦片。
- `pois.csv`
  最終可直接供遊戲或資料層使用的商舖位置表。
- `preview/index.html`
  本地預覽頁，可切樓層、拖拽縮放、點清單聚焦到對應位置。

## 重新生成

在工作區根目錄執行：

```powershell
python .\map\scripts\generate_lisboeta_tiles.py
```

## 輸出說明

`pois.csv` 主要欄位：

- `floor_id` / `floor_label`
- `code`
- `name_en`
- `category`
- `center_x` / `center_y`
- `tile_id`
- `tile_col` / `tile_row`
- `tile_local_x` / `tile_local_y`

## 設計補充

- 地圖採用統一暖色紙張底與粗筆邊線，方便後續做收集物、尋路點、互動熱區。
- 商舖定位優先使用原圖上的編號或名稱中心點；原圖上合併成一整塊但屬於多個商舖的區域，使用人工補點，避免把多店誤合併成一個互動點。
- `Ground Floor / 地下` 依照原始目錄圖的英文標示保留為 `g`，方便和來源素材對齊。
