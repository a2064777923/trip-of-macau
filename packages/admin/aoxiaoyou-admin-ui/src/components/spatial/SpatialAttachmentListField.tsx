import React from 'react';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import { Button, Card, Form, InputNumber, Select, Space, Typography } from 'antd';
import SpatialAssetPickerField from './SpatialAssetPickerField';

type NamePath = string | number | Array<string | number>;

const { Text } = Typography;

const usageOptions = [
  { label: '圖集 / 附件集', value: 'gallery' },
  { label: '彈窗 / 說明', value: 'popup' },
  { label: '音訊', value: 'audio' },
  { label: '影片', value: 'video' },
  { label: '地圖圖示', value: 'map-icon' },
  { label: '封面', value: 'cover' },
];

const statusOptions = [
  { label: '編輯中', value: 'draft' },
  { label: '已發佈', value: 'published' },
  { label: '已封存', value: 'archived' },
];

interface SpatialAttachmentListFieldProps {
  name: NamePath;
  title?: string;
}

function toNamePathArray(name: NamePath) {
  return Array.isArray(name) ? name : [name];
}

function inferAssetKindFromUsage(usageType?: string) {
  switch (usageType) {
    case 'audio':
      return 'audio';
    case 'video':
      return 'video';
    case 'map-icon':
      return 'icon';
    case 'cover':
      return 'image';
    case 'gallery':
    case 'popup':
    default:
      return undefined;
  }
}

function resolveAttachmentLabel(usageType?: string) {
  switch (usageType) {
    case 'audio':
      return '音訊資源';
    case 'video':
      return '影片資源';
    case 'map-icon':
      return '圖示資源';
    case 'cover':
      return '封面資源';
    case 'gallery':
      return '圖集 / 附件資源';
    default:
      return '附件資源';
  }
}

function normalizeAssetIdArray(value: unknown) {
  const values = Array.isArray(value) ? value : value == null ? [] : [value];
  return values
    .map((item) => {
      if (typeof item === 'number') {
        return Number.isNaN(item) ? null : item;
      }
      if (typeof item === 'string' && item.trim()) {
        const parsed = Number(item);
        return Number.isNaN(parsed) ? null : parsed;
      }
      return null;
    })
    .filter((item): item is number => item !== null);
}

const AttachmentAssetField: React.FC<{ listName: NamePath; fieldName: number }> = ({ listName, fieldName }) => {
  const form = Form.useFormInstance();
  const fullFieldPath = [...toNamePathArray(listName), fieldName];
  const usageType = Form.useWatch([...fullFieldPath, 'usageType'], form) as string | undefined;
  const assetIdPath = [...fullFieldPath, 'assetId'];
  const assetIdsPath = [...fullFieldPath, 'assetIds'];
  const assetId = Form.useWatch(assetIdPath, form) as number | undefined;
  const assetIds = Form.useWatch(assetIdsPath, form) as number[] | undefined;

  const syncSelection = React.useCallback(
    (value: unknown, currentUsageType?: string) => {
      const normalizedIds = normalizeAssetIdArray(value);

      if (currentUsageType === 'gallery') {
        form.setFieldValue(assetIdsPath, normalizedIds);
        form.setFieldValue(assetIdPath, normalizedIds[0]);
        return;
      }

      form.setFieldValue(assetIdPath, normalizedIds[0]);
      form.setFieldValue(assetIdsPath, normalizedIds.length ? [normalizedIds[0]] : []);
    },
    [assetIdPath, assetIdsPath, form],
  );

  React.useEffect(() => {
    if (usageType === 'gallery') {
      if ((!assetIds || !assetIds.length) && typeof assetId === 'number') {
        syncSelection([assetId], usageType);
        return;
      }
      if (assetIds?.length && assetIds[0] !== assetId) {
        form.setFieldValue(assetIdPath, assetIds[0]);
      }
      return;
    }

    if ((assetId === undefined || Number.isNaN(assetId)) && assetIds?.length) {
      syncSelection(assetIds, usageType);
    }
  }, [assetId, assetIdPath, assetIds, form, syncSelection, usageType]);

  return (
    <SpatialAssetPickerField
      name={usageType === 'gallery' ? assetIdsPath : assetIdPath}
      label={resolveAttachmentLabel(usageType)}
      required
      multiple={usageType === 'gallery'}
      assetKind={inferAssetKindFromUsage(usageType)}
      onValueChange={(value) => syncSelection(value, usageType)}
      help={
        usageType === 'gallery'
          ? '可直接多選、拖入多個檔案，或貼上剪貼簿內容；保存時會自動整理為目前附件順序。'
          : '可直接拖曳、點擊上傳或從剪貼簿貼上；完成後會自動綁定到這個附件欄位。'
      }
    />
  );
};

const SpatialAttachmentListField: React.FC<SpatialAttachmentListFieldProps> = ({
  name,
  title = '附件媒體資源',
}) => {
  return (
    <Card size="small" title={title}>
      <Form.List name={name}>
        {(fields, { add, remove }) => (
          <Space direction="vertical" size={12} style={{ width: '100%' }}>
            {fields.map((field) => (
              <Card
                key={field.key}
                size="small"
                extra={
                  <Button danger type="text" icon={<DeleteOutlined />} onClick={() => remove(field.name)}>
                    刪除
                  </Button>
                }
              >
                <Space direction="vertical" size={8} style={{ width: '100%' }}>
                  <Form.Item
                    name={[field.name, 'usageType']}
                    label="附件類型 / 用途"
                    rules={[{ required: true, message: '請選擇附件類型' }]}
                  >
                    <Select options={usageOptions} placeholder="請選擇附件類型 / 用途" />
                  </Form.Item>

                  <AttachmentAssetField listName={name} fieldName={field.name} />

                  <Text type="secondary">
                    每個附件都可以先直接上傳，再依需要改選成其他既有資源。圖集欄位支援一次綁定多個檔案。
                  </Text>

                  <Space style={{ display: 'flex' }} size={16} align="start">
                    <Form.Item name={[field.name, 'sortOrder']} label="排序" style={{ flex: 1 }}>
                      <InputNumber style={{ width: '100%' }} min={0} />
                    </Form.Item>
                    <Form.Item name={[field.name, 'status']} label="狀態" initialValue="draft" style={{ flex: 1 }}>
                      <Select options={statusOptions} />
                    </Form.Item>
                  </Space>
                </Space>
              </Card>
            ))}
            <Button
              type="dashed"
              icon={<PlusOutlined />}
              onClick={() => add({ usageType: 'gallery', sortOrder: fields.length, status: 'draft' })}
            >
              新增附件
            </Button>
          </Space>
        )}
      </Form.List>
    </Card>
  );
};

export default SpatialAttachmentListField;
