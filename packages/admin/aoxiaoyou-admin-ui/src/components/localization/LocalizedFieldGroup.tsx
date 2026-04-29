import React, { useEffect, useMemo, useState } from 'react';
import { CopyOutlined, TranslationOutlined } from '@ant-design/icons';
import {
  App as AntdApp,
  Alert,
  Button,
  Card,
  Divider,
  Form,
  Input,
  Modal,
  Segmented,
  Space,
  Tag,
  Typography,
} from 'antd';
import type { FormInstance, Rule } from 'antd/es/form';
import { translateAdminText } from '../../services/api';
import type { AdminTranslationSettings, SupportedLocale } from '../../types/admin';
import { findSuspiciousTextIssue } from '../../utils/textEncodingGuard';

const { Paragraph, Text } = Typography;

export const SUPPORTED_LOCALES: SupportedLocale[] = ['zh-Hant', 'zh-Hans', 'en', 'pt'];

export const LOCALE_LABELS: Record<SupportedLocale, string> = {
  'zh-Hant': '繁體中文',
  'zh-Hans': '簡體中文',
  en: 'English',
  pt: 'Português',
};

export const LOCALE_SHORT_LABELS: Record<SupportedLocale, string> = {
  'zh-Hant': '繁中',
  'zh-Hans': '簡中',
  en: 'EN',
  pt: 'PT',
};

type LocaleStatusMode = 'manual' | 'machine' | 'error';

interface LocaleStatusMeta {
  mode?: LocaleStatusMode;
  engine?: string;
  message?: string;
  translating?: boolean;
}

export interface LocalizedFieldNames {
  'zh-Hant': string;
  'zh-Hans': string;
  en: string;
  pt: string;
}

interface LocalizedFieldGroupProps {
  form: FormInstance;
  label: string;
  fieldNames: LocalizedFieldNames;
  help?: string;
  required?: boolean;
  multiline?: boolean;
  rows?: number;
  placeholder?: string;
  translationDefaults?: Partial<AdminTranslationSettings>;
}

function hasText(value?: string | null): value is string {
  return typeof value === 'string' && value.trim().length > 0;
}

function fallbackOrder(locale: SupportedLocale): SupportedLocale[] {
  switch (locale) {
    case 'zh-Hant':
      return ['zh-Hant', 'zh-Hans', 'en', 'pt'];
    case 'zh-Hans':
      return ['zh-Hans', 'zh-Hant', 'en', 'pt'];
    case 'en':
      return ['en', 'pt', 'zh-Hant', 'zh-Hans'];
    case 'pt':
      return ['pt', 'en', 'zh-Hant', 'zh-Hans'];
    default:
      return ['zh-Hant', 'zh-Hans', 'en', 'pt'];
  }
}

function resolvePreview(locale: SupportedLocale, values: Partial<Record<SupportedLocale, string>>) {
  const order = fallbackOrder(locale);
  for (const candidate of order) {
    if (hasText(values[candidate])) {
      return {
        text: values[candidate]!.trim(),
        resolvedLocale: candidate,
      };
    }
  }

  return {
    text: '',
    resolvedLocale: locale,
  };
}

function buildRequiredRules(label: string): Rule[] {
  return [{ required: true, message: `請先填寫${label}的主要語言內容` }];
}

function createEmptyMeta(): Record<SupportedLocale, LocaleStatusMeta> {
  return {
    'zh-Hant': {},
    'zh-Hans': {},
    en: {},
    pt: {},
  };
}

export function buildLocalizedFieldNames(baseName: string): LocalizedFieldNames {
  return {
    'zh-Hant': `${baseName}Zht`,
    'zh-Hans': `${baseName}Zh`,
    en: `${baseName}En`,
    pt: `${baseName}Pt`,
  };
}

