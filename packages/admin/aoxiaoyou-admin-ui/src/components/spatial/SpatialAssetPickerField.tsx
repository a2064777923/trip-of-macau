import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Card,
  Empty,
  Form,
  Image,
  Select,
  Space,
  Typography,
  Upload,
  message,
} from 'antd';
import {
  FileImageOutlined,
  FileOutlined,
  InboxOutlined,
  PlayCircleOutlined,
  SoundOutlined,
} from '@ant-design/icons';
import type { UploadRequestOption } from 'rc-upload/lib/interface';
import { getAdminContentAssets, uploadAdminContentAsset } from '../../services/api';
import type { AdminContentAssetItem } from '../../types/admin';

const { Dragger } = Upload;
const { Link, Paragraph, Text } = Typography;
type NamePath = string | number | Array<string | number>;

interface SpatialAssetPickerFieldProps {
  name: NamePath;
  label: string;
  assetKind?: string;
  required?: boolean;
  allowClear?: boolean;
  placeholder?: string;
  help?: string;
}

function pickAssetLabel(asset: AdminContentAssetItem) {
  const objectKey = asset.objectKey || asset.canonicalUrl || `資源 #${asset.id}`;
  return `#${asset.id} · ${objectKey}`;
}

function inferAssetKindFromFile(file: File, fallback?: string) {
  if (fallback) {
    return fallback;
  }
  const mimeType = (file.type || '').toLowerCase();
  if (mimeType.startsWith('image/')) {
    return 'image';
  }
  if (mimeType.startsWith('audio/')) {
    return 'audio';
  }
  if (mimeType === 'application/json' || file.name.toLowerCase().endsWith('.json')) {
    return 'json';
  }
  return 'other';
}

function buildAccept(assetKind?: string) {
  switch (assetKind) {
    case 'image':
    case 'icon':
      return 'image/*';
    case 'audio':
      return 'audio/*';
    case 'json':
      return '.json,application/json';
    default:
      return undefined;
  }
}

function isImageAsset(asset?: AdminContentAssetItem | null) {
  return !!asset?.mimeType && asset.mimeType.toLowerCase().startsWith('image/');
}

function isAudioAsset(asset?: AdminContentAssetItem | null) {
  return !!asset?.mimeType && asset.mimeType.toLowerCase().startsWith('audio/');
}

function isVideoAsset(asset?: AdminContentAssetItem | null) {
  return !!asset?.mimeType && asset.mimeType.toLowerCase().startsWith('video/');
}

function renderAssetIcon(asset?: AdminContentAssetItem | null) {
  if (isImageAsset(asset)) {
    return <FileImageOutlined />;
  }
  if (isAudioAsset(asset)) {
    return <SoundOutlined />;
  }
  if (isVideoAsset(asset)) {
    return <PlayCircleOutlined />;
  }
  return <FileOutlined />;
}

