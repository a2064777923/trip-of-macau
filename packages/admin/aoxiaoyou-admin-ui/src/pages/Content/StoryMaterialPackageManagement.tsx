import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  FileImageOutlined,
  FileOutlined,
  FileTextOutlined,
  LinkOutlined,
  PlayCircleOutlined,
  SoundOutlined,
} from '@ant-design/icons';
import { PageContainer } from '@ant-design/pro-components';
import {
  Alert,
  App as AntdApp,
  Button,
  Card,
  Col,
  Descriptions,
  Drawer,
  Empty,
  Form,
  Image,
  Input,
  Modal,
  Row,
  Select,
  Skeleton,
  Space,
  Statistic,
  Table,
  Tag,
  Tooltip,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  bindStoryMaterialItemCandidate,
  createAiGenerationJob,
  finalizeAiGenerationCandidate,
  getAiVoices,
  approveStoryMaterialQaItem,
  getStoryMaterialQaDetail,
  getStoryMaterialQaItems,
  getStoryMaterialQaOverview,
  getStoryMaterialItemVersions,
  getStoryMaterialPackage,
  getStoryMaterialPackages,
  importStoryMaterialItemAsset,
  previewAiVoice,
  previewStoryMaterialProduction,
  promoteStoryMaterialItem,
  refreshAiGenerationJob,
  rejectStoryMaterialQaItem,
  replaceStoryMaterialQaItem,
  rollbackStoryMaterialItemVersion,
  runStoryMaterialQaConsistencyCheck,
} from '../../services/api';
import type {
  AiGenerationJobItem,
  AiVoiceItem,
  StoryMaterialPackageDetail,
  StoryMaterialPackageItem,
  StoryMaterialPackageSummary,
} from '../../services/api';
import type {
  AdminContentAssetItem,
  StoryMaterialQaConsistencyReport,
  StoryMaterialQaDetail,
  StoryMaterialQaFinding,
  StoryMaterialQaItem,
  StoryMaterialQaItemQuery,
  StoryMaterialQaOverview,
  StoryMaterialProductionPreflightResponse,
  StoryMaterialVersionRecord,
} from '../../types/admin';
import MediaAssetPickerField from '../../components/media/MediaAssetPickerField';
import './StoryMaterialPackageManagement.scss';

const { Paragraph, Text, Title } = Typography;

interface ImportFormValues {
  itemId?: number;
  relativeLocalPath?: string;
  forcedCosObjectKey?: string;
  providerName?: string;
  modelCode?: string;
  estimatedCost?: string;
  verificationNote?: string;
}

interface NarrationFormValues {
  itemId?: number;
  providerId?: number;
  modelCode?: string;
  voiceCode?: string;
  languageCode?: string;
  scriptText?: string;
}

const statusLabelMap: Record<string, string> = {
  draft: '編輯中',
  planned: '已規劃',
  published: '已發佈',
  archived: '已封存',
  uploaded: '已上傳',
  generated: '已生成',
  approved: '已審批',
  manual_import_required: '需手動匯入',
  retry_required: '需重試',
  rejected: '已拒絕',
};

const statusColorMap: Record<string, string> = {
  draft: 'gold',
  planned: 'blue',
  published: 'green',
  archived: 'default',
  uploaded: 'cyan',
  generated: 'purple',
  approved: 'lime',
  manual_import_required: 'red',
  retry_required: 'orange',
  rejected: 'red',
};

const healthLabelMap: Record<string, { label: string; color: string; description: string }> = {
  usable: { label: '可用', color: 'green', description: '目前版本可被操作員檢視與復用。' },
  planned_slot: { label: '待生產', color: 'gold', description: '仍是素材需求位，尚未有可用資產。' },
  missing_asset: { label: '缺少資產', color: 'red', description: '目前或已發布版本缺少 content asset。' },
  no_public_url: { label: '無公開連結', color: 'orange', description: '缺少 canonicalUrl，無法穩定預覽或進入 runtime。' },
  unpublished: { label: '未發布', color: 'blue', description: '有資產但未發布到故事 runtime。' },
  stale_version: { label: '版本過期', color: 'volcano', description: '目前指向的版本不是最新版本。' },
  cos_unavailable: { label: 'COS 不可用', color: 'red', description: 'COS HEAD 或公開資源檢查失敗。' },
  wrong_kind: { label: '類型不符', color: 'red', description: '資產類型與素材需求不一致。' },
  oversized: { label: '過大', color: 'orange', description: '檔案尺寸可能影響小程序載入。' },
  preview_failed: { label: '預覽失敗', color: 'orange', description: '資產處理狀態或載入狀態不穩。' },
  needs_regeneration: { label: '需重生', color: 'magenta', description: '需要重新生成或手動匯入替代資源。' },
  rejected: { label: '已拒絕', color: 'red', description: '目前或歷史版本已被 QA 拒絕。' },
};

const healthSummaryOrder = [
  'usable',
  'planned_slot',
  'no_public_url',
  'unpublished',
  'stale_version',
  'cos_unavailable',
  'needs_regeneration',
  'rejected',
];

const itemTypeLabelMap: Record<string, string> = {
  image: '圖片',
  icon: '圖標',
  audio: '音訊',
  video: '影片',
  lottie: 'Lottie 動畫',
  json: 'JSON',
  script: '腳本',
  prompt: '提示詞',
  content_block: '內容積木',
  story_chapter: '故事章節',
  exploration_element: '探索元素',
  game_reward: '遊戲內獎勵',
  honor_title: '榮譽稱號',
};

const targetLabelMap: Record<string, string> = {
  storyline: '故事線',
  story_chapter: '章節',
  content_asset: '內容資產',
  content_block: '內容積木',
  experience_flow: '體驗流程',
  exploration_element: '探索元素',
  game_reward: '遊戲內獎勵',
  honor_title: '榮譽稱號',
};

const quickActions = [
  { label: '媒體資源', path: '/content/media' },
  { label: '內容積木', path: '/content/blocks' },
  { label: '故事路線編排', path: '/content/storyline-mode' },
  { label: '體驗流程', path: '/content/experience' },
  { label: '奬勵配置', path: '/collection/game-rewards' },
];

interface FilterState {
  keyword?: string;
  packageStatus?: string;
}

type MaterialAssetFilter =
  | 'all'
  | 'usable'
  | 'planned_slot'
  | 'missing_asset'
  | 'no_public_url'
  | 'unpublished'
  | 'stale_version'
  | 'cos_unavailable'
  | 'needs_regeneration'
  | 'rejected';

interface MaterialAssetState {
  key: Exclude<MaterialAssetFilter, 'all'>;
  label: string;
  color: string;
  description: string;
}

const materialAssetStateMeta: Record<Exclude<MaterialAssetFilter, 'all'>, MaterialAssetState> = {
  usable: {
    key: 'usable',
    label: '可用資產',
    color: 'green',
    description: '已有版本、公開連結與發布版本，可進入故事消費鏈路。',
  },
  missing_asset: {
    key: 'missing_asset',
    label: '缺少資產',
    color: 'gold',
    description: '這一行是素材需求位，尚未匯入或生成真正資產，因此保留用來追蹤待辦。',
  },
  planned_slot: {
    key: 'planned_slot',
    label: '待生產',
    color: 'gold',
    description: '仍是素材需求位，等待生成、匯入或替換資產。',
  },
  no_public_url: {
    key: 'no_public_url',
    label: '無公開連結',
    color: 'orange',
    description: '已有本地或 COS 路徑，但缺少 canonicalUrl，通常代表尚未同步到可預覽的公開資源。',
  },
  unpublished: {
    key: 'unpublished',
    label: '未發布版本',
    color: 'blue',
    description: '已有版本或資產，但尚未發布到故事 runtime。',
  },
  stale_version: {
    key: 'stale_version',
    label: '版本過期',
    color: 'volcano',
    description: '目前指向版本不是最新版本，需確認是否採用新版本。',
  },
  cos_unavailable: {
    key: 'cos_unavailable',
    label: 'COS 不可用',
    color: 'red',
    description: 'COS 公開資源檢查失敗，需重新上傳或修正權限。',
  },
  needs_regeneration: {
    key: 'needs_regeneration',
    label: '需重生',
    color: 'magenta',
    description: '素材狀態顯示需要重新生成或手動匯入。',
  },
  rejected: {
    key: 'rejected',
    label: '已拒絕',
    color: 'red',
    description: '目前版本或項目被 QA 拒絕，需替換或回滾。',
  },
};

interface QaFilterState {
  keyword?: string;
  itemStatus?: string;
  assetKind?: string;
  chapterCode?: string;
  usageTarget?: string;
  healthState?: string;
  runtimeExposure?: string;
  providerName?: string;
  modelCode?: string;
}

interface QaActionFormValues {
  versionId?: number;
  replacementAssetId?: number;
  targetStatus?: string;
  note?: string;
  confirmedImpact?: boolean;
}

function pickPackageTitle(packageItem?: Partial<StoryMaterialPackageSummary | StoryMaterialPackageDetail> | null) {
  return (
    packageItem?.titleZht ||
    packageItem?.titleZh ||
    packageItem?.titleEn ||
    packageItem?.titlePt ||
    packageItem?.code ||
    '未命名故事素材包'
  );
}

