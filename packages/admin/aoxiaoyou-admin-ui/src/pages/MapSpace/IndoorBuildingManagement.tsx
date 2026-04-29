import React from 'react';
import { useRequest } from 'ahooks';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { type ActionType, type ProColumns } from '@ant-design/pro-table';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Alert,
  Button,
  Card,
  Col,
  Descriptions,
  Empty,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Tabs,
  Typography,
  message,
} from 'antd';
import { EditOutlined, EnvironmentOutlined, PlusOutlined } from '@ant-design/icons';
import {
  createIndoorBuilding,
  createIndoorFloor,
  deleteIndoorFloor,
  getAdminPois,
  getAdminTranslationSettings,
  getCities,
  getIndoorBuildingDetail,
  getIndoorBuildings,
  getSubMaps,
  updateIndoorBuilding,
  updateIndoorFloor,
} from '../../services/api';
import type {
  AdminIndoorBuildingDetail,
  AdminIndoorBuildingItem,
  AdminIndoorBuildingPayload,
  AdminIndoorFloorItem,
  AdminIndoorFloorPayload,
  AdminPoiListItem,
  AdminSpatialAssetLinkItem,
  AdminSubMapItem,
  AdminTranslationSettings,
  CityItem,
} from '../../types/admin';
import LocalizedFieldGroup, { buildLocalizedFieldNames } from '../../components/localization/LocalizedFieldGroup';
import MediaAssetPickerField from '../../components/media/MediaAssetPickerField';
import SpatialCoordinateFieldGroup from '../../components/spatial/SpatialCoordinateFieldGroup';
import SpatialAttachmentListField from '../../components/spatial/SpatialAttachmentListField';
import SpatialPopupDisplayField from '../../components/spatial/SpatialPopupDisplayField';
import { focusFirstInvalidField } from '../../utils/formErrorFeedback';
import {
  collectSpatialAttachmentAssetIds,
  hydrateSpatialAttachmentDrafts,
  normalizeSpatialAttachmentDrafts,
} from '../../utils/spatialAttachments';
import MapTileManagement from './MapTileManagement';

const { Paragraph, Text, Title } = Typography;

const buildingNameFields = buildLocalizedFieldNames('name');
const buildingAddressFields = buildLocalizedFieldNames('address');
const buildingDescriptionFields = buildLocalizedFieldNames('description');
const floorNameFields = {
  'zh-Hant': 'floorNameZht',
  'zh-Hans': 'floorNameZh',
  en: 'floorNameEn',
  pt: 'floorNamePt',
} as const;
const floorDescriptionFields = buildLocalizedFieldNames('description');

const statusOptions = [
  { label: '\u672a\u767c\u4f48', value: 'unpublished' },
  { label: '\u7de8\u8f2f\u4e2d', value: 'editing' },
  { label: '\u5be9\u6279\u4e2d', value: 'reviewing' },
  { label: '\u5df2\u767c\u4f48', value: 'published' },
  { label: '\u5df2\u5c01\u5b58', value: 'archived' },
  { label: '\u5df2\u522a\u9664', value: 'deleted' },
];

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

const SHOWCASE_BUILDING_CODE = 'lisboeta_macau';

function hasText(value?: string | null) {
  return Boolean(value && value.trim());
}

function buildLocaleCoverage(values: {
  zht?: string | null;
  zh?: string | null;
  en?: string | null;
  pt?: string | null;
}) {
  return [
    { label: '\u7e41\u4e2d', ready: hasText(values.zht) },
    { label: '\u7b80\u4e2d', ready: hasText(values.zh) },
    { label: 'EN', ready: hasText(values.en) },
    { label: 'PT', ready: hasText(values.pt) },
  ];
}

function resolveDefaultBuildingId(
  items: AdminIndoorBuildingItem[],
  initialBuildingId?: number | null,
  currentBuildingId?: number | null,
) {
  if (initialBuildingId && items.some((item) => item.id === initialBuildingId)) {
    return initialBuildingId;
  }
  if (currentBuildingId && items.some((item) => item.id === currentBuildingId)) {
    return currentBuildingId;
  }
  return items.find((item) => item.buildingCode === SHOWCASE_BUILDING_CODE)?.id || items[0]?.id || null;
}

