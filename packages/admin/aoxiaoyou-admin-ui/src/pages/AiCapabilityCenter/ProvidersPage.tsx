import React, { useEffect, useMemo, useState } from 'react';
import { useRequest } from 'ahooks';
import { App as AntdApp, Alert, Button, Card, Col, Drawer, Empty, Form, Input, InputNumber, Modal, Row, Select, Space, Switch, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  createAiProvider,
  deleteAiProvider,
  getAiCapabilities,
  getAiProviderSyncJobs,
  getAiProviderTemplates,
  getAiProviders,
  syncAiProviderInventory,
  testAiProvider,
  updateAiProvider,
  type AiProviderItem,
  type AiProviderPayload,
  type AiProviderTemplateItem,
  type AiProviderTestPayload,
} from '../../services/api';
import {
  describeInventorySemantics,
  describeSyncSemantics,
  resolveProviderTruth,
  resolveTemplateForProvider,
} from './providerTruth';

const { Paragraph, Text, Title } = Typography;

function statusColor(value?: string | number) {
  if (value === 1 || value === 'healthy' || value === 'completed' || value === 'success') {
    return 'green';
  }
  if (value === 'warning' || value === 'unknown' || value === 'idle' || value === 'pending') {
    return 'gold';
  }
  if (value === 0 || value === 'failed' || value === 'error') {
    return 'red';
  }
  return 'default';
}

