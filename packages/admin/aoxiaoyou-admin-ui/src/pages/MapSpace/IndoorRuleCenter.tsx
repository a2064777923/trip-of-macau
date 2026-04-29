import React from "react";
import { PageContainer } from "@ant-design/pro-components";
import {
  App as AntdApp,
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Typography,
} from "antd";
import { useLocation, useNavigate } from "react-router-dom";
import {
  getIndoorBuildingDetail,
  getIndoorBuildings,
  getIndoorRuleBehaviorDetail,
  getIndoorRuleOverview,
  updateIndoorRuleBehaviorStatus,
} from "../../services/api";
import type {
  AdminIndoorBuildingItem,
  AdminIndoorFloorItem,
  AdminIndoorRuleGovernanceDetail,
  AdminIndoorRuleGovernanceItem,
  AdminIndoorRuleOverviewQuery,
} from "../../types/admin";
import IndoorRuleConflictPanel from "../../components/indoor/IndoorRuleConflictPanel";

const { Text } = Typography;

const statusOptions = [
  { label: "全部狀態", value: undefined },
  { label: "草稿", value: "draft" },
  { label: "啟用", value: "enabled" },
  { label: "停用", value: "disabled" },
  { label: "已發佈", value: "published" },
];

const runtimeOptions = [
  { label: "全部支援層級", value: undefined },
  { label: "Phase 15 僅存檔", value: "phase15_storage_only" },
  { label: "Phase 16 規劃中", value: "phase16_planned" },
  { label: "Phase 16 已支援", value: "phase16_supported" },
  { label: "預覽", value: "preview" },
  { label: "未來功能", value: "future_only" },
];

const linkedEntityTypeOptions = [
  { label: "全部綁定類型", value: undefined },
  { label: "任務", value: "task" },
  { label: "活動", value: "activity" },
  { label: "收集物", value: "collectible" },
  { label: "徽章", value: "badge" },
  { label: "章節", value: "chapter" },
  { label: "事件", value: "event" },
];

function getStatusTag(status?: string) {
  switch (status) {
    case "enabled":
      return <Tag color="green">啟用中</Tag>;
    case "published":
      return <Tag color="blue">已發佈</Tag>;
    case "disabled":
      return <Tag>已停用</Tag>;
    default:
      return <Tag color="gold">草稿</Tag>;
  }
}

