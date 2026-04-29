import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { type ActionType, type ProColumns } from '@ant-design/pro-table';
import { Button, Form, Input, Modal, Popconfirm, Select, Space, Tag, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import {
  createBadge,
  deleteBadge,
  getBadges,
  type BadgeItem,
  updateBadge,
} from '../../services/api';
import {
  CollectionBehaviorSection,
  CollectionBindingSection,
  CollectionLocalizedCoreFields,
  CollectionMediaSection,
  applyCollectionLocaleFallback,
  useCollectionAuthoringOptions,
} from './CollectionAuthoringShared';

type BadgeFormValues = Partial<BadgeItem>;

const badgeTypeOptions = [
  { value: 'storyline', label: '故事線徽章' },
  { value: 'city_exploration', label: '城市探索徽章' },
  { value: 'collection', label: '收集成就徽章' },
  { value: 'activity', label: '活動徽章' },
  { value: 'special', label: '特別徽章' },
  { value: 'hidden', label: '隱藏徽章' },
];

const rarityOptions = [
  { value: 'common', label: '普通' },
  { value: 'rare', label: '稀有' },
  { value: 'epic', label: '史詩' },
  { value: 'legendary', label: '傳奇' },
];

const statusOptions = [
  { value: 'draft', label: '編輯中' },
  { value: 'published', label: '已發佈' },
  { value: 'archived', label: '已封存' },
];

function withBadgeDefaults(detail?: Partial<BadgeItem>): BadgeFormValues {
  const next: BadgeFormValues = {
    badgeType: 'special',
    rarity: 'common',
    isHidden: 0,
    status: 'draft',
    popupPresetCode: 'achievement-toast',
    displayPresetCode: 'badge-ribbon',
    triggerPresetCode: 'chapter-completion',
    storylineBindings: [],
    cityBindings: [],
    subMapBindings: [],
    indoorBuildingBindings: [],
    indoorFloorBindings: [],
    attachmentAssetIds: [],
    ...detail,
  };
  next.storylineBindings = detail?.storylineBindings || [];
  next.cityBindings = detail?.cityBindings || [];
  next.subMapBindings = detail?.subMapBindings || [];
  next.indoorBuildingBindings = detail?.indoorBuildingBindings || [];
  next.indoorFloorBindings = detail?.indoorFloorBindings || [];
  next.attachmentAssetIds = detail?.attachmentAssetIds || [];
  return next;
}

function buildPayload(values: BadgeFormValues): BadgeFormValues {
  const normalized = applyCollectionLocaleFallback(values, ['name', 'description', 'exampleContent']);
  return {
    ...normalized,
    nameZh: normalized.nameZh || normalized.nameZht || '',
    storylineBindings: normalized.storylineBindings || [],
    cityBindings: normalized.cityBindings || [],
    subMapBindings: normalized.subMapBindings || [],
    indoorBuildingBindings: normalized.indoorBuildingBindings || [],
    indoorFloorBindings: normalized.indoorFloorBindings || [],
    attachmentAssetIds: normalized.attachmentAssetIds || [],
  };
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

const BadgeManagement: React.FC = () => {
  const [form] = Form.useForm<BadgeFormValues>();
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<BadgeItem | null>(null);
  const options = useCollectionAuthoringOptions(form);

  const columns = useMemo<ProColumns<BadgeItem>[]>(() => [
    { title: '代碼', dataIndex: 'badgeCode', width: 180 },
    { title: '名稱', dataIndex: 'nameZht', width: 220, render: (_, record) => record.nameZht || record.nameZh },
    {
      title: '類型',
      dataIndex: 'badgeType',
      width: 140,
      render: (_, record) => <Tag>{badgeTypeOptions.find((item) => item.value === record.badgeType)?.label || record.badgeType}</Tag>,
    },
    {
      title: '稀有度',
      dataIndex: 'rarity',
      width: 120,
      render: (_, record) => <Tag color="purple">{rarityOptions.find((item) => item.value === record.rarity)?.label || record.rarity}</Tag>,
    },
    {
      title: '顯示',
      width: 120,
      render: (_, record) => <Tag color={record.isHidden ? 'magenta' : 'green'}>{record.isHidden ? '隱藏' : '公開'}</Tag>,
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
            form.setFieldsValue(withBadgeDefaults(record));
            setModalOpen(true);
          }}
        >
          編輯
        </a>,
        <Popconfirm
          key="delete"
          title="確定刪除此徽章？"
          onConfirm={async () => {
            const response = await deleteBadge(record.id);
            if (!response.success) {
              message.error(response.message || '刪除失敗');
              return;
            }
            message.success('已刪除徽章');
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
    form.setFieldsValue(withBadgeDefaults());
    setModalOpen(true);
  };

  const submitForm = async () => {
    const values = await form.validateFields();
    const payload = buildPayload(values);
    const response = editingItem
      ? await updateBadge(editingItem.id, payload)
      : await createBadge(payload);
    if (!response.success) {
      message.error(response.message || '儲存徽章失敗');
      return;
    }
    message.success(editingItem ? '徽章已更新' : '徽章已建立');
    setModalOpen(false);
    actionRef.current?.reload();
  };

  return (
    <PageContainer
      title="徽章管理"
      subTitle="配置成就徽章的四語內容、地圖 / 室內綁定、展示模板與解鎖觸發條件。"
    >
      <ProTable<BadgeItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        search={false}
        request={async (params) => {
          const response = await getBadges({
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
            新增徽章
          </Button>,
        ]}
      />

      <Modal
        title={editingItem ? `編輯徽章：${editingItem.nameZht || editingItem.nameZh}` : '新增徽章'}
        open={modalOpen}
        onOk={submitForm}
        onCancel={() => setModalOpen(false)}
        width={1280}
        destroyOnHidden
      >
        <Form form={form} layout="vertical">
          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item
              name="badgeCode"
              label="徽章代碼"
              rules={[{ required: true, message: '請輸入徽章代碼' }]}
              style={{ flex: 1 }}
            >
              <Input placeholder="badge_lisboeta_pathfinder" />
            </Form.Item>
            <Form.Item name="badgeType" label="徽章類型" style={{ flex: 1 }}>
              <Select options={badgeTypeOptions} />
            </Form.Item>
            <Form.Item name="rarity" label="稀有度" style={{ flex: 1 }}>
              <Select options={rarityOptions} />
            </Form.Item>
            <Form.Item name="status" label="狀態" style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="isHidden" label="是否隱藏" style={{ flex: 1 }}>
              <Select options={[{ value: 0, label: '否' }, { value: 1, label: '是' }]} />
            </Form.Item>
          </Space>

          <CollectionLocalizedCoreFields
            form={form}
            translationDefaults={options.translationDefaults}
            entityLabel="徽章"
          />

          <CollectionMediaSection
            includeIcon
            includeAnimation
            attachmentHelp="附件可作為徽章解鎖說明、動效、延伸媒體或分享素材。"
          />

          <CollectionBindingSection form={form} options={options} />
          <CollectionBehaviorSection form={form} />
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default BadgeManagement;

