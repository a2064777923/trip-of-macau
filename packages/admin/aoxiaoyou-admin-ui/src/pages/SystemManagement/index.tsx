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
  getAdminTranslationSettings,
  translateAdminText,
  updateAdminTranslationSettings,
} from '../../services/api';
import type {
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

const SystemManagement: React.FC = () => {
  const [settingsForm] = Form.useForm();
  const [labForm] = Form.useForm();
  const [labResults, setLabResults] = useState<AdminTranslateLocaleResult[]>([]);
  const [labSubmitting, setLabSubmitting] = useState(false);

  const settingsRequest = useRequest(getAdminTranslationSettings);

  useEffect(() => {
    if (settingsRequest.data?.success && settingsRequest.data.data) {
      const settings = settingsRequest.data.data;
      settingsForm.setFieldsValue({
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
  }, [labForm, settingsForm, settingsRequest.data]);

  const settings = settingsRequest.data?.data;

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

  return (
    <PageContainer
      title="翻譯與多語設定"
      subTitle="管理四語內容的主欄位語言、翻譯引擎優先序與翻譯測試流程。"
    >
      <Row gutter={[24, 24]}>
        <Col xs={24} xl={14}>
          <Card title="全域翻譯預設" bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Alert
                type="info"
                showIcon
                message="Phase 8 僅要求主欄位可儲存草稿，其餘語言可以透過翻譯補齊。"
                description="一般內容儲存不會因翻譯引擎短暫失效而被阻塞。翻譯是明確的作者操作，不會在輸入或儲存時自動觸發。"
              />

              <Form
                form={settingsForm}
                layout="vertical"
                onFinish={async (values) => {
                  const response = await updateAdminTranslationSettings(values);
                  if (!response.success || !response.data) {
                    message.error(response.message || '儲存翻譯設定失敗');
                    return;
                  }
                  message.success('翻譯設定已更新');
                  settingsRequest.refresh();
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
                  extra="系統會依照這個順序嘗試翻譯；前一個引擎失敗時，才會自然切換到下一個。"
                >
                  <Select
                    mode="multiple"
                    options={ENGINE_OPTIONS}
                    placeholder="請選擇並排序翻譯引擎"
                  />
                </Form.Item>

                <Form.Item
                  name="overwriteFilledLocales"
                  label="預設覆蓋策略"
                  valuePropName="checked"
                  extra="開啟後，「全部重新翻譯」會直接覆蓋既有欄位；關閉時只補空白欄位。"
                >
                  <Switch checkedChildren="允許覆蓋" unCheckedChildren="僅補空白" />
                </Form.Item>

                <Space>
                  <Button type="primary" htmlType="submit">
                    儲存設定
                  </Button>
                  <Button onClick={() => settingsRequest.refresh()} icon={<SyncOutlined />}>
                    重新讀取
                  </Button>
                </Space>
              </Form>
            </Space>
          </Card>
        </Col>

        <Col xs={24} xl={10}>
          <Card title="橋接服務狀態" bordered={false}>
            {settings ? (
              <Descriptions column={1} size="small" bordered>
                <Descriptions.Item label="翻譯橋接">
                  {settings.bridgeEnabled ? <Tag color="success">已啟用</Tag> : <Tag color="default">未啟用</Tag>}
                </Descriptions.Item>
                <Descriptions.Item label="請求逾時">
                  {settings.requestTimeoutMs} ms
                </Descriptions.Item>
                <Descriptions.Item label="單次最大字數">
                  {settings.maxTextLength}
                </Descriptions.Item>
                <Descriptions.Item label="目前主欄位語言">
                  {LOCALE_LABELS[settings.primaryAuthoringLocale]}
                </Descriptions.Item>
                <Descriptions.Item label="目前引擎順序">
                  <Space wrap>
                    {settings.enginePriority.map((engine) => (
                      <Tag key={engine}>{engine}</Tag>
                    ))}
                  </Space>
                </Descriptions.Item>
                <Descriptions.Item label="Bridge Script">
                  <Text code>{settings.bridgeScriptPath || '未公開'}</Text>
                </Descriptions.Item>
              </Descriptions>
            ) : (
              <Result status="info" title="尚未讀取到翻譯設定" />
            )}

            <Alert
              type="warning"
              showIcon
              style={{ marginTop: 16 }}
              message="安全說明"
              description="這裡只顯示橋接健康資訊與可安全公開的執行參數，不會暴露任何第三方平台密鑰。詳細錯誤請到後端日誌查看。"
            />
          </Card>
        </Col>

        <Col span={24}>
          <Card title="翻譯測試台" bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Paragraph style={{ marginBottom: 0 }}>
                在正式編輯內容之前，可以先用這個測試台驗證目前的主欄位語言與引擎優先序是否符合預期。
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
                      rules={[{ required: true, message: '請選擇至少一個目標語言' }]}
                    >
                      <Select mode="multiple" options={localeOptions} />
                    </Form.Item>
                  </Col>
                  <Col xs={24} md={6}>
                    <Form.Item
                      name="overwriteFilledLocales"
                      label="覆蓋策略"
                      valuePropName="checked"
                    >
                      <Switch checkedChildren="覆蓋" unCheckedChildren="補空白" />
                    </Form.Item>
                  </Col>
                </Row>

                <Form.Item
                  name="text"
                  label="待翻譯內容"
                  rules={[{ required: true, message: '請輸入測試內容' }]}
                >
                  <Input.TextArea rows={4} placeholder="請輸入要送往翻譯橋接服務的文字內容" />
                </Form.Item>

                <Space>
                  <Button type="primary" htmlType="submit" icon={<TranslationOutlined />} loading={labSubmitting}>
                    執行翻譯測試
                  </Button>
                  <Button onClick={() => { labForm.resetFields(); setLabResults([]); }}>
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