function displayText(value?: string | number | null, fallback = '未填寫') {
  if (value === null || typeof value === 'undefined' || value === '') {
    return fallback;
  }
  return String(value);
}

function statusTag(status?: string) {
  if (!status) {
    return <Tag>未設定</Tag>;
  }
  return <Tag color={statusColorMap[status] || 'default'}>{statusLabelMap[status] || status}</Tag>;
}

function typedTag(value?: string, fallback = '未分類') {
  if (!value) {
    return <Tag>{fallback}</Tag>;
  }
  return <Tag>{itemTypeLabelMap[value] || targetLabelMap[value] || value}</Tag>;
}

function compactMoney(value?: string | number | null) {
  if (value === null || typeof value === 'undefined' || value === '') {
    return '0';
  }
  return String(value);
}

function isHttpUrl(value?: string | null) {
  return /^https?:\/\//i.test(value || '');
}

function fileNameFromPath(value?: string | null) {
  if (!value) {
    return '';
  }
  const withoutQuery = value.split('?')[0].split('#')[0];
  const parts = withoutQuery.split(/[\\/]/).filter(Boolean);
  const fileName = parts[parts.length - 1] || withoutQuery;
  try {
    return decodeURIComponent(fileName);
  } catch {
    return fileName;
  }
}

function shortenMiddle(value: string, head = 22, tail = 28) {
  if (value.length <= head + tail + 3) {
    return value;
  }
  return `${value.slice(0, head)}...${value.slice(-tail)}`;
}

function assetLocation(record: StoryMaterialVersionRecord) {
  return record.canonicalUrl || record.cosObjectKey || record.localPath || '';
}

function assetDisplayName(record: StoryMaterialVersionRecord) {
  const location = assetLocation(record);
  return fileNameFromPath(location) || record.assetKind || `asset-${record.contentAssetId || record.id}`;
}

function isImageLike(record: StoryMaterialVersionRecord) {
  const value = `${record.assetKind || ''} ${assetLocation(record)}`.toLowerCase();
  return (
    value.includes('image') ||
    value.includes('icon') ||
    /\.(png|jpe?g|webp|gif|svg)$/i.test(value)
  );
}

function isVideoLike(record: StoryMaterialVersionRecord) {
  const value = `${record.assetKind || ''} ${assetLocation(record)}`.toLowerCase();
  return value.includes('video') || /\.(mp4|webm|mov|m4v)$/i.test(value);
}

function isAudioLike(record: StoryMaterialVersionRecord) {
  const value = `${record.assetKind || ''} ${assetLocation(record)}`.toLowerCase();
  return value.includes('audio') || /\.(mp3|wav|m4a|aac|ogg)$/i.test(value);
}

function isLottieLike(record: StoryMaterialVersionRecord) {
  const value = `${record.assetKind || ''} ${assetLocation(record)}`.toLowerCase();
  return value.includes('lottie');
}

function firstCandidate(job?: AiGenerationJobItem | null) {
  return (job?.candidates || []).find((item) => item.isFinalized || item.finalizedAssetId || item.storageUrl);
}

function PathCell({ value }: { value?: string | null }) {
  if (!value) {
    return <Text type="secondary">未配置</Text>;
  }
  return (
    <Tooltip title={value} placement="topLeft">
      <span className="story-material-package__path" title={value}>
        {value}
      </span>
    </Tooltip>
  );
}

