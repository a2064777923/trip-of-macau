import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import {
  App as AntdApp,
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Table,
  Tabs,
  Tag,
} from 'antd';
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import {
  createAdminRewardPresentation,
  createAdminRewardRule,
  deleteAdminRewardPresentation,
  deleteAdminRewardRule,
  getAdminRewardGovernanceOverview,
  getAdminRewardPresentationDetail,
  getAdminRewardRuleDetail,
  updateAdminRewardPresentation,
  updateAdminRewardRule,
} from '../../services/api';
import type {
  AdminRewardGovernanceOverview,
  AdminRewardPresentationItem,
  AdminRewardRuleItem,
} from '../../types/admin';
import {
  formatRuleSummary,
  presentationTypeOptions,
  renderOwnerTags,
  RewardPresentationBuilderSection,
  RewardRuleBuilderSection,
  ruleTypeOptions,
  SplitRewardStats,
  rewardStatusOptions,
  type RewardPresentationFormValues,
  type RewardRuleFormValues,
  withRewardPresentationDefaults,
  withRewardRuleDefaults,
  buildRewardPresentationPayload,
  buildRewardRulePayload,
} from '../../components/rewards/RewardDomainShared';

const emptyOverview: AdminRewardGovernanceOverview = {
  summary: {},
  rules: [],
  presentations: [],
};

function optionLabel(options: Array<{ label: string; value: string | number }>, value?: string | number | null) {
  return options.find((item) => item.value === value)?.label || value || '-';
}

