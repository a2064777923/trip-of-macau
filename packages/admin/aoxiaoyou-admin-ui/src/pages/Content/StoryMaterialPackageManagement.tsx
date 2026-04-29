import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PageContainer } from '@ant-design/pro-components';
import {
  Alert,
  App as AntdApp,
  Button,
  Card,
  Col,
  Empty,
  Input,
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
  getStoryMaterialPackage,
  getStoryMaterialPackages,
} from '../../services/api';
import type {
  StoryMaterialPackageDetail,
  StoryMaterialPackageItem,
  StoryMaterialPackageSummary,
} from '../../services/api';
import './StoryMaterialPackageManagement.scss';

const { Paragraph, Text, Title } = Typography;

const statusLabelMap: Record<string, string> = {
  draft: '編輯中',
  planned: '已規劃',
  published: '已發佈',
  archived: '已封存',
  uploaded: '已上傳',
  generated: '已生成',
};

const statusColorMap: Record<string, string> = {
  draft: 'gold',
  planned: 'blue',
  published: 'green',
  archived: 'default',
  uploaded: 'cyan',
  generated: 'purple',
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
  const [filters, setFilters] = useState<FilterState>({
    keyword: '東西方文明的戰火與共生',
    packageStatus: undefined,
  });

  const counters = useMemo(() => deriveCounters(detail), [detail]);

  const selectedSummary = useMemo(
    () => packages.find((item) => item.id === selectedPackageId) || null,
    [packages, selectedPackageId],
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
        title: '來源',
        dataIndex: 'provenanceType',
        width: 140,
        render: (value: string) => <Text>{displayText(value, '未標記')}</Text>,
      },
    ],
    [],
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
    </PageContainer>
  );
};

export default StoryMaterialPackageManagement;