function VersionAssetPreview({ record }: { record: StoryMaterialVersionRecord }) {
  const location = assetLocation(record);
  const previewUrl = isHttpUrl(record.canonicalUrl) ? record.canonicalUrl : '';
  const displayName = assetDisplayName(record);
  const compactLocation = location ? shortenMiddle(location) : '';
  const kind = record.assetKind || 'asset';

  let preview: React.ReactNode = (
    <div className="story-material-package__asset-preview-fallback">
      <FileOutlined />
      <span>{kind}</span>
    </div>
  );

  if (previewUrl && isImageLike(record)) {
    preview = (
      <Image
        src={previewUrl}
        alt={displayName}
        width={96}
        height={72}
        className="story-material-package__asset-preview-image"
      />
    );
  } else if (previewUrl && isVideoLike(record)) {
    preview = (
      <video
        src={previewUrl}
        className="story-material-package__asset-preview-video"
        muted
        controls
        preload="metadata"
      />
    );
  } else if (previewUrl && isAudioLike(record)) {
    preview = (
      <div className="story-material-package__asset-preview-audio">
        <SoundOutlined />
        <audio src={previewUrl} controls preload="metadata" />
      </div>
    );
  } else if (isLottieLike(record)) {
    preview = (
      <div className="story-material-package__asset-preview-fallback story-material-package__asset-preview-fallback--lottie">
        <PlayCircleOutlined />
        <span>Lottie</span>
      </div>
    );
  } else if ((record.assetKind || '').toLowerCase() === 'json' || /\.json$/i.test(location)) {
    preview = (
      <div className="story-material-package__asset-preview-fallback">
        <FileTextOutlined />
        <span>JSON</span>
      </div>
    );
  } else if (isImageLike(record)) {
    preview = (
      <div className="story-material-package__asset-preview-fallback">
        <FileImageOutlined />
        <span>圖片</span>
      </div>
    );
  }

  return (
    <div className="story-material-package__version-asset">
      <div className="story-material-package__asset-preview">{preview}</div>
      <div className="story-material-package__version-asset-info">
        <Space size={[4, 4]} wrap>
          <Tag color={isLottieLike(record) ? 'purple' : 'default'}>{kind}</Tag>
          {record.contentAssetId ? <Tag color="blue">#{record.contentAssetId}</Tag> : null}
        </Space>
        <Tooltip title={location || '未配置資產路徑'} placement="topLeft">
          {previewUrl ? (
            <Typography.Link
              href={previewUrl}
              target="_blank"
              rel="noreferrer"
              className="story-material-package__asset-link"
            >
              <LinkOutlined /> {displayName}
            </Typography.Link>
          ) : (
            <Text className="story-material-package__asset-link">{displayName}</Text>
          )}
        </Tooltip>
        {compactLocation ? (
          <Tooltip title={location} placement="topLeft">
            <Text type="secondary" className="story-material-package__asset-url material-url-text">
              {compactLocation}
            </Text>
          </Tooltip>
        ) : (
          <Text type="secondary">未配置資產路徑</Text>
        )}
        {record.posterFallbackItemKey ? (
          <Text type="secondary" className="story-material-package__asset-url material-url-text">
            fallback：{record.posterFallbackItemKey}
          </Text>
        ) : null}
      </div>
    </div>
  );
}

function countDistinct(values: Array<string | undefined | null>) {
  return new Set(values.filter((value): value is string => Boolean(value && value !== 'global' && value !== 'storyline'))).size;
}

function isExplorationItem(item: StoryMaterialPackageItem) {
  const fields = [item.itemType, item.targetType, item.usageTarget, item.targetCode, item.itemKey]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();
  return fields.includes('exploration') || fields.includes('pickup') || fields.includes('challenge');
}

function getMaterialItemAssetState(item: StoryMaterialPackageItem): MaterialAssetState {
  const version = item.versionSummary;
  const hasAsset = Boolean(item.assetId || item.currentVersionId || version?.contentAssetId);
  const hasAnyLocation = Boolean(
    item.canonicalUrl ||
      item.cosObjectKey ||
      item.localPath ||
      version?.canonicalUrl ||
      version?.cosObjectKey ||
      version?.localPath,
  );
  const hasPublicUrl = Boolean(item.canonicalUrl || version?.canonicalUrl);
  const hasPublishedVersion = Boolean(item.publishedVersionId || version?.promotionStatus === 'published');

  if (!hasAsset && !hasAnyLocation) {
    return materialAssetStateMeta.missing_asset;
  }
  if (!hasPublicUrl) {
    return materialAssetStateMeta.no_public_url;
  }
  if (!hasPublishedVersion) {
    return materialAssetStateMeta.unpublished;
  }
  return materialAssetStateMeta.usable;
}

function qaHealthState(qaItem?: StoryMaterialQaItem | null): MaterialAssetState | null {
  const state = qaItem?.healthStates?.[0] as MaterialAssetFilter | undefined;
  if (!state || state === 'all') {
    return null;
  }
  return materialAssetStateMeta[state] || {
    key: state,
    label: healthLabelMap[state]?.label || state,
    color: healthLabelMap[state]?.color || 'default',
    description: healthLabelMap[state]?.description || state,
  };
}

function qaFindingSeverityTag(severity?: string) {
  const normalized = (severity || '').toLowerCase();
  if (normalized === 'blocking') {
    return <Tag color="red">阻斷</Tag>;
  }
  if (normalized === 'warning') {
    return <Tag color="orange">警告</Tag>;
  }
  if (normalized === 'info') {
    return <Tag color="blue">資訊</Tag>;
  }
  return <Tag>{severity || '未分類'}</Tag>;
}

function qaFindingRowKey(finding: StoryMaterialQaFinding) {
  return [
    finding.severity,
    finding.findingCode,
    finding.sourceType,
    finding.sourceId,
    finding.expectedValue,
    finding.actualValue,
    finding.messageZht,
  ]
    .filter(Boolean)
    .join('|');
}

function qaItemToLegacyItem(qaItem: StoryMaterialQaItem): StoryMaterialPackageItem {
  return {
    id: qaItem.id,
    packageId: qaItem.packageId,
    itemKey: qaItem.itemKey,
    itemType: qaItem.itemType,
    assetKind: qaItem.assetKind,
    targetType: qaItem.targetType,
    targetId: qaItem.targetId,
    targetCode: qaItem.targetCode,
    assetId: qaItem.assetId,
    currentVersionId: qaItem.currentVersionId,
    currentVersionNo: qaItem.currentVersionNo,
    publishedVersionId: qaItem.publishedVersionId,
    versionSummary: qaItem.currentVersion || null,
    lastProducedAt: qaItem.lastProducedAt,
    localPath: qaItem.localPath,
    cosObjectKey: qaItem.cosObjectKey,
    canonicalUrl: qaItem.canonicalUrl,
    usageTarget: qaItem.usageTarget,
    chapterCode: qaItem.chapterCode,
    status: qaItem.itemStatus,
    createdAt: undefined,
    updatedAt: qaItem.updatedAt,
  };
}

function deriveCounters(detail?: StoryMaterialPackageDetail | null) {
  const items = detail?.items || [];
  return {
    materialCount: detail?.counters?.materialCount ?? items.length,
    assetCount: detail?.counters?.assetCount ?? items.filter((item) => item.assetId).length,
    storyObjectCount:
      detail?.counters?.storyObjectCount ??
      items.filter((item) => item.targetType && (item.targetId || item.targetCode)).length,
    chapterCount: countDistinct(items.map((item) => item.chapterCode)),
    explorationCount: items.filter(isExplorationItem).length,
  };
}

function deriveAssetHealthCounters(detail?: StoryMaterialPackageDetail | null) {
  const items = detail?.items || [];
  return items.reduce(
    (acc, item) => {
      acc[getMaterialItemAssetState(item).key] += 1;
      return acc;
    },
    {
      usable: 0,
      missing_asset: 0,
      no_public_url: 0,
      unpublished: 0,
    } as Record<Exclude<MaterialAssetFilter, 'all'>, number>,
  );
}

function deriveQaHealthCounters(overview?: StoryMaterialQaOverview | null) {
  const counters = overview?.healthStateCounters || {};
  return {
    usable: counters.usable || 0,
    planned_slot: counters.planned_slot || 0,
    missing_asset: counters.missing_asset || 0,
    no_public_url: counters.no_public_url || 0,
    unpublished: counters.unpublished || 0,
    stale_version: counters.stale_version || 0,
    cos_unavailable: counters.cos_unavailable || 0,
    needs_regeneration: counters.needs_regeneration || 0,
    rejected: counters.rejected || 0,
  } as Record<Exclude<MaterialAssetFilter, 'all'>, number>;
}

const StoryMaterialPackageManagement: React.FC = () => {
  const { message } = AntdApp.useApp();
  const navigate = useNavigate();
  const [packages, setPackages] = useState<StoryMaterialPackageSummary[]>([]);
  const [selectedPackageId, setSelectedPackageId] = useState<number | null>(null);
  const [detail, setDetail] = useState<StoryMaterialPackageDetail | null>(null);
  const [loadingList, setLoadingList] = useState(false);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [preflightOpen, setPreflightOpen] = useState(false);
  const [preflightLoading, setPreflightLoading] = useState(false);
  const [preflightResult, setPreflightResult] = useState<StoryMaterialProductionPreflightResponse | null>(null);
  const [importOpen, setImportOpen] = useState(false);
  const [importLoading, setImportLoading] = useState(false);
  const [narrationOpen, setNarrationOpen] = useState(false);
  const [narrationLoading, setNarrationLoading] = useState(false);
  const [voicePreviewUrl, setVoicePreviewUrl] = useState<string>();
  const [voiceOptions, setVoiceOptions] = useState<AiVoiceItem[]>([]);
  const [versionOpen, setVersionOpen] = useState(false);
  const [versionLoading, setVersionLoading] = useState(false);
  const [versionItem, setVersionItem] = useState<StoryMaterialPackageItem | null>(null);
  const [versions, setVersions] = useState<StoryMaterialVersionRecord[]>([]);
  const [qaOverview, setQaOverview] = useState<StoryMaterialQaOverview | null>(null);
  const [qaItems, setQaItems] = useState<StoryMaterialQaItem[]>([]);
  const [qaLoading, setQaLoading] = useState(false);
  const [qaFilters, setQaFilters] = useState<QaFilterState>({});
  const [qaDetail, setQaDetail] = useState<StoryMaterialQaDetail | null>(null);
  const [qaDetailLoading, setQaDetailLoading] = useState(false);
  const [qaReportOpen, setQaReportOpen] = useState(false);
  const [qaReportLoading, setQaReportLoading] = useState(false);
  const [qaReport, setQaReport] = useState<StoryMaterialQaConsistencyReport | null>(null);
  const [qaActionOpen, setQaActionOpen] = useState(false);
  const [qaActionType, setQaActionType] = useState<'reject' | 'approve' | 'replace' | null>(null);
  const [qaActionLoading, setQaActionLoading] = useState(false);
  const [preflightForm] = Form.useForm();
  const [importForm] = Form.useForm<ImportFormValues>();
  const [narrationForm] = Form.useForm<NarrationFormValues>();
  const [qaActionForm] = Form.useForm<QaActionFormValues>();
  const watchedImportItemId = Form.useWatch('itemId', importForm);
  const [filters, setFilters] = useState<FilterState>({
    keyword: '東西方文明的戰火與共生',
    packageStatus: undefined,
  });
  const [assetFilter, setAssetFilter] = useState<MaterialAssetFilter>('all');

  const counters = useMemo(() => deriveCounters(detail), [detail]);
  const fallbackAssetHealthCounters = useMemo(() => deriveAssetHealthCounters(detail), [detail]);
  const assetHealthCounters = useMemo(
    () => (qaOverview ? deriveQaHealthCounters(qaOverview) : fallbackAssetHealthCounters),
    [fallbackAssetHealthCounters, qaOverview],
  );
  const visibleMaterialItems = useMemo(() => {
    if (qaItems.length) {
      if (assetFilter === 'all') {
        return qaItems.map(qaItemToLegacyItem);
      }
      return qaItems
        .filter((item) => item.healthStates?.includes(assetFilter))
        .map(qaItemToLegacyItem);
    }
    const items = detail?.items || [];
    if (assetFilter === 'all') {
      return items;
    }
    return items.filter((item) => getMaterialItemAssetState(item).key === assetFilter);
  }, [assetFilter, detail?.items, qaItems]);

  const selectedSummary = useMemo(
    () => packages.find((item) => item.id === selectedPackageId) || null,
    [packages, selectedPackageId],
  );

  const itemOptions = useMemo(
    () =>
      (detail?.items || []).map((item) => ({
        value: item.id,
        label: `${item.itemKey} / ${item.assetKind || 'other'} / v${item.currentVersionNo || 0}`,
      })),
    [detail?.items],
  );

  const selectedImportItem = useMemo(
    () => (detail?.items || []).find((item) => item.id === watchedImportItemId),
    [detail?.items, watchedImportItemId],
  );

  const buildQaQuery = (nextFilters = qaFilters): StoryMaterialQaItemQuery => ({
    pageNum: 1,
    pageSize: 500,
    keyword: nextFilters.keyword,
    itemStatus: nextFilters.itemStatus,
    assetKind: nextFilters.assetKind,
    chapterCode: nextFilters.chapterCode,
    usageTarget: nextFilters.usageTarget,
    healthState: nextFilters.healthState,
    runtimeExposure: nextFilters.runtimeExposure,
    providerName: nextFilters.providerName,
    modelCode: nextFilters.modelCode,
  });

  const loadQaWorkspace = async (packageId: number, nextFilters = qaFilters) => {
    setQaLoading(true);
    try {
      const query = buildQaQuery(nextFilters);
      const [overviewResponse, itemsResponse] = await Promise.all([
        getStoryMaterialQaOverview(packageId, query),
        getStoryMaterialQaItems(packageId, query),
      ]);
      if (!overviewResponse.success || !overviewResponse.data) {
        throw new Error(overviewResponse.message || '讀取 QA 總覽失敗');
      }
      if (!itemsResponse.success || !itemsResponse.data) {
        throw new Error(itemsResponse.message || '讀取 QA 素材列表失敗');
      }
      setQaOverview(overviewResponse.data);
      setQaItems(itemsResponse.data.list || []);
    } catch (error) {
      message.warning(error instanceof Error ? error.message : 'QA 工作台資料暫時不可用，已退回素材包基本資料。');
      setQaOverview(null);
      setQaItems([]);
    } finally {
      setQaLoading(false);
    }
  };

  const loadDetail = async (packageId: number) => {
    setSelectedPackageId(packageId);
    setLoadingDetail(true);
    try {
      const response = await getStoryMaterialPackage(packageId);
      if (!response.success || !response.data) {
        throw new Error(response.message || '讀取故事素材包詳情失敗');
      }
      setDetail(response.data);
      await loadQaWorkspace(packageId);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '讀取故事素材包詳情失敗');
      setDetail(null);
      setQaOverview(null);
      setQaItems([]);
    } finally {
      setLoadingDetail(false);
    }
  };

  const loadPackages = async (nextFilters = filters) => {
    setLoadingList(true);
    try {
      const response = await getStoryMaterialPackages({
        pageNum: 1,
        pageSize: 24,
        keyword: nextFilters.keyword,
        packageStatus: nextFilters.packageStatus,
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || '讀取故事素材包失敗');
      }
      const nextPackages = response.data.list || [];
      setPackages(nextPackages);
      const nextSelected =
        nextPackages.find((item) => item.id === selectedPackageId) ||
        nextPackages.find((item) => item.code === 'east_west_war_and_coexistence_package') ||
        nextPackages[0];
      if (nextSelected) {
        await loadDetail(nextSelected.id);
      } else {
        setSelectedPackageId(null);
        setDetail(null);
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '讀取故事素材包失敗');
    } finally {
      setLoadingList(false);
    }
  };

  const reloadSelectedDetail = async () => {
    if (selectedPackageId) {
      await loadDetail(selectedPackageId);
    }
  };

  const refreshQaOnly = async (nextFilters = qaFilters) => {
    if (!selectedPackageId) {
      return;
    }
    await loadQaWorkspace(selectedPackageId, nextFilters);
  };

  const handlePreflight = async () => {
    if (!detail) {
      message.warning('請先選擇故事素材包');
      return;
    }
    setPreflightLoading(true);
    try {
      const values = await preflightForm.validateFields();
      const response = await previewStoryMaterialProduction(detail.id, {
        estimatedTotalCost: values.estimatedTotalCost,
        dailyCostCeiling: values.dailyCostCeiling,
        batchCostCeiling: values.batchCostCeiling,
        verificationNote: values.verificationNote,
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || '生產預檢失敗');
      }
      setPreflightResult(response.data);
      setPreflightOpen(true);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '生產預檢失敗');
    } finally {
      setPreflightLoading(false);
    }
  };

  const openImportModal = (record?: StoryMaterialPackageItem) => {
    importForm.setFieldsValue({
      itemId: record?.id,
      relativeLocalPath: record?.localPath,
      forcedCosObjectKey: record?.cosObjectKey,
      providerName: record?.provenanceType || 'local',
      modelCode: 'manual-import',
      estimatedCost: '0',
      verificationNote: 'Phase 36 後台匯入',
    });
    setImportOpen(true);
  };

  const handleImportSubmit = async () => {
    if (!detail) {
      return;
    }
    setImportLoading(true);
    try {
      const values = await importForm.validateFields();
      const item = (detail.items || []).find((entry) => entry.id === values.itemId);
      if (!item) {
        throw new Error('請選擇素材項目');
      }
      const response = await importStoryMaterialItemAsset(detail.id, item.id, {
        relativeLocalPath: values.relativeLocalPath || item.localPath || '',
        forcedCosObjectKey: values.forcedCosObjectKey || item.cosObjectKey,
        providerName: values.providerName || 'local',
        modelCode: values.modelCode || 'manual-import',
        estimatedCost: values.estimatedCost || '0',
        assetKind: item.assetKind,
        promptText: item.promptText,
        scriptText: item.scriptText,
        posterFallbackItemKey: item.fallbackItemKey,
        verificationNote: values.verificationNote,
        localeCode: 'zh-Hant',
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || '匯入本地素材失敗');
      }
      message.success(`已匯入版本 v${response.data.versionNo || ''}`);
      setImportOpen(false);
      await reloadSelectedDetail();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '匯入本地素材失敗');
    } finally {
      setImportLoading(false);
    }
  };

  const openNarrationDrawer = async (record?: StoryMaterialPackageItem) => {
    narrationForm.setFieldsValue({
      itemId: record?.id || (detail?.items || []).find((item) => item.assetKind === 'audio')?.id,
      languageCode: 'zh',
      scriptText: record?.scriptText || '',
    });
    setNarrationOpen(true);
    try {
      const response = await getAiVoices({ languageCode: 'zh' });
      if (response.success && response.data) {
        setVoiceOptions(response.data);
      }
    } catch {
      setVoiceOptions([]);
    }
  };

  const handleVoicePreview = async () => {
    const values = await narrationForm.validateFields(['providerId', 'modelCode', 'voiceCode', 'languageCode', 'scriptText']);
    setNarrationLoading(true);
    try {
      const response = await previewAiVoice({
        providerId: Number(values.providerId),
        modelCode: values.modelCode!,
        voiceCode: values.voiceCode!,
        languageCode: values.languageCode,
        scriptText: values.scriptText,
      });
      if (!response.success || !response.data?.previewUrl) {
        throw new Error(response.message || '生成試聽失敗');
      }
      setVoicePreviewUrl(response.data.previewUrl);
      message.success('已生成試聽');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '生成試聽失敗');
    } finally {
      setNarrationLoading(false);
    }
  };

  const handleNarrationGenerate = async () => {
    if (!detail) {
      return;
    }
    setNarrationLoading(true);
    try {
      const values = await narrationForm.validateFields();
      const item = (detail.items || []).find((entry) => entry.id === values.itemId);
      if (!item) {
        throw new Error('請選擇音頻素材項目');
      }
      const jobResponse = await createAiGenerationJob({
        capabilityCode: 'admin_voice_synthesis',
        providerId: Number(values.providerId),
        generationType: 'audio',
        sourceScope: 'story_material_package',
        sourceScopeId: detail.id,
        promptTitle: item.itemKey,
        promptText: values.scriptText || item.scriptText,
        promptVariablesJson: JSON.stringify({
          voiceCode: values.voiceCode,
          languageCode: values.languageCode || 'zh',
        }),
      });
      if (!jobResponse.success || !jobResponse.data) {
        throw new Error(jobResponse.message || '建立旁白任務失敗');
      }
      const refreshed = await refreshAiGenerationJob(jobResponse.data.id);
      const job = refreshed.data || jobResponse.data;
      const candidate = firstCandidate(job);
      if (!candidate) {
        message.warning('旁白任務未取得可綁定候選，狀態標記為 retry_required，請稍後重試或改用手動匯入。');
        return;
      }
      const finalized = candidate.finalizedAssetId
        ? candidate
        : (await finalizeAiGenerationCandidate(candidate.id, { assetKind: 'audio', localeCode: 'zh-Hant', status: 'uploaded' })).data?.candidates?.find((entry) => entry.id === candidate.id);
      if (!finalized?.id) {
        throw new Error('候選音頻未能定稿，請重試或手動匯入');
      }
      const bindResponse = await bindStoryMaterialItemCandidate(detail.id, item.id, {
        aiCandidateId: finalized.id,
        providerName: 'bailian',
        modelCode: values.modelCode,
        assetKind: 'audio',
        scriptText: values.scriptText || item.scriptText,
        posterFallbackItemKey: item.fallbackItemKey,
        verificationNote: 'Phase 36 旁白候選綁定',
      });
      if (!bindResponse.success) {
        throw new Error(bindResponse.message || '綁定旁白候選失敗');
      }
      message.success('已綁定旁白候選版本，發布前請先試聽確認');
      await reloadSelectedDetail();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '生成旁白失敗');
    } finally {
      setNarrationLoading(false);
    }
  };

  const openVersionDrawer = async (record: StoryMaterialPackageItem) => {
    if (!detail) {
      return;
    }
    setVersionItem(record);
    setVersionOpen(true);
    setVersionLoading(true);
    setQaDetailLoading(true);
    try {
      const [versionResponse, qaResponse] = await Promise.all([
        getStoryMaterialItemVersions(detail.id, record.id),
        getStoryMaterialQaDetail(detail.id, record.id),
      ]);
      if (!versionResponse.success) {
        throw new Error(versionResponse.message || '讀取版本歷史失敗');
      }
      setVersions(versionResponse.data || []);
      if (qaResponse.success && qaResponse.data) {
        setQaDetail(qaResponse.data);
      } else {
        setQaDetail(null);
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '讀取版本歷史失敗');
      setVersions([]);
      setQaDetail(null);
    } finally {
      setVersionLoading(false);
      setQaDetailLoading(false);
    }
  };

  const handlePromote = async (record: StoryMaterialPackageItem, version?: StoryMaterialVersionRecord) => {
    if (!detail) {
      return;
    }
    try {
      const response = await promoteStoryMaterialItem(detail.id, record.id, {
        versionId: version?.id || record.currentVersionId,
        targetStatus: 'published',
        superAdminConfirmation: true,
        verificationNote: '後台素材包頁確認發布',
      });
      if (!response.success) {
        throw new Error(response.message || '發布到故事失敗');
      }
      message.success('已發布到故事');
      await reloadSelectedDetail();
      if (versionItem?.id === record.id) {
        await openVersionDrawer(record);
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '發布到故事失敗');
    }
  };

  const handleRollback = async (record: StoryMaterialPackageItem, version: StoryMaterialVersionRecord) => {
    if (!detail) {
      return;
    }
    Modal.confirm({
      title: '回滾版本',
      content: `此操作會把「${record.itemKey}」目前指標切回 v${version.versionNo}，後續版本仍會保留在歷史中，不會刪除。`,
      okText: '確認回滾',
      cancelText: '取消',
      onOk: async () => {
        const response = await rollbackStoryMaterialItemVersion(detail.id, record.id, {
          rollbackVersionId: version.id,
          superAdminConfirmation: true,
          verificationNote: '後台素材包頁回滾版本',
        });
        if (!response.success) {
          throw new Error(response.message || '回滾版本失敗');
        }
        message.success('已回滾版本');
        await reloadSelectedDetail();
        await openVersionDrawer(record);
      },
    });
  };

  const runConsistencyCheck = async () => {
    if (!detail) {
      message.warning('請先選擇故事素材包');
      return;
    }
    setQaReportLoading(true);
    try {
      const response = await runStoryMaterialQaConsistencyCheck(detail.id, {
        includeCosHead: false,
        includeLocalFileCheck: false,
        maxCosChecks: 20,
        runtimeOnly: false,
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || '一致性檢查失敗');
      }
      setQaReport(response.data);
      setQaReportOpen(true);
      await refreshQaOnly();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '一致性檢查失敗');
    } finally {
      setQaReportLoading(false);
    }
  };

  const openQaAction = (type: 'reject' | 'approve' | 'replace', version?: StoryMaterialVersionRecord) => {
    if (!versionItem) {
      return;
    }
    setQaActionType(type);
    qaActionForm.setFieldsValue({
      versionId: version?.id || versionItem.currentVersionId || undefined,
      targetStatus: type === 'approve' ? 'approved' : type === 'replace' ? 'uploaded' : undefined,
      note:
        type === 'reject'
          ? 'QA 檢查後拒絕此版本'
          : type === 'replace'
            ? 'QA 替換為既有媒體資產'
            : 'QA 檢查後批准此版本',
      confirmedImpact: false,
    });
    setQaActionOpen(true);
  };

  const submitQaAction = async () => {
    if (!detail || !versionItem || !qaActionType) {
      return;
    }
    setQaActionLoading(true);
    try {
      const values = await qaActionForm.validateFields();
      const basePayload = {
        versionId: values.versionId,
        note: values.note,
        confirmedImpact: values.confirmedImpact,
      };
      const response =
        qaActionType === 'reject'
          ? await rejectStoryMaterialQaItem(detail.id, versionItem.id, basePayload)
          : qaActionType === 'approve'
            ? await approveStoryMaterialQaItem(detail.id, versionItem.id, {
                ...basePayload,
                targetStatus: values.targetStatus || 'approved',
              })
            : await replaceStoryMaterialQaItem(detail.id, versionItem.id, {
                ...basePayload,
                replacementAssetId: Number(values.replacementAssetId),
                targetStatus: values.targetStatus || 'uploaded',
              });
      if (!response.success) {
        throw new Error(response.message || 'QA 操作失敗');
      }
      message.success(response.data?.messageZht || 'QA 操作已完成');
      setQaActionOpen(false);
      await reloadSelectedDetail();
      await openVersionDrawer(versionItem);
    } catch (error) {
      message.error(error instanceof Error ? error.message : 'QA 操作失敗');
    } finally {
      setQaActionLoading(false);
    }
  };

  useEffect(() => {
    void loadPackages();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const columns = useMemo<ColumnsType<StoryMaterialPackageItem>>(
    () => [
      {
        title: '素材鍵',
        dataIndex: 'itemKey',
        width: 240,
        fixed: 'left',
        render: (value: string, record) => (
          <Space direction="vertical" size={2} className="story-material-package__item-key">
            <Text strong ellipsis={{ tooltip: value }}>
              {value}
            </Text>
            <Text type="secondary" ellipsis={{ tooltip: record.targetCode }}>
              {record.targetCode || `#${record.id}`}
            </Text>
          </Space>
        ),
      },
      {
        title: '類型',
        dataIndex: 'itemType',
        width: 140,
        render: (value: string, record) => (
          <Space size={4} wrap>
            {typedTag(value)}
            {record.assetKind ? <Tag color="geekblue">{itemTypeLabelMap[record.assetKind] || record.assetKind}</Tag> : null}
          </Space>
        ),
      },
      {
        title: '章節',
        dataIndex: 'chapterCode',
        width: 170,
        render: (value: string) => <Text>{displayText(value, '全線 / 全域')}</Text>,
      },
      {
        title: '用途',
        dataIndex: 'usageTarget',
        width: 220,
        render: (value: string, record) => (
          <Space direction="vertical" size={2}>
            <Text>{displayText(value, '未標記用途')}</Text>
            <Text type="secondary">{targetLabelMap[record.targetType || ''] || record.targetType || '未綁定主體'}</Text>
          </Space>
        ),
      },
      {
        title: '資產 ID',
        dataIndex: 'assetId',
        width: 170,
        render: (value: number, record) => {
          const assetState = getMaterialItemAssetState(record);
          return (
            <Space direction="vertical" size={2}>
              {value ? <Text code>{value}</Text> : <Text type="secondary">無</Text>}
              <Tooltip title={assetState.description}>
                <Tag color={assetState.color}>{assetState.label}</Tag>
              </Tooltip>
            </Space>
          );
        },
      },
      {
        title: '健康狀態',
        dataIndex: 'healthState',
        width: 180,
        render: (_value, record) => {
          const qaItem = qaItems.find((item) => item.id === record.id);
          const state = qaHealthState(qaItem) || getMaterialItemAssetState(record);
          return (
            <Space direction="vertical" size={4}>
              <Tooltip title={state.description}>
                <Tag color={state.color}>{state.label}</Tag>
              </Tooltip>
              {qaItem?.findingsCount ? <Text type="secondary">{qaItem.findingsCount} 個發現</Text> : null}
            </Space>
          );
        },
      },
      {
        title: 'COS 路徑',
        dataIndex: 'cosObjectKey',
        width: 320,
        render: (value: string, record) => <PathCell value={value || record.canonicalUrl || record.localPath} />,
      },
      {
        title: '狀態',
        dataIndex: 'status',
        width: 120,
        render: (value: string) => statusTag(value),
      },
      {
        title: '版本',
        dataIndex: 'currentVersionNo',
        width: 150,
        render: (_value, record) => (
          <Space direction="vertical" size={2}>
            <Text>目前 v{record.currentVersionNo || 0}</Text>
            {record.publishedVersionId ? <Text type="secondary">已發布 #{record.publishedVersionId}</Text> : <Text type="secondary">未發布</Text>}
            {record.canonicalUrl || record.versionSummary?.canonicalUrl ? <Tag color="green">有公開 URL</Tag> : <Tag color="orange">無公開 URL</Tag>}
          </Space>
        ),
      },
      {
        title: '來源',
        dataIndex: 'provenanceType',
        width: 140,
        render: (value: string) => <Text>{displayText(value, '未標記')}</Text>,
      },
      {
        title: '操作',
        fixed: 'right',
        width: 260,
        render: (_, record) => (
          <Space wrap size={4}>
            <Button size="small" onClick={() => void openVersionDrawer(record)}>
              QA 詳情
            </Button>
            <Button size="small" onClick={() => openImportModal(record)}>
              匯入
            </Button>
            {record.assetKind === 'audio' ? (
              <Button size="small" onClick={() => void openNarrationDrawer(record)}>
                旁白
              </Button>
            ) : null}
            <Button size="small" type="link" onClick={() => void handlePromote(record)}>
              發布到故事
            </Button>
          </Space>
        ),
      },
    ],
    [detail, importForm, message, qaItems, versionItem],
  );

  const renderPackageCards = () => {
    if (loadingList) {
      return <Skeleton active paragraph={{ rows: 4 }} />;
    }
    if (!packages.length) {
      return <Empty description="尚未找到故事素材包，請確認 Phase 33 種子資料已匯入。" />;
    }
    return (
      <Row gutter={[16, 16]}>
        {packages.map((item) => {
          const active = item.id === selectedPackageId;
          return (
            <Col xs={24} lg={12} xl={8} key={item.id}>
              <Card
                hoverable
                className={active ? 'story-material-package__card story-material-package__card--active' : 'story-material-package__card'}
                onClick={() => void loadDetail(item.id)}
              >
                <Space direction="vertical" size={8} style={{ width: '100%' }}>
                  <Space align="start" style={{ width: '100%', justifyContent: 'space-between' }}>
                    <Title level={5} style={{ margin: 0 }}>
                      {pickPackageTitle(item)}
                    </Title>
                    {statusTag(item.packageStatus)}
                  </Space>
                  <Text type="secondary" ellipsis={{ tooltip: item.code }}>
                    {item.code}
                  </Text>
                  <Paragraph type="secondary" ellipsis={{ rows: 2, tooltip: item.summaryZht || item.summaryZh }}>
                    {item.summaryZht || item.summaryZh || '暫未填寫素材包摘要'}
                  </Paragraph>
                  <Row gutter={12}>
                    <Col span={8}>
                      <Statistic title="素材" value={item.counters?.materialCount || 0} />
                    </Col>
                    <Col span={8}>
                      <Statistic title="資產" value={item.counters?.assetCount || 0} />
                    </Col>
                    <Col span={8}>
                      <Statistic title="物件" value={item.counters?.storyObjectCount || 0} />
                    </Col>
                  </Row>
                </Space>
              </Card>
            </Col>
          );
        })}
      </Row>
    );
  };

  return (
    <PageContainer
      title="故事素材包"
      subTitle="檢視旗艦故事線的文本、素材、資產、章節、體驗流程與探索元素是否已完整落庫。"
      extra={<Button onClick={() => void loadPackages()} loading={loadingList || loadingDetail}>重新載入</Button>}
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Alert
          type="info"
          showIcon
          message="此頁是素材包檢視與導航入口"
          description="媒體、內容積木、故事路線、體驗流程與獎勵仍在既有工作台深度編輯，這裡負責核對完整性、來源與關聯。"
        />

        <Card title="搜尋故事素材包">
          <Space wrap>
            <Input
              allowClear
              placeholder="輸入素材包代碼、故事名稱或摘要"
              style={{ width: 320 }}
              value={filters.keyword}
              onChange={(event) => setFilters((previous) => ({ ...previous, keyword: event.target.value }))}
              onPressEnter={() => void loadPackages()}
            />
            <Select
              allowClear
              placeholder="狀態"
              style={{ width: 180 }}
              value={filters.packageStatus}
              options={[
                { label: '編輯中', value: 'draft' },
                { label: '已規劃', value: 'planned' },
                { label: '已發佈', value: 'published' },
                { label: '已封存', value: 'archived' },
              ]}
              onChange={(value) => setFilters((previous) => ({ ...previous, packageStatus: value }))}
            />
            <Button type="primary" onClick={() => void loadPackages()}>
              查詢
            </Button>
            <Button
              onClick={() => {
                const resetFilters: FilterState = { keyword: '', packageStatus: undefined };
                setFilters(resetFilters);
                void loadPackages(resetFilters);
              }}
            >
              清除
            </Button>
          </Space>
        </Card>

        {renderPackageCards()}

        <Card
          title={detail ? pickPackageTitle(detail) : selectedSummary ? pickPackageTitle(selectedSummary) : '素材包詳情'}
          extra={
            <Space wrap>
              {quickActions.map((action) => (
                <Button key={action.path} onClick={() => navigate(action.path)}>
                  {action.label}
                </Button>
              ))}
            </Space>
          }
        >
          {loadingDetail ? (
            <Skeleton active paragraph={{ rows: 8 }} />
          ) : detail ? (
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Card className="story-material-package__production" size="small">
                <Space wrap>
                  <Button type="primary" onClick={() => void handlePreflight()} loading={preflightLoading}>
                    生產預檢
                  </Button>
                  <Button onClick={() => void runConsistencyCheck()} loading={qaReportLoading}>
                    一致性檢查
                  </Button>
                  <Button onClick={() => openImportModal()}>匯入本地素材</Button>
                  <Button onClick={() => void openNarrationDrawer()}>生成旁白</Button>
                  <Button disabled title="Phase 36-05 會接入 ffmpeg 字幕閘口">
                    建立章節短片
                  </Button>
                  <Button onClick={() => void reloadSelectedDetail()}>重新載入版本</Button>
                </Space>
                <Form form={preflightForm} layout="inline" className="story-material-package__preflight-form">
                  <Form.Item name="estimatedTotalCost" label="估算成本">
                    <Input placeholder="54.50" style={{ width: 120 }} />
                  </Form.Item>
                  <Form.Item name="batchCostCeiling" label="批次上限">
                    <Input placeholder="180.00" style={{ width: 120 }} />
                  </Form.Item>
                  <Form.Item name="dailyCostCeiling" label="每日上限">
                    <Input placeholder="360.00" style={{ width: 120 }} />
                  </Form.Item>
                </Form>
              </Card>

              <Card size="small" title="QA 篩選">
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  <Row gutter={[12, 12]}>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">關鍵字</Text>
                      <Input
                        allowClear
                        placeholder="素材鍵、路徑、用途"
                        value={qaFilters.keyword}
                        onChange={(event) => setQaFilters((previous) => ({ ...previous, keyword: event.target.value }))}
                        onPressEnter={() => void refreshQaOnly()}
                      />
                    </Col>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">素材狀態</Text>
                      <Select
                        allowClear
                        style={{ width: '100%' }}
                        value={qaFilters.itemStatus}
                        options={[
                          { label: '已規劃', value: 'planned' },
                          { label: '已上傳', value: 'uploaded' },
                          { label: '已審批', value: 'approved' },
                          { label: '已發佈', value: 'published' },
                          { label: '需重試', value: 'retry_required' },
                          { label: '已拒絕', value: 'rejected' },
                        ]}
                        onChange={(value) => setQaFilters((previous) => ({ ...previous, itemStatus: value }))}
                      />
                    </Col>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">資產類型</Text>
                      <Select
                        allowClear
                        style={{ width: '100%' }}
                        value={qaFilters.assetKind}
                        options={['image', 'icon', 'audio', 'video', 'lottie', 'json', 'other'].map((value) => ({
                          label: itemTypeLabelMap[value] || value,
                          value,
                        }))}
                        onChange={(value) => setQaFilters((previous) => ({ ...previous, assetKind: value }))}
                      />
                    </Col>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">章節</Text>
                      <Select
                        allowClear
                        showSearch
                        style={{ width: '100%' }}
                        value={qaFilters.chapterCode}
                        options={Array.from(new Set((detail.items || []).map((item) => item.chapterCode).filter(Boolean))).map(
                          (value) => ({ label: value, value }),
                        )}
                        onChange={(value) => setQaFilters((previous) => ({ ...previous, chapterCode: value }))}
                      />
                    </Col>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">用途</Text>
                      <Input
                        allowClear
                        placeholder="storyline.cover / chapter.video"
                        value={qaFilters.usageTarget}
                        onChange={(event) => setQaFilters((previous) => ({ ...previous, usageTarget: event.target.value }))}
                      />
                    </Col>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">健康狀態</Text>
                      <Select
                        allowClear
                        style={{ width: '100%' }}
                        value={qaFilters.healthState}
                        options={healthSummaryOrder.map((value) => ({
                          label: healthLabelMap[value]?.label || value,
                          value,
                        }))}
                        onChange={(value) => {
                          setQaFilters((previous) => ({ ...previous, healthState: value }));
                          setAssetFilter((value as MaterialAssetFilter) || 'all');
                        }}
                      />
                    </Col>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">公開曝光</Text>
                      <Select
                        allowClear
                        style={{ width: '100%' }}
                        value={qaFilters.runtimeExposure}
                        options={[
                          { label: '公開 runtime', value: 'runtime' },
                          { label: '僅後台', value: 'admin_only' },
                        ]}
                        onChange={(value) => setQaFilters((previous) => ({ ...previous, runtimeExposure: value }))}
                      />
                    </Col>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">供應商</Text>
                      <Input
                        allowClear
                        placeholder="image-2 / bailian"
                        value={qaFilters.providerName}
                        onChange={(event) => setQaFilters((previous) => ({ ...previous, providerName: event.target.value }))}
                      />
                    </Col>
                    <Col xs={24} md={8} xl={6}>
                      <Text type="secondary">模型</Text>
                      <Input
                        allowClear
                        placeholder="gpt-image-2 / cosyvoice"
                        value={qaFilters.modelCode}
                        onChange={(event) => setQaFilters((previous) => ({ ...previous, modelCode: event.target.value }))}
                      />
                    </Col>
                  </Row>
                  <Space wrap>
                    <Button type="primary" loading={qaLoading} onClick={() => void refreshQaOnly()}>
                      套用 QA 篩選
                    </Button>
                    <Button
                      onClick={() => {
                        setQaFilters({});
                        setAssetFilter('all');
                        void refreshQaOnly({});
                      }}
                    >
                      清除 QA 篩選
                    </Button>
                    <Text type="secondary">篩選會套用到後端 QA 結果；下方表格仍保留素材需求位以方便追蹤。</Text>
                  </Space>
                </Space>
              </Card>

              <Row gutter={[16, 16]}>
                <Col xs={12} md={4}>
                  <Statistic title="素材" value={counters.materialCount} />
                </Col>
                <Col xs={12} md={4}>
                  <Statistic title="資產" value={counters.assetCount} />
                </Col>
                <Col xs={12} md={4}>
                  <Statistic title="故事物件" value={counters.storyObjectCount} />
                </Col>
                <Col xs={12} md={4}>
                  <Statistic title="章節" value={counters.chapterCount} />
                </Col>
                <Col xs={12} md={4}>
                  <Statistic title="探索元素" value={counters.explorationCount} />
                </Col>
                <Col xs={12} md={4}>
                  <Statistic title="故事線 ID" value={detail.storylineId || 0} />
                </Col>
              </Row>

              <Card size="small" title="素材可用性">
                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  <Alert
                    type="info"
                    showIcon
                    message="素材包行是素材需求位，不等於已可用資產"
                    description="不可預覽或已損毀的素材會保留為待處理項，不會計入可用資產。待生產、無公開連結或未發布的行會保留在素材包內，因為它們代表故事線仍需要補齊的資源；可用性篩選只改變檢視，不會刪除 manifest 任務位。"
                  />
                  <Row gutter={[16, 16]}>
                    {healthSummaryOrder.map((key) => (
                      <Col xs={12} md={6} xl={3} key={key}>
                        <Card
                          size="small"
                          className={
                            assetFilter === key
                              ? 'story-material-package__health-card story-material-package__health-card--active'
                              : 'story-material-package__health-card'
                          }
                          onClick={() => {
                            setAssetFilter(key as MaterialAssetFilter);
                            setQaFilters((previous) => ({ ...previous, healthState: key }));
                          }}
                        >
                          <Statistic
                            title={healthLabelMap[key]?.label || key}
                            value={assetHealthCounters[key as Exclude<MaterialAssetFilter, 'all'>] || 0}
                          />
                        </Card>
                      </Col>
                    ))}
                  </Row>
                  <Space wrap>
                    <Text strong>檢視：</Text>
                    <Select
                      value={assetFilter}
                      style={{ width: 220 }}
                      onChange={(value) => {
                        setAssetFilter(value);
                        setQaFilters((previous) => ({ ...previous, healthState: value === 'all' ? undefined : value }));
                      }}
                      options={[
                        { value: 'all', label: `全部需求位 (${detail.items?.length || 0})` },
                        { value: 'usable', label: `只看可用資產 (${assetHealthCounters.usable})` },
                        { value: 'planned_slot', label: `只看待生產 (${assetHealthCounters.planned_slot})` },
                        { value: 'missing_asset', label: `只看缺少資產 (${assetHealthCounters.missing_asset})` },
                        { value: 'no_public_url', label: `只看無公開連結 (${assetHealthCounters.no_public_url})` },
                        { value: 'unpublished', label: `只看未發布 (${assetHealthCounters.unpublished})` },
                        { value: 'stale_version', label: `只看版本過期 (${assetHealthCounters.stale_version})` },
                        { value: 'cos_unavailable', label: `只看 COS 不可用 (${assetHealthCounters.cos_unavailable})` },
                        { value: 'needs_regeneration', label: `只看需重生 (${assetHealthCounters.needs_regeneration})` },
                        { value: 'rejected', label: `只看已拒絕 (${assetHealthCounters.rejected})` },
                      ]}
                    />
                    <Text type="secondary">
                      目前顯示 {visibleMaterialItems.length} / {detail.items?.length || 0} 個素材需求位。
                    </Text>
                  </Space>
                </Space>
              </Card>

              <Row gutter={[16, 16]}>
                <Col xs={24} xl={12}>
                  <Card size="small" title="史實依據">
                    <Paragraph ellipsis={{ rows: 4, expandable: true, symbol: '展開' }}>
                      {detail.historicalBasisZht || detail.historicalBasisZh || '尚未填寫史實依據。'}
                    </Paragraph>
                  </Card>
                </Col>
                <Col xs={24} xl={12}>
                  <Card size="small" title="文學演繹">
                    <Paragraph ellipsis={{ rows: 4, expandable: true, symbol: '展開' }}>
                      {detail.literaryDramatizationZht || detail.literaryDramatizationZh || '尚未填寫文學演繹說明。'}
                    </Paragraph>
                  </Card>
                </Col>
              </Row>

              <Card size="small" title="存放位置與清單">
                <Row gutter={[16, 16]}>
                  <Col xs={24} lg={8}>
                    <Text type="secondary">本地素材根目錄</Text>
                    <PathCell value={detail.localRoot} />
                  </Col>
                  <Col xs={24} lg={8}>
                    <Text type="secondary">COS 前綴</Text>
                    <PathCell value={detail.cosPrefix} />
                  </Col>
                  <Col xs={24} lg={8}>
                    <Text type="secondary">Manifest</Text>
                    <PathCell value={detail.manifestPath} />
                  </Col>
                </Row>
              </Card>

              <Table
                rowKey="id"
                columns={columns}
                dataSource={visibleMaterialItems}
                scroll={{ x: 1500 }}
                pagination={{ pageSize: 12, showSizeChanger: true }}
                locale={{ emptyText: <Empty description="此素材包暫無項目，請檢查 seed 或後端資料。" /> }}
              />
            </Space>
          ) : (
            <Empty description="請先選擇一個故事素材包。" />
          )}
        </Card>
      </Space>
      <Modal
        open={preflightOpen}
        title="生產預檢"
        footer={<Button onClick={() => setPreflightOpen(false)}>關閉</Button>}
        onCancel={() => setPreflightOpen(false)}
        width={760}
      >
        {preflightResult ? (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="素材數量">{preflightResult.itemCount || 0}</Descriptions.Item>
              <Descriptions.Item label="目標資產類型">{(preflightResult.targetAssetKinds || []).join(', ') || '-'}</Descriptions.Item>
              <Descriptions.Item label="估算成本">{compactMoney(preflightResult.estimatedTotalCost)}</Descriptions.Item>
              <Descriptions.Item label="每日上限">{compactMoney(preflightResult.dailyCostCeiling)}</Descriptions.Item>
              <Descriptions.Item label="批次上限">{compactMoney(preflightResult.batchCostCeiling)}</Descriptions.Item>
              <Descriptions.Item label="需要超級管理員確認">
                {preflightResult.requiresSuperAdminConfirmation ? <Tag color="red">是</Tag> : <Tag color="green">否</Tag>}
              </Descriptions.Item>
            </Descriptions>
            <Alert
              type={preflightResult.requiresSuperAdminConfirmation ? 'warning' : 'success'}
              showIcon
              message="風險摘要"
              description={
                preflightResult.risks?.length
                  ? preflightResult.risks.map((risk) => risk.message || risk.riskCode).join('；')
                  : '目前未發現成本或批次風險。'
              }
            />
          </Space>
        ) : null}
      </Modal>

      <Modal
        open={importOpen}
        title="匯入本地素材"
        okText="匯入"
        confirmLoading={importLoading}
        onOk={() => void handleImportSubmit()}
        onCancel={() => setImportOpen(false)}
        width={760}
      >
        <Form form={importForm} layout="vertical">
          <Form.Item name="itemId" label="素材項目" rules={[{ required: true, message: '請選擇素材項目' }]}>
            <Select showSearch options={itemOptions} optionFilterProp="label" />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="relativeLocalPath" label="relativeLocalPath" rules={[{ required: true, message: '請填寫本地相對路徑' }]}>
                <Input placeholder="images/heroes/ch01-ama-coast.png" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="forcedCosObjectKey" label="forcedCosObjectKey">
                <Input placeholder="miniapp/assets/..." />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item name="providerName" label="providerName">
                <Input />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="modelCode" label="modelCode">
                <Input />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="estimatedCost" label="estimatedCost">
                <Input />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="verificationNote" label="驗證備註">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Alert
            type="info"
            showIcon
            message="目前已選資源"
            description={
              <Space direction="vertical" size={2}>
                <Text>路徑：{displayText(selectedImportItem?.localPath)}</Text>
                <Text>COS：{displayText(selectedImportItem?.cosObjectKey)}</Text>
                <Text>狀態：{selectedImportItem?.status ? statusTag(selectedImportItem.status) : '未設定'}</Text>
              </Space>
            }
          />
        </Form>
      </Modal>

      <Drawer
        open={narrationOpen}
        title="生成旁白"
        width={760}
        onClose={() => setNarrationOpen(false)}
        extra={
          <Space>
            <Button onClick={() => void handleVoicePreview()} loading={narrationLoading}>
              試聽
            </Button>
            <Button type="primary" onClick={() => void handleNarrationGenerate()} loading={narrationLoading}>
              建立任務並綁定候選
            </Button>
          </Space>
        }
      >
        <Alert
          type="warning"
          showIcon
          message="音頻閘口"
          description="manualImportRequired 或 retry_required 時不會自動發布；請重試或改用匯入本地素材。"
        />
        <Form form={narrationForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="itemId" label="音頻素材項目" rules={[{ required: true, message: '請選擇音頻素材項目' }]}>
            <Select showSearch optionFilterProp="label" options={itemOptions.filter((option) => {
              const row = (detail?.items || []).find((item) => item.id === option.value);
              return row?.assetKind === 'audio';
            })} />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="providerId" label="供應商 ID" rules={[{ required: true, message: '請填寫供應商 ID' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="modelCode" label="模型代碼" rules={[{ required: true, message: '請填寫模型代碼' }]}>
                <Input placeholder="cosyvoice-v2" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="voiceCode" label="音色" rules={[{ required: true, message: '請選擇音色' }]}>
                <Select
                  showSearch
                  optionFilterProp="label"
                  options={voiceOptions.map((voice) => ({
                    value: voice.voiceCode,
                    label: `${voice.displayName} / ${voice.voiceCode}`,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="languageCode" label="輸出語言">
                <Select
                  options={[
                    { value: 'zh', label: '普通話' },
                    { value: 'yue', label: '粵語' },
                    { value: 'en', label: '英文' },
                    { value: 'pt', label: '葡文' },
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="scriptText" label="旁白腳本">
            <Input.TextArea rows={8} />
          </Form.Item>
          {voicePreviewUrl ? <audio src={voicePreviewUrl} controls style={{ width: '100%' }} /> : null}
        </Form>
      </Drawer>

      <Drawer
        open={versionOpen}
        title={versionItem ? `QA 詳情：${versionItem.itemKey}` : 'QA 詳情'}
        width={960}
        onClose={() => setVersionOpen(false)}
        className="story-material-package__version-drawer"
      >
        {versionLoading ? (
          <Skeleton active />
        ) : (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions bordered size="small" column={2}>
              <Descriptions.Item label="目前版本">v{versionItem?.currentVersionNo || 0}</Descriptions.Item>
              <Descriptions.Item label="已發布版本">{versionItem?.publishedVersionId || '未發布'}</Descriptions.Item>
              <Descriptions.Item label="狀態">{statusTag(versionItem?.status)}</Descriptions.Item>
              <Descriptions.Item label="目前資產">{versionItem?.assetId || '無'}</Descriptions.Item>
            </Descriptions>
            <Card size="small" title="QA 發現與操作" loading={qaDetailLoading}>
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                {qaDetail?.findings?.length ? (
                  <Space direction="vertical" size={8} style={{ width: '100%' }}>
                    {qaDetail.findings.map((finding, index) => (
                      <Alert
                        key={`${finding.findingCode}-${index}`}
                        type={finding.severity === 'blocking' ? 'error' : finding.severity === 'warning' ? 'warning' : 'info'}
                        showIcon
                        message={
                          <Space wrap>
                            {qaFindingSeverityTag(finding.severity)}
                            <Text strong>{finding.findingCode}</Text>
                            <Text>{finding.messageZht}</Text>
                          </Space>
                        }
                        description={
                          <Space direction="vertical" size={2}>
                            <Text type="secondary">
                              預期：{displayText(finding.expectedValue)} / 實際：{displayText(finding.actualValue)}
                            </Text>
                            <Text type="secondary">建議：{displayText(finding.actionHintZht)}</Text>
                          </Space>
                        }
                      />
                    ))}
                  </Space>
                ) : (
                  <Alert type="success" showIcon message="目前沒有阻斷性 QA 發現。" />
                )}
                <Space wrap>
                  <Button danger onClick={() => openQaAction('reject')}>
                    拒絕版本
                  </Button>
                  <Button onClick={() => openQaAction('approve')}>批准版本</Button>
                  <Button type="primary" onClick={() => openQaAction('replace')}>
                    替換資產
                  </Button>
                </Space>
              </Space>
            </Card>
            <Table
              rowKey="id"
              dataSource={versions}
              pagination={false}
              scroll={{ x: 1400 }}
              locale={{ emptyText: '此素材尚未產生可回溯版本，請先匯入、生成或發布資產。' }}
              columns={[
                {
                  title: '版本',
                  dataIndex: 'versionNo',
                  width: 90,
                  render: (value, record) => (
                    <Space direction="vertical" size={2}>
                      <Text strong>v{value}</Text>
                      {record.id === versionItem?.currentVersionId ? <Tag color="blue">目前</Tag> : null}
                      {record.promotionStatus === 'published' ? <Tag color="green">已發布版本</Tag> : null}
                    </Space>
                  ),
                },
                {
                  title: '狀態',
                  dataIndex: 'promotionStatus',
                  width: 130,
                  render: (value) => statusTag(value),
                },
                {
                  title: '模型與成本',
                  width: 220,
                  render: (_, record) => (
                    <Space direction="vertical" size={2}>
                      <Text>{record.providerName || '-'}</Text>
                      <Text type="secondary">{record.modelCode || '-'}</Text>
                      <Text type="secondary">估算 {compactMoney(record.estimatedCost)} / 實際 {compactMoney(record.actualCost)}</Text>
                    </Space>
                  ),
                },
                {
                  title: '資產',
                  width: 380,
                  render: (_, record) => <VersionAssetPreview record={record} />,
                },
                {
                  title: '父級與裁切',
                  width: 240,
                  render: (_, record) => (
                    <Space direction="vertical" size={2}>
                      <Text>parentVersionId：{record.parentVersionId || '-'}</Text>
                      <Text>parentItem：{record.parentItemKey || '-'}</Text>
                      <Text ellipsis={{ tooltip: record.cropMetadataJson }}>cropRect：{record.cropMetadataJson || '-'}</Text>
                    </Space>
                  ),
                },
                {
                  title: '提示詞 / 腳本',
                  width: 280,
                  render: (_, record) => (
                    <Space direction="vertical" size={2}>
                      <Text ellipsis={{ tooltip: record.promptText }}>promptText：{record.promptText || '-'}</Text>
                      <Text ellipsis={{ tooltip: record.scriptText }}>scriptText：{record.scriptText || '-'}</Text>
                      <Text ellipsis={{ tooltip: record.subtitleMetadataJson }}>
                        外掛字幕資料：{record.subtitleMetadataJson || '-'}
                      </Text>
                    </Space>
                  ),
                },
                {
                  title: '操作',
                  fixed: 'right',
                  width: 180,
                  render: (_, record) => (
                    <Space direction="vertical" size={4}>
                      <Button size="small" type="link" onClick={() => versionItem && void handlePromote(versionItem, record)}>
                        發布到故事
                      </Button>
                      <Button size="small" onClick={() => openQaAction('approve', record)}>
                        批准版本
                      </Button>
                      <Button size="small" danger onClick={() => openQaAction('reject', record)}>
                        拒絕版本
                      </Button>
                      <Button size="small" onClick={() => openQaAction('replace', record)}>
                        替換資產
                      </Button>
                      <Button size="small" danger onClick={() => versionItem && void handleRollback(versionItem, record)}>
                        回滾版本
                      </Button>
                    </Space>
                  ),
                },
              ]}
            />
          </Space>
        )}
      </Drawer>

      <Drawer
        open={qaReportOpen}
        title="一致性檢查"
        width={920}
        onClose={() => setQaReportOpen(false)}
      >
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Row gutter={[16, 16]}>
            <Col span={8}>
              <Statistic title="阻斷" value={qaReport?.blockingCount || 0} valueStyle={{ color: '#cf1322' }} />
            </Col>
            <Col span={8}>
              <Statistic title="警告" value={qaReport?.warningCount || 0} valueStyle={{ color: '#d48806' }} />
            </Col>
            <Col span={8}>
              <Statistic title="資訊" value={qaReport?.infoCount || 0} valueStyle={{ color: '#1677ff' }} />
            </Col>
          </Row>
          <Table<StoryMaterialQaFinding>
            rowKey={qaFindingRowKey}
            dataSource={qaReport?.findings || []}
            pagination={{ pageSize: 8 }}
            scroll={{ x: 1000 }}
            columns={[
              {
                title: '級別',
                dataIndex: 'severity',
                width: 100,
                render: (value) => qaFindingSeverityTag(value),
              },
              {
                title: 'findingCode',
                dataIndex: 'findingCode',
                width: 210,
                render: (value) => <Text code>{value}</Text>,
              },
              {
                title: 'messageZht',
                dataIndex: 'messageZht',
                width: 260,
                render: (value) => <Text>{value}</Text>,
              },
              {
                title: 'expectedValue',
                dataIndex: 'expectedValue',
                width: 180,
                render: (value) => <PathCell value={value} />,
              },
              {
                title: 'actualValue',
                dataIndex: 'actualValue',
                width: 220,
                render: (value) => <PathCell value={value} />,
              },
              {
                title: 'actionHintZht',
                dataIndex: 'actionHintZht',
                width: 260,
                render: (value) => <Text>{value}</Text>,
              },
            ]}
          />
        </Space>
      </Drawer>

      <Modal
        open={qaActionOpen}
        title={
          qaActionType === 'reject'
            ? '拒絕版本'
            : qaActionType === 'replace'
              ? '替換資產'
              : '批准版本'
        }
        okText="確認"
        cancelText="取消"
        confirmLoading={qaActionLoading}
        onOk={() => void submitQaAction()}
        onCancel={() => setQaActionOpen(false)}
        width={760}
      >
        <Form form={qaActionForm} layout="vertical">
          <Form.Item name="versionId" label="版本 ID">
            <Input disabled />
          </Form.Item>
          {qaActionType === 'replace' ? (
            <MediaAssetPickerField
              name="replacementAssetId"
              label="替換資產"
              valueMode="asset-id"
              required
              uploadSource="material-qa-replace"
              help="請選擇已存在的 content_assets.id；系統會建立新版本並保留舊版本。"
            />
          ) : null}
          {qaActionType !== 'reject' ? (
            <Form.Item name="targetStatus" label="目標狀態">
              <Select
                options={[
                  { label: '已上傳', value: 'uploaded' },
                  { label: '已審批', value: 'approved' },
                  { label: '已發佈', value: 'published' },
                ]}
              />
            </Form.Item>
          ) : null}
          <Form.Item name="note" label="QA 備註" rules={[{ required: qaActionType === 'reject', message: '請填寫拒絕原因' }]}>
            <Input.TextArea rows={4} placeholder="說明拒絕、批准或替換的原因，方便日後追蹤。" />
          </Form.Item>
          <Form.Item name="confirmedImpact" label="是否已確認公開影響">
            <Select
              options={[
                { label: '否，僅保存普通 QA 操作', value: false },
                { label: '是，已確認會影響公開素材或發布狀態', value: true },
              ]}
            />
          </Form.Item>
          <Alert
            type="warning"
            showIcon
            message="公開影響確認"
            description="若目標狀態為已發佈，或目前素材已在故事 runtime 使用，後端會要求 confirmedImpact，部分操作還需要超級管理員角色。"
          />
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default StoryMaterialPackageManagement;
