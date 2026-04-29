import React from "react";
import {
  Alert,
  App as AntdApp,
  Button,
  Col,
  Drawer,
  Empty,
  Form,
  Input,
  Row,
  Select,
  Space,
  Switch,
  Tag,
  Tabs,
} from "antd";
import type {
  AdminIndoorBehaviorProfile,
  AdminIndoorEffectDefinition,
  AdminIndoorFloorItem,
  AdminIndoorMarkerPayload,
  AdminIndoorNodeItem,
  AdminIndoorNodePayload,
  AdminIndoorNodePoint,
  AdminIndoorOverlayGeometry,
  AdminIndoorPathGraph,
  AdminIndoorRuleCondition,
  AdminIndoorRuleValidationResponse,
  AdminIndoorTriggerStep,
} from "../../types/admin";
import { validateIndoorRuleGraph } from "../../services/api";
import IndoorBehaviorRail from "./IndoorBehaviorRail";
import IndoorOverlayGeometryEditor from "./IndoorOverlayGeometryEditor";
import IndoorPathEditor from "./IndoorPathEditor";
import IndoorRuleAppearanceEditor from "./IndoorRuleAppearanceEditor";
import IndoorRuleEffectEditor from "./IndoorRuleEffectEditor";
import IndoorRuleTriggerChainEditor from "./IndoorRuleTriggerChainEditor";
import IndoorRuleValidationSummary from "./IndoorRuleValidationSummary";
import IndoorRuleWorkbenchMapPanel from "./IndoorRuleWorkbenchMapPanel";

interface Props {
  open: boolean;
  floor?: AdminIndoorFloorItem | null;
  nodeId?: number | null;
  nodes?: AdminIndoorNodeItem[];
  initialValues: Partial<AdminIndoorMarkerPayload>;
  basePayload: AdminIndoorNodePayload;
  rewardRuleOptions?: Array<{ value: number; label: string }>;
  rewardRuleLoading?: boolean;
  onClose: () => void;
  onApply: (values: Partial<AdminIndoorMarkerPayload>) => void;
}

const runtimeSupportOptions = [
  { label: "Phase 15 僅存檔", value: "phase15_storage_only" },
  { label: "Phase 16 規劃中", value: "phase16_planned" },
  { label: "Phase 16 已支援", value: "phase16_supported" },
  { label: "預覽", value: "preview" },
  { label: "未來功能", value: "future_only" },
];

const presentationModeOptions = [
  { label: "標記", value: "marker" },
  { label: "疊加物", value: "overlay" },
  { label: "混合", value: "hybrid" },
];

const overlayTypeOptions = [
  { label: "點位", value: "point" },
  { label: "折線", value: "polyline" },
  { label: "多邊形", value: "polygon" },
];

const inheritModeOptions = [
  { label: "覆寫", value: "override" },
  { label: "追加", value: "append" },
  { label: "沿用綁定實體預設", value: "linked_entity_default" },
  { label: "只用綁定實體", value: "linked_entity_only" },
  { label: "手動定義", value: "manual" },
];

const behaviorStatusOptions = [
  { label: "草稿", value: "draft" },
  { label: "啟用", value: "enabled" },
  { label: "停用", value: "disabled" },
  { label: "已發佈", value: "published" },
];

function normalizePointCollection(
  points?: AdminIndoorNodePoint[] | null,
): AdminIndoorNodePoint[] {
  return (points || [])
    .filter((point): point is AdminIndoorNodePoint => Boolean(point))
    .map((point, index) => ({
      x: Number(point.x ?? 0),
      y: Number(point.y ?? 0),
      order: point.order ?? index,
    }));
}

