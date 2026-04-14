import React, { useEffect, useMemo, useState } from 'react';
import { Alert, Card, DatePicker, Form, Input, InputNumber, Select, Space, Typography } from 'antd';
import type { FormInstance } from 'antd/es/form';
import dayjs from 'dayjs';

const { Paragraph, Text } = Typography;

type NamePath = string | number | Array<string | number>;
type UnlockPreset = 'progress' | 'storyline' | 'collectible' | 'schedule' | 'custom';

interface SpatialUnlockConditionFieldProps {
  form: FormInstance;
  unlockTypeName: NamePath;
  unlockConditionName: NamePath;
}

interface UnlockConditionShape {
  conditionType?: string;
  minProgress?: number;
  requiredStorylineCodes?: string[];
  requiredCollectibleCodes?: string[];
  unlockAt?: string;
}

const presetOptions = [
  { label: '探索度達標', value: 'progress' },
  { label: '完成指定故事線', value: 'storyline' },
  { label: '持有指定收集物 / 徽章', value: 'collectible' },
  { label: '指定時間解鎖', value: 'schedule' },
  { label: '自定義 JSON', value: 'custom' },
];

function parseCondition(rawValue?: string): UnlockConditionShape | null {
  if (!rawValue?.trim()) {
    return null;
  }
  try {
    return JSON.parse(rawValue) as UnlockConditionShape;
  } catch {
    return null;
  }
}

function inferPreset(rawValue?: string, parsed?: UnlockConditionShape | null): UnlockPreset {
  if (!rawValue?.trim()) {
    return 'progress';
  }
  if (!parsed) {
    return 'custom';
  }
  switch (parsed.conditionType) {
    case 'exploration_progress':
      return 'progress';
    case 'storyline_completion':
      return 'storyline';
    case 'collectible_gate':
      return 'collectible';
    case 'schedule_unlock':
      return 'schedule';
    default:
      return 'custom';
  }
}

function stringifyCondition(condition: UnlockConditionShape) {
  return JSON.stringify(condition, null, 2);
}

function defaultConditionForPreset(preset: UnlockPreset): UnlockConditionShape {
  switch (preset) {
    case 'storyline':
      return {
        conditionType: 'storyline_completion',
        requiredStorylineCodes: ['macau-war-route'],
      };
    case 'collectible':
      return {
        conditionType: 'collectible_gate',
        requiredCollectibleCodes: ['first-visit-stamp'],
      };
    case 'schedule':
      return {
        conditionType: 'schedule_unlock',
        unlockAt: dayjs().add(1, 'day').startOf('day').format('YYYY-MM-DDTHH:mm:ss'),
      };
    case 'custom':
      return {
        conditionType: 'custom',
      };
    case 'progress':
    default:
      return {
        conditionType: 'exploration_progress',
        minProgress: 80,
      };
  }
}

const SpatialUnlockConditionField: React.FC<SpatialUnlockConditionFieldProps> = ({
  form,
  unlockTypeName,
  unlockConditionName,
}) => {
  const unlockType = Form.useWatch(unlockTypeName, form) as string | undefined;
  const rawValue = Form.useWatch(unlockConditionName, form) as string | undefined;
  const parsed = useMemo(() => parseCondition(rawValue), [rawValue]);
  const [preset, setPreset] = useState<UnlockPreset>(inferPreset(rawValue, parsed));

  useEffect(() => {
    setPreset(inferPreset(rawValue, parsed));
  }, [parsed, rawValue]);

  const updateCondition = (next: UnlockConditionShape) => {
    form.setFieldValue(unlockConditionName, stringifyCondition(next));
  };

  if (unlockType !== 'condition') {
    return (
      <Alert
        type="info"
        showIcon
        message="目前未使用條件解鎖"
        description="若切換為「條件解鎖」，可使用下方預設表單生成 JSON，不必手動書寫。"
        style={{ marginBottom: 24 }}
      />
    );
  }

  const condition = parsed || defaultConditionForPreset(preset);

  return (
    <Card size="small" title="解鎖條件設定" style={{ marginBottom: 24 }}>
      <Space direction="vertical" size={16} style={{ width: '100%' }}>
        <Paragraph type="secondary" style={{ marginBottom: 0 }}>
          優先用預設方式配置。只有在條件比較特殊時，才切換到「自定義 JSON」。
        </Paragraph>

        <Form.Item label="條件預設" style={{ marginBottom: 0 }}>
          <Select
            options={presetOptions}
            value={preset}
            onChange={(value: UnlockPreset) => {
              setPreset(value);
              updateCondition(defaultConditionForPreset(value));
            }}
          />
        </Form.Item>

        {preset === 'progress' ? (
          <Form.Item label="至少達到的探索度 (%)" style={{ marginBottom: 0 }}>
            <InputNumber
              min={0}
              max={100}
              style={{ width: '100%' }}
              value={condition.minProgress}
              onChange={(value) =>
                updateCondition({
                  conditionType: 'exploration_progress',
                  minProgress: typeof value === 'number' ? value : 0,
                })
              }
            />
          </Form.Item>
        ) : null}

        {preset === 'storyline' ? (
          <Form.Item label="需完成的故事線代碼" style={{ marginBottom: 0 }}>
            <Select
              mode="tags"
              placeholder="例如 macau-war-route"
              value={condition.requiredStorylineCodes || []}
              onChange={(value) =>
                updateCondition({
                  conditionType: 'storyline_completion',
                  requiredStorylineCodes: value,
                })
              }
            />
          </Form.Item>
        ) : null}

        {preset === 'collectible' ? (
          <Form.Item label="需持有的收集物 / 徽章代碼" style={{ marginBottom: 0 }}>
            <Select
              mode="tags"
              placeholder="例如 first-visit-stamp"
              value={condition.requiredCollectibleCodes || []}
              onChange={(value) =>
                updateCondition({
                  conditionType: 'collectible_gate',
                  requiredCollectibleCodes: value,
                })
              }
            />
          </Form.Item>
        ) : null}

        {preset === 'schedule' ? (
          <Form.Item label="解鎖時間" style={{ marginBottom: 0 }}>
            <DatePicker
              showTime
              style={{ width: '100%' }}
              value={condition.unlockAt ? dayjs(condition.unlockAt) : null}
              onChange={(value) =>
                updateCondition({
                  conditionType: 'schedule_unlock',
                  unlockAt: value ? value.format('YYYY-MM-DDTHH:mm:ss') : undefined,
                })
              }
            />
          </Form.Item>
        ) : null}

        {preset === 'custom' ? (
          <Form.Item name={unlockConditionName} label="自定義 JSON" style={{ marginBottom: 0 }}>
            <Input.TextArea
              rows={6}
              placeholder='例如 {"conditionType":"custom","requiresProgress":80}'
            />
          </Form.Item>
        ) : (
          <Alert
            type="success"
            showIcon
            message="目前將儲存為 JSON"
            description={
              <Space direction="vertical" size={4}>
                <Text>這份 JSON 會直接送到後端保存：</Text>
                <Text code>{stringifyCondition(condition)}</Text>
              </Space>
            }
          />
        )}
      </Space>
    </Card>
  );
};

export default SpatialUnlockConditionField;
