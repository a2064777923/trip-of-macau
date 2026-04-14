import React from 'react';
import { Button, Card, Form, InputNumber, Select, Space, Typography } from 'antd';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import SpatialAssetPickerField from './SpatialAssetPickerField';

type NamePath = string | number | Array<string | number>;

const { Text } = Typography;

const usageOptions = [
  { label: '圖集', value: 'gallery' },
  { label: '彈窗', value: 'popup' },
  { label: '音訊', value: 'audio' },
  { label: '影片', value: 'video' },
  { label: '地圖圖標', value: 'map-icon' },
  { label: '封面補充', value: 'cover' },
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
    case 'map-icon':
      return 'icon';
    case 'gallery':
    case 'popup':
    case 'cover':
      return 'image';
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
      return '圖標資源';
    case 'cover':
      return '封面補充資源';
    default:
      return '附件資源';
  }
}

const AttachmentAssetField: React.FC<{ listName: NamePath; fieldName: number }> = ({ listName, fieldName }) => {
  const form = Form.useFormInstance();
  const fullFieldPath = [...toNamePathArray(listName), fieldName];
  const usageType = Form.useWatch([...fullFieldPath, 'usageType'], form) as string | undefined;

  return (
    <SpatialAssetPickerField
      name={[...fullFieldPath, 'assetId']}
      label={resolveAttachmentLabel(usageType)}
      required
      assetKind={inferAssetKindFromUsage(usageType)}
      help="可直接拖拽、點擊上傳或貼上檔案，上傳後會自動綁定到這筆附件。"
    />
  );
};

const SpatialAttachmentListField: React.FC<SpatialAttachmentListFieldProps> = ({
  name,
  title = '附加媒體與排序',
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
                    label="用途"
                    rules={[{ required: true, message: '請選擇附件用途' }]}
                  >
                    <Select options={usageOptions} placeholder="請選擇附件用途" />
                  </Form.Item>

                  <AttachmentAssetField listName={name} fieldName={field.name} />

                  <Text type="secondary">
                    每筆附件都可先直接上傳，再視需要切換為其他已存在資源。
                  </Text>

                  <Space style={{ display: 'flex' }} size={16} align="start">
                    <Form.Item name={[field.name, 'sortOrder']} label="排序" style={{ flex: 1 }}>
                      <InputNumber style={{ width: '100%' }} min={0} />
                    </Form.Item>
                    <Form.Item name={[field.name, 'status']} label="狀態" initialValue="draft" style={{ flex: 1 }}>
                      <Select
                        options={[
                          { label: '草稿', value: 'draft' },
                          { label: '已發布', value: 'published' },
                          { label: '已封存', value: 'archived' },
                        ]}
                      />
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
