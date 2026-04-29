import React, { useEffect, useMemo, useState } from 'react';
import { useRequest } from 'ahooks';
import { App as AntdApp, Button, Card, Col, Form, Input, InputNumber, Row, Select, Space, Tag, Typography } from 'antd';
import type { FormInstance } from 'antd';
import {
  getAiInventory,
  getAiVoices,
  previewAiVoice,
  syncAiProviderVoices,
  type AiGenerationJobPayload,
  type AiInventoryItem,
  type AiVoiceItem,
} from '../../services/api';

const { Paragraph, Text } = Typography;

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

function normalizeLanguageOptions(voice?: AiVoiceItem) {
  const codes = voice?.languageCodes?.length ? voice.languageCodes : ['zh'];
  return codes.map((code) => ({
    value: code,
    label: LANGUAGE_LABELS[code] || code.toUpperCase(),
  }));
}

function safeParsePayload(raw?: string) {
  if (!raw) {
    return {};
  }
  try {
    return JSON.parse(raw) as Record<string, unknown>;
  } catch {
    return {};
  }
}

function buildPayload(config: {
  voiceCode?: string;
  languageCode?: string;
  instruction?: string;
  format?: string;
  sampleRate?: number;
  rate?: number;
  pitch?: number;
  volume?: number;
}) {
  const payload: Record<string, unknown> = {};
  if (config.voiceCode) {
    payload.voice = config.voiceCode;
  }
  if (config.languageCode) {
    payload.languageCode = config.languageCode;
  }
  if (config.instruction) {
    payload.instruction = config.instruction;
  }
  if (config.format) {
    payload.format = config.format;
  }
  if (config.sampleRate) {
    payload.sampleRate = config.sampleRate;
  }
  if (config.rate != null) {
    payload.rate = config.rate;
  }
  if (config.pitch != null) {
    payload.pitch = config.pitch;
  }
  if (config.volume != null) {
    payload.volume = config.volume;
  }
  return payload;
}

export interface AiTtsWorkbenchFieldsProps {
  form: FormInstance<AiGenerationJobPayload>;
  capabilityCode?: string;
}

