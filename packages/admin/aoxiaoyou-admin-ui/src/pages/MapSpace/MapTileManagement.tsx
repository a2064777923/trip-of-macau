import React, { useEffect, useMemo, useState } from "react";
import { PageContainer } from "@ant-design/pro-components";
import { useNavigate } from "react-router-dom";
import {
  App as AntdApp,
  Alert,
  Button,
  Card,
  Col,
  Descriptions,
  Divider,
  Empty,
  Form,
  Input,
  InputNumber,
  List,
  Modal,
  Row,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Tabs,
  Typography,
  Upload,
} from "antd";
import type { UploadFile } from "antd/es/upload/interface";
import {
  AimOutlined,
  CloudUploadOutlined,
  DeleteOutlined,
  EditOutlined,
  FileSearchOutlined,
  InboxOutlined,
  PlusOutlined,
  ReloadOutlined,
  SaveOutlined,
} from "@ant-design/icons";
import {
  confirmIndoorMarkerCsv,
  createIndoorNode,
  deleteIndoorNode,
  getAdminActivities,
  getAdminPois,
  getAdminRewardRules,
  getAdminStorylines,
  getAdminTranslationSettings,
  getBadges,
  getCollectibles,
  getIndoorBuildingDetail,
  getIndoorBuildings,
  getIndoorFloorDetail,
  getIndoorNodes,
  getStorylineChapters,
  importIndoorFloorImage,
  importIndoorTileZip,
  previewIndoorMarkerCsv,
  previewIndoorTileZip,
  validateIndoorRuleGraph,
  updateIndoorFloor,
  updateIndoorNode,
} from "../../services/api";
import type {
  AdminIndoorBehaviorProfile,
  AdminIndoorBuildingDetail,
  AdminIndoorBuildingItem,
  AdminIndoorFloorItem,
  AdminIndoorMarkerCsvPreview,
  AdminIndoorMarkerItem,
  AdminIndoorNodeItem,
  AdminIndoorNodePayload,
  AdminIndoorOverlayGeometry,
  AdminIndoorPathGraph,
  AdminIndoorNodePoint,
  AdminIndoorMarkerPayload,
  AdminIndoorTilePreview,
  AdminPoiListItem,
  AdminStoryChapterItem,
  AdminStorylineListItem,
  AdminTranslationSettings,
} from "../../types/admin";
import LocalizedFieldGroup, {
  buildLocalizedFieldNames,
} from "../../components/localization/LocalizedFieldGroup";
import IndoorRuleWorkbench from "../../components/indoor/IndoorRuleWorkbench";
import IndoorPathEditor from "../../components/indoor/IndoorPathEditor";
import IndoorOverlayGeometryEditor from "../../components/indoor/IndoorOverlayGeometryEditor";
import IndoorRuleAppearanceEditor from "../../components/indoor/IndoorRuleAppearanceEditor";
import IndoorRuleTriggerChainEditor from "../../components/indoor/IndoorRuleTriggerChainEditor";
import IndoorRuleEffectEditor from "../../components/indoor/IndoorRuleEffectEditor";
import MediaAssetPickerField from "../../components/media/MediaAssetPickerField";
import SpatialPopupDisplayField from "../../components/spatial/SpatialPopupDisplayField";

const { Dragger } = Upload;
const { Text } = Typography;

const markerNameFields = buildLocalizedFieldNames("nodeName");
const markerDescriptionFields = buildLocalizedFieldNames("description");

const markerTypeOptions = [
  { label: "自訂標記", value: "custom" },
  { label: "POI", value: "poi" },
  { label: "商店", value: "shop" },
  { label: "服務", value: "service" },
  { label: "地標", value: "landmark" },
  { label: "升降機", value: "elevator" },
  { label: "樓梯", value: "stairs" },
  { label: "洗手間", value: "restroom" },
  { label: "入口", value: "entrance" },
  { label: "出口", value: "exit" },
];

const linkedEntityOptions = [
  { label: "未綁定", value: "" },
  { label: "任務", value: "task" },
  { label: "活動", value: "activity" },
  { label: "收集物", value: "collectible" },
  { label: "徽章", value: "badge" },
  { label: "章節", value: "chapter" },
  { label: "觸發事件", value: "event" },
];

const statusOptions = [
  { label: "未發佈", value: "unpublished" },
  { label: "編輯中", value: "editing" },
  { label: "審批中", value: "reviewing" },
  { label: "已發布", value: "published" },
  { label: "已下線", value: "archived" },
  { label: "已刪除", value: "deleted" },
];

const SHOWCASE_BUILDING_CODE = "lisboeta_macau";
const SHOWCASE_FLOOR_CODES = ["G", "1F", "2F"];

const defaultMarkerPopupConfig = JSON.stringify(
  { enabled: false, mode: "bubble" },
  null,
  2,
);
const defaultMarkerDisplayConfig = JSON.stringify(
  { labelMode: "always", showPulse: true },
  null,
  2,
);
const presentationModeOptions = [
  { label: "標記點", value: "marker" },
  { label: "覆蓋物", value: "overlay" },
  { label: "混合模式", value: "hybrid" },
];
const overlayTypeOptions = [
  { label: "點", value: "point" },
  { label: "折線", value: "polyline" },
  { label: "多邊形", value: "polygon" },
];
const runtimeSupportOptions = [
  { label: "Phase 15 僅存檔", value: "phase15_storage_only" },
  { label: "Phase 16 規劃中", value: "phase16_planned" },
  { label: "Phase 16 可執行", value: "phase16_supported" },
  { label: "未來擴展", value: "future_only" },
  { label: "預覽實驗", value: "preview" },
];
const inheritModeOptions = [
  { label: "覆寫", value: "override" },
  { label: "附加", value: "append" },
  { label: "沿用綁定主體", value: "linked_entity_default" },
  { label: "只用綁定主體", value: "linked_entity_only" },
  { label: "手動定義", value: "manual" },
];
const defaultMarkerTags = ["室內", "互動標記"];

const markerTagPresetOptions = [
  "室內",
  "互動標記",
  "入口",
  "出口",
  "服務台",
  "洗手間",
  "電梯",
  "樓梯",
  "餐飲",
  "購物",
  "打卡點",
  "彩蛋",
].map((value) => ({ label: value, value }));

const markerTagTemplateOptions = [
  { label: "基礎服務點", value: ["室內", "互動標記", "服務台"] },
  { label: "出入口導引", value: ["室內", "互動標記", "入口", "出口"] },
  { label: "導覽打卡點", value: ["室內", "互動標記", "導覽", "打卡點"] },
  { label: "彩蛋挑戰點", value: ["室內", "互動標記", "彩蛋"] },
  { label: "無障礙設施", value: ["室內", "互動標記", "電梯", "服務"] },
];

interface MarkerDraftRecord {
  draftId: string;
  draftName: string;
  savedAt: string;
  isAuto: boolean;
  values: Partial<AdminIndoorMarkerPayload>;
  editingMarkerId?: number | null;
}

interface LinkedEntityOption {
  label: string;
  value: number;
}

interface TileManifestEntry {
  z: number;
  x: number;
  y: number;
  url?: string;
}

interface TileManifest {
  defaultLevel: number;
  gridCols: number;
  gridRows: number;
  tiles: TileManifestEntry[];
}

function pickLocalizedName(input?: {
  nameZht?: string;
  nameZh?: string;
  nameEn?: string;
  namePt?: string;
  floorNameZht?: string;
  floorNameZh?: string;
  floorNameEn?: string;
  floorNamePt?: string;
  nodeNameZht?: string;
  nodeNameZh?: string;
  nodeNameEn?: string;
  nodeNamePt?: string;
  buildingCode?: string;
  floorCode?: string;
  markerCode?: string;
}) {
  return (
    input?.nameZht ||
    input?.nameZh ||
    input?.nameEn ||
    input?.namePt ||
    input?.floorNameZht ||
    input?.floorNameZh ||
    input?.floorNameEn ||
    input?.floorNamePt ||
    input?.nodeNameZht ||
    input?.nodeNameZh ||
    input?.nodeNameEn ||
    input?.nodeNamePt ||
    input?.buildingCode ||
    input?.floorCode ||
    input?.markerCode ||
    "-"
  );
}

function buildUploadFile(file: File | null, uid: string): UploadFile[] {
  if (!file) {
    return [];
  }
  return [
    { uid, name: file.name, status: "done", size: file.size, type: file.type },
  ];
}

function parseTileManifest(value?: string | null): TileManifest | null {
  if (!value) {
    return null;
  }
  try {
    const parsed = JSON.parse(value) as Partial<TileManifest>;
    if (!parsed.tiles?.length || !parsed.gridCols || !parsed.gridRows) {
      return null;
    }
    return {
      defaultLevel: Number.isFinite(parsed.defaultLevel)
        ? Number(parsed.defaultLevel)
        : 0,
      gridCols: parsed.gridCols,
      gridRows: parsed.gridRows,
      tiles: parsed.tiles,
    };
  } catch (error) {
    console.warn("Failed to parse indoor tile manifest.", error);
    return null;
  }
}

function renderStatus(status?: string | null) {
  if (status === "published") {
    return <Tag color="green">已發布</Tag>;
  }
  if (status === "unpublished") {
    return <Tag>未發佈</Tag>;
  }
  if (status === "editing" || status === "draft") {
    return <Tag color="gold">編輯中</Tag>;
  }
  if (status === "reviewing") {
    return <Tag color="processing">審批中</Tag>;
  }
  if (status === "deleted") {
    return <Tag color="red">已刪除</Tag>;
  }
  if (status === "archived") {
    return <Tag>已下線</Tag>;
  }
  return <Tag color="gold">編輯中</Tag>;
}

function renderImportStatus(status?: string | null) {
  if (status === "ready") {
    return <Tag color="green">就緒</Tag>;
  }
  if (status === "failed") {
    return <Tag color="red">失敗</Tag>;
  }
  if (status === "processing") {
    return <Tag color="processing">處理中</Tag>;
  }
  return <Tag>未導入</Tag>;
}

function resolveDefaultIndoorBuildingId(
  items: AdminIndoorBuildingItem[],
  initialBuildingId?: number | null,
  currentBuildingId?: number | null,
) {
  if (
    initialBuildingId &&
    items.some((item) => item.id === initialBuildingId)
  ) {
    return initialBuildingId;
  }
  if (
    currentBuildingId &&
    items.some((item) => item.id === currentBuildingId)
  ) {
    return currentBuildingId;
  }
  return (
    items.find((item) => item.buildingCode === SHOWCASE_BUILDING_CODE)?.id ||
    items[0]?.id ||
    null
  );
}

function resolveDefaultIndoorFloorId(
  floors: AdminIndoorFloorItem[],
  preferredFloorId?: number | null,
  buildingCode?: string | null,
) {
  if (
    preferredFloorId &&
    floors.some((floor) => floor.id === preferredFloorId)
  ) {
    return preferredFloorId;
  }
  if (buildingCode === SHOWCASE_BUILDING_CODE) {
    for (const code of SHOWCASE_FLOOR_CODES) {
      const match = floors.find((floor) => floor.floorCode === code);
      if (match) {
        return match.id;
      }
    }
  }
  return floors[0]?.id || null;
}

function redDot(color = "#ef4444") {
  return (
    <div
      style={{
        width: 16,
        height: 16,
        borderRadius: "50%",
        background: color,
        boxShadow: `0 0 0 5px ${color}22`,
      }}
    />
  );
}

function parseTagList(value?: string | null) {
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed)
      ? parsed.map((item) => String(item)).filter(Boolean)
      : [];
  } catch {
    return [];
  }
}

function stringifyTagList(tags: string[]) {
  return JSON.stringify(tags.filter(Boolean), null, 2);
}

function buildDefaultBehaviorProfile(index = 0): AdminIndoorBehaviorProfile {
  return {
    behaviorCode: `behavior-${index + 1}`,
    behaviorNameZh: `互動行為 ${index + 1}`,
    appearancePresetCode: "schedule_window",
    triggerTemplateCode: "tap",
    effectTemplateCode: "popup",
    appearanceRules: [
      {
        id: `appearance-${index + 1}`,
        category: "schedule_window",
        config: {},
      },
    ],
    triggerRules: [{ id: `trigger-${index + 1}`, category: "tap", config: {} }],
    effectRules: [{ id: `effect-${index + 1}`, category: "popup", config: {} }],
    rewardRuleIds: [],
    pathGraph: {
      points: [],
      durationMs: 2400,
      holdMs: 0,
      loop: false,
      easing: "linear",
    },
    overlayGeometry: undefined,
    inheritMode: "override",
    runtimeSupportLevel: "phase15_storage_only",
    sortOrder: index,
    status: "draft",
  };
}

function normalizeBehaviorProfiles(
  profiles?: AdminIndoorBehaviorProfile[] | null,
): AdminIndoorBehaviorProfile[] {
  return (profiles || [])
    .filter(
      (behavior): behavior is AdminIndoorBehaviorProfile =>
        !!behavior && typeof behavior === "object",
    )
    .map((behavior, index) => {
      const defaults = buildDefaultBehaviorProfile(index);
      return {
        ...defaults,
        ...behavior,
        appearanceRules: behavior?.appearanceRules || [],
        triggerRules: behavior?.triggerRules || [],
        effectRules: behavior?.effectRules || [],
        rewardRuleIds: Array.isArray(behavior?.rewardRuleIds)
          ? Array.from(
              new Set(
                behavior.rewardRuleIds
                  .map((id) => Number(id))
                  .filter((id) => Number.isFinite(id) && id > 0),
              ),
            )
          : [],
        pathGraph: behavior?.pathGraph
          ? {
              ...defaults.pathGraph,
              ...behavior.pathGraph,
              points: normalizePointCollection(behavior.pathGraph.points),
            }
          : defaults.pathGraph,
        overlayGeometry: normalizeOverlayGeometry(behavior?.overlayGeometry),
        inheritMode: behavior?.inheritMode || defaults.inheritMode,
        runtimeSupportLevel:
          behavior?.runtimeSupportLevel || defaults.runtimeSupportLevel,
        sortOrder: behavior?.sortOrder ?? index,
        status: behavior?.status || defaults.status,
      };
    });
}

function normalizePointCollection(
  points?: AdminIndoorNodePoint[] | null,
): AdminIndoorNodePoint[] {
  return (points || []).map((point, index) => ({
    x: point?.x ?? null,
    y: point?.y ?? null,
    order: point?.order ?? index,
  }));
}

function normalizeOverlayGeometry(
  overlayGeometry?: AdminIndoorOverlayGeometry | null,
): AdminIndoorOverlayGeometry | undefined {
  if (!overlayGeometry) {
    return undefined;
  }
  const points = normalizePointCollection(overlayGeometry.points);
  if (!points.length) {
    return undefined;
  }
  return {
    geometryType: overlayGeometry.geometryType || "point",
    points,
    properties: overlayGeometry.properties || undefined,
  };
}

function serializePathGraph(
  pathGraph?: AdminIndoorPathGraph | null,
): AdminIndoorPathGraph | undefined {
  if (!pathGraph) {
    return undefined;
  }
  const points = normalizePointCollection(pathGraph.points);
  if (!points.length) {
    return undefined;
  }
  return {
    ...pathGraph,
    points,
    durationMs: pathGraph.durationMs ?? 1600,
    holdMs: pathGraph.holdMs ?? 0,
    loop: Boolean(pathGraph.loop),
    easing: pathGraph.easing || "ease-in-out",
  };
}

