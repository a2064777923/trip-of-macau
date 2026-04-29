import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import {
  Alert,
  Button,
  Card,
  Col,
  Descriptions,
  Form,
  Input,
  InputNumber,
  List,
  Result,
  Row,
  Select,
  Space,
  Switch,
  Tag,
  Typography,
  message,
} from 'antd';
import { SyncOutlined, TranslationOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import {
  getAdminCarryoverSettings,
  getAdminIndoorRuntimeSettings,
  getAdminMediaPolicySettings,
  getAdminTranslationSettings,
  translateAdminText,
  updateAdminCarryoverSettings,
  updateAdminIndoorRuntimeSettings,
  updateAdminMediaPolicySettings,
  updateAdminTranslationSettings,
} from '../../services/api';
import type {
  AdminCarryoverSettings,
  AdminIndoorRuntimeSettings,
  AdminMediaPolicyKindSettings,
  AdminTranslateLocaleResult,
  SupportedLocale,
} from '../../types/admin';
import {
  LOCALE_LABELS,
  SUPPORTED_LOCALES,
} from '../../components/localization/LocalizedFieldGroup';

const { Paragraph, Text } = Typography;

const ENGINE_OPTIONS = [
  { value: 'google', label: 'Google' },
  { value: 'bing', label: 'Bing' },
  { value: 'deepl', label: 'DeepL' },
  { value: 'yandex', label: 'Yandex' },
  { value: 'baidu', label: 'Baidu' },
  { value: 'alibaba', label: 'Alibaba' },
  { value: 'iciba', label: 'Iciba' },
  { value: 'sogou', label: 'Sogou' },
  { value: 'tencent', label: 'Tencent' },
];

const IMAGE_POLICY_OPTIONS = [
  { value: 'lossless', label: '無損' },
  { value: 'compressed', label: '圖片壓縮' },
  { value: 'passthrough', label: '原檔直通' },
];

const GENERIC_POLICY_OPTIONS = [
  { value: 'compressed', label: '壓縮後上傳' },
  { value: 'passthrough', label: '原檔直通' },
  { value: 'lossless', label: '僅限具權限帳號無損' },
];

const POLICY_NOTE_LABELS: Record<string, string> = {
  'Image uploads scale down when lossless upload is not allowed': '帳號沒有無損權限時，圖片會自動壓縮後再上傳',
  'Video uploads keep the original file in this phase': '此階段影片保留原檔上傳',
  'Audio uploads keep the original file in this phase': '此階段音訊保留原檔上傳',
  'Other files keep the original payload': '其他檔案保留原始內容上傳',
};

function bytesToMb(value?: number | null) {
  if (value === undefined || value === null) {
    return undefined;
  }
  return Number((value / 1024 / 1024).toFixed(2));
}

function mbToBytes(value?: number | null) {
  if (value === undefined || value === null) {
    return undefined;
  }
  return Math.round(value * 1024 * 1024);
}

function toPolicyFormValue(policy?: AdminMediaPolicyKindSettings) {
  return {
    maxFileSizeMb: bytesToMb(policy?.maxFileSizeBytes),
    preferredPolicyCode: policy?.preferredPolicyCode,
    qualityPercent: policy?.qualityPercent,
    maxWidthPx: policy?.maxWidthPx,
    maxHeightPx: policy?.maxHeightPx,
    preserveMetadata: policy?.preserveMetadata,
    note: policy?.note ? POLICY_NOTE_LABELS[policy.note] || policy.note : policy?.note,
  };
}

function fromPolicyFormValue(value?: {
  maxFileSizeMb?: number;
  preferredPolicyCode?: string;
  qualityPercent?: number;
  maxWidthPx?: number | null;
  maxHeightPx?: number | null;
  preserveMetadata?: boolean;
  note?: string;
}) {
  return {
    maxFileSizeBytes: mbToBytes(value?.maxFileSizeMb),
    preferredPolicyCode: value?.preferredPolicyCode,
    qualityPercent: value?.qualityPercent,
    maxWidthPx: value?.maxWidthPx,
    maxHeightPx: value?.maxHeightPx,
    preserveMetadata: value?.preserveMetadata,
    note: value?.note,
  };
}

const SystemManagement: React.FC = () => {
  const [carryoverForm] = Form.useForm();
  const [translationForm] = Form.useForm();
  const [mediaPolicyForm] = Form.useForm();
  const [indoorRuntimeForm] = Form.useForm();
  const [labForm] = Form.useForm();
  const [labResults, setLabResults] = useState<AdminTranslateLocaleResult[]>([]);
  const [labSubmitting, setLabSubmitting] = useState(false);

  const carryoverRequest = useRequest(getAdminCarryoverSettings);
  const translationRequest = useRequest(getAdminTranslationSettings);
  const mediaPolicyRequest = useRequest(getAdminMediaPolicySettings);
  const indoorRuntimeRequest = useRequest(getAdminIndoorRuntimeSettings);

  useEffect(() => {
    if (carryoverRequest.data?.success && carryoverRequest.data.data) {
      carryoverForm.setFieldsValue(carryoverRequest.data.data);
    }
  }, [carryoverForm, carryoverRequest.data]);

  useEffect(() => {
    if (translationRequest.data?.success && translationRequest.data.data) {
      const settings = translationRequest.data.data;
      translationForm.setFieldsValue({
        primaryAuthoringLocale: settings.primaryAuthoringLocale,
        enginePriority: settings.enginePriority,
        overwriteFilledLocales: settings.overwriteFilledLocales,
      });
      labForm.setFieldsValue({
        sourceLocale: settings.primaryAuthoringLocale,
        targetLocales: SUPPORTED_LOCALES.filter((locale) => locale !== settings.primaryAuthoringLocale),
        overwriteFilledLocales: settings.overwriteFilledLocales,
      });
    }
  }, [labForm, translationForm, translationRequest.data]);

  useEffect(() => {
    if (mediaPolicyRequest.data?.success && mediaPolicyRequest.data.data) {
      const settings = mediaPolicyRequest.data.data;
      mediaPolicyForm.setFieldsValue({
        maxBatchCount: settings.maxBatchCount,
        maxBatchTotalMb: bytesToMb(settings.maxBatchTotalBytes),
        image: toPolicyFormValue(settings.image),
        video: toPolicyFormValue(settings.video),
        audio: toPolicyFormValue(settings.audio),
        file: toPolicyFormValue(settings.file),
      });
    }
  }, [mediaPolicyForm, mediaPolicyRequest.data]);

  useEffect(() => {
    if (indoorRuntimeRequest.data?.success && indoorRuntimeRequest.data.data) {
      indoorRuntimeForm.setFieldsValue(indoorRuntimeRequest.data.data);
    }
  }, [indoorRuntimeForm, indoorRuntimeRequest.data]);

  const localeOptions = useMemo(
    () =>
      SUPPORTED_LOCALES.map((locale) => ({
        value: locale,
        label: LOCALE_LABELS[locale],
      })),
    [],
  );

  const renderLabStatus = (result: AdminTranslateLocaleResult) => {
    if (result.status === 'success') {
      return <Tag color="success">成功</Tag>;
    }
    if (result.status === 'skipped') {
      return <Tag color="gold">略過</Tag>;
    }
    return <Tag color="error">失敗</Tag>;
  };

  const renderPolicyGroup = (
    name: 'image' | 'video' | 'audio' | 'file',
    title: string,
    description: string,
    policyOptions: Array<{ value: string; label: string }>,
    allowDimensions: boolean,
  ) => (
    <Card size="small" title={title} extra={<Text type="secondary">{description}</Text>}>
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Form.Item name={[name, 'maxFileSizeMb']} label="單檔上限（MB）">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name={[name, 'preferredPolicyCode']} label="預設策略">
            <Select options={policyOptions} />
          </Form.Item>
        </Col>
        <Col xs={24} md={8}>
          <Form.Item name={[name, 'qualityPercent']} label="品質百分比">
            <InputNumber min={1} max={100} style={{ width: '100%' }} />
          </Form.Item>
        </Col>
        {allowDimensions ? (
          <>
            <Col xs={24} md={6}>
              <Form.Item name={[name, 'maxWidthPx']} label="最大寬度（px）">
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={6}>
              <Form.Item name={[name, 'maxHeightPx']} label="最大高度（px）">
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </>
        ) : null}
        <Col xs={24} md={allowDimensions ? 6 : 8}>
          <Form.Item name={[name, 'preserveMetadata']} label="保留中繼資料" valuePropName="checked">
            <Switch checkedChildren="保留" unCheckedChildren="裁減" />
          </Form.Item>
        </Col>
        <Col xs={24} md={allowDimensions ? 6 : 16}>
          <Form.Item name={[name, 'note']} label="策略說明">
            <Input placeholder="例如：無權限帳號自動壓縮後再上傳" />
          </Form.Item>
        </Col>
      </Row>
    </Card>
  );

  return (
    <PageContainer
      title="系統配置"
      subTitle="集中管理翻譯主欄位語言、翻譯引擎順序，以及媒體上傳策略與批次匯入限制。"
    >
      <Row gutter={[24, 24]}>
        <Col span={24}>
          <Card title="總控預設" bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Alert
                type="info"
                showIcon
                message="集中管理 Phase 14 的顯式預設欄位"
                description="這裡會同步管理翻譯主欄位語言、翻譯引擎順序、預設上傳策略，以及地圖與室內縮放預設，方便後台營運快速核對。"
              />
              <Form
                form={carryoverForm}
                layout="vertical"
                onFinish={async (values: AdminCarryoverSettings) => {
                  const response = await updateAdminCarryoverSettings(values);
                  if (!response.success || !response.data) {
                    message.error(response.message || '更新總控預設失敗');
                    return;
                  }
                  message.success('總控預設已更新');
                  carryoverRequest.refresh();
                  translationRequest.refresh();
                  mediaPolicyRequest.refresh();
                  indoorRuntimeRequest.refresh();
                }}
              >
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={8}>
                    <Form.Item
                      name="translationDefaultLocale"
                      label="翻譯主欄位語言"
                      rules={[{ required: true, message: '請選擇翻譯主欄位語言' }]}
                    >
                      <Select options={localeOptions} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={16}>
                    <Form.Item
                      name="translationEnginePriority"
                      label="翻譯引擎優先順序"
                      rules={[{ required: true, message: '請至少選擇一個翻譯引擎' }]}
                    >
                      <Select mode="multiple" options={ENGINE_OPTIONS} />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={8}>
                    <Form.Item
                      name="mediaUploadDefaultPolicyCode"
                      label="預設上傳策略"
                      rules={[{ required: true, message: '請選擇預設上傳策略' }]}
                    >
                      <Select options={IMAGE_POLICY_OPTIONS} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8}>
                    <Form.Item
                      name="mapZoomDefaultMinScale"
                      label="地圖縮放下限"
                      rules={[{ required: true, message: '請輸入地圖縮放下限' }]}
                    >
                      <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={8}>
                    <Form.Item
                      name="mapZoomDefaultMaxScale"
                      label="地圖縮放上限"
                      rules={[{ required: true, message: '請輸入地圖縮放上限' }]}
                    >
                      <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                </Row>
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={12}>
                    <Form.Item
                      name="indoorZoomDefaultMinScale"
                      label="室內縮放下限（米）"
                      rules={[{ required: true, message: '請輸入室內縮放下限' }]}
                    >
                      <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={12}>
                    <Form.Item
                      name="indoorZoomDefaultMaxScale"
                      label="室內縮放上限（米）"
                      rules={[{ required: true, message: '請輸入室內縮放上限' }]}
                    >
                      <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                </Row>
                <Space>
                  <Button type="primary" htmlType="submit">
                    儲存總控預設
                  </Button>
                  <Button icon={<SyncOutlined />} onClick={() => carryoverRequest.refresh()}>
                    重新載入
                  </Button>
                </Space>
              </Form>
            </Space>
          </Card>
        </Col>
        <Col span={24}>
          <Card title="室內瓦片與縮放設定" bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Alert
                type="info"
                showIcon
                message="控制樓層瓦片導入時的自動縮放推導與預設瓦片尺寸"
                description="預設規則會以最大比例尺 50 公分、最小比例尺 20 公尺推導手機端縮放範圍；這裡的數值會直接影響 ZIP / 整圖切片導入時自動填入的 zoomMin、defaultZoom、zoomMax。"
              />

              <Form
                form={indoorRuntimeForm}
                layout="vertical"
                onFinish={async (values: AdminIndoorRuntimeSettings) => {
                  const response = await updateAdminIndoorRuntimeSettings(values);
                  if (!response.success || !response.data) {
                    message.error(response.message || '儲存室內瓦片設定失敗');
                    return;
                  }
                  message.success('室內瓦片與縮放設定已更新');
                  indoorRuntimeRequest.refresh();
                }}
              >
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name="minScaleMeters"
                      label="最小可視比例尺（公尺）"
                      rules={[{ required: true, message: '請輸入最小可視比例尺' }]}
                      extra="預設 20 公尺，數值越大代表允許縮得更遠。"
                    >
                      <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name="maxScaleMeters"
                      label="最大可視比例尺（公尺）"
                      rules={[{ required: true, message: '請輸入最大可視比例尺' }]}
                      extra="預設 0.5 公尺，數值越小代表允許放得更近。"
                    >
                      <InputNumber min={0.1} step={0.1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name="referenceViewportPx"
                      label="參考視窗寬度（px）"
                      rules={[{ required: true, message: '請輸入參考視窗寬度' }]}
                      extra="用於把樓層面積換算成手機端縮放倍數。"
                    >
                      <InputNumber min={1} step={1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name="defaultTileSizePx"
                      label="預設瓦片尺寸（px）"
                      rules={[{ required: true, message: '請輸入預設瓦片尺寸' }]}
                      extra="系統會限制在 128 到 1024 px 之間。"
                    >
                      <InputNumber min={128} max={1024} step={128} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                </Row>

                <Space>
                  <Button type="primary" htmlType="submit">
                    儲存室內瓦片設定
                  </Button>
                  <Button icon={<SyncOutlined />} onClick={() => indoorRuntimeRequest.refresh()}>
                    重新載入
                  </Button>
                </Space>
              </Form>
            </Space>
          </Card>
        </Col>

        <Col span={24}>
          <Card title="媒體上傳策略" bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Alert
                type="info"
                showIcon
                message="後端會依這裡的設定與管理員權限決定最終處理策略"
                description="即使前端指定了某種上傳模式，後端仍會再檢查帳號是否擁有無損權限，並把最終策略與處理快照寫回資料庫。"
              />

              <Form
                form={mediaPolicyForm}
                layout="vertical"
                onFinish={async (values) => {
                  const response = await updateAdminMediaPolicySettings({
                    maxBatchCount: values.maxBatchCount,
                    maxBatchTotalBytes: mbToBytes(values.maxBatchTotalMb) || 0,
                    image: fromPolicyFormValue(values.image),
                    video: fromPolicyFormValue(values.video),
                    audio: fromPolicyFormValue(values.audio),
                    file: fromPolicyFormValue(values.file),
                  });
                  if (!response.success || !response.data) {
                    message.error(response.message || '儲存媒體策略失敗');
                    return;
                  }
                  message.success('媒體上傳策略已更新');
                  mediaPolicyRequest.refresh();
                }}
              >
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={6}>
                    <Form.Item name="maxBatchCount" label="單次批次檔案數上限">
                      <InputNumber min={1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item name="maxBatchTotalMb" label="單次批次總容量（MB）">
                      <InputNumber min={1} style={{ width: '100%' }} />
                    </Form.Item>
                  </Col>
                </Row>

                <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                  {renderPolicyGroup('image', '圖片策略', '封面圖、圖集、圖標', IMAGE_POLICY_OPTIONS, true)}
                  {renderPolicyGroup('video', '影片策略', '宣傳片、故事短片', GENERIC_POLICY_OPTIONS, false)}
                  {renderPolicyGroup('audio', '音訊策略', '導覽音訊、旁白、音效', GENERIC_POLICY_OPTIONS, false)}
                  {renderPolicyGroup('file', '其他檔案策略', 'JSON、壓縮檔與其他附件', GENERIC_POLICY_OPTIONS, false)}
                </Space>

                <Space style={{ marginTop: 16 }}>
                  <Button type="primary" htmlType="submit">
                    儲存媒體策略
                  </Button>
                  <Button icon={<SyncOutlined />} onClick={() => mediaPolicyRequest.refresh()}>
                    重新載入
                  </Button>
                </Space>
              </Form>
            </Space>
          </Card>
        </Col>

        <Col xs={24} xl={14}>
          <Card title="翻譯與主欄位設定" bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Alert
                type="info"
                showIcon
                message="主欄位語言與翻譯引擎都在這裡設定"
                description="多語欄位的一鍵翻譯會依照這裡設定的主欄位語言與引擎優先順序嘗試翻譯，前一個引擎阻塞時才會切換到下一個。"
              />

              <Form
                form={translationForm}
                layout="vertical"
                onFinish={async (values) => {
                  const response = await updateAdminTranslationSettings(values);
                  if (!response.success || !response.data) {
                    message.error(response.message || '儲存翻譯設定失敗');
                    return;
                  }
                  message.success('翻譯設定已更新');
                  translationRequest.refresh();
                }}
              >
                <Form.Item
                  name="primaryAuthoringLocale"
                  label="主欄位語言"
                  rules={[{ required: true, message: '請選擇主欄位語言' }]}
                >
                  <Select options={localeOptions} />
                </Form.Item>

                <Form.Item
                  name="enginePriority"
                  label="翻譯引擎優先順序"
                  rules={[{ required: true, message: '請至少選擇一個翻譯引擎' }]}
                  extra="系統會依這個順序依次嘗試翻譯；前一個引擎失敗或超時時，再自動切換到下一個。"
                >
                  <Select
                    mode="multiple"
                    options={ENGINE_OPTIONS}
                    placeholder="請依序選擇翻譯引擎"
                  />
                </Form.Item>

                <Form.Item
                  name="overwriteFilledLocales"
                  label="一鍵翻譯覆蓋策略"
                  valuePropName="checked"
                  extra="關閉時只會填補空白欄位；開啟後會覆蓋既有翻譯內容。"
                >
                  <Switch checkedChildren="覆蓋已填內容" unCheckedChildren="只補空白欄位" />
                </Form.Item>

                <Space>
                  <Button type="primary" htmlType="submit">
                    儲存翻譯設定
                  </Button>
                  <Button icon={<SyncOutlined />} onClick={() => translationRequest.refresh()}>
                    重新載入
                  </Button>
                </Space>
              </Form>
            </Space>
          </Card>
        </Col>

        <Col xs={24} xl={10}>
          <Card title="翻譯服務狀態" bordered={false}>
            {translationRequest.data?.data ? (
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="翻譯橋接">{translationRequest.data.data.bridgeEnabled ? <Tag color="success">已啟用</Tag> : <Tag>未啟用</Tag>}</Descriptions.Item>
                <Descriptions.Item label="請求逾時">{translationRequest.data.data.requestTimeoutMs} ms</Descriptions.Item>
                <Descriptions.Item label="最大字數">{translationRequest.data.data.maxTextLength}</Descriptions.Item>
                <Descriptions.Item label="目前主欄位語言">
                  {LOCALE_LABELS[translationRequest.data.data.primaryAuthoringLocale]}
                </Descriptions.Item>
                <Descriptions.Item label="目前引擎順序">
                  <Space wrap>
                    {translationRequest.data.data.enginePriority.map((engine) => (
                      <Tag key={engine}>{engine}</Tag>
                    ))}
                  </Space>
                </Descriptions.Item>
                <Descriptions.Item label="橋接腳本">
                  <Text code>{translationRequest.data.data.bridgeScriptPath || '未設定'}</Text>
                </Descriptions.Item>
              </Descriptions>
            ) : (
              <Result status="info" title="尚未載入翻譯設定" />
            )}
          </Card>
        </Col>

        <Col span={24}>
          <Card title="翻譯測試台" bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Paragraph style={{ marginBottom: 0 }}>
                在正式套用到內容欄位之前，可先在這裡用主欄位語言與目前引擎優先順序測試翻譯結果。
              </Paragraph>

              <Form
                form={labForm}
                layout="vertical"
                onFinish={async (values: {
                  sourceLocale: SupportedLocale;
                  targetLocales: SupportedLocale[];
                  text: string;
                  overwriteFilledLocales?: boolean;
                }) => {
                  setLabSubmitting(true);
                  try {
                    const response = await translateAdminText({
                      sourceLocale: values.sourceLocale,
                      targetLocales: values.targetLocales,
                      text: values.text,
                      overwriteFilledLocales: values.overwriteFilledLocales,
                    });
                    if (!response.success || !response.data) {
                      message.error(response.message || '翻譯測試失敗');
                      setLabResults([]);
                      return;
                    }
                    setLabResults(response.data.results || []);
                    message.success('翻譯測試已完成');
                  } finally {
                    setLabSubmitting(false);
                  }
                }}
              >
                <Row gutter={[16, 16]}>
                  <Col xs={24} md={8}>
                    <Form.Item
                      name="sourceLocale"
                      label="來源語言"
                      rules={[{ required: true, message: '請選擇來源語言' }]}
                    >
                      <Select options={localeOptions} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={10}>
                    <Form.Item
                      name="targetLocales"
                      label="目標語言"
                      rules={[{ required: true, message: '請至少選擇一個目標語言' }]}
                    >
                      <Select mode="multiple" options={localeOptions} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item name="overwriteFilledLocales" label="覆蓋策略" valuePropName="checked">
                      <Switch checkedChildren="覆蓋" unCheckedChildren="只補空白" />
                    </Form.Item>
                  </Col>
                </Row>

                <Form.Item
                  name="text"
                  label="待翻譯內容"
                  rules={[{ required: true, message: '請輸入測試內容' }]}
                >
                  <Input.TextArea rows={4} placeholder="請輸入要送往翻譯引擎的文本內容" />
                </Form.Item>

                <Space>
                  <Button type="primary" htmlType="submit" icon={<TranslationOutlined />} loading={labSubmitting}>
                    執行翻譯測試
                  </Button>
                  <Button
                    onClick={() => {
                      labForm.resetFields();
                      setLabResults([]);
                    }}
                  >
                    清空結果
                  </Button>
                </Space>
              </Form>

              <List
                bordered
                locale={{ emptyText: '尚未執行翻譯測試' }}
                dataSource={labResults}
                renderItem={(result) => (
                  <List.Item>
                    <Space direction="vertical" size={6} style={{ width: '100%' }}>
                      <Space wrap>
                        <Text strong>{LOCALE_LABELS[result.targetLocale]}</Text>
                        {renderLabStatus(result)}
                        {result.engine ? <Tag color="purple">{result.engine}</Tag> : null}
                        {(result.attemptedEngines || []).map((engine) => (
                          <Tag key={`${result.targetLocale}-${engine}`}>{engine}</Tag>
                        ))}
                      </Space>
                      {result.translatedText ? (
                        <Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>
                          {result.translatedText}
                        </Paragraph>
                      ) : null}
                      {result.message ? <Text type="secondary">{result.message}</Text> : null}
                    </Space>
                  </List.Item>
                )}
              />
            </Space>
          </Card>
        </Col>
      </Row>
    </PageContainer>
  );
};

export default SystemManagement;