const ProvidersPage: React.FC = () => {
  const { message, modal } = AntdApp.useApp();
  const [form] = Form.useForm<AiProviderPayload & { templateCode?: string; replaceApiKey?: boolean; replaceApiSecret?: boolean }>();
  const [testForm] = Form.useForm<AiProviderTestPayload>();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<AiProviderItem | null>(null);
  const [selectedProvider, setSelectedProvider] = useState<AiProviderItem | null>(null);
  const [syncHistoryProvider, setSyncHistoryProvider] = useState<AiProviderItem | null>(null);

  const templatesReq = useRequest(() => getAiProviderTemplates());
  const providersReq = useRequest(() => getAiProviders());
  const capabilitiesReq = useRequest(() => getAiCapabilities());
  const syncJobsReq = useRequest(
    () => getAiProviderSyncJobs(syncHistoryProvider!.id),
    {
      ready: !!syncHistoryProvider,
      refreshDeps: [syncHistoryProvider?.id],
    },
  );

  const templates = templatesReq.data?.data || [];
  const providers = providersReq.data?.data || [];
  const capabilities = capabilitiesReq.data?.data || [];
  const syncJobs = syncJobsReq.data?.data || [];
  const templateMap = useMemo(
    () =>
      Object.fromEntries(templates.map((template) => [template.platformCode, template])) as Record<
        string,
        AiProviderTemplateItem
      >,
    [templates],
  );
  const witnessProviders = useMemo(
    () =>
      [
        ['travel_qa', 'dashscope-chat'],
        ['admin_image_generation', 'dashscope-image'],
        ['admin_tts_generation', 'dashscope-tts'],
      ]
        .map(([capabilityCode, providerName]) => ({
          capabilityCode,
          provider: providers.find((item) => item.providerName === providerName) || null,
        }))
        .filter((item) => item.provider),
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

  const templateOptions = useMemo(
    () =>
      templates.map((template) => ({
        value: template.platformCode,
        label: template.platformLabel,
      })),
    [templates],
  );

  const providerColumns: ColumnsType<AiProviderItem> = [
    {
      title: '供應商',
      dataIndex: 'displayName',
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Text strong>{item.displayName}</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {item.providerName}
          </Text>
        </Space>
      ),
    },
    {
      title: '平台模板',
      dataIndex: 'platformLabel',
      width: 200,
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Text>{item.platformLabel || item.platformCode || '-'}</Text>
          <Space wrap size={[4, 4]}>
            {item.syncStrategy ? <Tag color="blue">{item.syncStrategy}</Tag> : null}
            {templateMap[item.platformCode || '']?.inventorySemantics ? (
              <Tag>{templateMap[item.platformCode || ''].inventorySemantics}</Tag>
            ) : null}
          </Space>
        </Space>
      ),
    },
    {
      title: '接入真值',
      width: 240,
      render: (_, item) => {
        const template = resolveTemplateForProvider(item, templates);
        const truth = resolveProviderTruth(item, template);
        return (
          <Space direction="vertical" size={0}>
            <Tag color={truth.color}>{truth.code}</Tag>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {truth.summary}
            </Text>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {truth.detail}
            </Text>
          </Space>
        );
      },
    },
    {
      title: '同步語義',
      width: 240,
      render: (_, item) => {
        const template = resolveTemplateForProvider(item, templates);
        const syncDescriptor = describeSyncSemantics(item.syncStrategy || template?.syncStrategy);
        const inventoryDescriptor = describeInventorySemantics(template?.inventorySemantics);
        return (
          <Space direction="vertical" size={0}>
            <Text code>{item.syncStrategy || template?.syncStrategy || 'manual'}</Text>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {syncDescriptor.detail}
            </Text>
            <Text type="secondary" style={{ fontSize: 12 }}>
              {inventoryDescriptor.label}
            </Text>
          </Space>
        );
      },
    },
    {
      title: '健康',
      dataIndex: 'healthStatus',
      width: 160,
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Tag color={statusColor(item.healthStatus)}>{item.healthStatus || 'unknown'}</Tag>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {item.healthMessage || '尚未測試'}
          </Text>
        </Space>
      ),
    },
    {
      title: '能力綁定',
      dataIndex: 'capabilityCodes',
      render: (value) => <Space wrap>{(value || []).map((code: string) => <Tag key={code}>{code}</Tag>)}</Space>,
    },
    {
      title: '庫存',
      width: 130,
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Text>{item.inventoryRecordCount || 0} 筆</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {item.lastInventorySyncStatus || 'idle'}
          </Text>
        </Space>
      ),
    },
    {
      title: '操作',
      width: 270,
      fixed: 'right',
      render: (_, item) => (
        <Space size="small">
          <Button size="small" onClick={() => openDrawer(item)}>
            編輯
          </Button>
          <Button size="small" onClick={() => openTestModal(item)}>
            測試
          </Button>
          <Button size="small" onClick={() => void handleSync(item)}>
            同步
          </Button>
          <Button size="small" onClick={() => setSyncHistoryProvider(item)}>
            記錄
          </Button>
          <Button size="small" danger onClick={() => void handleDelete(item)}>
            刪除
          </Button>
        </Space>
      ),
    },
  ];

  useEffect(() => {
    if (!drawerOpen) {
      form.resetFields();
      return;
    }

    if (editingRecord) {
      form.setFieldsValue({
        templateCode: editingRecord.platformCode,
        providerName: editingRecord.providerName,
        platformCode: editingRecord.platformCode,
        displayName: editingRecord.displayName,
        platformLabel: editingRecord.platformLabel,
        providerType: editingRecord.providerType,
        endpointStyle: editingRecord.endpointStyle,
        syncStrategy: editingRecord.syncStrategy,
        authScheme: editingRecord.authScheme,
        apiBaseUrl: editingRecord.apiBaseUrl,
        docsUrl: editingRecord.docsUrl,
        modelName: editingRecord.modelName,
        capabilityCodes: editingRecord.capabilityCodes,
        featureFlagsJson: editingRecord.featureFlagsJson,
        credentialSchemaJson: editingRecord.credentialSchemaJson,
        providerSettingsJson: editingRecord.providerSettingsJson,
        requestTimeoutMs: editingRecord.requestTimeoutMs,
        maxRetries: editingRecord.maxRetries,
        quotaDaily: editingRecord.quotaDaily,
        costPer1kTokens: editingRecord.costPer1kTokens,
        status: editingRecord.status,
        replaceApiKey: false,
        replaceApiSecret: false,
      });
      return;
    }

    form.setFieldsValue({
      templateCode: undefined,
      providerName: '',
      displayName: '',
      capabilityCodes: [],
      apiBaseUrl: '',
      requestTimeoutMs: 30000,
      maxRetries: 2,
      quotaDaily: 1000,
      costPer1kTokens: 0,
      status: 1,
      replaceApiKey: true,
      replaceApiSecret: false,
    });
  }, [drawerOpen, editingRecord, form]);

  const applyTemplate = (templateCode?: string) => {
    if (!templateCode) {
      return;
    }
    const template = templates.find((item) => item.platformCode === templateCode);
    if (!template) {
      return;
    }
    form.setFieldsValue({
      providerName: template.platformCode,
      platformCode: template.platformCode,
      displayName: template.platformLabel,
      platformLabel: template.platformLabel,
      providerType: template.providerType,
      endpointStyle: template.endpointStyle,
      syncStrategy: template.syncStrategy,
      authScheme: template.authScheme,
      apiBaseUrl: template.defaultBaseUrl,
      docsUrl: template.docsUrl,
      modelName: template.defaultModelName,
      capabilityCodes: form.getFieldValue('capabilityCodes') || [],
    });
  };

  const openDrawer = (record?: AiProviderItem) => {
    setEditingRecord(record || null);
    setDrawerOpen(true);
  };

  const closeDrawer = () => {
    setDrawerOpen(false);
    setEditingRecord(null);
  };

  const openTestModal = (provider: AiProviderItem) => {
    setSelectedProvider(provider);
    testForm.setFieldsValue({
      capabilityCode: provider.capabilityCodes?.[0],
      modelOverride: provider.modelName,
      prompt: '請回覆一段簡短的連通測試訊息，確認模型與供應商均可正常使用。',
    });
  };

  const handleDelete = async (provider: AiProviderItem) => {
    const confirmed = await new Promise<boolean>((resolve) => {
      modal.confirm({
        title: '確認刪除供應商',
        content: `即將刪除「${provider.displayName}」，相關能力路由將需要重新指定供應商。`,
        onOk: async () => resolve(true),
        onCancel: async () => resolve(false),
      });
    });

    if (!confirmed) {
      return;
    }

    const response = await deleteAiProvider(provider.id);
    if (!response.success) {
      message.error(response.message || '刪除供應商失敗');
      return;
    }
    message.success('已刪除供應商');
    void providersReq.refresh();
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload: AiProviderPayload = {
        providerName: values.providerName,
        platformCode: values.platformCode,
        displayName: values.displayName,
        platformLabel: values.platformLabel,
        providerType: values.providerType,
        endpointStyle: values.endpointStyle,
        syncStrategy: values.syncStrategy,
        authScheme: values.authScheme,
        apiBaseUrl: values.apiBaseUrl,
        docsUrl: values.docsUrl,
        modelName: values.modelName,
        capabilityCodes: values.capabilityCodes,
        featureFlagsJson: values.featureFlagsJson,
        credentialSchemaJson: values.credentialSchemaJson,
        providerSettingsJson: values.providerSettingsJson,
        requestTimeoutMs: values.requestTimeoutMs,
        maxRetries: values.maxRetries,
        quotaDaily: values.quotaDaily,
        costPer1kTokens: values.costPer1kTokens,
        status: values.status,
        apiKey: values.replaceApiKey ? values.apiKey : undefined,
        replaceApiKey: !!values.replaceApiKey,
        apiSecret: values.replaceApiSecret ? values.apiSecret : undefined,
        replaceApiSecret: !!values.replaceApiSecret,
      };

      const response = editingRecord
        ? await updateAiProvider(editingRecord.id, payload)
        : await createAiProvider(payload);

      if (!response.success || !response.data) {
        throw new Error(response.message || '儲存供應商失敗');
      }

      message.success(editingRecord ? '已更新供應商' : '已建立供應商');
      closeDrawer();
      void providersReq.refresh();
    } catch (error: any) {
      message.error(error?.message || '儲存供應商失敗');
    }
  };

  const handleSync = async (provider: AiProviderItem) => {
    const response = await syncAiProviderInventory(provider.id);
    if (!response.success || !response.data) {
      message.error(response.message || '同步模型庫失敗');
      return;
    }
    message.success(`已啟動 ${provider.displayName} 的模型庫同步`);
    setSyncHistoryProvider(provider);
    void providersReq.refresh();
    void syncJobsReq.refresh();
  };

  const handleTest = async () => {
    if (!selectedProvider) {
      return;
    }

    try {
      const values = await testForm.validateFields();
      const response = await testAiProvider(selectedProvider.id, values);
      if (!response.success || !response.data) {
        throw new Error(response.message || '供應商連通測試失敗');
      }

      modal.info({
        title: `測試結果：${selectedProvider.displayName}`,
        width: 640,
        content: (
          <Space direction="vertical" size={8}>
            <Text>狀態：{response.data.success === 1 ? '成功' : '失敗'}</Text>
            <Text>模型：{response.data.resolvedModel || '-'}</Text>
            <Text>延遲：{response.data.latencyMs ? `${response.data.latencyMs} ms` : '-'}</Text>
            <Paragraph copyable style={{ marginBottom: 0 }}>
              {response.data.preview || response.data.message || '供應商已回應，但沒有可顯示的預覽。'}
            </Paragraph>
          </Space>
        ),
      });
      setSelectedProvider(null);
      testForm.resetFields();
      void providersReq.refresh();
    } catch (error: any) {
      message.error(error?.message || '供應商連通測試失敗');
    }
  };

  return (
    <>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card style={{ borderRadius: 22 }}>
          <Title level={4} style={{ marginTop: 0 }}>
            供應商與金鑰
          </Title>
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            先選平台模板，再填入 Base URL、金鑰與能力綁定。密鑰只會以遮罩狀態回傳，連通測試與模型庫同步會按供應商策略誠實展示結果。
          </Paragraph>
        </Card>

        {witnessProviders.length ? (
          <Alert
            type="info"
            showIcon
            message="Phase 22 見證路徑"
            description={
              <Space wrap size={[8, 8]}>
                {witnessProviders.map(({ capabilityCode, provider }) => {
                  if (!provider) {
                    return null;
                  }
                  const template = resolveTemplateForProvider(provider, templates);
                  const truth = resolveProviderTruth(provider, template);
                  return (
                    <Tag key={`${capabilityCode}-${provider.providerName}`} color={truth.color}>
                      {capabilityCode} → {provider.providerName} · {truth.code}
                    </Tag>
                  );
                })}
              </Space>
            }
          />
        ) : null}

        <Card title="平台模板" style={{ borderRadius: 22 }}>
          <Row gutter={[16, 16]}>
            {templates.map((template) => (
              <Col xs={24} md={12} xl={8} key={template.platformCode}>
                <Card size="small" style={{ borderRadius: 18, height: '100%' }}>
                  <Space direction="vertical" size={8} style={{ width: '100%' }}>
                    <Space wrap>
                      <Text strong>{template.platformLabel}</Text>
                      <Tag color="blue">{template.syncStrategy}</Tag>
                      {template.inventorySemantics ? <Tag>{template.inventorySemantics}</Tag> : null}
                    </Space>
                    <Text type="secondary">{template.description}</Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {describeSyncSemantics(template.syncStrategy).detail}
                    </Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {describeInventorySemantics(template.inventorySemantics).detail}
                    </Text>
                    <Space wrap>
                      {(template.supportedModalities || []).map((item) => (
                        <Tag key={item}>{item}</Tag>
                      ))}
                    </Space>
                    <Button onClick={() => {
                      openDrawer();
                      setTimeout(() => {
                        form.setFieldValue('templateCode', template.platformCode);
                        applyTemplate(template.platformCode);
                      }, 0);
                    }}>
                      以此模板新增供應商
                    </Button>
                  </Space>
                </Card>
              </Col>
            ))}
          </Row>
        </Card>

        <Card
          title="已接入供應商"
          extra={<Button type="primary" onClick={() => openDrawer()}>新增供應商</Button>}
          style={{ borderRadius: 22 }}
        >
          {providers.length ? (
            <Table
              rowKey="id"
              columns={providerColumns}
              dataSource={providers}
              pagination={false}
              scroll={{ x: 1400 }}
            />
          ) : (
            <Empty description="尚未接入任何供應商。" />
          )}
        </Card>
      </Space>

      <Drawer
        title={editingRecord ? '編輯供應商' : '新增供應商'}
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
          <Form.Item name="templateCode" label="平台模板">
            <Select
              allowClear
              options={templateOptions}
              placeholder="選擇模板後可自動帶入預設欄位"
              onChange={(value) => applyTemplate(value)}
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="providerName"
                label="供應商代碼"
                rules={[{ required: true, message: '請填寫供應商代碼' }]}
              >
                <Input />
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
            <Col span={8}>
              <Form.Item name="platformCode" label="平台代碼">
                <Input />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="providerType" label="供應商類型">
                <Input />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="endpointStyle" label="端點風格">
                <Input />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="apiBaseUrl"
                label="API Base URL"
                rules={[{ required: true, message: '請填寫 API Base URL' }]}
              >
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="docsUrl" label="文件連結">
                <Input />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="syncStrategy" label="同步策略">
                <Input />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="authScheme" label="授權方式">
                <Input />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="modelName" label="預設模型">
                <Input />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="capabilityCodes" label="能力綁定">
            <Select mode="multiple" options={capabilityOptions} />
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="requestTimeoutMs" label="逾時 (ms)">
                <InputNumber min={1000} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="maxRetries" label="重試次數">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="quotaDaily" label="日配額">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="costPer1kTokens" label="估算 US$/1k tokens">
                <InputNumber min={0} step={0.0001} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="狀態">
                <Select options={[{ value: 1, label: '啟用' }, { value: 0, label: '停用' }]} />
              </Form.Item>
            </Col>
          </Row>

          {editingRecord?.apiKeyMasked ? (
            <Alert
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
              message={`目前 API Key：${editingRecord.apiKeyMasked}`}
            />
          ) : null}

          <Form.Item name="replaceApiKey" label="更新 API Key" valuePropName="checked">
            <Switch checkedChildren="更新" unCheckedChildren="保持" />
          </Form.Item>
          <Form.Item shouldUpdate noStyle>
            {() =>
              form.getFieldValue('replaceApiKey') ? (
                <Form.Item
                  name="apiKey"
                  label="API Key"
                  rules={[{ required: !editingRecord, message: '請填寫 API Key' }]}
                >
                  <Input.Password />
                </Form.Item>
              ) : null
            }
          </Form.Item>

          {editingRecord?.apiSecretMasked ? (
            <Alert
              type="info"
              showIcon
              style={{ marginBottom: 16 }}
              message={`目前 API Secret：${editingRecord.apiSecretMasked}`}
            />
          ) : null}

          <Form.Item name="replaceApiSecret" label="更新 API Secret" valuePropName="checked">
            <Switch checkedChildren="更新" unCheckedChildren="保持" />
          </Form.Item>
          <Form.Item shouldUpdate noStyle>
            {() =>
              form.getFieldValue('replaceApiSecret') ? (
                <Form.Item name="apiSecret" label="API Secret">
                  <Input.Password />
                </Form.Item>
              ) : null
            }
          </Form.Item>

          <Form.Item name="featureFlagsJson" label="Feature Flags JSON">
            <Input.TextArea rows={3} placeholder='例如 {"supportsFallback": true}' />
          </Form.Item>
          <Form.Item name="providerSettingsJson" label="進階供應商設定 JSON">
            <Input.TextArea rows={3} placeholder="留給特殊供應商使用的額外設定。" />
          </Form.Item>
        </Form>
      </Drawer>

      <Modal
        open={!!selectedProvider}
        title={selectedProvider ? `連通測試：${selectedProvider.displayName}` : '供應商測試'}
        onCancel={() => {
          setSelectedProvider(null);
          testForm.resetFields();
        }}
        onOk={() => void handleTest()}
      >
        <Form form={testForm} layout="vertical">
          <Form.Item name="capabilityCode" label="能力">
            <Select allowClear options={capabilityOptions} />
          </Form.Item>
          <Form.Item name="modelOverride" label="模型覆寫">
            <Input />
          </Form.Item>
          <Form.Item name="prompt" label="測試提示詞">
            <Input.TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        title={syncHistoryProvider ? `${syncHistoryProvider.displayName} 的同步記錄` : '同步記錄'}
        open={!!syncHistoryProvider}
        width={680}
        onClose={() => setSyncHistoryProvider(null)}
      >
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          {syncJobs.map((job) => (
            <Card key={job.id} size="small" style={{ borderRadius: 16 }}>
              <Space direction="vertical" size={4} style={{ width: '100%' }}>
                <Space wrap>
                  <Text strong>同步任務 #{job.id}</Text>
                  <Tag color={statusColor(job.jobStatus)}>{job.jobStatus}</Tag>
                </Space>
                <Text type="secondary">
                  發現 {job.discoveredCount || 0} / 新增 {job.createdCount || 0} / 更新 {job.updatedCount || 0}
                </Text>
                <Text type="secondary">{job.message || job.errorDetail || '此任務尚未提供詳細訊息。'}</Text>
                <Text type="secondary">
                  {job.startedAt?.replace('T', ' ').slice(0, 19) || '-'} 至{' '}
                  {job.finishedAt?.replace('T', ' ').slice(0, 19) || '進行中'}
                </Text>
              </Space>
            </Card>
          ))}
          {!syncJobs.length ? <Alert type="info" showIcon message="此供應商尚無同步記錄。" /> : null}
        </Space>
      </Drawer>
    </>
  );
};

export default ProvidersPage;
