import React, { useMemo, useState } from 'react';
import { Alert, Button, Card, Col, Form, InputNumber, Row, Select, Space, Typography, message } from 'antd';
import type { FormInstance } from 'antd/es/form';
import { AimOutlined } from '@ant-design/icons';
import { previewSpatialCoordinate } from '../../services/api';
import type { AdminCoordinatePreviewResult, CoordinateSystem } from '../../types/admin';

const { Text } = Typography;
type NamePath = string | number | Array<string | number>;

const coordinateSystemOptions: Array<{ label: string; value: CoordinateSystem }> = [
  { label: 'GCJ-02（高德 / 騰訊）', value: 'GCJ02' },
  { label: 'WGS84（GPS）', value: 'WGS84' },
  { label: 'BD-09（百度）', value: 'BD09' },
  { label: '未知 / 待確認', value: 'UNKNOWN' },
];

interface SpatialCoordinateFieldGroupProps {
  form: FormInstance;
  title?: string;
  required?: boolean;
  sourceSystemName: NamePath;
  sourceLatitudeName: NamePath;
  sourceLongitudeName: NamePath;
  normalizedLatitudeName?: NamePath;
  normalizedLongitudeName?: NamePath;
}

function toNumber(value: unknown) {
  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : undefined;
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : undefined;
  }
  return undefined;
}

const SpatialCoordinateFieldGroup: React.FC<SpatialCoordinateFieldGroupProps> = ({
  form,
  title = '座標與坐標系',
  required,
  sourceSystemName,
  sourceLatitudeName,
  sourceLongitudeName,
  normalizedLatitudeName,
  normalizedLongitudeName,
}) => {
  const [preview, setPreview] = useState<AdminCoordinatePreviewResult | null>(null);
  const [previewing, setPreviewing] = useState(false);

  const sourceSystem = Form.useWatch(sourceSystemName, form) as CoordinateSystem | undefined;
  const sourceLatitude = Form.useWatch(sourceLatitudeName, form);
  const sourceLongitude = Form.useWatch(sourceLongitudeName, form);
  const normalizedLatitude = normalizedLatitudeName ? Form.useWatch(normalizedLatitudeName, form) : undefined;
  const normalizedLongitude = normalizedLongitudeName ? Form.useWatch(normalizedLongitudeName, form) : undefined;

  const normalizedSummary = useMemo(() => {
    const latitude = preview?.normalizedLatitude ?? toNumber(normalizedLatitude);
    const longitude = preview?.normalizedLongitude ?? toNumber(normalizedLongitude);
    if (latitude == null || longitude == null) {
      return null;
    }
    return `${latitude}, ${longitude}`;
  }, [normalizedLatitude, normalizedLongitude, preview]);

  const handlePreview = async () => {
    const latitude = toNumber(sourceLatitude);
    const longitude = toNumber(sourceLongitude);
    if (latitude == null || longitude == null) {
      message.warning('請先輸入來源緯度與來源經度。');
      return;
    }

    setPreviewing(true);
    try {
      const response = await previewSpatialCoordinate({
        sourceCoordinateSystem: sourceSystem || 'GCJ02',
        latitude,
        longitude,
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || '座標換算失敗');
      }
      setPreview(response.data);
      if (normalizedLatitudeName) {
        form.setFieldValue(normalizedLatitudeName, response.data.normalizedLatitude ?? null);
      }
      if (normalizedLongitudeName) {
        form.setFieldValue(normalizedLongitudeName, response.data.normalizedLongitude ?? null);
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '座標換算失敗');
    } finally {
      setPreviewing(false);
    }
  };

  return (
    <Card
      size="small"
      title={
        <Space size={8}>
          <Text strong>{title}</Text>
          {required ? <Text type="secondary">必填</Text> : null}
        </Space>
      }
      styles={{ body: { paddingBottom: 8 } }}
    >
      <Row gutter={16}>
        <Col xs={24} md={8}>
          <Form.Item name={sourceSystemName} label="來源坐標系" initialValue="GCJ02">
            <Select options={coordinateSystemOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item
            name={sourceLatitudeName}
            label="來源緯度"
            rules={required ? [{ required: true, message: '請輸入來源緯度' }] : undefined}
          >
            <InputNumber style={{ width: '100%' }} step={0.000001} placeholder="例如 22.1987" />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item
            name={sourceLongitudeName}
            label="來源經度"
            rules={required ? [{ required: true, message: '請輸入來源經度' }] : undefined}
          >
            <InputNumber style={{ width: '100%' }} step={0.000001} placeholder="例如 113.5439" />
          </Form.Item>
        </Col>
      </Row>

      <Row gutter={16}>
        {normalizedLatitudeName ? (
          <Col xs={24} md={8}>
            <Form.Item name={normalizedLatitudeName} label="換算後緯度（GCJ-02）">
              <InputNumber style={{ width: '100%' }} step={0.000001} disabled />
            </Form.Item>
          </Col>
        ) : null}
        {normalizedLongitudeName ? (
          <Col xs={24} md={8}>
            <Form.Item name={normalizedLongitudeName} label="換算後經度（GCJ-02）">
              <InputNumber style={{ width: '100%' }} step={0.000001} disabled />
            </Form.Item>
          </Col>
        ) : null}
      </Row>

      <Row gutter={16} style={{ marginTop: 4, marginBottom: 8 }}>
        <Col xs={24} md={8}>
          <Button block icon={<AimOutlined />} loading={previewing} onClick={() => void handlePreview()}>
            預覽並換算
          </Button>
        </Col>
      </Row>

      {preview?.note || normalizedSummary ? (
        <Alert
          type="info"
          showIcon
          message={preview?.normalizationStatus || '座標預覽'}
          description={
            <Space direction="vertical" size={4}>
              {preview?.note ? <Text>{preview.note}</Text> : null}
              {normalizedSummary ? <Text>目前換算後坐標：{normalizedSummary}</Text> : null}
            </Space>
          }
        />
      ) : null}
    </Card>
  );
};

export default SpatialCoordinateFieldGroup;
