import React from 'react';
import { Alert, Descriptions, Drawer, List, Skeleton, Space, Tag, Typography } from 'antd';
import type { AdminContentAssetItem, AdminContentAssetUsageSummary } from '../../types/admin';
import {
  MediaAssetMeta,
  MediaAssetPreview,
  isAudioAsset,
  isLottieAsset,
  isVideoAsset,
} from './MediaAssetPreview';

const { Paragraph, Text, Link } = Typography;

interface MediaAssetDetailDrawerProps {
  open: boolean;
  asset?: AdminContentAssetItem | null;
  usageSummary?: AdminContentAssetUsageSummary | null;
  usageLoading?: boolean;
  onClose: () => void;
}

const MediaAssetDetailDrawer: React.FC<MediaAssetDetailDrawerProps> = ({
  open,
  asset,
  usageSummary,
  usageLoading = false,
  onClose,
}) => {
  return (
    <Drawer title="資源詳情" open={open} width={640} onClose={onClose} destroyOnClose>
      {asset ? (
        <Space direction="vertical" size={20} style={{ width: '100%' }}>
          <Space align="start" size={16}>
            <MediaAssetPreview asset={asset} size={140} />
            <MediaAssetMeta asset={asset} />
          </Space>

          {isAudioAsset(asset) && asset.canonicalUrl ? (
            <audio controls src={asset.canonicalUrl} style={{ width: '100%' }} />
          ) : null}
          {isVideoAsset(asset) && asset.canonicalUrl ? (
            <video controls src={asset.canonicalUrl} style={{ width: '100%', maxHeight: 280 }} />
          ) : null}

          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="資源 ID">{asset.id}</Descriptions.Item>
            <Descriptions.Item label="原始檔名">{asset.originalFilename || '-'}</Descriptions.Item>
            <Descriptions.Item label="副檔名">{asset.fileExtension || '-'}</Descriptions.Item>
            <Descriptions.Item label="MIME">{asset.mimeType || '-'}</Descriptions.Item>
            <Descriptions.Item label="語言">{asset.localeCode || '-'}</Descriptions.Item>
            <Descriptions.Item label="上傳來源">{asset.uploadSource || '-'}</Descriptions.Item>
            <Descriptions.Item label="客戶端相對路徑">{asset.clientRelativePath || '-'}</Descriptions.Item>
            <Descriptions.Item label="上傳管理員">{asset.uploadedByAdminName || '-'}</Descriptions.Item>
            <Descriptions.Item label="處理策略">{asset.processingPolicyCode || '-'}</Descriptions.Item>
            <Descriptions.Item label="處理狀態">{asset.processingStatus || '-'}</Descriptions.Item>
            <Descriptions.Item label="處理說明">{asset.processingNote || '-'}</Descriptions.Item>
            <Descriptions.Item label="檔案大小">
              {asset.fileSizeBytes ? `${(asset.fileSizeBytes / 1024).toFixed(1)} KB` : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="尺寸">
              {asset.widthPx && asset.heightPx ? `${asset.widthPx} × ${asset.heightPx}` : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="發布狀態">{asset.status || '-'}</Descriptions.Item>
            <Descriptions.Item label="發布時間">{asset.publishedAt || '-'}</Descriptions.Item>
            <Descriptions.Item label="建立時間">{asset.createdAt || '-'}</Descriptions.Item>
            <Descriptions.Item label="更新時間">{asset.updatedAt || '-'}</Descriptions.Item>
            <Descriptions.Item label="COS 物件鍵">{asset.objectKey || '-'}</Descriptions.Item>
            <Descriptions.Item label="Lottie 子類型">
              {isLottieAsset(asset) ? asset.animationSubtype || 'lottie-json' : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Lottie 預設循環">
              {isLottieAsset(asset) ? (asset.defaultLoop ? '是' : '否') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Lottie 自動播放">
              {isLottieAsset(asset) ? (asset.defaultAutoplay ? '是' : '否') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Poster 資源 ID">
              {isLottieAsset(asset) ? asset.posterAssetId || '-' : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Fallback 資源 ID">
              {isLottieAsset(asset) ? asset.fallbackAssetId || '-' : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="資源連結">
              {asset.canonicalUrl ? (
                <Link href={asset.canonicalUrl} target="_blank">
                  在新視窗開啟
                </Link>
              ) : (
                '-'
              )}
            </Descriptions.Item>
          </Descriptions>

          <div>
            <Text strong>引用情況</Text>
            <div style={{ marginTop: 8 }}>
              {usageLoading ? (
                <Skeleton active paragraph={{ rows: 3 }} />
              ) : usageSummary ? (
                <>
                  {usageSummary.usageCount > 0 ? (
                    <Alert
                      type="warning"
                      showIcon
                      message={`這份資源目前被 ${usageSummary.usageCount} 個位置引用`}
                      description="刪除 API 會在資源仍被引用時直接阻止刪除，請先解除引用再進行清理。"
                      style={{ marginBottom: 12 }}
                    />
                  ) : (
                    <Alert
                      type="success"
                      showIcon
                      message="目前沒有任何引用"
                      description="這份資源暫時未被內容、故事線、地圖附件或其他欄位使用。"
                      style={{ marginBottom: 12 }}
                    />
                  )}
                  <List
                    size="small"
                    locale={{ emptyText: '尚未登錄任何引用' }}
                    dataSource={usageSummary.usages || []}
                    renderItem={(usage) => (
                      <List.Item>
                        <Space direction="vertical" size={4} style={{ width: '100%' }}>
                          <Space wrap size={[6, 6]}>
                            <Text strong>{usage.entityName || usage.entityCode || `#${usage.entityId ?? '-'}`}</Text>
                            {usage.entityType ? <Tag>{usage.entityType}</Tag> : null}
                            {usage.usageType ? <Tag color="blue">{usage.usageType}</Tag> : null}
                            {usage.relationType ? <Tag color="purple">{usage.relationType}</Tag> : null}
                            {usage.status ? (
                              <Tag color={usage.status === 'published' || usage.status === '1' ? 'green' : 'default'}>
                                {usage.status}
                              </Tag>
                            ) : null}
                          </Space>
                          <Text type="secondary">
                            欄位：{usage.fieldName || '-'}
                            {usage.entityCode ? ` / 代碼：${usage.entityCode}` : ''}
                            {usage.title ? ` / 標題：${usage.title}` : ''}
                          </Text>
                        </Space>
                      </List.Item>
                    )}
                  />
                </>
              ) : (
                <Text type="secondary">尚未載入引用資料。</Text>
              )}
            </div>
          </div>

          {asset.processingProfileJson ? (
            <div>
              <Text strong>處理設定快照</Text>
              <Paragraph
                code
                style={{
                  whiteSpace: 'pre-wrap',
                  marginTop: 8,
                  marginBottom: 0,
                  maxHeight: 260,
                  overflow: 'auto',
                }}
              >
                {asset.processingProfileJson}
              </Paragraph>
            </div>
          ) : null}
        </Space>
      ) : null}
    </Drawer>
  );
};

export default MediaAssetDetailDrawer;
