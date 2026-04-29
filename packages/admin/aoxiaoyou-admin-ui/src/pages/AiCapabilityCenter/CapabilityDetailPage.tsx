import React, { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useRequest } from 'ahooks';
import {
  App as AntdApp,
  Button,
  Card,
  Col,
  Drawer,
  Empty,
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
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  createAiPolicy,
  createAiPromptTemplate,
  createAiQuotaRule,
  deleteAiPolicy,
  deleteAiPromptTemplate,
  deleteAiQuotaRule,
  getAiCapabilities,
  getAiInventory,
  getAiPolicies,
  getAiPromptTemplates,
  getAiProviders,
  getAiQuotaRules,
  type AiPolicyItem,
  type AiPolicyPayload,
  type AiPromptTemplateItem,
  type AiPromptTemplatePayload,
  type AiQuotaRuleItem,
  type AiQuotaRulePayload,
  updateAiPolicy,
  updateAiPromptTemplate,
  updateAiQuotaRule,
} from '../../services/api';
import { getCapabilityCatalogItem, inferCapabilitySummary } from './catalog';

const { Paragraph, Text, Title } = Typography;

type EditorMode = 'policy' | 'quota' | 'template' | null;

function statusColor(value?: string) {
  if (value === 'enabled') {
    return 'green';
  }
  if (value === 'planned' || value === 'draft') {
    return 'gold';
  }
  if (value === 'disabled' || value === 'archived') {
    return 'red';
  }
  return 'default';
}

