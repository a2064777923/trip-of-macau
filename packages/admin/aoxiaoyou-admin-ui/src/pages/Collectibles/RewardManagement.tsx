import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { type ActionType, type ProColumns } from '@ant-design/pro-table';
import { Button, DatePicker, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Tag, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import dayjs, { type Dayjs } from 'dayjs';
import type { AdminRewardItem } from '../../types/admin';
import {
  createAdminReward,
  deleteAdminReward,
  getAdminRewards,
  updateAdminReward,
} from '../../services/api';
import {
  CollectionBehaviorSection,
  CollectionBindingSection,
  CollectionLocalizedCoreFields,
  CollectionMediaSection,
  applyCollectionLocaleFallback,
  useCollectionAuthoringOptions,
} from './CollectionAuthoringShared';

interface RewardFormValues extends Omit<Partial<AdminRewardItem>, 'publishStartAt' | 'publishEndAt'> {
  publishStartAt?: Dayjs | null;
  publishEndAt?: Dayjs | null;
}

const statusOptions = [
  { value: 'draft', label: '編輯中' },
  { value: 'published', label: '已發佈' },
  { value: 'archived', label: '已封存' },
];

function renderStatus(status?: string) {
  if (status === 'published') {
    return <Tag color="green">已發佈</Tag>;
  }
  if (status === 'archived') {
    return <Tag>已封存</Tag>;
  }
  return <Tag color="orange">編輯中</Tag>;
}

function withRewardDefaults(detail?: Partial<AdminRewardItem>): RewardFormValues {
  const next: RewardFormValues = {
    stampCost: 0,
    inventoryTotal: 0,
    inventoryRedeemed: 0,
    sortOrder: 0,
    status: 'draft',
    popupPresetCode: 'reward-modal',
    displayPresetCode: 'inventory-card',
    triggerPresetCode: 'reward-redemption',
    storylineBindings: [],
    cityBindings: [],
    subMapBindings: [],
    indoorBuildingBindings: [],
    indoorFloorBindings: [],
    attachmentAssetIds: [],
    ...detail,
    publishStartAt: detail?.publishStartAt ? dayjs(detail.publishStartAt) : undefined,
    publishEndAt: detail?.publishEndAt ? dayjs(detail.publishEndAt) : undefined,
  };
  next.storylineBindings = detail?.storylineBindings || [];
  next.cityBindings = detail?.cityBindings || [];
  next.subMapBindings = detail?.subMapBindings || [];
  next.indoorBuildingBindings = detail?.indoorBuildingBindings || [];
  next.indoorFloorBindings = detail?.indoorFloorBindings || [];
  next.attachmentAssetIds = detail?.attachmentAssetIds || [];
  return next;
}

function buildPayload(values: RewardFormValues): Partial<AdminRewardItem> {
  const normalized = applyCollectionLocaleFallback(values, ['name', 'subtitle', 'description', 'highlight', 'exampleContent']);
  return {
    ...normalized,
    nameZh: normalized.nameZh || normalized.nameZht || '',
    publishStartAt: normalized.publishStartAt ? normalized.publishStartAt.format('YYYY-MM-DDTHH:mm:ss') : null,
    publishEndAt: normalized.publishEndAt ? normalized.publishEndAt.format('YYYY-MM-DDTHH:mm:ss') : null,
    storylineBindings: normalized.storylineBindings || [],
    cityBindings: normalized.cityBindings || [],
    subMapBindings: normalized.subMapBindings || [],
    indoorBuildingBindings: normalized.indoorBuildingBindings || [],
    indoorFloorBindings: normalized.indoorFloorBindings || [],
    attachmentAssetIds: normalized.attachmentAssetIds || [],
  };
}

const RewardManagement: React.FC = () => {
  const [form] = Form.useForm<RewardFormValues>();
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<AdminRewardItem | null>(null);
  const options = useCollectionAuthoringOptions(form);

  const columns = useMemo<ProColumns<AdminRewardItem>[]>(() => [
    { title: '代碼', dataIndex: 'code', width: 180 },
    { title: '名稱', dataIndex: 'nameZht', width: 220, render: (_, record) => record.nameZht || record.nameZh },
    {
      title: '印章成本',
      dataIndex: 'stampCost',
      width: 120,
      render: (_, record) => <Tag color="gold">{record.stampCost || 0} 枚</Tag>,
    },
    {
      title: '庫存',
      width: 140,
      render: (_, record) => {
        const total = record.inventoryTotal || 0;
        const redeemed = record.inventoryRedeemed || 0;
        return <span>{Math.max(total - redeemed, 0)} / {total}</span>;
      },
    },
    {
      title: '綁定',
      width: 260,
      render: (_, record) => (
        <Space wrap size={[4, 4]}>
          <Tag color="blue">故事線 {record.storylineBindings?.length || 0}</Tag>
          <Tag color="geekblue">城市 {record.cityBindings?.length || 0}</Tag>
          <Tag color="cyan">子地圖 {record.subMapBindings?.length || 0}</Tag>
          <Tag color="magenta">室內建築 {record.indoorBuildingBindings?.length || 0}</Tag>
          <Tag color="orange">樓層 {record.indoorFloorBindings?.length || 0}</Tag>
        </Space>
      ),
    },
    {
      title: '模板',
      width: 240,
      render: (_, record) => (
        <Space wrap size={[4, 4]}>
          <Tag>{record.popupPresetCode || 'popup'}</Tag>
          <Tag>{record.displayPresetCode || 'display'}</Tag>
          <Tag>{record.triggerPresetCode || 'trigger'}</Tag>
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
          onClick={() => {
            setEditingItem(record);
            form.setFieldsValue(withRewardDefaults(record));
            setModalOpen(true);
          }}
        >
          編輯
        </a>,
        <Popconfirm
          key="delete"
          title="確定刪除此獎勵？"
          onConfirm={async () => {
            const response = await deleteAdminReward(record.id);
            if (!response.success) {
              message.error(response.message || '刪除失敗');
              return;
            }
            message.success('已刪除獎勵');
            actionRef.current?.reload();
          }}
        >
          <a>刪除</a>
        </Popconfirm>,
      ],
    },
  ], [form]);

  const openCreateModal = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue(withRewardDefaults());
    setModalOpen(true);
  };

  const submitForm = async () => {
    const values = await form.validateFields();
    const payload = buildPayload(values);
    const response = editingItem
      ? await updateAdminReward(editingItem.id, payload)
      : await createAdminReward(payload);
    if (!response.success) {
      message.error(response.message || '儲存獎勵失敗');
      return;
    }
    message.success(editingItem ? '獎勵已更新' : '獎勵已建立');
    setModalOpen(false);
    actionRef.current?.reload();
  };

  return (
    <PageContainer
      title="獎勵管理"
      subTitle="配置可兌換獎勵的四語內容、庫存、地圖 / 室內綁定與兌換展示模板。"
    >
      <ProTable<AdminRewardItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        search={false}
        request={async (params) => {
          const response = await getAdminRewards({
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
            新增獎勵
          </Button>,
        ]}
      />

      <Modal
        title={editingItem ? `編輯獎勵：${editingItem.nameZht || editingItem.nameZh}` : '新增獎勵'}
        open={modalOpen}
        onOk={submitForm}
        onCancel={() => setModalOpen(false)}
        width={1280}
        destroyOnHidden
      >
        <Form form={form} layout="vertical">
          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item
              name="code"
              label="獎勵代碼"
              rules={[{ required: true, message: '請輸入獎勵代碼' }]}
              style={{ flex: 1 }}
            >
              <Input placeholder="reward_lisboeta_secret_cut" />
            </Form.Item>
            <Form.Item name="status" label="狀態" style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
            <Form.Item name="sortOrder" label="排序" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="stampCost" label="印章成本" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="inventoryTotal" label="總庫存" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="inventoryRedeemed" label="已兌換" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="publishStartAt" label="上線時間" style={{ flex: 1 }}>
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="publishEndAt" label="下線時間" style={{ flex: 1 }}>
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <CollectionLocalizedCoreFields
            form={form}
            translationDefaults={options.translationDefaults}
            entityLabel="獎勵"
            includeSubtitle
            includeHighlight
          />

          <CollectionMediaSection attachmentHelp="附件可用於兌換說明、實拍素材、音訊導覽或兌換後展示的延伸內容。" />
          <CollectionBindingSection form={form} options={options} />
          <CollectionBehaviorSection form={form} />
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default RewardManagement;

