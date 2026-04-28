import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import {
  App as AntdApp,
  Button,
  Drawer,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  createAdminStoryContentBlock,
  deleteAdminStoryContentBlock,
  getAdminStoryContentBlockDetail,
  getAdminStoryContentBlocks,
  getAdminTranslationSettings,
  updateAdminStoryContentBlock,
} from '../../services/api';
import type {
  AdminStoryContentBlockItem,
  AdminStoryContentBlockPayload,
} from '../../types/admin';
import LocalizedFieldGroup, {
  buildLocalizedFieldNames,
} from '../../components/localization/LocalizedFieldGroup';
import MediaAssetPickerField from '../../components/media/MediaAssetPickerField';

const { Text } = Typography;

const titleFields = buildLocalizedFieldNames('title');
const summaryFields = buildLocalizedFieldNames('summary');
const bodyFields = buildLocalizedFieldNames('body');

const blockTypeOptions = [
  { label: '富文本', value: 'rich_text' },
  { label: '引言', value: 'quote' },
  { label: '單圖', value: 'image' },
  { label: '圖集', value: 'gallery' },
  { label: '音頻', value: 'audio' },
  { label: '影片', value: 'video' },
  { label: 'Lottie 動畫', value: 'lottie' },
  { label: '附件列表', value: 'attachment_list' },
];

const statusOptions = [
  { label: '編輯中', value: 'draft' },
  { label: '已發佈', value: 'published' },
  { label: '已封存', value: 'archived' },
];

interface FilterValues {
  keyword?: string;
  blockType?: string;
  status?: string;
}

const defaultBlockValues: Partial<AdminStoryContentBlockPayload> = {
  blockType: 'rich_text',
  status: 'draft',
  displayMode: 'default',
  sortOrder: 0,
};

function pickBlockTitle(block?: Partial<AdminStoryContentBlockItem> | null) {
  if (!block) {
    return '';
  }
  return block.titleZht || block.titleZh || block.titleEn || block.titlePt || block.code || `積木 #${block.id}`;
}

function resolvePrimaryAssetKind(blockType?: string) {
  switch (blockType) {
    case 'image':
    case 'gallery':
      return 'image';
    case 'audio':
      return 'audio';
    case 'video':
      return 'video';
    case 'lottie':
      return 'lottie';
    default:
      return undefined;
  }
}

