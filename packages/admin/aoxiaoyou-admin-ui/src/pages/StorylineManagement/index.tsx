import React, { useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable, { type ActionType, type ProColumns } from '@ant-design/pro-table';
import {
  Alert,
  Button,
  DatePicker,
  Drawer,
  Form,
  Input,
  InputNumber,
  Popconfirm,
  Select,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import { BranchesOutlined, PlusOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import dayjs, { type Dayjs } from 'dayjs';
import {
  createAdminStoryline,
  deleteAdminStoryline,
  getAdminStorylineDetail,
  getAdminStorylines,
  getAdminTranslationSettings,
  getCities,
  updateAdminStoryline,
} from '../../services/api';
import type {
  AdminStorylineDetail,
  AdminStorylineListItem,
  AdminStorylinePayload,
  CityItem,
} from '../../types/admin';
import LocalizedFieldGroup, {
  buildLocalizedFieldNames,
} from '../../components/localization/LocalizedFieldGroup';
import SpatialAssetPickerField from '../../components/spatial/SpatialAssetPickerField';
import MediaAssetArrayField from '../../components/media/MediaAssetArrayField';

const { Text } = Typography;

const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已發布', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const difficultyOptions = [
  { label: '簡單', value: 'easy' },
  { label: '中等', value: 'medium' },
  { label: '困難', value: 'hard' },
];

const storylineNameFields = buildLocalizedFieldNames('name');
const storylineDescriptionFields = buildLocalizedFieldNames('description');
const rewardBadgeFields = buildLocalizedFieldNames('rewardBadge');

interface StorylineFormValues extends Omit<AdminStorylinePayload, 'publishedAt'> {
  publishedAt?: Dayjs | null;
}

function hasText(value?: string | null): value is string {
  return typeof value === 'string' && value.trim().length > 0;
}

function firstText(...values: Array<string | null | undefined>) {
  return values.find((value) => hasText(value))?.trim();
}

function applyLocalizedFallback<T extends Record<string, any>>(values: T, baseNames: string[]): T {
  const next = { ...values } as Record<string, any>;
  baseNames.forEach((baseName) => {
    const zhKey = `${baseName}Zh`;
    const zhtKey = `${baseName}Zht`;
    const enKey = `${baseName}En`;
    const ptKey = `${baseName}Pt`;
    const zhValue = firstText(next[zhKey], next[zhtKey], next[enKey], next[ptKey]);
    const zhtValue = firstText(next[zhtKey], next[zhKey], next[enKey], next[ptKey]);
    if (zhValue) {
      next[zhKey] = zhValue;
    }
    if (zhtValue) {
      next[zhtKey] = zhtValue;
    }
  });
  return next as T;
}

function pickCityName(city?: CityItem | null) {
  if (!city) {
    return '';
  }
  return city.nameZht || city.nameZh || city.nameEn || city.namePt || city.code;
}

function pickStorylineName(record?: Partial<AdminStorylineListItem> | null) {
  if (!record) {
    return '';
  }
  return record.nameZht || record.nameZh || record.nameEn || record.namePt || record.code || '';
}

function withStorylineDefaults(detail?: Partial<AdminStorylineDetail>): Partial<StorylineFormValues> {
  return {
    ...detail,
    difficulty: detail?.difficulty || 'easy',
    cityBindings: detail?.cityBindings || (detail?.cityId ? [detail.cityId] : []),
    subMapBindings: detail?.subMapBindings || [],
    attachmentAssetIds: detail?.attachmentAssetIds || [],
    status: detail?.status || 'draft',
    estimatedMinutes: detail?.estimatedMinutes ?? 45,
    sortOrder: detail?.sortOrder ?? 0,
    publishedAt: detail?.publishedAt ? dayjs(detail.publishedAt) : undefined,
  };
}

function buildStorylinePayload(values: StorylineFormValues): AdminStorylinePayload {
  const normalized = applyLocalizedFallback(values, ['name', 'description', 'rewardBadge']);
  return {
    ...normalized,
    nameZh: firstText(normalized.nameZh, normalized.nameZht) || '',
    nameZht: firstText(normalized.nameZht, normalized.nameZh),
    cityBindings: normalized.cityBindings || [],
    subMapBindings: normalized.subMapBindings || [],
    attachmentAssetIds: normalized.attachmentAssetIds || [],
    publishedAt: normalized.publishedAt ? normalized.publishedAt.format('YYYY-MM-DDTHH:mm:ss') : undefined,
  };
}

function renderStatus(status?: string) {
  if (status === 'published') {
    return <Tag color="green">已發布</Tag>;
  }
  if (status === 'archived') {
    return <Tag>已封存</Tag>;
  }
  return <Tag color="gold">草稿</Tag>;
}

const StorylineManagement: React.FC = () => {
  const navigate = useNavigate();
  const actionRef = useRef<ActionType>();
  const [editorOpen, setEditorOpen] = useState(false);
  const [editing, setEditing] = useState<AdminStorylineDetail | null>(null);
  const [form] = Form.useForm<StorylineFormValues>();

  const translationSettingsRequest = useRequest(getAdminTranslationSettings);
  const citiesRequest = useRequest(() => getCities({ pageNum: 1, pageSize: 100 }));

  const cities = citiesRequest.data?.data?.list || [];
  const selectedCityBindings = Form.useWatch('cityBindings', form) as number[] | undefined;

  const cityMap = useMemo(() => {
    const map = new Map<number, CityItem>();
    cities.forEach((city) => {
      map.set(city.id, city);
    });
    return map;
  }, [cities]);

  const allSubMaps = useMemo(
    () =>
      cities.flatMap((city) =>
        (city.subMaps || []).map((subMap) => ({
          ...subMap,
          cityId: city.id,
        })),
      ),
    [cities],
  );

  const availableSubMaps = useMemo(() => {
    if (!selectedCityBindings?.length) {
      return allSubMaps;
    }
    return allSubMaps.filter((subMap) => selectedCityBindings.includes(subMap.cityId));
  }, [allSubMaps, selectedCityBindings]);

  const subMapLabelMap = useMemo(() => {
    const map = new Map<number, string>();
    allSubMaps.forEach((subMap) => {
      const city = cityMap.get(subMap.cityId);
      const subMapName = subMap.nameZht || subMap.nameZh || subMap.nameEn || subMap.namePt || subMap.code;
      map.set(subMap.id, city ? `${subMapName} / ${pickCityName(city)}` : subMapName);
    });
    return map;
  }, [allSubMaps, cityMap]);

  const columns = useMemo<ProColumns<AdminStorylineListItem>[]>(
    () => [
      {
        title: '代碼',
        dataIndex: 'code',
        width: 180,
        copyable: true,
      },
      {
        title: '故事線',
        key: 'storyline',
        render: (_, record) => (
          <Space direction="vertical" size={0}>
            <Text strong>{pickStorylineName(record)}</Text>
            <Text type="secondary">{record.nameEn || record.namePt || '尚未補齊其他語言'}</Text>
          </Space>
        ),
      },
      {
        title: '綁定地圖',
        key: 'bindings',
        hideInSearch: true,
        render: (_, record) => (
          <Space wrap size={[6, 6]}>
            {(record.cityBindings || [])
              .map((cityId) => cityMap.get(cityId))
              .filter(Boolean)
              .map((city) => (
                <Tag key={`city-${city!.id}`} color="blue">
                  {pickCityName(city)}
                </Tag>
              ))}
            {(record.subMapBindings || []).map((subMapId) => (
              <Tag key={`submap-${subMapId}`} color="purple">
                {subMapLabelMap.get(subMapId) || `子地圖 #${subMapId}`}
              </Tag>
            ))}
            {!record.cityBindings?.length && !record.subMapBindings?.length ? <Text type="secondary">未綁定</Text> : null}
          </Space>
        ),
      },
      {
        title: '難度',
        dataIndex: 'difficulty',
        hideInSearch: true,
        width: 100,
        render: (_, record) => {
          const label =
            difficultyOptions.find((item) => item.value === record.difficulty)?.label || record.difficulty || '-';
          return <Tag color="geekblue">{label}</Tag>;
        },
      },
      {
        title: '預估時間',
        dataIndex: 'estimatedMinutes',
        hideInSearch: true,
        width: 120,
        render: (value) => `${value || 0} 分鐘`,
      },
      {
        title: '章節數',
        dataIndex: 'totalChapters',
        hideInSearch: true,
        width: 96,
      },
      {
        title: '狀態',
        dataIndex: 'status',
        width: 110,
        valueType: 'select',
        valueEnum: {
          draft: { text: '草稿' },
          published: { text: '已發布' },
          archived: { text: '已封存' },
        },
        render: (_, record) => renderStatus(record.status),
      },
      {
        title: '操作',
        key: 'action',
        valueType: 'option',
        width: 300,
        render: (_, record) => [
          <Button
            key="edit"
            type="link"
            onClick={async () => {
              const response = await getAdminStorylineDetail(record.storylineId);
              if (!response.success || !response.data) {
                message.error(response.message || '無法載入故事線詳情');
                return;
              }
              setEditing(response.data);
              form.setFieldsValue(withStorylineDefaults(response.data));
              setEditorOpen(true);
            }}
          >
            編輯
          </Button>,
          <Button
            key="chapters"
            type="link"
            icon={<BranchesOutlined />}
            onClick={() => navigate(`/content/chapters?storylineId=${record.storylineId}`)}
          >
            章節編排
          </Button>,
          <Button
            key="mode"
            type="link"
            icon={<BranchesOutlined />}
            onClick={() => navigate(`/content/storylines/${record.storylineId}/mode`)}
          >
            路線與覆寫
          </Button>,
          <Popconfirm
            key="delete"
            title="確定刪除這條故事線？"
            description="刪除後需要重新編排章節與關聯內容。"
            onConfirm={async () => {
              const response = await deleteAdminStoryline(record.storylineId);
              if (!response.success) {
                message.error(response.message || '故事線刪除失敗');
                return;
              }
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
    [cityMap, form, navigate, subMapLabelMap],
  );

  return (
    <PageContainer
      title="故事線管理"
      subTitle="維護故事線元資料、四語內容、封面 / 橫幅 / 附件媒體與多城市、多子地圖綁定。章節設計請到獨立的章節編排頁處理。"
      extra={[
        <Button
          key="add"
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setEditing(null);
            form.resetFields();
            form.setFieldsValue(withStorylineDefaults());
            setEditorOpen(true);
          }}
        >
          新增故事線
        </Button>,
      ]}
    >
      <Alert
        showIcon
        type="info"
        style={{ marginBottom: 16 }}
        message="故事線與章節已拆分"
        description="這個頁面只處理故事線元資料與空間綁定；章節、錨點、解鎖條件、完成條件與獎勵效果，請進入獨立的「章節編排」頁面維護。"
      />

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
        width={1080}
        destroyOnHidden
        title={editing ? `編輯故事線：${pickStorylineName(editing)}` : '新增故事線'}
        onClose={() => setEditorOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setEditorOpen(false)}>取消</Button>
            <Button
              type="primary"
              onClick={async () => {
                const values = await form.validateFields();
                const payload = buildStorylinePayload(values);
                const response = editing?.storylineId
                  ? await updateAdminStoryline(editing.storylineId, payload)
                  : await createAdminStoryline(payload);
                if (!response.success) {
                  message.error(response.message || '故事線儲存失敗');
                  return;
                }
                message.success(editing ? '故事線已更新' : '故事線已建立');
                setEditorOpen(false);
                actionRef.current?.reload();
              }}
            >
              儲存
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          <Alert
            showIcon
            type="info"
            style={{ marginBottom: 16 }}
            message="支援多區域綁定"
            description="一條故事線可以同時綁定多個城市與多張子地圖，前台會依據這些關聯決定入口展示與解鎖範圍。"
          />

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item
              name="code"
              label="故事線代碼"
              rules={[{ required: true, message: '請輸入故事線代碼' }]}
              style={{ flex: 1 }}
            >
              <Input placeholder="例如 macau-war-route" />
            </Form.Item>
            <Form.Item name="difficulty" label="難度" style={{ flex: 1 }}>
              <Select options={difficultyOptions} />
            </Form.Item>
            <Form.Item name="estimatedMinutes" label="預估分鐘" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="sortOrder" label="排序" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
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
            <Form.Item name="cityBindings" label="綁定城市" style={{ flex: 1 }}>
              <Select
                mode="multiple"
                allowClear
                placeholder="可綁定多個城市"
                options={cities.map((city) => ({
                  label: `${pickCityName(city)} (${city.code})`,
                  value: city.id,
                }))}
              />
            </Form.Item>
            <Form.Item name="subMapBindings" label="綁定子地圖" style={{ flex: 1 }}>
              <Select
                mode="multiple"
                allowClear
                placeholder="可綁定多張子地圖"
                options={availableSubMaps.map((subMap) => ({
                  label: subMapLabelMap.get(subMap.id) || `${subMap.code}`,
                  value: subMap.id,
                }))}
              />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <div style={{ flex: 1 }}>
              <SpatialAssetPickerField
                name="coverAssetId"
                label="封面資源"
                assetKind="image"
                required
                help="故事線封面圖，前台卡片與故事詳情頁會優先使用。"
              />
            </div>
            <div style={{ flex: 1 }}>
              <SpatialAssetPickerField
                name="bannerAssetId"
                label="橫幅資源"
                assetKind="image"
                help="大型故事橫幅或導覽主視覺，可直接拖曳上傳。"
              />
            </div>
            <Form.Item name="status" label="狀態" style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
          </Space>

          <LocalizedFieldGroup
            form={form}
            label="故事線介紹"
            fieldNames={storylineDescriptionFields}
            multiline
            rows={5}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="完成獎勵標題"
            fieldNames={rewardBadgeFields}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <MediaAssetArrayField
            name="attachmentAssetIds"
            label="故事線附件媒體"
            help="可直接拖曳或從檔案夾上傳圖片、音訊或影片，系統會自動回填到附件清單。"
          />

          <Form.Item name="publishedAt" label="發布時間">
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default StorylineManagement;

