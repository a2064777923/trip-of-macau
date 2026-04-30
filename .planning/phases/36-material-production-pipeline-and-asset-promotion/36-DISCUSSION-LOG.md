# Phase 36: Material Production Pipeline and Asset Promotion - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in `36-CONTEXT.md`; this log preserves the alternatives considered.

**Date:** 2026-04-30
**Phase:** 36 - Material Production Pipeline and Asset Promotion
**Areas discussed:** 生成策略與成本控制, 素材板與切圖流程, 音頻與影片產線, 狀態提升與回滾

---

## 生成策略與成本控制

| Option | Description | Selected |
| --- | --- | --- |
| 手動逐項生成 | 每個素材點擊後才調用供應商 | |
| 批量生成但需確認 | 每批開始前顯示成本/數量/風險確認 | ✓ |
| 逐項與批量皆可 | 默認逐項，允許勾選批量生成 | |

**User's choice:** 更希望 Codex 直接根據故事線需求生成素材並完成配置；admin 端可以有啟動入口。  
**Notes:** `image-2` 可能無法通過後端直接調用，允許由 Codex 本身生成，再走本地文件、COS、`content_assets`、素材包 item 狀態提升。

| Option | Description | Selected |
| --- | --- | --- |
| 允許真實 image-2 | 限故事線、需確認、記錄成本 | ✓ |
| 後續 phase 再做 | 先做導入/上傳/狀態流 | |
| 只允許本地腳本 | 不在 admin 頁面提供生成按鈕 | |

**User's choice:** 允許，但如果後端不能直連，就由 Codex 生成。  
**Notes:** `image-2` 不開放給所有用戶作為通用功能。

| Option | Description | Selected |
| --- | --- | --- |
| 保留候選 | 保留每次生成候選，不覆蓋舊版本，人工選一個 finalize | ✓ |
| 只保留最新 | 避免資料太多 | |
| 保留最近 N 次 | N 可在系統設定中配置 | |

**User's choice:** 保留每次生成候選。  
**Notes:** 不滿意時可以回看和選擇之前版本。

| Option | Description | Selected |
| --- | --- | --- |
| 只記錄用量 | 不限制 | |
| 數量上限 | 設每日/每批生成數量上限 | |
| 成本上限 | 設每日/每批估算成本上限，超過需超級管理員確認 | ✓ |

**User's choice:** 成本上限。  
**Notes:** 要能看到估算成本並有超級管理員確認。

---

## 素材板與切圖流程

| Option | Description | Selected |
| --- | --- | --- |
| 每章一張素材板 | 每章一張拾取物素材板，另外一張全線稱號/徽章素材板 | ✓ |
| 每種用途一張大板 | 例如拾取物一張、稱號一張、章節 hero 一張 | |
| 單獨生成 | 每個素材單獨生成，不做素材板 | |

**User's choice:** 每章一張拾取物素材板，另加全線稱號/徽章素材板。  
**Notes:** 這樣節省生成資源並保持風格一致。

| Option | Description | Selected |
| --- | --- | --- |
| Admin 手動裁切 | Admin 端提供框選/裁切 | |
| 預設網格自動切 | 先按 manifest 預設網格自動切，再允許人工微調 | ✓ |
| 本地腳本切好 | 執行階段用本地腳本切好，admin 只查看替換 | |

**User's choice:** 由 Codex 識圖後判斷哪塊對應哪個素材並自動切。  
**Notes:** 不做成系統功能。要記錄關鍵行動模板指令，後續故事線可復用。

| Option | Description | Selected |
| --- | --- | --- |
| 嚴格沿用 manifest | 使用 manifest 的 `itemKey` 和 `localPath` | |
| 保留 parent board 關聯 | 允許新名字，但必須保留 parent board 關聯 | ✓ |
| 系統自動命名 | 人工只看中文標題 | |

**User's choice:** 保留 parent board 關聯，同時仍要能對應 manifest item。  
**Notes:** 最終 DB/COS 回填不可和 manifest 脫節。

