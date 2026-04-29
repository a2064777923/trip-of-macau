import React, { useMemo, useState } from 'react';
import { useRequest } from 'ahooks';
import {
  App as AntdApp,
  Button,
  Card,
  Empty,
  Form,
  Input,
  Modal,
  Row,
  Col,
  Select,
  Space,
  Tabs,
  Tag,
  Typography,
} from 'antd';
import {
  createAiVoiceClone,
  deleteAiVoice,
  getAiInventory,
  getAiProviders,
  getAiVoices,
  previewAiVoice,
  refreshAiVoice,
  syncAiProviderVoices,
  type AiVoiceClonePayload,
  type AiVoiceItem,
} from '../../services/api';
import MediaAssetPickerField from '../../components/media/MediaAssetPickerField';

const { Paragraph, Text, Title } = Typography;

const LANGUAGE_LABELS: Record<string, string> = {
  zh: '普通話',
  yue: '粵語',
  en: '英文',
  pt: '葡文',
  fr: '法文',
  de: '德文',
  ja: '日文',
  ko: '韓文',
  ru: '俄文',
  th: '泰文',
  id: '印尼文',
  vi: '越南文',
};

const VoiceWorkbenchPage: React.FC = () => {
  const { message, modal } = AntdApp.useApp();
  const [cloneForm] = Form.useForm<AiVoiceClonePayload>();
  const [providerId, setProviderId] = useState<number>();
  const [modelCode, setModelCode] = useState<string>();
  const [languageCode, setLanguageCode] = useState<string>();
  const [tabKey, setTabKey] = useState<'system_catalog' | 'voice_clone'>('system_catalog');
  const [cloneOpen, setCloneOpen] = useState(false);
  const [previewUrls, setPreviewUrls] = useState<Record<string, string>>({});
  const [previewingKey, setPreviewingKey] = useState<string>();
  const [syncing, setSyncing] = useState(false);
  const [previewScript, setPreviewScript] = useState('歡迎來到澳門，這是一段用於測試音色、語言與情感表現的試聽文本。');

  const providersReq = useRequest(() => getAiProviders());
  const modelsReq = useRequest(
    () =>
      getAiInventory({
        providerId,
        capabilityCode: 'admin_tts_generation',
      }),
    { ready: !!providerId, refreshDeps: [providerId] },
  );
  const voicesReq = useRequest(
    () =>
      getAiVoices({
        providerId,
        modelCode,
        languageCode,
        sourceType: tabKey,
      }),
    {
      ready: !!providerId,
      refreshDeps: [providerId, modelCode, languageCode, tabKey],
    },
  );
  const providers = useMemo(
    () =>
      (providersReq.data?.data || []).filter(
        (item) =>
          item.endpointStyle === 'dashscope_tts' ||
          (item.capabilityCodes || []).includes('admin_tts_generation'),
      ),
    [providersReq.data?.data],
  );

  const modelOptions = useMemo(
    () =>
      (modelsReq.data?.data || [])
        .filter(
          (item) =>
            (item.modalityCodes || []).includes('audio') ||
            (item.capabilityCodes || []).includes('admin_tts_generation'),
        )
        .map((item) => ({
          value: item.inventoryCode,
          label: `${item.displayName} (${item.inventoryCode})`,
        })),
    [modelsReq.data?.data],
  );

  const voices = voicesReq.data?.data || [];

  const handleSync = async () => {
    if (!providerId) {
      message.warning('請先選擇供應商');
      return;
    }
    setSyncing(true);
    try {
      const response = await syncAiProviderVoices(providerId, modelCode ? { modelCode } : undefined);
      if (!response.success) {
        throw new Error(response.message || '同步官方音色失敗');
      }
      await voicesReq.refresh();
      message.success(`已同步 ${response.data?.length || 0} 個音色`);
    } catch (error: any) {
      message.error(error?.message || '同步官方音色失敗');
    } finally {
      setSyncing(false);
    }
  };

  const handlePreview = async (voice: AiVoiceItem) => {
    const cacheKey = `${voice.id}:${voice.voiceCode}`;
    const resolvedLanguageCode =
      languageCode && (voice.languageCodes || []).includes(languageCode)
        ? languageCode
        : voice.languageCodes?.[0] || 'zh';
    setPreviewingKey(cacheKey);
    try {
      const response = await previewAiVoice({
        providerId: voice.providerId,
        modelCode: voice.parentModelCode || modelCode || '',
        voiceCode: voice.voiceCode,
        languageCode: resolvedLanguageCode,
        scriptText: previewScript || voice.previewText || '歡迎來到澳門，這是一段語音試聽文本。',
      });
      if (!response.success || !response.data?.previewUrl) {
        throw new Error(response.message || '生成試聽失敗');
      }
      setPreviewUrls((current) => ({ ...current, [cacheKey]: response.data!.previewUrl }));
      message.success('已生成試聽');
    } catch (error: any) {
      message.error(error?.message || '生成試聽失敗');
    } finally {
      setPreviewingKey(undefined);
    }
  };

  const handleRefreshVoice = async (voiceId: number) => {
    const response = await refreshAiVoice(voiceId);
    if (!response.success) {
      message.error(response.message || '刷新自定義音色失敗');
      return;
    }
    message.success('已刷新音色狀態');
    await voicesReq.refresh();
  };

  const handleDeleteVoice = async (voice: AiVoiceItem) => {
    modal.confirm({
      title: '刪除自定義音色',
      content: `將刪除「${voice.displayName}」及其遠端音色記錄，確定繼續嗎？`,
      onOk: async () => {
        const response = await deleteAiVoice(voice.id);
        if (!response.success) {
          message.error(response.message || '刪除音色失敗');
          return;
        }
        message.success('已刪除自定義音色');
        await voicesReq.refresh();
      },
    });
  };

  const openCloneModal = () => {
    cloneForm.setFieldsValue({
      providerId,
      targetModel: modelCode,
      languageCodes: ['zh', 'yue', 'en', 'pt'],
    });
    setCloneOpen(true);
  };

  const handleCloneSubmit = async () => {
    try {
      const values = await cloneForm.validateFields();
      const response = await createAiVoiceClone(values);
      if (!response.success || !response.data) {
        throw new Error(response.message || '建立自定義音色失敗');
      }
      message.success('已提交聲音復刻任務');
      setCloneOpen(false);
      await voicesReq.refresh();
    } catch (error: any) {
      message.error(error?.message || '建立自定義音色失敗');
    }
  };

  const renderVoiceCards = (items: AiVoiceItem[]) => {
    if (!items.length) {
      return <Empty description="目前沒有符合條件的音色資料" />;
    }

    return (
      <Row gutter={[16, 16]}>
        {items.map((voice) => {
          const cacheKey = `${voice.id}:${voice.voiceCode}`;
          const audioUrl = previewUrls[cacheKey] || voice.previewUrl;
          return (
            <Col xs={24} xl={12} key={voice.id}>
              <Card style={{ borderRadius: 20, height: '100%' }}>
                <Space direction="vertical" size={12} style={{ width: '100%' }}>
                  <Space wrap>
                    <Title level={4} style={{ margin: 0 }}>
                      {voice.displayName}
                    </Title>
                    <Tag color={voice.sourceType === 'voice_clone' ? 'gold' : 'blue'}>
                      {voice.sourceType === 'voice_clone' ? '自定義音色' : '系統音色'}
                    </Tag>
                    <Tag color={voice.availabilityStatus === 'available' ? 'green' : 'gold'}>
                      {voice.availabilityStatus || 'unknown'}
                    </Tag>
                    {voice.cloneStatus ? <Tag>{voice.cloneStatus}</Tag> : null}
                  </Space>

                  <Text type="secondary">
                    {voice.parentModelCode || '-'} / {voice.voiceCode}
                  </Text>

                  <Space wrap>
                    {(voice.languageCodes || []).map((code) => (
                      <Tag key={code}>{LANGUAGE_LABELS[code] || code}</Tag>
                    ))}
                  </Space>

                  <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                    {voice.previewText || '可直接試聽後選用，生成時會回填所選音色代碼。'}
                  </Paragraph>

                  {audioUrl ? <audio controls style={{ width: '100%' }} src={audioUrl} /> : null}

                  <Space wrap>
                    <Button
                      onClick={() => void handlePreview(voice)}
                      loading={previewingKey === cacheKey}
                    >
                      生成試聽
                    </Button>
                    {voice.sourceType === 'voice_clone' ? (
                      <>
                        <Button onClick={() => void handleRefreshVoice(voice.id)}>刷新狀態</Button>
                        <Button danger onClick={() => void handleDeleteVoice(voice)}>
                          刪除
                        </Button>
                      </>
                    ) : null}
                  </Space>
                </Space>
              </Card>
            </Col>
          );
        })}
      </Row>
    );
  };

  return (
    <>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card style={{ borderRadius: 22 }}>
          <Title level={4} style={{ marginTop: 0 }}>
            音色與聲音工坊
          </Title>
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            在這裡按語音模型同步官方音色、快速試聽、設定語言輸出，並將自定義復刻音色保存為個人可重用資源。
          </Paragraph>
        </Card>

        <Card
          title="篩選與操作"
          extra={
            <Space>
              <Button onClick={() => void handleSync()} loading={syncing} disabled={!providerId}>
                同步官方音色
              </Button>
              <Button type="primary" onClick={openCloneModal} disabled={!providerId}>
                聲音復刻
              </Button>
            </Space>
          }
          style={{ borderRadius: 22 }}
        >
          <Space direction="vertical" size={12} style={{ width: '100%' }}>
            <Space wrap size={[12, 12]} style={{ width: '100%' }}>
              <Select
                allowClear
                style={{ width: 240 }}
                placeholder="選擇供應商"
                options={providers.map((item) => ({
                  value: item.id,
                  label: item.displayName,
                }))}
                value={providerId}
                onChange={(value) => {
                  setProviderId(value);
                  setModelCode(undefined);
                }}
              />
              <Select
                allowClear
                style={{ width: 280 }}
                placeholder="選擇語音模型"
                options={modelOptions}
                value={modelCode}
                onChange={(value) => setModelCode(value)}
                disabled={!providerId}
              />
              <Select
                allowClear
                style={{ width: 220 }}
                placeholder="試聽語言 / 篩選"
                value={languageCode}
                onChange={(value) => setLanguageCode(value)}
                options={Object.entries(LANGUAGE_LABELS).map(([value, label]) => ({ value, label }))}
              />
            </Space>
            <Input.TextArea
              rows={2}
              value={previewScript}
              onChange={(event) => setPreviewScript(event.target.value)}
              placeholder="輸入你想用來試聽的文本，例如普通話、粵語、英文或葡文導覽句子。"
            />
          </Space>
        </Card>

        <Card style={{ borderRadius: 22 }}>
          <Tabs
            activeKey={tabKey}
            onChange={(value) => setTabKey(value as 'system_catalog' | 'voice_clone')}
            items={[
              {
                key: 'system_catalog',
                label: `系統音色 (${tabKey === 'system_catalog' ? voices.length : ''})`,
                children: renderVoiceCards(voices),
              },
              {
                key: 'voice_clone',
                label: `自定義音色 (${tabKey === 'voice_clone' ? voices.length : ''})`,
                children: renderVoiceCards(voices),
              },
            ]}
          />
        </Card>
      </Space>

      <Modal
        open={cloneOpen}
        width={720}
        title="聲音復刻"
        onCancel={() => setCloneOpen(false)}
        onOk={() => void handleCloneSubmit()}
        okText="提交復刻"
      >
        <Form form={cloneForm} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="providerId"
                label="供應商"
                rules={[{ required: true, message: '請選擇供應商' }]}
              >
                <Select
                  options={providers.map((item) => ({
                    value: item.id,
                    label: item.displayName,
                  }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="targetModel"
                label="目標語音模型"
                rules={[{ required: true, message: '請選擇目標模型' }]}
              >
                <Select options={modelOptions} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="voiceName"
            label="自定義音色名稱"
            rules={[{ required: true, message: '請輸入音色名稱' }]}
          >
            <Input placeholder="例如：澳門歷史旁白男聲" />
          </Form.Item>

          <MediaAssetPickerField
            name="sourceAssetId"
            label="音頻資源"
            assetKind="audio"
            placeholder="可直接選擇、拖入或上傳音頻資源"
            help="支援本地選檔、拖拽上傳與自動回填；若已選音頻資源，提交復刻時會優先使用該資源。"
            uploadSource="ai-voice-clone"
            onValueChange={(value) => {
              if (value !== null && value !== undefined && value !== '') {
                cloneForm.setFieldValue('sourceUrl', undefined);
                void cloneForm.validateFields(['sourceUrl']).catch(() => undefined);
              }
            }}
          />

          <Form.Item
            name="sourceUrl"
            label="或輸入公開音頻 URL"
            rules={[
              {
                validator: async (_, value) => {
                  const sourceAssetId = cloneForm.getFieldValue('sourceAssetId');
                  if (sourceAssetId || (typeof value === 'string' && value.trim())) {
                    return;
                  }
                  throw new Error('請至少選擇一個音頻資源或輸入可訪問的 URL');
                },
              },
            ]}
          >
            <Input placeholder="若未選擇資源，可直接輸入可公開訪問的音頻 URL" />
          </Form.Item>

          <Form.Item name="previewText" label="試聽文本">
            <Input.TextArea
              rows={3}
              placeholder="建立後可用這段文本快速生成試聽，例如景點導覽或活動旁白。"
            />
          </Form.Item>

          <Form.Item name="languageCodes" label="預設支援語言">
            <Select
              mode="multiple"
              options={Object.entries(LANGUAGE_LABELS).map(([value, label]) => ({ value, label }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default VoiceWorkbenchPage;
