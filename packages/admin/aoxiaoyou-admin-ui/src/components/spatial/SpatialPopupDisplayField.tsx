import React, { useMemo, useState } from 'react';
import { Card, Col, Form, Input, Row, Select, Space, Switch, Typography } from 'antd';
import type { FormInstance } from 'antd/es/form';

type NamePath = string | number | Array<string | number>;

interface SpatialPopupDisplayFieldProps {
  form: FormInstance;
  popupFieldName: NamePath;
  displayFieldName: NamePath;
}

const { Text } = Typography;

const popupTemplateOptions = [
  {
    label: '底部資訊卡',
    value: 'sheet',
    patch: { enabled: true, mode: 'sheet', mediaUsageType: 'cover', summary: '' },
  },
  {
    label: '地圖氣泡',
    value: 'bubble',
    patch: { enabled: true, mode: 'bubble', mediaUsageType: 'gallery', summary: '' },
  },
  {
    label: '沉浸式介紹',
    value: 'immersive',
    patch: { enabled: true, mode: 'immersive', mediaUsageType: 'video', summary: '' },
  },
];

const displayTemplateOptions = [
  {
    label: '標準卡片',
    value: 'card',
    patch: { layout: 'card', theme: 'default', showSubtitle: true, note: '' },
  },
  {
    label: '資訊優先',
    value: 'rail',
    patch: { layout: 'rail', theme: 'info-first', showSubtitle: true, note: '' },
  },
  {
    label: '地圖疊層',
    value: 'overlay',
    patch: { layout: 'overlay', theme: 'immersive', showSubtitle: false, note: '' },
  },
];

function safeParse(value: unknown) {
  if (typeof value !== 'string' || !value.trim()) {
    return {};
  }
  try {
    return JSON.parse(value);
  } catch {
    return {};
  }
}

function stringifyConfig(value: Record<string, unknown>) {
  return JSON.stringify(value, null, 2);
}