function buildSvgPointString(points?: AdminIndoorNodePoint[] | null) {
  return normalizePointCollection(points)
    .map(
      (point) =>
        `${Number((point.x ?? 0) * 100).toFixed(3)},${Number((point.y ?? 0) * 100).toFixed(3)}`,
    )
    .join(" ");
}

function renderOverlayGeometry(
  geometry: AdminIndoorOverlayGeometry | undefined,
  color: string,
  keyPrefix: string,
  fillOpacity = 0.14,
) {
  const normalized = normalizeOverlayGeometry(geometry);
  const points = normalizePointCollection(normalized?.points);
  if (!normalized || !points.length) {
    return null;
  }
  const pointString = buildSvgPointString(points);
  const circles = points.map((point, index) => (
    <circle
      key={`${keyPrefix}-point-${index}`}
      cx={(point.x ?? 0) * 100}
      cy={(point.y ?? 0) * 100}
      r={1.05}
      fill={color}
      stroke="#ffffff"
      strokeWidth={0.35}
    />
  ));
  if (normalized.geometryType === "point") {
    return circles;
  }
  if (normalized.geometryType === "polyline") {
    return (
      <React.Fragment key={`${keyPrefix}-polyline`}>
        <polyline
          points={pointString}
          fill="none"
          stroke={color}
          strokeWidth={1.15}
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        {circles}
      </React.Fragment>
    );
  }
  return (
    <React.Fragment key={`${keyPrefix}-polygon`}>
      <polygon
        points={pointString}
        fill={color}
        fillOpacity={fillOpacity}
        stroke={color}
        strokeWidth={1.05}
        strokeLinejoin="round"
      />
      {circles}
    </React.Fragment>
  );
}

function renderPathGraph(
  points: AdminIndoorNodePoint[] | undefined,
  color: string,
  keyPrefix: string,
) {
  const normalizedPoints = normalizePointCollection(points);
  if (!normalizedPoints.length) {
    return null;
  }
  const pointString = buildSvgPointString(normalizedPoints);
  return (
    <React.Fragment key={`${keyPrefix}-path`}>
      <polyline
        points={pointString}
        fill="none"
        stroke={color}
        strokeWidth={1.05}
        strokeLinecap="round"
        strokeLinejoin="round"
        strokeDasharray="2.4 1.8"
      />
      {normalizedPoints.map((point, index) => (
        <circle
          key={`${keyPrefix}-path-point-${index}`}
          cx={(point.x ?? 0) * 100}
          cy={(point.y ?? 0) * 100}
          r={0.95}
          fill="#ffffff"
          stroke={color}
          strokeWidth={0.45}
        />
      ))}
    </React.Fragment>
  );
}

function floorMarkerCount(floor?: AdminIndoorFloorItem | null) {
  if (!floor) {
    return 0;
  }
  return floor.nodes?.length ?? floor.markers?.length ?? floor.markerCount ?? 0;
}

function markerDraftStorageKey(floorId: number) {
  return `trip-of-macau:indoor-marker-drafts:${floorId}`;
}

function loadMarkerDraftsFromStorage(floorId: number): MarkerDraftRecord[] {
  if (typeof window === "undefined") {
    return [];
  }
  try {
    const raw = window.localStorage.getItem(markerDraftStorageKey(floorId));
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) {
      return [];
    }
    return parsed.filter((item) => item && typeof item === "object");
  } catch {
    return [];
  }
}

function saveMarkerDraftsToStorage(
  floorId: number,
  drafts: MarkerDraftRecord[],
) {
  if (typeof window === "undefined") {
    return;
  }
  window.localStorage.setItem(
    markerDraftStorageKey(floorId),
    JSON.stringify(drafts),
  );
}

function buildMarkerDraftName(
  values?: Partial<AdminIndoorMarkerPayload> | null,
) {
  if (!values) {
    return "未命名草稿";
  }
  return (
    values.nodeNameZht ||
    values.nodeNameZh ||
    values.nodeNameEn ||
    values.markerCode ||
    "未命名草稿"
  );
}

function hasMeaningfulMarkerDraft(
  values?: Partial<AdminIndoorMarkerPayload> | null,
) {
  if (!values) {
    return false;
  }
  return Boolean(
    values.markerCode ||
    values.nodeNameZh ||
    values.nodeNameZht ||
    values.nodeNameEn ||
    values.nodeNamePt ||
    values.descriptionZh ||
    values.relatedPoiId ||
    values.iconAssetId ||
    values.animationAssetId ||
    values.presentationMode ||
    values.overlayType ||
    values.overlayGeometry?.points?.length ||
    values.behaviors?.length ||
    values.linkedEntityType ||
    values.linkedEntityId,
  );
}

const UploadSelector: React.FC<{
  title: string;
  description: string;
  accept?: string;
  file: File | null;
  uid: string;
  onSelect: (file: File | null) => void;
}> = ({ title, description, accept, file, uid, onSelect }) => (
  <Dragger
    accept={accept}
    beforeUpload={(nextFile) => {
      onSelect(nextFile as unknown as File);
      return false;
    }}
    fileList={buildUploadFile(file, uid)}
    maxCount={1}
    onRemove={() => {
      onSelect(null);
      return true;
    }}
    style={{ background: "#fafcff" }}
  >
    <Space direction="vertical" size={8}>
      <InboxOutlined style={{ fontSize: 28, color: "#5b66b5" }} />
      <Text strong>{title}</Text>
      <Text type="secondary">{description}</Text>
    </Space>
  </Dragger>
);

interface MapTileManagementProps {
  embedded?: boolean;
  initialBuildingId?: number | null;
  initialFloorId?: number | null;
  onSelectionChange?: (selection: {
    buildingId: number | null;
    floorId: number | null;
  }) => void;
}

