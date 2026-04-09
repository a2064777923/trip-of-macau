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
          message="该模块已纳入正式重构范围"
          description="当前页面先作为信息架构占位与后续实施入口，后续会按数据库、API、权限与交互设计逐步补齐。"
        />
        <Card>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Space wrap>
              {tags.map((tag) => (
                <Tag key={tag} color="blue">{tag}</Tag>
              ))}
            </Space>
            <Paragraph style={{ marginBottom: 0 }}>{description}</Paragraph>
            {todoItems.length > 0 && (
              <div>
                <Text strong>后续建设项</Text>
                <ul>
                  {todoItems.map((item) => (
                    <li key={item}>{item}</li>
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
