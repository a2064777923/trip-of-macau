# Lottie JSON-only 動畫設計

## route-copper-pulse

- 用途：故事線路線高亮、當前章節路徑脈衝。
- 視覺：銅色光點沿路線節點呼吸式亮起，灰色非當前路線保持低透明度。
- 技術：JSON-only Lottie；小程序端使用官方 `lottie-miniprogram` canvas，`path` 使用 COS 網絡地址。
- fallback：`story_banner_route_map`。

## pickup-shimmer

- 用途：拾取物、支線線索、隱藏碎片的出現提示。
- 視覺：紅點底座加銅金微光，中心短暫閃爍，避免遮擋地圖。
- 技術：循環播放，默認 `defaultLoop=true`、`defaultAutoplay=true`。
- fallback：`poster_video_fallback` 或普通紅點 marker。

## chapter-clear

- 用途：章節完成、全收集完成、隱藏挑戰解鎖。
- 視覺：碎片合攏成一枚小銅鏡，再散出地圖路線粒子。
- 技術：可在獎勵彈窗或全屏演出上播放；頁面退出時需銷毀動畫實例。
- fallback：`poster_video_fallback`。

## final-mirror-synthesis

- 用途：終章「完整濠江戰火銅鏡」合成與全線通關。
- 視覺：五枚碎片從五個方向飛入，海浪、城牆、鐘聲、炮火與廣場波紋逐層疊合。
- 技術：JSON-only，不使用 `.lottie` 包或序列幀；首版不依賴骨骼動畫。
- fallback：`poster_video_fallback`，必要時再落回 `story_cover_copper_mirror`。
