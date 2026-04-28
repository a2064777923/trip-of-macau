import React from 'react';
import { Button, Card, Form, Space, Typography } from 'antd';
import type { NamePath } from 'antd/es/form/interface';
import { DeleteOutlined, PlusOutlined } from '@ant-design/icons';
import MediaAssetPickerField from './MediaAssetPickerField';
import MediaUploadPanel from './MediaUploadPanel';

const { Text } = Typography;

interface MediaAssetArrayFieldProps {
  name: NamePath;
  label: string;
  assetKind?: string;
  help?: string;
}

const MediaAssetArrayField: React.FC<MediaAssetArrayFieldProps> = ({
  name,
  label,
  assetKind,
  help,
}) => {
  const form = Form.useFormInstance();
  const listPath = Array.isArray(name) ? name : [name];

  return (
    <Card
      size="small"
      title={label}
      extra={<Text type="secondary">可直接上傳、拖拽、貼上或呼叫 AI 工作台生成附件</Text>}
      style={{ marginBottom: 24 }}
    >
      {help ? (
        <Typography.Paragraph type="secondary" style={{ marginBottom: 16 }}>
          {help}
        </Typography.Paragraph>
      ) : null}

      <Form.List name={name}>
        {(fields, { add, remove }) => (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            {fields.map((field, index) => (
              <Card
                key={field.key}
                size="small"
                title={`附件 ${index + 1}`}
                extra={
                  <Button type="text" danger icon={<DeleteOutlined />} onClick={() => remove(field.name)}>
                    移除
                  </Button>
                }
              >
                <MediaAssetPickerField
                  name={[...listPath, field.name]}
                  label="媒體資源"
                  assetKind={assetKind}
                  valueMode="asset-id"
                />
              </Card>
            ))}

            <Button type="dashed" icon={<PlusOutlined />} onClick={() => add(undefined)}>
              新增附件欄位
            </Button>

            <MediaUploadPanel
              onUploaded={(assets) => {
                const current = (form.getFieldValue(name) as Array<number | undefined>) || [];
                const nextValues = [...current];
                assets.forEach((asset) => {
                  if (!nextValues.includes(asset.id)) {
                    nextValues.push(asset.id);
                  }
                });
                form.setFieldValue(name, nextValues);
              }}
            />
          </Space>
        )}
      </Form.List>
    </Card>
  );
};

export default MediaAssetArrayField;
