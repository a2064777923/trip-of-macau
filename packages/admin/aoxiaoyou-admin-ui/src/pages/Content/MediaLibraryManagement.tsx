import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import {
  Alert,
  Button,
  Card,
  Col,
  Empty,
  Form,
  Input,
  List,
  Row,
  Select,
  Space,
  Statistic,
  Tag,
  Typography,
} from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import type { AdminContentAssetItem, AdminContentAssetUsageSummary } from '../../types/admin';
import { getAdminContentAssets, getAdminContentAssetUsages } from '../../services/api';
import MediaAssetDetailDrawer from '../../components/media/MediaAssetDetailDrawer';
import MediaUploadPanel from '../../components/media/MediaUploadPanel';
import {
  MediaAssetMeta,
  MediaAssetPreview,
  assetTitle,
  isLottieAsset,
} from '../../components/media/MediaAssetPreview';

const { Link, Text } = Typography;

interface FilterState {
  keyword?: string;
  assetKind?: string;
  status?: string;
  uploadSource?: string;
  processingPolicyCode?: string;
  processingStatus?: string;
}

const MediaLibraryManagement: React.FC = () => {
  const [filterForm] = Form.useForm<FilterState>();
  const [assets, setAssets] = useState<AdminContentAssetItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedAsset, setSelectedAsset] = useState<AdminContentAssetItem | null>(null);
  const [usageSummary, setUsageSummary] = useState<AdminContentAssetUsageSummary | null>(null);
  const [usageLoading, setUsageLoading] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(12);
  const [total, setTotal] = useState(0);

  const loadAssets = async (nextPageNum = pageNum, nextPageSize = pageSize) => {
    setLoading(true);
    try {
      const values = filterForm.getFieldsValue();
      const response = await getAdminContentAssets({
        pageNum: nextPageNum,
        pageSize: nextPageSize,
        ...values,
      });
      if (response.success && response.data) {
        setAssets(response.data.list || []);
        setTotal(response.data.total || 0);
        setPageNum(nextPageNum);
        setPageSize(nextPageSize);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadAssets(1, 12);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    let active = true;
    const loadUsageSummary = async () => {
      if (!drawerOpen || !selectedAsset) {
        setUsageSummary(null);
        return;
      }
      setUsageLoading(true);
      try {
        const response = await getAdminContentAssetUsages(selectedAsset.id);
        if (active && response.success && response.data) {
          setUsageSummary(response.data);
        } else if (active) {
          setUsageSummary(null);
        }
      } finally {
        if (active) {
          setUsageLoading(false);
        }
      }
    };
    void loadUsageSummary();
    return () => {
      active = false;
    };
  }, [drawerOpen, selectedAsset]);

  const currentPageStats = useMemo(
    () => ({
      images: assets.filter((asset) => asset.assetKind === 'image' || asset.assetKind === 'icon').length,
      audio: assets.filter((asset) => asset.assetKind === 'audio').length,
      video: assets.filter((asset) => asset.assetKind === 'video').length,
      lottie: assets.filter((asset) => isLottieAsset(asset)).length,
      published: assets.filter((asset) => asset.status === 'published').length,
    }),
    [assets],
  );

  return (
    <PageContainer
      title="媒體資源中心"
      subTitle="集中管理上傳到 COS 的圖片、影片、音訊、Lottie 動畫與其他附件，提供搜尋、篩選、詳情與回填能力。"
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Alert
          type="info"
          showIcon
          message="中央媒體庫已接上真實後端"
          description="所有上傳都會經由後端寫入 COS，並根據管理員權限與系統策略決定是否壓縮處理。Lottie 第一版使用 JSON 動畫資產。"
        />

        <Row gutter={[24, 24]}>
          <Col xs={24} xl={12}>
            <MediaUploadPanel
              onUploaded={(items) => {
                const first = items[0];
                if (first) {
                  setSelectedAsset(first);
                  setDrawerOpen(true);
                }
                void loadAssets(1, pageSize);
              }}
            />
          </Col>
          <Col xs={24} xl={12}>
            <Card title="目前篩選結果摘要" extra={<Button icon={<ReloadOutlined />} onClick={() => void loadAssets()} />}>
              <Row gutter={[16, 16]}>
                <Col span={12}>
                  <Statistic title="符合條件資源總數" value={total} />
                </Col>
                <Col span={12}>
                  <Statistic title="本頁已發布" value={currentPageStats.published} />
                </Col>
                <Col span={12}>
                  <Statistic title="本頁圖片 / 圖標" value={currentPageStats.images} />
                </Col>
                <Col span={6}>
                  <Statistic title="音訊" value={currentPageStats.audio} />
                </Col>
                <Col span={6}>
                  <Statistic title="影片" value={currentPageStats.video} />
                </Col>
                <Col span={12}>
                  <Statistic title="Lottie 動畫" value={currentPageStats.lottie} />
                </Col>
              </Row>
            </Card>
          </Col>
        </Row>

        <Card title="搜尋與篩選">
          <Form
            form={filterForm}
            layout="vertical"
            onFinish={() => {
              void loadAssets(1, pageSize);
            }}
          >
            <Row gutter={[16, 16]}>
              <Col xs={24} md={8}>
                <Form.Item name="keyword" label="關鍵字">
                  <Input placeholder="檔名、路徑、物件鍵、上傳者" />
                </Form.Item>
              </Col>
              <Col xs={12} md={4}>
                <Form.Item name="assetKind" label="資源類型">
                  <Select
                    allowClear
                    options={[
                      { value: 'image', label: '圖片' },
                      { value: 'icon', label: '圖標' },
                      { value: 'video', label: '影片' },
                      { value: 'audio', label: '音訊' },
                      { value: 'lottie', label: 'Lottie' },
                      { value: 'json', label: 'JSON' },
                      { value: 'other', label: '其他' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col xs={12} md={4}>
                <Form.Item name="status" label="發布狀態">
                  <Select
                    allowClear
                    options={[
                      { value: 'draft', label: '草稿' },
                      { value: 'published', label: '已發布' },
                      { value: 'archived', label: '已封存' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col xs={12} md={4}>
                <Form.Item name="uploadSource" label="上傳來源">
                  <Select
                    allowClear
                    options={[
                      { value: 'picker', label: '選擇檔案' },
                      { value: 'drag-drop', label: '拖拽上傳' },
                      { value: 'folder', label: '資料夾匯入' },
                      { value: 'clipboard', label: '剪貼簿貼上' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col xs={12} md={4}>
                <Form.Item name="processingPolicyCode" label="處理策略">
                  <Select
                    allowClear
                    options={[
                      { value: 'lossless', label: '無損' },
                      { value: 'image-compressed', label: '圖片壓縮' },
                      { value: 'passthrough', label: '原檔直傳' },
                    ]}
                  />
                </Form.Item>
              </Col>
              <Col xs={12} md={4}>
                <Form.Item name="processingStatus" label="處理狀態">
                  <Select
                    allowClear
                    options={[
                      { value: 'processed', label: '已處理' },
                      { value: 'stored', label: '已存檔' },
                      { value: 'passthrough', label: '直接保留' },
                    ]}
                  />
                </Form.Item>
              </Col>
            </Row>
            <Space>
              <Button type="primary" htmlType="submit">
                重新查詢
              </Button>
              <Button
                onClick={() => {
                  filterForm.resetFields();
                  void loadAssets(1, pageSize);
                }}
              >
                清除條件
              </Button>
            </Space>
          </Form>
        </Card>

        <Card title="資源清單" extra={<Text type="secondary">點擊卡片可查看詳情與引用情況</Text>}>
          <List
            loading={loading}
            dataSource={assets}
            locale={{ emptyText: <Empty description="尚未找到符合條件的資源" /> }}
            grid={{ gutter: 16, xs: 1, sm: 2, lg: 3, xl: 4 }}
            pagination={{
              current: pageNum,
              pageSize,
              total,
              onChange: (nextPage, nextPageSize) => {
                void loadAssets(nextPage, nextPageSize);
              },
              showSizeChanger: true,
            }}
            renderItem={(asset) => (
              <List.Item>
                <Card
                  hoverable
                  onClick={() => {
                    setSelectedAsset(asset);
                    setDrawerOpen(true);
                  }}
                  styles={{ body: { padding: 16 } }}
                  actions={[
                    <Button
                      key="detail"
                      type="link"
                      onClick={(event) => {
                        event.stopPropagation();
                        setSelectedAsset(asset);
                        setDrawerOpen(true);
                      }}
                    >
                      詳情
                    </Button>,
                    asset.canonicalUrl ? (
                      <Link
                        key="open"
                        href={asset.canonicalUrl}
                        target="_blank"
                        onClick={(event) => event.stopPropagation()}
                      >
                        開啟
                      </Link>
                    ) : (
                      <Text key="disabled" type="secondary">
                        無連結
                      </Text>
                    ),
                  ]}
                >
                  <Space direction="vertical" size={12} style={{ width: '100%' }}>
                    <MediaAssetPreview asset={asset} size={168} />
                    <MediaAssetMeta asset={asset} />
                    <Space wrap size={[4, 4]}>
                      {asset.status ? (
                        <Tag color={asset.status === 'published' ? 'success' : 'default'}>{asset.status}</Tag>
                      ) : null}
                      {asset.uploadedByAdminName ? <Tag color="cyan">{asset.uploadedByAdminName}</Tag> : null}
                    </Space>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {asset.fileSizeBytes ? `${(asset.fileSizeBytes / 1024).toFixed(1)} KB` : '大小未提供'}
                    </Text>
                    <Text type="secondary" style={{ fontSize: 12 }} ellipsis>
                      {assetTitle(asset)}
                    </Text>
                  </Space>
                </Card>
              </List.Item>
            )}
          />
        </Card>
      </Space>

      <MediaAssetDetailDrawer
        open={drawerOpen}
        asset={selectedAsset}
        usageSummary={usageSummary}
        usageLoading={usageLoading}
        onClose={() => setDrawerOpen(false)}
      />
    </PageContainer>
  );
};

export default MediaLibraryManagement;
