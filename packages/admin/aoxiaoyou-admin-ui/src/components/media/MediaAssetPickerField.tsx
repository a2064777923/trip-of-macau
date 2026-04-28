import React, { useEffect, useMemo, useState } from 'react';
import { FileOutlined } from '@ant-design/icons';
import { Alert, App as AntdApp, Button, Card, Empty, Form, Select, Space, Typography, Upload } from 'antd';
import type { NamePath } from 'antd/es/form/interface';
import type { UploadRequestOption } from 'rc-upload/lib/interface';
import AiCreativeWorkbenchModal from '../ai/AiCreativeWorkbenchModal';
import { getAdminContentAssets, uploadAdminContentAsset } from '../../services/api';
import type { AdminContentAssetItem } from '../../types/admin';
import { MediaAssetMeta, MediaAssetPreview, assetTitle, isAudioAsset, isVideoAsset } from './MediaAssetPreview';

const { Dragger } = Upload;
const { Link, Text } = Typography;

type PickerValueMode = 'asset-id' | 'url';
type PickerValue = number | string | Array<number | string> | null | undefined;

export interface MediaAssetPickerFieldProps {
  name: NamePath;
  label: string;
  assetKind?: string;
  required?: boolean;
  allowClear?: boolean;
  placeholder?: string;
  help?: string;
  valueMode?: PickerValueMode;
  uploadSource?: string;
  multiple?: boolean;
  onValueChange?: (value: PickerValue) => void;
  defaultCapabilityCode?: string;
  defaultGenerationType?: 'text' | 'image' | 'tts';
  defaultPromptTitle?: string;
  defaultPromptText?: string;
  defaultSourceScope?: string;
  defaultSourceScopeId?: number;
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
  if (mimeType.startsWith('video/')) {
    return 'video';
  }
  if (mimeType === 'application/json' || file.name.toLowerCase().endsWith('.json')) {
    return undefined;
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
    case 'video':
      return 'video/*';
    case 'lottie':
    case 'json':
      return '.json,application/json';
    default:
      return undefined;
  }
}

function normalizePickerValues(value: PickerValue) {
  if (Array.isArray(value)) {
    return value.filter((item): item is number | string => item !== null && item !== undefined && item !== '');
  }
  if (value === null || value === undefined || value === '') {
    return [];
  }
  return [value];
}

function toOptionValue(asset: AdminContentAssetItem, valueMode: PickerValueMode) {
  return valueMode === 'asset-id' ? asset.id : asset.canonicalUrl;
}

function mergeAssets(currentAssets: AdminContentAssetItem[], incomingAssets: AdminContentAssetItem[]) {
  const merged = new Map<number, AdminContentAssetItem>();
  incomingAssets.forEach((asset) => merged.set(asset.id, asset));
  currentAssets.forEach((asset) => {
    if (!merged.has(asset.id)) {
      merged.set(asset.id, asset);
    }
  });
  return Array.from(merged.values()).sort((left, right) => right.id - left.id);
}

const assetRowStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'flex-start',
  gap: 12,
  minWidth: 0,
};

const compactMetaContainerStyle: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  gap: 4,
  minWidth: 0,
  flex: 1,
};