function parsePositiveSearchParam(value: string | null) {
  if (!value) {
    return null;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

function pickLocalizedName(input?: {
  nameZht?: string;
  nameZh?: string;
  nameEn?: string;
  namePt?: string;
  floorNameZht?: string;
  floorNameZh?: string;
  floorNameEn?: string;
  floorNamePt?: string;
  code?: string;
  floorCode?: string;
}) {
  return (
    input?.nameZht ||
    input?.nameZh ||
    input?.nameEn ||
    input?.namePt ||
    input?.floorNameZht ||
    input?.floorNameZh ||
    input?.floorNameEn ||
    input?.floorNamePt ||
    input?.code ||
    input?.floorCode ||
    '-'
  );
}

function buildPoiSearchText(item: AdminPoiListItem) {
  return [
    pickLocalizedName(item),
    item.poiId,
    item.code,
    item.cityName,
    item.subMapName,
    item.subMapCode,
    item.categoryCode,
  ]
    .filter(Boolean)
    .join(' ');
}

function renderStatus(status?: string) {
  if (status === 'published') {
    return <Tag color="green">{'\u5df2\u767c\u4f48'}</Tag>;
  }
  if (status === 'unpublished') {
    return <Tag>{'\u672a\u767c\u4f48'}</Tag>;
  }
  if (status === 'editing' || status === 'draft') {
    return <Tag color="gold">{'\u7de8\u8f2f\u4e2d'}</Tag>;
  }
  if (status === 'reviewing') {
    return <Tag color="processing">{'\u5be9\u6279\u4e2d'}</Tag>;
  }
  if (status === 'deleted') {
    return <Tag color="red">{'\u5df2\u522a\u9664'}</Tag>;
  }
  if (status === 'archived') {
    return <Tag color="default">{'\u5df2\u5c01\u5b58'}</Tag>;
  }
  return <Tag color="gold">{'\u7de8\u8f2f\u4e2d'}</Tag>;
}

function renderImportStatus(status?: string | null) {
  if (status === 'ready') {
    return <Tag color="green">{'\u5df2\u5c31\u7dd2'}</Tag>;
  }
  if (status === 'failed') {
    return <Tag color="red">{'\u8655\u7406\u5931\u6557'}</Tag>;
  }
  if (status === 'processing') {
    return <Tag color="processing">{'\u8655\u7406\u4e2d'}</Tag>;
  }
  return <Tag>{'\u5c1a\u672a\u8655\u7406'}</Tag>;
}

function buildLegacyAttachmentLinks(assetIds?: Array<number | null | undefined>): AdminSpatialAssetLinkItem[] {
  return (assetIds || [])
    .filter((assetId): assetId is number => typeof assetId === 'number')
    .map((assetId, index) => ({
      usageType: 'gallery',
      assetId,
      sortOrder: index,
      status: 'draft',
    }));
}

function resolveAttachmentLinks(
  attachments?: AdminSpatialAssetLinkItem[] | null,
  attachmentAssetIds?: Array<number | null | undefined>,
): AdminSpatialAssetLinkItem[] {
  const normalizedAttachments = (attachments || []).filter(
    (attachment): attachment is AdminSpatialAssetLinkItem =>
      !!attachment && typeof attachment.assetId === 'number',
  );

  if (normalizedAttachments.length) {
    return normalizedAttachments;
  }

  return buildLegacyAttachmentLinks(attachmentAssetIds);
}

function withBuildingDefaults(record?: Partial<AdminIndoorBuildingDetail>): Partial<AdminIndoorBuildingPayload> {
  return {
    bindingMode: 'map',
    cityId: record?.cityId,
    subMapId: record?.subMapId,
    poiId: record?.poiId,
    totalFloors: record?.totalFloors ?? 1,
    basementFloors: record?.basementFloors ?? 0,
    sourceCoordinateSystem: record?.sourceCoordinateSystem || 'GCJ02',
    sourceLatitude: record?.sourceLatitude ?? record?.lat ?? null,
    sourceLongitude: record?.sourceLongitude ?? record?.lng ?? null,
    lat: record?.lat ?? null,
    lng: record?.lng ?? null,
    popupConfigJson: record?.popupConfigJson || defaultPopupConfig,
    displayConfigJson: record?.displayConfigJson || defaultDisplayConfig,
    attachments: hydrateSpatialAttachmentDrafts(resolveAttachmentLinks(record?.attachments, record?.attachmentAssetIds)),
    attachmentAssetIds: record?.attachmentAssetIds || [],
    status: record?.status || 'unpublished',
    sortOrder: record?.sortOrder ?? 0,
    ...record,
  };
}

function withFloorDefaults(record?: Partial<AdminIndoorFloorItem>): Partial<AdminIndoorFloorPayload> {
  return {
    floorNumber: record?.floorNumber ?? 1,
    popupConfigJson: record?.popupConfigJson || defaultPopupConfig,
    displayConfigJson: record?.displayConfigJson || defaultDisplayConfig,
    attachments: hydrateSpatialAttachmentDrafts(resolveAttachmentLinks(record?.attachments, record?.attachmentAssetIds)),
    attachmentAssetIds: record?.attachmentAssetIds || [],
    zoomMin: record?.zoomMin ?? 0.5,
    zoomMax: record?.zoomMax ?? 2.5,
    defaultZoom: record?.defaultZoom ?? 1,
    sortOrder: record?.sortOrder ?? record?.floorNumber ?? 1,
    status: record?.status || 'unpublished',
    ...record,
  };
}

function buildBuildingPayloadFromDetail(
  record: AdminIndoorBuildingDetail,
  overrides: Partial<AdminIndoorBuildingPayload> = {},
): AdminIndoorBuildingPayload {
  const attachments = resolveAttachmentLinks(record.attachments, record.attachmentAssetIds);
  return {
    buildingCode: record.buildingCode,
    bindingMode: 'map',
    cityId: record.cityId,
    subMapId: record.subMapId || undefined,
    poiId: record.poiId || undefined,
    nameZh: record.nameZh,
    nameEn: record.nameEn,
    nameZht: record.nameZht,
    namePt: record.namePt,
    addressZh: record.addressZh,
    addressEn: record.addressEn,
    addressZht: record.addressZht,
    addressPt: record.addressPt,
    sourceCoordinateSystem: record.sourceCoordinateSystem,
    sourceLatitude: record.sourceLatitude ?? record.lat ?? undefined,
    sourceLongitude: record.sourceLongitude ?? record.lng ?? undefined,
    lat: record.lat ?? undefined,
    lng: record.lng ?? undefined,
    totalFloors: record.totalFloors ?? undefined,
    basementFloors: record.basementFloors ?? undefined,
    coverImageUrl: record.coverImageUrl ?? undefined,
    coverAssetId: record.coverAssetId ?? undefined,
    descriptionZh: record.descriptionZh,
    descriptionEn: record.descriptionEn,
    descriptionZht: record.descriptionZht,
    descriptionPt: record.descriptionPt,
    popupConfigJson: record.popupConfigJson,
    displayConfigJson: record.displayConfigJson,
    attachments,
    attachmentAssetIds: attachments
      .map((item) => item.assetId)
      .filter((assetId): assetId is number => typeof assetId === 'number'),
    status: record.status || 'unpublished',
    sortOrder: record.sortOrder ?? 0,
    publishedAt: record.publishedAt || undefined,
    ...overrides,
  };
}

function buildFloorPayloadFromDetail(
  record: AdminIndoorFloorItem,
  overrides: Partial<AdminIndoorFloorPayload> = {},
): AdminIndoorFloorPayload {
  const attachments = resolveAttachmentLinks(record.attachments, record.attachmentAssetIds);
  return {
    indoorMapId: record.indoorMapId ?? undefined,
    floorCode: record.floorCode,
    floorNumber: record.floorNumber,
    floorNameZh: record.floorNameZh,
    floorNameEn: record.floorNameEn,
    floorNameZht: record.floorNameZht,
    floorNamePt: record.floorNamePt,
    descriptionZh: record.descriptionZh,
    descriptionEn: record.descriptionEn,
    descriptionZht: record.descriptionZht,
    descriptionPt: record.descriptionPt,
    coverAssetId: record.coverAssetId ?? undefined,
    floorPlanAssetId: record.floorPlanAssetId ?? undefined,
    tilePreviewImageUrl: record.tilePreviewImageUrl ?? undefined,
    altitudeMeters: record.altitudeMeters ?? undefined,
    areaSqm: record.areaSqm ?? undefined,
    zoomMin: record.zoomMin ?? undefined,
    zoomMax: record.zoomMax ?? undefined,
    defaultZoom: record.defaultZoom ?? undefined,
    popupConfigJson: record.popupConfigJson,
    displayConfigJson: record.displayConfigJson,
    attachments,
    attachmentAssetIds: attachments
      .map((item) => item.assetId)
      .filter((assetId): assetId is number => typeof assetId === 'number'),
    sortOrder: record.sortOrder ?? record.floorNumber,
    status: record.status || 'unpublished',
    publishedAt: record.publishedAt || undefined,
    ...overrides,
  };
}

interface IndoorBuildingManagementProps {
  embedded?: boolean;
  initialBuildingId?: number | null;
  onOpenFloorAuthoring?: (selection: { buildingId: number; floorId?: number | null }) => void;
}

const IndoorBuildingManagement: React.FC<IndoorBuildingManagementProps> = ({
  embedded = false,
  initialBuildingId = null,
  onOpenFloorAuthoring,
}) => {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const actionRef = React.useRef<ActionType>();
  const [buildingForm] = Form.useForm<AdminIndoorBuildingPayload>();
  const [floorForm] = Form.useForm<AdminIndoorFloorPayload>();
  const [buildingRecords, setBuildingRecords] = React.useState<AdminIndoorBuildingItem[]>([]);
  const [selectedBuildingId, setSelectedBuildingId] = React.useState<number | null>(null);
  const [selectedBuilding, setSelectedBuilding] = React.useState<AdminIndoorBuildingDetail | null>(null);
  const [loadingBuildingDetail, setLoadingBuildingDetail] = React.useState(false);
  const [buildingModalOpen, setBuildingModalOpen] = React.useState(false);
  const [floorModalOpen, setFloorModalOpen] = React.useState(false);
  const [editingBuildingId, setEditingBuildingId] = React.useState<number | null>(null);
  const [editingFloorId, setEditingFloorId] = React.useState<number | null>(null);
  const activeTab = embedded ? 'catalog' : searchParams.get('tab') === 'authoring' ? 'authoring' : 'catalog';
  const searchBuildingId = parsePositiveSearchParam(searchParams.get('buildingId'));
  const searchFloorId = parsePositiveSearchParam(searchParams.get('floorId'));
  const effectiveInitialBuildingId = searchBuildingId ?? initialBuildingId;

  const buildingCityId = Form.useWatch('cityId', buildingForm);
  const buildingSubMapId = Form.useWatch('subMapId', buildingForm);
  const buildingPoiId = Form.useWatch('poiId', buildingForm);

  const translationSettingsRequest = useRequest(getAdminTranslationSettings);
  const citiesRequest = useRequest(() => getCities({ pageNum: 1, pageSize: 200 }));
  const subMapsRequest = useRequest(() => getSubMaps({ pageNum: 1, pageSize: 500 }));
  const poisRequest = useRequest(() => getAdminPois({ pageNum: 1, pageSize: 500 }));

  const translationDefaults = translationSettingsRequest.data?.data as AdminTranslationSettings | undefined;
  const showcaseBuilding = React.useMemo(
    () => buildingRecords.find((item) => item.buildingCode === SHOWCASE_BUILDING_CODE) || null,
    [buildingRecords],
  );
  const activeBuilding = React.useMemo(
    () => (selectedBuilding && selectedBuilding.id === selectedBuildingId ? selectedBuilding : null),
    [selectedBuilding, selectedBuildingId],
  );
  const cityOptions = (citiesRequest.data?.data?.list || []).map((city: CityItem) => ({
    label: pickLocalizedName(city),
    value: city.id,
  }));
  const availableSubMaps = (subMapsRequest.data?.data?.list || []).filter(
    (item: AdminSubMapItem) => !buildingCityId || item.cityId === buildingCityId,
  );
  const subMapOptions = availableSubMaps.map((item) => ({
    label: pickLocalizedName(item),
    value: item.id,
  }));
  const availablePois = (poisRequest.data?.data?.list || []).filter(
    (item: AdminPoiListItem) =>
      (!buildingCityId || item.cityId === buildingCityId) && (!buildingSubMapId || item.subMapId === buildingSubMapId),
  );
  const poiOptions = availablePois.map((item) => {
    const displayName = pickLocalizedName(item);
    const locationLabel = [item.cityName, item.subMapName || item.subMapCode].filter(Boolean).join(' / ') || '\u672a\u6307\u5b9a\u5730\u5716';
    return {
      label: (
        <Space direction="vertical" size={0} style={{ width: '100%' }}>
          <Space size={8} wrap>
            <Text strong>{displayName}</Text>
            <Text type="secondary">#{item.poiId}</Text>
          </Space>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {item.code} {'\u00b7'} {locationLabel}
          </Text>
        </Space>
      ),
      value: item.poiId,
      plainLabel: `${displayName} (#${item.poiId})`,
      searchText: buildPoiSearchText(item),
    };
  });

  const loadBuildingDetail = React.useCallback(async (buildingId: number) => {
    setLoadingBuildingDetail(true);
    try {
      const response = await getIndoorBuildingDetail(buildingId);
      if (!response.success || !response.data) {
        throw new Error(response.message || '\u7121\u6cd5\u8f09\u5165\u5ba4\u5167\u5efa\u7bc9\u8a73\u60c5');
      }
      setSelectedBuilding(response.data);
      setSelectedBuildingId(buildingId);
    } catch (error) {
      setSelectedBuilding(null);
      message.error(error instanceof Error ? error.message : '\u7121\u6cd5\u8f09\u5165\u5ba4\u5167\u5efa\u7bc9\u8a73\u60c5');
    } finally {
      setLoadingBuildingDetail(false);
    }
  }, []);

  const openFloorAuthoring = React.useCallback(
    (buildingId: number, floorId?: number | null) => {
      if (onOpenFloorAuthoring) {
        onOpenFloorAuthoring({ buildingId, floorId });
        return;
      }
      const params = new URLSearchParams({ tab: 'authoring', buildingId: String(buildingId) });
      if (floorId) {
        params.set('floorId', String(floorId));
      }
      navigate(`/space/indoor-buildings?${params.toString()}`);
    },
    [navigate, onOpenFloorAuthoring],
  );

  React.useEffect(() => {
    const nextBuildingId = resolveDefaultBuildingId(buildingRecords, effectiveInitialBuildingId, selectedBuildingId);
    if (nextBuildingId && nextBuildingId !== selectedBuildingId) {
      void loadBuildingDetail(nextBuildingId);
    }
  }, [buildingRecords, effectiveInitialBuildingId, loadBuildingDetail, selectedBuildingId]);

  const columns = React.useMemo<ProColumns<AdminIndoorBuildingItem>[]>(
    () => [
      {
        title: '\u641c\u5c0b',
        dataIndex: 'keyword',
        hideInTable: true,
        fieldProps: { placeholder: '\u5efa\u7bc9\u540d\u7a31 / \u4ee3\u78bc / \u5730\u5740' },
      },
      {
        title: '\u57ce\u5e02',
        dataIndex: 'cityId',
        hideInTable: true,
        valueType: 'select',
        fieldProps: { options: cityOptions, showSearch: true },
      },
      {
        title: '\u72c0\u614b',
        dataIndex: 'status',
        hideInTable: true,
        valueType: 'select',
        fieldProps: { options: statusOptions },
      },
      {
        title: '\u5ba4\u5167\u5efa\u7bc9',
        dataIndex: 'nameZh',
        width: 260,
        render: (_, record) => (
          <Space direction="vertical" size={0}>
            <Text strong>{pickLocalizedName(record)}</Text>
            <Text type="secondary">{'\u4ee3\u78bc\uff1a' + record.buildingCode}</Text>
          </Space>
        ),
      },
      {
        title: '\u7d81\u5b9a\u65b9\u5f0f',
        dataIndex: 'bindingMode',
        width: 140,
        render: (_, record) => (record.bindingMode === 'poi' ? '\u7d81\u5b9a POI' : '\u7d81\u5b9a\u5730\u5716'),
      },
      {
        title: '\u57ce\u5e02 / \u5b50\u5730\u5716',
        key: 'location',
        width: 220,
        render: (_, record) => (
          <Space direction="vertical" size={0}>
            <Text>{record.cityName || record.cityCode || '-'}</Text>
            <Text type="secondary">{record.subMapName || record.subMapCode || '\u50c5\u7d81\u5b9a\u5927\u5730\u5716'}</Text>
          </Space>
        ),
      },
      {
        title: '\u5165\u53e3',
        key: 'entry',
        width: 210,
        render: (_, record) =>
          record.bindingMode === 'poi' ? (
            <Text>{record.poiName || ('POI #' + record.poiId)}</Text>
          ) : record.lat != null && record.lng != null ? (
            <Text>{record.lat}, {record.lng}</Text>
          ) : (
            <Text type="secondary">{'\u5c1a\u672a\u8a2d\u5b9a'}</Text>
          ),
      },
      {
        title: '\u6a13\u5c64\u6578',
        key: 'floors',
        width: 120,
        render: (_, record) => String(record.floorCount || 0) + ' / ' + String(record.totalFloors || 0),
      },
      {
        title: '\u72c0\u614b',
        key: 'statusLabel',
        width: 100,
        render: (_, record) => renderStatus(record.status),
      },
      {
        title: '\u64cd\u4f5c',
        key: 'action',
        valueType: 'option',
        width: 220,
        render: (_, record) => [
          <Button
            key="authoring"
            type="link"
            size="small"
            onClick={() => openFloorAuthoring(record.id, null)}
          >
            {'\u7de8\u6392\u6a13\u5c64'}
          </Button>,
          <Button key="manage" type="link" size="small" onClick={() => void loadBuildingDetail(record.id)}>
            {'\u67e5\u770b\u8a73\u60c5'}
          </Button>,
          <Button key="edit" type="link" size="small" icon={<EditOutlined />} onClick={() => void openBuildingEditor(record.id)}>
            {'\u7de8\u8f2f'}
          </Button>,
        ],
      },
    ],
    [cityOptions, loadBuildingDetail],
  );

  const floorColumns = React.useMemo(
    () => [
      {
        title: '\u6a13\u5c64',
        key: 'floor',
        render: (_: unknown, record: AdminIndoorFloorItem) => (
          <Space direction="vertical" size={0}>
            <Text strong>{pickLocalizedName({ nameZh: record.floorNameZh, nameZht: record.floorNameZht, nameEn: record.floorNameEn, namePt: record.floorNamePt })}</Text>
            <Text type="secondary">{'\u5e8f\u865f\uff1a' + record.floorNumber + ' / ' + '\u4ee3\u78bc\uff1a' + (record.floorCode || '-')}</Text>
          </Space>
        ),
      },
      {
        title: '\u9762\u7a4d',
        key: 'area',
        width: 120,
        render: (_: unknown, record: AdminIndoorFloorItem) => (record.areaSqm != null ? String(record.areaSqm) + ' m2' : '-'),
      },
      {
        title: '\u7e2e\u653e',
        key: 'zoom',
        width: 180,
        render: (_: unknown, record: AdminIndoorFloorItem) =>
          String(record.zoomMin ?? '-') + ' / ' + String(record.defaultZoom ?? '-') + ' / ' + String(record.zoomMax ?? '-'),
      },
      {
        title: '\u72c0\u614b',
        key: 'status',
        width: 100,
        render: (_: unknown, record: AdminIndoorFloorItem) => renderStatus(record.status),
      },
      {
        title: '\u64cd\u4f5c',
        key: 'action',
        width: 220,
        render: (_: unknown, record: AdminIndoorFloorItem) => (
          <Space>
            <Button type="link" size="small" onClick={() => openFloorAuthoring(record.buildingId, record.id)}>
              {'\u7de8\u6392\u5716\u8cc7'}
            </Button>
            <Button type="link" size="small" onClick={() => openFloorEditor(record)}>
              {'\u7de8\u8f2f'}
            </Button>
            <Popconfirm
              title={'\u78ba\u5b9a\u522a\u9664\u6b64\u6a13\u5c64\uff1f'}
              description={'\u5df2\u7d81\u5b9a\u7684\u74e6\u7247\u3001\u6a19\u8a18\u3001\u9644\u4ef6\u8207\u5716\u8cc7\u90fd\u6703\u4e00\u4f75\u79fb\u9664\uff0c\u8acb\u78ba\u8a8d\u5f8c\u518d\u522a\u9664\u3002'}
              onConfirm={() => void handleDeleteFloor(record.id)}
            >
              <Button type="link" size="small" danger>
                {'\u522a\u9664'}
              </Button>
            </Popconfirm>
          </Space>
        ),
      },
    ],
    [],
  );

  async function openBuildingEditor(buildingId?: number) {
    buildingForm.resetFields();
    if (buildingId) {
      const response = await getIndoorBuildingDetail(buildingId);
      if (!response.success || !response.data) {
        message.error(response.message || '\u7121\u6cd5\u8f09\u5165\u5ba4\u5167\u5efa\u7bc9\u8a73\u60c5');
        return;
      }
      buildingForm.setFieldsValue(withBuildingDefaults(response.data));
      setEditingBuildingId(buildingId);
    } else {
      buildingForm.setFieldsValue(withBuildingDefaults());
      setEditingBuildingId(null);
    }
    setBuildingModalOpen(true);
  }

  function openFloorEditor(floor?: AdminIndoorFloorItem) {
    floorForm.resetFields();
    floorForm.setFieldsValue(withFloorDefaults(floor));
    setEditingFloorId(floor?.id || null);
    setFloorModalOpen(true);
  }

  async function handleSaveBuilding() {
    try {
      const values = await buildingForm.validateFields();
      const attachments = normalizeSpatialAttachmentDrafts(resolveAttachmentLinks(values.attachments, values.attachmentAssetIds));
      const payload: AdminIndoorBuildingPayload = {
        ...values,
        bindingMode: 'map',
        subMapId: values.subMapId || undefined,
        poiId: values.poiId || undefined,
        sourceLatitude: values.poiId ? values.sourceLatitude ?? values.lat : values.sourceLatitude ?? values.lat,
        sourceLongitude: values.poiId ? values.sourceLongitude ?? values.lng : values.sourceLongitude ?? values.lng,
        lat: values.lat ?? undefined,
        lng: values.lng ?? undefined,
        attachments,
        attachmentAssetIds: collectSpatialAttachmentAssetIds(attachments),
      };

      if (editingBuildingId) {
        const response = await updateIndoorBuilding(editingBuildingId, payload);
        if (!response.success || !response.data) {
          throw new Error(response.message || '\u5ba4\u5167\u5efa\u7bc9\u66f4\u65b0\u5931\u6557');
        }
        message.success('\u5ba4\u5167\u5efa\u7bc9\u5df2\u66f4\u65b0');
        await loadBuildingDetail(response.data.id);
      } else {
        const response = await createIndoorBuilding(payload);
        if (!response.success || !response.data) {
          throw new Error(response.message || '\u5ba4\u5167\u5efa\u7bc9\u5efa\u7acb\u5931\u6557');
        }
        message.success('\u5ba4\u5167\u5efa\u7bc9\u5df2\u5efa\u7acb');
        await loadBuildingDetail(response.data.id);
      }

      setBuildingModalOpen(false);
      actionRef.current?.reload();
    } catch (error) {
      if (focusFirstInvalidField(buildingForm, 'indoor-building-form', error)) {
        message.warning('\u8acb\u5148\u4fee\u6b63\u8868\u55ae\u4e2d\u5c1a\u672a\u5b8c\u6210\u7684\u6b04\u4f4d');
        return;
      }
      message.error(error instanceof Error ? error.message : '\u5ba4\u5167\u5efa\u7bc9\u4fdd\u5b58\u5931\u6557');
    }
  }

  async function handleSaveFloor() {
    if (!selectedBuildingId) {
      message.warning('\u8acb\u5148\u9078\u5b9a\u5ba4\u5167\u5efa\u7bc9');
      return;
    }

    try {
      const values = await floorForm.validateFields();
      const attachments = normalizeSpatialAttachmentDrafts(resolveAttachmentLinks(values.attachments, values.attachmentAssetIds));
      const payload: AdminIndoorFloorPayload = {
        ...values,
        attachments,
        attachmentAssetIds: collectSpatialAttachmentAssetIds(attachments),
      };

      if (editingFloorId) {
        const response = await updateIndoorFloor(editingFloorId, payload);
        if (!response.success) {
          throw new Error(response.message || '\u6a13\u5c64\u66f4\u65b0\u5931\u6557');
        }
        message.success('\u6a13\u5c64\u5df2\u66f4\u65b0');
      } else {
        const response = await createIndoorFloor(selectedBuildingId, payload);
        if (!response.success) {
          throw new Error(response.message || '\u6a13\u5c64\u5efa\u7acb\u5931\u6557');
        }
        message.success('\u6a13\u5c64\u5df2\u5efa\u7acb');
      }

      setFloorModalOpen(false);
      await loadBuildingDetail(selectedBuildingId);
    } catch (error) {
      if (focusFirstInvalidField(floorForm, 'indoor-floor-form', error)) {
        message.warning('\u8acb\u5148\u4fee\u6b63\u8868\u55ae\u4e2d\u5c1a\u672a\u5b8c\u6210\u7684\u6b04\u4f4d');
        return;
      }
      message.error(error instanceof Error ? error.message : '\u6a13\u5c64\u4fdd\u5b58\u5931\u6557');
    }
  }

  async function handleDeleteFloor(floorId: number) {
    const response = await deleteIndoorFloor(floorId);
    if (!response.success) {
      message.error(response.message || '\u6a13\u5c64\u522a\u9664\u5931\u6557');
      return;
    }
    message.success('\u6a13\u5c64\u5df2\u522a\u9664');
    if (selectedBuildingId) {
      await loadBuildingDetail(selectedBuildingId);
    }
  }

  async function handleQuickBuildingStatusChange(status: string) {
    if (!activeBuilding) {
      return;
    }
    const response = await updateIndoorBuilding(
      activeBuilding.id,
      buildBuildingPayloadFromDetail(activeBuilding, {
        status,
        publishedAt: status === 'published' ? activeBuilding.publishedAt || new Date().toISOString() : undefined,
      }),
    );
    if (!response.success || !response.data) {
      message.error(response.message || '\u5ba4\u5167\u5efa\u7bc9\u72c0\u614b\u66f4\u65b0\u5931\u6557');
      return;
    }
    message.success('\u5ba4\u5167\u5efa\u7bc9\u72c0\u614b\u5df2\u66f4\u65b0');
    await loadBuildingDetail(activeBuilding.id);
    actionRef.current?.reload();
  }

  async function handleQuickFloorStatusChange(floor: AdminIndoorFloorItem, status: string) {
    const response = await updateIndoorFloor(
      floor.id,
      buildFloorPayloadFromDetail(floor, {
        status,
        publishedAt: status === 'published' ? floor.publishedAt || new Date().toISOString() : undefined,
      }),
    );
    if (!response.success) {
      message.error(response.message || '\u6a13\u5c64\u72c0\u614b\u66f4\u65b0\u5931\u6557');
      return;
    }
    message.success('\u6a13\u5c64\u72c0\u614b\u5df2\u66f4\u65b0');
  }
  const content = (
    <>
      <ProTable<AdminIndoorBuildingItem>
        actionRef={actionRef}
        columns={columns}
        rowKey="id"
        request={async (params) => {
          const response = await getIndoorBuildings({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.keyword as string,
            cityId: params.cityId as number,
            status: params.status as string,
          });
          const items = response.data?.list || [];
          setBuildingRecords(items);
          return {
            data: items,
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        search={{ labelWidth: 'auto' }}
        options={{ density: false, setting: false }}
        onRow={(record) => ({
          onClick: () => void loadBuildingDetail(record.id),
        })}
        toolBarRender={() => [
          <Button key="create" type="primary" icon={<PlusOutlined />} onClick={() => void openBuildingEditor()}>
            {'\u65b0\u589e\u5ba4\u5167\u5efa\u7bc9'}
          </Button>,
        ]}
      />

      {showcaseBuilding ? (
        <Alert
          style={{ marginBottom: 24 }}
          type={activeBuilding?.buildingCode === SHOWCASE_BUILDING_CODE ? 'success' : 'info'}
          showIcon
          message={'\u5df2\u8f09\u5165\u6fb3\u9580\u8461\u4eac\u4eba\u5ba4\u5167\u5730\u5716\u793a\u4f8b'}
          description={
            '\u9019\u500b\u793a\u4f8b\u5efa\u7bc9\u53ef\u7528\u4f86\u6aa2\u67e5\u6a13\u5c64\u7d50\u69cb\u3001\u74e6\u7247\u9810\u89bd\u3001\u5a92\u9ad4\u8cc7\u6e90\u8207\u5f8c\u7e8c\u7684\u5716\u8cc7\u3001\u6a19\u8a18\u3001CSV \u7de8\u6392\u6d41\u7a0b\u662f\u5426\u4e00\u81f4\u3002'
          }
          action={
            <Space wrap>
              <Button size="small" onClick={() => void loadBuildingDetail(showcaseBuilding.id)}>
                {'\u8f09\u5165\u793a\u4f8b\u5efa\u7bc9'}
              </Button>
              {activeBuilding?.id === showcaseBuilding.id ? (
                <Button
                  size="small"
                  type="primary"
                  onClick={() => openFloorAuthoring(activeBuilding.id, activeBuilding.floors?.[0]?.id || null)}
                >
                  {'\u958b\u555f\u6a13\u5c64\u5716\u8cc7\u7de8\u6392'}
                </Button>
              ) : null}
            </Space>
          }
        />
      ) : null}

      <Card
        title={'\u5ba4\u5167\u5efa\u7bc9\u8a73\u60c5\u8207\u6a13\u5c64\u7e3d\u89bd'}
        extra={
          activeBuilding ? (
            <Space>
              <Select
                size="small"
                style={{ width: 140 }}
                value={activeBuilding.status || 'unpublished'}
                options={statusOptions}
                onChange={(value) => void handleQuickBuildingStatusChange(value)}
              />
              <Button onClick={() => void openBuildingEditor(activeBuilding.id)}>{'\u7de8\u8f2f\u5efa\u7bc9'}</Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => openFloorEditor()}>
                {'\u65b0\u589e\u6a13\u5c64'}
              </Button>
            </Space>
          ) : null
        }
        loading={loadingBuildingDetail}
      >
        {activeBuilding ? (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            {activeBuilding.buildingCode === SHOWCASE_BUILDING_CODE ? (
              <Alert
                type="success"
                showIcon
                message={'\u76ee\u524d\u6b63\u5728\u7de8\u8f2f\u793a\u4f8b\u5efa\u7bc9'}
                description={
                  '\u6a13\u5c64\u9810\u89bd\u3001\u74e6\u7247\u3001\u6a19\u8a18\u8207 CSV \u5c0e\u5165\u6d41\u7a0b\u90fd\u5efa\u8b70\u5148\u4ee5\u9019\u7b46\u8cc7\u6599\u6e2c\u8a66\uff0c\u78ba\u8a8d\u6700\u65b0 UI \u8207\u8cc7\u6599\u7d50\u69cb\u5df2\u5c0d\u9f4a\u3002'
                }
              />
            ) : null}

            <Row gutter={[16, 16]}>
              <Col xs={24} xl={14}>
                <Descriptions size="small" column={3} bordered>
                  <Descriptions.Item label={'\u5efa\u7bc9\u540d\u7a31'}>{pickLocalizedName(activeBuilding)}</Descriptions.Item>
                  <Descriptions.Item label={'\u5efa\u7bc9\u4ee3\u78bc'}>{activeBuilding.buildingCode}</Descriptions.Item>
                  <Descriptions.Item label={'\u72c0\u614b'}>{renderStatus(activeBuilding.status)}</Descriptions.Item>
                  <Descriptions.Item label={'\u7d81\u5b9a\u65b9\u5f0f'}>
                    {activeBuilding.bindingMode === 'poi' ? '\u7d81\u5b9a POI' : '\u7d81\u5b9a\u5927\u5730\u5716 / \u5b50\u5730\u5716'}
                  </Descriptions.Item>
                  <Descriptions.Item label={'\u57ce\u5e02 / \u5b50\u5730\u5716'}>
                    {activeBuilding.cityName || activeBuilding.cityCode || '-'}
                    {activeBuilding.subMapName ? ` / ${activeBuilding.subMapName}` : ''}
                  </Descriptions.Item>
                  <Descriptions.Item label={'\u5165\u53e3'}>
                    {activeBuilding.bindingMode === 'poi'
                      ? activeBuilding.poiName || `POI #${activeBuilding.poiId}`
                      : activeBuilding.lat != null && activeBuilding.lng != null
                        ? `${activeBuilding.lat}, ${activeBuilding.lng}`
                        : '-'}
                  </Descriptions.Item>
                  <Descriptions.Item label={'\u6a13\u5c64\u6578'}>
                    {`\u5730\u4e0a ${activeBuilding.totalFloors || 0} \u5c64 / \u5730\u4e0b ${activeBuilding.basementFloors || 0} \u5c64`}
                  </Descriptions.Item>
                  <Descriptions.Item label={'\u9644\u4ef6\u6578\u91cf'}>{`${activeBuilding.attachments?.length || activeBuilding.attachmentAssetIds?.length || 0} \u500b`}</Descriptions.Item>
                  <Descriptions.Item label={'\u6700\u5f8c\u66f4\u65b0'}>{activeBuilding.updatedAt || '-'}</Descriptions.Item>
                </Descriptions>
              </Col>
              <Col xs={24} md={12} xl={5}>
                <Card size="small" title={'\u8a9e\u8a00\u8986\u84cb'}>
                  <Space wrap>
                    {buildLocaleCoverage({
                      zh: activeBuilding.nameZh || activeBuilding.descriptionZh,
                      zht: activeBuilding.nameZht || activeBuilding.descriptionZht,
                      en: activeBuilding.nameEn || activeBuilding.descriptionEn,
                      pt: activeBuilding.namePt || activeBuilding.descriptionPt,
                    }).map((item) => (
                      <Tag key={item.label} color={item.ready ? 'green' : 'default'}>
                        {item.label} {item.ready ? '\u5df2\u5b8c\u6210' : '\u5f85\u88dc\u9f4a'}
                      </Tag>
                    ))}
                  </Space>
                  <Paragraph type="secondary" style={{ marginTop: 12, marginBottom: 0 }}>
                    {'\u5efa\u7bc9\u540d\u7a31\u3001\u5730\u5740\u8207\u4ecb\u7d39\u90fd\u53ef\u5728\u7de8\u8f2f\u8868\u55ae\u4e2d\u7dad\u8b77\u56db\u8a9e\u5167\u5bb9\u3002'}
                  </Paragraph>
                </Card>
              </Col>
              <Col xs={24} md={12} xl={5}>
                <Card size="small" title={'\u5c01\u9762\u8207\u9644\u4ef6'}>
                  {activeBuilding.coverImageUrl ? (
                    <div
                      style={{
                        width: '100%',
                        aspectRatio: '4 / 3',
                        overflow: 'hidden',
                        borderRadius: 12,
                        background: '#f5f7fb',
                        border: '1px solid #e6ebf5',
                      }}
                    >
                      <img
                        src={activeBuilding.coverImageUrl}
                        alt={pickLocalizedName(activeBuilding)}
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                      />
                    </div>
                  ) : (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={'\u5c1a\u672a\u8a2d\u5b9a\u5c01\u9762'} />
                  )}
                  <Paragraph type="secondary" style={{ marginTop: 12, marginBottom: 0 }}>
                    {`\u5c01\u9762\u8cc7\u6e90\uff1a${activeBuilding.coverAssetId ? `#${activeBuilding.coverAssetId}` : '\u672a\u7d81\u5b9a'}`}
                  </Paragraph>
                  <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                    {`\u9644\u4ef6\u8cc7\u6e90\uff1a${activeBuilding.attachments?.length || activeBuilding.attachmentAssetIds?.length || 0} \u500b`}
                  </Paragraph>
                </Card>
              </Col>
            </Row>

            {activeBuilding.descriptionZht || activeBuilding.descriptionZh || activeBuilding.descriptionEn || activeBuilding.descriptionPt ? (
              <Card size="small" title={'\u5efa\u7bc9\u4ecb\u7d39'}>
                <Paragraph style={{ marginBottom: 0 }}>
                  {activeBuilding.descriptionZht ||
                    activeBuilding.descriptionZh ||
                    activeBuilding.descriptionEn ||
                    activeBuilding.descriptionPt}
                </Paragraph>
              </Card>
            ) : null}

            <Card size="small" title={'\u6a13\u5c64\u6458\u8981'}>
              {(activeBuilding.floors || []).length ? (
                <Row gutter={[16, 16]}>
                  {(activeBuilding.floors || []).map((floor) => (
                    <Col xs={24} md={12} xl={8} key={floor.id}>
                      <Card
                        size="small"
                        title={pickLocalizedName({
                          floorNameZh: floor.floorNameZh,
                          floorNameZht: floor.floorNameZht,
                          floorNameEn: floor.floorNameEn,
                          floorNamePt: floor.floorNamePt,
                          floorCode: floor.floorCode,
                        })}
                        extra={
                          <Space size={4}>
                            <Select
                              size="small"
                              style={{ width: 132 }}
                              value={floor.status || 'unpublished'}
                              options={statusOptions}
                              onChange={(value) => void handleQuickFloorStatusChange(floor, value)}
                            />
                            <Button type="link" size="small" onClick={() => openFloorAuthoring(floor.buildingId, floor.id)}>
                              {'\u7de8\u6392\u5716\u8cc7'}
                            </Button>
                          </Space>
                        }
                      >
                        <div
                          style={{
                            width: '100%',
                            aspectRatio: '4 / 3',
                            overflow: 'hidden',
                            borderRadius: 12,
                            background: '#f5f7fb',
                            border: '1px solid #e6ebf5',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                          }}
                        >
                          {floor.tilePreviewImageUrl || floor.floorPlanUrl ? (
                            <img
                              src={floor.tilePreviewImageUrl || floor.floorPlanUrl || ''}
                              alt={pickLocalizedName({
                                floorNameZh: floor.floorNameZh,
                                floorNameZht: floor.floorNameZht,
                                floorNameEn: floor.floorNameEn,
                                floorNamePt: floor.floorNamePt,
                                floorCode: floor.floorCode,
                              })}
                              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            />
                          ) : (
                            <Text type="secondary">{'\u5c1a\u672a\u4e0a\u50b3\u6a13\u5c64\u9810\u89bd\u5716'}</Text>
                          )}
                        </div>
                        <Space wrap style={{ marginTop: 12 }}>
                          {renderStatus(floor.status)}
                          {renderImportStatus(floor.importStatus)}
                          <Tag color={floor.tileRootUrl ? 'cyan' : 'default'}>{`COS ${floor.tileRootUrl ? '\u5df2\u767c\u4f48' : '\u672a\u767c\u4f48'}`}</Tag>
                          <Tag color={floor.tileEntryCount ? 'green' : 'default'}>{`Manifest ${floor.tileEntryCount ? '\u5df2\u751f\u6210' : '\u672a\u751f\u6210'}`}</Tag>
                        </Space>
                        <Paragraph type="secondary" style={{ marginTop: 12, marginBottom: 0 }}>
                          {`\u7e2e\u653e\uff1a${floor.zoomMin ?? '-'} / ${floor.defaultZoom ?? '-'} / ${floor.zoomMax ?? '-'}`}
                        </Paragraph>
                        <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                          {`\u9762\u7a4d\uff1a${floor.areaSqm ?? '-'} m2 \u00b7 \u6a19\u8a18\uff1a${floor.markerCount ?? (floor.markers || []).length} \u500b`}
                        </Paragraph>
                      </Card>
                    </Col>
                  ))}
                </Row>
              ) : (
                <Empty description={'\u5c1a\u672a\u5efa\u7acb\u6a13\u5c64\uff0c\u53ef\u5f9e\u53f3\u4e0a\u89d2\u65b0\u589e\u6a13\u5c64'} />
              )}
            </Card>

            <Card size="small" title={'\u6a13\u5c64\u5217\u8868'}>
              <Table
                rowKey="id"
                columns={floorColumns}
                dataSource={activeBuilding.floors || []}
                pagination={false}
                locale={{ emptyText: <Empty description={'\u5c1a\u672a\u5efa\u7acb\u6a13\u5c64'} /> }}
              />
            </Card>
          </Space>
        ) : (
          <Empty description={'\u8acb\u5148\u5f9e\u4e0a\u65b9\u5217\u8868\u9078\u64c7\u4e00\u68df\u5ba4\u5167\u5efa\u7bc9\u67e5\u770b\u8a73\u60c5'} />
        )}
      </Card>
      <Modal
        title={editingBuildingId ? '\u7de8\u8f2f\u5ba4\u5167\u5efa\u7bc9' : '\u65b0\u589e\u5ba4\u5167\u5efa\u7bc9'}
        open={buildingModalOpen}
        destroyOnHidden
        width={1080}
        onCancel={() => {
          buildingForm.resetFields();
          setBuildingModalOpen(false);
        }}
        onOk={() => void handleSaveBuilding()}
      >
        <Form form={buildingForm} name="indoor-building-form" layout="vertical">
          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item
              name="buildingCode"
              label={'\u5efa\u7bc9\u4ee3\u78bc'}
              rules={[{ required: true, message: '\u8acb\u8f38\u5165\u5efa\u7bc9\u4ee3\u78bc' }]}
              style={{ flex: 1 }}
            >
              <Input placeholder="grand_lisboa_palace" />
            </Form.Item>
            <Card size="small" style={{ flex: 1, marginBottom: 24 }}>
              <Paragraph style={{ marginBottom: 0 }}>
                {'\u5ba4\u5167\u5efa\u7bc9\u5fc5\u9808\u7d81\u5b9a\u57ce\u5e02\uff0c\u4e5f\u53ef\u4ee5\u540c\u6642\u7d81\u5b9a\u5b50\u5730\u5716\u8207\u5165\u53e3 POI\u3002\u82e5\u5df2\u7d81\u5b9a POI\uff0c\u53ef\u6cbf\u7528\u5176\u5165\u53e3\u8cc7\u8a0a\uff1b\u82e5\u53ea\u7d81\u5b9a\u5927\u5730\u5716\u6216\u5b50\u5730\u5716\uff0c\u5247\u9700\u8981\u88dc\u4e0a\u5165\u53e3\u5ea7\u6a19\u3002'}
              </Paragraph>
            </Card>
          </Space>

          <Form.Item name="bindingMode" hidden>
            <Input />
          </Form.Item>

          <LocalizedFieldGroup
            form={buildingForm}
            label={'\u5efa\u7bc9\u540d\u7a31'}
            fieldNames={buildingNameFields}
            required
            translationDefaults={translationDefaults}
          />
          <LocalizedFieldGroup
            form={buildingForm}
            label={'\u5efa\u7bc9\u5730\u5740'}
            fieldNames={buildingAddressFields}
            translationDefaults={translationDefaults}
          />

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="cityId" label={'\u57ce\u5e02'} rules={[{ required: true, message: '\u8acb\u9078\u64c7\u57ce\u5e02' }]} style={{ flex: 1 }}>
              <Select
                showSearch
                options={cityOptions}
                onChange={() => {
                  buildingForm.setFieldValue('subMapId', undefined);
                  buildingForm.setFieldValue('poiId', undefined);
                }}
              />
            </Form.Item>
            <Form.Item name="subMapId" label={'\u7d81\u5b9a\u5b50\u5730\u5716\uff08\u9078\u586b\uff09'} style={{ flex: 1 }}>
              <Select allowClear showSearch options={subMapOptions} placeholder={'\u53ef\u53ea\u7d81\u5b9a\u5927\u5730\u5716\uff0c\u4e5f\u53ef\u4ee5\u518d\u9078\u5b9a\u5b50\u5730\u5716'} />
            </Form.Item>
            <Form.Item name="poiId" label={'\u5165\u53e3 POI'} style={{ flex: 1.45, minWidth: 360 }}>
              <Select
                allowClear
                showSearch
                options={poiOptions}
                optionLabelProp="plainLabel"
                optionFilterProp="searchText"
                popupMatchSelectWidth={520}
                listHeight={360}
                onChange={(poiId: number | undefined) => {
                  if (!poiId) {
                    return;
                  }
                  const selectedPoi = availablePois.find((item) => item.poiId === poiId);
                  if (selectedPoi) {
                    buildingForm.setFieldsValue({
                      cityId: selectedPoi.cityId,
                      subMapId: selectedPoi.subMapId || undefined,
                    });
                  }
                }}
              />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="totalFloors" label={'\u5730\u4e0a\u6a13\u5c64\u6578'} style={{ flex: 1 }}>
              <InputNumber min={1} max={300} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="basementFloors" label={'\u5730\u4e0b\u6a13\u5c64\u6578'} style={{ flex: 1 }}>
              <InputNumber min={0} max={100} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="sortOrder" label={'\u6392\u5e8f'} style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="status" label={'\u72c0\u614b'} style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
          </Space>

          <SpatialCoordinateFieldGroup
            form={buildingForm}
            required={!buildingPoiId}
            title={'\u5165\u53e3\u5ea7\u6a19'}
            sourceSystemName="sourceCoordinateSystem"
            sourceLatitudeName="sourceLatitude"
            sourceLongitudeName="sourceLongitude"
            normalizedLatitudeName="lat"
            normalizedLongitudeName="lng"
          />
          {buildingPoiId ? (
            <Card size="small" title={'POI \u88dc\u5145\u8aaa\u660e'} style={{ marginBottom: 24 }}>
              <Space align="start">
                <EnvironmentOutlined style={{ marginTop: 4 }} />
                <Text>{'\u5df2\u7d81\u5b9a\u5165\u53e3 POI \u6642\uff0c\u53ef\u76f4\u63a5\u6cbf\u7528 POI \u7684\u4f4d\u7f6e\u8207\u5730\u5716\u6b78\u5c6c\uff1b\u5982\u9700\u66f4\u7cbe\u7d30\u7684\u5efa\u7bc9\u5165\u53e3\u63cf\u8ff0\uff0c\u4ecd\u53ef\u88dc\u5145\u5c01\u9762\u3001\u9644\u4ef6\u8207\u591a\u8a9e\u4ecb\u7d39\u3002'}</Text>
              </Space>
            </Card>
          ) : null}

          <MediaAssetPickerField
            name="coverAssetId"
            label={'\u5efa\u7bc9\u5c01\u9762'}
            assetKind="image"
            valueMode="asset-id"
            help={'\u5efa\u8b70\u4e0a\u50b3\u4e00\u5f35\u6700\u5177\u4ee3\u8868\u6027\u7684\u5efa\u7bc9\u5c01\u9762\uff0c\u4f9b\u5217\u8868\u3001\u5ba4\u5167\u8a73\u60c5\u8207\u6545\u4e8b\u5167\u5bb9\u5171\u7528\u3002'}
          />
          <SpatialAttachmentListField name="attachments" title={'\u5efa\u7bc9\u9644\u4ef6\u8cc7\u6e90'} />

          <LocalizedFieldGroup
            form={buildingForm}
            label={'\u5efa\u7bc9\u4ecb\u7d39'}
            fieldNames={buildingDescriptionFields}
            multiline
            translationDefaults={translationDefaults}
          />

          <SpatialPopupDisplayField
            form={buildingForm}
            popupFieldName="popupConfigJson"
            displayFieldName="displayConfigJson"
          />
        </Form>
      </Modal>
      <Modal
        title={editingFloorId ? '\u7de8\u8f2f\u6a13\u5c64' : '\u65b0\u589e\u6a13\u5c64'}
        open={floorModalOpen}
        destroyOnHidden
        width={980}
        onCancel={() => {
          floorForm.resetFields();
          setFloorModalOpen(false);
        }}
        onOk={() => void handleSaveFloor()}
      >
        <Form form={floorForm} name="indoor-floor-form" layout="vertical">
          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="floorNumber" label={'\u6a13\u5c64\u5e8f\u865f'} rules={[{ required: true, message: '\u8acb\u8f38\u5165\u6a13\u5c64\u5e8f\u865f' }]} style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="floorCode" label={'\u6a13\u5c64\u4ee3\u78bc'} style={{ flex: 1 }}>
              <Input placeholder="F1 / B1 / G" />
            </Form.Item>
            <Form.Item name="sortOrder" label={'\u6392\u5e8f'} style={{ flex: 1 }}>
              <InputNumber min={-100} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="status" label={'\u72c0\u614b'} style={{ flex: 1 }}>
              <Select options={statusOptions} />
            </Form.Item>
          </Space>

          <LocalizedFieldGroup
            form={floorForm}
            label={'\u6a13\u5c64\u540d\u7a31'}
            fieldNames={floorNameFields}
            required
            translationDefaults={translationDefaults}
          />

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="areaSqm" label={'\u6a13\u5c64\u9762\u7a4d\uff08\u5e73\u65b9\u7c73\uff09'} style={{ flex: 1 }}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="altitudeMeters" label={'\u6d77\u62d4\u9ad8\u5ea6\uff08\u7c73\uff09'} style={{ flex: 1 }}>
              <InputNumber style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="indoorMapId" label={'\u5ba4\u5167\u5730\u5716\u8cc7\u6e90 ID'} style={{ flex: 1 }}>
              <InputNumber min={1} style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <Space style={{ display: 'flex' }} size={16} align="start">
            <Form.Item name="zoomMin" label={'\u6700\u5c0f\u7e2e\u653e\u500d\u6578'} style={{ flex: 1 }}>
              <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="defaultZoom" label={'\u9810\u8a2d\u7e2e\u653e\u500d\u6578'} style={{ flex: 1 }}>
              <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="zoomMax" label={'\u6700\u5927\u7e2e\u653e\u500d\u6578'} style={{ flex: 1 }}>
              <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
            </Form.Item>
          </Space>

          <MediaAssetPickerField
            name="coverAssetId"
            label={'\u6a13\u5c64\u5c01\u9762'}
            assetKind="image"
            valueMode="asset-id"
          />
          <MediaAssetPickerField
            name="floorPlanAssetId"
            label={'\u6a13\u5c64\u5e73\u9762\u5716 / \u9810\u89bd\u5716'}
            assetKind="image"
            valueMode="asset-id"
          />
          <SpatialAttachmentListField name="attachments" title={'\u6a13\u5c64\u9644\u4ef6\u8cc7\u6e90'} />

          <LocalizedFieldGroup
            form={floorForm}
            label={'\u6a13\u5c64\u4ecb\u7d39'}
            fieldNames={floorDescriptionFields}
            multiline
            translationDefaults={translationDefaults}
          />

          <SpatialPopupDisplayField
            form={floorForm}
            popupFieldName="popupConfigJson"
            displayFieldName="displayConfigJson"
          />
        </Form>
      </Modal>
    </>
  );

  if (embedded) {
    return content;
  }

  const syncSearchParams = (nextTab: 'catalog' | 'authoring', buildingId?: number | null, floorId?: number | null) => {
    const nextParams = new URLSearchParams(searchParams);
    nextParams.set('tab', nextTab);
    if (buildingId) {
      nextParams.set('buildingId', String(buildingId));
    } else {
      nextParams.delete('buildingId');
    }
    if (floorId) {
      nextParams.set('floorId', String(floorId));
    } else {
      nextParams.delete('floorId');
    }
    if (nextParams.toString() === searchParams.toString()) {
      return;
    }
    setSearchParams(nextParams, { replace: true });
  };

  const authoringBuildingId = searchBuildingId ?? selectedBuildingId ?? activeBuilding?.id ?? null;
  const authoringFloorId = searchFloorId ?? activeBuilding?.floors?.[0]?.id ?? null;

  return (
    <PageContainer
      title={'\u5ba4\u5167\u5efa\u7bc9\u8207\u5c0f\u5730\u5716\u7ba1\u7406'}
      subTitle={'\u5728\u540c\u4e00\u500b\u6a21\u7d44\u5167\u5b8c\u6210\u5ba4\u5167\u5efa\u7bc9\u3001\u6a13\u5c64\u3001\u5716\u8cc7\u3001\u6a19\u8a18\u8207 CSV \u532f\u5165\u7684\u5168\u5957\u5de5\u4f5c\u6d41\u3002'}
    >
      <Tabs
        activeKey={activeTab}
        destroyOnHidden
        onChange={(key) =>
          syncSearchParams(
            key === 'authoring' ? 'authoring' : 'catalog',
            selectedBuildingId ?? activeBuilding?.id ?? null,
            searchFloorId ?? null,
          )
        }
        items={[
          {
            key: 'catalog',
            label: '\u57fa\u790e\u8cc7\u6599',
            children: content,
          },
          {
            key: 'authoring',
            label: '\u5716\u8cc7\u3001\u6a19\u8a18\u8207 CSV',
            children: (
              <MapTileManagement
                embedded
                initialBuildingId={authoringBuildingId}
                initialFloorId={authoringFloorId}
                onSelectionChange={({ buildingId, floorId }) => syncSearchParams('authoring', buildingId, floorId)}
              />
            ),
          },
        ]}
      />
    </PageContainer>
  );
};

export default IndoorBuildingManagement;
