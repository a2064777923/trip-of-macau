import React, { useMemo, useRef, useState } from 'react';
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
  Switch,
  Tag,
  Typography,
  message,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import dayjs, { type Dayjs } from 'dayjs';
import {
  createAdminActivity,
  deleteAdminActivity,
  getAdminActivities,
  getAdminActivityDetail,
  getAdminStorylines,
  getAdminTranslationSettings,
  getCities,
  updateAdminActivity,
} from '../../services/api';
import type {
  AdminActivityItem,
  AdminActivityPayload,
  AdminStorylineListItem,
  CityItem,
} from '../../types/admin';
import LocalizedFieldGroup, {
  buildLocalizedFieldNames,
} from '../../components/localization/LocalizedFieldGroup';
import SpatialAssetPickerField from '../../components/spatial/SpatialAssetPickerField';
import MediaAssetArrayField from '../../components/media/MediaAssetArrayField';

const { Text } = Typography;

const titleFields = buildLocalizedFieldNames('title');
const summaryFields = buildLocalizedFieldNames('summary');
const descriptionFields = buildLocalizedFieldNames('description');
const htmlFields = buildLocalizedFieldNames('html');
const venueFields = buildLocalizedFieldNames('venueName');
const addressFields = buildLocalizedFieldNames('address');

const activityTypeOptions = [
  { label: '官方活動', value: 'official_event' },
  { label: '私人活動', value: 'private_event' },
  { label: '發現頁活動', value: 'discovery_campaign' },
  { label: '全域任務', value: 'global_task' },
];

const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已發布', value: 'published' },
  { label: '已結束', value: 'ended' },
  { label: '已取消', value: 'cancelled' },
];

interface ActivityFormValues extends Omit<AdminActivityPayload, 'signupStartAt' | 'signupEndAt' | 'publishStartAt' | 'publishEndAt'> {
  signupStartAt?: Dayjs | null;
  signupEndAt?: Dayjs | null;
  publishStartAt?: Dayjs | null;
  publishEndAt?: Dayjs | null;
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

function pickStorylineName(record?: Partial<AdminStorylineListItem> | null) {
  if (!record) {
    return '';
  }
  return record.nameZht || record.nameZh || record.nameEn || record.namePt || record.code || '';
}

function pickCityName(record?: CityItem | null) {
  if (!record) {
    return '';
  }
  return record.nameZht || record.nameZh || record.nameEn || record.namePt || record.code;
}

function withActivityDefaults(detail?: Partial<AdminActivityItem>): Partial<ActivityFormValues> {
  return {
    ...detail,
    activityType: detail?.activityType || 'official_event',
    status: detail?.status || 'draft',
    isPinned: detail?.isPinned || 0,
    participationCount: detail?.participationCount ?? 0,
    sortOrder: detail?.sortOrder ?? 0,
    signupStartAt: detail?.signupStartAt ? dayjs(detail.signupStartAt) : undefined,
    signupEndAt: detail?.signupEndAt ? dayjs(detail.signupEndAt) : undefined,
    publishStartAt: detail?.publishStartAt ? dayjs(detail.publishStartAt) : undefined,
    publishEndAt: detail?.publishEndAt ? dayjs(detail.publishEndAt) : undefined,
    cityBindings: detail?.cityBindings || [],
    subMapBindings: detail?.subMapBindings || [],
    storylineBindings: detail?.storylineBindings || [],
    attachmentAssetIds: detail?.attachmentAssetIds || [],
  };
}

function buildActivityPayload(values: ActivityFormValues): AdminActivityPayload {
  const normalized = applyLocalizedFallback(values, ['title', 'summary', 'description', 'html', 'venueName', 'address']);
  return {
    ...normalized,
    titleZh: firstText(normalized.titleZh, normalized.titleZht) || '',
    titleZht: firstText(normalized.titleZht, normalized.titleZh),
    summaryZh: firstText(normalized.summaryZh, normalized.summaryZht, normalized.descriptionZh, normalized.descriptionZht),
    summaryZht: firstText(normalized.summaryZht, normalized.summaryZh, normalized.descriptionZht, normalized.descriptionZh),
    descriptionZh: firstText(normalized.descriptionZh, normalized.summaryZh),
    descriptionZht: firstText(normalized.descriptionZht, normalized.descriptionZh, normalized.summaryZht, normalized.summaryZh),
    signupStartAt: normalized.signupStartAt ? normalized.signupStartAt.format('YYYY-MM-DDTHH:mm:ss') : null,
    signupEndAt: normalized.signupEndAt ? normalized.signupEndAt.format('YYYY-MM-DDTHH:mm:ss') : null,
    publishStartAt: normalized.publishStartAt ? normalized.publishStartAt.format('YYYY-MM-DDTHH:mm:ss') : null,
    publishEndAt: normalized.publishEndAt ? normalized.publishEndAt.format('YYYY-MM-DDTHH:mm:ss') : null,
    isPinned: normalized.isPinned ? 1 : 0,
    cityBindings: normalized.cityBindings || [],
    subMapBindings: normalized.subMapBindings || [],
    storylineBindings: normalized.storylineBindings || [],
    attachmentAssetIds: normalized.attachmentAssetIds || [],
  };
}

function renderStatus(status?: string) {
  if (status === 'published') {
    return <Tag color="green">已發布</Tag>;
  }
  if (status === 'ended') {
    return <Tag color="blue">已結束</Tag>;
  }
  if (status === 'cancelled') {
    return <Tag color="red">已取消</Tag>;
  }
  return <Tag color="gold">草稿</Tag>;
}

const OperationsManagement: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [editorOpen, setEditorOpen] = useState(false);
  const [editing, setEditing] = useState<AdminActivityItem | null>(null);
  const [form] = Form.useForm<ActivityFormValues>();

