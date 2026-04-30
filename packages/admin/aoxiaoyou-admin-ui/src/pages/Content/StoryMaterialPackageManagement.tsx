import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
  getStoryMaterialItemVersions,
  getStoryMaterialPackage,
  getStoryMaterialPackages,
  importStoryMaterialItemAsset,
  previewAiVoice,
  previewStoryMaterialProduction,
  promoteStoryMaterialItem,
  refreshAiGenerationJob,
  rollbackStoryMaterialItemVersion,
} from '../../services/api';
import type {
  AiGenerationJobItem,
  AiVoiceItem,
  StoryMaterialPackageDetail,
  StoryMaterialPackageItem,
  StoryMaterialPackageSummary,
} from '../../services/api';
import type {
  StoryMaterialProductionPreflightResponse,
  StoryMaterialVersionRecord,
} from '../../types/admin';
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
};

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
  const [preflightForm] = Form.useForm();
  const [importForm] = Form.useForm<ImportFormValues>();
  const [narrationForm] = Form.useForm<NarrationFormValues>();
  const watchedImportItemId = Form.useWatch('itemId', importForm);
  const [filters, setFilters] = useState<FilterState>({
    keyword: '東西方文明的戰火與共生',
    packageStatus: undefined,
  });

  const counters = useMemo(() => deriveCounters(detail), [detail]);

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

  const loadDetail = async (packageId: number) => {
    setSelectedPackageId(packageId);
    setLoadingDetail(true);
    try {
      const response = await getStoryMaterialPackage(packageId);
      if (!response.success || !response.data) {
        throw new Error(response.message || '讀取故事素材包詳情失敗');
      }
      setDetail(response.data);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '讀取故事素材包詳情失敗');
      setDetail(null);
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
    try {
      const response = await getStoryMaterialItemVersions(detail.id, record.id);
      if (!response.success) {
        throw new Error(response.message || '讀取版本歷史失敗');
      }
      setVersions(response.data || []);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '讀取版本歷史失敗');
      setVersions([]);
    } finally {
      setVersionLoading(false);
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
        width: 110,
        render: (value: number) => (value ? <Text code>{value}</Text> : <Text type="secondary">無</Text>),
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
        width: 130,
        render: (_value, record) => (
          <Space direction="vertical" size={2}>
            <Text>目前 v{record.currentVersionNo || 0}</Text>
            {record.publishedVersionId ? <Text type="secondary">已發布 #{record.publishedVersionId}</Text> : <Text type="secondary">未發布</Text>}
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
              查看版本
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
    [detail, importForm, message, versionItem],
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
                dataSource={detail.items || []}
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
        title={versionItem ? `查看版本：${versionItem.itemKey}` : '查看版本'}
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
            <Table
              rowKey="id"
              dataSource={versions}
              pagination={false}
              scroll={{ x: 1400 }}
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
                  width: 280,
                  render: (_, record) => (
                    <Space direction="vertical" size={2}>
                      <Text>{record.assetKind || '-'}</Text>
                      <PathCell value={record.canonicalUrl || record.cosObjectKey || record.localPath} />
                      {record.posterFallbackItemKey ? <Text type="secondary">fallback：{record.posterFallbackItemKey}</Text> : null}
                    </Space>
                  ),
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
                      <Text ellipsis={{ tooltip: record.subtitleMetadataJson }}>subtitleMetadata：{record.subtitleMetadataJson || '-'}</Text>
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
    </PageContainer>
  );
};

export default StoryMaterialPackageManagement;