const AiTtsWorkbenchFields: React.FC<AiTtsWorkbenchFieldsProps> = ({ form, capabilityCode }) => {
  const { message } = AntdApp.useApp();
  const providerId = Form.useWatch('providerId', form);
  const inventoryId = Form.useWatch('inventoryId', form);
  const generationType = Form.useWatch('generationType', form);
  const promptText = Form.useWatch('promptText', form);
  const requestPayloadJson = Form.useWatch('requestPayloadJson', form);

  const [voiceCode, setVoiceCode] = useState<string>();
  const [languageCode, setLanguageCode] = useState<string>('zh');
  const [instruction, setInstruction] = useState<string>();
  const [format, setFormat] = useState<string>('mp3');
  const [sampleRate, setSampleRate] = useState<number>(24000);
  const [rate, setRate] = useState<number>(1);
  const [pitch, setPitch] = useState<number>(1);
  const [volume, setVolume] = useState<number>(50);
  const [previewUrl, setPreviewUrl] = useState<string>();
  const [previewing, setPreviewing] = useState(false);
  const [syncing, setSyncing] = useState(false);

  const inventoryReq = useRequest(
    () =>
      getAiInventory({
        providerId,
        capabilityCode: capabilityCode || 'admin_tts_generation',
      }),
    {
      ready: !!providerId && generationType === 'tts',
      refreshDeps: [providerId, capabilityCode, generationType],
    },
  );

  const ttsModels = useMemo(
    () =>
      (inventoryReq.data?.data || []).filter(
        (item) =>
          (item.modalityCodes || []).includes('audio') ||
          (item.capabilityCodes || []).includes('admin_tts_generation'),
      ),
    [inventoryReq.data?.data],
  );

  const selectedModel = useMemo(
    () => ttsModels.find((item) => item.id === inventoryId),
    [ttsModels, inventoryId],
  );

  const modelCode = selectedModel?.inventoryCode;

  const voicesReq = useRequest(
    () =>
      getAiVoices({
        providerId,
        modelCode,
      }),
    {
      ready: !!providerId && !!modelCode && generationType === 'tts',
      refreshDeps: [providerId, modelCode, generationType],
    },
  );

  const voices = voicesReq.data?.data || [];
  const selectedVoice = useMemo(
    () => voices.find((item) => item.voiceCode === voiceCode),
    [voices, voiceCode],
  );

  useEffect(() => {
    if (generationType !== 'tts') {
      return;
    }
    const parsed = safeParsePayload(requestPayloadJson);
    setVoiceCode(typeof parsed.voice === 'string' ? parsed.voice : undefined);
    setLanguageCode(typeof parsed.languageCode === 'string' ? parsed.languageCode : 'zh');
    setInstruction(typeof parsed.instruction === 'string' ? parsed.instruction : undefined);
    setFormat(typeof parsed.format === 'string' ? parsed.format : 'mp3');
    setSampleRate(typeof parsed.sampleRate === 'number' ? parsed.sampleRate : 24000);
    setRate(typeof parsed.rate === 'number' ? parsed.rate : 1);
    setPitch(typeof parsed.pitch === 'number' ? parsed.pitch : 1);
    setVolume(typeof parsed.volume === 'number' ? parsed.volume : 50);
  }, [generationType, requestPayloadJson]);

  useEffect(() => {
    if (!voices.length) {
      setVoiceCode(undefined);
      return;
    }
    if (voiceCode && voices.some((item) => item.voiceCode === voiceCode)) {
      return;
    }
    const firstVoice = voices[0];
    setVoiceCode(firstVoice.voiceCode);
    if (firstVoice.languageCodes?.length) {
      setLanguageCode(firstVoice.languageCodes[0]);
    }
  }, [voiceCode, voices]);

  useEffect(() => {
    if (generationType !== 'tts') {
      return;
    }
    const nextPayload = buildPayload({
      voiceCode,
      languageCode,
      instruction,
      format,
      sampleRate,
      rate,
      pitch,
      volume,
    });
    const serialized = JSON.stringify(nextPayload);
    if (serialized !== requestPayloadJson) {
      form.setFieldValue('requestPayloadJson', serialized);
    }
  }, [
    form,
    format,
    generationType,
    instruction,
    languageCode,
    pitch,
    rate,
    requestPayloadJson,
    sampleRate,
    voiceCode,
    volume,
  ]);

  const voiceOptions = useMemo(
    () =>
      voices.map((item) => ({
        value: item.voiceCode,
        label: `${item.displayName} (${item.voiceCode})`,
      })),
    [voices],
  );

  const languageOptions = useMemo(
    () => normalizeLanguageOptions(selectedVoice),
    [selectedVoice],
  );

  const handleSyncVoices = async () => {
    if (!providerId || !modelCode) {
      message.warning('請先選擇供應商與語音模型');
      return;
    }
    setSyncing(true);
    try {
      const response = await syncAiProviderVoices(providerId, { modelCode });
      if (!response.success) {
        throw new Error(response.message || '同步音色失敗');
      }
      await voicesReq.refresh();
      message.success(`已同步 ${response.data?.length || 0} 個音色`);
    } catch (error: any) {
      message.error(error?.message || '同步音色失敗');
    } finally {
      setSyncing(false);
    }
  };

  const handlePreview = async () => {
    if (!providerId || !modelCode || !voiceCode) {
      message.warning('請先選擇音色');
      return;
    }
    setPreviewing(true);
    try {
      const response = await previewAiVoice({
        providerId,
        modelCode,
        voiceCode,
        languageCode,
        instruction,
        format,
        sampleRate,
        rate,
        pitch,
        volume,
        scriptText: promptText,
      });
      if (!response.success || !response.data?.previewUrl) {
        throw new Error(response.message || '生成試聽失敗');
      }
      setPreviewUrl(response.data.previewUrl);
      message.success('已生成試聽');
    } catch (error: any) {
      message.error(error?.message || '生成試聽失敗');
    } finally {
      setPreviewing(false);
    }
  };

  if (generationType !== 'tts') {
    return null;
  }

  return (
    <Card
      size="small"
      title="語音模型與音色設定"
      extra={
        <Space>
          <Button onClick={() => void handleSyncVoices()} loading={syncing} disabled={!providerId || !modelCode}>
            同步官方音色
          </Button>
          <Button type="primary" ghost onClick={() => void handlePreview()} loading={previewing}>
            生成試聽
          </Button>
        </Space>
      }
      style={{ marginBottom: 16, borderRadius: 18 }}
    >
      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <Paragraph type="secondary" style={{ marginBottom: 0 }}>
          先在上方選好供應商與語音模型，這裡會按模型載入對應音色、可用語言與試聽配置。
        </Paragraph>

        <Row gutter={16}>
          <Col span={12}>
            <Form.Item label="已選語音模型" style={{ marginBottom: 0 }}>
              <Select
                value={inventoryId}
                placeholder="請先在上方選擇語音模型"
                options={ttsModels.map((item: AiInventoryItem) => ({
                  value: item.id,
                  label: `${item.displayName} (${item.inventoryCode})`,
                }))}
                onChange={(value) => form.setFieldValue('inventoryId', value)}
              />
            </Form.Item>
          </Col>
          <Col span={12}>
            <Form.Item label="音色" style={{ marginBottom: 0 }}>
              <Select
                value={voiceCode}
                placeholder={modelCode ? '請選擇音色' : '請先選擇語音模型'}
                options={voiceOptions}
                onChange={(value) => {
                  setVoiceCode(value);
                  setPreviewUrl(undefined);
                }}
                disabled={!modelCode}
                showSearch
                optionFilterProp="label"
              />
            </Form.Item>
          </Col>
        </Row>

        <Row gutter={16}>
          <Col span={8}>
            <Form.Item label="輸出語言" style={{ marginBottom: 0 }}>
              <Select
                value={languageCode}
                options={languageOptions}
                onChange={setLanguageCode}
                disabled={!voiceCode}
              />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item label="音訊格式" style={{ marginBottom: 0 }}>
              <Select
                value={format}
                onChange={setFormat}
                options={[
                  { value: 'mp3', label: 'MP3' },
                  { value: 'wav', label: 'WAV' },
                  { value: 'opus', label: 'OPUS' },
                ]}
              />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item label="採樣率" style={{ marginBottom: 0 }}>
              <Select
                value={sampleRate}
                onChange={setSampleRate}
                options={[
                  { value: 24000, label: '24000 Hz' },
                  { value: 22050, label: '22050 Hz' },
                  { value: 16000, label: '16000 Hz' },
                ]}
              />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item label="語氣 / 方言指令" style={{ marginBottom: 0 }}>
          <Input.TextArea
            rows={3}
            value={instruction}
            onChange={(event) => setInstruction(event.target.value)}
            placeholder="可選填，例如：請用粵語、溫暖且有故事感的方式播報。"
          />
        </Form.Item>

        <Row gutter={16}>
          <Col span={8}>
            <Form.Item label="語速" style={{ marginBottom: 0 }}>
              <InputNumber
                min={0.5}
                max={2}
                step={0.1}
                value={rate}
                onChange={(value) => setRate(typeof value === 'number' ? value : 1)}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item label="音高" style={{ marginBottom: 0 }}>
              <InputNumber
                min={0.5}
                max={2}
                step={0.1}
                value={pitch}
                onChange={(value) => setPitch(typeof value === 'number' ? value : 1)}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>
          <Col span={8}>
            <Form.Item label="音量" style={{ marginBottom: 0 }}>
              <InputNumber
                min={0}
                max={100}
                step={1}
                value={volume}
                onChange={(value) => setVolume(typeof value === 'number' ? value : 50)}
                style={{ width: '100%' }}
              />
            </Form.Item>
          </Col>
        </Row>

        {selectedVoice ? (
          <Card size="small" style={{ borderRadius: 16, background: '#fafafa' }}>
            <Space direction="vertical" size={8} style={{ width: '100%' }}>
              <Space wrap>
                <Text strong>{selectedVoice.displayName}</Text>
                <Tag color={selectedVoice.sourceType === 'voice_clone' ? 'gold' : 'blue'}>
                  {selectedVoice.sourceType === 'voice_clone' ? '自定義音色' : '系統音色'}
                </Tag>
                {(selectedVoice.languageCodes || []).map((code) => (
                  <Tag key={code}>{LANGUAGE_LABELS[code] || code}</Tag>
                ))}
              </Space>
              <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                {selectedVoice.previewText || '可直接生成試聽，確認聲線後再提交正式生成任務。'}
              </Paragraph>
              {previewUrl || selectedVoice.previewUrl ? (
                <audio
                  controls
                  style={{ width: '100%' }}
                  src={previewUrl || selectedVoice.previewUrl}
                />
              ) : null}
            </Space>
          </Card>
        ) : (
          <Text type="secondary">當前模型尚未載入音色，可先同步官方音色目錄。</Text>
        )}
      </Space>
    </Card>
  );
};

export default AiTtsWorkbenchFields;