const LocalizedFieldGroup: React.FC<LocalizedFieldGroupProps> = ({
  form,
  label,
  fieldNames,
  help,
  required,
  multiline,
  rows = 4,
  placeholder,
  translationDefaults,
}) => {
  const { message } = AntdApp.useApp();
  const primaryLocale = translationDefaults?.primaryAuthoringLocale || 'zh-Hant';
  const sourceLocale: SupportedLocale = primaryLocale;
  const overwriteDefault = !!translationDefaults?.overwriteFilledLocales;
  const enginePriority = translationDefaults?.enginePriority || [];

  const [previewLocale, setPreviewLocale] = useState<SupportedLocale>(primaryLocale);
  const [localeMeta, setLocaleMeta] = useState<Record<SupportedLocale, LocaleStatusMeta>>(createEmptyMeta());

  const zhHantValue = Form.useWatch(fieldNames['zh-Hant'], form);
  const zhHansValue = Form.useWatch(fieldNames['zh-Hans'], form);
  const enValue = Form.useWatch(fieldNames.en, form);
  const ptValue = Form.useWatch(fieldNames.pt, form);

  const localeValues = useMemo(
    () => ({
      'zh-Hant': zhHantValue,
      'zh-Hans': zhHansValue,
      en: enValue,
      pt: ptValue,
    }),
    [enValue, ptValue, zhHansValue, zhHantValue],
  );

  useEffect(() => {
    setPreviewLocale(primaryLocale);
    setLocaleMeta(createEmptyMeta());
  }, [fieldNames.en, fieldNames.pt, fieldNames['zh-Hans'], fieldNames['zh-Hant'], primaryLocale]);

  const preview = useMemo(
    () => resolvePreview(previewLocale, localeValues),
    [localeValues, previewLocale],
  );

  const editorOrder = useMemo(() => {
    const rest = SUPPORTED_LOCALES.filter((locale) => locale !== primaryLocale);
    return [primaryLocale, ...rest];
  }, [primaryLocale]);

  const previewFallbackNote =
    preview.text && preview.resolvedLocale !== previewLocale
      ? `目前預覽使用 ${LOCALE_LABELS[preview.resolvedLocale]} 欄位回退`
      : '目前顯示的是所選語言欄位內容';

  const setMetaForLocales = (locales: SupportedLocale[], next: Partial<LocaleStatusMeta>) => {
    setLocaleMeta((current) => {
      const updated = { ...current };
      locales.forEach((locale) => {
        updated[locale] = { ...updated[locale], ...next };
      });
      return updated;
    });
  };

  const onManualChange = (locale: SupportedLocale) => {
    setLocaleMeta((current) => ({
      ...current,
      [locale]: {
        mode: 'manual',
        translating: false,
        message: undefined,
      },
    }));
  };

  const applyValues = (values: Partial<Record<SupportedLocale, string>>) => {
    const nextValues: Record<string, string> = {};
    Object.entries(values).forEach(([locale, value]) => {
      if (locale in fieldNames && typeof value === 'string') {
        nextValues[fieldNames[locale as SupportedLocale]] = value;
      }
    });
    form.setFieldsValue(nextValues);
  };

  const runTranslation = async (targetLocales: SupportedLocale[], overwriteFilledLocales: boolean) => {
    const sourceText = localeValues[sourceLocale];

    if (!hasText(sourceText)) {
      message.warning(`請先填寫 ${LOCALE_LABELS[sourceLocale]} 內容，再進行翻譯`);
      return;
    }

    if (!targetLocales.length) {
      message.info('目前沒有需要翻譯的語言欄位');
      return;
    }

    setMetaForLocales(targetLocales, {
      translating: true,
      mode: undefined,
      message: undefined,
      engine: undefined,
    });

    try {
      const response = await translateAdminText({
        sourceLocale,
        targetLocales,
        text: sourceText.trim(),
        enginePriority,
        overwriteFilledLocales,
        existingTranslations: localeValues,
      });

      if (!response.success || !response.data) {
        throw new Error(response.message || '翻譯服務暫時不可用');
      }

      const translatedValues: Partial<Record<SupportedLocale, string>> = {};
      const successLocales: SupportedLocale[] = [];
      const suspiciousResults: Partial<Record<SupportedLocale, string>> = {};

      response.data.results.forEach((result) => {
        if (result.status === 'success' && hasText(result.translatedText)) {
          const suspiciousIssue = findSuspiciousTextIssue({ translatedText: result.translatedText });
          if (suspiciousIssue) {
            suspiciousResults[result.targetLocale] =
              `Translation result was rejected because it looks corrupted (${suspiciousIssue.reason}).`;
            return;
          }
          translatedValues[result.targetLocale] = result.translatedText.trim();
          successLocales.push(result.targetLocale);
        }
      });

      if (Object.keys(translatedValues).length > 0) {
        applyValues(translatedValues);
      }

      setLocaleMeta((current) => {
        const updated = { ...current };
        targetLocales.forEach((locale) => {
          if (suspiciousResults[locale]) {
            updated[locale] = {
              mode: 'error',
              translating: false,
              message: suspiciousResults[locale],
            };
            return;
          }
          const result = response.data.results.find((item) => item.targetLocale === locale);
          if (result?.status === 'success') {
            updated[locale] = {
              mode: 'machine',
              translating: false,
              engine: result.engine,
            };
            return;
          }

          updated[locale] = {
            mode: 'error',
            translating: false,
            engine: result?.engine,
            message: result?.message || '翻譯未完成，可稍後重試或手動補齊',
          };
        });
        return updated;
      });

      if (successLocales.length > 0) {
        message.success(`已更新 ${successLocales.length} 個語言欄位`);
      } else {
        message.warning('翻譯未產生可用內容，請檢查翻譯引擎設定後再試');
      }
      if (Object.keys(suspiciousResults).length > 0) {
        message.error('Some translation results looked corrupted and were not applied.');
      }
    } catch (error) {
      setMetaForLocales(targetLocales, {
        translating: false,
        mode: 'error',
        message: error instanceof Error ? error.message : '翻譯服務暫時不可用',
      });
      message.error(error instanceof Error ? error.message : '翻譯服務暫時不可用');
    }
  };

  const translateMissing = async () => {
    const missingLocales = SUPPORTED_LOCALES.filter(
      (locale) => locale !== sourceLocale && !hasText(localeValues[locale]),
    );
    await runTranslation(missingLocales, false);
  };

  const translateAll = async () => {
    const targetLocales = SUPPORTED_LOCALES.filter((locale) => locale !== sourceLocale);
    await new Promise<void>((resolve) => {
      Modal.confirm({
        title: '覆蓋翻譯',
        content: '這會用主欄位內容重新覆蓋其他語言欄位，是否繼續？',
        okText: '確認覆蓋',
        cancelText: '取消',
        onOk: async () => {
          await runTranslation(targetLocales, true);
          resolve();
        },
        onCancel: () => resolve(),
      });
    });
  };

  const copySourceToOthers = () => {
    const sourceText = localeValues[sourceLocale];
    if (!hasText(sourceText)) {
      message.warning('請先填寫主欄位內容');
      return;
    }

    const copiedValues: Partial<Record<SupportedLocale, string>> = {};
    SUPPORTED_LOCALES.forEach((locale) => {
      if (locale !== sourceLocale) {
        copiedValues[locale] = sourceText.trim();
      }
    });

    applyValues(copiedValues);
    setMetaForLocales(
      SUPPORTED_LOCALES.filter((locale) => locale !== sourceLocale),
      {
        mode: 'manual',
        translating: false,
        message: undefined,
        engine: undefined,
      },
    );
    message.success('已將主欄位內容複製到其他語言欄位');
  };

  const clearMachineTranslations = () => {
    const removableLocales = SUPPORTED_LOCALES.filter((locale) => localeMeta[locale].mode === 'machine');
    if (!removableLocales.length) {
      message.info('目前沒有可清除的機器翻譯內容');
      return;
    }

    const clearedValues: Partial<Record<SupportedLocale, string>> = {};
    removableLocales.forEach((locale) => {
      clearedValues[locale] = '';
    });
    applyValues(clearedValues);

    setLocaleMeta((current) => {
      const updated = { ...current };
      removableLocales.forEach((locale) => {
        updated[locale] = {};
      });
      return updated;
    });

    message.success('已清除機器翻譯內容');
  };

  const renderStatusTag = (locale: SupportedLocale) => {
    const meta = localeMeta[locale];
    if (meta.translating) {
      return <Tag color="processing">翻譯中</Tag>;
    }
    if (meta.mode === 'machine') {
      return <Tag color="purple">{meta.engine ? `機器翻譯 · ${meta.engine}` : '機器翻譯'}</Tag>;
    }
    if (meta.mode === 'manual') {
      return <Tag color="blue">手動編輯</Tag>;
    }
    if (meta.mode === 'error') {
      return <Tag color="error">翻譯失敗</Tag>;
    }
    if (hasText(localeValues[locale])) {
      return <Tag color="success">已填寫</Tag>;
    }
    return <Tag>未填寫</Tag>;
  };

  return (
    <Card
      size="small"
      styles={{ body: { padding: 20 } }}
      style={{ marginBottom: 24, background: '#fff' }}
      title={
        <Space wrap size={8}>
          <Text strong>{label}</Text>
          {required ? <Tag color="error">必填主欄位</Tag> : null}
          <Tag color="gold">主欄位：{LOCALE_LABELS[primaryLocale]}</Tag>
        </Space>
      }
      extra={
        <Space size={8} wrap>
          {SUPPORTED_LOCALES.map((locale) => (
            <Tag key={locale} color={hasText(localeValues[locale]) ? 'success' : 'default'}>
              {LOCALE_SHORT_LABELS[locale]}
            </Tag>
          ))}
        </Space>
      }
    >
      {help ? (
        <Alert type="info" showIcon style={{ marginBottom: 16 }} message={help} />
      ) : null}

      <Space size={12} wrap style={{ width: '100%', marginBottom: 12 }}>
        <Text type="secondary">翻譯來源語言</Text>
        <Tag color="gold">{LOCALE_LABELS[sourceLocale]}</Tag>
        <Button icon={<TranslationOutlined />} onClick={() => void translateMissing()}>
          以主欄位翻譯未填語言
        </Button>
        <Button onClick={() => void translateAll()}>全部重新翻譯</Button>
        <Button icon={<CopyOutlined />} onClick={copySourceToOthers}>
          複製主欄位到其他語言
        </Button>
        <Button danger type="text" onClick={clearMachineTranslations}>
          清除機器翻譯
        </Button>
      </Space>

      <Space wrap size={8}>
        <Tag color="purple">{overwriteDefault ? '覆蓋策略：允許覆蓋已有內容' : '覆蓋策略：只填空白欄位'}</Tag>
        {enginePriority.map((engine) => (
          <Tag key={engine}>{engine}</Tag>
        ))}
      </Space>

      <Divider style={{ margin: '16px 0' }} />

      <Card
        size="small"
        title="預覽與欄位結果"
        style={{ marginBottom: 16, background: '#fafcff', borderColor: '#e8ecf7' }}
      >
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Segmented
            value={previewLocale}
            onChange={(value) => setPreviewLocale(value as SupportedLocale)}
            options={SUPPORTED_LOCALES.map((locale) => ({
              label: LOCALE_SHORT_LABELS[locale],
              value: locale,
            }))}
          />
          <Text type="secondary">{previewFallbackNote}</Text>
          {preview.text ? (
            <Paragraph style={{ marginBottom: 0, whiteSpace: 'pre-wrap' }}>{preview.text}</Paragraph>
          ) : (
            <Text type="secondary">尚未建立多語內容，請先填寫主欄位後再補齊其他語言。</Text>
          )}
        </Space>
      </Card>

      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        {editorOrder.map((locale) => {
          const isPrimary = locale === primaryLocale;
          const meta = localeMeta[locale];

          return (
            <Card
              key={locale}
              size="small"
              style={{
                borderColor: isPrimary ? '#bda8ff' : '#edf0f7',
                background: isPrimary ? '#fbf8ff' : '#fff',
              }}
              title={
                <Space wrap size={8}>
                  <Text strong>{LOCALE_LABELS[locale]}</Text>
                  {isPrimary ? <Tag color="gold">主要編輯語言</Tag> : null}
                  {renderStatusTag(locale)}
                </Space>
              }
            >
              <Form.Item
                name={fieldNames[locale]}
                rules={isPrimary && required ? buildRequiredRules(label) : undefined}
                style={{ marginBottom: meta.mode === 'error' ? 8 : 0 }}
              >
                {multiline ? (
                  <Input.TextArea
                    rows={rows}
                    placeholder={placeholder || `請輸入${LOCALE_LABELS[locale]}內容`}
                    onChange={() => onManualChange(locale)}
                  />
                ) : (
                  <Input
                    placeholder={placeholder || `請輸入${LOCALE_LABELS[locale]}內容`}
                    onChange={() => onManualChange(locale)}
                  />
                )}
              </Form.Item>
              {meta.mode === 'error' && meta.message ? (
                <Text type="danger">{meta.message}</Text>
              ) : null}
            </Card>
          );
        })}
      </Space>
    </Card>
  );
};

export default LocalizedFieldGroup;
