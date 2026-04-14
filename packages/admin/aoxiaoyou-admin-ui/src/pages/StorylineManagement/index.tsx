import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable, { type ActionType, type ProColumns } from '@ant-design/pro-table';
import {
  Button,
  DatePicker,
  Drawer,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  message,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import dayjs from 'dayjs';
import {
  createAdminStoryline,
  createStorylineChapter,
  deleteAdminStoryline,
  deleteStorylineChapter,
  getAdminStorylineDetail,
  getAdminStorylines,
  getAdminTranslationSettings,
  getCities,
  getStorylineChapters,
  updateAdminStoryline,
  updateStorylineChapter,
} from '../../services/api';
import type {
  AdminStoryChapterItem,
  AdminStorylineDetail,
  AdminStorylineListItem,
  CityItem,
} from '../../types/admin';
import LocalizedFieldGroup, {
  buildLocalizedFieldNames,
} from '../../components/localization/LocalizedFieldGroup';

const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已發布', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const storylineNameFields = buildLocalizedFieldNames('name');
const storylineDescriptionFields = buildLocalizedFieldNames('description');
const rewardBadgeFields = buildLocalizedFieldNames('rewardBadge');

const chapterTitleFields = buildLocalizedFieldNames('title');
const chapterSummaryFields = buildLocalizedFieldNames('summary');
const chapterDetailFields = buildLocalizedFieldNames('detail');
const chapterAchievementFields = buildLocalizedFieldNames('achievement');
const chapterCollectibleFields = buildLocalizedFieldNames('collectible');
const chapterLocationFields = buildLocalizedFieldNames('locationName');

function pickStorylineName(record: AdminStorylineListItem) {
  return record.nameZht || record.nameZh || record.nameEn || record.namePt || record.code;
}

const StorylineManagement: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [editorOpen, setEditorOpen] = useState(false);
  const [chapterDrawerOpen, setChapterDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<AdminStorylineDetail | null>(null);
  const [chapterEditing, setChapterEditing] = useState<AdminStoryChapterItem | null>(null);
  const [chapterStoryline, setChapterStoryline] = useState<AdminStorylineListItem | null>(null);
  const [chapterList, setChapterList] = useState<AdminStoryChapterItem[]>([]);
  const [cityOptions, setCityOptions] = useState<CityItem[]>([]);
  const [form] = Form.useForm();
  const [chapterForm] = Form.useForm();

  const translationSettingsRequest = useRequest(getAdminTranslationSettings);

  const loadCities = async () => {
    const res = await getCities({ pageNum: 1, pageSize: 100 });
    setCityOptions(res.data?.list || []);
  };

  const loadChapters = async (storylineId: number) => {
    const response = await getStorylineChapters(storylineId, { pageNum: 1, pageSize: 100 });
    if (response.success) {
      setChapterList(response.data?.list || []);
    }
  };

  const openChapterDrawer = async (record: AdminStorylineListItem) => {
    setChapterStoryline(record);
    setChapterEditing(null);
    chapterForm.resetFields();
    await loadChapters(record.storylineId);
    chapterForm.setFieldsValue({
      chapterOrder: 1,
      unlockType: 'sequence',
      sortOrder: 1,
      status: 'draft',
    });
    setChapterDrawerOpen(true);
  };

  const columns = useMemo<ProColumns<AdminStorylineListItem>[]>(
    () => [
      { title: '代碼', dataIndex: 'code', copyable: true, width: 140 },
      { title: '故事線名稱', render: (_, record) => pickStorylineName(record) },
      { title: '城市', dataIndex: 'cityName', hideInSearch: true },
      { title: '難度', dataIndex: 'difficulty', hideInSearch: true },
      { title: '預估分鐘', dataIndex: 'estimatedMinutes', hideInSearch: true, width: 110 },
      { title: '章節數', dataIndex: 'totalChapters', hideInSearch: true, width: 90 },
      {
        title: '狀態',
        dataIndex: 'status',
        valueType: 'select',
        valueEnum: {
          draft: { text: '草稿' },
          published: { text: '已發布' },
          archived: { text: '已封存' },
        },
        render: (_, record) => {
          const color = record.status === 'published' ? 'green' : record.status === 'archived' ? 'default' : 'gold';
          const label = record.status === 'published' ? '已發布' : record.status === 'archived' ? '已封存' : '草稿';
          return <Tag color={color}>{label}</Tag>;
        },
        width: 120,
      },
      {
        title: '操作',
        key: 'action',
        valueType: 'option',
        render: (_, record) => [
          <Button
            key="edit"
            type="link"
            onClick={async () => {
              await loadCities();
              const response = await getAdminStorylineDetail(record.storylineId);
              if (response.success && response.data) {
                setEditing(response.data);
                form.setFieldsValue({
                  ...response.data,
                  publishedAt: response.data.publishedAt ? dayjs(response.data.publishedAt) : undefined,
                });
                setEditorOpen(true);
              }
            }}
          >
            編輯
          </Button>,
          <Button key="chapters" type="link" onClick={() => void openChapterDrawer(record)}>
            章節編排
          </Button>,
          <Popconfirm
            key="delete"
            title="確定刪除這條故事線？"
            onConfirm={async () => {
              await deleteAdminStoryline(record.storylineId);
              message.success('故事線已刪除');
              actionRef.current?.reload();
            }}
          >
            <Button type="link" danger>
              刪除
            </Button>
          </Popconfirm>,
        ],
      },
    ],
    [form],
  );

  return (
    <PageContainer
      title="故事線管理"
      subTitle="管理故事線四語內容、章節編排、封面資源與發布狀態。"
      extra={[
        <Button
          key="add"
          type="primary"
          icon={<PlusOutlined />}
          onClick={async () => {
            await loadCities();
            setEditing(null);
            form.resetFields();
            form.setFieldsValue({ difficulty: 'easy', sortOrder: 0, status: 'draft' });
            setEditorOpen(true);
          }}
        >
          新增故事線
        </Button>,
      ]}
    >
      <ProTable<AdminStorylineListItem>
        actionRef={actionRef}
        rowKey="storylineId"
        columns={columns}
        request={async (params) => {
          const response = await getAdminStorylines({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.code as string,
            status: params.status as string,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
      />

      <Drawer
        open={editorOpen}
        title={editing ? '編輯故事線' : '新增故事線'}
        width={980}
        onClose={() => setEditorOpen(false)}
        destroyOnClose
      >
        <Form
          layout="vertical"
          form={form}
          onFinish={async (values) => {
            const payload = {
              ...values,
              publishedAt: values.publishedAt ? values.publishedAt.format('YYYY-MM-DDTHH:mm:ss') : undefined,
            };
            if (editing) {
              await updateAdminStoryline(editing.storylineId, payload);
              message.success('故事線已更新');
            } else {
              await createAdminStoryline(payload);
              message.success('故事線已建立');
            }
            setEditorOpen(false);
            actionRef.current?.reload();
          }}
        >
          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="cityId" label="關聯城市 / 子地圖" style={{ flex: 1 }}>
              <Select
                allowClear
                options={cityOptions.map((item) => ({
                  label: `${item.nameZht || item.nameZh || item.code} (${item.code})`,
                  value: item.id,
                }))}
              />
            </Form.Item>
            <Form.Item
              name="code"
              label="故事線代碼"
              rules={[{ required: true, message: '請輸入故事線代碼' }]}
              style={{ flex: 1 }}
            >
              <Input />
            </Form.Item>
          </Space>

          <LocalizedFieldGroup
            form={form}
            label="故事線名稱"
            fieldNames={storylineNameFields}
            required
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="difficulty" label="難度" style={{ flex: 1 }}>
              <Select
                options={[
                  { label: '簡單', value: 'easy' },
                  { label: '中等', value: 'medium' },
                  { label: '困難', value: 'hard' },
                ]}
              />
            </Form.Item>
            <Form.Item name="estimatedMinutes" label="預估分鐘" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
            <Form.Item name="sortOrder" label="排序" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={0} />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="coverAssetId" label="封面資源 ID" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={1} />
            </Form.Item>
            <Form.Item name="bannerAssetId" label="橫幅資源 ID" style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} min={1} />
            </Form.Item>
            <Form.Item name="status" label="狀態" style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
          </Space>

          <LocalizedFieldGroup
            form={form}
            label="故事線介紹"
            fieldNames={storylineDescriptionFields}
            multiline
            rows={4}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="完成獎勵名稱"
            fieldNames={rewardBadgeFields}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <Form.Item name="publishedAt" label="發布時間">
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>

          <Space>
            <Button type="primary" htmlType="submit">
              儲存
            </Button>
            <Button onClick={() => setEditorOpen(false)}>取消</Button>
          </Space>
        </Form>
      </Drawer>

      <Drawer
        open={chapterDrawerOpen}
        title={chapterStoryline ? `章節編排：${pickStorylineName(chapterStoryline)}` : '章節編排'}
        width={1100}
        onClose={() => setChapterDrawerOpen(false)}
        destroyOnClose
      >
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <Button
            type="primary"
            onClick={() => {
              setChapterEditing(null);
              chapterForm.resetFields();
              chapterForm.setFieldsValue({
                chapterOrder: chapterList.length + 1,
                unlockType: 'sequence',
                sortOrder: chapterList.length + 1,
                status: 'draft',
              });
            }}
          >
            新增章節
          </Button>

          <Table<AdminStoryChapterItem>
            rowKey="id"
            pagination={false}
            dataSource={chapterList}
            columns={[
              { title: '章節序', dataIndex: 'chapterOrder', width: 90 },
              {
                title: '章節名稱',
                render: (_, record) => record.titleZht || record.titleZh || record.titleEn || record.titlePt || '-',
              },
              { title: '解鎖方式', dataIndex: 'unlockType', width: 120 },
              { title: '媒體資源', dataIndex: 'mediaAssetId', width: 120 },
              {
                title: '狀態',
                dataIndex: 'status',
                width: 100,
                render: (value) => {
                  const color = value === 'published' ? 'green' : value === 'archived' ? 'default' : 'gold';
                  const label = value === 'published' ? '已發布' : value === 'archived' ? '已封存' : '草稿';
                  return <Tag color={color}>{label}</Tag>;
                },
              },
              {
                title: '操作',
                key: 'action',
                width: 180,
                render: (_, record) => (
                  <Space>
                    <Button
                      type="link"
                      onClick={() => {
                        setChapterEditing(record);
                        chapterForm.setFieldsValue({
                          ...record,
                          publishedAt: record.publishedAt ? dayjs(record.publishedAt) : undefined,
                        });
                      }}
                    >
                      編輯
                    </Button>
                    <Popconfirm
                      title="確定刪除這個章節？"
                      onConfirm={async () => {
                        if (!chapterStoryline) return;
                        await deleteStorylineChapter(chapterStoryline.storylineId, record.id);
                        message.success('章節已刪除');
                        await loadChapters(chapterStoryline.storylineId);
                        actionRef.current?.reload();
                      }}
                    >
                      <Button type="link" danger>
                        刪除
                      </Button>
                    </Popconfirm>
                  </Space>
                ),
              },
            ]}
          />

          <Form
            layout="vertical"
            form={chapterForm}
            onFinish={async (values) => {
              if (!chapterStoryline) return;
              const payload = {
                ...values,
                publishedAt: values.publishedAt ? values.publishedAt.format('YYYY-MM-DDTHH:mm:ss') : undefined,
              };
              if (chapterEditing) {
                await updateStorylineChapter(chapterStoryline.storylineId, chapterEditing.id, payload);
                message.success('章節已更新');
              } else {
                await createStorylineChapter(chapterStoryline.storylineId, payload);
                message.success('章節已建立');
              }
              setChapterEditing(null);
              chapterForm.resetFields();
              chapterForm.setFieldsValue({
                chapterOrder: chapterList.length + 1,
                unlockType: 'sequence',
                sortOrder: chapterList.length + 1,
                status: 'draft',
              });
              await loadChapters(chapterStoryline.storylineId);
              actionRef.current?.reload();
            }}
          >
            <Space style={{ display: 'flex' }} size={16} align="start">
              <Form.Item
                name="chapterOrder"
                label="章節順序"
                rules={[{ required: true, message: '請輸入章節順序' }]}
                style={{ flex: 1 }}
              >
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item name="sortOrder" label="排序" style={{ flex: 1 }}>
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item name="mediaAssetId" label="媒體資源 ID" style={{ flex: 1 }}>
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
            </Space>

            <LocalizedFieldGroup
              form={chapterForm}
              label="章節名稱"
              fieldNames={chapterTitleFields}
              required
              translationDefaults={translationSettingsRequest.data?.data}
            />

            <LocalizedFieldGroup
              form={chapterForm}
              label="章節摘要"
              fieldNames={chapterSummaryFields}
              multiline
              rows={3}
              translationDefaults={translationSettingsRequest.data?.data}
            />

            <LocalizedFieldGroup
              form={chapterForm}
              label="章節內容"
              fieldNames={chapterDetailFields}
              multiline
              rows={4}
              translationDefaults={translationSettingsRequest.data?.data}
            />

            <Space style={{ display: 'flex' }} size={16} align="start">
              <Form.Item name="unlockType" label="解鎖方式" style={{ flex: 1 }}>
                <Select
                  options={[
                    { label: '順序解鎖', value: 'sequence' },
                    { label: '印章數量', value: 'stamp_count' },
                    { label: '指定時間', value: 'time' },
                  ]}
                />
              </Form.Item>
              <Form.Item name="unlockParamJson" label="解鎖條件 JSON" style={{ flex: 2 }}>
                <Input placeholder='例如：{"requiredPoiId":1001}' />
              </Form.Item>
              <Form.Item name="status" label="狀態" style={{ flex: 1 }}>
                <Select options={statusOptions} />
              </Form.Item>
            </Space>

            <LocalizedFieldGroup
              form={chapterForm}
              label="完成成就"
              fieldNames={chapterAchievementFields}
              translationDefaults={translationSettingsRequest.data?.data}
            />

            <LocalizedFieldGroup
              form={chapterForm}
              label="收集物提示"
              fieldNames={chapterCollectibleFields}
              translationDefaults={translationSettingsRequest.data?.data}
            />

            <LocalizedFieldGroup
              form={chapterForm}
              label="地點名稱"
              fieldNames={chapterLocationFields}
              translationDefaults={translationSettingsRequest.data?.data}
            />

            <Form.Item name="publishedAt" label="發布時間">
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>

            <Space>
              <Button type="primary" htmlType="submit">
                {chapterEditing ? '更新章節' : '建立章節'}
              </Button>
              <Button
                onClick={() => {
                  setChapterEditing(null);
                  chapterForm.resetFields();
                }}
              >
                清空
              </Button>
            </Space>
          </Form>
        </Space>
      </Drawer>
    </PageContainer>
  );
};

export default StorylineManagement;