const SpatialAssetPickerField: React.FC<SpatialAssetPickerFieldProps> = ({
  name,
  label,
  assetKind,
  required,
  allowClear = true,
  placeholder,
  help,
}) => {
  const form = Form.useFormInstance();
  const selectedAssetId = Form.useWatch(name, form) as number | null | undefined;
  const [assets, setAssets] = useState<AdminContentAssetItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    let active = true;
    const loadAssets = async () => {
      setLoading(true);
      try {
        const response = await getAdminContentAssets({
          pageNum: 1,
          pageSize: 200,
          assetKind,
        });
        if (active && response.success && response.data) {
          setAssets(response.data.list || []);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };
    void loadAssets();
    return () => {
      active = false;
    };
  }, [assetKind]);

  const selectedAsset = useMemo(
    () => assets.find((asset) => asset.id === selectedAssetId) || null,
    [assets, selectedAssetId],
  );

  const options = useMemo(
    () =>
      assets.map((asset) => ({
        label: pickAssetLabel(asset),
        value: asset.id,
      })),
    [assets],
  );

  const handleUploadedAsset = (asset: AdminContentAssetItem) => {
    setAssets((current) => {
      const next = current.filter((item) => item.id !== asset.id);
      next.unshift(asset);
      return next;
    });
    form.setFieldValue(name, asset.id);
    message.success(`已上傳並選取${label}`);
  };

  const uploadFile = async (file: File) => {
    setUploading(true);
    try {
      const response = await uploadAdminContentAsset({
        file,
        assetKind: inferAssetKindFromFile(file, assetKind),
        status: 'draft',
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || '資源上傳失敗');
      }
      handleUploadedAsset(response.data);
      return response.data;
    } finally {
      setUploading(false);
    }
  };

  const handleCustomRequest = async (options: UploadRequestOption) => {
    try {
      const file = options.file as File;
      const asset = await uploadFile(file);
      options.onSuccess?.(asset as never);
    } catch (error) {
      const uploadError = error instanceof Error ? error : new Error('資源上傳失敗');
      message.error(uploadError.message);
      options.onError?.(uploadError);
    }
  };

  const handlePaste = async (event: React.ClipboardEvent<HTMLDivElement>) => {
    const files = Array.from(event.clipboardData.files || []);
    if (!files.length) {
      Array.from(event.clipboardData.items || []).forEach((item) => {
        if (item.kind === 'file') {
          const file = item.getAsFile();
          if (file) {
            files.push(file);
          }
        }
      });
    }
    if (!files.length) {
      return;
    }
    event.preventDefault();
    try {
      await uploadFile(files[0]);
      message.info('已讀取剪貼簿中的檔案並完成上傳');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '讀取剪貼簿檔案失敗');
    }
  };

  return (
    <>
      <Form.Item
        name={name}
        label={label}
        extra={help}
        rules={required ? [{ required: true, message: `請選擇${label}` }] : undefined}
      >
        <Select
          allowClear={allowClear}
          showSearch
          loading={loading}
          placeholder={placeholder || `請選擇${label}`}
          optionFilterProp="label"
          options={options}
          optionRender={(option) => {
            const asset = assets.find((item) => item.id === option.data.value);
            return (
              <Space direction="vertical" size={2}>
                <Space size={8}>
                  {renderAssetIcon(asset)}
                  <Text>{option.data.label}</Text>
                </Space>
                {asset?.canonicalUrl ? (
                  <Link href={asset.canonicalUrl} target="_blank">
                    開啟資源
                  </Link>
                ) : null}
              </Space>
            );
          }}
        />
      </Form.Item>

      <Card
        size="small"
        title="目前資源"
        style={{ marginTop: -12, marginBottom: 12 }}
        bodyStyle={{ paddingTop: 12 }}
      >
        {selectedAsset ? (
          <Space direction="vertical" size={12} style={{ width: '100%' }}>
            <Space align="start" size={16} style={{ width: '100%', justifyContent: 'space-between' }}>
              <Space align="start" size={12}>
                {isImageAsset(selectedAsset) && selectedAsset.canonicalUrl ? (
                  <Image
                    src={selectedAsset.canonicalUrl}
                    width={96}
                    height={96}
                    style={{ borderRadius: 12, objectFit: 'cover' }}
                  />
                ) : null}
                {!isImageAsset(selectedAsset) ? (
                  <Card
                    size="small"
                    style={{
                      width: 96,
                      height: 96,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                    }}
                  >
                    <Space direction="vertical" align="center" size={8}>
                      <Text style={{ fontSize: 24 }}>{renderAssetIcon(selectedAsset)}</Text>
                      <Text type="secondary">{selectedAsset.assetKind || 'asset'}</Text>
                    </Space>
                  </Card>
                ) : null}
                <Space direction="vertical" size={4}>
                  <Text strong>{pickAssetLabel(selectedAsset)}</Text>
                  <Text type="secondary">類型：{selectedAsset.assetKind || '未標記'}</Text>
                  <Text type="secondary">MIME：{selectedAsset.mimeType || '未知'}</Text>
                  {selectedAsset.fileSizeBytes ? (
                    <Text type="secondary">大小：{Math.max(selectedAsset.fileSizeBytes / 1024, 0.1).toFixed(1)} KB</Text>
                  ) : null}
                  {selectedAsset.canonicalUrl ? (
                    <Link href={selectedAsset.canonicalUrl} target="_blank">
                      另開視窗檢視
                    </Link>
                  ) : null}
                </Space>
              </Space>
            </Space>
            {isAudioAsset(selectedAsset) && selectedAsset.canonicalUrl ? (
              <audio controls src={selectedAsset.canonicalUrl} style={{ width: '100%' }} />
            ) : null}
            {isVideoAsset(selectedAsset) && selectedAsset.canonicalUrl ? (
              <video controls src={selectedAsset.canonicalUrl} style={{ width: '100%', maxHeight: 220 }} />
            ) : null}
          </Space>
        ) : selectedAssetId ? (
          <Alert
            type="info"
            showIcon
            message={`目前已選資源 #${selectedAssetId}`}
            description="這份資源不在目前已載入的最近資源列表中，但仍會保留既有綁定。"
          />
        ) : (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="尚未選取資源" />
        )}
      </Card>

      <div onPaste={handlePaste} tabIndex={0} style={{ marginBottom: 24, outline: 'none' }}>
        <Dragger
          accept={buildAccept(assetKind)}
          customRequest={(options) => {
            void handleCustomRequest(options);
          }}
          disabled={uploading}
          maxCount={1}
          multiple={false}
          openFileDialogOnClick
          showUploadList={false}
        >
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">直接拖拽、點擊上傳，或聚焦後貼上剪貼簿檔案</p>
          <p className="ant-upload-hint">
            上傳成功後會自動選取新資源。若想替換成舊資源，仍可回到上方下拉框重新選擇。
          </p>
        </Dragger>
      </div>
    </>
  );
};

export default SpatialAssetPickerField;
