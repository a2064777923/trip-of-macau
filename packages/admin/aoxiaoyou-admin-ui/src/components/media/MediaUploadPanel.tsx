import React, { useMemo, useRef, useState } from 'react';
import {
  CloudUploadOutlined,
  FolderOpenOutlined,
  InboxOutlined,
  PaperClipOutlined,
  PictureOutlined,
  UploadOutlined,
} from '@ant-design/icons';
import { Alert, App as AntdApp, Button, Card, List, Select, Space, Tag, Typography } from 'antd';
import {
  batchUploadAdminContentAssets,
  uploadAdminContentAsset,
} from '../../services/api';
import type {
  AdminAssetUploadPayload,
  AdminContentAssetItem,
  SupportedLocale,
} from '../../types/admin';

const { Paragraph, Text } = Typography;

const ASSET_KIND_OPTIONS = [
  { value: '', label: '自動判定' },
  { value: 'image', label: '圖片' },
  { value: 'icon', label: '圖標' },
  { value: 'video', label: '影片' },
  { value: 'audio', label: '音訊' },
  { value: 'lottie', label: 'Lottie 動畫' },
  { value: 'json', label: 'JSON 檔' },
  { value: 'other', label: '其他檔案' },
];

const LOCALE_OPTIONS: Array<{ value: SupportedLocale | ''; label: string }> = [
  { value: '', label: '未指定語言' },
  { value: 'zh-Hant', label: '繁體中文' },
  { value: 'zh-Hans', label: '簡體中文' },
  { value: 'en', label: 'English' },
  { value: 'pt', label: 'Português' },
];

function inferAssetKind(file: File, fallback?: string) {
  if (fallback) {
    return fallback;
  }
  const mimeType = (file.type || '').toLowerCase();
  if (mimeType.startsWith('image/')) {
    return 'image';
  }
  if (mimeType.startsWith('video/')) {
    return 'video';
  }
  if (mimeType.startsWith('audio/')) {
    return 'audio';
  }
  // 讓後端自行辨識普通 JSON 與 Lottie JSON。
  if (mimeType.includes('json') || file.name.toLowerCase().endsWith('.json')) {
    return undefined;
  }
  return 'other';
}

