import React, { useEffect, useState } from 'react';
import {
  FileImageOutlined,
  FileOutlined,
  PlayCircleOutlined,
  SoundOutlined,
} from '@ant-design/icons';
import { Card, Image, Space, Tag, Typography } from 'antd';
import type { AdminContentAssetItem } from '../../types/admin';

const { Text } = Typography;

function assetLocation(asset?: AdminContentAssetItem | null) {
  return [
    asset?.mimeType,
    asset?.assetKind,
    asset?.fileExtension,
    asset?.originalFilename,
    asset?.canonicalUrl,
    asset?.objectKey,
    asset?.clientRelativePath,
  ]
    .filter(Boolean)
    .join(' ')
    .toLowerCase();
}

export function isImageAsset(asset?: AdminContentAssetItem | null) {
  const value = assetLocation(asset);
  return (
    value.includes('image/') ||
    asset?.assetKind === 'image' ||
    asset?.assetKind === 'icon' ||
    /\.(png|jpe?g|webp|gif|svg)(\?|#|$)/i.test(value)
  );
}

export function isAudioAsset(asset?: AdminContentAssetItem | null) {
  const value = assetLocation(asset);
  return value.includes('audio/') || asset?.assetKind === 'audio' || /\.(mp3|wav|m4a|aac|ogg)(\?|#|$)/i.test(value);
}

export function isVideoAsset(asset?: AdminContentAssetItem | null) {
  const value = assetLocation(asset);
  return value.includes('video/') || asset?.assetKind === 'video' || /\.(mp4|webm|mov|m4v)(\?|#|$)/i.test(value);
}

export function isLottieAsset(asset?: AdminContentAssetItem | null) {
  return asset?.assetKind === 'lottie' || asset?.animationSubtype === 'lottie-json';
}

export function assetTitle(asset?: AdminContentAssetItem | null) {
  if (!asset) {
    return '尚未選擇資源';
  }
  return asset.originalFilename || asset.objectKey || `資源 #${asset.id}`;
}

export function assetSubtitle(asset?: AdminContentAssetItem | null) {
  if (!asset) {
    return '';
  }
  return asset.clientRelativePath || asset.canonicalUrl || asset.objectKey || '';
}

function iconForAsset(asset?: AdminContentAssetItem | null) {
  if (isImageAsset(asset)) {
    return <FileImageOutlined />;
  }
  if (isAudioAsset(asset)) {
    return <SoundOutlined />;
  }
  if (isVideoAsset(asset)) {
    return <PlayCircleOutlined />;
  }
  if (isLottieAsset(asset)) {
    return <PlayCircleOutlined />;
  }
  return <FileOutlined />;
}

export const MediaAssetPreview: React.FC<{
  asset?: AdminContentAssetItem | null;
  size?: number;
}> = ({ asset, size = 120 }) => {
  const [imageFailed, setImageFailed] = useState(false);

  useEffect(() => {
    setImageFailed(false);
  }, [asset?.id, asset?.canonicalUrl]);

  if (!asset) {
    return (
      <Card
        size="small"
        styles={{ body: { height: size, display: 'flex', alignItems: 'center', justifyContent: 'center' } }}
      >
        <Text type="secondary">尚未選擇資源</Text>
      </Card>
    );
  }

  if (isImageAsset(asset) && asset.canonicalUrl && !imageFailed) {
    return (
      <Image
        src={asset.canonicalUrl}
        alt={assetTitle(asset)}
        width={size}
        height={size}
        style={{ borderRadius: 14, objectFit: 'cover' }}
        onError={() => setImageFailed(true)}
      />
    );
  }

  const healthLabel = !asset.canonicalUrl
    ? '無公開連結'
    : imageFailed
      ? '預覽載入失敗'
      : isLottieAsset(asset)
        ? 'JSON 動畫'
        : '不可直接預覽';
  const healthColor = !asset.canonicalUrl || imageFailed ? 'orange' : isLottieAsset(asset) ? 'purple' : 'default';

  return (
    <Card
      size="small"
      styles={{ body: { height: size, display: 'flex', alignItems: 'center', justifyContent: 'center' } }}
    >
      <Space direction="vertical" align="center" size={6}>
        <Text style={{ fontSize: 24 }}>{iconForAsset(asset)}</Text>
        <Tag color={isLottieAsset(asset) ? 'purple' : 'default'}>
          {isLottieAsset(asset) ? 'Lottie' : asset.assetKind || 'asset'}
        </Tag>
        <Tag color={healthColor}>{healthLabel}</Tag>
        {isLottieAsset(asset) ? (
          <Text type="secondary" style={{ fontSize: 12 }}>
            小程序端用 Lottie Player 渲染
          </Text>
        ) : null}
        {!asset.canonicalUrl || imageFailed || (!isImageAsset(asset) && !isLottieAsset(asset)) ? (
          <Text type="secondary" style={{ maxWidth: size - 12, fontSize: 12, textAlign: 'center' }}>
            此資源可保留在素材包中追蹤，但不應視為可發布素材。
          </Text>
        ) : null}
      </Space>
    </Card>
  );
};

export const MediaAssetMeta: React.FC<{
  asset?: AdminContentAssetItem | null;
}> = ({ asset }) => {
  const title = assetTitle(asset);
  const subtitle = assetSubtitle(asset);

  return (
    <Space direction="vertical" size={4} style={{ width: '100%', minWidth: 0 }}>
      <Text strong style={{ display: 'block', width: '100%', minWidth: 0 }} ellipsis={{ tooltip: title }}>
        {title}
      </Text>
      {subtitle ? (
        <Text
          type="secondary"
          style={{ display: 'block', width: '100%', minWidth: 0, fontSize: 12 }}
          ellipsis={{ tooltip: subtitle }}
        >
          {subtitle}
        </Text>
      ) : null}
      <Space size={[4, 4]} wrap>
        {asset?.assetKind ? <Tag color={isLottieAsset(asset) ? 'purple' : 'default'}>{asset.assetKind}</Tag> : null}
        {asset?.animationSubtype ? <Tag color="purple">{asset.animationSubtype}</Tag> : null}
        {asset?.uploadSource ? <Tag color="blue">{asset.uploadSource}</Tag> : null}
        {asset?.processingPolicyCode ? <Tag color="purple">{asset.processingPolicyCode}</Tag> : null}
        {asset?.processingStatus ? <Tag color="gold">{asset.processingStatus}</Tag> : null}
      </Space>
    </Space>
  );
};
