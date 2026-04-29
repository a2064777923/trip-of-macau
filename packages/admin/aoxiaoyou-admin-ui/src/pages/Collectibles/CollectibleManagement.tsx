import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { type ActionType, type ProColumns } from '@ant-design/pro-table';
import { Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Tag, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import {
  createCollectible,
  deleteCollectible,
  getCollectibles,
  type CollectibleItem,
  updateCollectible,
} from '../../services/api';
import {
  CollectionBehaviorSection,
  CollectionBindingSection,
  CollectionLocalizedCoreFields,
  CollectionMediaSection,
  applyCollectionLocaleFallback,
  useCollectionAuthoringOptions,
} from './CollectionAuthoringShared';

type CollectibleFormValues = Partial<CollectibleItem>;

const collectibleTypeOptions = [
  { value: 'item', label: '一般收集物' },
  { value: 'document', label: '文獻 / 檔案' },
  { value: 'fragment', label: '碎片' },
  { value: 'costume', label: '造型 / 外觀' },
  { value: 'stamp_card', label: '印章卡片' },
];

const rarityOptions = [
  { value: 'common', label: '普通' },
  { value: 'uncommon', label: '少見' },
  { value: 'rare', label: '稀有' },
  { value: 'epic', label: '史詩' },
  { value: 'legendary', label: '傳奇' },
];

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

function withCollectibleDefaults(detail?: Partial<CollectibleItem>): CollectibleFormValues {
  const next: CollectibleFormValues = {
    collectibleType: 'item',
    rarity: 'common',
    isRepeatable: 0,
    isLimited: 0,
    maxOwnership: 1,
    status: 'draft',
    popupPresetCode: 'story-modal',
    displayPresetCode: 'map-keepsake',
    triggerPresetCode: 'poi-arrival',
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

function buildPayload(values: CollectibleFormValues): CollectibleFormValues {
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

const CollectibleManagement: React.FC = () => {
  const [form] = Form.useForm<CollectibleFormValues>();
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<CollectibleItem | null>(null);
  const options = useCollectionAuthoringOptions(form);

  const columns = useMemo<ProColumns<CollectibleItem>[]>(() => [
    { title: '代碼', dataIndex: 'collectibleCode', width: 180 },
    { title: '名稱', dataIndex: 'nameZht', width: 220, render: (_, record) => record.nameZht || record.nameZh },
    {
      title: '類型',
      dataIndex: 'collectibleType',
      width: 120,
      render: (_, record) => <Tag>{collectibleTypeOptions.find((item) => item.value === record.collectibleType)?.label || record.collectibleType}</Tag>,
    },
    {
      title: '稀有度',
      dataIndex: 'rarity',
      width: 120,
      render: (_, record) => <Tag color="purple">{rarityOptions.find((item) => item.value === record.rarity)?.label || record.rarity}</Tag>,
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
            form.setFieldsValue(withCollectibleDefaults(record));
            setModalOpen(true);
          }}
        >
          編輯
        </a>,
        <Popconfirm
          key="delete"
          title="確定刪除此收集物？"
          onConfirm={async () => {
            const response = await deleteCollectible(record.id);
            if (!response.success) {
              message.error(response.message || '刪除失敗');
              return;
            }
            message.success('已刪除收集物');
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
    form.setFieldsValue(withCollectibleDefaults());
    setModalOpen(true);
  };

  const submitForm = async () => {
    const values = await form.validateFields();
    const payload = buildPayload(values);
    const response = editingItem
      ? await updateCollectible(editingItem.id, payload)
      : await createCollectible(payload);
    if (!response.success) {
      message.error(response.message || '儲存收集物失敗');
      return;
    }
    message.success(editingItem ? '收集物已更新' : '收集物已建立');
    setModalOpen(false);
    actionRef.current?.reload();
  };

  return (
    <PageContainer
      title="收集物管理"
      subTitle="配置收集物的四語內容、地圖 / 室內綁定、彈窗 / 展示 / 觸發模板與附件媒體。"
    >
      <ProTable<CollectibleItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        search={{ labelWidth: 'auto' }}
        request={async (params) => {
          const response = await getCollectibles({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.collectibleCode as string,
            rarity: params.rarity as string,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        toolBarRender={() => [
          <Button key="create" type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            新增收集物
          </Button>,
        ]}
      />

      <Modal
        title={editingItem ? `編輯收集物：${editingItem.nameZht || editingItem.nameZh}` : '新增收集物'}
        open={modalOpen}
        onOk={submitForm}
        onCancel={() => setModalOpen(false)}
        width={1280}
        destroyOnHidden
      >
        <Form form={form} layout="vertical">
          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item
              name="collectibleCode"
              label="收集物代碼"
              rules={[{ required: true, message: '請輸入收集物代碼' }]}
              style={{ flex: 1 }}
            >
              <Input placeholder="collectible_lisboeta_night_pass" />
            </Form.Item>
            <Form.Item name="collectibleType" label="收集物類型" style={{ flex: 1 }}>
              <Select options={collectibleTypeOptions} />
            </Form.Item>
            <Form.Item name="rarity" label="稀有度" style={{ flex: 1 }}>
              <Select options={rarityOptions} />
            </Form.Item>
            <Form.Item name="status" label="狀態" style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="acquisitionSource" label="取得來源" style={{ flex: 1 }}>
              <Input placeholder="storyline / indoor / activity / poi" />
            </Form.Item>
            <Form.Item name="seriesId" label="系列 ID" style={{ flex: 1 }}>
              <InputNumber min={1} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="maxOwnership" label="持有上限" style={{ flex: 1 }}>
              <InputNumber min={1} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="sortOrder" label="排序" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="isRepeatable" label="可重複取得" style={{ flex: 1 }}>
              <Select options={[{ value: 0, label: '否' }, { value: 1, label: '是' }]} />
            </Form.Item>
            <Form.Item name="isLimited" label="限時內容" style={{ flex: 1 }}>
              <Select options={[{ value: 0, label: '否' }, { value: 1, label: '是' }]} />
            </Form.Item>
          </Space>

          <CollectionLocalizedCoreFields
            form={form}
            translationDefaults={options.translationDefaults}
            entityLabel="收集物"
          />

          <CollectionMediaSection
            includeIcon
            includeAnimation
            attachmentHelp="附件可作為圖集、音訊導覽、影片或延伸檔案，會同步進公開接口與小程序。"
          />

          <CollectionBindingSection form={form} options={options} />
          <CollectionBehaviorSection form={form} />
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default CollectibleManagement;