  const translationSettingsRequest = useRequest(getAdminTranslationSettings);
  const citiesRequest = useRequest(() => getCities({ pageNum: 1, pageSize: 100 }));
  const storylineRequest = useRequest(() => getAdminStorylines({ pageNum: 1, pageSize: 200 }));

  const cities = citiesRequest.data?.data?.list || [];
  const storylines = storylineRequest.data?.data?.list || [];
  const selectedCityBindings = Form.useWatch('cityBindings', form) as number[] | undefined;

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

  const subMapOptions = useMemo(() => {
    if (!selectedCityBindings?.length) {
      return allSubMaps;
    }
    return allSubMaps.filter((subMap) => selectedCityBindings.includes(subMap.cityId));
  }, [allSubMaps, selectedCityBindings]);

  const columns = useMemo<ProColumns<AdminActivityItem>[]>(
    () => [
      {
        title: '活動 / 任務',
        key: 'title',
        render: (_, record) => (
          <Space direction="vertical" size={0}>
            <Text strong>{record.titleZht || record.titleZh || record.title}</Text>
            <Text type="secondary">{record.code}</Text>
          </Space>
        ),
      },
      {
        title: '類型',
        dataIndex: 'activityType',
        valueType: 'select',
        valueEnum: {
          official_event: { text: '官方活動' },
          private_event: { text: '私人活動' },
          discovery_campaign: { text: '發現頁活動' },
          global_task: { text: '全域任務' },
        },
        render: (_, record) => (
          <Tag color={record.activityType === 'global_task' ? 'purple' : record.activityType === 'private_event' ? 'orange' : 'blue'}>
            {activityTypeOptions.find((item) => item.value === record.activityType)?.label || record.activityType || '未設定'}
          </Tag>
        ),
      },
      {
        title: '發布時段',
        hideInSearch: true,
        render: (_, record) => (
          <Space direction="vertical" size={0}>
            <Text>{record.publishStartAt || '未設定開始'}</Text>
            <Text type="secondary">{record.publishEndAt || '未設定結束'}</Text>
          </Space>
        ),
      },
      {
        title: '主辦 / 報名',
        hideInSearch: true,
        render: (_, record) => (
          <Space direction="vertical" size={0}>
            <Text>{record.organizerName || '未設定主辦方'}</Text>
            <Text type="secondary">
              {record.signupCapacity ? `名額 ${record.signupCapacity}` : '不限名額'}
              {typeof record.signupFeeAmount === 'number' ? ` / MOP ${record.signupFeeAmount}` : ''}
            </Text>
          </Space>
        ),
      },
      {
        title: '置頂 / 狀態',
        hideInSearch: true,
        render: (_, record) => (
          <Space wrap>
            {record.isPinned ? <Tag color="volcano">置頂</Tag> : null}
            {renderStatus(record.status)}
          </Space>
        ),
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
              const response = await getAdminActivityDetail(record.id);
              if (!response.success || !response.data) {
                message.error(response.message || '無法載入活動詳情');
                return;
              }
              setEditing(response.data);
              form.setFieldsValue(withActivityDefaults(response.data));
              setEditorOpen(true);
            }}
          >
            編輯
          </Button>,
          <Popconfirm
            key="delete"
            title="確定刪除這個活動 / 任務？"
            onConfirm={async () => {
              const response = await deleteAdminActivity(record.id);
              if (!response.success) {
                message.error(response.message || '刪除失敗');
                return;
              }
              message.success('活動 / 任務已刪除');
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
      title="任務與活動"
      subTitle="這裡是正式的活動 / 任務 authoring 介面，可配置四語內容、HTML 圖文、報名資訊、上線下線時段、置頂策略與地圖 / 故事線綁定。"
      extra={[
        <Button
          key="add"
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setEditing(null);
            form.resetFields();
            form.setFieldsValue(withActivityDefaults());
            setEditorOpen(true);
          }}
        >
          新增活動 / 任務
        </Button>,
      ]}
    >
      <Alert
        showIcon
        type="info"
        style={{ marginBottom: 16 }}
        message="活動與任務共用一套資料模型"
        description="請用「類型」區分官方活動、私人活動、發現頁活動與全域任務。封面、主視覺與附件媒體都走中央媒體庫與上傳管線。"
      />

      <ProTable<AdminActivityItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const response = await getAdminActivities({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.code as string,
            status: params.status as string,
            activityType: params.activityType as string,
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
        width={1100}
        destroyOnHidden
        title={editing ? `編輯活動：${editing.titleZht || editing.titleZh || editing.title}` : '新增活動 / 任務'}
        onClose={() => setEditorOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setEditorOpen(false)}>取消</Button>
            <Button
              type="primary"
              onClick={async () => {
                const values = await form.validateFields();
                const payload = buildActivityPayload(values);
                const response = editing?.id
                  ? await updateAdminActivity(editing.id, payload)
                  : await createAdminActivity(payload);
                if (!response.success) {
                  message.error(response.message || '活動 / 任務儲存失敗');
                  return;
                }
                message.success(editing ? '活動 / 任務已更新' : '活動 / 任務已建立');
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
          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item
              name="code"
              label="活動代碼"
              rules={[{ required: true, message: '請輸入活動代碼' }]}
              style={{ flex: 1 }}
            >
              <Input placeholder="例如 macau-fort-night-walk" />
            </Form.Item>
            <Form.Item name="activityType" label="類型" style={{ flex: 1 }}>
              <Select options={activityTypeOptions} />
            </Form.Item>
            <Form.Item name="sortOrder" label="排序" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="status" label="狀態" style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
          </Space>

          <LocalizedFieldGroup
            form={form}
            label="活動標題"
            fieldNames={titleFields}
            required
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="活動摘要"
            fieldNames={summaryFields}
            multiline
            rows={4}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="活動詳情"
            fieldNames={descriptionFields}
            multiline
            rows={5}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="HTML 圖文內容"
            fieldNames={htmlFields}
            multiline
            rows={6}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="場地名稱"
            fieldNames={venueFields}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="地址"
            fieldNames={addressFields}
            multiline
            rows={3}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="cityBindings" label="綁定城市" style={{ flex: 1 }}>
              <Select
                mode="multiple"
                allowClear
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
                options={subMapOptions.map((subMap) => ({
                  label: `${subMap.nameZht || subMap.nameZh || subMap.code} / ${pickCityName(cities.find((city) => city.id === subMap.cityId))}`,
                  value: subMap.id,
                }))}
              />
            </Form.Item>
            <Form.Item name="storylineBindings" label="綁定故事線" style={{ flex: 1 }}>
              <Select
                mode="multiple"
                allowClear
                options={storylines.map((storyline) => ({
                  label: `${pickStorylineName(storyline)} (${storyline.code})`,
                  value: storyline.storylineId,
                }))}
              />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="organizerName" label="主辦方" style={{ flex: 1 }}>
              <Input />
            </Form.Item>
            <Form.Item name="organizerContact" label="聯絡方式" style={{ flex: 1 }}>
              <Input />
            </Form.Item>
            <Form.Item name="organizerWebsite" label="官方網址" style={{ flex: 1 }}>
              <Input />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="signupCapacity" label="報名名額" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="signupFeeAmount" label="報名費 (MOP)" style={{ flex: 1 }}>
              <InputNumber min={0} precision={2} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="participationCount" label="目前參與人數" style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="isPinned" label="置頂顯示" valuePropName="checked" style={{ flex: 1 }}>
              <Switch checkedChildren="置頂" unCheckedChildren="一般" />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="signupStartAt" label="報名開始" style={{ flex: 1 }}>
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="signupEndAt" label="報名結束" style={{ flex: 1 }}>
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="publishStartAt" label="上線時間" style={{ flex: 1 }}>
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="publishEndAt" label="下線時間" style={{ flex: 1 }}>
              <DatePicker showTime style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <div style={{ flex: 1 }}>
              <SpatialAssetPickerField
                name="coverAssetId"
                label="封面資源"
                assetKind="image"
                help="活動卡片封面圖，前台發現頁與活動列表會優先使用。"
              />
            </div>
            <div style={{ flex: 1 }}>
              <SpatialAssetPickerField
                name="heroAssetId"
                label="主視覺資源"
                assetKind="image"
                help="活動詳情頁的大圖或橫幅主視覺。"
              />
            </div>
          </Space>

          <MediaAssetArrayField
            name="attachmentAssetIds"
            label="活動附件媒體"
            help="可直接拖放圖片、音訊、影片上傳，儲存後會以既定順序掛到活動詳情頁。"
          />
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default OperationsManagement;