const RewardRuleCenter: React.FC = () => {
  const { message } = AntdApp.useApp();
  const [ruleForm] = Form.useForm<RewardRuleFormValues>();
  const [presentationForm] = Form.useForm<RewardPresentationFormValues>();
  const [overview, setOverview] = useState<AdminRewardGovernanceOverview>(emptyOverview);
  const [loading, setLoading] = useState(false);
  const [ruleModalOpen, setRuleModalOpen] = useState(false);
  const [presentationModalOpen, setPresentationModalOpen] = useState(false);
  const [ruleSubmitting, setRuleSubmitting] = useState(false);
  const [presentationSubmitting, setPresentationSubmitting] = useState(false);
  const [editingRule, setEditingRule] = useState<AdminRewardRuleItem | null>(null);
  const [editingPresentation, setEditingPresentation] = useState<AdminRewardPresentationItem | null>(null);
  const [ruleKeyword, setRuleKeyword] = useState('');
  const [ruleStatus, setRuleStatus] = useState<string | undefined>();
  const [ruleType, setRuleType] = useState<string | undefined>();
  const [presentationKeyword, setPresentationKeyword] = useState('');
  const [presentationStatus, setPresentationStatus] = useState<string | undefined>();
  const [presentationType, setPresentationType] = useState<string | undefined>();

  const loadOverview = async () => {
    setLoading(true);
    try {
      const response = await getAdminRewardGovernanceOverview();
      if (!response.success || !response.data) {
        throw new Error(response.message || '載入獎勵治理中心失敗');
      }
      setOverview(response.data);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '載入獎勵治理中心失敗');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadOverview();
  }, []);

  const filteredRules = useMemo(() => {
    return (overview.rules || []).filter((item) => {
      if (ruleStatus && item.status !== ruleStatus) {
        return false;
      }
      if (ruleType && item.ruleType !== ruleType) {
        return false;
      }
      if (!ruleKeyword.trim()) {
        return true;
      }
      const keyword = ruleKeyword.trim().toLowerCase();
      return [item.code, item.nameZh, item.nameZht, item.summaryText]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword));
    });
  }, [overview.rules, ruleKeyword, ruleStatus, ruleType]);

  const filteredPresentations = useMemo(() => {
    return (overview.presentations || []).filter((item) => {
      if (presentationStatus && item.status !== presentationStatus) {
        return false;
      }
      if (presentationType && item.presentationType !== presentationType) {
        return false;
      }
      if (!presentationKeyword.trim()) {
        return true;
      }
      const keyword = presentationKeyword.trim().toLowerCase();
      return [item.code, item.nameZh, item.nameZht, item.summaryText]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword));
    });
  }, [overview.presentations, presentationKeyword, presentationStatus, presentationType]);

  const openCreateRuleModal = () => {
    setEditingRule(null);
    ruleForm.resetFields();
    ruleForm.setFieldsValue(withRewardRuleDefaults());
    setRuleModalOpen(true);
  };

  const openEditRuleModal = async (ruleId: number) => {
    const response = await getAdminRewardRuleDetail(ruleId);
    if (!response.success || !response.data) {
      message.error(response.message || '載入規則詳情失敗');
      return;
    }
    setEditingRule(response.data);
    ruleForm.setFieldsValue(withRewardRuleDefaults(response.data));
    setRuleModalOpen(true);
  };

  const submitRule = async () => {
    setRuleSubmitting(true);
    try {
      const values = await ruleForm.validateFields();
      const payload = buildRewardRulePayload(values);
      const response = editingRule
        ? await updateAdminRewardRule(editingRule.id, payload)
        : await createAdminRewardRule(payload);
      if (!response.success) {
        message.error(response.message || '儲存規則失敗');
        return;
      }
      message.success(editingRule ? '規則已更新' : '規則已建立');
      setRuleModalOpen(false);
      await loadOverview();
    } finally {
      setRuleSubmitting(false);
    }
  };

  const openCreatePresentationModal = () => {
    setEditingPresentation(null);
    presentationForm.resetFields();
    presentationForm.setFieldsValue(withRewardPresentationDefaults());
    setPresentationModalOpen(true);
  };

  const openEditPresentationModal = async (presentationId: number) => {
    const response = await getAdminRewardPresentationDetail(presentationId);
    if (!response.success || !response.data) {
      message.error(response.message || '載入演出詳情失敗');
      return;
    }
    setEditingPresentation(response.data);
    presentationForm.setFieldsValue(withRewardPresentationDefaults(response.data));
    setPresentationModalOpen(true);
  };

  const submitPresentation = async () => {
    setPresentationSubmitting(true);
    try {
      const values = await presentationForm.validateFields();
      const payload = buildRewardPresentationPayload(values);
      const response = editingPresentation
        ? await updateAdminRewardPresentation(editingPresentation.id, payload)
        : await createAdminRewardPresentation(payload);
      if (!response.success) {
        message.error(response.message || '儲存演出失敗');
        return;
      }
      message.success(editingPresentation ? '演出已更新' : '演出已建立');
      setPresentationModalOpen(false);
      await loadOverview();
    } finally {
      setPresentationSubmitting(false);
    }
  };

  return (
    <PageContainer
      title="獎勵規則與演出中心"
      subTitle="集中治理共享規則、獲得演出、室內互動同步關係與影響範圍，避免獎勵主體各自分裂配置。"
      extra={
        <Button icon={<ReloadOutlined />} onClick={() => void loadOverview()}>
          重新整理
        </Button>
      }
    >
      <SplitRewardStats summary={overview.summary} />

      <Card size="small" style={{ marginBottom: 24 }}>
        <Space wrap>
          <Tag color="blue">共享規則 {overview.summary?.ruleCount || 0}</Tag>
          <Tag color="purple">獲得演出 {overview.summary?.presentationCount || 0}</Tag>
          <Tag color="cyan">同步室內互動 {overview.summary?.linkedIndoorBehaviorCount || 0}</Tag>
        </Space>
      </Card>

      <Tabs
        items={[
          {
            key: 'rules',
            label: '共享規則',
            children: (
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <Card size="small">
                  <Space wrap>
                    <Input
                      allowClear
                      value={ruleKeyword}
                      onChange={(event) => setRuleKeyword(event.target.value)}
                      placeholder="搜尋規則代碼、名稱或摘要"
                      style={{ width: 260 }}
                    />
                    <Select
                      allowClear
                      value={ruleStatus}
                      onChange={setRuleStatus}
                      placeholder="狀態"
                      style={{ width: 180 }}
                      options={rewardStatusOptions}
                    />
                    <Select
                      allowClear
                      value={ruleType}
                      onChange={setRuleType}
                      placeholder="規則類型"
                      style={{ width: 220 }}
                      options={ruleTypeOptions}
                    />
                    <Button type="primary" icon={<PlusOutlined />} onClick={openCreateRuleModal}>
                      新增共享規則
                    </Button>
                  </Space>
                </Card>

                <Table<AdminRewardRuleItem>
                  rowKey="id"
                  loading={loading}
                  dataSource={filteredRules}
                  pagination={{ pageSize: 8 }}
                  scroll={{ x: 1080 }}
                  columns={[
                    {
                      title: '規則名稱',
                      dataIndex: 'nameZht',
                      width: 240,
                      render: (_, record) => record.nameZht || record.nameZh || record.code,
                    },
                    { title: '代碼', dataIndex: 'code', width: 200 },
                    {
                      title: '類型',
                      dataIndex: 'ruleType',
                      width: 160,
                      render: (value) => <Tag color="blue">{optionLabel(ruleTypeOptions, value)}</Tag>,
                    },
                    {
                      title: '摘要',
                      width: 260,
                      render: (_, record) => formatRuleSummary(record),
                    },
                    {
                      title: '條件群組',
                      width: 140,
                      render: (_, record) => <Tag>{record.conditionGroups?.length || 0} 組</Tag>,
                    },
                    {
                      title: '已綁定主體',
                      width: 260,
                      render: (_, record) => renderOwnerTags(record.linkedOwners),
                    },
                    {
                      title: '狀態',
                      width: 120,
                      render: (_, record) => (
                        <Tag color={record.status === 'published' ? 'green' : 'orange'}>
                          {record.status === 'published' ? '已發佈' : record.status || '編輯中'}
                        </Tag>
                      ),
                    },
                    {
                      title: '操作',
                      width: 160,
                      fixed: 'right',
                      render: (_, record) => (
                        <Space>
                          <a onClick={() => void openEditRuleModal(record.id)}>編輯</a>
                          <Popconfirm
                            title="確定刪除此規則？"
                            onConfirm={async () => {
                              const response = await deleteAdminRewardRule(record.id);
                              if (!response.success) {
                                message.error(response.message || '刪除規則失敗');
                                return;
                              }
                              message.success('規則已刪除');
                              await loadOverview();
                            }}
                          >
                            <a>刪除</a>
                          </Popconfirm>
                        </Space>
                      ),
                    },
                  ]}
                />
              </Space>
            ),
          },
          {
            key: 'presentations',
            label: '獲得演出',
            children: (
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                <Card size="small">
                  <Space wrap>
                    <Input
                      allowClear
                      value={presentationKeyword}
                      onChange={(event) => setPresentationKeyword(event.target.value)}
                      placeholder="搜尋演出代碼、名稱或摘要"
                      style={{ width: 260 }}
                    />
                    <Select
                      allowClear
                      value={presentationStatus}
                      onChange={setPresentationStatus}
                      placeholder="狀態"
                      style={{ width: 180 }}
                      options={rewardStatusOptions}
                    />
                    <Select
                      allowClear
                      value={presentationType}
                      onChange={setPresentationType}
                      placeholder="演出類型"
                      style={{ width: 220 }}
                      options={presentationTypeOptions}
                    />
                    <Button type="primary" icon={<PlusOutlined />} onClick={openCreatePresentationModal}>
                      新增獲得演出
                    </Button>
                  </Space>
                </Card>

                <Table<AdminRewardPresentationItem>
                  rowKey="id"
                  loading={loading}
                  dataSource={filteredPresentations}
                  pagination={{ pageSize: 8 }}
                  scroll={{ x: 1180 }}
                  columns={[
                    {
                      title: '演出名稱',
                      dataIndex: 'nameZht',
                      width: 240,
                      render: (_, record) => record.nameZht || record.nameZh || record.code,
                    },
                    { title: '代碼', dataIndex: 'code', width: 200 },
                    {
                      title: '主演出',
                      dataIndex: 'presentationType',
                      width: 180,
                      render: (value) => <Tag color="purple">{optionLabel(presentationTypeOptions, value)}</Tag>,
                    },
                    {
                      title: '播放策略',
                      width: 280,
                      render: (_, record) => (
                        <Space wrap>
                          <Tag>{record.firstTimeOnly ? '首次限定' : '可重播'}</Tag>
                          <Tag>{record.skippable ? '可跳過' : '不可跳過'}</Tag>
                          <Tag>{record.minimumDisplayMs || 0} ms</Tag>
                          <Tag>{record.queuePolicy || 'enqueue'}</Tag>
                        </Space>
                      ),
                    },
                    {
                      title: '步驟數',
                      width: 120,
                      render: (_, record) => <Tag>{record.steps?.length || 0} 步</Tag>,
                    },
                    {
                      title: '已綁定主體',
                      width: 260,
                      render: (_, record) => renderOwnerTags(record.linkedOwners),
                    },
                    {
                      title: '狀態',
                      width: 120,
                      render: (_, record) => (
                        <Tag color={record.status === 'published' ? 'green' : 'orange'}>
                          {record.status === 'published' ? '已發佈' : record.status || '編輯中'}
                        </Tag>
                      ),
                    },
                    {
                      title: '操作',
                      width: 160,
                      fixed: 'right',
                      render: (_, record) => (
                        <Space>
                          <a onClick={() => void openEditPresentationModal(record.id)}>編輯</a>
                          <Popconfirm
                            title="確定刪除此演出？"
                            onConfirm={async () => {
                              const response = await deleteAdminRewardPresentation(record.id);
                              if (!response.success) {
                                message.error(response.message || '刪除演出失敗');
                                return;
                              }
                              message.success('演出已刪除');
                              await loadOverview();
                            }}
                          >
                            <a>刪除</a>
                          </Popconfirm>
                        </Space>
                      ),
                    },
                  ]}
                />
              </Space>
            ),
          },
        ]}
      />

      <Modal
        title={editingRule ? `編輯共享規則：${editingRule.nameZht || editingRule.nameZh || editingRule.code}` : '新增共享規則'}
        open={ruleModalOpen}
        onOk={submitRule}
        confirmLoading={ruleSubmitting}
        onCancel={() => setRuleModalOpen(false)}
        width={1240}
        destroyOnHidden
      >
        <Form form={ruleForm} layout="vertical">
          <Row gutter={[16, 16]}>
            <Col xs={24} md={10}>
              <Form.Item name="code" label="規則代碼" rules={[{ required: true, message: '請輸入規則代碼' }]}>
                <Input placeholder="rule_macau_story_completion" />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="ruleType" label="規則類型">
                <Select options={ruleTypeOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="status" label="狀態">
                <Select options={rewardStatusOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={4}>
              <Form.Item name="summaryText" label="規則摘要">
                <Input placeholder="簡短描述此規則用途" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="nameZh" label="簡中名稱" rules={[{ required: true, message: '請輸入規則名稱' }]}>
                <Input placeholder="故事線完成規則" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="nameZht" label="繁中名稱">
                <Input placeholder="故事線完成規則" />
              </Form.Item>
            </Col>
          </Row>

          <RewardRuleBuilderSection form={ruleForm} />
        </Form>
      </Modal>

      <Modal
        title={editingPresentation ? `編輯演出：${editingPresentation.nameZht || editingPresentation.nameZh || editingPresentation.code}` : '新增獲得演出'}
        open={presentationModalOpen}
        onOk={submitPresentation}
        confirmLoading={presentationSubmitting}
        onCancel={() => setPresentationModalOpen(false)}
        width={1240}
        destroyOnHidden
      >
        <Form form={presentationForm} layout="vertical">
          <Row gutter={[16, 16]}>
            <Col xs={24} md={8}>
              <Form.Item name="code" label="演出代碼" rules={[{ required: true, message: '請輸入演出代碼' }]}>
                <Input placeholder="presentation_macau_fullscreen_unlock" />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item name="status" label="狀態">
                <Select options={rewardStatusOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="presentationType" label="主演出類型">
                <Select options={presentationTypeOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="priorityWeight" label="優先級">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="nameZh" label="簡中名稱" rules={[{ required: true, message: '請輸入演出名稱' }]}>
                <Input placeholder="全屏解锁演出" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="nameZht" label="繁中名稱">
                <Input placeholder="全屏解鎖演出" />
              </Form.Item>
            </Col>
          </Row>

          <RewardPresentationBuilderSection form={presentationForm} />
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default RewardRuleCenter;

