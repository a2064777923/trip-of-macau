import React from 'react';
import {
  FileImageOutlined,
  FileOutlined,
  PlayCircleOutlined,
  SoundOutlined,
} from '@ant-design/icons';
import { Card, Image, Space, Tag, Typography } from 'antd';
import type { AdminContentAssetItem } from '../../types/admin';

const { Text } = Typography;

export function isImageAsset(asset?: AdminContentAssetItem | null) {
  return !!asset?.mimeType && asset.mimeType.toLowerCase().startsWith('image/');
}

export function isAudioAsset(asset?: AdminContentAssetItem | null) {
  return !!asset?.mimeType && asset.mimeType.toLowerCase().startsWith('audio/');
}

export function isVideoAsset(asset?: AdminContentAssetItem | null) {
  return !!asset?.mimeType && asset.mimeType.toLowerCase().startsWith('video/');
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

  if (isImageAsset(asset) && asset.canonicalUrl) {
    return (
      <Image
        src={asset.canonicalUrl}
        alt={assetTitle(asset)}
        width={size}
        height={size}
        style={{ borderRadius: 14, objectFit: 'cover' }}
      />
    );
  }

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
        {isLottieAsset(asset) ? (
          <Text type="secondary" style={{ fontSize: 12 }}>
            JSON 動畫
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
