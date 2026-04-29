import React, { useEffect, useMemo, useState } from 'react';
import {
  App as AntdApp,
  Button,
  Card,
  Col,
  Collapse,
  Drawer,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Switch,
  Table,
  Tag,
  Tooltip,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  cloneAdminExperienceTemplate,
  createAdminExperienceTemplate,
  getAdminExperienceTemplatePresets,
  getAdminExperienceTemplates,
  getAdminExperienceTemplateUsage,
  updateAdminExperienceTemplate,
} from '../../services/api';
import type {
  AdminExperienceTemplateItem,
  AdminExperienceTemplatePayload,
  AdminExperienceTemplatePreset,
  AdminExperienceTemplateUsage,
} from '../../types/admin';
import { focusFirstInvalidField } from '../../utils/formErrorFeedback';

const { Text, Title, Paragraph } = Typography;

const templateTypeOptions = [
  { label: '展示模板', value: 'presentation' },
  { label: '效果演出', value: 'effect' },
  { label: '觸發效果', value: 'trigger_effect' },
  { label: '互動玩法', value: 'gameplay' },
  { label: '顯示條件', value: 'display_condition' },
  { label: '觸發條件', value: 'trigger_condition' },
  { label: '任務玩法', value: 'task_gameplay' },
  { label: '獎勵演出', value: 'reward_presentation' },
];

const riskOptions = [
  { label: '低風險', value: 'low' },
  { label: '普通', value: 'normal' },
  { label: '高風險', value: 'high' },
  { label: '關鍵', value: 'critical' },
];

