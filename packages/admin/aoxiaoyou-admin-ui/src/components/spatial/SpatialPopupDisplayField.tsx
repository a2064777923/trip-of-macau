import React, { useMemo } from 'react';
import { Card, Col, Form, Input, Row, Select, Switch } from 'antd';
import type { FormInstance } from 'antd/es/form';

type NamePath = string | number | Array<string | number>;

interface SpatialPopupDisplayFieldProps {
  form: FormInstance;
  popupFieldName: NamePath;
  displayFieldName: NamePath;
}

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

  return (
    <Card size="small" title="彈窗與展示設定" styles={{ body: { paddingBottom: 8 } }}>
      <Row gutter={16}>
        <Col xs={24} md={12}>
          <Card size="small" title="彈窗介紹">
            <Form.Item label="啟用彈窗">
              <Switch checked={!!popupValue.enabled} onChange={(checked) => updatePopup({ enabled: checked })} />
            </Form.Item>
            <Form.Item label="彈窗模式">
              <Select
                value={popupValue.mode || 'sheet'}
                options={[
                  { label: '底部資訊卡', value: 'sheet' },
                  { label: '地圖氣泡', value: 'bubble' },
                  { label: '全屏沉浸式', value: 'immersive' },
                ]}
                onChange={(value) => updatePopup({ mode: value })}
              />
            </Form.Item>
            <Form.Item label="優先顯示資源類型">
              <Select
                value={popupValue.mediaUsageType || 'cover'}
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
                value={popupValue.summary || ''}
                onChange={(event) => updatePopup({ summary: event.target.value })}
                placeholder="可簡述此地圖或 POI 的彈窗介紹策略"
              />
            </Form.Item>
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card size="small" title="列表與地圖展示">
            <Form.Item label="展示樣式">
              <Select
                value={displayValue.layout || 'card'}
                options={[
                  { label: '卡片', value: 'card' },
                  { label: '橫向條目', value: 'rail' },
                  { label: '地圖浮層', value: 'overlay' },
                ]}
                onChange={(value) => updateDisplay({ layout: value })}
              />
            </Form.Item>
            <Form.Item label="視覺主題">
              <Select
                value={displayValue.theme || 'default'}
                options={[
                  { label: '預設', value: 'default' },
                  { label: '沉浸式', value: 'immersive' },
                  { label: '資訊優先', value: 'info-first' },
                ]}
                onChange={(value) => updateDisplay({ theme: value })}
              />
            </Form.Item>
            <Form.Item label="顯示副標">
              <Switch checked={displayValue.showSubtitle !== false} onChange={(checked) => updateDisplay({ showSubtitle: checked })} />
            </Form.Item>
            <Form.Item label="展示備註">
              <Input.TextArea
                rows={3}
                value={displayValue.note || ''}
                onChange={(event) => updateDisplay({ note: event.target.value })}
                placeholder="例如：首頁卡片僅展示封面與一句摘要"
              />
            </Form.Item>
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