const MediaAssetPickerField: React.FC<MediaAssetPickerFieldProps> = ({
  name,
  label,
  assetKind,
  required,
  allowClear = true,
  placeholder,
  help,
  valueMode = 'asset-id',
  uploadSource = 'picker',
  multiple = false,
  onValueChange,
  defaultCapabilityCode,
  defaultGenerationType,
  defaultPromptTitle,
  defaultPromptText,
  defaultSourceScope,
  defaultSourceScopeId,
}) => {
  const { message } = AntdApp.useApp();
  const form = Form.useFormInstance();
  const selectedValue = Form.useWatch(name, form) as PickerValue;
  const [assets, setAssets] = useState<AdminContentAssetItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [aiWorkbenchOpen, setAiWorkbenchOpen] = useState(false);

  useEffect(() => {
    let active = true;

    const loadAssets = async () => {
      setLoading(true);
      try {
        const response = await getAdminContentAssets({
          pageNum: 1,
          pageSize: 2000,
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

  const selectedValues = useMemo(
    () => normalizePickerValues(selectedValue).map((item) => String(item)),
    [selectedValue],
  );

  useEffect(() => {
    if (valueMode !== 'asset-id' || !selectedValues.length) {
      return;
    }

    const missingIds = selectedValues
      .filter((value) => !assets.some((asset) => String(asset.id) === value))
      .map((value) => Number(value))
      .filter((value) => Number.isFinite(value));

    if (!missingIds.length) {
      return;
    }

    let active = true;

    const loadMissingAssets = async () => {
      const responses = await Promise.all(
        missingIds.map((assetId) =>
          getAdminContentAssets({
            pageNum: 1,
            pageSize: 10,
            keyword: String(assetId),
          }),
        ),
      );

      if (!active) {
        return;
      }

      const loadedAssets = responses
        .flatMap((response) => response.data?.list || [])
        .filter((asset) => missingIds.includes(asset.id));

      if (loadedAssets.length) {
        setAssets((currentAssets) => mergeAssets(currentAssets, loadedAssets));
      }
    };

    void loadMissingAssets();
    return () => {
      active = false;
    };
  }, [assets, selectedValues, valueMode]);

  const selectedAssets = useMemo(
    () =>
      selectedValues
        .map((selectedKey) => {
          if (valueMode === 'asset-id') {
            return assets.find((asset) => String(asset.id) === selectedKey);
          }
          return assets.find((asset) => asset.canonicalUrl === selectedKey);
        })
        .filter((asset): asset is AdminContentAssetItem => !!asset),
    [assets, selectedValues, valueMode],
  );

  const missingValues = useMemo(() => {
    if (!selectedValues.length) {
      return [];
    }
    const selectedKeySet = new Set(
      selectedAssets
        .map((asset) => toOptionValue(asset, valueMode))
        .filter((value): value is number | string => value !== null && value !== undefined)
        .map((value) => String(value)),
    );
    return selectedValues.filter((value) => !selectedKeySet.has(value));
  }, [selectedAssets, selectedValues, valueMode]);

  const showAiWorkbench = useMemo(
    () => valueMode === 'asset-id' && ['image', 'icon', 'audio'].includes(assetKind || ''),
    [assetKind, valueMode],
  );

  const options = useMemo(
    () =>
      assets
        .filter((asset) => valueMode === 'asset-id' || !!asset.canonicalUrl)
        .map((asset) => ({
          label: `#${asset.id} | ${assetTitle(asset)}`,
          value: toOptionValue(asset, valueMode)!,
          searchText: `${asset.id} ${assetTitle(asset)} ${asset.originalFilename || ''} ${asset.objectKey || ''}`,
        })),
    [assets, valueMode],
  );

  const updateFieldValue = (nextValues: Array<number | string>) => {
    const deduped = Array.from(new Set(nextValues.map((item) => String(item))))
      .map((item) => (valueMode === 'asset-id' ? Number(item) : item))
      .filter((item) => item !== null && item !== undefined && item !== '');

    const nextValue: PickerValue = multiple ? deduped : deduped[0] ?? undefined;
    form.setFieldValue(name, nextValue);
    void form.validateFields([name]).catch(() => undefined);
    onValueChange?.(nextValue);
  };

  const commitSelectedAsset = (asset: AdminContentAssetItem) => {
    setAssets((currentAssets) => mergeAssets(currentAssets, [asset]));
    const nextValue = toOptionValue(asset, valueMode);
    if (nextValue === null || nextValue === undefined || nextValue === '') {
      throw new Error('資源缺少可回填值，暫時無法套用。');
    }

    if (multiple) {
      const currentValue = form.getFieldValue(name) as PickerValue;
      updateFieldValue([...normalizePickerValues(currentValue), nextValue]);
      message.success(`已把資源加入「${label}」`);
      return;
    }

    updateFieldValue([nextValue]);
    message.success(`已選擇「${label}」`);
  };

  const uploadFile = async (file: File) => {
    setUploading(true);
    try {
      const response = await uploadAdminContentAsset({
        file,
        assetKind: inferAssetKindFromFile(file, assetKind),
        status: 'draft',
        uploadSource,
        clientRelativePath: (file as File & { webkitRelativePath?: string }).webkitRelativePath || undefined,
      });

      if (!response.success || !response.data) {
        throw new Error(response.message || '資源上傳失敗');
      }

      commitSelectedAsset(response.data);
      return response.data;
    } finally {
      setUploading(false);
    }
  };

  const handleCustomRequest = async (options: UploadRequestOption) => {
    try {
      const asset = await uploadFile(options.file as File);
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
      return;
    }

    event.preventDefault();

    try {
      const uploadTargets = multiple ? files : [files[0]];
      for (const file of uploadTargets) {
        await uploadFile(file);
      }
      message.success(multiple ? '已把剪貼簿內容加入圖集資源' : '已把剪貼簿內容加入資源');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '從剪貼簿上傳失敗');
    }
  };

  const handleWorkbenchFinalized = async (assetId?: number) => {
    if (!assetId) {
      return;
    }
    try {
      const response = await getAdminContentAssets({
        pageNum: 1,
        pageSize: 10,
        keyword: String(assetId),
      });
      const asset = response.data?.list?.find((item) => item.id === assetId);
      if (asset) {
        commitSelectedAsset(asset);
        return;
      }
    } catch {
      // Ignore and fall back to setting the id directly.
    }

    if (multiple) {
      const currentValue = form.getFieldValue(name) as PickerValue;
      updateFieldValue([...normalizePickerValues(currentValue), assetId]);
      return;
    }
    updateFieldValue([assetId]);
  };

  return (
    <>
      <Form.Item
        name={name}
        label={label}
        extra={help}
        rules={
          required
            ? [
                {
                  validator: async (_, value) => {
                    if (normalizePickerValues(value).length > 0) {
                      return;
                    }
                    throw new Error(`請選擇${label}`);
                  },
                },
              ]
            : undefined
        }
      >
        <Select
          allowClear={allowClear}
          showSearch
          loading={loading}
          mode={multiple ? 'multiple' : undefined}
          maxTagCount={multiple ? 'responsive' : undefined}
          placeholder={placeholder || `請選擇${label}`}
          optionFilterProp="searchText"
          options={options}
          onChange={(value) => {
            if (multiple) {
              updateFieldValue(Array.isArray(value) ? value : []);
              return;
            }
            updateFieldValue(value == null ? [] : [value]);
          }}
          onClear={() => updateFieldValue([])}
        />
      </Form.Item>

      {showAiWorkbench ? (
        <Space style={{ marginBottom: 12 }}>
          <Button onClick={() => setAiWorkbenchOpen(true)}>AI 創作工作台</Button>
          <Text type="secondary">可直接生成並回填到目前資源欄位。</Text>
        </Space>
      ) : null}

      <Card
        size="small"
        title={multiple ? '目前已選圖集資源' : '目前已選資源'}
        style={{ marginTop: -12, marginBottom: 12 }}
        styles={{ body: { paddingTop: 12, overflow: 'hidden' } }}
      >
        {selectedAssets.length ? (
          multiple ? (
            <Space wrap size={12} style={{ width: '100%' }}>
              {selectedAssets.map((asset) => (
                <Card
                  key={asset.id}
                  size="small"
                  style={{ width: 320, overflow: 'hidden' }}
                  styles={{ body: { overflow: 'hidden' } }}
                >
                  <div style={assetRowStyle}>
                    <MediaAssetPreview asset={asset} size={72} />
                    <div style={compactMetaContainerStyle}>
                      <MediaAssetMeta asset={asset} />
                      {asset.canonicalUrl ? (
                        <Link href={asset.canonicalUrl} target="_blank">
                          開啟資源
                        </Link>
                      ) : null}
                    </div>
                  </div>
                </Card>
              ))}
            </Space>
          ) : (
            <Space direction="vertical" size={12} style={{ width: '100%', minWidth: 0 }}>
              <div style={{ ...assetRowStyle, gap: 16, width: '100%' }}>
                <MediaAssetPreview asset={selectedAssets[0]} size={96} />
                <div style={compactMetaContainerStyle}>
                  <MediaAssetMeta asset={selectedAssets[0]} />
                  {selectedAssets[0].canonicalUrl ? (
                    <Link href={selectedAssets[0].canonicalUrl} target="_blank">
                      開啟資源
                    </Link>
                  ) : null}
                </div>
              </div>
              {isAudioAsset(selectedAssets[0]) && selectedAssets[0].canonicalUrl ? (
                <audio controls src={selectedAssets[0].canonicalUrl} style={{ width: '100%' }} />
              ) : null}
              {isVideoAsset(selectedAssets[0]) && selectedAssets[0].canonicalUrl ? (
                <video controls src={selectedAssets[0].canonicalUrl} style={{ width: '100%', maxHeight: 220 }} />
              ) : null}
            </Space>
          )
        ) : missingValues.length ? (
          <Space direction="vertical" size={8} style={{ width: '100%' }}>
            {missingValues.map((value) => (
              <Card key={value} size="small">
                <Space align="start" size={12}>
                  <Text style={{ fontSize: 24, lineHeight: 1 }}>
                    <FileOutlined />
                  </Text>
                  <Space direction="vertical" size={4}>
                    <Text strong>{`已選資源編號：${value}`}</Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      目前仍保留這個選擇，但暫時沒有在已載入清單中找到。你可以重新搜尋、直接上傳，或稍後再替換。
                    </Text>
                  </Space>
                </Space>
              </Card>
            ))}
          </Space>
        ) : (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="尚未選擇資源" />
        )}
      </Card>

      <Card
        size="small"
        title={multiple ? '快速上傳圖集' : '快速上傳'}
        style={{ marginTop: -4, marginBottom: 12 }}
        styles={{ body: { paddingTop: 12 } }}
      >
        <div onPaste={handlePaste}>
          <Dragger
            multiple={multiple}
            directory={multiple}
            accept={buildAccept(assetKind)}
            customRequest={handleCustomRequest}
            showUploadList={false}
            disabled={uploading}
          >
            <Space direction="vertical" size={4}>
              <Text strong>
                {uploading
                  ? '上傳中...'
                  : multiple
                    ? `點擊、拖拽或貼上本地檔案，即可直接加入${label}`
                    : `點擊、拖拽或貼上本地檔案，即可直接建立${label}`}
              </Text>
              <Text type="secondary">
                支援本地檔案、拖拽與剪貼簿貼上；上傳完成後會自動回填到目前欄位。
              </Text>
            </Space>
          </Dragger>
        </div>
      </Card>

      {multiple ? (
        <Alert
          type="info"
          showIcon
          message="圖集欄位支援一次綁定多個附件"
          description="你可以拖入多個檔案、批量選擇或貼上多張圖片；提交時會保留目前排序。"
        />
      ) : null}

      <AiCreativeWorkbenchModal
        open={aiWorkbenchOpen}
        onClose={() => setAiWorkbenchOpen(false)}
        defaultCapabilityCode={
          defaultCapabilityCode || (assetKind === 'audio' ? 'admin_tts_generation' : 'admin_image_generation')
        }
        defaultGenerationType={
          defaultGenerationType ||
          (assetKind === 'audio' ? 'tts' : assetKind === 'image' || assetKind === 'icon' ? 'image' : 'text')
        }
        defaultPromptTitle={defaultPromptTitle || label}
        defaultPromptText={defaultPromptText}
        defaultSourceScope={defaultSourceScope}
        defaultSourceScopeId={defaultSourceScopeId}
        defaultAssetKind={assetKind}
        onFinalized={({ assetId }) => {
          void handleWorkbenchFinalized(assetId);
          setAiWorkbenchOpen(false);
        }}
      />
    </>
  );
};

export default MediaAssetPickerField;