const statusOptions = [
  { label: '編輯中', value: 'draft' },
  { label: '已發佈', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const statusColor = (status?: string) => {
  if (status === 'published') return 'green';
  if (status === 'archived') return 'default';
  return 'gold';
};

const riskColor = (risk?: string) => {
  if (risk === 'critical') return 'magenta';
  if (risk === 'high') return 'red';
  if (risk === 'low') return 'cyan';
  return 'blue';
};

const pickName = (item?: AdminExperienceTemplateItem | AdminExperienceTemplatePreset) =>
  item?.nameZht || item?.nameZh || '';

const jsonRule = {
  validator(_: unknown, value?: string) {
    if (!value) return Promise.resolve();
    try {
      const parsed = JSON.parse(value);
      if (!parsed || typeof parsed !== 'object' || !parsed.schemaVersion) {
        return Promise.reject(new Error('JSON 必須是物件並包含 schemaVersion'));
      }
      return Promise.resolve();
    } catch {
      return Promise.reject(new Error('請輸入有效 JSON'));
    }
  },
};

const presetToPayload = (preset: AdminExperienceTemplatePreset): AdminExperienceTemplatePayload => ({
  code: `${preset.presetCode}.copy.${Date.now()}`,
  templateType: preset.templateType,
  category: preset.category,
  nameZh: preset.nameZh,
  nameZht: preset.nameZht || preset.nameZh,
  summaryZh: preset.summaryZh,
  summaryZht: preset.summaryZht || preset.summaryZh,
  configJson: preset.configJson,
  schemaJson: preset.schemaJson,
  riskLevel: preset.riskLevel || 'normal',
  status: 'draft',
  sortOrder: 0,
});

const ExperienceTemplateLibrary: React.FC = () => {
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<AdminExperienceTemplatePayload>();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [presets, setPresets] = useState<AdminExperienceTemplatePreset[]>([]);
  const [templates, setTemplates] = useState<AdminExperienceTemplateItem[]>([]);
  const [editing, setEditing] = useState<AdminExperienceTemplateItem | null>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [jsonPreview, setJsonPreview] = useState<AdminExperienceTemplatePreset | null>(null);
  const [usage, setUsage] = useState<AdminExperienceTemplateUsage | null>(null);
  const [usageOpen, setUsageOpen] = useState(false);
  const [filters, setFilters] = useState({
    keyword: '',
    templateType: undefined as string | undefined,
    category: '',
    riskLevel: undefined as string | undefined,
    status: undefined as string | undefined,
    usedOnly: false,
    highRiskOnly: false,
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const [presetRes, templateRes] = await Promise.all([
        getAdminExperienceTemplatePresets(),
        getAdminExperienceTemplates({ pageNum: 1, pageSize: 300 }),
      ]);
      if (presetRes.success && presetRes.data) setPresets(presetRes.data);
      if (templateRes.success && templateRes.data) setTemplates(templateRes.data.list || []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const groupedPresets = useMemo(() => {
    return presets.reduce<Record<string, AdminExperienceTemplatePreset[]>>((acc, preset) => {
      const key = preset.templateType || 'other';
      acc[key] = acc[key] || [];
      acc[key].push(preset);
      return acc;
    }, {});
  }, [presets]);

  const visibleTemplates = useMemo(() => {
    const keyword = filters.keyword.trim().toLowerCase();
    return templates.filter((template) => {
      if (keyword && !`${template.code} ${template.nameZh} ${template.nameZht} ${template.summaryZh}`.toLowerCase().includes(keyword)) return false;
      if (filters.templateType && template.templateType !== filters.templateType) return false;
      if (filters.category && template.category !== filters.category) return false;
      if (filters.riskLevel && template.riskLevel !== filters.riskLevel) return false;
      if (filters.status && template.status !== filters.status) return false;
      if (filters.usedOnly && !template.usageCount) return false;
      if (filters.highRiskOnly && !['high', 'critical'].includes(template.riskLevel || '')) return false;
      return true;
    });
  }, [templates, filters]);

  const openEditor = (item?: AdminExperienceTemplateItem | AdminExperienceTemplatePreset) => {
    if (item && 'presetCode' in item) {
      setEditing(null);
      form.setFieldsValue(presetToPayload(item));
    } else {
      const templateItem = item as AdminExperienceTemplateItem | undefined;
      setEditing(templateItem || null);
      form.setFieldsValue(templateItem ? { ...templateItem } : {
        templateType: 'presentation',
        riskLevel: 'normal',
        status: 'draft',
        configJson: '{\n  "schemaVersion": 1\n}',
        schemaJson: '{\n  "schemaVersion": 1\n}',
      });
    }
    setDrawerOpen(true);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      const response = editing
        ? await updateAdminExperienceTemplate(editing.id, values)
        : await createAdminExperienceTemplate(values);
      if (!response.success) throw new Error(response.message || '保存模板失敗');
      message.success('模板已保存');
      setDrawerOpen(false);
      setEditing(null);
      await loadData();
    } catch (error) {
      focusFirstInvalidField(form, 'experienceTemplateLibraryForm', error);
      if (error instanceof Error) message.error(error.message);
    } finally {
      setSaving(false);
    }
  };

  const handleClone = async (item: AdminExperienceTemplateItem) => {
    const response = await cloneAdminExperienceTemplate(item.id, {
      code: `${item.code}.copy.${Date.now()}`,
      nameZh: `${pickName(item)} 副本`,
      nameZht: `${pickName(item)} 副本`,
      summaryZh: item.summaryZh,
      summaryZht: item.summaryZht || item.summaryZh,
      status: 'draft',
    });
    if (!response.success) {
      message.error(response.message || '複製模板失敗');
      return;
    }
    message.success('已建立模板副本');
    await loadData();
  };

  const openUsage = async (item: AdminExperienceTemplateItem) => {
    const response = await getAdminExperienceTemplateUsage(item.id);
    if (!response.success || !response.data) {
      message.error(response.message || '讀取使用處失敗');
      return;
    }
    setUsage(response.data);
    setUsageOpen(true);
  };

  const columns: ColumnsType<AdminExperienceTemplateItem> = [
    {
      title: '模板',
      render: (_, record) => (
        <Space direction="vertical" size={2} style={{ width: '100%' }}>
          <Text strong ellipsis={{ tooltip: pickName(record) }}>{pickName(record)}</Text>
          <Tooltip title={record.code}><Text type="secondary" className="experience-code-text">{record.code}</Text></Tooltip>
        </Space>
      ),
    },
    { title: '類型', dataIndex: 'templateType', width: 150, render: (value) => <Tag>{value}</Tag> },
    { title: '分類', dataIndex: 'category', width: 130 },
    { title: '風險', dataIndex: 'riskLevel', width: 90, render: (value) => <Tag color={riskColor(value)}>{value || 'normal'}</Tag> },
    { title: '使用', dataIndex: 'usageCount', width: 80 },
    { title: '狀態', dataIndex: 'status', width: 100, render: (value) => <Tag color={statusColor(value)}>{value}</Tag> },
    {
      title: '操作',
      width: 220,
      render: (_, record) => (
        <Space size={0} wrap>
          <Button type="link" onClick={() => openEditor(record)}>編輯</Button>
          <Button type="link" onClick={() => handleClone(record)}>複製</Button>
          <Button type="link" onClick={() => openUsage(record)}>查看使用處</Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="experience-workbench-shell">
      <Card className="experience-workbench-hero">
        <Title level={3} style={{ marginTop: 0 }}>互動與任務模板庫</Title>
        <Paragraph style={{ marginBottom: 0 }}>
          展示、出現條件、觸發條件、觸發效果、任務玩法與獎勵演出共用模板。操作員先從安全預設開始，再按場景調整結構化欄位；進階 JSON 只作折疊補充。
        </Paragraph>
      </Card>

      <Row gutter={16}>
        <Col xs={24} xl={9}>
          <Card title="預設模板目錄" loading={loading}>
            <Space direction="vertical" style={{ width: '100%' }} size="large">
              {Object.entries(groupedPresets).map(([type, items]) => (
                <div key={type}>
                  <Text strong>{templateTypeOptions.find((option) => option.value === type)?.label || type}</Text>
                  <div className="experience-template-preset-grid" style={{ marginTop: 8 }}>
                    {items.map((preset) => (
                      <Card key={preset.presetCode} size="small" className="experience-template-preset-card">
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Text strong ellipsis={{ tooltip: pickName(preset) }}>{pickName(preset)}</Text>
                          <Tooltip title={preset.presetCode}><Text type="secondary" className="experience-code-text">{preset.presetCode}</Text></Tooltip>
                          <Text type="secondary">{preset.summaryZht || preset.summaryZh}</Text>
                          <Space wrap>
                            <Tag color={riskColor(preset.riskLevel)}>{preset.riskLevel}</Tag>
                            {(preset.recommendedTriggerTypes || []).slice(0, 2).map((trigger) => <Tag key={trigger}>{trigger}</Tag>)}
                          </Space>
                          <Space wrap>
                            <Button size="small" type="primary" onClick={() => openEditor(preset)}>套用為新模板</Button>
                            <Button size="small" onClick={() => setJsonPreview(preset)}>查看 JSON</Button>
                          </Space>
                        </Space>
                      </Card>
                    ))}
                  </div>
                </div>
              ))}
            </Space>
          </Card>
        </Col>
        <Col xs={24} xl={15}>
          <Space direction="vertical" style={{ width: '100%' }} size="middle">
            <Card title="篩選">
              <div className="experience-governance-filter-grid">
                <label className="experience-filter-field"><span className="experience-filter-field-label">關鍵字</span><Input value={filters.keyword} onChange={(event) => setFilters({ ...filters, keyword: event.target.value })} /></label>
                <label className="experience-filter-field"><span className="experience-filter-field-label">模板類型</span><Select allowClear value={filters.templateType} options={templateTypeOptions} onChange={(value) => setFilters({ ...filters, templateType: value })} /></label>
                <label className="experience-filter-field"><span className="experience-filter-field-label">分類</span><Input value={filters.category} onChange={(event) => setFilters({ ...filters, category: event.target.value })} /></label>
                <label className="experience-filter-field"><span className="experience-filter-field-label">風險</span><Select allowClear value={filters.riskLevel} options={riskOptions} onChange={(value) => setFilters({ ...filters, riskLevel: value })} /></label>
                <label className="experience-filter-field"><span className="experience-filter-field-label">狀態</span><Select allowClear value={filters.status} options={statusOptions} onChange={(value) => setFilters({ ...filters, status: value })} /></label>
                <label className="experience-filter-field"><span className="experience-filter-field-label">只看已使用</span><Switch checked={filters.usedOnly} onChange={(checked) => setFilters({ ...filters, usedOnly: checked })} /></label>
                <label className="experience-filter-field"><span className="experience-filter-field-label">只看高風險</span><Switch checked={filters.highRiskOnly} onChange={(checked) => setFilters({ ...filters, highRiskOnly: checked })} /></label>
                <label className="experience-filter-field"><span className="experience-filter-field-label">操作</span><Button onClick={loadData} loading={loading}>重新載入</Button></label>
              </div>
            </Card>
            <Card title="模板列表" extra={<Button type="primary" onClick={() => openEditor()}>新增模板</Button>}>
              <Table rowKey="id" loading={loading} columns={columns} dataSource={visibleTemplates} pagination={{ pageSize: 10 }} />
            </Card>
          </Space>
        </Col>
      </Row>

      <Drawer
        open={drawerOpen}
        title={editing ? '編輯互動與任務模板' : '新增互動與任務模板'}
        width={760}
        forceRender
        onClose={() => setDrawerOpen(false)}
        extra={<Space><Button onClick={() => setDrawerOpen(false)}>取消</Button><Button type="primary" loading={saving} onClick={handleSave}>保存</Button></Space>}
      >
        <Form form={form} name="experienceTemplateLibraryForm" layout="vertical" scrollToFirstError>
          <Title level={5}>基礎資料</Title>
          <Row gutter={12}>
            <Col span={12}><Form.Item label="模板代碼" name="code" rules={[{ required: true }]}><Input /></Form.Item></Col>
            <Col span={12}><Form.Item label="模板類型" name="templateType" rules={[{ required: true }]}><Select options={templateTypeOptions} /></Form.Item></Col>
            <Col span={12}><Form.Item label="分類" name="category"><Input /></Form.Item></Col>
            <Col span={12}><Form.Item label="風險等級" name="riskLevel"><Select options={riskOptions} /></Form.Item></Col>
            <Col span={24}><Form.Item label="繁體名稱" name="nameZh" rules={[{ required: true }]}><Input /></Form.Item></Col>
            <Col span={24}><Form.Item label="繁體摘要" name="summaryZh"><Input.TextArea rows={3} /></Form.Item></Col>
            <Col span={12}><Form.Item label="狀態" name="status"><Select options={statusOptions} /></Form.Item></Col>
            <Col span={12}><Form.Item label="排序" name="sortOrder"><InputNumber style={{ width: '100%' }} /></Form.Item></Col>
          </Row>
          <Title level={5}>結構化配置表單</Title>
          <Row gutter={12}>
            <Col span={12}>
              <Form.Item noStyle shouldUpdate={(prev, next) => prev.category !== next.category}>
                {({ getFieldValue }) => (
                  <Form.Item label="配置摘要">
                    <Input value={getFieldValue('category')} disabled />
                  </Form.Item>
                )}
              </Form.Item>
            </Col>
            <Col span={12}><Form.Item label="schemaVersion"><Input value="1" disabled /></Form.Item></Col>
          </Row>
          <Collapse
            items={[{
              key: 'advanced',
              label: '進階 JSON',
              children: (
                <>
                  <Form.Item label="模板配置 JSON" name="configJson" rules={[jsonRule]}><Input.TextArea rows={8} /></Form.Item>
                  <Form.Item label="表單 Schema JSON" name="schemaJson" rules={[jsonRule]}><Input.TextArea rows={6} /></Form.Item>
                </>
              ),
            }]}
          />
        </Form>
      </Drawer>

      <Drawer open={!!jsonPreview} title="查看 JSON" width={720} onClose={() => setJsonPreview(null)}>
        <pre>{JSON.stringify({ configJson: jsonPreview?.configJson, schemaJson: jsonPreview?.schemaJson }, null, 2)}</pre>
      </Drawer>

      <Drawer open={usageOpen} title="模板使用處" width={760} onClose={() => setUsageOpen(false)}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Text strong>{usage?.templateNameZh} · 使用 {usage?.usageCount || 0} 處</Text>
          <Table
            rowKey={(record) => `${record.flowId}-${record.stepId}`}
            dataSource={usage?.flowStepRefs || []}
            pagination={false}
            columns={[
              { title: '流程', render: (_, record) => `${record.flowNameZh || record.flowCode} #${record.flowId}` },
              { title: '步驟', render: (_, record) => `${record.stepNameZh || record.stepCode} #${record.stepId}` },
              { title: '觸發', dataIndex: 'triggerType' },
              { title: '狀態', dataIndex: 'status', render: (value) => <Tag color={statusColor(value)}>{value}</Tag> },
            ]}
          />
        </Space>
      </Drawer>
      {!drawerOpen && (
        <Form form={form} style={{ display: 'none' }}>
          <Form.Item name="__formConnector" hidden preserve={false}>
            <Input />
          </Form.Item>
        </Form>
      )}
    </div>
  );
};

export default ExperienceTemplateLibrary;
