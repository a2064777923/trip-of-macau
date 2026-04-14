import React, { useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { useRequest } from 'ahooks';
import {
  Alert,
  Button,
  Card,
  Col,
  Drawer,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import {
  createAdminPoi,
  deleteAdminPoi,
  getAdminPoiDetail,
  getAdminPois,
  getAdminTranslationSettings,
  getCities,
  updateAdminPoi,
  type CityItem,
} from '../../services/api';
import type {
  AdminPoiDetail,
  AdminPoiListItem,
  AdminPoiPayload,
  AdminSubMapItem,
} from '../../types/admin';
import LocalizedFieldGroup, { buildLocalizedFieldNames } from '../../components/localization/LocalizedFieldGroup';
import SpatialAssetPickerField from '../../components/spatial/SpatialAssetPickerField';
import SpatialAttachmentListField from '../../components/spatial/SpatialAttachmentListField';
import SpatialCoordinateFieldGroup from '../../components/spatial/SpatialCoordinateFieldGroup';
import SpatialPopupDisplayField from '../../components/spatial/SpatialPopupDisplayField';

const { Paragraph, Text } = Typography;

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

const poiCategoryOptions = [
  { label: '歷史古蹟', value: 'historic' },
  { label: '博物館 / 展館', value: 'museum' },
  { label: '地標景點', value: 'landmark' },
  { label: '街區漫遊', value: 'neighbourhood' },
  { label: '餐飲美食', value: 'food' },
  { label: '購物商場', value: 'shopping' },
  { label: '校園地點', value: 'campus' },
  { label: '交通節點', value: 'transport' },
  { label: '活動場地', value: 'event' },
  { label: '服務設施', value: 'service' },
];

const CUSTOM_CATEGORY_VALUE = '__custom_category__';

const nameFields = buildLocalizedFieldNames('name');
const subtitleFields = buildLocalizedFieldNames('subtitle');
const addressFields = buildLocalizedFieldNames('address');
const introTitleFields = buildLocalizedFieldNames('introTitle');
const introSummaryFields = buildLocalizedFieldNames('introSummary');
const descriptionFields = buildLocalizedFieldNames('description');

const defaultPopupConfig = JSON.stringify(
  {
    enabled: false,
    mode: 'sheet',
    mediaUsageType: 'cover',
  },
  null,
  2,
);

const defaultDisplayConfig = JSON.stringify(
  {
    layout: 'card',
    theme: 'default',
    showSubtitle: true,
  },
  null,
  2,
);

interface AdminPoiFormValues extends AdminPoiPayload {
  categoryPreset?: string;
  categoryCustom?: string;
}

function pickCityName(city?: CityItem | null) {
  if (!city) {
    return '';
  }
  return city.nameZht || city.nameZh || city.nameEn || city.namePt || city.code;
}

function pickSubMapName(subMap?: AdminSubMapItem | null) {
  if (!subMap) {
    return '';
  }
  return subMap.nameZht || subMap.nameZh || subMap.nameEn || subMap.namePt || subMap.code;
}

function pickPoiName(record?: AdminPoiListItem | AdminPoiDetail | null) {
  if (!record) {
    return '';
  }
  return record.nameZht || record.nameZh || record.nameEn || record.namePt || record.code;
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

function resolveCategoryFields(categoryCode?: string | null) {
  if (!categoryCode) {
    return {};
  }
  const isPreset = poiCategoryOptions.some((item) => item.value === categoryCode);
  if (isPreset) {
    return { categoryPreset: categoryCode };
  }
  return {
    categoryPreset: CUSTOM_CATEGORY_VALUE,
    categoryCustom: categoryCode,
  };
}

function withPoiDefaults(detail?: Partial<AdminPoiDetail>): Partial<AdminPoiFormValues> {
  return {
    difficulty: detail?.difficulty || 'easy',
    triggerRadius: detail?.triggerRadius ?? 50,
    manualCheckinRadius: detail?.manualCheckinRadius ?? 200,
    staySeconds: detail?.staySeconds ?? 30,
    sourceCoordinateSystem: detail?.sourceCoordinateSystem || 'GCJ02',
    popupConfigJson: detail?.popupConfigJson || defaultPopupConfig,
    displayConfigJson: detail?.displayConfigJson || defaultDisplayConfig,
    attachments: detail?.attachments || [],
    sortOrder: detail?.sortOrder ?? 0,
    status: detail?.status || 'draft',
    ...resolveCategoryFields(detail?.categoryCode),
    ...detail,
  };
}

function buildPoiPayload(values: AdminPoiFormValues): AdminPoiPayload {
  const { categoryPreset, categoryCustom, ...rest } = values;
  const categoryCode =
    categoryPreset === CUSTOM_CATEGORY_VALUE ? categoryCustom?.trim() : categoryPreset?.trim();

  return {
    ...rest,
    categoryCode: categoryCode || undefined,
  };
}

const POIManagement: React.FC = () => {
  const [filters, setFilters] = useState<{ keyword?: string; cityId?: number; subMapId?: number }>({});
  const [editorOpen, setEditorOpen] = useState(false);
  const [editingPoi, setEditingPoi] = useState<AdminPoiDetail | null>(null);
  const [form] = Form.useForm<AdminPoiFormValues>();

  const translationSettingsRequest = useRequest(getAdminTranslationSettings);
  const citiesRequest = useRequest(() => getCities({ pageNum: 1, pageSize: 100 }), {});
  const poiRequest = useRequest(
    () =>
      getAdminPois({
        pageNum: 1,
        pageSize: 200,
        keyword: filters.keyword,
        cityId: filters.cityId,
        subMapId: filters.subMapId,
      }),
    {
      refreshDeps: [filters.keyword, filters.cityId, filters.subMapId],
    },
  );

  const cities = citiesRequest.data?.data?.list || [];
  const selectedCityId = Form.useWatch('cityId', form);
  const selectedCategoryPreset = Form.useWatch('categoryPreset', form);

  const formSubMaps = useMemo(
    () => cities.find((city) => city.id === selectedCityId)?.subMaps || [],
    [cities, selectedCityId],
  );
  const filterSubMaps = useMemo(
    () => cities.find((city) => city.id === filters.cityId)?.subMaps || [],
    [cities, filters.cityId],
  );

  const columns = useMemo(
    () => [
      {
        title: 'POI',
        key: 'poi',
        render: (_: unknown, record: AdminPoiListItem) => (
          <Space direction="vertical" size={0}>
            <Text strong>{pickPoiName(record)}</Text>
            <Text type="secondary">代碼：{record.code}</Text>
          </Space>
        ),
      },
      {
        title: '綁定地圖 / 子地圖',
        key: 'spatial',
        render: (_: unknown, record: AdminPoiListItem) => (
          <Space direction="vertical" size={0}>
            <Text>{record.cityName || '未設定城市'}</Text>
            <Text type="secondary">{record.subMapName || '未綁定子地圖'}</Text>
          </Space>
        ),
      },
      {
        title: '坐標',
        key: 'coordinate',
        render: (_: unknown, record: AdminPoiListItem) => `${record.latitude}, ${record.longitude}`,
      },
      {
        title: '分類',
        dataIndex: 'categoryCode',
        render: (value: string | undefined) => value || '未設定',
      },
      {
        title: '地圖圖標',
        key: 'mapIcon',
        render: (_: unknown, record: AdminPoiListItem) => record.mapIconAssetId || '未設定',
      },
      {
        title: '狀態',
        key: 'status',
        render: (_: unknown, record: AdminPoiListItem) => renderStatus(record.status),
      },
      {
        title: '操作',
        key: 'action',
        render: (_: unknown, record: AdminPoiListItem) => (
          <Space wrap>
            <Button type="link" icon={<EditOutlined />} onClick={() => void openEdit(record.poiId)}>
              編輯
            </Button>
            <Button type="link" danger icon={<DeleteOutlined />} onClick={() => void handleDelete(record.poiId)}>
              刪除
            </Button>
          </Space>
        ),
      },
    ],
    [],
  );

  const refreshPois = async () => {
    await poiRequest.refreshAsync();
  };

  const openCreate = () => {
    form.setFieldsValue({
      ...withPoiDefaults(),
      categoryPreset: 'landmark',
    });
    setEditingPoi(null);
    setEditorOpen(true);
  };

  const openEdit = async (poiId: number) => {
    const response = await getAdminPoiDetail(poiId);
    if (!response.success || !response.data) {
      message.error(response.message || '無法載入 POI 詳情');
      return;
    }
    form.setFieldsValue(withPoiDefaults(response.data));
    setEditingPoi(response.data);
    setEditorOpen(true);
  };

  const handleDelete = async (poiId: number) => {
    const response = await deleteAdminPoi(poiId);
    if (!response.success) {
      message.error(response.message || 'POI 刪除失敗');
      return;
    }
    message.success('POI 已刪除');
    await refreshPois();
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload = buildPoiPayload(values);
    if (editingPoi?.poiId) {
      const response = await updateAdminPoi(editingPoi.poiId, {
        ...payload,
        storylineId: editingPoi.storylineId,
      });
      if (!response.success) {
        throw new Error(response.message || 'POI 更新失敗');
      }
      message.success('POI 已更新');
    } else {
      const response = await createAdminPoi(payload);
      if (!response.success) {
        throw new Error(response.message || 'POI 建立失敗');
      }
      message.success('POI 已建立');
    }
    setEditorOpen(false);
    await refreshPois();
  };

  return (
    <PageContainer
      title="POI 管理"
      subTitle="以城市 / 子地圖綁定、地圖圖標、坐標換算、彈窗展示與多媒體附件為核心整理 POI。"
    >
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <Input.Search
            allowClear
            placeholder="搜尋 POI 代碼或名稱"
            style={{ width: 280 }}
            onSearch={(value) => setFilters((current) => ({ ...current, keyword: value || undefined }))}
          />
          <Select
            allowClear
            placeholder="城市"
            style={{ width: 220 }}
            options={cities.map((city) => ({
              label: `${pickCityName(city)} (${city.code})`,
              value: city.id,
            }))}
            onChange={(value) => setFilters((current) => ({ ...current, cityId: value, subMapId: undefined }))}
          />
          <Select
            allowClear
            placeholder="子地圖"
            style={{ width: 220 }}
            options={filterSubMaps.map((subMap) => ({
              label: `${pickSubMapName(subMap)} (${subMap.code})`,
              value: subMap.id,
            }))}
            onChange={(value) => setFilters((current) => ({ ...current, subMapId: value }))}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            新增 POI
          </Button>
        </Space>
      </Card>

      <Card>
        <Table<AdminPoiListItem>
          rowKey="poiId"
          loading={poiRequest.loading}
          columns={columns}
          dataSource={poiRequest.data?.data?.list || []}
          pagination={{ pageSize: 10 }}
          locale={{ emptyText: '尚未建立任何 POI' }}
        />
      </Card>

      <Drawer
        open={editorOpen}
        width={1180}
        destroyOnClose
        title={editingPoi ? `編輯 POI：${pickPoiName(editingPoi)}` : '新增 POI'}
        onClose={() => setEditorOpen(false)}
        extra={
          <Space>
            <Button onClick={() => setEditorOpen(false)}>取消</Button>
            <Button type="primary" onClick={() => void handleSubmit()}>
              儲存
            </Button>
          </Space>
        }
      >
        <Form form={form} layout="vertical">
          {editingPoi?.storylineName ? (
            <Alert
              showIcon
              type="info"
              style={{ marginBottom: 16 }}
              message="故事線回顯"
              description={`這個 POI 目前已被故事線「${editingPoi.storylineName}」引用；故事線綁定請到故事線 / 章節編排中維護。`}
            />
          ) : null}

          <Alert
            showIcon
            type="info"
            style={{ marginBottom: 16 }}
            message="區域資訊改為由地圖綁定推導"
            description="POI 不再獨立編寫行政區 / 分區。只要正確綁定城市與子地圖，前台展示與統計都會以這個綁定為準。"
          />

          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item
                name="cityId"
                label="所屬城市"
                rules={[{ required: true, message: '請選擇所屬城市' }]}
              >
                <Select
                  options={cities.map((city) => ({
                    label: `${pickCityName(city)} (${city.code})`,
                    value: city.id,
                  }))}
                  onChange={() => form.setFieldValue('subMapId', undefined)}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="subMapId" label="所屬子地圖">
                <Select
                  allowClear
                  placeholder="可不綁定"
                  options={formSubMaps.map((subMap) => ({
                    label: `${pickSubMapName(subMap)} (${subMap.code})`,
                    value: subMap.id,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item
                name="code"
                label="POI 代碼"
                rules={[{ required: true, message: '請輸入 POI 代碼' }]}
              >
                <Input placeholder="例如 a-ma-temple" />
              </Form.Item>
            </Col>
          </Row>

          <LocalizedFieldGroup
            form={form}
            label="POI 名稱"
            fieldNames={nameFields}
            required
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="POI 副標"
            fieldNames={subtitleFields}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <SpatialCoordinateFieldGroup
            form={form}
            required
            sourceSystemName="sourceCoordinateSystem"
            sourceLatitudeName="sourceLatitude"
            sourceLongitudeName="sourceLongitude"
            normalizedLatitudeName="latitude"
            normalizedLongitudeName="longitude"
          />

          <LocalizedFieldGroup
            form={form}
            label="地址"
            fieldNames={addressFields}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item name="categoryPreset" label="分類代碼">
                <Select
                  options={[
                    ...poiCategoryOptions,
                    { label: '自定義分類', value: CUSTOM_CATEGORY_VALUE },
                  ]}
                  placeholder="請選擇分類"
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="difficulty" label="難度">
                <Select options={difficultyOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="triggerRadius" label="自動觸發半徑（米）">
                <InputNumber style={{ width: '100%' }} min={10} />
              </Form.Item>
            </Col>
          </Row>

          {selectedCategoryPreset === CUSTOM_CATEGORY_VALUE ? (
            <Form.Item
              name="categoryCustom"
              label="自定義分類代碼"
              rules={[{ required: true, message: '請輸入自定義分類代碼' }]}
            >
              <Input placeholder="例如 heritage-route / hidden-scene / art-space" />
            </Form.Item>
          ) : null}

          <Form.Item name="manualCheckinRadius" label="手動打卡半徑（米）">
            <InputNumber style={{ width: '100%' }} min={10} />
          </Form.Item>

          <Row gutter={16}>
            <Col xs={24} md={8}>
              <SpatialAssetPickerField
                name="coverAssetId"
                label="封面資源"
                required
                assetKind="image"
                help="可直接上傳圖片，成功後會自動選取。"
              />
            </Col>
            <Col xs={24} md={8}>
              <SpatialAssetPickerField
                name="mapIconAssetId"
                label="地圖圖標資源"
                required
                assetKind="icon"
                help="可直接上傳 icon / 小圖標，地圖展示會使用這個資源。"
              />
            </Col>
            <Col xs={24} md={8}>
              <SpatialAssetPickerField
                name="audioAssetId"
                label="音訊資源"
                assetKind="audio"
                help="可選配語音導覽或現場聲景。"
              />
            </Col>
          </Row>

          <Form.Item name="staySeconds" label="建議停留秒數">
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>

          <LocalizedFieldGroup
            form={form}
            label="導覽標題"
            fieldNames={introTitleFields}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="導覽摘要"
            fieldNames={introSummaryFields}
            multiline
            rows={4}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={form}
            label="POI 詳細介紹"
            fieldNames={descriptionFields}
            multiline
            rows={6}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <SpatialPopupDisplayField
            form={form}
            popupFieldName="popupConfigJson"
            displayFieldName="displayConfigJson"
          />

          <SpatialAttachmentListField name="attachments" title="POI 附件" />

          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item name="sortOrder" label="排序">
                <InputNumber style={{ width: '100%' }} min={0} />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="status" label="狀態">
                <Select options={statusOptions} />
              </Form.Item>
            </Col>
          </Row>

          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            故事線與章節的綁定不在這裡主動維護。若某條故事線引用了這個 POI，系統會在此頁回顯對應資訊。
          </Paragraph>
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default POIManagement;
