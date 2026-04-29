import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { type ActionType, type ProColumns } from '@ant-design/pro-table';
import {
  App as AntdApp,
  Button,
  Col,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Tag,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import {
  createAdminRedeemablePrize,
  deleteAdminRedeemablePrize,
  getAdminRedeemablePrizeDetail,
  getAdminRedeemablePrizes,
  updateAdminRedeemablePrize,
} from '../../services/api';
import type { AdminRedeemablePrizeItem } from '../../types/admin';
import {
  buildRedeemablePrizePayload,
  CollectionBindingSection,
  CollectionLocalizedCoreFields,
  CollectionMediaSection,
  fulfillmentModeOptions,
  prizeTypeOptions,
  RedeemablePrizeConfigSection,
  RewardRelationSection,
  rewardStatusOptions,
  type RedeemablePrizeFormValues,
  useRewardDomainReferenceData,
  withRedeemablePrizeDefaults,
} from '../../components/rewards/RewardDomainShared';

function optionLabel(options: Array<{ label: string; value: string | number }>, value?: string | number | null) {
  return options.find((item) => item.value === value)?.label || value || '-';
}

function renderStatus(status?: string) {
  if (status === 'published') {
    return <Tag color="green">已發佈</Tag>;
  }
  if (status === 'archived') {
    return <Tag>已封存</Tag>;
  }
  return <Tag color="orange">編輯中</Tag>;
}

const RedeemablePrizeManagement: React.FC = () => {
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<RedeemablePrizeFormValues>();
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingItem, setEditingItem] = useState<AdminRedeemablePrizeItem | null>(null);
  const references = useRewardDomainReferenceData(form);

  const columns = useMemo<ProColumns<AdminRedeemablePrizeItem>[]>(
    () => [
      {
        title: '名稱',
        dataIndex: 'nameZht',
        width: 260,
        render: (_, record) => record.nameZht || record.nameZh || record.code,
      },
      {
        title: '代碼',
        dataIndex: 'code',
        width: 220,
      },
      {
        title: '物品 / 方式',
        width: 220,
        render: (_, record) => (
          <Space wrap>
            <Tag color="blue">{optionLabel(prizeTypeOptions, record.prizeType)}</Tag>
            <Tag>{optionLabel(fulfillmentModeOptions, record.fulfillmentMode)}</Tag>
          </Space>
        ),
      },
      {
        title: '兌換成本',
        width: 120,
        render: (_, record) => <Tag color="gold">{record.stampCost || 0} 枚</Tag>,
      },
      {
        title: '庫存',
        width: 140,
        render: (_, record) => {
          const total = record.inventoryTotal || 0;
          const redeemed = record.inventoryRedeemed || 0;
          return `${Math.max(total - redeemed, 0)} / ${total}`;
        },
      },
      {
        title: '規則 / 演出',
        width: 220,
        render: (_, record) => (
          <Space wrap>
            <Tag>{record.linkedRules?.length || 0} 條規則</Tag>
            <Tag color="purple">{record.presentation?.presentationType || '未設定演出'}</Tag>
          </Space>
        ),
      },
      {
        title: '狀態',
        dataIndex: 'status',
        width: 120,
        render: (_, record) => renderStatus(record.status),
      },
      {
        title: '操作',
        width: 160,
        valueType: 'option',
        render: (_, record) => [
          <a
            key="edit"
            onClick={async () => {
              const response = await getAdminRedeemablePrizeDetail(record.id);
              if (!response.success || !response.data) {
                message.error(response.message || '載入兌換獎勵詳情失敗');
                return;
              }
              setEditingItem(response.data);
              form.setFieldsValue(withRedeemablePrizeDefaults(response.data));
              setModalOpen(true);
            }}
          >
            編輯
          </a>,
          <Popconfirm
            key="delete"
            title="確定刪除此兌換獎勵？"
            onConfirm={async () => {
              const response = await deleteAdminRedeemablePrize(record.id);
              if (!response.success) {
                message.error(response.message || '刪除失敗');
                return;
              }
              message.success('已刪除兌換獎勵');
              actionRef.current?.reload();
            }}
          >
            <a>刪除</a>
          </Popconfirm>,
        ],
      },
    ],
    [form, message],
  );

  const openCreateModal = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue(withRedeemablePrizeDefaults());
    setModalOpen(true);
  };

  const submitForm = async () => {
    setSubmitting(true);
    try {
      const values = await form.validateFields();
      const payload = buildRedeemablePrizePayload(values);
      const response = editingItem
        ? await updateAdminRedeemablePrize(editingItem.id, payload)
        : await createAdminRedeemablePrize(payload);
      if (!response.success) {
        message.error(response.message || '儲存兌換獎勵失敗');
        return;
      }
      message.success(editingItem ? '兌換獎勵已更新' : '兌換獎勵已建立');
      setModalOpen(false);
      actionRef.current?.reload();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <PageContainer
      title="兌換獎勵物品管理"
      subTitle="管理線下領取、郵寄配送、虛擬發放與券碼類獎勵，並配置共享規則、地圖綁定與獲得演出。"
    >
      <ProTable<AdminRedeemablePrizeItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        search={false}
        request={async (params) => {
          const response = await getAdminRedeemablePrizes({
            pageNum: params.current,
            pageSize: params.pageSize,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        toolBarRender={() => [
          <Button key="create" type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            新增兌換獎勵
          </Button>,
        ]}
      />

      <Modal
        title={editingItem ? `編輯兌換獎勵：${editingItem.nameZht || editingItem.nameZh || editingItem.code}` : '新增兌換獎勵'}
        open={modalOpen}
        onOk={submitForm}
        confirmLoading={submitting}
        onCancel={() => setModalOpen(false)}
        width={1280}
        destroyOnHidden
      >
        <Form form={form} layout="vertical">
          <Row gutter={[16, 16]}>
            <Col xs={24} md={10}>
              <Form.Item
                name="code"
                label="獎勵代碼"
                rules={[{ required: true, message: '請輸入獎勵代碼' }]}
              >
                <Input placeholder="prize_lisboeta_archivist_box" />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="status" label="狀態">
                <Select options={rewardStatusOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={4}>
              <Form.Item name="sortOrder" label="排序">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="publishStartAt" label="上線時間">
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={[16, 16]}>
            <Col xs={24} md={8}>
              <Form.Item name="publishEndAt" label="下線時間">
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <CollectionLocalizedCoreFields
            form={form}
            translationDefaults={references.translationDefaults}
            entityLabel="兌換獎勵"
            includeSubtitle
            includeHighlight
            includeExampleContent={false}
          />

          <RedeemablePrizeConfigSection form={form} />
          <RewardRelationSection form={form} references={references} rewardFamily="redeemable" />
          <CollectionBindingSection form={form} options={references} />
          <CollectionMediaSection attachmentHelp="附件可用於兌換說明、實拍圖集、券碼示例、語音介紹或物流告知。" />
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default RedeemablePrizeManagement;