function normalizeOverlayGeometry(
  geometry?: AdminIndoorOverlayGeometry | null,
): AdminIndoorOverlayGeometry | undefined {
  if (!geometry) {
    return undefined;
  }
  const points = normalizePointCollection(geometry.points);
  if (!points.length) {
    return undefined;
  }
  return {
    geometryType: geometry.geometryType || "polygon",
    properties: geometry.properties || undefined,
    points,
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

function buildDefaultBehaviorProfile(index = 0): AdminIndoorBehaviorProfile {
  return {
    behaviorCode: `behavior-${index + 1}`,
    behaviorNameZh: `互动行为 ${index + 1}`,
    behaviorNameZht: `互動行為 ${index + 1}`,
    behaviorNameEn: `Interaction ${index + 1}`,
    behaviorNamePt: `Interacao ${index + 1}`,
    appearancePresetCode: "schedule_window",
    triggerTemplateCode: "tap",
    effectTemplateCode: "popup",
    appearanceRules: [],
    triggerRules: [],
    effectRules: [],
    rewardRuleIds: [],
    pathGraph: {
      points: [],
      durationMs: 1600,
      holdMs: 0,
      loop: false,
      easing: "ease-in-out",
    },
    overlayGeometry: undefined,
    inheritMode: "override",
    runtimeSupportLevel: "phase15_storage_only",
    sortOrder: index,
    status: "draft",
  };
}

function normalizeIdList(ids?: number[] | null): number[] {
  if (!ids?.length) {
    return [];
  }
  return Array.from(
    new Set(
      ids
        .map((id) => Number(id))
        .filter((id) => Number.isFinite(id) && id > 0),
    ),
  );
}

function normalizeBehaviorProfiles(
  profiles?: AdminIndoorBehaviorProfile[] | null,
): AdminIndoorBehaviorProfile[] {
  return (profiles || [])
    .filter(
      (behavior): behavior is AdminIndoorBehaviorProfile =>
        Boolean(behavior) && typeof behavior === "object",
    )
    .map((behavior, index) => {
      const defaults = buildDefaultBehaviorProfile(index);
      return {
        ...defaults,
        ...behavior,
        appearanceRules:
          behavior?.appearanceRules || ([] as AdminIndoorRuleCondition[]),
        triggerRules:
          behavior?.triggerRules || ([] as AdminIndoorTriggerStep[]),
        effectRules:
          behavior?.effectRules || ([] as AdminIndoorEffectDefinition[]),
        rewardRuleIds: normalizeIdList(behavior?.rewardRuleIds),
        pathGraph: behavior?.pathGraph
          ? {
              ...defaults.pathGraph,
              ...behavior.pathGraph,
              points: normalizePointCollection(behavior.pathGraph.points),
            }
          : defaults.pathGraph,
        overlayGeometry: normalizeOverlayGeometry(behavior?.overlayGeometry),
        sortOrder: behavior?.sortOrder ?? index,
      };
    });
}

function cloneValue<T>(source: T): T {
  return JSON.parse(JSON.stringify(source ?? {})) as T;
}

const IndoorRuleWorkbench: React.FC<Props> = ({
  open,
  floor,
  nodeId,
  nodes = [],
  initialValues,
  basePayload,
  rewardRuleOptions = [],
  rewardRuleLoading = false,
  onClose,
  onApply,
}) => {
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<Partial<AdminIndoorMarkerPayload>>();
  const [activeBehaviorIndex, setActiveBehaviorIndex] = React.useState(0);
  const [pickMode, setPickMode] = React.useState<"marker" | "path" | "overlay">(
    "marker",
  );
  const [validating, setValidating] = React.useState(false);
  const [validation, setValidation] =
    React.useState<AdminIndoorRuleValidationResponse | null>(null);

  React.useEffect(() => {
    if (!open) {
      return;
    }
    const nextValues = cloneValue(initialValues);
    const normalized = normalizeBehaviorProfiles(nextValues.behaviors);
    nextValues.behaviors = normalized.length
      ? normalized
      : [buildDefaultBehaviorProfile(0)];
    nextValues.overlayGeometry = normalizeOverlayGeometry(
      nextValues.overlayGeometry,
    );
    form.setFieldsValue(nextValues);
    setActiveBehaviorIndex(0);
    setPickMode("marker");
    setValidation(null);
  }, [form, initialValues, open]);

  const watchOptions = React.useMemo(() => ({ form, preserve: true }), [form]);
  const behaviorProfiles =
    (Form.useWatch("behaviors", watchOptions) as
      | AdminIndoorBehaviorProfile[]
      | undefined) || [];
  const relativeX = Form.useWatch("relativeX", watchOptions) as
    | number
    | undefined;
  const relativeY = Form.useWatch("relativeY", watchOptions) as
    | number
    | undefined;
  const presentationMode = Form.useWatch("presentationMode", watchOptions) as
    | string
    | undefined;

  const normalizedBehaviors = React.useMemo(
    () => normalizeBehaviorProfiles(behaviorProfiles),
    [behaviorProfiles],
  );
  const activeBehavior = normalizedBehaviors[activeBehaviorIndex];
  const rewardRuleLabelMap = React.useMemo(
    () =>
      new Map(
        rewardRuleOptions.map((option) => [option.value, option.label] as const),
      ),
    [rewardRuleOptions],
  );
  const activeBehaviorBasePath = React.useMemo<Array<string | number>>(
    () => ["behaviors", activeBehaviorIndex],
    [activeBehaviorIndex],
  );

  React.useEffect(() => {
    if (!normalizedBehaviors.length) {
      setActiveBehaviorIndex(0);
      return;
    }
    if (activeBehaviorIndex >= normalizedBehaviors.length) {
      setActiveBehaviorIndex(normalizedBehaviors.length - 1);
    }
  }, [activeBehaviorIndex, normalizedBehaviors.length]);

  const setBehaviors = React.useCallback(
    (nextBehaviors: AdminIndoorBehaviorProfile[]) => {
      form.setFieldValue(
        "behaviors",
        nextBehaviors.map((behavior, index) => ({
          ...behavior,
          sortOrder: index,
        })),
      );
    },
    [form],
  );

  const ensureBehaviorForEditing = React.useCallback(() => {
    if (normalizedBehaviors.length) {
      return normalizedBehaviors;
    }
    const next = [buildDefaultBehaviorProfile(0)];
    setBehaviors(next);
    setActiveBehaviorIndex(0);
    return next;
  }, [normalizedBehaviors, setBehaviors]);

  const addBehavior = () => {
    const next = [
      ...normalizedBehaviors,
      buildDefaultBehaviorProfile(normalizedBehaviors.length),
    ];
    setBehaviors(next);
    setActiveBehaviorIndex(next.length - 1);
  };

  const duplicateBehavior = (index: number) => {
    const source = normalizedBehaviors[index];
    if (!source) {
      return;
    }
    const next = [...normalizedBehaviors];
    next.splice(index + 1, 0, {
      ...cloneValue(source),
      behaviorCode: `${source.behaviorCode || `behavior-${index + 1}`}-copy`,
      behaviorNameZh: `${source.behaviorNameZh || source.behaviorNameZht || "互动行为"} 副本`,
      behaviorNameZht: `${source.behaviorNameZht || source.behaviorNameZh || "互動行為"} 副本`,
      behaviorNameEn: `${source.behaviorNameEn || source.behaviorCode || "Interaction"} Copy`,
      behaviorNamePt: `${source.behaviorNamePt || source.behaviorCode || "Interacao"} Copia`,
    });
    setBehaviors(next);
    setActiveBehaviorIndex(index + 1);
  };

  const moveBehavior = (index: number, delta: number) => {
    const target = index + delta;
    if (target < 0 || target >= normalizedBehaviors.length) {
      return;
    }
    const next = [...normalizedBehaviors];
    const [current] = next.splice(index, 1);
    next.splice(target, 0, current);
    setBehaviors(next);
    setActiveBehaviorIndex(target);
  };

  const deleteBehavior = (index: number) => {
    const next = normalizedBehaviors.filter(
      (_, currentIndex) => currentIndex !== index,
    );
    setBehaviors(next);
    setActiveBehaviorIndex(Math.max(0, Math.min(index, next.length - 1)));
  };

  const changeBehaviorStatus = (index: number, status: string) => {
    const next = normalizedBehaviors.map((behavior, currentIndex) =>
      currentIndex === index ? { ...behavior, status } : behavior,
    );
    setBehaviors(next);
  };

  const appendPathPoint = React.useCallback(
    (x: number, y: number) => {
      const currentBehaviors = ensureBehaviorForEditing();
      const next = [...currentBehaviors];
      const active =
        next[activeBehaviorIndex] ||
        buildDefaultBehaviorProfile(activeBehaviorIndex);
      const points = normalizePointCollection(active.pathGraph?.points);
      active.pathGraph = {
        ...active.pathGraph,
        points: [...points, { x, y, order: points.length }],
      };
      next[activeBehaviorIndex] = active;
      setBehaviors(next);
    },
    [activeBehaviorIndex, ensureBehaviorForEditing, setBehaviors],
  );

  const appendOverlayPoint = React.useCallback(
    (x: number, y: number) => {
      const currentBehaviors = ensureBehaviorForEditing();
      const next = [...currentBehaviors];
      const active =
        next[activeBehaviorIndex] ||
        buildDefaultBehaviorProfile(activeBehaviorIndex);
      const rawGeometry = active.overlayGeometry;
      const currentGeometry = normalizeOverlayGeometry(rawGeometry);
      const geometryType =
        rawGeometry?.geometryType || currentGeometry?.geometryType || "polygon";
      const currentPoints = normalizePointCollection(rawGeometry?.points);
      active.overlayGeometry = {
        geometryType,
        properties: rawGeometry?.properties || currentGeometry?.properties,
        points:
          geometryType === "point"
            ? [{ x, y, order: 0 }]
            : [...currentPoints, { x, y, order: currentPoints.length }],
      };
      next[activeBehaviorIndex] = active;
      setBehaviors(next);
    },
    [activeBehaviorIndex, ensureBehaviorForEditing, setBehaviors],
  );

  const handleClearCurrentPath = React.useCallback(() => {
    const currentBehaviors = ensureBehaviorForEditing();
    const next = [...currentBehaviors];
    const active =
      next[activeBehaviorIndex] ||
      buildDefaultBehaviorProfile(activeBehaviorIndex);
    active.pathGraph = {
      ...active.pathGraph,
      points: [],
    };
    next[activeBehaviorIndex] = active;
    setBehaviors(next);
  }, [activeBehaviorIndex, ensureBehaviorForEditing, setBehaviors]);

  const handleClearCurrentOverlay = React.useCallback(() => {
    const currentBehaviors = ensureBehaviorForEditing();
    const next = [...currentBehaviors];
    const active =
      next[activeBehaviorIndex] ||
      buildDefaultBehaviorProfile(activeBehaviorIndex);
    active.overlayGeometry = undefined;
    next[activeBehaviorIndex] = active;
    setBehaviors(next);
  }, [activeBehaviorIndex, ensureBehaviorForEditing, setBehaviors]);

  const handleMapPick = ({ x, y }: { x: number; y: number }) => {
    form.setFieldsValue({ relativeX: x, relativeY: y });
    if (pickMode === "path") {
      appendPathPoint(x, y);
      return;
    }
    if (pickMode === "overlay") {
      appendOverlayPoint(x, y);
    }
  };

  const handleAppendCurrentPathPoint = () => {
    if (relativeX == null || relativeY == null) {
      message.warning("請先在縮略圖上選取座標。");
      return;
    }
    appendPathPoint(relativeX, relativeY);
  };

  const handleAppendCurrentOverlayPoint = () => {
    if (relativeX == null || relativeY == null) {
      message.warning("請先在縮略圖上選取座標。");
      return;
    }
    appendOverlayPoint(relativeX, relativeY);
  };

  const handleApply = async () => {
    try {
      const values = await form.validateFields();
      const draft: Partial<AdminIndoorMarkerPayload> = {
        ...values,
        overlayGeometry: normalizeOverlayGeometry(values.overlayGeometry),
        behaviors: normalizeBehaviorProfiles(values.behaviors),
      };

      const payload: AdminIndoorNodePayload = {
        ...basePayload,
        ...draft,
        nodeNameZh: draft.nodeNameZh || basePayload.nodeNameZh || "",
        behaviors: (draft.behaviors || []).map((behavior, index) => ({
          ...behavior,
          behaviorCode: behavior.behaviorCode || `behavior-${index + 1}`,
          appearanceRules: behavior.appearanceRules || [],
          triggerRules: behavior.triggerRules || [],
          effectRules: behavior.effectRules || [],
          rewardRuleIds: normalizeIdList(behavior.rewardRuleIds),
          pathGraph: serializePathGraph(behavior.pathGraph),
          overlayGeometry: normalizeOverlayGeometry(behavior.overlayGeometry),
          sortOrder: behavior.sortOrder ?? index,
          status: behavior.status || "draft",
        })),
      };

      setValidating(true);
      const response = await validateIndoorRuleGraph(payload, {
        floorId: floor?.id,
        nodeId: nodeId || undefined,
      });

      if (!response.success || !response.data) {
        const failed = {
          valid: false,
          errors: [response.message || "互動規則校驗失敗"],
          warnings: [],
        };
        setValidation(failed);
        message.error(failed.errors[0]);
        return;
      }

      setValidation(response.data);
      if (!response.data.valid) {
        message.error(response.data.errors?.[0] || "互動規則校驗未通過");
        return;
      }

      if (response.data.warnings?.length) {
        message.warning("互動規則已通過校驗，但仍有提醒需要留意。");
      } else {
        message.success("互動規則校驗通過。");
      }

      onApply(draft);
    } catch (error) {
      if (error instanceof Error && error.message) {
        message.error(error.message);
      }
    } finally {
      setValidating(false);
    }
  };

  const floorLabel =
    floor?.floorNameZht || floor?.floorNameZh || floor?.floorCode;

  return (
    <Drawer
      title={floorLabel ? `互動規則工作台 · ${floorLabel}` : "互動規則工作台"}
      placement="right"
      width="100vw"
      open={open}
      onClose={onClose}
      destroyOnHidden
      extra={
        <Space>
          <Button onClick={onClose}>取消</Button>
          <Button
            type="primary"
            loading={validating}
            onClick={() => void handleApply()}
          >
            校驗成功後套用
          </Button>
        </Space>
      }
    >
      <Form form={form} layout="vertical">
        <Space direction="vertical" size="large" style={{ width: "100%" }}>
          <Alert
            type="info"
            showIcon
            message="互動規則獨立工作台"
            description="先在左側建立互動行為，再於中間配置出現條件、觸發鏈、效果、路徑與疊加物。右側縮略圖可直接取點與預覽。"
          />

          <Row gutter={16}>
            <Col xs={24} xl={6}>
              <IndoorBehaviorRail
                behaviors={normalizedBehaviors}
                activeIndex={activeBehaviorIndex}
                onSelect={setActiveBehaviorIndex}
                onAdd={addBehavior}
                onDuplicate={duplicateBehavior}
                onMoveUp={(index) => moveBehavior(index, -1)}
                onMoveDown={(index) => moveBehavior(index, 1)}
                onDelete={deleteBehavior}
                onStatusChange={changeBehaviorStatus}
              />
            </Col>

            <Col xs={24} xl={10}>
              <Space
                direction="vertical"
                size="middle"
                style={{ width: "100%" }}
              >
                <Row gutter={12}>
                  <Col span={12}>
                    <Form.Item name="presentationMode" label="呈現模式">
                      <Select options={presentationModeOptions} />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="overlayType" label="疊加物幾何類型">
                      <Select
                        allowClear
                        disabled={presentationMode === "marker"}
                        options={overlayTypeOptions}
                      />
                    </Form.Item>
                  </Col>
                </Row>

                <Row gutter={12}>
                  <Col span={12}>
                    <Form.Item
                      name="inheritLinkedEntityRules"
                      label="沿用綁定實體規則"
                      valuePropName="checked"
                    >
                      <Switch />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="runtimeSupportLevel" label="整體支援層級">
                      <Select options={runtimeSupportOptions} />
                    </Form.Item>
                  </Col>
                </Row>

                {activeBehavior ? (
                  <>
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[...activeBehaviorBasePath, "behaviorCode"]}
                          label="行為代碼"
                          rules={[
                            { required: true, message: "請填寫行為代碼" },
                          ]}
                        >
                          <Input placeholder="night-overlay" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[...activeBehaviorBasePath, "inheritMode"]}
                          label="繼承模式"
                        >
                          <Select options={inheritModeOptions} />
                        </Form.Item>
                      </Col>
                    </Row>

                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[...activeBehaviorBasePath, "behaviorNameZht"]}
                          label="繁中名稱"
                          rules={[
                            { required: true, message: "請填寫繁中名稱" },
                          ]}
                        >
                          <Input placeholder="夜間巡遊光帶" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[...activeBehaviorBasePath, "behaviorNameZh"]}
                          label="簡中名稱"
                        >
                          <Input placeholder="夜间巡游光带" />
                        </Form.Item>
                      </Col>
                    </Row>

                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[...activeBehaviorBasePath, "behaviorNameEn"]}
                          label="英文名稱"
                        >
                          <Input placeholder="Night Parade Ribbon" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[...activeBehaviorBasePath, "behaviorNamePt"]}
                          label="葡文名稱"
                        >
                          <Input placeholder="Faixa do Desfile Noturno" />
                        </Form.Item>
                      </Col>
                    </Row>

                    <Row gutter={12}>
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
                          <Select options={behaviorStatusOptions} />
                        </Form.Item>
                      </Col>
                    </Row>

                    <Form.Item
                      name={[...activeBehaviorBasePath, "rewardRuleIds"]}
                      label="共享獎勵規則"
                      extra="從室內互動端直接關聯既有獎勵規則，保存時會同步寫入 canonical reward_rule_bindings。"
                    >
                      <Select
                        mode="multiple"
                        allowClear
                        showSearch
                        loading={rewardRuleLoading}
                        optionFilterProp="label"
                        options={rewardRuleOptions}
                        placeholder="選擇要綁定到這條互動行為的獎勵規則"
                      />
                    </Form.Item>
                    {activeBehavior.rewardRuleIds?.length ? (
                      <Space wrap size={[8, 8]}>
                        {activeBehavior.rewardRuleIds.map((ruleId) => (
                          <Tag key={ruleId} color="purple">
                            {rewardRuleLabelMap.get(ruleId) || `規則 #${ruleId}`}
                          </Tag>
                        ))}
                      </Space>
                    ) : null}

                    <Tabs
                      destroyOnHidden
                      items={[
                        {
                          key: "appearance",
                          label: "出現條件",
                          children: (
                            <IndoorRuleAppearanceEditor
                              form={form}
                              basePath={activeBehaviorBasePath}
                            />
                          ),
                        },
                        {
                          key: "triggers",
                          label: "觸發鏈",
                          children: (
                            <IndoorRuleTriggerChainEditor
                              form={form}
                              basePath={activeBehaviorBasePath}
                            />
                          ),
                        },
                        {
                          key: "effects",
                          label: "效果",
                          children: (
                            <IndoorRuleEffectEditor
                              form={form}
                              basePath={activeBehaviorBasePath}
                            />
                          ),
                        },
                        {
                          key: "path",
                          label: "路徑編排",
                          children: (
                            <IndoorPathEditor
                              form={form}
                              basePath={activeBehaviorBasePath}
                              currentPoint={{ x: relativeX, y: relativeY }}
                              picking={pickMode === "path"}
                              onArmPick={() =>
                                setPickMode((current) =>
                                  current === "path" ? "marker" : "path",
                                )
                              }
                              onAppendCurrentPoint={
                                handleAppendCurrentPathPoint
                              }
                              onClearAll={handleClearCurrentPath}
                            />
                          ),
                        },
                        {
                          key: "overlay",
                          label: "疊加物幾何",
                          children: (
                            <IndoorOverlayGeometryEditor
                              form={form}
                              name={[
                                ...activeBehaviorBasePath,
                                "overlayGeometry",
                              ]}
                              currentPoint={{ x: relativeX, y: relativeY }}
                              picking={pickMode === "overlay"}
                              onArmPick={() =>
                                setPickMode((current) =>
                                  current === "overlay" ? "marker" : "overlay",
                                )
                              }
                              onAppendCurrentPoint={
                                handleAppendCurrentOverlayPoint
                              }
                              onClearAll={handleClearCurrentOverlay}
                            />
                          ),
                        },
                      ]}
                    />
                  </>
                ) : (
                  <Empty
                    description="目前沒有互動行為。請先在左側建立一條行為，再開始編排規則。"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                  >
                    <Button type="primary" onClick={addBehavior}>
                      新增互動行為
                    </Button>
                  </Empty>
                )}
              </Space>
            </Col>

            <Col xs={24} xl={8}>
              <Space
                direction="vertical"
                size="middle"
                style={{ width: "100%" }}
              >
                <IndoorRuleWorkbenchMapPanel
                  floor={floor}
                  nodes={nodes}
                  pickMode={pickMode}
                  currentPoint={{ x: relativeX, y: relativeY }}
                  currentPathPoints={activeBehavior?.pathGraph?.points || []}
                  currentOverlayGeometry={activeBehavior?.overlayGeometry}
                  onChangePickMode={setPickMode}
                  onPick={handleMapPick}
                />
                <IndoorRuleValidationSummary validation={validation} />
              </Space>
            </Col>
          </Row>
        </Space>
      </Form>
    </Drawer>
  );
};

export default IndoorRuleWorkbench;