const SpatialPopupDisplayField: React.FC<SpatialPopupDisplayFieldProps> = ({
  form,
  popupFieldName,
  displayFieldName,
}) => {
  const popupConfig = Form.useWatch(popupFieldName, form);
  const displayConfig = Form.useWatch(displayFieldName, form);
  const [showPopupJsonEditor, setShowPopupJsonEditor] = useState(false);
  const [showDisplayJsonEditor, setShowDisplayJsonEditor] = useState(false);

  const popupValue = useMemo(() => safeParse(popupConfig), [popupConfig]);
  const displayValue = useMemo(() => safeParse(displayConfig), [displayConfig]);

  const updatePopup = (patch: Record<string, unknown>) => {
    form.setFieldValue(
      popupFieldName,
      stringifyConfig({
        ...popupValue,
        ...patch,
      }),
    );
  };

  const updateDisplay = (patch: Record<string, unknown>) => {
    form.setFieldValue(
      displayFieldName,
      stringifyConfig({
        ...displayValue,
        ...patch,
      }),
    );
  };

  const handlePopupTemplateChange = (value: string) => {
    const template = popupTemplateOptions.find((item) => item.value === value);
    if (template) {
      updatePopup(template.patch);
    }
  };

  const handleDisplayTemplateChange = (value: string) => {
    const template = displayTemplateOptions.find((item) => item.value === value);
    if (template) {
      updateDisplay(template.patch);
    }
  };

  return (
    <Card size="small" title="彈窗與展示設定" styles={{ body: { paddingBottom: 8 } }}>
      <Row gutter={16}>
        <Col xs={24} md={12}>
          <Card size="small" title="彈窗介紹">
            <Form.Item label="預設模板">
              <Select
                value={(popupValue.mode as string) || 'sheet'}
                options={popupTemplateOptions.map((item) => ({ label: item.label, value: item.value }))}
                onChange={handlePopupTemplateChange}
              />
            </Form.Item>
            <Form.Item label="啟用彈窗">
              <Switch checked={!!popupValue.enabled} onChange={(checked) => updatePopup({ enabled: checked })} />
            </Form.Item>
            <Form.Item label="彈窗模式">
              <Select
                value={(popupValue.mode as string) || 'sheet'}
                options={[
                  { label: '底部資訊卡', value: 'sheet' },
                  { label: '地圖氣泡', value: 'bubble' },
                  { label: '全螢幕沉浸式', value: 'immersive' },
                ]}
                onChange={(value) => updatePopup({ mode: value })}
              />
            </Form.Item>
            <Form.Item label="優先展示資源類型">
              <Select
                value={(popupValue.mediaUsageType as string) || 'cover'}
                options={[
                  { label: '封面', value: 'cover' },
                  { label: '圖集', value: 'gallery' },
                  { label: '影片', value: 'video' },
                  { label: '音訊', value: 'audio' },
                ]}
                onChange={(value) => updatePopup({ mediaUsageType: value })}
              />
            </Form.Item>
            <Form.Item label="彈窗摘要">
              <Input.TextArea
                rows={3}
                value={(popupValue.summary as string) || ''}
                onChange={(event) => updatePopup({ summary: event.target.value })}
                placeholder="可描述此地圖、POI 或樓層彈窗的呈現方式"
              />
            </Form.Item>
            <Space style={{ display: 'flex', justifyContent: 'space-between' }}>
              <Text type="secondary">需要更細節的控制時，再切到 JSON 編輯模式。</Text>
              <Space size={8}>
                <Text type="secondary">進階 JSON</Text>
                <Switch checked={showPopupJsonEditor} onChange={setShowPopupJsonEditor} />
              </Space>
            </Space>
            {showPopupJsonEditor ? (
              <Form.Item label="彈窗設定 JSON" style={{ marginTop: 12, marginBottom: 0 }}>
                <Input.TextArea
                  rows={5}
                  value={popupConfig || ''}
                  onChange={(event) => form.setFieldValue(popupFieldName, event.target.value)}
                  placeholder='例如 {"enabled":true,"mode":"sheet"}'
                />
              </Form.Item>
            ) : null}
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card size="small" title="列表與地圖展示">
            <Form.Item label="預設模板">
              <Select
                value={(displayValue.layout as string) || 'card'}
                options={displayTemplateOptions.map((item) => ({ label: item.label, value: item.value }))}
                onChange={handleDisplayTemplateChange}
              />
            </Form.Item>
            <Form.Item label="展示模式">
              <Select
                value={(displayValue.layout as string) || 'card'}
                options={[
                  { label: '卡片', value: 'card' },
                  { label: '橫向列表', value: 'rail' },
                  { label: '地圖疊層', value: 'overlay' },
                ]}
                onChange={(value) => updateDisplay({ layout: value })}
              />
            </Form.Item>
            <Form.Item label="視覺主題">
              <Select
                value={(displayValue.theme as string) || 'default'}
                options={[
                  { label: '標準', value: 'default' },
                  { label: '沉浸式', value: 'immersive' },
                  { label: '資訊優先', value: 'info-first' },
                ]}
                onChange={(value) => updateDisplay({ theme: value })}
              />
            </Form.Item>
            <Form.Item label="顯示副標">
              <Switch
                checked={displayValue.showSubtitle !== false}
                onChange={(checked) => updateDisplay({ showSubtitle: checked })}
              />
            </Form.Item>
            <Form.Item label="展示備註">
              <Input.TextArea
                rows={3}
                value={(displayValue.note as string) || ''}
                onChange={(event) => updateDisplay({ note: event.target.value })}
                placeholder="例如：首頁卡片優先展示封面與一句摘要"
              />
            </Form.Item>
            <Space style={{ display: 'flex', justifyContent: 'space-between' }}>
              <Text type="secondary">如需手動補更多控制欄位，可開啟 JSON 編輯。</Text>
              <Space size={8}>
                <Text type="secondary">進階 JSON</Text>
                <Switch checked={showDisplayJsonEditor} onChange={setShowDisplayJsonEditor} />
              </Space>
            </Space>
            {showDisplayJsonEditor ? (
              <Form.Item label="展示設定 JSON" style={{ marginTop: 12, marginBottom: 0 }}>
                <Input.TextArea
                  rows={5}
                  value={displayConfig || ''}
                  onChange={(event) => form.setFieldValue(displayFieldName, event.target.value)}
                  placeholder='例如 {"layout":"overlay","theme":"immersive"}'
                />
              </Form.Item>
            ) : null}
          </Card>
        </Col>
      </Row>
      <Form.Item name={popupFieldName} hidden>
        <Input />
      </Form.Item>
      <Form.Item name={displayFieldName} hidden>
        <Input />
      </Form.Item>
    </Card>
  );
};

export default SpatialPopupDisplayField;
