import React from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Alert, Card, Space, Tag, Typography } from 'antd';

const { Paragraph, Text } = Typography;

interface ModulePlaceholderProps {
  title: string;
  subTitle: string;
  tags?: string[];
  description: string;
  todoItems?: string[];
}

const ModulePlaceholder: React.FC<ModulePlaceholderProps> = ({
  title,
  subTitle,
  tags = [],
  description,
  todoItems = [],
}) => {
  return (
    <PageContainer title={title} subTitle={subTitle}>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Alert
          type="info"
          showIcon
          message="此模組已納入正式改版規劃"
          description="目前先提供獨立入口與資訊架構說明，後續會依照資料模型、API、權限與操作流程逐步補齊，避免再次導向錯頁或共用不相干的管理介面。"
        />
        <Card bordered={false}>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            {!!tags.length && (
              <Space wrap>
                {tags.map((tag) => (
                  <Tag key={tag} color="blue">
                    {tag}
                  </Tag>
                ))}
              </Space>
            )}
            <Paragraph style={{ marginBottom: 0 }}>{description}</Paragraph>
            {!!todoItems.length && (
              <div>
                <Text strong>後續建設重點</Text>
                <ul style={{ marginTop: 12, marginBottom: 0, paddingInlineStart: 20 }}>
                  {todoItems.map((item) => (
                    <li key={item} style={{ marginBottom: 8 }}>
                      {item}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </Space>
        </Card>
      </Space>
    </PageContainer>
  );
};

export default ModulePlaceholder;