const FloorCanvas: React.FC<{
  floor: AdminIndoorFloorItem | null;
  loading?: boolean;
  editingMarkerId: number | null;
  nodes: AdminIndoorNodeItem[];
  draftX?: number;
  draftY?: number;
  pickMode?: "marker" | "path" | "overlay";
  currentPathPoints?: AdminIndoorNodePoint[];
  currentOverlayGeometry?: AdminIndoorOverlayGeometry;
  onPick: (event: React.MouseEvent<HTMLDivElement>) => void;
  onSelectMarker: (marker: AdminIndoorMarkerItem) => void;
}> = ({
  floor,
  loading = false,
  editingMarkerId,
  nodes,
  draftX,
  draftY,
  pickMode = "marker",
  currentPathPoints,
  currentOverlayGeometry,
  onPick,
  onSelectMarker,
}) => {
  const manifest = useMemo(
    () => parseTileManifest(floor?.tileManifestJson),
    [floor?.tileManifestJson],
  );
  const tiles = useMemo(() => {
    if (!manifest) {
      return [];
    }
    return manifest.tiles.filter(
      (tile) => tile.z === manifest.defaultLevel && !!tile.url,
    );
  }, [manifest]);
  const aspectRatio = useMemo(() => {
    if (floor?.imageWidthPx && floor?.imageHeightPx) {
      return `${floor.imageWidthPx} / ${floor.imageHeightPx}`;
    }
    if (manifest) {
      return `${manifest.gridCols} / ${manifest.gridRows}`;
    }
    return "4 / 3";
  }, [floor?.imageHeightPx, floor?.imageWidthPx, manifest]);
  const previewImageUrl = floor?.tilePreviewImageUrl || floor?.floorPlanUrl;
  const anchoredNodes = useMemo(
    () =>
      nodes.filter((node) => node.relativeX != null && node.relativeY != null),
    [nodes],
  );
  const overlayNodes = useMemo(
    () =>
      nodes.filter(
        (node) =>
          node.id !== editingMarkerId &&
          node.presentationMode !== "marker" &&
          normalizePointCollection(node.overlayGeometry?.points).length > 0,
      ),
    [editingMarkerId, nodes],
  );

  if (!floor) {
    return <Empty description="請先選擇樓層" />;
  }

  if (loading) {
    return (
      <div
        style={{
          position: "relative",
          width: "100%",
          aspectRatio,
          borderRadius: 16,
          overflow: "hidden",
          border: "1px solid #e6ebf5",
          background: "linear-gradient(135deg, #f5f8ff 0%, #eef3fb 100%)",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Space direction="vertical" size={8} align="center">
          <Text strong>樓層資料載入中</Text>
          <Text type="secondary">
            正在同步瓦片、標記與縮略圖，完成後才會顯示畫布。
          </Text>
        </Space>
      </div>
    );
  }

  return (
    <div
      onClick={onPick}
      style={{
        position: "relative",
        width: "100%",
        aspectRatio,
        borderRadius: 16,
        overflow: "hidden",
        border: "1px solid #e6ebf5",
        background: "#f6f8fc",
        cursor: "crosshair",
      }}
    >
      {previewImageUrl ? (
        <img
          src={previewImageUrl}
          alt={pickLocalizedName(floor)}
          style={{
            position: "absolute",
            inset: 0,
            width: "100%",
            height: "100%",
            objectFit: "fill",
          }}
        />
      ) : tiles.length ? (
        tiles.map((tile) => (
          <img
            key={`${tile.z}-${tile.x}-${tile.y}`}
            src={tile.url}
            alt={`${tile.z}-${tile.x}-${tile.y}`}
            style={{
              position: "absolute",
              left: `${(tile.x / manifest!.gridCols) * 100}%`,
              top: `${(tile.y / manifest!.gridRows) * 100}%`,
              width: `${100 / manifest!.gridCols}%`,
              height: `${100 / manifest!.gridRows}%`,
            }}
          />
        ))
      ) : (
        <div
          style={{
            position: "absolute",
            inset: 0,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            background:
              "repeating-linear-gradient(45deg, #f4f6fb, #f4f6fb 12px, #eef2f8 12px, #eef2f8 24px)",
          }}
        >
          <Text type="secondary">此樓層尚未導入平面圖或瓦片預覽</Text>
        </div>
      )}

      <svg
        viewBox="0 0 100 100"
        preserveAspectRatio="none"
        style={{
          position: "absolute",
          inset: 0,
          width: "100%",
          height: "100%",
          pointerEvents: "none",
        }}
      >
        {overlayNodes.map((node) =>
          renderOverlayGeometry(
            node.overlayGeometry || undefined,
            editingMarkerId === node.id ? "#1d4ed8" : "#22c55e",
            `node-overlay-${node.id}`,
            editingMarkerId === node.id ? 0.22 : 0.12,
          ),
        )}
        {renderOverlayGeometry(
          currentOverlayGeometry,
          "#2563eb",
          "draft-overlay",
          0.2,
        )}
        {renderPathGraph(currentPathPoints, "#2563eb", "draft-path")}
      </svg>

      {anchoredNodes.map((marker) => (
        <button
          key={marker.id}
          type="button"
          onClick={(event) => {
            event.stopPropagation();
            onSelectMarker(marker);
          }}
          style={{
            position: "absolute",
            left: `${(marker.relativeX || 0) * 100}%`,
            top: `${(marker.relativeY || 0) * 100}%`,
            transform: "translate(-50%, -50%)",
            border:
              editingMarkerId === marker.id ? "2px solid #1d4ed8" : "none",
            borderRadius: 999,
            background: "transparent",
            padding: 0,
            cursor: "pointer",
          }}
        >
          {marker.iconUrl && marker.presentationMode !== "overlay" ? (
            <img
              src={marker.iconUrl}
              alt={pickLocalizedName(marker)}
              style={{ width: 28, height: 28, objectFit: "contain" }}
            />
          ) : (
            redDot(
              marker.presentationMode === "overlay" ? "#22c55e" : "#ef4444",
            )
          )}
        </button>
      ))}

      {draftX != null && draftY != null ? (
        <div
          style={{
            position: "absolute",
            left: `${draftX * 100}%`,
            top: `${draftY * 100}%`,
            transform: "translate(-50%, -50%)",
            pointerEvents: "none",
          }}
        >
          {redDot("#2563eb")}
        </div>
      ) : null}

      <div
        style={{
          position: "absolute",
          left: 12,
          top: 12,
          pointerEvents: "none",
        }}
      >
        <Tag
          color={
            pickMode === "marker"
              ? "blue"
              : pickMode === "path"
                ? "purple"
                : "green"
          }
        >
          {pickMode === "marker"
            ? "點位取點"
            : pickMode === "path"
              ? "路徑取點"
              : "疊加物取點"}
        </Tag>
      </div>
    </div>
  );
};

const MapTileManagement: React.FC<MapTileManagementProps> = ({
  embedded = false,
  initialBuildingId = null,
  initialFloorId = null,
  onSelectionChange,
}) => {
  const navigate = useNavigate();
  const { message: messageApi } = AntdApp.useApp();
  const [floorForm] = Form.useForm();
  const [markerForm] = Form.useForm<AdminIndoorMarkerPayload>();
  const [translationSettings, setTranslationSettings] =
    useState<AdminTranslationSettings>();
  const [buildings, setBuildings] = useState<AdminIndoorBuildingItem[]>([]);
  const [pois, setPois] = useState<AdminPoiListItem[]>([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState<number | null>(
    null,
  );
  const [selectedBuilding, setSelectedBuilding] =
    useState<AdminIndoorBuildingDetail | null>(null);
  const [selectedFloorId, setSelectedFloorId] = useState<number | null>(null);
  const [selectedFloor, setSelectedFloor] =
    useState<AdminIndoorFloorItem | null>(null);
  const [loadingBuildings, setLoadingBuildings] = useState(false);
  const [loadingFloor, setLoadingFloor] = useState(false);
  const [floorSwitching, setFloorSwitching] = useState(false);
  const [runningAction, setRunningAction] = useState<string | null>(null);
  const [tileSizePx, setTileSizePx] = useState<number>(512);
  const [zipFile, setZipFile] = useState<File | null>(null);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [tilePreview, setTilePreview] = useState<AdminIndoorTilePreview | null>(
    null,
  );
  const [csvPreview, setCsvPreview] =
    useState<AdminIndoorMarkerCsvPreview | null>(null);
  const [editingMarkerId, setEditingMarkerId] = useState<number | null>(null);
  const [placementArmed, setPlacementArmed] = useState(false);
  const [pickMode, setPickMode] = useState<"marker" | "path" | "overlay">(
    "marker",
  );
  const [activeBehaviorIndex, setActiveBehaviorIndex] = useState(0);
  const [pendingBehaviorIndex, setPendingBehaviorIndex] = useState<
    number | null
  >(null);
  const [behaviorProfilesDraft, setBehaviorProfilesDraft] = useState<
    AdminIndoorBehaviorProfile[]
  >([]);
  const [storylines, setStorylines] = useState<AdminStorylineListItem[]>([]);
  const [chapters, setChapters] = useState<AdminStoryChapterItem[]>([]);
  const [activities, setActivities] = useState<
    Array<{
      id: number;
      code?: string;
      title?: string;
      titleZh?: string;
      titleZht?: string;
    }>
  >([]);
  const [collectibles, setCollectibles] = useState<
    Array<{
      id: number;
      collectibleCode: string;
      nameZh: string;
      nameZht?: string;
      nameEn?: string;
    }>
  >([]);
  const [badges, setBadges] = useState<
    Array<{
      id: number;
      badgeCode: string;
      nameZh: string;
      nameZht?: string;
      nameEn?: string;
    }>
  >([]);
  const [rewardRuleOptions, setRewardRuleOptions] = useState<
    Array<{ value: number; label: string }>
  >([]);
  const [rewardRuleLoading, setRewardRuleLoading] = useState(false);
  const [markerDrafts, setMarkerDrafts] = useState<MarkerDraftRecord[]>([]);
  const [showTagJsonEditor, setShowTagJsonEditor] = useState(false);
  const [ruleWorkbenchOpen, setRuleWorkbenchOpen] = useState(false);
  const buildingRequestRef = React.useRef(0);
  const floorRequestRef = React.useRef(0);
  const emittedSelectionRef = React.useRef<{
    buildingId: number | null;
    floorId: number | null;
  }>({
    buildingId: null,
    floorId: null,
  });
  const lastExternalSelectionRef = React.useRef<{
    buildingId: number | null;
    floorId: number | null;
  }>({
    buildingId: null,
    floorId: null,
  });

  const activeBuilding = useMemo(
    () =>
      selectedBuilding && selectedBuilding.id === selectedBuildingId
        ? selectedBuilding
        : null,
    [selectedBuilding, selectedBuildingId],
  );
  const activeFloor = useMemo(
    () =>
      selectedFloor && selectedFloor.id === selectedFloorId
        ? selectedFloor
        : null,
    [selectedFloor, selectedFloorId],
  );
  const activeFloorNodes = useMemo(
    () => activeFloor?.nodes || activeFloor?.markers || [],
    [activeFloor?.markers, activeFloor?.nodes],
  );

  const buildingOptions = useMemo(
    () =>
      buildings.map((building) => ({
        label: `${pickLocalizedName(building)} (#${building.id})`,
        value: building.id,
      })),
    [buildings],
  );
  const showcaseBuilding = useMemo(
    () =>
      buildings.find(
        (building) => building.buildingCode === SHOWCASE_BUILDING_CODE,
      ) || null,
    [buildings],
  );

  const floorOptions = useMemo(
    () =>
      (activeBuilding?.floors || []).map((floor) => ({
        label: `${pickLocalizedName(floor)} (${floor.floorCode || floor.floorNumber})`,
        value: floor.id,
      })),
    [activeBuilding?.floors],
  );

  const poiOptions = useMemo(
    () =>
      pois.map((poi) => ({
        label: `${pickLocalizedName(poi)} (#${poi.poiId})`,
        value: poi.poiId,
      })),
    [pois],
  );
  const filteredPoiOptions = useMemo(() => {
    const currentCityId = activeBuilding?.cityId;
    return pois
      .filter((poi) => !currentCityId || poi.cityId === currentCityId)
      .map((poi) => ({
        label: `${pickLocalizedName(poi)} (#${poi.poiId})`,
        value: poi.poiId,
      }));
  }, [activeBuilding?.cityId, pois]);

  const linkedEntityType = Form.useWatch("linkedEntityType", markerForm) as
    | string
    | undefined;
  const presentationMode = Form.useWatch("presentationMode", markerForm) as
    | string
    | undefined;
  const markerTagJson = Form.useWatch("tagsJson", markerForm) as
    | string
    | undefined;
  const draftX = Form.useWatch("relativeX", markerForm) as number | undefined;
  const draftY = Form.useWatch("relativeY", markerForm) as number | undefined;
  const selectedIconAssetId = Form.useWatch("iconAssetId", markerForm) as
    | number
    | undefined;
  const markerTags = useMemo(
    () => parseTagList(markerTagJson),
    [markerTagJson],
  );
  const markerDraftSnapshotRef = React.useRef<
    Partial<AdminIndoorMarkerPayload> | undefined
  >(undefined);
  const markerDraftTimerRef = React.useRef<number | null>(null);
  const behaviorProfiles = useMemo(
    () => normalizeBehaviorProfiles(behaviorProfilesDraft),
    [behaviorProfilesDraft],
  );
  const activeBehaviorProfile = behaviorProfiles[activeBehaviorIndex];
  const currentPathPoints = useMemo(
    () => normalizePointCollection(activeBehaviorProfile?.pathGraph?.points),
    [activeBehaviorProfile?.pathGraph?.points],
  );
  const currentOverlayPreview = useMemo(
    () => normalizeOverlayGeometry(activeBehaviorProfile?.overlayGeometry),
    [activeBehaviorProfile?.overlayGeometry],
  );
  const activeBehaviorBasePath = useMemo<Array<string | number>>(
    () => ["behaviors", activeBehaviorIndex],
    [activeBehaviorIndex],
  );
  const activeBehaviorEditorKey = useMemo(
    () => `behavior-${activeBehaviorIndex}-${behaviorProfiles.length}`,
    [activeBehaviorIndex, behaviorProfiles.length],
  );
  const syncBehaviorProfilesDraft = React.useCallback(
    (profiles?: AdminIndoorBehaviorProfile[] | null) => {
      const nextProfiles = normalizeBehaviorProfiles(profiles);
      setBehaviorProfilesDraft((currentProfiles) => {
        if (JSON.stringify(currentProfiles) === JSON.stringify(nextProfiles)) {
          return currentProfiles;
        }
        return nextProfiles;
      });
    },
    [],
  );
  const clearPendingAutoDraftSave = React.useCallback(() => {
    if (markerDraftTimerRef.current != null) {
      window.clearTimeout(markerDraftTimerRef.current);
      markerDraftTimerRef.current = null;
    }
  }, []);
  const scheduleAutoDraftSave = React.useCallback(
    (values?: Partial<AdminIndoorMarkerPayload>) => {
      clearPendingAutoDraftSave();
      markerDraftSnapshotRef.current = values;
      if (!activeFloor?.id || !values || !hasMeaningfulMarkerDraft(values)) {
        return;
      }
      const floorId = activeFloor.id;
      const editingId = editingMarkerId;
      const snapshot = JSON.parse(
        JSON.stringify(values),
      ) as Partial<AdminIndoorMarkerPayload>;
      markerDraftTimerRef.current = window.setTimeout(() => {
        const currentDrafts = loadMarkerDraftsFromStorage(floorId).filter(
          (draft) => !draft.isAuto,
        );
        const autoDraft: MarkerDraftRecord = {
          draftId: "auto",
          draftName: `${buildMarkerDraftName(snapshot)}（自動暫存）`,
          savedAt: new Date().toISOString(),
          isAuto: true,
          values: snapshot,
          editingMarkerId: editingId,
        };
        const nextDrafts = [autoDraft, ...currentDrafts];
        saveMarkerDraftsToStorage(floorId, nextDrafts);
        setMarkerDrafts(nextDrafts);
        markerDraftTimerRef.current = null;
      }, 900);
    },
    [activeFloor?.id, clearPendingAutoDraftSave, editingMarkerId],
  );
  const linkedEntityIdOptions = useMemo<LinkedEntityOption[]>(() => {
    switch (linkedEntityType) {
      case "task":
      case "activity":
        return activities.map((item) => ({
          value: item.id,
          label: `${item.titleZht || item.titleZh || item.title || item.code || `活動 #${item.id}`} (#${item.id})`,
        }));
      case "collectible":
        return collectibles.map((item) => ({
          value: item.id,
          label: `${item.nameZht || item.nameZh || item.nameEn || item.collectibleCode} (#${item.id})`,
        }));
      case "badge":
        return badges.map((item) => ({
          value: item.id,
          label: `${item.nameZht || item.nameZh || item.nameEn || item.badgeCode} (#${item.id})`,
        }));
      case "chapter":
        return chapters.map((item) => ({
          value: item.id,
          label: `${item.titleZht || item.titleZh || item.titleEn || `章節 #${item.id}`} (#${item.id})`,
        }));
      case "event":
        return activities.map((item) => ({
          value: item.id,
          label: `${item.titleZht || item.titleZh || item.title || item.code || `事件 #${item.id}`} (#${item.id})`,
        }));
      default:
        return [];
    }
  }, [activities, badges, chapters, collectibles, linkedEntityType]);

  const emitSelection = React.useCallback(
    (nextSelection: { buildingId: number | null; floorId: number | null }) => {
      if (
        emittedSelectionRef.current.buildingId === nextSelection.buildingId &&
        emittedSelectionRef.current.floorId === nextSelection.floorId
      ) {
        return;
      }
      emittedSelectionRef.current = nextSelection;
      onSelectionChange?.(nextSelection);
    },
    [onSelectionChange],
  );

  const resetMarkerEditor = React.useCallback(
    (floor?: AdminIndoorFloorItem | null) => {
      const nextValues = {
        markerCode: undefined,
        nodeType: "custom",
        presentationMode: "marker",
        overlayType: undefined,
        nodeNameZh: "",
        nodeNameEn: "",
        nodeNameZht: "",
        nodeNamePt: "",
        descriptionZh: "",
        descriptionEn: "",
        descriptionZht: "",
        descriptionPt: "",
        relativeX: 0.5,
        relativeY: 0.5,
        relatedPoiId: undefined,
        iconAssetId: undefined,
        animationAssetId: undefined,
        linkedEntityType: "",
        linkedEntityId: undefined,
        tagsJson: stringifyTagList(defaultMarkerTags),
        popupConfigJson: defaultMarkerPopupConfig,
        displayConfigJson: defaultMarkerDisplayConfig,
        overlayGeometry: undefined,
        inheritLinkedEntityRules: false,
        runtimeSupportLevel: "phase15_storage_only",
        metadataJson: "{}",
        behaviors: [buildDefaultBehaviorProfile(0)],
        sortOrder: (floor?.nodes?.length || floor?.markers?.length || 0) + 1,
        status: "draft",
      };
      markerForm.setFieldsValue(nextValues);
      syncBehaviorProfilesDraft(nextValues.behaviors);
      clearPendingAutoDraftSave();
      markerDraftSnapshotRef.current = undefined;
      setEditingMarkerId(null);
      setPlacementArmed(false);
      setPickMode("marker");
      setPendingBehaviorIndex(null);
      setActiveBehaviorIndex(0);
    },
    [clearPendingAutoDraftSave, markerForm, syncBehaviorProfilesDraft],
  );

  const loadNodeIntoEditor = React.useCallback(
    (record: AdminIndoorNodeItem) => {
      const nextValues = {
        markerCode: record.markerCode,
        nodeType: record.nodeType || "custom",
        presentationMode: record.presentationMode || "marker",
        overlayType: record.overlayType || undefined,
        nodeNameZh: record.nodeNameZh,
        nodeNameEn: record.nodeNameEn,
        nodeNameZht: record.nodeNameZht,
        nodeNamePt: record.nodeNamePt,
        descriptionZh: record.descriptionZh,
        descriptionEn: record.descriptionEn,
        descriptionZht: record.descriptionZht,
        descriptionPt: record.descriptionPt,
        relativeX: record.relativeX,
        relativeY: record.relativeY,
        relatedPoiId: record.relatedPoiId,
        iconAssetId: record.iconAssetId,
        animationAssetId: record.animationAssetId,
        linkedEntityType: record.linkedEntityType || "",
        linkedEntityId: record.linkedEntityId,
        tagsJson:
          record.tagsJson || stringifyTagList(record.tags || defaultMarkerTags),
        popupConfigJson: record.popupConfigJson || defaultMarkerPopupConfig,
        displayConfigJson:
          record.displayConfigJson || defaultMarkerDisplayConfig,
        overlayGeometry: normalizeOverlayGeometry(record.overlayGeometry),
        inheritLinkedEntityRules: record.inheritLinkedEntityRules,
        runtimeSupportLevel:
          record.runtimeSupportLevel || "phase15_storage_only",
        metadataJson: record.metadataJson || "{}",
        behaviors: normalizeBehaviorProfiles(record.behaviors),
        sortOrder: record.sortOrder,
        status: record.status || "draft",
      };
      markerForm.setFieldsValue(nextValues);
      syncBehaviorProfilesDraft(nextValues.behaviors);
      clearPendingAutoDraftSave();
      markerDraftSnapshotRef.current = nextValues;
      setEditingMarkerId(record.id);
      setPlacementArmed(false);
      setPickMode("marker");
      setPendingBehaviorIndex(null);
      setActiveBehaviorIndex(0);
    },
    [clearPendingAutoDraftSave, markerForm, syncBehaviorProfilesDraft],
  );

  const syncFloorForm = React.useCallback(
    (floor: AdminIndoorFloorItem) => {
      floorForm.setFieldsValue({
        floorNumber: floor.floorNumber,
        floorCode: floor.floorCode,
        areaSqm: floor.areaSqm,
        zoomMin: floor.zoomMin,
        defaultZoom: floor.defaultZoom,
        zoomMax: floor.zoomMax,
        status: floor.status || "unpublished",
      });
      resetMarkerEditor(floor);
    },
    [floorForm, resetMarkerEditor],
  );

  const loadBasics = React.useCallback(async () => {
    setRewardRuleLoading(true);
    try {
      const [
        translationResponse,
        poiResponse,
        storylineResponse,
        activityResponse,
        collectibleResponse,
        badgeResponse,
        rewardRuleResponse,
      ] = await Promise.all([
        getAdminTranslationSettings(),
        getAdminPois({ pageNum: 1, pageSize: 500 }),
        getAdminStorylines({ pageNum: 1, pageSize: 200 }),
        getAdminActivities({ pageNum: 1, pageSize: 200 }),
        getCollectibles({ pageNum: 1, pageSize: 200 }),
        getBadges({ pageNum: 1, pageSize: 200 }),
        getAdminRewardRules({ pageNum: 1, pageSize: 500 }),
      ]);
      if (translationResponse.success && translationResponse.data) {
        setTranslationSettings(translationResponse.data);
      }
      if (poiResponse.success && poiResponse.data) {
        setPois(poiResponse.data.list || []);
      }
      const storylineItems =
        storylineResponse.success && storylineResponse.data
          ? storylineResponse.data.list || []
          : [];
      setStorylines(storylineItems);
      if (activityResponse.success && activityResponse.data) {
        setActivities(activityResponse.data.list || []);
      }
      if (collectibleResponse.success && collectibleResponse.data) {
        setCollectibles(collectibleResponse.data.list || []);
      }
      if (badgeResponse.success && badgeResponse.data) {
        setBadges(badgeResponse.data.list || []);
      }
      if (rewardRuleResponse.success && rewardRuleResponse.data) {
        setRewardRuleOptions(
          (rewardRuleResponse.data.list || []).map((rule) => ({
            value: rule.id,
            label: `${rule.nameZht || rule.nameZh || rule.code}${
              rule.summaryText ? ` · ${rule.summaryText}` : ""
            }`,
          })),
        );
      } else {
        setRewardRuleOptions([]);
      }
      if (storylineItems.length) {
        const chapterResponses = await Promise.all(
          storylineItems.slice(0, 20).map((item) =>
            getStorylineChapters(item.storylineId, {
              pageNum: 1,
              pageSize: 200,
            }),
          ),
        );
        setChapters(
          chapterResponses
            .filter((response) => response.success && response.data)
            .flatMap((response) => response.data?.list || []),
        );
      } else {
        setChapters([]);
      }
    } finally {
      setRewardRuleLoading(false);
    }
  }, []);

  const loadBuildings = React.useCallback(async () => {
    setLoadingBuildings(true);
    try {
      const response = await getIndoorBuildings({ pageNum: 1, pageSize: 200 });
      if (!response.success || !response.data) {
        throw new Error(response.message || "無法載入室內建築列表");
      }
      const items = response.data.list || [];
      setBuildings(items);
      setSelectedBuildingId((current) =>
        resolveDefaultIndoorBuildingId(items, initialBuildingId, current),
      );
    } catch (error) {
      messageApi.error(
        error instanceof Error ? error.message : "無法載入室內建築列表",
      );
    } finally {
      setLoadingBuildings(false);
    }
  }, [initialBuildingId]);

  const loadBuildingDetail = React.useCallback(
    async (buildingId: number, preferredFloorId?: number | null) => {
      const requestId = ++buildingRequestRef.current;
      const response = await getIndoorBuildingDetail(buildingId);
      if (requestId !== buildingRequestRef.current) {
        return;
      }
      if (!response.success || !response.data) {
        throw new Error(response.message || "無法載入建築詳情");
      }
      setSelectedBuilding(response.data);
      const floors = response.data.floors || [];
      const nextFloorId = resolveDefaultIndoorFloorId(
        floors,
        preferredFloorId,
        response.data.buildingCode,
      );
      setSelectedFloorId((current) =>
        current === nextFloorId ? current : nextFloorId,
      );
      if (!nextFloorId) {
        setSelectedFloor(null);
        setFloorSwitching(false);
      }
      emitSelection({ buildingId, floorId: nextFloorId });
    },
    [emitSelection],
  );

  const loadFloorDetail = React.useCallback(
    async (floorId: number) => {
      const requestId = ++floorRequestRef.current;
      setLoadingFloor(true);
      try {
        const [response, nodeResponse] = await Promise.all([
          getIndoorFloorDetail(floorId),
          getIndoorNodes(floorId),
        ]);
        if (requestId !== floorRequestRef.current) {
          return;
        }
        if (!response.success || !response.data) {
          throw new Error(response.message || "無法載入樓層詳情");
        }
        const mergedFloor: AdminIndoorFloorItem = {
          ...response.data,
          nodes:
            nodeResponse.success && nodeResponse.data ? nodeResponse.data : [],
          markers:
            nodeResponse.success && nodeResponse.data
              ? nodeResponse.data
              : response.data.markers,
          markerCount:
            nodeResponse.success && nodeResponse.data
              ? nodeResponse.data.length
              : response.data.markerCount,
        };
        setSelectedFloor(mergedFloor);
        syncFloorForm(mergedFloor);
        setTilePreview(null);
        setCsvPreview(null);
        setFloorSwitching(false);
      } catch (error) {
        if (requestId !== floorRequestRef.current) {
          return;
        }
        setFloorSwitching(false);
        messageApi.error(
          error instanceof Error ? error.message : "無法載入樓層詳情",
        );
      } finally {
        if (requestId === floorRequestRef.current) {
          setLoadingFloor(false);
        }
      }
    },
    [syncFloorForm],
  );

  const refreshContext = React.useCallback(async () => {
    if (selectedBuildingId) {
      await loadBuildingDetail(selectedBuildingId, selectedFloorId);
    }
  }, [loadBuildingDetail, selectedBuildingId, selectedFloorId]);

  const handleBuildingChange = React.useCallback(
    (buildingId: number) => {
      if (buildingId === selectedBuildingId) {
        return;
      }
      setFloorSwitching(true);
      setSelectedBuildingId(buildingId);
      setSelectedFloorId(null);
      emitSelection({ buildingId, floorId: null });
    },
    [emitSelection, selectedBuildingId],
  );

  const handleFloorChange = React.useCallback(
    (floorId: number) => {
      if (floorId === selectedFloorId) {
        return;
      }
      setFloorSwitching(true);
      setSelectedFloorId(floorId);
      emitSelection({ buildingId: selectedBuildingId, floorId });
    },
    [emitSelection, selectedBuildingId, selectedFloorId],
  );

  useEffect(() => {
    void Promise.all([loadBasics(), loadBuildings()]);
  }, [loadBasics, loadBuildings]);

  useEffect(() => {
    if (!selectedBuildingId) {
      setSelectedBuilding(null);
      setSelectedFloorId(null);
      setSelectedFloor(null);
      setFloorSwitching(false);
      return;
    }
    const preferredFloorId =
      initialFloorId ??
      lastExternalSelectionRef.current.floorId ??
      selectedFloorId;
    void loadBuildingDetail(selectedBuildingId, preferredFloorId);
  }, [initialFloorId, loadBuildingDetail, selectedBuildingId, selectedFloorId]);

  useEffect(() => {
    if (!selectedFloorId) {
      if (!selectedBuildingId) {
        setSelectedFloor(null);
        resetMarkerEditor(null);
        setFloorSwitching(false);
      }
      return;
    }
    void loadFloorDetail(selectedFloorId);
  }, [loadFloorDetail, resetMarkerEditor, selectedBuildingId, selectedFloorId]);

  useEffect(() => {
    const nextSelection = {
      buildingId: initialBuildingId ?? null,
      floorId: initialFloorId ?? null,
    };
    if (
      lastExternalSelectionRef.current.buildingId ===
        nextSelection.buildingId &&
      lastExternalSelectionRef.current.floorId === nextSelection.floorId
    ) {
      return;
    }
    lastExternalSelectionRef.current = nextSelection;
    if (
      nextSelection.buildingId &&
      nextSelection.buildingId !== selectedBuildingId &&
      buildings.some((item) => item.id === nextSelection.buildingId)
    ) {
      setFloorSwitching(true);
      setSelectedBuildingId(nextSelection.buildingId);
      return;
    }
    if (
      nextSelection.floorId &&
      nextSelection.floorId !== selectedFloorId &&
      activeBuilding?.floors?.some(
        (floor) => floor.id === nextSelection.floorId,
      )
    ) {
      setFloorSwitching(true);
      setSelectedFloorId(nextSelection.floorId);
    }
  }, [
    activeBuilding?.floors,
    buildings,
    initialBuildingId,
    initialFloorId,
    selectedBuildingId,
    selectedFloorId,
  ]);

  useEffect(() => {
    if (!activeFloor?.id) {
      clearPendingAutoDraftSave();
      markerDraftSnapshotRef.current = undefined;
      setMarkerDrafts([]);
      return;
    }
    setMarkerDrafts(loadMarkerDraftsFromStorage(activeFloor.id));
  }, [activeFloor?.id, clearPendingAutoDraftSave]);

  useEffect(() => {
    if (pendingBehaviorIndex != null) {
      if (behaviorProfiles.length > pendingBehaviorIndex) {
        setActiveBehaviorIndex(pendingBehaviorIndex);
        setPendingBehaviorIndex(null);
      }
      return;
    }
    if (!behaviorProfiles.length) {
      if (activeBehaviorIndex !== 0) {
        setActiveBehaviorIndex(0);
      }
      return;
    }
    if (activeBehaviorIndex > behaviorProfiles.length - 1) {
      setActiveBehaviorIndex(behaviorProfiles.length - 1);
    }
  }, [activeBehaviorIndex, behaviorProfiles.length, pendingBehaviorIndex]);

  useEffect(() => {
    return () => {
      clearPendingAutoDraftSave();
    };
  }, [clearPendingAutoDraftSave]);

  const addBehaviorProfile = React.useCallback(() => {
    const currentProfiles = normalizeBehaviorProfiles(behaviorProfiles);
    const nextBehaviorIndex = currentProfiles.length;
    const nextProfiles = [
      ...currentProfiles,
      buildDefaultBehaviorProfile(nextBehaviorIndex),
    ];
    markerForm.setFieldsValue({ behaviors: nextProfiles });
    syncBehaviorProfilesDraft(nextProfiles);
    setPendingBehaviorIndex(nextBehaviorIndex);
    messageApi.success(`已新增互動行為 ${nextBehaviorIndex + 1}`);
  }, [behaviorProfiles, markerForm, syncBehaviorProfilesDraft]);

  const removeBehaviorProfile = React.useCallback(
    (index: number) => {
      const currentProfiles = normalizeBehaviorProfiles(behaviorProfiles);
      const nextProfiles = currentProfiles
        .filter((_, profileIndex) => profileIndex !== index)
        .map((profile, profileIndex) => ({
          ...profile,
          sortOrder: profileIndex,
        }));
      markerForm.setFieldValue("behaviors", nextProfiles);
      syncBehaviorProfilesDraft(nextProfiles);
      setActiveBehaviorIndex(
        nextProfiles.length
          ? Math.max(0, Math.min(index, nextProfiles.length - 1))
          : 0,
      );
    },
    [behaviorProfiles, markerForm, syncBehaviorProfilesDraft],
  );

  const saveCurrentMarkerDraft = React.useCallback(
    (manual: boolean) => {
      if (!activeFloor?.id) {
        return;
      }
      const values =
        markerForm.getFieldsValue() as Partial<AdminIndoorMarkerPayload>;
      if (!hasMeaningfulMarkerDraft(values)) {
        messageApi.info("目前沒有可保存的標記草稿內容");
        return;
      }
      const currentDrafts = loadMarkerDraftsFromStorage(activeFloor.id).filter(
        (draft) => manual || !draft.isAuto,
      );
      const draft: MarkerDraftRecord = {
        draftId: manual ? `manual-${Date.now()}` : "auto",
        draftName: manual
          ? buildMarkerDraftName(values)
          : `${buildMarkerDraftName(values)}（自動暫存）`,
        savedAt: new Date().toISOString(),
        isAuto: !manual,
        values,
        editingMarkerId,
      };
      const nextDrafts = manual
        ? [draft, ...currentDrafts.filter((item) => item.draftId !== "auto")]
        : [draft, ...currentDrafts.filter((item) => item.draftId !== "auto")];
      saveMarkerDraftsToStorage(activeFloor.id, nextDrafts);
      setMarkerDrafts(nextDrafts);
      if (manual) {
        messageApi.success("標記草稿已保存到草稿區");
      }
    },
    [activeFloor?.id, editingMarkerId, markerForm],
  );

  const restoreMarkerDraft = React.useCallback(
    (draft: MarkerDraftRecord) => {
      const nextValues = {
        nodeType: "custom",
        tagsJson: stringifyTagList(defaultMarkerTags),
        popupConfigJson: defaultMarkerPopupConfig,
        displayConfigJson: defaultMarkerDisplayConfig,
        status: "unpublished",
        ...draft.values,
      };
      markerForm.setFieldsValue(nextValues);
      syncBehaviorProfilesDraft(
        (nextValues.behaviors as AdminIndoorBehaviorProfile[] | undefined) || [
          buildDefaultBehaviorProfile(0),
        ],
      );
      clearPendingAutoDraftSave();
      markerDraftSnapshotRef.current = nextValues;
      setEditingMarkerId(draft.editingMarkerId || null);
      setPlacementArmed(false);
      setPickMode("marker");
      setPendingBehaviorIndex(null);
      setActiveBehaviorIndex(0);
      messageApi.success(`已載入草稿：${draft.draftName}`);
    },
    [clearPendingAutoDraftSave, markerForm, syncBehaviorProfilesDraft],
  );

  const applyRuleWorkbenchValues = React.useCallback(
    (values: Partial<AdminIndoorMarkerPayload>) => {
      const mergedValues = {
        ...(markerForm.getFieldsValue() as Partial<AdminIndoorMarkerPayload>),
        ...values,
        overlayGeometry: normalizeOverlayGeometry(values.overlayGeometry),
        behaviors: normalizeBehaviorProfiles(values.behaviors),
      };
      markerForm.setFieldsValue(mergedValues);
      syncBehaviorProfilesDraft(mergedValues.behaviors);
      markerDraftSnapshotRef.current = mergedValues;
      setRuleWorkbenchOpen(false);
      setPickMode("marker");
      setPendingBehaviorIndex(null);
      setActiveBehaviorIndex(0);
      scheduleAutoDraftSave(mergedValues);
      messageApi.success("已套用互動規則工作台設定");
    },
    [markerForm, messageApi, scheduleAutoDraftSave, syncBehaviorProfilesDraft],
  );

  const deleteMarkerDraft = React.useCallback(
    (draftId: string) => {
      if (!activeFloor?.id) {
        return;
      }
      const nextDrafts = markerDrafts.filter(
        (draft) => draft.draftId !== draftId,
      );
      saveMarkerDraftsToStorage(activeFloor.id, nextDrafts);
      setMarkerDrafts(nextDrafts);
    },
    [activeFloor?.id, markerDrafts],
  );

  const markerColumns = [
    {
      title: "預覽",
      key: "preview",
      width: 84,
      render: (_: unknown, record: AdminIndoorMarkerItem) =>
        record.iconUrl && record.presentationMode !== "overlay" ? (
          <img
            src={record.iconUrl}
            alt={pickLocalizedName(record)}
            style={{ width: 28, height: 28, objectFit: "contain" }}
          />
        ) : (
          redDot(record.presentationMode === "overlay" ? "#22c55e" : "#ef4444")
        ),
    },
    {
      title: "標記名稱",
      key: "name",
      render: (_: unknown, record: AdminIndoorMarkerItem) => (
        <Space direction="vertical" size={0}>
          <Text strong>{pickLocalizedName(record)}</Text>
          <Text type="secondary">
            {record.markerCode || "未命名"} / {record.nodeType || "custom"}
          </Text>
        </Space>
      ),
    },
    {
      title: "座標",
      key: "coordinate",
      width: 140,
      render: (_: unknown, record: AdminIndoorMarkerItem) =>
        `${Number(record.relativeX || 0).toFixed(3)}, ${Number(record.relativeY || 0).toFixed(3)}`,
    },
    {
      title: "呈現",
      key: "presentation",
      width: 150,
      render: (_: unknown, record: AdminIndoorMarkerItem) => (
        <Space wrap size={[4, 4]}>
          <Tag
            color={
              record.presentationMode === "overlay"
                ? "green"
                : record.presentationMode === "hybrid"
                  ? "purple"
                  : "blue"
            }
          >
            {record.presentationMode || "marker"}
          </Tag>
          {record.overlayType ? <Tag>{record.overlayType}</Tag> : null}
        </Space>
      ),
    },
    {
      title: "規則",
      key: "behaviorCount",
      width: 110,
      render: (_: unknown, record: AdminIndoorMarkerItem) => {
        const count = record.behaviors?.length || 0;
        return count ? (
          <Tag color="cyan">{count} 組</Tag>
        ) : (
          <Text type="secondary">未配置</Text>
        );
      },
    },
    {
      title: "綁定",
      key: "binding",
      width: 180,
      render: (_: unknown, record: AdminIndoorMarkerItem) =>
        record.linkedEntityType
          ? `${record.linkedEntityType} #${record.linkedEntityId || "-"}`
          : "未綁定",
    },
    {
      title: "狀態",
      key: "status",
      width: 100,
      render: (_: unknown, record: AdminIndoorMarkerItem) =>
        renderStatus(record.status),
    },
    {
      title: "操作",
      key: "action",
      width: 160,
      render: (_: unknown, record: AdminIndoorMarkerItem) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => loadNodeIntoEditor(record)}
          >
            編輯
          </Button>
          <Button
            type="link"
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => {
              Modal.confirm({
                title: "確認刪除此標記？",
                content: "刪除後若要回復，需重新建立或重新匯入。",
                okText: "確認刪除",
                cancelText: "取消",
                onOk: async () => {
                  if (!activeFloor) return;
                  const response = await deleteIndoorNode(record.id);
                  if (!response.success) {
                    throw new Error(response.message || "標記刪除失敗");
                  }
                  await loadFloorDetail(activeFloor.id);
                  messageApi.success("標記已刪除");
                },
              });
            }}
          >
            刪除
          </Button>
        </Space>
      ),
    },
  ];

  const csvColumns = [
    { title: "行號", dataIndex: "rowNumber", width: 80 },
    { title: "標記代碼", dataIndex: "markerCode", width: 150 },
    { title: "名稱", dataIndex: "nodeNameZh", width: 160 },
    { title: "類型", dataIndex: "nodeType", width: 120 },
    { title: "呈現", dataIndex: "presentationMode", width: 110 },
    { title: "Appearance", dataIndex: "appearancePresetCode", width: 140 },
    { title: "Trigger", dataIndex: "triggerTemplateCode", width: 130 },
    { title: "Effect", dataIndex: "effectTemplateCode", width: 130 },
    { title: "Inherit", dataIndex: "inheritMode", width: 130 },
    {
      title: "座標",
      key: "coordinate",
      width: 150,
      render: (
        _: unknown,
        record: AdminIndoorMarkerCsvPreview["rows"][number],
      ) =>
        `${Number(record.relativeX || 0).toFixed(3)}, ${Number(record.relativeY || 0).toFixed(3)}`,
    },
    {
      title: "檢核結果",
      key: "valid",
      render: (
        _: unknown,
        record: AdminIndoorMarkerCsvPreview["rows"][number],
      ) =>
        record.valid ? (
          <Tag color="green">通過</Tag>
        ) : (
          <Space direction="vertical" size={4}>
            <Tag color="red">不合規</Tag>
            {record.errors.map((item) => (
              <Text key={item} type="danger" style={{ fontSize: 12 }}>
                {item}
              </Text>
            ))}
          </Space>
        ),
    },
  ];

  const handlePreviewZip = async () => {
    if (!selectedFloorId || !zipFile) {
      messageApi.warning("請先選擇樓層並上傳 ZIP 瓦片包");
      return;
    }
    setRunningAction("preview-zip");
    try {
      const response = await previewIndoorTileZip(
        selectedFloorId,
        zipFile,
        tileSizePx,
      );
      if (!response.success || !response.data) {
        throw new Error(response.message || "ZIP 預檢失敗");
      }
      setTilePreview(response.data);
      messageApi.success("ZIP 預檢完成");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "ZIP 預檢失敗");
    } finally {
      setRunningAction(null);
    }
  };

  const handleImportZip = async () => {
    if (!selectedFloorId || !zipFile) {
      messageApi.warning("請先選擇樓層並上傳 ZIP 瓦片包");
      return;
    }
    setRunningAction("import-zip");
    try {
      const response = await importIndoorTileZip(
        selectedFloorId,
        zipFile,
        tileSizePx,
      );
      if (!response.success || !response.data) {
        throw new Error(response.message || "ZIP 導入失敗");
      }
      setZipFile(null);
      setSelectedFloor(response.data);
      syncFloorForm(response.data);
      await refreshContext();
      messageApi.success("ZIP 瓦片包已導入");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "ZIP 導入失敗");
    } finally {
      setRunningAction(null);
    }
  };

  const handleImportImage = async () => {
    if (!selectedFloorId || !imageFile) {
      messageApi.warning("請先選擇樓層並上傳樓層整圖");
      return;
    }
    setRunningAction("import-image");
    try {
      const response = await importIndoorFloorImage(
        selectedFloorId,
        imageFile,
        tileSizePx,
      );
      if (!response.success || !response.data) {
        throw new Error(response.message || "整圖切片導入失敗");
      }
      setImageFile(null);
      setSelectedFloor(response.data);
      syncFloorForm(response.data);
      await refreshContext();
      messageApi.success("整圖已完成切片並同步更新樓層瓦片");
    } catch (error) {
      messageApi.error(
        error instanceof Error ? error.message : "整圖切片導入失敗",
      );
    } finally {
      setRunningAction(null);
    }
  };

  const handleSaveFloor = async () => {
    if (!activeFloor) {
      return;
    }
    const values = await floorForm.validateFields();
    setRunningAction("save-floor");
    try {
      const response = await updateIndoorFloor(activeFloor.id, {
        indoorMapId: activeFloor.indoorMapId,
        floorCode: values.floorCode ?? activeFloor.floorCode,
        floorNumber: values.floorNumber ?? activeFloor.floorNumber,
        floorNameZh: activeFloor.floorNameZh,
        floorNameEn: activeFloor.floorNameEn,
        floorNameZht: activeFloor.floorNameZht,
        floorNamePt: activeFloor.floorNamePt,
        descriptionZh: activeFloor.descriptionZh,
        descriptionEn: activeFloor.descriptionEn,
        descriptionZht: activeFloor.descriptionZht,
        descriptionPt: activeFloor.descriptionPt,
        coverAssetId: activeFloor.coverAssetId,
        floorPlanAssetId: activeFloor.floorPlanAssetId,
        tilePreviewImageUrl: activeFloor.tilePreviewImageUrl,
        altitudeMeters: activeFloor.altitudeMeters,
        areaSqm: values.areaSqm ?? activeFloor.areaSqm,
        zoomMin: values.zoomMin ?? activeFloor.zoomMin,
        zoomMax: values.zoomMax ?? activeFloor.zoomMax,
        defaultZoom: values.defaultZoom ?? activeFloor.defaultZoom,
        popupConfigJson: activeFloor.popupConfigJson,
        displayConfigJson: activeFloor.displayConfigJson,
        attachmentAssetIds: activeFloor.attachmentAssetIds || [],
        sortOrder: activeFloor.sortOrder,
        status: values.status ?? activeFloor.status,
        publishedAt: activeFloor.publishedAt,
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || "樓層設定更新失敗");
      }
      setSelectedFloor(response.data);
      syncFloorForm(response.data);
      await refreshContext();
      messageApi.success("樓層設定已更新");
    } catch (error) {
      messageApi.error(
        error instanceof Error ? error.message : "樓層設定更新失敗",
      );
    } finally {
      setRunningAction(null);
    }
  };

  const appendPointToBehaviorPath = React.useCallback(
    (x: number, y: number) => {
      const currentPoints = normalizePointCollection(
        markerForm.getFieldValue([
          "behaviors",
          activeBehaviorIndex,
          "pathGraph",
          "points",
        ]) as AdminIndoorNodePoint[] | undefined,
      );
      markerForm.setFieldValue(
        ["behaviors", activeBehaviorIndex, "pathGraph", "points"],
        [...currentPoints, { x, y, order: currentPoints.length }],
      );
    },
    [activeBehaviorIndex, markerForm],
  );

  const appendPointToOverlayGeometry = React.useCallback(
    (x: number, y: number) => {
      const geometry = (markerForm.getFieldValue([
        "behaviors",
        activeBehaviorIndex,
        "overlayGeometry",
      ]) || {}) as AdminIndoorOverlayGeometry;
      const currentPoints = normalizePointCollection(geometry.points);
      markerForm.setFieldValue(
        ["behaviors", activeBehaviorIndex, "overlayGeometry"],
        {
          geometryType: geometry.geometryType || "polygon",
          properties: geometry.properties,
          points:
            (geometry.geometryType || "polygon") === "point"
              ? [{ x, y, order: 0 }]
              : [...currentPoints, { x, y, order: currentPoints.length }],
        },
      );
    },
    [activeBehaviorIndex, markerForm],
  );

  const appendCurrentPathPoint = React.useCallback(() => {
    if (draftX == null || draftY == null) {
      messageApi.warning("請先在左側樓層圖上取點");
      return;
    }
    appendPointToBehaviorPath(Number(draftX), Number(draftY));
  }, [appendPointToBehaviorPath, draftX, draftY]);

  const appendCurrentOverlayPoint = React.useCallback(() => {
    if (draftX == null || draftY == null) {
      messageApi.warning("請先在左側樓層圖上取點");
      return;
    }
    appendPointToOverlayGeometry(Number(draftX), Number(draftY));
  }, [appendPointToOverlayGeometry, draftX, draftY]);

  const clearCurrentBehaviorPath = React.useCallback(() => {
    markerForm.setFieldValue(
      ["behaviors", activeBehaviorIndex, "pathGraph", "points"],
      [],
    );
  }, [activeBehaviorIndex, markerForm]);

  const clearCurrentBehaviorOverlay = React.useCallback(() => {
    markerForm.setFieldValue(
      ["behaviors", activeBehaviorIndex, "overlayGeometry"],
      undefined,
    );
  }, [activeBehaviorIndex, markerForm]);

  const handlePickPoint = (event: React.MouseEvent<HTMLDivElement>) => {
    const rect = event.currentTarget.getBoundingClientRect();
    const relativeX = Math.max(
      0,
      Math.min(1, (event.clientX - rect.left) / rect.width),
    );
    const relativeY = Math.max(
      0,
      Math.min(1, (event.clientY - rect.top) / rect.height),
    );
    markerForm.setFieldsValue({
      relativeX: Number(relativeX.toFixed(6)),
      relativeY: Number(relativeY.toFixed(6)),
    });
    if (pickMode === "path") {
      appendPointToBehaviorPath(
        Number(relativeX.toFixed(6)),
        Number(relativeY.toFixed(6)),
      );
      return;
    }
    if (pickMode === "overlay") {
      appendPointToOverlayGeometry(
        Number(relativeX.toFixed(6)),
        Number(relativeY.toFixed(6)),
      );
      return;
    }
    if (placementArmed) {
      setPlacementArmed(false);
      messageApi.success(
        `已擷取座標 (${relativeX.toFixed(3)}, ${relativeY.toFixed(3)})`,
      );
    }
  };

  const handleSaveMarker = async () => {
    if (!activeFloor) {
      return;
    }
    const values = await markerForm.validateFields();
    setRunningAction("save-marker");
    try {
      const payload: AdminIndoorNodePayload = {
        markerCode: values.markerCode,
        nodeType: values.nodeType,
        presentationMode: values.presentationMode || "marker",
        overlayType: values.overlayType || undefined,
        nodeNameZh: values.nodeNameZh,
        nodeNameEn: values.nodeNameEn,
        nodeNameZht: values.nodeNameZht,
        nodeNamePt: values.nodeNamePt,
        descriptionZh: values.descriptionZh,
        descriptionEn: values.descriptionEn,
        descriptionZht: values.descriptionZht,
        descriptionPt: values.descriptionPt,
        relativeX: values.relativeX,
        relativeY: values.relativeY,
        relatedPoiId: values.relatedPoiId,
        iconAssetId: values.iconAssetId,
        animationAssetId: values.animationAssetId,
        linkedEntityType: values.linkedEntityType || undefined,
        linkedEntityId: values.linkedEntityId,
        tags: markerTags,
        tagsJson: values.tagsJson,
        popupConfigJson: values.popupConfigJson,
        displayConfigJson: values.displayConfigJson,
        overlayGeometry: normalizeOverlayGeometry(values.overlayGeometry),
        inheritLinkedEntityRules: values.inheritLinkedEntityRules,
        runtimeSupportLevel:
          values.runtimeSupportLevel || "phase15_storage_only",
        metadataJson: values.metadataJson,
        behaviors: (values.behaviors || []).map(
          (behavior: AdminIndoorBehaviorProfile, index: number) => ({
            ...behavior,
            behaviorCode: behavior.behaviorCode || `behavior-${index + 1}`,
            appearanceRules: behavior.appearanceRules || [],
            triggerRules: behavior.triggerRules || [],
            effectRules: behavior.effectRules || [],
            pathGraph: serializePathGraph(behavior.pathGraph),
            overlayGeometry: normalizeOverlayGeometry(behavior.overlayGeometry),
            inheritMode: behavior.inheritMode || "override",
            runtimeSupportLevel:
              behavior.runtimeSupportLevel ||
              values.runtimeSupportLevel ||
              "phase15_storage_only",
            sortOrder: behavior.sortOrder ?? index,
            status: behavior.status || "draft",
          }),
        ),
        sortOrder: values.sortOrder,
        status: values.status,
      };
      const validation = await validateIndoorRuleGraph(payload, {
        floorId: activeFloor.id,
        nodeId: editingMarkerId || undefined,
      });
      if (!validation.success || !validation.data?.valid) {
        throw new Error(
          validation.message ||
            validation.data?.errors?.join("；") ||
            "規則校驗失敗",
        );
      }
      const response = editingMarkerId
        ? await updateIndoorNode(editingMarkerId, payload)
        : await createIndoorNode(activeFloor.id, payload);
      if (!response.success || !response.data) {
        throw new Error(response.message || "標記保存失敗");
      }
      const nextDrafts = markerDrafts.filter(
        (draft) => draft.draftId !== "auto",
      );
      saveMarkerDraftsToStorage(activeFloor.id, nextDrafts);
      setMarkerDrafts(nextDrafts);
      await loadFloorDetail(activeFloor.id);
      messageApi.success(editingMarkerId ? "標記已更新" : "標記已建立");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "標記保存失敗");
    } finally {
      setRunningAction(null);
    }
  };

  const handlePreviewCsv = async () => {
    if (!selectedFloorId || !csvFile) {
      messageApi.warning("請先選擇樓層並上傳標記 CSV");
      return;
    }
    setRunningAction("preview-csv");
    try {
      const response = await previewIndoorMarkerCsv(selectedFloorId, csvFile);
      if (!response.success || !response.data) {
        throw new Error(response.message || "CSV 預檢失敗");
      }
      setCsvPreview(response.data);
      messageApi.success("CSV 預檢完成");
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "CSV 預檢失敗");
    } finally {
      setRunningAction(null);
    }
  };

  const handleConfirmCsv = async () => {
    if (!activeFloor || !csvPreview) {
      return;
    }
    if (csvPreview.invalidRows > 0) {
      messageApi.warning("CSV 仍有不合規資料，請先修正再確認");
      return;
    }
    setRunningAction("confirm-csv");
    try {
      const response = await confirmIndoorMarkerCsv(activeFloor.id, {
        sourceFilename: csvPreview.sourceFilename,
        rows: csvPreview.rows.map((row) => ({
          rowNumber: row.rowNumber,
          markerCode: row.markerCode,
          nodeType: row.nodeType,
          nodeNameZh: row.nodeNameZh,
          nodeNameEn: row.nodeNameEn,
          nodeNameZht: row.nodeNameZht,
          nodeNamePt: row.nodeNamePt,
          descriptionZh: row.descriptionZh,
          descriptionEn: row.descriptionEn,
          descriptionZht: row.descriptionZht,
          descriptionPt: row.descriptionPt,
          relativeX: row.relativeX,
          relativeY: row.relativeY,
          relatedPoiId: row.relatedPoiId,
          iconAssetId: row.iconAssetId,
          animationAssetId: row.animationAssetId,
          linkedEntityType: row.linkedEntityType,
          linkedEntityId: row.linkedEntityId,
          tagsJson: row.tagsJson,
          popupConfigJson: row.popupConfigJson,
          displayConfigJson: row.displayConfigJson,
          metadataJson: row.metadataJson,
          sortOrder: row.sortOrder,
          status: row.status,
          presentationMode: row.presentationMode,
          appearancePresetCode: row.appearancePresetCode,
          triggerTemplateCode: row.triggerTemplateCode,
          effectTemplateCode: row.effectTemplateCode,
          inheritMode: row.inheritMode,
        })),
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || "CSV 導入失敗");
      }
      setCsvFile(null);
      setCsvPreview(null);
      await loadFloorDetail(activeFloor.id);
      messageApi.success(`已匯入 ${response.data.importedRows} 個標記`);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : "CSV 導入失敗");
    } finally {
      setRunningAction(null);
    }
  };

  const downloadCsvTemplate = () => {
    const header = [
      "markerCode",
      "nodeType",
      "nameZh",
      "nameEn",
      "nameZht",
      "namePt",
      "descriptionZh",
      "descriptionEn",
      "descriptionZht",
      "descriptionPt",
      "relativeX",
      "relativeY",
      "relatedPoiId",
      "iconAssetId",
      "animationAssetId",
      "linkedEntityType",
      "linkedEntityId",
      "tagsJson",
      "popupConfigJson",
      "displayConfigJson",
      "metadataJson",
      "sortOrder",
      "status",
      "presentationMode",
      "appearancePresetCode",
      "triggerTemplateCode",
      "effectTemplateCode",
      "inheritMode",
    ];
    const sample = [
      "museum-gate",
      "entrance",
      "博物館入口",
      "Main Entrance",
      "博物館入口",
      "Entrada Principal",
      "遊客由此進入展區",
      "Visitors enter here",
      "遊客由此進入展區",
      "Os visitantes entram aqui",
      "0.245",
      "0.618",
      "",
      "",
      "",
      "chapter",
      "1",
      '["入口","導覽"]',
      '{"enabled":true}',
      '{"labelMode":"always"}',
      '{"hint":"story-start"}',
      "10",
      "published",
      "overlay",
      "schedule_window",
      "tap",
      "popup",
      "override",
    ];
    const csv = `${header.join(",")}\n${sample.map((item) => `"${String(item).replace(/"/g, '""')}"`).join(",")}`;
    const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = "indoor-marker-template.csv";
    anchor.click();
    URL.revokeObjectURL(url);
  };

  const content = (
    <Space direction="vertical" size="large" style={{ width: "100%" }}>
      <Card title="工作台定位">
        <Row gutter={[16, 16]}>
          <Col xs={24} md={10}>
            <Text strong>建築</Text>
            <Select
              showSearch
              style={{ width: "100%", marginTop: 8 }}
              loading={loadingBuildings}
              options={buildingOptions}
              value={selectedBuildingId ?? undefined}
              onChange={handleBuildingChange}
              optionFilterProp="label"
              placeholder="請選擇建築"
            />
          </Col>
          <Col xs={24} md={10}>
            <Text strong>樓層</Text>
            <Select
              showSearch
              style={{ width: "100%", marginTop: 8 }}
              disabled={!activeBuilding?.floors?.length}
              loading={floorSwitching || loadingFloor}
              options={floorOptions}
              value={selectedFloorId ?? undefined}
              onChange={handleFloorChange}
              optionFilterProp="label"
              placeholder="請選擇樓層"
            />
          </Col>
          <Col xs={24} md={4}>
            <Alert
              style={{ marginTop: 30 }}
              type={activeFloor ? "success" : "warning"}
              showIcon
              message={
                floorSwitching || loadingFloor
                  ? "樓層資料載入中"
                  : activeFloor
                    ? "已定位到樓層"
                    : "尚未選擇樓層"
              }
            />
          </Col>
        </Row>
        {activeBuilding?.floors?.length ? (
          <Space wrap style={{ marginTop: 16 }}>
            <Text type="secondary">快速切換樓層：</Text>
            {activeBuilding.floors.map((floor) => (
              <Button
                key={floor.id}
                size="small"
                type={selectedFloorId === floor.id ? "primary" : "default"}
                onClick={() => handleFloorChange(floor.id)}
              >
                {floor.floorCode || pickLocalizedName(floor)}
              </Button>
            ))}
          </Space>
        ) : null}
        {showcaseBuilding ? (
          <Alert
            style={{ marginTop: 16 }}
            type={
              activeBuilding?.buildingCode === SHOWCASE_BUILDING_CODE
                ? "success"
                : "info"
            }
            showIcon
            message="可直接驗證澳門葡京人真實示例"
            description="若目前沒有指定建築，系統會優先聚焦到已同步資料庫、COS 與小程序室內頁的葡京人示例。你也可以隨時一鍵切回它。"
            action={
              <Space wrap>
                <Button
                  size="small"
                  onClick={() => handleBuildingChange(showcaseBuilding.id)}
                >
                  載入葡京人
                </Button>
                {activeBuilding?.buildingCode === SHOWCASE_BUILDING_CODE &&
                activeBuilding.floors?.length ? (
                  <Button
                    size="small"
                    type="primary"
                    onClick={() =>
                      handleFloorChange(
                        resolveDefaultIndoorFloorId(
                          activeBuilding.floors || [],
                          null,
                          SHOWCASE_BUILDING_CODE,
                        ) || activeBuilding.floors[0].id,
                      )
                    }
                  >
                    回到示例樓層
                  </Button>
                ) : null}
              </Space>
            }
          />
        ) : null}
      </Card>

      {!activeBuilding ? (
        <Card>
          <Empty description="目前還沒有可用的室內建築。請先到「建築與樓層」建立室內建築與樓層。" />
        </Card>
      ) : (
        <>
          <Row gutter={[16, 16]}>
            <Col xs={24} xl={8}>
              <Card title="建築摘要">
                {activeBuilding.coverImageUrl ? (
                  <div
                    style={{
                      width: "100%",
                      aspectRatio: "4 / 3",
                      overflow: "hidden",
                      borderRadius: 12,
                      background: "#f5f7fb",
                      border: "1px solid #e6ebf5",
                      marginBottom: 16,
                    }}
                  >
                    <img
                      src={activeBuilding.coverImageUrl}
                      alt={pickLocalizedName(activeBuilding)}
                      style={{
                        width: "100%",
                        height: "100%",
                        objectFit: "cover",
                      }}
                    />
                  </div>
                ) : null}
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="建築">
                    {pickLocalizedName(activeBuilding)}
                  </Descriptions.Item>
                  <Descriptions.Item label="代碼">
                    {activeBuilding.buildingCode}
                  </Descriptions.Item>
                  <Descriptions.Item label="綁定方式">
                    {activeBuilding.bindingMode === "poi"
                      ? "POI 綁定"
                      : "地圖 / 子地圖綁定"}
                  </Descriptions.Item>
                  <Descriptions.Item label="樓層規模">
                    {(activeBuilding.floors || []).length} /{" "}
                    {activeBuilding.totalFloors || 0}
                  </Descriptions.Item>
                  <Descriptions.Item label="位置">
                    {activeBuilding.cityName || activeBuilding.cityCode || "-"}
                    {activeBuilding.subMapName
                      ? ` / ${activeBuilding.subMapName}`
                      : ""}
                  </Descriptions.Item>
                  <Descriptions.Item label="狀態">
                    {renderStatus(activeBuilding.status)}
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            </Col>
            <Col xs={24} md={8} xl={5}>
              <Card>
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="標記數">
                    {floorMarkerCount(activeFloor)}
                  </Descriptions.Item>
                  <Descriptions.Item label="瓦片數">
                    {activeFloor?.tileEntryCount || 0}
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            </Col>
            <Col xs={24} md={8} xl={5}>
              <Card>
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="導入狀態">
                    {renderImportStatus(activeFloor?.importStatus)}
                  </Descriptions.Item>
                  <Descriptions.Item label="網格">
                    {activeFloor?.gridCols || 0} x {activeFloor?.gridRows || 0}
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            </Col>
            <Col xs={24} md={8} xl={6}>
              <Card>
                <Descriptions column={1} size="small">
                  <Descriptions.Item label="縮放範圍">
                    {activeFloor
                      ? `${activeFloor.zoomMin || "-"} / ${activeFloor.zoomMax || "-"}`
                      : "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="預設縮放">
                    {activeFloor?.defaultZoom || "-"}
                  </Descriptions.Item>
                </Descriptions>
              </Card>
            </Col>
          </Row>

          {activeBuilding.buildingCode === "lisboeta_macau" ? (
            <Alert
              type="success"
              showIcon
              message="已載入澳門葡京人室內示例"
              description="這套示例資料已同步進資料庫與 COS，可直接切換 G / 1F / 2F，檢查瓦片導入、縮放推導、標記列表與 CSV 匯入結果。"
            />
          ) : null}

          {activeFloor ? (
            <Row gutter={[16, 16]}>
              <Col xs={24} xl={12}>
                <Card title="目前樓層預覽">
                  <div
                    style={{
                      width: "100%",
                      aspectRatio: "16 / 10",
                      overflow: "hidden",
                      borderRadius: 12,
                      background: "#f5f7fb",
                      border: "1px solid #e6ebf5",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    {activeFloor.tilePreviewImageUrl ||
                    activeFloor.floorPlanUrl ? (
                      <img
                        src={
                          activeFloor.tilePreviewImageUrl ||
                          activeFloor.floorPlanUrl ||
                          ""
                        }
                        alt={pickLocalizedName(activeFloor)}
                        style={{
                          width: "100%",
                          height: "100%",
                          objectFit: "cover",
                        }}
                      />
                    ) : (
                      <Text type="secondary">此樓層尚未產生預覽圖</Text>
                    )}
                  </div>
                </Card>
              </Col>
              <Col xs={24} xl={12}>
                <Card title="當前樓層已接通能力">
                  <Space wrap>
                    <Tag
                      color={
                        activeFloor.tilePreviewImageUrl ||
                        activeFloor.floorPlanUrl
                          ? "green"
                          : "default"
                      }
                    >
                      樓層預覽{" "}
                      {activeFloor.tilePreviewImageUrl ||
                      activeFloor.floorPlanUrl
                        ? "已接通"
                        : "待補"}
                    </Tag>
                    <Tag color={activeFloor.tileRootUrl ? "cyan" : "default"}>
                      COS 瓦片 {activeFloor.tileRootUrl ? "已發布" : "未發布"}
                    </Tag>
                    <Tag
                      color={activeFloor.tileManifestJson ? "green" : "default"}
                    >
                      Manifest{" "}
                      {activeFloor.tileManifestJson ? "已生成" : "未生成"}
                    </Tag>
                    <Tag
                      color={
                        activeFloor.tileZoomDerivationJson
                          ? "purple"
                          : "default"
                      }
                    >
                      縮放推導{" "}
                      {activeFloor.tileZoomDerivationJson ? "已生成" : "未生成"}
                    </Tag>
                    <Tag
                      color={
                        floorMarkerCount(activeFloor) > 0
                          ? "magenta"
                          : "default"
                      }
                    >
                      標記 {floorMarkerCount(activeFloor)} 個
                    </Tag>
                  </Space>
                  <Descriptions
                    size="small"
                    column={1}
                    style={{ marginTop: 16 }}
                  >
                    <Descriptions.Item label="附件資源">
                      {activeFloor.attachmentAssetIds?.length || 0} 項
                    </Descriptions.Item>
                    <Descriptions.Item label="最後導入">
                      {activeFloor.importedAt || "尚未導入"}
                    </Descriptions.Item>
                    <Descriptions.Item label="瓦片來源檔">
                      {activeFloor.tileSourceFilename || "未設定"}
                    </Descriptions.Item>
                  </Descriptions>
                </Card>
              </Col>
            </Row>
          ) : null}

          <Card
            title="樓層圖資、瓦片與縮放設定"
            loading={loadingFloor || floorSwitching}
          >
            {activeFloor ? (
              <Space
                direction="vertical"
                size="large"
                style={{ width: "100%" }}
              >
                <Descriptions size="small" column={3} bordered>
                  <Descriptions.Item label="樓層">
                    {pickLocalizedName(activeFloor)}
                  </Descriptions.Item>
                  <Descriptions.Item label="代碼">
                    {activeFloor.floorCode || "-"}
                  </Descriptions.Item>
                  <Descriptions.Item label="導入狀態">
                    {renderImportStatus(activeFloor.importStatus)}
                  </Descriptions.Item>
                  <Descriptions.Item label="瓦片來源">
                    {activeFloor.tileSourceType || "未導入"}
                  </Descriptions.Item>
                  <Descriptions.Item label="瓦片數">
                    {activeFloor.tileEntryCount || 0}
                  </Descriptions.Item>
                  <Descriptions.Item label="網格">
                    {activeFloor.gridCols || 0} x {activeFloor.gridRows || 0}
                  </Descriptions.Item>
                </Descriptions>
                {activeFloor.importNote ? (
                  <Alert
                    type="info"
                    showIcon
                    message="導入備註"
                    description={activeFloor.importNote}
                  />
                ) : null}

                <Row gutter={[16, 16]}>
                  <Col xs={24} xl={12}>
                    <Card
                      size="small"
                      title="ZIP 瓦片包"
                      extra={
                        <Space>
                          <Space.Compact>
                            <InputNumber
                              min={128}
                              max={1024}
                              step={128}
                              value={tileSizePx}
                              onChange={(value) =>
                                setTileSizePx(
                                  typeof value === "number" ? value : 512,
                                )
                              }
                            />
                            <Button disabled style={{ pointerEvents: "none" }}>
                              px
                            </Button>
                          </Space.Compact>
                          <Button
                            icon={<FileSearchOutlined />}
                            loading={runningAction === "preview-zip"}
                            onClick={() => void handlePreviewZip()}
                          >
                            預檢
                          </Button>
                          <Button
                            type="primary"
                            icon={<CloudUploadOutlined />}
                            loading={runningAction === "import-zip"}
                            onClick={() => void handleImportZip()}
                          >
                            導入
                          </Button>
                        </Space>
                      }
                    >
                      <UploadSelector
                        title="拖曳或點擊上傳 ZIP 瓦片包"
                        description="支援 z/x/y.png 與 z/x_y.png，會先做預檢再決定是否寫入樓層資料。"
                        accept=".zip"
                        file={zipFile}
                        uid="zip-upload"
                        onSelect={setZipFile}
                      />
                    </Card>
                  </Col>
                  <Col xs={24} xl={12}>
                    <Card
                      size="small"
                      title="樓層整圖切片"
                      extra={
                        <Button
                          type="primary"
                          icon={<CloudUploadOutlined />}
                          loading={runningAction === "import-image"}
                          onClick={() => void handleImportImage()}
                        >
                          切片並上傳
                        </Button>
                      }
                    >
                      <UploadSelector
                        title="拖曳或點擊上傳樓層整圖"
                        description="後台會自動切片、生成 manifest、推導縮放倍數，並上傳到 COS。"
                        accept="image/*"
                        file={imageFile}
                        uid="image-upload"
                        onSelect={setImageFile}
                      />
                    </Card>
                  </Col>
                </Row>

                <Row gutter={[16, 16]}>
                  <Col xs={24} xl={12}>
                    <Card size="small" title="預覽與推導結果">
                      {tilePreview ? (
                        <>
                          <Descriptions size="small" column={2}>
                            <Descriptions.Item label="來源">
                              {tilePreview.sourceType}
                            </Descriptions.Item>
                            <Descriptions.Item label="檔名">
                              {tilePreview.sourceFilename || "-"}
                            </Descriptions.Item>
                            <Descriptions.Item label="尺寸">
                              {tilePreview.imageWidthPx || 0} x{" "}
                              {tilePreview.imageHeightPx || 0}
                            </Descriptions.Item>
                            <Descriptions.Item label="網格">
                              {tilePreview.gridCols || 0} x{" "}
                              {tilePreview.gridRows || 0}
                            </Descriptions.Item>
                            <Descriptions.Item label="瓦片數">
                              {tilePreview.tileEntryCount || 0}
                            </Descriptions.Item>
                            <Descriptions.Item label="縮放">
                              {tilePreview.zoomMin || "-"} /{" "}
                              {tilePreview.defaultZoom || "-"} /{" "}
                              {tilePreview.zoomMax || "-"}
                            </Descriptions.Item>
                          </Descriptions>
                          <Input.TextArea
                            style={{ marginTop: 12 }}
                            rows={8}
                            readOnly
                            value={tilePreview.derivationJson || ""}
                          />
                          {tilePreview.notes?.length ? (
                            <Alert
                              style={{ marginTop: 12 }}
                              type="info"
                              showIcon
                              message="推導說明"
                              description={
                                <Space direction="vertical" size={4}>
                                  {tilePreview.notes.map((note) => (
                                    <Text key={note}>{note}</Text>
                                  ))}
                                </Space>
                              }
                            />
                          ) : null}
                        </>
                      ) : (
                        <Empty description="完成 ZIP 或整圖預覽後，這裡會顯示網格、瓦片與縮放推導結果。" />
                      )}
                    </Card>
                  </Col>
                  <Col xs={24} xl={12}>
                    <Card size="small" title="樓層縮放設定">
                      <Form form={floorForm} layout="vertical">
                        <Row gutter={16}>
                          <Col span={8}>
                            <Form.Item
                              name="floorNumber"
                              label="樓層序號"
                              rules={[
                                { required: true, message: "請輸入樓層序號" },
                              ]}
                            >
                              <InputNumber style={{ width: "100%" }} />
                            </Form.Item>
                          </Col>
                          <Col span={8}>
                            <Form.Item name="floorCode" label="樓層代碼">
                              <Input />
                            </Form.Item>
                          </Col>
                          <Col span={8}>
                            <Form.Item name="status" label="狀態">
                              <Select options={statusOptions} />
                            </Form.Item>
                          </Col>
                        </Row>
                        <Row gutter={16}>
                          <Col span={6}>
                            <Form.Item name="areaSqm" label="面積 (m²)">
                              <InputNumber min={0} style={{ width: "100%" }} />
                            </Form.Item>
                          </Col>
                          <Col span={6}>
                            <Form.Item name="zoomMin" label="最小縮放">
                              <InputNumber
                                min={0.1}
                                step={0.1}
                                style={{ width: "100%" }}
                              />
                            </Form.Item>
                          </Col>
                          <Col span={6}>
                            <Form.Item name="defaultZoom" label="預設縮放">
                              <InputNumber
                                min={0.1}
                                step={0.1}
                                style={{ width: "100%" }}
                              />
                            </Form.Item>
                          </Col>
                          <Col span={6}>
                            <Form.Item name="zoomMax" label="最大縮放">
                              <InputNumber
                                min={0.1}
                                step={0.1}
                                style={{ width: "100%" }}
                              />
                            </Form.Item>
                          </Col>
                        </Row>
                        <Button
                          type="primary"
                          icon={<SaveOutlined />}
                          loading={runningAction === "save-floor"}
                          onClick={() => void handleSaveFloor()}
                        >
                          保存樓層設定
                        </Button>
                      </Form>
                    </Card>
                  </Col>
                </Row>

                <Card size="small" title="目前樓層 Manifest">
                  <Input.TextArea
                    rows={8}
                    readOnly
                    value={activeFloor.tileManifestJson || ""}
                  />
                </Card>
              </Space>
            ) : (
              <Empty description="請先選擇樓層再操作圖資導入。" />
            )}
          </Card>

          <Card
            title="標記、疊加物與縮略圖取點"
            extra={
              floorSwitching || loadingFloor ? (
                <Tag color="processing">樓層資料載入中</Tag>
              ) : null
            }
          >
            {activeFloor ? (
              <Row gutter={[16, 16]}>
                <Col xs={24} xl={13}>
                  <Alert
                    type={placementArmed ? "success" : "info"}
                    showIcon
                    message={
                      placementArmed
                        ? "已啟用取點模式"
                        : "可直接點擊縮略圖擷取座標"
                    }
                    description="點擊樓層縮略圖即可吸取相對位置。若未自訂 icon，小程序前台會預設以紅點顯示。"
                  />
                  <div style={{ marginTop: 12 }}>
                    <FloorCanvas
                      loading={loadingFloor || floorSwitching}
                      floor={activeFloor}
                      nodes={activeFloorNodes}
                      editingMarkerId={editingMarkerId}
                      draftX={draftX}
                      draftY={draftY}
                      pickMode={pickMode}
                      currentPathPoints={currentPathPoints}
                      currentOverlayGeometry={currentOverlayPreview}
                      onPick={handlePickPoint}
                      onSelectMarker={loadNodeIntoEditor}
                    />
                  </div>
                  <Space style={{ marginTop: 12 }}>
                    <Button
                      icon={<AimOutlined />}
                      type={placementArmed ? "primary" : "default"}
                      onClick={() => setPlacementArmed((current) => !current)}
                    >
                      {placementArmed ? "取消取點模式" : "下一次點擊後取點"}
                    </Button>
                    <Button
                      icon={<PlusOutlined />}
                      onClick={() => resetMarkerEditor(activeFloor)}
                    >
                      新增空白標記
                    </Button>
                    <Button onClick={() => saveCurrentMarkerDraft(true)}>
                      保存到草稿區
                    </Button>
                  </Space>
                  <Card
                    size="small"
                    title="草稿儲存區"
                    style={{ marginTop: 16 }}
                  >
                    {markerDrafts.length ? (
                      <List
                        dataSource={markerDrafts}
                        renderItem={(draft) => (
                          <List.Item
                            actions={[
                              <Button
                                key={`restore-${draft.draftId}`}
                                type="link"
                                size="small"
                                onClick={() => restoreMarkerDraft(draft)}
                              >
                                繼續編輯
                              </Button>,
                              <Button
                                key={`delete-${draft.draftId}`}
                                type="link"
                                size="small"
                                danger
                                onClick={() => deleteMarkerDraft(draft.draftId)}
                              >
                                刪除
                              </Button>,
                            ]}
                          >
                            <List.Item.Meta
                              title={
                                <Space>
                                  <Text strong>{draft.draftName}</Text>
                                  <Tag
                                    color={draft.isAuto ? "blue" : "default"}
                                  >
                                    {draft.isAuto ? "自動暫存" : "手動草稿"}
                                  </Tag>
                                </Space>
                              }
                              description={`最後保存：${new Date(draft.savedAt).toLocaleString("zh-Hant-HK")}`}
                            />
                          </List.Item>
                        )}
                      />
                    ) : (
                      <Empty
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        description="尚未保存任何草稿；編輯中的內容也會自動暫存到這裡。"
                      />
                    )}
                  </Card>
                </Col>
                <Col xs={24} xl={11}>
                  <Card
                    size="small"
                    title={editingMarkerId ? "編輯標記" : "新增標記"}
                    extra={
                      <Space>
                        <Tag color={selectedIconAssetId ? "blue" : "red"}>
                          {selectedIconAssetId ? "已配置 icon" : "預設紅點"}
                        </Tag>
                        <Button onClick={() => resetMarkerEditor(activeFloor)}>
                          重設
                        </Button>
                        <Button
                          type="primary"
                          icon={<SaveOutlined />}
                          loading={runningAction === "save-marker"}
                          onClick={() => void handleSaveMarker()}
                        >
                          {editingMarkerId ? "更新標記" : "建立標記"}
                        </Button>
                      </Space>
                    }
                  >
                    <Form
                      form={markerForm}
                      layout="vertical"
                      onValuesChange={(changedValues, allValues) => {
                        if (
                          changedValues &&
                          typeof changedValues === "object" &&
                          "behaviors" in changedValues
                        ) {
                          syncBehaviorProfilesDraft(
                            ((allValues as Partial<AdminIndoorMarkerPayload>)
                              ?.behaviors as
                              | AdminIndoorBehaviorProfile[]
                              | undefined) || [],
                          );
                        }
                        scheduleAutoDraftSave(
                          allValues as Partial<AdminIndoorMarkerPayload>,
                        );
                      }}
                    >
                      <Row gutter={16}>
                        <Col span={12}>
                          <Form.Item name="markerCode" label="標記代碼">
                            <Input placeholder="museum-gate" />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="nodeType" label="標記類型">
                            <Select options={markerTypeOptions} />
                          </Form.Item>
                        </Col>
                      </Row>
                      <LocalizedFieldGroup
                        form={markerForm}
                        label="標記名稱"
                        fieldNames={markerNameFields}
                        required
                        translationDefaults={translationSettings}
                      />
                      <LocalizedFieldGroup
                        form={markerForm}
                        label="標記說明"
                        fieldNames={markerDescriptionFields}
                        multiline
                        rows={3}
                        translationDefaults={translationSettings}
                      />
                      <Row gutter={16}>
                        <Col span={12}>
                          <Form.Item
                            name="relativeX"
                            label="相對 X"
                            rules={[
                              { required: true, message: "請設定相對 X 座標" },
                            ]}
                          >
                            <InputNumber
                              min={0}
                              max={1}
                              step={0.001}
                              style={{ width: "100%" }}
                            />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item
                            name="relativeY"
                            label="相對 Y"
                            rules={[
                              { required: true, message: "請設定相對 Y 座標" },
                            ]}
                          >
                            <InputNumber
                              min={0}
                              max={1}
                              step={0.001}
                              style={{ width: "100%" }}
                            />
                          </Form.Item>
                        </Col>
                      </Row>
                      <Row gutter={16}>
                        <Col span={12}>
                          <Form.Item name="relatedPoiId" label="綁定 POI">
                            <Select
                              allowClear
                              showSearch
                              options={filteredPoiOptions}
                              optionFilterProp="label"
                              placeholder="只顯示當前建築所在城市 / 子地圖的 POI"
                            />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="status" label="狀態">
                            <Select options={statusOptions} />
                          </Form.Item>
                        </Col>
                      </Row>
                      <Row gutter={16}>
                        <Col span={12}>
                          <Form.Item
                            name="linkedEntityType"
                            label="綁定實體類型"
                          >
                            <Select options={linkedEntityOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={12}>
                          <Form.Item name="linkedEntityId" label="綁定實體">
                            {linkedEntityIdOptions.length ? (
                              <Select
                                allowClear
                                showSearch
                                optionFilterProp="label"
                                options={linkedEntityIdOptions}
                                placeholder="搜尋並選擇已存在的實體"
                              />
                            ) : (
                              <InputNumber
                                min={1}
                                style={{ width: "100%" }}
                                placeholder={
                                  linkedEntityType
                                    ? "目前類型沒有可搜尋清單，請輸入 ID"
                                    : "請先選擇綁定實體類型"
                                }
                              />
                            )}
                          </Form.Item>
                        </Col>
                      </Row>
                      <Form.Item name="sortOrder" label="排序">
                        <InputNumber min={0} style={{ width: "100%" }} />
                      </Form.Item>
                      <MediaAssetPickerField
                        name="iconAssetId"
                        label="地圖圖示"
                        assetKind="icon"
                        valueMode="asset-id"
                        help="可直接上傳圖片或 GIF。若未設定，小程序前台會預設以紅點顯示。"
                      />
                      <MediaAssetPickerField
                        name="animationAssetId"
                        label="動效資源"
                        assetKind="image"
                        valueMode="asset-id"
                      />
                      <Divider style={{ margin: "8px 0 16px" }} />
                      <Card
                        size="small"
                        title="互動規則編排"
                        extra={
                          <Space>
                            <Button
                              icon={<FileSearchOutlined />}
                              onClick={() => {
                                if (!activeBuilding?.id || !activeFloor?.id) {
                                  return;
                                }
                                navigate(
                                  `/space/indoor-rules?buildingId=${activeBuilding.id}&floorId=${activeFloor.id}`,
                                );
                              }}
                              disabled={!activeBuilding?.id || !activeFloor?.id}
                            >
                              打開治理中心
                            </Button>
                            <Button
                              type="primary"
                              icon={<EditOutlined />}
                              onClick={() => setRuleWorkbenchOpen(true)}
                            >
                              編輯互動規則
                            </Button>
                          </Space>
                        }
                      >
                        <Space
                          direction="vertical"
                          size="middle"
                          style={{ width: "100%" }}
                        >
                          <Alert
                            type="info"
                            showIcon
                            message="互動規則已改為獨立工作台編排"
                            description="在彈窗工作台內可逐條建立行為、配置出現條件、觸發鏈、效果、路徑與疊加物幾何，且會先校驗通過才套用回目前表單。"
                          />
                          <Space wrap>
                            <Tag color="blue">
                              行為 {behaviorProfiles.length}
                            </Tag>
                            <Tag color="purple">
                              呈現模式 {presentationMode || "marker"}
                            </Tag>
                            <Tag
                              color={
                                activeBehaviorProfile ? "green" : "default"
                              }
                            >
                              {activeBehaviorProfile
                                ? `當前：${activeBehaviorProfile.behaviorNameZht || activeBehaviorProfile.behaviorNameZh || activeBehaviorProfile.behaviorCode || "未命名行為"}`
                                : "尚未建立行為"}
                            </Tag>
                          </Space>
                          <List
                            size="small"
                            bordered
                            dataSource={behaviorProfiles.slice(0, 3)}
                            locale={{
                              emptyText: "尚未建立互動行為，可在工作台中新增。",
                            }}
                            renderItem={(behavior, index) => (
                              <List.Item>
                                <Space
                                  style={{
                                    width: "100%",
                                    justifyContent: "space-between",
                                  }}
                                >
                                  <div>
                                    <Text strong>
                                      {behavior.behaviorNameZht ||
                                        behavior.behaviorNameZh ||
                                        behavior.behaviorCode ||
                                        `互動行為 ${index + 1}`}
                                    </Text>
                                    <br />
                                    <Text type="secondary">
                                      {behavior.behaviorCode ||
                                        `behavior-${index + 1}`}
                                    </Text>
                                  </div>
                                  <Space wrap>
                                    <Tag>
                                      {behavior.appearanceRules?.length || 0}{" "}
                                      出現
                                    </Tag>
                                    <Tag>
                                      {behavior.triggerRules?.length || 0} 觸發
                                    </Tag>
                                    <Tag>
                                      {behavior.effectRules?.length || 0} 效果
                                    </Tag>
                                  </Space>
                                </Space>
                              </List.Item>
                            )}
                          />
                          {behaviorProfiles.length > 3 ? (
                            <Text type="secondary">
                              尚有 {behaviorProfiles.length - 3}{" "}
                              條互動行為未在此摘要列出，請在工作台中查看。
                            </Text>
                          ) : null}
                        </Space>
                      </Card>
                      <IndoorRuleWorkbench
                        open={ruleWorkbenchOpen}
                        floor={activeFloor}
                        nodeId={editingMarkerId}
                        nodes={activeFloorNodes}
                        initialValues={
                          markerForm.getFieldsValue(
                            true,
                          ) as Partial<AdminIndoorMarkerPayload>
                        }
                        basePayload={{
                          ...(markerForm.getFieldsValue(
                            true,
                          ) as Partial<AdminIndoorNodePayload>),
                          nodeNameZh:
                            markerForm.getFieldValue("nodeNameZh") || "",
                        }}
                        rewardRuleOptions={rewardRuleOptions}
                        rewardRuleLoading={rewardRuleLoading}
                        onClose={() => setRuleWorkbenchOpen(false)}
                        onApply={applyRuleWorkbenchValues}
                      />
                      {false ? (
                        <Card
                          style={{ display: "none" }}
                          size="small"
                          title="互動規則編排"
                          extra={
                            <Space>
                              {behaviorProfiles.length ? (
                                <Select
                                  style={{ width: 220 }}
                                  value={activeBehaviorIndex}
                                  options={behaviorProfiles.map(
                                    (behavior, index) => ({
                                      label:
                                        behavior.behaviorNameZht ||
                                        behavior.behaviorNameZh ||
                                        behavior.behaviorNameEn ||
                                        behavior.behaviorCode ||
                                        `behavior-${index + 1}`,
                                      value: index,
                                    }),
                                  )}
                                  onChange={(value) => {
                                    setPendingBehaviorIndex(null);
                                    setActiveBehaviorIndex(value);
                                  }}
                                />
                              ) : null}
                              <Button
                                icon={<PlusOutlined />}
                                onClick={addBehaviorProfile}
                              >
                                新增行為
                              </Button>
                              {behaviorProfiles.length ? (
                                <Button
                                  danger
                                  icon={<DeleteOutlined />}
                                  onClick={() =>
                                    removeBehaviorProfile(activeBehaviorIndex)
                                  }
                                >
                                  刪除此行為
                                </Button>
                              ) : null}
                            </Space>
                          }
                        >
                          {behaviorProfiles.length ? (
                            <Space
                              key={activeBehaviorEditorKey}
                              direction="vertical"
                              size="middle"
                              style={{ width: "100%" }}
                            >
                              <Row gutter={16}>
                                <Col span={12}>
                                  <Form.Item
                                    name="presentationMode"
                                    label="呈現模式"
                                  >
                                    <Select options={presentationModeOptions} />
                                  </Form.Item>
                                </Col>
                                <Col span={12}>
                                  <Form.Item
                                    name="overlayType"
                                    label="疊加物類型"
                                  >
                                    <Select
                                      allowClear
                                      disabled={presentationMode === "marker"}
                                      options={overlayTypeOptions}
                                    />
                                  </Form.Item>
                                </Col>
                              </Row>
                              <Row gutter={16}>
                                <Col span={12}>
                                  <Form.Item
                                    name="inheritLinkedEntityRules"
                                    label="繼承綁定實體規則"
                                    valuePropName="checked"
                                  >
                                    <Switch />
                                  </Form.Item>
                                </Col>
                                <Col span={12}>
                                  <Form.Item
                                    name="runtimeSupportLevel"
                                    label="節點支援層級"
                                  >
                                    <Select options={runtimeSupportOptions} />
                                  </Form.Item>
                                </Col>
                              </Row>
                              <Row gutter={16}>
                                <Col span={8}>
                                  <Form.Item
                                    name={[
                                      ...activeBehaviorBasePath,
                                      "behaviorCode",
                                    ]}
                                    label="行為代碼"
                                  >
                                    <Input placeholder="night-overlay" />
                                  </Form.Item>
                                </Col>
                                <Col span={8}>
                                  <Form.Item
                                    name={[
                                      ...activeBehaviorBasePath,
                                      "behaviorNameZht",
                                    ]}
                                    label="繁中名稱"
                                  >
                                    <Input placeholder="夜間禮賓浮層" />
                                  </Form.Item>
                                </Col>
                                <Col span={8}>
                                  <Form.Item
                                    name={[
                                      ...activeBehaviorBasePath,
                                      "inheritMode",
                                    ]}
                                    label="承襲模式"
                                  >
                                    <Select options={inheritModeOptions} />
                                  </Form.Item>
                                </Col>
                              </Row>
                              <Row gutter={16}>
                                <Col span={12}>
                                  <Form.Item
                                    name={[
                                      ...activeBehaviorBasePath,
                                      "runtimeSupportLevel",
                                    ]}
                                    label="行為支援層級"
                                  >
                                    <Select options={runtimeSupportOptions} />
                                  </Form.Item>
                                </Col>
                                <Col span={12}>
                                  <Form.Item
                                    name={[...activeBehaviorBasePath, "status"]}
                                    label="行為狀態"
                                  >
                                    <Select options={statusOptions} />
                                  </Form.Item>
                                </Col>
                              </Row>
                              <Tabs
                                destroyOnHidden
                                items={[
                                  {
                                    key: "appearance",
                                    label: "出現條件",
                                    children: (
                                      <IndoorRuleAppearanceEditor
                                        form={markerForm}
                                        basePath={activeBehaviorBasePath}
                                      />
                                    ),
                                  },
                                  {
                                    key: "triggers",
                                    label: "觸發鏈",
                                    children: (
                                      <IndoorRuleTriggerChainEditor
                                        form={markerForm}
                                        basePath={activeBehaviorBasePath}
                                      />
                                    ),
                                  },
                                  {
                                    key: "effects",
                                    label: "效果與路徑",
                                    children: (
                                      <Space
                                        direction="vertical"
                                        size="middle"
                                        style={{ width: "100%" }}
                                      >
                                        <IndoorRuleEffectEditor
                                          form={markerForm}
                                          basePath={activeBehaviorBasePath}
                                        />
                                        <IndoorPathEditor
                                          form={markerForm}
                                          basePath={activeBehaviorBasePath}
                                          currentPoint={{
                                            x: draftX,
                                            y: draftY,
                                          }}
                                          picking={pickMode === "path"}
                                          onArmPick={() =>
                                            setPickMode((current) =>
                                              current === "path"
                                                ? "marker"
                                                : "path",
                                            )
                                          }
                                          onAppendCurrentPoint={
                                            appendCurrentPathPoint
                                          }
                                          onClearAll={clearCurrentBehaviorPath}
                                        />
                                        {presentationMode === "overlay" ||
                                        presentationMode === "hybrid" ? (
                                          <IndoorOverlayGeometryEditor
                                            form={markerForm}
                                            name={[
                                              ...activeBehaviorBasePath,
                                              "overlayGeometry",
                                            ]}
                                            currentPoint={{
                                              x: draftX,
                                              y: draftY,
                                            }}
                                            picking={pickMode === "overlay"}
                                            onArmPick={() =>
                                              setPickMode((current) =>
                                                current === "overlay"
                                                  ? "marker"
                                                  : "overlay",
                                              )
                                            }
                                            onAppendCurrentPoint={
                                              appendCurrentOverlayPoint
                                            }
                                            onClearAll={
                                              clearCurrentBehaviorOverlay
                                            }
                                          />
                                        ) : (
                                          <Alert
                                            type="info"
                                            showIcon
                                            message="目前為純標記模式"
                                            description="把上方的呈現模式切換為 overlay 或 hybrid，即可編輯疊加物幾何。"
                                          />
                                        )}
                                      </Space>
                                    ),
                                  },
                                ]}
                              />
                            </Space>
                          ) : (
                            <Alert
                              type="info"
                              showIcon
                              message="尚未建立任何行為檔案"
                              description="先新增一個行為檔案，再為它編排出現條件、觸發鏈、效果與路徑。"
                            />
                          )}
                        </Card>
                      ) : null}
                      <Form.Item label="標籤模板">
                        <Select
                          allowClear
                          placeholder="先套用一組常見詞條，再按需要微調"
                          options={markerTagTemplateOptions.map((item) => ({
                            label: item.label,
                            value: item.label,
                          }))}
                          onChange={(value) => {
                            const template = markerTagTemplateOptions.find(
                              (item) => item.label === value,
                            );
                            if (!template) {
                              return;
                            }
                            markerForm.setFieldValue(
                              "tagsJson",
                              stringifyTagList(template.value),
                            );
                          }}
                        />
                      </Form.Item>
                      <Form.Item label="標籤設定">
                        <Select
                          mode="tags"
                          tokenSeparators={[","]}
                          value={markerTags}
                          options={markerTagPresetOptions}
                          onChange={(values) =>
                            markerForm.setFieldValue(
                              "tagsJson",
                              stringifyTagList(values),
                            )
                          }
                          placeholder="可直接選預設詞條，也可自行輸入新的標籤"
                        />
                      </Form.Item>
                      <Space
                        style={{
                          display: "flex",
                          justifyContent: "space-between",
                          marginBottom: 12,
                        }}
                      >
                        <Text type="secondary">
                          預設情況直接用上面的詞條選擇；只有要輸入特殊 JSON
                          結構時才打開進階編輯。
                        </Text>
                        <Space size={8}>
                          <Text type="secondary">進階 JSON</Text>
                          <Switch
                            checked={showTagJsonEditor}
                            onChange={setShowTagJsonEditor}
                          />
                        </Space>
                      </Space>
                      {showTagJsonEditor ? (
                        <Form.Item label="標籤 JSON（進階）">
                          <Input.TextArea
                            rows={3}
                            value={markerTagJson}
                            onChange={(event) =>
                              markerForm.setFieldValue(
                                "tagsJson",
                                event.target.value,
                              )
                            }
                            placeholder='例如 ["室內","彩蛋"] 或更複雜的標籤結構'
                          />
                        </Form.Item>
                      ) : null}
                      <Form.Item name="tagsJson" hidden>
                        <Input />
                      </Form.Item>
                      <SpatialPopupDisplayField
                        form={markerForm}
                        popupFieldName="popupConfigJson"
                        displayFieldName="displayConfigJson"
                      />
                      <Divider style={{ margin: "8px 0 16px" }} />
                      <Form.Item name="metadataJson" label="額外資料 JSON">
                        <Input.TextArea rows={3} />
                      </Form.Item>
                    </Form>
                  </Card>
                </Col>
                <Col span={24}>
                  <Card size="small" title="目前樓層標記列表">
                    <Table
                      rowKey="id"
                      columns={markerColumns}
                      dataSource={activeFloorNodes}
                      pagination={false}
                      scroll={{ x: 980 }}
                      locale={{ emptyText: "此樓層尚未建立任何節點" }}
                    />
                  </Card>
                </Col>
              </Row>
            ) : floorSwitching || loadingFloor ? (
              <Space
                direction="vertical"
                size={8}
                align="center"
                style={{ width: "100%", padding: "40px 0" }}
              >
                <Tag color="processing">樓層資料載入中</Tag>
                <Text type="secondary">
                  正在切換樓層並同步縮略圖、標記與草稿內容，完成後才會顯示工作台。
                </Text>
              </Space>
            ) : (
              <Empty description="請先選擇樓層再編輯標記" />
            )}
          </Card>

          <Card title="CSV 預檢與確認導入">
            {activeFloor ? (
              <Space
                direction="vertical"
                size="large"
                style={{ width: "100%" }}
              >
                <Alert
                  type="info"
                  showIcon
                  message="先預檢，後寫入"
                  description="CSV 可包含標記代碼、多語名稱、POI 綁定、資源 ID 與 JSON 設定；只有通過預檢後才會真正寫入資料庫。"
                />
                <Row gutter={[16, 16]}>
                  <Col xs={24} xl={16}>
                    <UploadSelector
                      title="拖曳或點擊上傳標記 CSV"
                      description="可先下載範本，整理完畢後再回來匯入。"
                      accept=".csv,text/csv"
                      file={csvFile}
                      uid="csv-upload"
                      onSelect={setCsvFile}
                    />
                  </Col>
                  <Col xs={24} xl={8}>
                    <Card size="small" title="導入操作">
                      <Space direction="vertical" style={{ width: "100%" }}>
                        <Button
                          icon={<FileSearchOutlined />}
                          loading={runningAction === "preview-csv"}
                          onClick={() => void handlePreviewCsv()}
                        >
                          預檢 CSV
                        </Button>
                        <Button
                          type="primary"
                          icon={<CloudUploadOutlined />}
                          loading={runningAction === "confirm-csv"}
                          onClick={() => void handleConfirmCsv()}
                        >
                          確認導入
                        </Button>
                        <Button onClick={downloadCsvTemplate}>
                          下載 CSV 範本
                        </Button>
                      </Space>
                    </Card>
                  </Col>
                </Row>
                {csvPreview ? (
                  <Card
                    size="small"
                    title="CSV 預檢結果"
                    extra={
                      <Space>
                        <Tag color="blue">總筆數 {csvPreview.totalRows}</Tag>
                        <Tag color="green">通過 {csvPreview.validRows}</Tag>
                        <Tag
                          color={csvPreview.invalidRows > 0 ? "red" : "default"}
                        >
                          不合規 {csvPreview.invalidRows}
                        </Tag>
                      </Space>
                    }
                  >
                    <Table
                      rowKey={(record) =>
                        `${record.rowNumber}-${record.markerCode}`
                      }
                      columns={csvColumns}
                      dataSource={csvPreview.rows}
                      pagination={{ pageSize: 8 }}
                      scroll={{ x: 1560 }}
                    />
                  </Card>
                ) : (
                  <Empty description="尚未產生 CSV 預檢結果" />
                )}
              </Space>
            ) : floorSwitching || loadingFloor ? (
              <Space
                direction="vertical"
                size={8}
                align="center"
                style={{ width: "100%", padding: "40px 0" }}
              >
                <Tag color="processing">樓層資料載入中</Tag>
                <Text type="secondary">
                  正在載入當前樓層後再開放 CSV 預檢與確認導入。
                </Text>
              </Space>
            ) : (
              <Empty description="請先選擇樓層再進行 CSV 預檢與導入。" />
            )}
          </Card>
        </>
      )}
    </Space>
  );

  if (embedded) {
    return content;
  }

  return (
    <PageContainer
      title="室內圖資、瓦片與標記編排"
      subTitle="在同一個工作台完成樓層瓦片導入、整圖切片、縮放推導、縮略圖取點與 CSV 預檢 / 確認，直接對齊小程序室內 live runtime。"
      extra={[
        <Button
          key="reload"
          icon={<ReloadOutlined />}
          onClick={() => void Promise.all([loadBuildings(), refreshContext()])}
        >
          重新整理
        </Button>,
        <Button
          key="workspace"
          onClick={() => {
            const params = new URLSearchParams({ tab: "catalog" });
            if (selectedBuildingId) {
              params.set("buildingId", String(selectedBuildingId));
            }
            if (selectedFloorId) {
              params.set("floorId", String(selectedFloorId));
            }
            navigate(`/space/indoor-buildings?${params.toString()}`);
          }}
        >
          返回室內工作台
        </Button>,
      ]}
    >
      {content}
    </PageContainer>
  );
};

export default MapTileManagement;