const CapabilityDetailPage: React.FC = () => {
  const { capabilityCode } = useParams<{ capabilityCode: string }>();
  const { message, modal } = AntdApp.useApp();
  const [editorMode, setEditorMode] = useState<EditorMode>(null);
  const [editingRecord, setEditingRecord] = useState<AiPolicyItem | AiQuotaRuleItem | AiPromptTemplateItem | null>(null);
  const [policyForm] = Form.useForm();
  const [quotaForm] = Form.useForm();
  const [templateForm] = Form.useForm();

  const capabilitiesReq = useRequest(() => getAiCapabilities());
  const policiesReq = useRequest(() => getAiPolicies({ capabilityCode }), {
    ready: !!capabilityCode,
    refreshDeps: [capabilityCode],
  });
  const quotasReq = useRequest(() => getAiQuotaRules({ capabilityCode }), {
    ready: !!capabilityCode,
    refreshDeps: [capabilityCode],
  });
  const templatesReq = useRequest(() => getAiPromptTemplates({ capabilityCode }), {
    ready: !!capabilityCode,
    refreshDeps: [capabilityCode],
  });
  const providersReq = useRequest(() => getAiProviders());
  const inventoryReq = useRequest(() => getAiInventory({ capabilityCode }), {
    ready: !!capabilityCode,
    refreshDeps: [capabilityCode],
  });

  const capability = useMemo(
    () => (capabilitiesReq.data?.data || []).find((item) => item.capabilityCode === capabilityCode),
    [capabilitiesReq.data?.data, capabilityCode],
  );

  const providers = providersReq.data?.data || [];
  const policies = policiesReq.data?.data || [];
  const quotaRules = quotasReq.data?.data || [];
  const promptTemplates = templatesReq.data?.data || [];
  const inventory = inventoryReq.data?.data || [];

  const providerOptions = providers.map((provider) => ({
    value: provider.id,
    label: provider.displayName,
  }));
  const inventoryOptions = inventory.map((item) => ({
    value: item.id,
    label: `${item.displayName} (${item.providerDisplayName || item.providerName || '未命名供應商'})`,
  }));
  const policyOptions = policies.map((item) => ({
    value: item.id,
    label: item.policyName,
  }));

  useEffect(() => {
    if (!editorMode) {
      policyForm.resetFields();
      quotaForm.resetFields();
      templateForm.resetFields();
      return;
    }

    if (editorMode === 'policy') {
      const record = editingRecord as AiPolicyItem | null;
      policyForm.setFieldsValue(
        record
          ? {
              ...record,
              capabilityCode,
              multimodalEnabled: record.multimodalEnabled === 1,
              voiceEnabled: record.voiceEnabled === 1,
              structuredOutputEnabled: record.structuredOutputEnabled === 1,
            }
          : {
              capabilityCode,
              policyType: capability?.supportsAdminCreative ? 'admin_creative' : 'public_runtime',
              executionMode: 'auto',
              responseMode: 'text',
              status: 'enabled',
              sortOrder: 0,
              providerBindings: [],
              multimodalEnabled: false,
              voiceEnabled: false,
              structuredOutputEnabled: true,
            },
      );
    }

    if (editorMode === 'quota') {
      quotaForm.setFieldsValue(
        editingRecord
          ? { ...(editingRecord as AiQuotaRuleItem), capabilityCode }
          : {
              capabilityCode,
              scopeType: 'global',
              windowType: 'hour',
              windowSize: 1,
              actionMode: 'throttle',
              status: 'enabled',
            },
      );
    }

    if (editorMode === 'template') {
      templateForm.setFieldsValue(
        editingRecord
          ? { ...(editingRecord as AiPromptTemplateItem), capabilityCode }
          : {
              capabilityCode,
              templateType: capability?.supportsImage ? 'image' : capability?.supportsAudio ? 'tts' : 'text',
              status: 'enabled',
              sortOrder: 0,
            },
      );
    }
  }, [capability?.supportsAdminCreative, capability?.supportsAudio, capability?.supportsImage, capabilityCode, editingRecord, editorMode, policyForm, quotaForm, templateForm]);

  if (!capabilityCode || !capability) {
    return <Empty description="找不到對應的能力配置頁。" />;
  }

  const catalog = getCapabilityCatalogItem(capabilityCode);

  const policyColumns: ColumnsType<AiPolicyItem> = [
    {
      title: '策略',
      dataIndex: 'policyName',
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Text strong>{item.policyName}</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {item.policyCode}
          </Text>
        </Space>
      ),
    },
    {
      title: '回應模式',
      dataIndex: 'responseMode',
      width: 120,
    },
    {
      title: '主備供應商',
      width: 180,
      render: (_, item) => `${item.providerBindings?.length || 0} 條路由`,
    },
    {
      title: '預設模型',
      dataIndex: 'defaultModel',
      width: 160,
      render: (value) => value || '-',
    },
    {
      title: '狀態',
      dataIndex: 'status',
      width: 120,
      render: (value) => <Tag color={statusColor(value)}>{value || 'unknown'}</Tag>,
    },
    {
      title: '操作',
      width: 150,
      render: (_, item) => (
        <Space size="small">
          <Button size="small" onClick={() => openEditor('policy', item)}>
            編輯
          </Button>
          <Button size="small" danger onClick={() => void handleDelete('policy', item)}>
            刪除
          </Button>
        </Space>
      ),
    },
  ];

  const quotaColumns: ColumnsType<AiQuotaRuleItem> = [
    {
      title: '範圍',
      width: 220,
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Text strong>{item.scopeType || 'global'}</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {item.scopeValue || '全部使用者'}
          </Text>
        </Space>
      ),
    },
    {
      title: '視窗',
      width: 140,
      render: (_, item) => `${item.windowType || 'hour'} / ${item.windowSize || 1}`,
    },
    {
      title: '請求上限',
      dataIndex: 'requestLimit',
      width: 120,
      render: (value) => value ?? '-',
    },
    {
      title: 'Token 上限',
      dataIndex: 'tokenLimit',
      width: 120,
      render: (value) => value ?? '-',
    },
    {
      title: '動作',
      dataIndex: 'actionMode',
      width: 120,
      render: (value) => value || '-',
    },
    {
      title: '狀態',
      dataIndex: 'status',
      width: 120,
      render: (value) => <Tag color={statusColor(value)}>{value || 'unknown'}</Tag>,
    },
    {
      title: '操作',
      width: 150,
      render: (_, item) => (
        <Space size="small">
          <Button size="small" onClick={() => openEditor('quota', item)}>
            編輯
          </Button>
          <Button size="small" danger onClick={() => void handleDelete('quota', item)}>
            刪除
          </Button>
        </Space>
      ),
    },
  ];

  const templateColumns: ColumnsType<AiPromptTemplateItem> = [
    {
      title: '模板',
      dataIndex: 'templateName',
      render: (_, item) => (
        <Space direction="vertical" size={0}>
          <Text strong>{item.templateName}</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {item.templateCode}
          </Text>
        </Space>
      ),
    },
    {
      title: '類型',
      dataIndex: 'templateType',
      width: 110,
    },
    {
      title: '資源槽位',
      dataIndex: 'assetSlotCode',
      width: 130,
      render: (value) => value || '-',
    },
    {
      title: '預設供應商',
      dataIndex: 'defaultProviderName',
      width: 170,
      render: (value) => value || '-',
    },
    {
      title: '狀態',
      dataIndex: 'status',
      width: 120,
      render: (value) => <Tag color={statusColor(value)}>{value || 'unknown'}</Tag>,
    },
    {
      title: '操作',
      width: 150,
      render: (_, item) => (
        <Space size="small">
          <Button size="small" onClick={() => openEditor('template', item)}>
            編輯
          </Button>
          <Button size="small" danger onClick={() => void handleDelete('template', item)}>
            刪除
          </Button>
        </Space>
      ),
    },
  ];

  function openEditor(mode: EditorMode, record?: AiPolicyItem | AiQuotaRuleItem | AiPromptTemplateItem) {
    setEditorMode(mode);
    setEditingRecord(record || null);
  }

  function closeEditor() {
    setEditorMode(null);
    setEditingRecord(null);
  }

  async function handleDelete(mode: Exclude<EditorMode, null>, record: AiPolicyItem | AiQuotaRuleItem | AiPromptTemplateItem) {
    const confirmed = await new Promise<boolean>((resolve) => {
      modal.confirm({
        title: '確認刪除',
        content: `即將刪除「${'policyName' in record ? record.policyName : 'templateName' in record ? record.templateName : record.scopeType || '此規則'}」。`,
        onOk: async () => resolve(true),
        onCancel: async () => resolve(false),
      });
    });

    if (!confirmed) {
      return;
    }

    const response =
      mode === 'policy'
        ? await deleteAiPolicy((record as AiPolicyItem).id)
        : mode === 'quota'
          ? await deleteAiQuotaRule((record as AiQuotaRuleItem).id)
          : await deleteAiPromptTemplate((record as AiPromptTemplateItem).id);

    if (!response.success) {
      message.error(response.message || '刪除失敗');
      return;
    }

    message.success('已刪除');
    void policiesReq.refresh();
    void quotasReq.refresh();
    void templatesReq.refresh();
  }

  async function submitEditor() {
    try {
      if (editorMode === 'policy') {
        const values = await policyForm.validateFields();
        const payload: AiPolicyPayload = {
          capabilityCode,
          ...values,
          multimodalEnabled: values.multimodalEnabled ? 1 : 0,
          voiceEnabled: values.voiceEnabled ? 1 : 0,
          structuredOutputEnabled: values.structuredOutputEnabled ? 1 : 0,
          providerBindings: (values.providerBindings || []).map((binding: any) => ({
            ...binding,
            enabled: binding.enabled === false ? 0 : 1,
          })),
        };
        const response = editingRecord
          ? await updateAiPolicy((editingRecord as AiPolicyItem).id, payload)
          : await createAiPolicy(payload);
        if (!response.success || !response.data) {
          throw new Error(response.message || '儲存策略失敗');
        }
        message.success(editingRecord ? '已更新策略' : '已建立策略');
        closeEditor();
        void policiesReq.refresh();
        return;
      }

      if (editorMode === 'quota') {
        const values = await quotaForm.validateFields();
        const payload: AiQuotaRulePayload = {
          capabilityCode,
          ...values,
        };
        const response = editingRecord
          ? await updateAiQuotaRule((editingRecord as AiQuotaRuleItem).id, payload)
          : await createAiQuotaRule(payload);
        if (!response.success || !response.data) {
          throw new Error(response.message || '儲存配額規則失敗');
        }
        message.success(editingRecord ? '已更新配額規則' : '已建立配額規則');
        closeEditor();
        void quotasReq.refresh();
        return;
      }

      if (editorMode === 'template') {
        const values = await templateForm.validateFields();
        const payload: AiPromptTemplatePayload = {
          capabilityCode,
          ...values,
        };
        const response = editingRecord
          ? await updateAiPromptTemplate((editingRecord as AiPromptTemplateItem).id, payload)
          : await createAiPromptTemplate(payload);
        if (!response.success || !response.data) {
          throw new Error(response.message || '儲存模板失敗');
        }
        message.success(editingRecord ? '已更新提示詞模板' : '已建立提示詞模板');
        closeEditor();
        void templatesReq.refresh();
      }
    } catch (error: any) {
      message.error(error?.message || '儲存失敗');
    }
  }

  return (
    <>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card style={{ borderRadius: 22 }}>
          <Space direction="vertical" size={10} style={{ width: '100%' }}>
            <Space wrap>
              <Title level={3} style={{ margin: 0 }}>
                {capability.displayNameZht}
              </Title>
              <Tag color={statusColor(capability.status)}>{capability.status}</Tag>
            </Space>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              {inferCapabilitySummary(capability.capabilityCode, capability.summaryZht)}
            </Paragraph>
            <Space wrap>
              {catalog?.operatorFocus.map((focus) => (
                <Tag key={focus}>{focus}</Tag>
              ))}
            </Space>
            <Space split={<Text type="secondary">/</Text>}>
              <Text type="secondary">策略 {policies.length}</Text>
              <Text type="secondary">配額規則 {quotaRules.length}</Text>
              <Text type="secondary">模板 {promptTemplates.length}</Text>
              <Text type="secondary">可用庫存 {inventory.length}</Text>
            </Space>
          </Space>
        </Card>

        <Card
          title="路由策略"
          extra={<Button type="primary" onClick={() => openEditor('policy')}>新增策略</Button>}
          style={{ borderRadius: 22 }}
        >
          <Table rowKey="id" columns={policyColumns} dataSource={policies} pagination={false} scroll={{ x: 1100 }} />
        </Card>

        <Card
          title="配額與限流規則"
          extra={<Button type="primary" onClick={() => openEditor('quota')}>新增配額規則</Button>}
          style={{ borderRadius: 22 }}
        >
          <Table rowKey="id" columns={quotaColumns} dataSource={quotaRules} pagination={false} scroll={{ x: 980 }} />
        </Card>

        <Card
          title="提示詞模板"
          extra={<Button type="primary" onClick={() => openEditor('template')}>新增模板</Button>}
          style={{ borderRadius: 22 }}
        >
          <Table rowKey="id" columns={templateColumns} dataSource={promptTemplates} pagination={false} scroll={{ x: 1000 }} />
        </Card>
      </Space>

      <Drawer
        title={editorMode === 'policy' ? '策略編輯器' : editorMode === 'quota' ? '配額規則編輯器' : '提示詞模板編輯器'}
        open={!!editorMode}
        width={820}
        onClose={closeEditor}
        extra={
          <Space>
            <Button onClick={closeEditor}>取消</Button>
            <Button type="primary" onClick={() => void submitEditor()}>
              儲存
            </Button>
          </Space>
        }
      >
        {editorMode === 'policy' ? (
          <Form form={policyForm} layout="vertical">
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="policyCode"
                  label="策略代碼"
                  rules={[{ required: true, message: '請填寫策略代碼' }]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="policyName"
                  label="策略名稱"
                  rules={[{ required: true, message: '請填寫策略名稱' }]}
                >
                  <Input />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="policyType" label="策略類型">
                  <Input />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="executionMode" label="執行模式">
                  <Select options={[{ value: 'auto', label: 'auto' }, { value: 'manual', label: 'manual' }]} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="responseMode" label="回應模式">
                  <Select
                    options={[
                      { value: 'text', label: 'text' },
                      { value: 'structured', label: 'structured' },
                      { value: 'voice', label: 'voice' },
                      { value: 'image', label: 'image' },
                    ]}
                  />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="defaultModel" label="預設模型">
                  <Input />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="temperature" label="Temperature">
                  <InputNumber min={0} max={2} step={0.1} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="maxTokens" label="Max Tokens">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item name="manualSwitchProviderId" label="手動切換供應商">
              <Select allowClear options={providerOptions} />
            </Form.Item>

            <Form.Item name="systemPrompt" label="System Prompt">
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item name="promptTemplate" label="Prompt Template">
              <Input.TextArea rows={4} />
            </Form.Item>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="multimodalEnabled" label="多模態" valuePropName="checked">
                  <Switch checkedChildren="開啟" unCheckedChildren="關閉" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="voiceEnabled" label="語音" valuePropName="checked">
                  <Switch checkedChildren="開啟" unCheckedChildren="關閉" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="structuredOutputEnabled" label="結構化輸出" valuePropName="checked">
                  <Switch checkedChildren="開啟" unCheckedChildren="關閉" />
                </Form.Item>
              </Col>
            </Row>

            <Form.List name="providerBindings">
              {(fields, { add, remove }) => (
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  <Title level={5} style={{ marginBottom: 0 }}>
                    主備援供應商路由
                  </Title>
                  {fields.map((field) => (
                    <Card key={field.key} size="small" style={{ borderRadius: 16 }}>
                      <Row gutter={12}>
                        <Col span={8}>
                          <Form.Item {...field} name={[field.name, 'providerId']} label="供應商" rules={[{ required: true, message: '請選擇供應商' }]}>
                            <Select options={providerOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={8}>
                          <Form.Item {...field} name={[field.name, 'inventoryId']} label="模型 / 端點">
                            <Select allowClear options={inventoryOptions} />
                          </Form.Item>
                        </Col>
                        <Col span={4}>
                          <Form.Item {...field} name={[field.name, 'bindingRole']} label="角色">
                            <Select options={[{ value: 'primary', label: 'primary' }, { value: 'fallback', label: 'fallback' }]} />
                          </Form.Item>
                        </Col>
                        <Col span={4}>
                          <Form.Item {...field} name={[field.name, 'enabled']} label="啟用">
                            <Select options={[{ value: 1, label: '是' }, { value: 0, label: '否' }]} />
                          </Form.Item>
                        </Col>
                      </Row>
                      <Row gutter={12}>
                        <Col span={6}>
                          <Form.Item {...field} name={[field.name, 'routeMode']} label="路由模式">
                            <Input />
                          </Form.Item>
                        </Col>
                        <Col span={6}>
                          <Form.Item {...field} name={[field.name, 'modelOverride']} label="模型覆寫">
                            <Input />
                          </Form.Item>
                        </Col>
                        <Col span={4}>
                          <Form.Item {...field} name={[field.name, 'sortOrder']} label="排序">
                            <InputNumber min={0} style={{ width: '100%' }} />
                          </Form.Item>
                        </Col>
                        <Col span={4}>
                          <Form.Item {...field} name={[field.name, 'weightPercent']} label="權重">
                            <InputNumber min={0} max={100} style={{ width: '100%' }} />
                          </Form.Item>
                        </Col>
                        <Col span={4}>
                          <Button danger onClick={() => remove(field.name)}>
                            移除
                          </Button>
                        </Col>
                      </Row>
                    </Card>
                  ))}
                  <Button onClick={() => add({ enabled: 1, bindingRole: 'primary', routeMode: 'primary', sortOrder: 0 })}>
                    新增路由綁定
                  </Button>
                </Space>
              )}
            </Form.List>

            <Form.Item name="parameterConfigJson" label="結構化參數 JSON">
              <Input.TextArea rows={3} placeholder='例如 {"topP":0.9,"responseFormat":"json"}' />
            </Form.Item>
            <Form.Item name="responseSchemaJson" label="Response Schema JSON">
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item name="postProcessRulesJson" label="後處理規則 JSON">
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item name="expertOverrideJson" label="進階覆寫 JSON">
              <Input.TextArea rows={3} placeholder="僅供專家模式使用。" />
            </Form.Item>
            <Form.Item name="notes" label="備註">
              <Input.TextArea rows={2} />
            </Form.Item>
            <Form.Item name="status" label="狀態">
              <Select options={[{ value: 'enabled', label: 'enabled' }, { value: 'disabled', label: 'disabled' }]} />
            </Form.Item>
          </Form>
        ) : null}

        {editorMode === 'quota' ? (
          <Form form={quotaForm} layout="vertical">
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="policyId" label="關聯策略">
                  <Select allowClear options={policyOptions} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="status" label="狀態">
                  <Select options={[{ value: 'enabled', label: 'enabled' }, { value: 'disabled', label: 'disabled' }]} />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="scopeType" label="範圍類型">
                  <Select
                    options={[
                      { value: 'global', label: 'global' },
                      { value: 'role', label: 'role' },
                      { value: 'user_group', label: 'user_group' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="scopeValue" label="範圍值">
                  <Input />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="windowType" label="視窗類型">
                  <Select
                    options={[
                      { value: 'minute', label: 'minute' },
                      { value: 'hour', label: 'hour' },
                      { value: 'day', label: 'day' },
                    ]}
                  />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="windowSize" label="視窗大小">
                  <InputNumber min={1} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="requestLimit" label="請求上限">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="tokenLimit" label="Token 上限">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="suspiciousConcurrencyThreshold" label="可疑並發門檻">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="actionMode" label="動作模式">
                  <Select options={[{ value: 'throttle', label: 'throttle' }, { value: 'block', label: 'block' }, { value: 'flag', label: 'flag' }]} />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item name="notes" label="備註">
              <Input.TextArea rows={3} />
            </Form.Item>
          </Form>
        ) : null}

        {editorMode === 'template' ? (
          <Form form={templateForm} layout="vertical">
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item
                  name="templateCode"
                  label="模板代碼"
                  rules={[{ required: true, message: '請填寫模板代碼' }]}
                >
                  <Input />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item
                  name="templateName"
                  label="模板名稱"
                  rules={[{ required: true, message: '請填寫模板名稱' }]}
                >
                  <Input />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item name="templateType" label="模板類型">
                  <Select options={[{ value: 'text', label: 'text' }, { value: 'image', label: 'image' }, { value: 'tts', label: 'tts' }]} />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="assetSlotCode" label="資源槽位">
                  <Input />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item name="sortOrder" label="排序">
                  <InputNumber min={0} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item name="systemPrompt" label="System Prompt">
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item
              name="promptTemplate"
              label="Prompt Template"
              rules={[{ required: true, message: '請填寫 Prompt Template' }]}
            >
              <Input.TextArea rows={5} />
            </Form.Item>

            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="defaultProviderId" label="預設供應商">
                  <Select allowClear options={providerOptions} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="defaultPolicyId" label="預設策略">
                  <Select allowClear options={policyOptions} />
                </Form.Item>
              </Col>
            </Row>

            <Form.Item name="variableSchemaJson" label="變量 Schema JSON">
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item name="outputConstraintsJson" label="輸出約束 JSON">
              <Input.TextArea rows={3} />
            </Form.Item>
            <Form.Item name="status" label="狀態">
              <Select options={[{ value: 'enabled', label: 'enabled' }, { value: 'disabled', label: 'disabled' }]} />
            </Form.Item>
          </Form>
        ) : null}
      </Drawer>
    </>
  );
};

export default CapabilityDetailPage;