const StoryContentBlockManagement: React.FC = () => {
  const { message } = AntdApp.useApp();
  const [filterForm] = Form.useForm<FilterValues>();
  const [editorForm] = Form.useForm<AdminStoryContentBlockPayload>();
  const [translationDefaults, setTranslationDefaults] = useState<any>();
  const [items, setItems] = useState<AdminStoryContentBlockItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [editorOpen, setEditorOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [editingItem, setEditingItem] = useState<AdminStoryContentBlockItem | null>(null);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);

  const activeBlockType = Form.useWatch('blockType', editorForm);

  const loadList = async (nextPageNum = pageNum, nextPageSize = pageSize) => {
    setLoading(true);
    try {
      const response = await getAdminStoryContentBlocks({
        pageNum: nextPageNum,
        pageSize: nextPageSize,
        ...filterForm.getFieldsValue(),
      });
      if (response.success && response.data) {
        setItems(response.data.list || []);
        setTotal(response.data.total || 0);
        setPageNum(nextPageNum);
        setPageSize(nextPageSize);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadList(1, 10);
    void getAdminTranslationSettings().then((response) => {
      if (response.success && response.data) {
        setTranslationDefaults(response.data);
      }
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openCreate = () => {
    setEditingItem(null);
    editorForm.resetFields();
    editorForm.setFieldsValue(defaultBlockValues);
    setEditorOpen(true);
  };

  const openEdit = async (record: AdminStoryContentBlockItem) => {
    const response = await getAdminStoryContentBlockDetail(record.id);
    const detail = response.success && response.data ? response.data : record;
    setEditingItem(detail);
    editorForm.resetFields();
    editorForm.setFieldsValue({
      ...defaultBlockValues,
      ...detail,
      attachmentAssetIds: detail.attachmentAssetIds || [],
    });
    setEditorOpen(true);
  };

  const handleSave = async () => {
    const values = await editorForm.validateFields();
    setSaving(true);
    try {
      const payload: AdminStoryContentBlockPayload = {
        ...values,
        attachmentAssetIds: values.attachmentAssetIds || [],
      };
      const response = editingItem
        ? await updateAdminStoryContentBlock(editingItem.id, payload)
        : await createAdminStoryContentBlock(payload);
      if (!response.success) {
        throw new Error(response.message || '保存內容積木失敗');
      }
      message.success(editingItem ? '內容積木已更新' : '內容積木已建立');
      setEditorOpen(false);
      void loadList(editingItem ? pageNum : 1, pageSize);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '保存內容積木失敗');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (record: AdminStoryContentBlockItem) => {
    const response = await deleteAdminStoryContentBlock(record.id);
    if (response.success) {
      message.success('內容積木已刪除');
      void loadList(pageNum, pageSize);
      return;
    }
    message.error(response.message || '刪除內容積木失敗');
  };

  const columns = useMemo<ColumnsType<AdminStoryContentBlockItem>>(
    () => [
      {
        title: 'ID',
        dataIndex: 'id',
        width: 80,
      },
      {
        title: '積木名稱',
        key: 'title',
        render: (_, record) => (
          <Space direction="vertical" size={2}>
            <Text strong>{pickBlockTitle(record)}</Text>
            <Text type="secondary">{record.code}</Text>
          </Space>
        ),
      },
      {
        title: '類型',
        dataIndex: 'blockType',
        width: 160,
        render: (value: string) => <Tag>{blockTypeOptions.find((item) => item.value === value)?.label || value}</Tag>,
      },
      {
        title: '狀態',
        dataIndex: 'status',
        width: 120,
        render: (value: string) => (
          <Tag color={value === 'published' ? 'green' : value === 'archived' ? 'default' : 'gold'}>
            {statusOptions.find((item) => item.value === value)?.label || value}
          </Tag>
        ),
      },
      {
        title: '排序',
        dataIndex: 'sortOrder',
        width: 100,
      },
      {
        title: '更新時間',
        dataIndex: 'updatedAt',
        width: 200,
      },
      {
        title: '操作',
        key: 'actions',
        width: 180,
        render: (_, record) => (
          <Space>
            <Button type="link" onClick={() => void openEdit(record)}>
              編輯
            </Button>
            <Button danger type="link" onClick={() => void handleDelete(record)}>
              刪除
            </Button>
          </Space>
        ),
      },
    ],
    [pageNum],
  );

  return (
    <PageContainer
      title="內容積木庫"
      subTitle="建立可跨章節重用的故事內容積木，供章節工作台直接選取、排序與局部覆寫。"
      extra={[
        <Button key="create" type="primary" onClick={openCreate}>
          新增內容積木
        </Button>,
      ]}
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Form
          form={filterForm}
          layout="inline"
          onFinish={() => {
            void loadList(1, pageSize);
          }}
        >
          <Form.Item name="keyword" label="關鍵字">
            <Input placeholder="代碼、名稱或摘要" allowClear style={{ width: 240 }} />
          </Form.Item>
          <Form.Item name="blockType" label="類型">
            <Select allowClear options={blockTypeOptions} style={{ width: 180 }} />
          </Form.Item>
          <Form.Item name="status" label="狀態">
            <Select allowClear options={statusOptions} style={{ width: 160 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                篩選
              </Button>
              <Button
                onClick={() => {
                  filterForm.resetFields();
                  void loadList(1, pageSize);
                }}
              >
                清除
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={items}
          pagination={{
            current: pageNum,
            pageSize,
            total,
            showSizeChanger: true,
            onChange: (nextPageNum, nextPageSize) => {
              void loadList(nextPageNum, nextPageSize);
            },
          }}
        />
      </Space>

      <Drawer
        title={editingItem ? `編輯內容積木：${pickBlockTitle(editingItem)}` : '新增內容積木'}
        open={editorOpen}
        width={860}
        onClose={() => setEditorOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setEditorOpen(false)}>取消</Button>
            <Button type="primary" loading={saving} onClick={() => void handleSave()}>
              保存
            </Button>
          </Space>
        }
        destroyOnClose
      >
        <Form form={editorForm} layout="vertical" initialValues={defaultBlockValues}>
          <Space style={{ width: '100%' }} size="large" align="start">
            <Form.Item name="code" label="積木代碼" style={{ flex: 1 }}>
              <Input placeholder="可留空，由系統自動生成" />
            </Form.Item>
            <Form.Item
              name="blockType"
              label="積木類型"
              rules={[{ required: true, message: '請選擇積木類型' }]}
              style={{ width: 220 }}
            >
              <Select options={blockTypeOptions} />
            </Form.Item>
            <Form.Item name="status" label="狀態" style={{ width: 180 }}>
              <Select options={statusOptions} />
            </Form.Item>
            <Form.Item name="sortOrder" label="排序" style={{ width: 140 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <LocalizedFieldGroup
            form={editorForm}
            label="積木標題"
            fieldNames={titleFields}
            translationDefaults={translationDefaults}
          />

          <LocalizedFieldGroup
            form={editorForm}
            label="積木摘要"
            fieldNames={summaryFields}
            multiline
            rows={3}
            translationDefaults={translationDefaults}
          />

          <LocalizedFieldGroup
            form={editorForm}
            label="積木正文"
            fieldNames={bodyFields}
            multiline
            rows={6}
            translationDefaults={translationDefaults}
          />

          <Space style={{ width: '100%' }} size="large" align="start">
            <div style={{ flex: 1 }}>
              <MediaAssetPickerField
                name="primaryAssetId"
                label="主資源"
                assetKind={resolvePrimaryAssetKind(activeBlockType)}
                valueMode="asset-id"
                help="可直接拖拽上傳，建立後會自動回填資源 ID。"
              />
            </div>
            <div style={{ flex: 1 }}>
              <MediaAssetPickerField
                name="attachmentAssetIds"
                label="附加資源"
                valueMode="asset-id"
                multiple
                help="圖集、附件列表與多媒體補充資源都可在這裡掛載。"
              />
            </div>
          </Space>

          <Space style={{ width: '100%' }} size="large" align="start">
            <Form.Item name="stylePreset" label="排版樣式" style={{ flex: 1 }}>
              <Input placeholder="例如：story-highlight / gallery-grid / quote-classic" />
            </Form.Item>
            <Form.Item name="displayMode" label="顯示模式" style={{ flex: 1 }}>
              <Input placeholder="例如：default / immersive / compact" />
            </Form.Item>
          </Space>

          <Form.Item name="visibilityJson" label="顯示開關 JSON">
            <Input.TextArea rows={3} placeholder='例如：{"showTitle":true,"showSummary":true}' />
          </Form.Item>

          <Form.Item name="configJson" label="進階配置 JSON">
            <Input.TextArea rows={4} placeholder='例如：{"autoplay":true,"loop":true}' />
          </Form.Item>
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default StoryContentBlockManagement;
