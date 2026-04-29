import React, { useEffect, useMemo, useState } from 'react';
import { useRequest } from 'ahooks';
import { App as AntdApp, Button, Card, Drawer, Empty, Form, Input, InputNumber, Modal, Row, Col, Select, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  createAiInventory,
  deleteAiInventory,
  getAiCapabilities,
  getAiInventory,
  getAiProviderTemplates,
  getAiProviders,
  updateAiInventory,
  type AiInventoryItem,
  type AiInventoryPayload,
} from '../../services/api';
import {
  describeInventorySemantics,
  describeSyncSemantics,
  resolveProviderTruth,
  resolveTemplateForProvider,
} from './providerTruth';

const { Paragraph, Text, Title } = Typography;

function statusColor(value?: string) {
  if (value === 'available' || value === 'active') {
    return 'green';
  }
  if (value === 'draft' || value === 'preview' || value === 'stale') {
    return 'gold';
  }
  if (value === 'disabled' || value === 'retired') {
    return 'red';
  }
  return 'default';
}

const ModelsPage: React.FC = () => {
  const { message, modal } = AntdApp.useApp();
  const [form] = Form.useForm<AiInventoryPayload>();
  const [providerId, setProviderId] = useState<number | undefined>();
  const [capabilityCode, setCapabilityCode] = useState<string | undefined>();
  const [sourceType, setSourceType] = useState<string | undefined>();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<AiInventoryItem | null>(null);

  const providersReq = useRequest(() => getAiProviders());
  const templatesReq = useRequest(() => getAiProviderTemplates());
  const capabilitiesReq = useRequest(() => getAiCapabilities());
  const inventoryReq = useRequest(
    () => getAiInventory({ providerId, capabilityCode, sourceType }),
    { refreshDeps: [providerId, capabilityCode, sourceType] },
  );

  const providers = providersReq.data?.data || [];
  const templates = templatesReq.data?.data || [];
  const capabilities = capabilitiesReq.data?.data || [];
  const inventory = inventoryReq.data?.data || [];
  const providersById = useMemo(
    () => Object.fromEntries(providers.map((provider) => [provider.id, provider])),
    [providers],
  );

  const columns: ColumnsType<AiInventoryItem> = [
    {
      title: '模型 / 端點',
      dataIndex: 'displayName',
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Text strong>{item.displayName}</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {item.inventoryCode}
          </Text>
        </Space>
      ),
    },
    {
      title: '供應商',
      dataIndex: 'providerDisplayName',
      width: 220,
      render: (_, item) => {
        const provider = providersById[item.providerId];
        const template = provider ? resolveTemplateForProvider(provider, templates) : undefined;
        const truth = provider ? resolveProviderTruth(provider, template) : null;
        return (
          <Space direction="vertical" size={0}>
            <Text>{item.providerDisplayName || item.providerName || '-'}</Text>
            {truth ? <Tag color={truth.color}>{truth.code}</Tag> : null}
          </Space>
        );
      },
    },
    {
      title: '接入語義',
      width: 260,
      render: (_, item) => {
        const provider = providersById[item.providerId];
        const template = provider ? resolveTemplateForProvider(provider, templates) : undefined;
        const syncDescriptor = describeSyncSemantics(item.syncStrategy || provider?.syncStrategy);
        const inventoryDescriptor = describeInventorySemantics(template?.inventorySemantics);
        return (
          <Space direction="vertical" size={0}>
            <Text>{item.inventoryType || '-'}</Text>
            <Text code>{item.syncStrategy || provider?.syncStrategy || 'manual'}</Text>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {inventoryDescriptor.label} / {item.sourceType || 'manual'}
            </Text>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {syncDescriptor.detail}
            </Text>
          </Space>
        );
      },
    },
    {
      title: '能力綁定',
      dataIndex: 'capabilityCodes',
      render: (value) => <Space wrap>{(value || []).map((code: string) => <Tag key={code}>{code}</Tag>)}</Space>,
    },
    {
      title: '模態',
      dataIndex: 'modalityCodes',
      width: 180,
      render: (value) => <Space wrap>{(value || []).map((code: string) => <Tag key={code}>{code}</Tag>)}</Space>,
    },
    {
      title: '狀態',
      dataIndex: 'availabilityStatus',
      width: 120,
      render: (value) => <Tag color={statusColor(value)}>{value || 'unknown'}</Tag>,
    },
    {
      title: '價格',
      width: 160,
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Text type="secondary">輸入 {item.inputPricePer1k != null ? `US$${item.inputPricePer1k}` : '-'}</Text>
          <Text type="secondary">輸出 {item.outputPricePer1k != null ? `US$${item.outputPricePer1k}` : '-'}</Text>
        </Space>
      ),
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      render: (_, item) => (
        <Space size="small">
          <Button size="small" onClick={() => openDrawer(item)}>
            編輯
          </Button>
          <Button size="small" danger onClick={() => void handleDelete(item)}>
            刪除
          </Button>
        </Space>
      ),
    },
  ];

  const providerOptions = useMemo(
    () =>
      providers.map((provider) => ({
        value: provider.id,
        label: provider.displayName,
      })),
    [providers],
  );

  const capabilityOptions = useMemo(
    () =>
      capabilities.map((capability) => ({
        value: capability.capabilityCode,
        label: capability.displayNameZht,
      })),
    [capabilities],
  );

  useEffect(() => {
    if (!drawerOpen) {
      form.resetFields();
      return;
    }

    if (editingRecord) {
      form.setFieldsValue({
        providerId: editingRecord.providerId,
        inventoryCode: editingRecord.inventoryCode,
        externalId: editingRecord.externalId,
        displayName: editingRecord.displayName,
        inventoryType: editingRecord.inventoryType,
        modalityCodes: editingRecord.modalityCodes,
        capabilityCodes: editingRecord.capabilityCodes,
        syncStrategy: editingRecord.syncStrategy,
        sourceType: editingRecord.sourceType,
        availabilityStatus: editingRecord.availabilityStatus,
        endpointPath: editingRecord.endpointPath,
        contextWindowTokens: editingRecord.contextWindowTokens,
        inputPricePer1k: editingRecord.inputPricePer1k,
        outputPricePer1k: editingRecord.outputPricePer1k,
        imagePricePerCall: editingRecord.imagePricePerCall,
        audioPricePerMinute: editingRecord.audioPricePerMinute,
        featureFlagsJson: editingRecord.featureFlagsJson,
        rawPayloadJson: editingRecord.rawPayloadJson,
        isDefault: editingRecord.isDefault,
        sortOrder: editingRecord.sortOrder,
      });
      return;
    }

    form.setFieldsValue({
      availabilityStatus: 'available',
      sourceType: 'manual',
      syncStrategy: 'manual',
      modalityCodes: [],
      capabilityCodes: [],
      isDefault: 0,
      sortOrder: 0,
    });
  }, [drawerOpen, editingRecord, form]);

  const openDrawer = (record?: AiInventoryItem) => {
    setEditingRecord(record || null);
    setDrawerOpen(true);
  };

  const closeDrawer = () => {
    setDrawerOpen(false);
    setEditingRecord(null);
  };

  const handleDelete = async (record: AiInventoryItem) => {
    const confirmed = await new Promise<boolean>((resolve) => {
      modal.confirm({
        title: '確認刪除模型 / 端點',
        content: `即將刪除「${record.displayName}」，此操作會解除與能力路由的關聯。`,
        onOk: async () => resolve(true),
        onCancel: async () => resolve(false),
      });
    });

    if (!confirmed) {
      return;
    }

    const response = await deleteAiInventory(record.id);
    if (!response.success) {
      message.error(response.message || '刪除模型 / 端點失敗');
      return;
    }

    message.success('已刪除模型 / 端點');
    void inventoryReq.refresh();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const response = editingRecord
        ? await updateAiInventory(editingRecord.id, values)
        : await createAiInventory(values);

      if (!response.success || !response.data) {
        throw new Error(response.message || '儲存模型 / 端點失敗');
      }

      message.success(editingRecord ? '已更新模型 / 端點' : '已建立模型 / 端點');
      closeDrawer();
      void inventoryReq.refresh();
    } catch (error: any) {
      message.error(error?.message || '儲存模型 / 端點失敗');
    }
  };

  return (
    <>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card style={{ borderRadius: 22 }}>
          <Title level={4} style={{ marginTop: 0 }}>
            模型與端點庫
          </Title>
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            這裡顯示供應商同步下來的模型或端點，也允許手動補登庫存。Phase 22 會把同步策略與庫存語義拆開誠實呈現，避免把文檔預置庫存誤認成 live 已驗證模型。
          </Paragraph>
        </Card>

        <Card
          title="篩選與管理"
          extra={<Button type="primary" onClick={() => openDrawer()}>新增模型 / 端點</Button>}
          style={{ borderRadius: 22 }}
        >
          <Space wrap size={[12, 12]} style={{ width: '100%' }}>
            <Select
              allowClear
              style={{ width: 220 }}
              placeholder="按供應商篩選"
              options={providerOptions}
              onChange={(value) => setProviderId(value)}
            />
            <Select
              allowClear
              style={{ width: 220 }}
              placeholder="按能力篩選"
              options={capabilityOptions}
              onChange={(value) => setCapabilityCode(value)}
            />
            <Select
              allowClear
              style={{ width: 220 }}
              placeholder="按來源類型篩選"
              options={[
                { value: 'synced', label: 'synced' },
                { value: 'catalog', label: 'catalog' },
                { value: 'endpoint', label: 'endpoint' },
                { value: 'manual', label: 'manual' },
              ]}
              onChange={(value) => setSourceType(value)}
            />
          </Space>

          {inventory.length ? (
            <Table
              rowKey="id"
              columns={columns}
              dataSource={inventory}
              pagination={{
                pageSize: 12,
                showSizeChanger: true,
                pageSizeOptions: ['12', '24', '48'],
                showTotal: (total, range) => `第 ${range[0]}-${range[1]} 項，共 ${total} 項`,
              }}
              scroll={{ x: 1300 }}
              style={{ marginTop: 16 }}
            />
          ) : (
            <Empty
              style={{ marginTop: 24 }}
              description="目前沒有模型 / 端點庫存。請先到供應商頁同步，或手動建立一筆 inventory。"
            />
          )}
        </Card>
      </Space>

      <Drawer
        title={editingRecord ? '編輯模型 / 端點' : '新增模型 / 端點'}
        open={drawerOpen}
        width={760}
        onClose={closeDrawer}
        extra={
          <Space>
            <Button onClick={closeDrawer}>取消</Button>
            <Button type="primary" onClick={() => void handleSubmit()}>
              儲存
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="providerId"
                label="供應商"
                rules={[{ required: true, message: '請選擇供應商' }]}
              >
                <Select options={providerOptions} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="displayName"
                label="顯示名稱"
                rules={[{ required: true, message: '請填寫顯示名稱' }]}
              >
                <Input />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="inventoryCode"
                label="Inventory Code"
                rules={[{ required: true, message: '請填寫 inventory code' }]}
              >
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="externalId"
                label="外部 ID"
                rules={[{ required: true, message: '請填寫外部 ID' }]}
              >
                <Input />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="inventoryType" label="庫存類型">
                <Input placeholder="例如 model / endpoint / deployment" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="syncStrategy" label="同步策略">
                <Input placeholder="例如 list_api / documented_catalog / manual" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="sourceType" label="來源類型">
                <Input placeholder="例如 synced / manual / endpoint" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="capabilityCodes" label="適用能力">
            <Select mode="multiple" options={capabilityOptions} />
          </Form.Item>

          <Form.Item name="modalityCodes" label="支援模態">
            <Select
              mode="multiple"
              options={[
                { value: 'text', label: 'text' },
                { value: 'vision', label: 'vision' },
                { value: 'image', label: 'image' },
                { value: 'audio', label: 'audio' },
              ]}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="availabilityStatus" label="可用狀態">
                <Input placeholder="例如 available / preview / disabled" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="endpointPath" label="端點路徑">
                <Input placeholder="適用於 endpoint 型供應商" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="contextWindowTokens" label="Context Window">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="isDefault" label="預設庫存">
                <Select options={[{ value: 1, label: '是' }, { value: 0, label: '否' }]} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="sortOrder" label="排序">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={6}>
              <Form.Item name="inputPricePer1k" label="輸入 US$/1k">
                <InputNumber min={0} step={0.0001} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="outputPricePer1k" label="輸出 US$/1k">
                <InputNumber min={0} step={0.0001} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="imagePricePerCall" label="圖片 US$/call">
                <InputNumber min={0} step={0.0001} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={6}>
              <Form.Item name="audioPricePerMinute" label="音訊 US$/min">
                <InputNumber min={0} step={0.0001} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="featureFlagsJson" label="Feature Flags JSON">
            <Input.TextArea rows={3} placeholder='例如 {"supportsStructuredOutput": true}' />
          </Form.Item>

          <Form.Item name="rawPayloadJson" label="原始供應商資料 JSON">
            <Input.TextArea rows={4} placeholder="保留同步原文或手動記錄的原始資料。" />
          </Form.Item>
        </Form>
      </Drawer>
    </>
  );
};

export default ModelsPage;