const IndoorRuleCenter: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<AdminIndoorRuleOverviewQuery>();

  const [filters, setFilters] = React.useState<AdminIndoorRuleOverviewQuery>({
    conflictOnly: false,
    enabledOnly: false,
  });
  const [loading, setLoading] = React.useState(false);
  const [buildingLoading, setBuildingLoading] = React.useState(false);
  const [floorLoading, setFloorLoading] = React.useState(false);
  const [rows, setRows] = React.useState<AdminIndoorRuleGovernanceItem[]>([]);
  const [buildings, setBuildings] = React.useState<AdminIndoorBuildingItem[]>(
    [],
  );
  const [floors, setFloors] = React.useState<AdminIndoorFloorItem[]>([]);
  const [panelOpen, setPanelOpen] = React.useState(false);
  const [panelLoading, setPanelLoading] = React.useState(false);
  const [detail, setDetail] =
    React.useState<AdminIndoorRuleGovernanceDetail | null>(null);
  const overviewRequestRef = React.useRef(0);

  React.useEffect(() => {
    const search = new URLSearchParams(location.search);
    const initialFilters: AdminIndoorRuleOverviewQuery = {
      conflictOnly: false,
      enabledOnly: false,
      buildingId: search.get("buildingId")
        ? Number(search.get("buildingId"))
        : undefined,
      floorId: search.get("floorId")
        ? Number(search.get("floorId"))
        : undefined,
    };
    setFilters(initialFilters);
    form.setFieldsValue(initialFilters);
  }, [form, location.search]);

  const loadOverview = React.useCallback(
    async (nextFilters = filters) => {
      const requestId = ++overviewRequestRef.current;
      setLoading(true);
      try {
        const response = await getIndoorRuleOverview(nextFilters);
        if (requestId !== overviewRequestRef.current) {
          return;
        }
        if (!response.success || !response.data) {
          throw new Error(response.message || "載入互動規則治理中心失敗");
        }
        setRows(response.data);
      } catch (error) {
        if (requestId !== overviewRequestRef.current) {
          return;
        }
        message.error(
          error instanceof Error ? error.message : "載入互動規則治理中心失敗",
        );
      } finally {
        if (requestId === overviewRequestRef.current) {
          setLoading(false);
        }
      }
    },
    [filters, message],
  );

  React.useEffect(() => {
    void loadOverview(filters);
  }, [filters, loadOverview]);

  React.useEffect(() => {
    const bootstrap = async () => {
      setBuildingLoading(true);
      try {
        const response = await getIndoorBuildings({
          pageNum: 1,
          pageSize: 200,
        });
        if (response.success && response.data) {
          setBuildings(response.data.list || []);
        }
      } finally {
        setBuildingLoading(false);
      }
    };
    void bootstrap();
  }, []);

  React.useEffect(() => {
    if (!filters.buildingId) {
      setFloors([]);
      return;
    }
    const loadFloors = async () => {
      setFloorLoading(true);
      try {
        const response = await getIndoorBuildingDetail(
          filters.buildingId as number,
        );
        if (response.success && response.data) {
          setFloors(response.data.floors || []);
        }
      } finally {
        setFloorLoading(false);
      }
    };
    void loadFloors();
  }, [filters.buildingId]);

  const openDetail = async (behaviorId: number) => {
    setPanelOpen(true);
    setPanelLoading(true);
    try {
      const response = await getIndoorRuleBehaviorDetail(behaviorId);
      if (!response.success || !response.data) {
        throw new Error(response.message || "載入規則詳情失敗");
      }
      setDetail(response.data);
    } catch (error) {
      message.error(
        error instanceof Error ? error.message : "載入規則詳情失敗",
      );
      setPanelOpen(false);
    } finally {
      setPanelLoading(false);
    }
  };

  const handleStatusChange = async (status: string) => {
    if (!detail) {
      return;
    }
    const response = await updateIndoorRuleBehaviorStatus(
      detail.behaviorId,
      status,
    );
    if (!response.success || !response.data) {
      message.error(response.message || "更新規則狀態失敗");
      return;
    }
    if (response.data.warnings?.length) {
      message.warning(response.data.warnings.join("；"));
    } else {
      message.success("規則狀態已更新");
    }
    await Promise.all([openDetail(detail.behaviorId), loadOverview()]);
  };

  const openAuthoring = (item?: { buildingId?: number; floorId?: number }) => {
    if (!item?.buildingId || !item?.floorId) {
      return;
    }
    navigate(
      `/space/indoor-buildings?tab=authoring&buildingId=${item.buildingId}&floorId=${item.floorId}`,
    );
  };

  const applyFilters = () => {
    const next = {
      ...form.getFieldsValue(),
      conflictOnly: Boolean(form.getFieldValue("conflictOnly")),
      enabledOnly: Boolean(form.getFieldValue("enabledOnly")),
    } as AdminIndoorRuleOverviewQuery;
    setFilters(next);
  };

  const resetFilters = () => {
    const next = {
      conflictOnly: false,
      enabledOnly: false,
    } satisfies AdminIndoorRuleOverviewQuery;
    form.setFieldsValue(next);
    setFilters(next);
  };

  return (
    <PageContainer
      title="互動規則治理中心"
      subTitle="集中檢視室內地圖的互動規則、衝突分佈、支援層級與狀態，並可直接跳回工作台修正。"
      extra={<Button onClick={() => void loadOverview()}>重新整理</Button>}
    >
      <Space direction="vertical" size="large" style={{ width: "100%" }}>
        <Card>
          <Form form={form} layout="vertical" onFinish={applyFilters}>
            <Row gutter={[16, 12]}>
              <Col xs={24} md={12} xl={6}>
                <Form.Item name="keyword" label="關鍵字">
                  <Input
                    placeholder="搜尋行為名稱、行為代碼或標記代碼"
                    allowClear
                  />
                </Form.Item>
              </Col>
              <Col xs={24} md={12} xl={5}>
                <Form.Item name="buildingId" label="室內建築">
                  <Select
                    allowClear
                    showSearch
                    loading={buildingLoading}
                    placeholder="選擇室內建築"
                    optionFilterProp="label"
                    options={buildings.map((item) => ({
                      label: `${item.nameZht || item.nameZh || item.buildingCode} · ${item.buildingCode}`,
                      value: item.id,
                    }))}
                    onChange={() => {
                      form.setFieldValue("floorId", undefined);
                    }}
                  />
                </Form.Item>
              </Col>
              <Col xs={24} md={12} xl={4}>
                <Form.Item name="floorId" label="樓層">
                  <Select
                    allowClear
                    showSearch
                    loading={floorLoading}
                    placeholder="選擇樓層"
                    optionFilterProp="label"
                    options={floors.map((item) => ({
                      label: `${item.floorNameZht || item.floorNameZh || item.floorCode} · ${item.floorCode || item.floorNumber}`,
                      value: item.id,
                    }))}
                  />
                </Form.Item>
              </Col>
              <Col xs={12} md={6} xl={3}>
                <Form.Item name="cityId" label="城市 ID">
                  <InputNumber
                    style={{ width: "100%" }}
                    placeholder="城市 ID"
                  />
                </Form.Item>
              </Col>
              <Col xs={12} md={6} xl={3}>
                <Form.Item name="relatedPoiId" label="POI ID">
                  <InputNumber style={{ width: "100%" }} placeholder="POI ID" />
                </Form.Item>
              </Col>
              <Col xs={12} md={8} xl={3}>
                <Form.Item name="linkedEntityType" label="綁定類型">
                  <Select
                    allowClear
                    placeholder="綁定類型"
                    options={linkedEntityTypeOptions}
                  />
                </Form.Item>
              </Col>
              <Col xs={12} md={8} xl={3}>
                <Form.Item name="status" label="狀態">
                  <Select options={statusOptions} />
                </Form.Item>
              </Col>
              <Col xs={24} md={8} xl={4}>
                <Form.Item name="runtimeSupportLevel" label="支援層級">
                  <Select options={runtimeOptions} />
                </Form.Item>
              </Col>
              <Col xs={12} md={6} xl={2}>
                <Form.Item
                  name="conflictOnly"
                  label="僅看衝突"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
              <Col xs={12} md={6} xl={2}>
                <Form.Item
                  name="enabledOnly"
                  label="僅看啟用"
                  valuePropName="checked"
                >
                  <Switch />
                </Form.Item>
              </Col>
            </Row>

            <Space wrap>
              <Button type="primary" htmlType="submit">
                套用篩選
              </Button>
              <Button onClick={resetFilters}>重置</Button>
            </Space>
          </Form>
        </Card>

        <Card size="small">
          <Space wrap size={[8, 8]}>
            <Tag color="blue">規則數量 {rows.length}</Tag>
            <Tag color="red">
              含衝突{" "}
              {
                rows.filter((item) => Number(item.conflictCount || 0) > 0)
                  .length
              }
            </Tag>
            <Tag color="green">
              啟用 / 已發佈{" "}
              {
                rows.filter(
                  (item) =>
                    item.status === "enabled" || item.status === "published",
                ).length
              }
            </Tag>
          </Space>
          <Text type="secondary" style={{ display: "block", marginTop: 8 }}>
            下方表格已綁定實際後端資料，建築與樓層選單也來自 8081
            管理後端，不再使用假資料。
          </Text>
        </Card>

        <Table<AdminIndoorRuleGovernanceItem>
          rowKey="behaviorId"
          loading={loading}
          dataSource={rows}
          scroll={{ x: 1200 }}
          locale={{ emptyText: "目前沒有符合條件的互動規則。" }}
          columns={[
            {
              title: "行為名稱",
              dataIndex: "behaviorNameZht",
              width: 260,
              render: (_, record) =>
                record.behaviorNameZht ||
                record.behaviorNameZh ||
                record.behaviorCode ||
                "-",
            },
            { title: "行為代碼", dataIndex: "behaviorCode", width: 180 },
            {
              title: "標記 / 樓層",
              key: "owner",
              width: 180,
              render: (_, record) =>
                `${record.markerCode || "-"} / ${record.floorCode || "-"}`,
            },
            { title: "所屬建築", dataIndex: "buildingNameZht", width: 180 },
            {
              title: "綁定實體",
              key: "linkedEntity",
              width: 180,
              render: (_, record) =>
                record.linkedEntityType
                  ? `${record.linkedEntityType} #${record.linkedEntityId}`
                  : "-",
            },
            {
              title: "規則摘要",
              key: "summary",
              width: 220,
              render: (_, record) => (
                <Space wrap>
                  <Tag>{record.appearanceRuleCount || 0} 出現</Tag>
                  <Tag>{record.triggerRuleCount || 0} 觸發</Tag>
                  <Tag>{record.effectRuleCount || 0} 效果</Tag>
                  <Tag color={record.hasPathGraph ? "cyan" : "default"}>
                    {record.hasPathGraph ? "含路徑" : "無路徑"}
                  </Tag>
                </Space>
              ),
            },
            {
              title: "狀態",
              dataIndex: "status",
              width: 120,
              render: (value) => getStatusTag(value),
            },
            {
              title: "衝突",
              dataIndex: "conflictCount",
              width: 120,
              render: (value) => (
                <Tag color={value ? "red" : "default"}>{value || 0}</Tag>
              ),
            },
            {
              title: "操作",
              key: "action",
              width: 200,
              fixed: "right",
              render: (_, record) => (
                <Space wrap>
                  <Button
                    type="link"
                    size="small"
                    onClick={() => void openDetail(record.behaviorId)}
                  >
                    查看詳情
                  </Button>
                  <Button
                    type="link"
                    size="small"
                    onClick={() => openAuthoring(record)}
                  >
                    前往工作台
                  </Button>
                </Space>
              ),
            },
          ]}
        />
      </Space>

      <IndoorRuleConflictPanel
        open={panelOpen}
        loading={panelLoading}
        detail={detail}
        onClose={() => setPanelOpen(false)}
        onEnable={() => void handleStatusChange("enabled")}
        onDisable={() => void handleStatusChange("disabled")}
        onOpenAuthoring={() => openAuthoring(detail || undefined)}
      />
    </PageContainer>
  );
};

export default IndoorRuleCenter;
