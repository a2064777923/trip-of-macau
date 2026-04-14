import React, { useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import { useRequest } from 'ahooks';
import {
  Button,
  Card,
  Col,
  Descriptions,
  Empty,
  Form,
  Input,
  InputNumber,
  List,
  Modal,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import {
  CheckCircleOutlined,
  EditOutlined,
  EnvironmentOutlined,
  PlusOutlined,
  RadarChartOutlined,
} from '@ant-design/icons';
import {
  createCity,
  createSubMap,
  getAdminTranslationSettings,
  getCities,
  getCityDetail,
  getSubMapDetail,
  publishCity,
  publishSubMap,
  suggestSpatialMetadata,
  updateCity,
  updateSubMap,
  type CityItem,
} from '../../services/api';
import type { AdminCityPayload, AdminSubMapItem, AdminSubMapPayload } from '../../types/admin';
import LocalizedFieldGroup, { buildLocalizedFieldNames } from '../../components/localization/LocalizedFieldGroup';
import SpatialAssetPickerField from '../../components/spatial/SpatialAssetPickerField';
import SpatialAttachmentListField from '../../components/spatial/SpatialAttachmentListField';
import SpatialCoordinateFieldGroup from '../../components/spatial/SpatialCoordinateFieldGroup';
import SpatialPopupDisplayField from '../../components/spatial/SpatialPopupDisplayField';
import SpatialUnlockConditionField from '../../components/spatial/SpatialUnlockConditionField';

const { Paragraph, Text, Title } = Typography;

const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已發布', value: 'published' },
  { label: '已封存', value: 'archived' },
];

const countryOptions = [
  { label: '澳門', value: 'MO' },
  { label: '香港', value: 'HK' },
  { label: '中國內地', value: 'CN' },
  { label: '其他', value: 'OTHER' },
];

const nameFields = buildLocalizedFieldNames('name');
const subtitleFields = buildLocalizedFieldNames('subtitle');
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

function pickCityName(record?: CityItem | null) {
  if (!record) {
    return '';
  }
  return record.nameZht || record.nameZh || record.nameEn || record.namePt || record.code;
}

function pickSubMapName(record?: AdminSubMapItem | null) {
  if (!record) {
    return '';
  }
  return record.nameZht || record.nameZh || record.nameEn || record.namePt || record.code;
}

function pickCountryLabel(record?: Partial<CityItem> | null) {
  if (!record?.countryCode) {
    return '未設定';
  }
  if (record.countryCode === 'OTHER') {
    return record.customCountryName?.trim() || '其他';
  }
  return countryOptions.find((item) => item.value === record.countryCode)?.label || record.countryCode;
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

function withCityDefaults(city?: Partial<CityItem>): Partial<AdminCityPayload> {
  return {
    countryCode: city?.countryCode || 'MO',
    customCountryName: city?.customCountryName,
    defaultZoom: city?.defaultZoom ?? 13,
    unlockType: city?.unlockType || 'default',
    sourceCoordinateSystem: city?.sourceCoordinateSystem || 'GCJ02',
    popupConfigJson: city?.popupConfigJson || defaultPopupConfig,
    displayConfigJson: city?.displayConfigJson || defaultDisplayConfig,
    attachments: city?.attachments || [],
    sortOrder: city?.sortOrder ?? 0,
    status: city?.status || 'draft',
    ...city,
  };
}

function withSubMapDefaults(subMap?: Partial<AdminSubMapItem>, cityId?: number): Partial<AdminSubMapPayload> {
  return {
    cityId: subMap?.cityId ?? cityId ?? 0,
    sourceCoordinateSystem: subMap?.sourceCoordinateSystem || 'GCJ02',
    popupConfigJson: subMap?.popupConfigJson || defaultPopupConfig,
    displayConfigJson: subMap?.displayConfigJson || defaultDisplayConfig,
    attachments: subMap?.attachments || [],
    sortOrder: subMap?.sortOrder ?? 0,
    status: subMap?.status || 'draft',
    ...subMap,
  };
}

const CityManagement: React.FC = () => {
  const [filters, setFilters] = useState<{ keyword?: string; status?: string }>({});
  const [cityModalOpen, setCityModalOpen] = useState(false);
  const [subMapModalOpen, setSubMapModalOpen] = useState(false);
  const [editingCityId, setEditingCityId] = useState<number | null>(null);
  const [editingSubMapId, setEditingSubMapId] = useState<number | null>(null);
  const [currentParentCity, setCurrentParentCity] = useState<CityItem | null>(null);
  const [cityForm] = Form.useForm<AdminCityPayload>();
  const [subMapForm] = Form.useForm<AdminSubMapPayload>();

  const selectedCountryCode = Form.useWatch('countryCode', cityForm);

  const translationSettingsRequest = useRequest(getAdminTranslationSettings);
  const citiesRequest = useRequest(
    () =>
      getCities({
        pageNum: 1,
        pageSize: 100,
        keyword: filters.keyword,
        status: filters.status,
      }),
    {
      refreshDeps: [filters.keyword, filters.status],
    },
  );

  const cities = citiesRequest.data?.data?.list || [];

  const buildSubMapColumns = (city: CityItem) => [
    {
      title: '子地圖',
      key: 'name',
      render: (_: unknown, record: AdminSubMapItem) => (
        <Space direction="vertical" size={0}>
          <Text strong>{pickSubMapName(record)}</Text>
          <Text type="secondary">代碼：{record.code}</Text>
        </Space>
      ),
    },
    {
      title: '中心坐標',
      key: 'center',
      render: (_: unknown, record: AdminSubMapItem) =>
        record.centerLat != null && record.centerLng != null ? `${record.centerLat}, ${record.centerLng}` : '未設定',
    },
    {
      title: '附件數量',
      key: 'attachments',
      render: (_: unknown, record: AdminSubMapItem) => record.attachments?.length || 0,
    },
    {
      title: '狀態',
      key: 'status',
      render: (_: unknown, record: AdminSubMapItem) => renderStatus(record.status),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: AdminSubMapItem) => (
        <Space wrap>
          <Button type="link" icon={<EditOutlined />} onClick={() => void openSubMapEditor(city, record)}>
            編輯
          </Button>
          {record.status !== 'published' ? (
            <Button type="link" icon={<CheckCircleOutlined />} onClick={() => void handlePublishSubMap(record.id)}>
              發布
            </Button>
          ) : null}
        </Space>
      ),
    },
  ];

  const refreshCities = async () => {
    await citiesRequest.refreshAsync();
  };

  const openCityEditor = async (city?: CityItem) => {
    if (city?.id) {
      const response = await getCityDetail(city.id);
      if (!response.success || !response.data) {
        message.error(response.message || '無法載入城市詳情');
        return;
      }
      cityForm.setFieldsValue(withCityDefaults(response.data));
      setEditingCityId(city.id);
    } else {
      cityForm.setFieldsValue(withCityDefaults());
      setEditingCityId(null);
    }
    setCityModalOpen(true);
  };

  const openSubMapEditor = async (city: CityItem | null, subMap?: AdminSubMapItem) => {
    if (!city?.id && !subMap?.cityId) {
      message.warning('請先選定所屬城市。');
      return;
    }
    setCurrentParentCity(city);
    if (subMap?.id) {
      const response = await getSubMapDetail(subMap.id);
      if (!response.success || !response.data) {
        message.error(response.message || '無法載入子地圖詳情');
        return;
      }
      subMapForm.setFieldsValue(withSubMapDefaults(response.data, city?.id || response.data.cityId));
      setEditingSubMapId(subMap.id);
    } else {
      subMapForm.setFieldsValue(withSubMapDefaults(undefined, city?.id));
      setEditingSubMapId(null);
    }
    setSubMapModalOpen(true);
  };

  const handleSuggest = async (entityType: 'city' | 'sub_map') => {
    const form = entityType === 'city' ? cityForm : subMapForm;
    const values = form.getFieldsValue() as unknown as Record<string, unknown>;
    const response = await suggestSpatialMetadata({
      entityType,
      code: values.code as string | undefined,
      nameZh: values.nameZh as string | undefined,
      nameEn: values.nameEn as string | undefined,
      nameZht: values.nameZht as string | undefined,
    });
    if (!response.success || !response.data) {
      message.error(response.message || '無法取得建議資料');
      return;
    }

    if (entityType === 'city') {
      cityForm.setFieldsValue({
        code: (values.code as string | undefined) || response.data.code,
        countryCode: response.data.countryCode || (values.countryCode as string | undefined),
        sourceCoordinateSystem:
          response.data.sourceCoordinateSystem || (values.sourceCoordinateSystem as AdminCityPayload['sourceCoordinateSystem']),
        sourceCenterLat: response.data.suggestedCenterLat ?? (values.sourceCenterLat as number | undefined),
        sourceCenterLng: response.data.suggestedCenterLng ?? (values.sourceCenterLng as number | undefined),
        centerLat: response.data.suggestedCenterLat ?? (values.centerLat as number | undefined),
        centerLng: response.data.suggestedCenterLng ?? (values.centerLng as number | undefined),
        defaultZoom: response.data.defaultZoom ?? (values.defaultZoom as number | undefined),
      });
    } else {
      subMapForm.setFieldsValue({
        code: (values.code as string | undefined) || response.data.code,
        sourceCoordinateSystem:
          response.data.sourceCoordinateSystem || (values.sourceCoordinateSystem as AdminSubMapPayload['sourceCoordinateSystem']),
        sourceCenterLat: response.data.suggestedCenterLat ?? (values.sourceCenterLat as number | undefined),
        sourceCenterLng: response.data.suggestedCenterLng ?? (values.sourceCenterLng as number | undefined),
        centerLat: response.data.suggestedCenterLat ?? (values.centerLat as number | undefined),
        centerLng: response.data.suggestedCenterLng ?? (values.centerLng as number | undefined),
      });
    }

    message.success(response.data.note || '已套用建議資料');
  };

  const handleSaveCity = async () => {
    const values = await cityForm.validateFields();
    const payload: AdminCityPayload = {
      ...values,
      customCountryName: values.countryCode === 'OTHER' ? values.customCountryName?.trim() : undefined,
    };

    if (editingCityId) {
      const response = await updateCity(editingCityId, payload);
      if (!response.success) {
        throw new Error(response.message || '城市更新失敗');
      }
      message.success('城市已更新');
    } else {
      const response = await createCity(payload);
      if (!response.success) {
        throw new Error(response.message || '城市建立失敗');
      }
      message.success('城市已建立');
    }
    setCityModalOpen(false);
    await refreshCities();
  };

  const handleSaveSubMap = async () => {
    const values = await subMapForm.validateFields();
    if (editingSubMapId) {
      const response = await updateSubMap(editingSubMapId, values);
      if (!response.success) {
        throw new Error(response.message || '子地圖更新失敗');
      }
      message.success('子地圖已更新');
    } else {
      const response = await createSubMap(values);
      if (!response.success) {
        throw new Error(response.message || '子地圖建立失敗');
      }
      message.success('子地圖已建立');
    }
    setSubMapModalOpen(false);
    await refreshCities();
  };

  const handlePublishCity = async (cityId: number) => {
    const response = await publishCity(cityId);
    if (!response.success) {
      message.error(response.message || '城市發布失敗');
      return;
    }
    message.success('城市已發布');
    await refreshCities();
  };

  const handlePublishSubMap = async (subMapId: number) => {
    const response = await publishSubMap(subMapId);
    if (!response.success) {
      message.error(response.message || '子地圖發布失敗');
      return;
    }
    message.success('子地圖已發布');
    await refreshCities();
  };

  return (
    <PageContainer
      title="城市與子地圖管理"
      subTitle="集中管理大地圖、子地圖、中心坐標、封面資源、彈窗展示與附件媒體。"
    >
      <Card style={{ marginBottom: 16 }}>
        <Space wrap>
          <Input.Search
            allowClear
            placeholder="搜尋城市代碼或名稱"
            style={{ width: 280 }}
            onSearch={(value) => setFilters((current) => ({ ...current, keyword: value || undefined }))}
          />
          <Select
            allowClear
            placeholder="狀態"
            style={{ width: 180 }}
            options={statusOptions}
            onChange={(value) => setFilters((current) => ({ ...current, status: value }))}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => void openCityEditor()}>
            新增城市
          </Button>
        </Space>
      </Card>

      <List
        loading={citiesRequest.loading}
        dataSource={cities}
        locale={{ emptyText: <Empty description="尚未建立任何城市" /> }}
        renderItem={(city) => (
          <List.Item style={{ padding: 0, marginBottom: 16, display: 'block' }}>
            <Card
              title={
                <Space wrap>
                  <Title level={4} style={{ margin: 0 }}>
                    {pickCityName(city)}
                  </Title>
                  {renderStatus(city.status)}
                  <Tag icon={<EnvironmentOutlined />}>{city.code}</Tag>
                  <Tag>{pickCountryLabel(city)}</Tag>
                </Space>
              }
              extra={
                <Space wrap>
                  <Button icon={<EditOutlined />} onClick={() => void openCityEditor(city)}>
                    編輯城市
                  </Button>
                  <Button type="primary" icon={<PlusOutlined />} onClick={() => void openSubMapEditor(city)}>
                    新增子地圖
                  </Button>
                  {city.status !== 'published' ? (
                    <Button icon={<CheckCircleOutlined />} onClick={() => void handlePublishCity(city.id)}>
                      發布城市
                    </Button>
                  ) : null}
                </Space>
              }
            >
              <Row gutter={[16, 16]}>
                <Col xs={24} md={16}>
                  <Descriptions column={{ xs: 1, md: 2 }} size="small" bordered>
                    <Descriptions.Item label="國家 / 地區">{pickCountryLabel(city)}</Descriptions.Item>
                    <Descriptions.Item label="中心坐標">
                      {city.centerLat != null && city.centerLng != null ? `${city.centerLat}, ${city.centerLng}` : '未設定'}
                    </Descriptions.Item>
                    <Descriptions.Item label="來源坐標系">{city.sourceCoordinateSystem || 'GCJ02'}</Descriptions.Item>
                    <Descriptions.Item label="預設縮放">{city.defaultZoom ?? '未設定'}</Descriptions.Item>
                    <Descriptions.Item label="附件數量">{city.attachments?.length || 0}</Descriptions.Item>
                    <Descriptions.Item label="封面資源">{city.coverAssetId || '未設定'}</Descriptions.Item>
                    <Descriptions.Item label="解鎖方式">{city.unlockType || 'default'}</Descriptions.Item>
                  </Descriptions>
                  <Paragraph style={{ marginTop: 12, marginBottom: 0 }} type="secondary">
                    {city.descriptionZht || city.descriptionZh || '尚未填寫城市介紹。'}
                  </Paragraph>
                </Col>
                <Col xs={24} md={8}>
                  <Card size="small" title="子地圖列表">
                    <Table
                      rowKey="id"
                      size="small"
                      pagination={false}
                      columns={buildSubMapColumns(city)}
                      dataSource={(city.subMaps || []).map((item) => ({
                        ...item,
                        cityId: city.id,
                      }))}
                      locale={{ emptyText: '尚未建立子地圖' }}
                    />
                  </Card>
                </Col>
              </Row>
            </Card>
          </List.Item>
        )}
      />

      <Modal
        open={cityModalOpen}
        width={1200}
        destroyOnClose
        title={editingCityId ? '編輯城市' : '新增城市'}
        onCancel={() => setCityModalOpen(false)}
        onOk={() => void handleSaveCity()}
      >
        <Form form={cityForm} layout="vertical">
          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item
                name="code"
                label="城市代碼"
                rules={[{ required: true, message: '請輸入城市代碼' }]}
              >
                <Input placeholder="例如 macau / hong-kong / ecnu" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="countryCode" label="所屬國家 / 地區">
                <Select options={countryOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="defaultZoom" label="預設縮放">
                <InputNumber style={{ width: '100%' }} min={1} max={22} />
              </Form.Item>
            </Col>
          </Row>

          {selectedCountryCode === 'OTHER' ? (
            <Form.Item
              name="customCountryName"
              label="自定義國家 / 地區名稱"
              rules={[{ required: true, message: '請輸入自定義國家 / 地區名稱' }]}
            >
              <Input placeholder="例如 Portugal / United Kingdom / France" />
            </Form.Item>
          ) : null}

          <Space style={{ marginBottom: 12 }}>
            <Button icon={<RadarChartOutlined />} onClick={() => void handleSuggest('city')}>
              自動補齊建議資料
            </Button>
            <Text type="secondary">會根據代碼與名稱補全國家、中心坐標與縮放建議。</Text>
          </Space>

          <LocalizedFieldGroup
            form={cityForm}
            label="城市名稱"
            fieldNames={nameFields}
            required
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={cityForm}
            label="城市副標"
            fieldNames={subtitleFields}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <SpatialCoordinateFieldGroup
            form={cityForm}
            required
            sourceSystemName="sourceCoordinateSystem"
            sourceLatitudeName="sourceCenterLat"
            sourceLongitudeName="sourceCenterLng"
            normalizedLatitudeName="centerLat"
            normalizedLongitudeName="centerLng"
          />

          <Row gutter={16}>
            <Col xs={24} md={12}>
              <SpatialAssetPickerField
                name="coverAssetId"
                label="封面資源"
                required
                assetKind="image"
                help="可直接上傳封面圖，成功後會自動綁定。"
              />
            </Col>
            <Col xs={24} md={12}>
              <SpatialAssetPickerField
                name="bannerAssetId"
                label="橫幅資源"
                assetKind="image"
                help="可上傳城市橫幅、宣傳圖或長圖。"
              />
            </Col>
          </Row>

          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item name="unlockType" label="解鎖方式">
                <Select
                  options={[
                    { label: '預設開放', value: 'default' },
                    { label: '手動開放', value: 'manual' },
                    { label: '條件解鎖', value: 'condition' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>

          <SpatialUnlockConditionField
            form={cityForm}
            unlockTypeName="unlockType"
            unlockConditionName="unlockConditionJson"
          />

          <LocalizedFieldGroup
            form={cityForm}
            label="城市介紹"
            fieldNames={descriptionFields}
            multiline
            rows={5}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <SpatialPopupDisplayField
            form={cityForm}
            popupFieldName="popupConfigJson"
            displayFieldName="displayConfigJson"
          />

          <SpatialAttachmentListField name="attachments" title="城市附件" />

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
        </Form>
      </Modal>

      <Modal
        open={subMapModalOpen}
        width={1200}
        destroyOnClose
        title={editingSubMapId ? '編輯子地圖' : '新增子地圖'}
        onCancel={() => setSubMapModalOpen(false)}
        onOk={() => void handleSaveSubMap()}
      >
        <Form form={subMapForm} layout="vertical">
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
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item
                name="code"
                label="子地圖代碼"
                rules={[{ required: true, message: '請輸入子地圖代碼' }]}
              >
                <Input placeholder="例如 macau-peninsula / taipa / coloane" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="boundsJson" label="範圍資訊 JSON">
                <Input.TextArea rows={4} placeholder='例如 {"north":22.21,"south":22.18}' />
              </Form.Item>
            </Col>
          </Row>

          <Space style={{ marginBottom: 12 }}>
            <Button icon={<RadarChartOutlined />} onClick={() => void handleSuggest('sub_map')}>
              自動補齊建議資料
            </Button>
            {currentParentCity ? <Text type="secondary">目前所屬城市：{pickCityName(currentParentCity)}</Text> : null}
          </Space>

          <LocalizedFieldGroup
            form={subMapForm}
            label="子地圖名稱"
            fieldNames={nameFields}
            required
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <LocalizedFieldGroup
            form={subMapForm}
            label="子地圖副標"
            fieldNames={subtitleFields}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <SpatialCoordinateFieldGroup
            form={subMapForm}
            required
            sourceSystemName="sourceCoordinateSystem"
            sourceLatitudeName="sourceCenterLat"
            sourceLongitudeName="sourceCenterLng"
            normalizedLatitudeName="centerLat"
            normalizedLongitudeName="centerLng"
          />

          <SpatialAssetPickerField
            name="coverAssetId"
            label="封面資源"
            required
            assetKind="image"
            help="可直接上傳子地圖封面圖。"
          />

          <LocalizedFieldGroup
            form={subMapForm}
            label="子地圖介紹"
            fieldNames={descriptionFields}
            multiline
            rows={5}
            translationDefaults={translationSettingsRequest.data?.data}
          />

          <SpatialPopupDisplayField
            form={subMapForm}
            popupFieldName="popupConfigJson"
            displayFieldName="displayConfigJson"
          />

          <SpatialAttachmentListField name="attachments" title="子地圖附件" />

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
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default CityManagement;