const MediaUploadPanel: React.FC<{
  onUploaded?: (assets: AdminContentAssetItem[]) => void;
}> = ({ onUploaded }) => {
  const { message } = AntdApp.useApp();
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const folderInputRef = useRef<HTMLInputElement | null>(null);
  const [uploading, setUploading] = useState(false);
  const [assetKind, setAssetKind] = useState<string>('');
  const [localeCode, setLocaleCode] = useState<SupportedLocale | ''>('');
  const [status, setStatus] = useState<'draft' | 'published'>('draft');
  const [recentUploads, setRecentUploads] = useState<AdminContentAssetItem[]>([]);

  const helperTags = useMemo(
    () => [
      `預設狀態：${status === 'draft' ? '草稿' : '已發布'}`,
      assetKind ? `上傳類型：${assetKind}` : '上傳類型：自動判定',
      localeCode ? `語言：${localeCode}` : '語言：未指定',
    ],
    [assetKind, localeCode, status],
  );

  const handleUpload = async (
    files: File[],
    uploadSource: string,
    relativePaths?: string[],
  ) => {
    if (!files.length) {
      return;
    }
    setUploading(true);
    try {
      if (files.length === 1 && !relativePaths?.[0]) {
        const payload: AdminAssetUploadPayload = {
          file: files[0],
          assetKind: inferAssetKind(files[0], assetKind || undefined),
          localeCode: localeCode || undefined,
          status,
          uploadSource,
        };
        const response = await uploadAdminContentAsset(payload);
        if (!response.success || !response.data) {
          throw new Error(response.message || '上傳資源失敗');
        }
        setRecentUploads((current) => [response.data, ...current].slice(0, 8));
        onUploaded?.([response.data]);
        message.success(`已上傳 1 個資源：${response.data.originalFilename || response.data.objectKey || response.data.id}`);
        return;
      }

      const response = await batchUploadAdminContentAssets({
        files,
        assetKind: assetKind || undefined,
        localeCode: localeCode || undefined,
        status,
        uploadSource,
        clientRelativePaths: relativePaths,
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || '批量上傳失敗');
      }
      const uploadedItems = response.data.items || [];
      setRecentUploads((current) => [...uploadedItems, ...current].slice(0, 8));
      onUploaded?.(uploadedItems);
      if (response.data.failedCount) {
        message.warning(`已上傳 ${response.data.uploadedCount} 個資源，另有 ${response.data.failedCount} 個失敗`);
      } else {
        message.success(`已上傳 ${response.data.uploadedCount} 個資源`);
      }
    } finally {
      setUploading(false);
    }
  };

  const readFilesFromInput = async (
    fileList: FileList | null,
    uploadSource: string,
    includeRelativePath: boolean,
  ) => {
    if (!fileList || !fileList.length) {
      return;
    }
    const files = Array.from(fileList);
    const relativePaths = includeRelativePath
      ? files.map((file) => (file as File & { webkitRelativePath?: string }).webkitRelativePath || file.name)
      : undefined;
    await handleUpload(files, uploadSource, relativePaths);
  };

  return (
    <Card title="上傳工作區" extra={uploading ? <Tag color="processing">上傳中</Tag> : <Tag>待命</Tag>}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Alert
          type="info"
          showIcon
          message="支援四種匯入方式"
          description="可直接選檔、拖拽到框內、匯入整個資料夾，或把焦點停留在此區域後直接從剪貼簿貼上圖片或檔案。Lottie 第一版支援 JSON 動畫。"
        />

        <Space wrap>
          {helperTags.map((item) => (
            <Tag key={item}>{item}</Tag>
          ))}
        </Space>

        <Space wrap size="middle">
          <Select
            value={assetKind}
            onChange={setAssetKind}
            options={ASSET_KIND_OPTIONS}
            style={{ width: 180 }}
          />
          <Select
            value={localeCode}
            onChange={setLocaleCode}
            options={LOCALE_OPTIONS}
            style={{ width: 180 }}
          />
          <Select
            value={status}
            onChange={(value) => setStatus(value)}
            options={[
              { value: 'draft', label: '上傳為草稿' },
              { value: 'published', label: '上傳並發布' },
            ]}
            style={{ width: 180 }}
          />
        </Space>

        <div
          tabIndex={0}
          onDragOver={(event) => {
            event.preventDefault();
          }}
          onDrop={(event) => {
            event.preventDefault();
            const files = Array.from(event.dataTransfer.files || []);
            void handleUpload(files, 'drag-drop');
          }}
          onPaste={(event) => {
            const files = Array.from(event.clipboardData.files || []);
            if (!files.length) {
              return;
            }
            event.preventDefault();
            void handleUpload(files, 'clipboard');
          }}
          style={{
            border: '1px dashed #91a3ff',
            borderRadius: 16,
            padding: 24,
            background: '#f8faff',
            outline: 'none',
          }}
        >
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Space align="center" size={12}>
              <InboxOutlined style={{ fontSize: 28, color: '#5b66b5' }} />
              <div>
                <Text strong style={{ fontSize: 16 }}>
                  把檔案拖進這裡，或使用下方按鈕匯入
                </Text>
                <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                  焦點停留在此區域時，也可以直接從剪貼簿貼上圖片或檔案。
                </Paragraph>
              </div>
            </Space>

            <Space wrap>
              <Button
                type="primary"
                icon={<UploadOutlined />}
                loading={uploading}
                onClick={() => fileInputRef.current?.click()}
              >
                選擇檔案
              </Button>
              <Button
                icon={<FolderOpenOutlined />}
                loading={uploading}
                onClick={() => folderInputRef.current?.click()}
              >
                匯入資料夾
              </Button>
              <Tag icon={<PaperClipOutlined />}>支援拖拽 / 剪貼簿貼上</Tag>
              <Tag icon={<PictureOutlined />}>圖片 / 音訊 / 影片 / Lottie JSON / 其他</Tag>
            </Space>
          </Space>
        </div>

        <input
          ref={fileInputRef}
          type="file"
          multiple
          style={{ display: 'none' }}
          onChange={(event) => {
            void readFilesFromInput(event.target.files, 'picker', false);
            event.target.value = '';
          }}
        />
        <input
          ref={folderInputRef}
          type="file"
          multiple
          style={{ display: 'none' }}
          {...({ webkitdirectory: 'true', directory: 'true' } as React.InputHTMLAttributes<HTMLInputElement>)}
          onChange={(event) => {
            void readFilesFromInput(event.target.files, 'folder', true);
            event.target.value = '';
          }}
        />

        <div>
          <Text strong>最近上傳</Text>
          <List
            style={{ marginTop: 12 }}
            locale={{ emptyText: '尚未有新的上傳記錄' }}
            dataSource={recentUploads}
            renderItem={(item) => (
              <List.Item>
                <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                  <Space direction="vertical" size={2}>
                    <Text strong>{item.originalFilename || item.objectKey || `資源 #${item.id}`}</Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {(item.assetKind || 'asset').toUpperCase()} / {item.uploadSource || 'picker'} /{' '}
                      {item.processingPolicyCode || 'passthrough'}
                    </Text>
                  </Space>
                  {item.canonicalUrl ? (
                    <Button type="link" href={item.canonicalUrl} target="_blank" icon={<CloudUploadOutlined />}>
                      查看資源
                    </Button>
                  ) : null}
                </Space>
              </List.Item>
            )}
          />
        </div>
      </Space>
    </Card>
  );
};

export default MediaUploadPanel;