| Option | Description | Selected |
| --- | --- | --- |
| 自動回填 | 自動建立/更新 `content_assets`，回填素材包 item，狀態到 `uploaded` | ✓ |
| 本地文件先行 | 人工確認後才上傳和回填 | |
| 只上傳 COS | 不改 DB，人工最後批量綁定 | |

**User's choice:** 自動回填。  
**Notes:** 這是一次性生產步驟，不是通用 admin 功能。

---

## 音頻與影片產線

| Option | Description | Selected |
| --- | --- | --- |
| 粵語優先 | 先做粵語旁白，其他後續 | |
| 粵語 + 普通話 | 每章至少兩版 | |
| 普通話先行 | 先做普通話，粵語後補 | ✓ |

**User's choice:** 先做普通話。  
**Notes:** 粵語、英文、葡文作後續版本。

| Option | Description | Selected |
| --- | --- | --- |
| 百煉 CosyVoice | 直接生成並上傳 COS | ✓ |
| 替代 TTS 佔位 | 百煉失敗時使用替代佔位 | |
| 優先百煉，失敗不發布 | 明確佔位但不進 `published` | |

**User's choice:** 直接用百煉 CosyVoice。  
**Notes:** 生成結果應進 COS 和素材包鏈路。

| Option | Description | Selected |
| --- | --- | --- |
| 每章短 MP4 | 每章 hero 圖 + 旁白 + 字幕 + 輕微 pan/zoom | ✓ |
| 只做終章 recap | 其他章節先用 hero 圖與音頻 | |
| 不生成影片 | 只保留 metadata 和 poster | |

**User's choice:** 每章短 MP4。  
**Notes:** 影片可以由 image-2 生成的 hero 圖加平移、縮放、旁白、字幕拼接。

| Option | Description | Selected |
| --- | --- | --- |
| 內嵌繁體字幕 | 字幕由 `audio-scripts.md` 生成 | ✓ |
| 不內嵌字幕 | 小程序用文字積木顯示 | |
| 後續 polish | 旁白音頻先行，字幕後補 | |

**User's choice:** 影片必帶繁體字幕。  
**Notes:** 影片輸出需要可直接播放驗收。

---

## 狀態提升與回滾

| Option | Description | Selected |
| --- | --- | --- |
| `generated` | 文件已產出但未上傳 | |
| `uploaded` | 生成成功即自動上傳 COS | ✓ |
| candidate only | 先停在候選，不改 item 狀態 | |

**User's choice:** 生成成功就自動上傳 COS，到 `uploaded`。  
**Notes:** 不需要長時間停在 `generated`。

| Option | Description | Selected |
| --- | --- | --- |
| 自動 approved | 本地視覺/播放檢查通過後自動標記 | ✓ |
| 人工確認 | 必須 admin 頁面按確認 | |
| 不用 approved | Phase 36 只做到 uploaded | |

**User's choice:** Codex 本地檢查通過後自動 approved。  
**Notes:** 檢查需要有可追蹤 evidence。

| Option | Description | Selected |
| --- | --- | --- |
| 自動 published | approved 後自動發布，故事線立即可用 | ✓ |
| 手動批量發布 | 需手動操作 | |
| 只發布核心素材 | 其他停在 approved | |

**User's choice:** approved 後自動 published。  
**Notes:** 這條故事線要能立即使用生成素材。

| Option | Description | Selected |
| --- | --- | --- |
| version/candidate 回滾 | 回滾時把 `asset_id` 指回舊版本 | ✓ |
| 只保留上一版 | 簡化回滾 | |
| 不做回滾 | 只允許重新生成/替換 | |

**User's choice:** 保留 candidate/version 記錄並支持回滾。  
**Notes:** 不應覆蓋舊版。

---

## the agent's Discretion

- 具體 candidate/version 表設計由 planner 決定。
- 具體本地切圖腳本和視頻組裝工具由 planner 決定。
- 具體成本估算公式由 planner 決定，但需要保守且可見。

## Deferred Ideas

- 通用 admin `image-2` 生圖能力。
- admin 內建通用素材板裁切器。
- 粵語、英文、葡文音頻版本。
- 複雜 AI 視頻生成與互動玩法驗收。
