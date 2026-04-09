import React from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import { Badge, Card, Tag, Typography } from 'antd';
import type { ProColumns } from '@ant-design/pro-table';
import { getAdminActivities } from '../../services/api';
import type { AdminActivityItem } from '../../types/admin';

const { Text } = Typography;

const statusColorMap: Record<string, string> = {
  draft: 'default',
  published: 'processing',
  ended: 'success',
  cancelled: 'error',
};

const OperationsManagement: React.FC = () => {
  const columns: ProColumns<AdminActivityItem>[] = [
    { title: '活动标题', dataIndex: 'title' },
    { title: '活动编码', dataIndex: 'code', copyable: true, hideInSearch: true },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: {
        draft: { text: '草稿' },
        published: { text: '已发布' },
        ended: { text: '已结束' },
        cancelled: { text: '已取消' },
      },
      render: (_, record) => <Tag color={statusColorMap[record.status || 'draft']}>{record.status || 'draft'}</Tag>,
    },
    {
      title: '参与人数',
      dataIndex: 'participationCount',
      hideInSearch: true,
      render: (value) => <Badge color="#7c5cff" text={`${value || 0} 人`} />,
    },
    { title: '开始时间', dataIndex: 'startTime', valueType: 'dateTime', hideInSearch: true },
    { title: '结束时间', dataIndex: 'endTime', valueType: 'dateTime', hideInSearch: true },
    {
      title: '活动说明',
      dataIndex: 'description',
      hideInSearch: true,
      render: (value) => <Text type="secondary">{value || '用于联动故事线、商户广告与节庆活动。'}</Text>,
    },
  ];

  return (
    <PageContainer title="运营管理" subTitle="管理节庆活动、品牌联动和用户拉新投放节奏">
      <Card>
        <ProTable<AdminActivityItem>
          rowKey="id"
          columns={columns}
          request={async (params) => {
            const response = await getAdminActivities({
              pageNum: params.current,
              pageSize: params.pageSize,
              keyword: params.title as string,
              status: params.status as string,
            });
            return {
              data: response.data?.list || [],
              success: response.success,
              total: response.data?.total || 0,
            };
          }}
          search={{ labelWidth: 'auto' }}
          pagination={{ pageSize: 10 }}
          headerTitle="活动列表"
          toolBarRender={false}
        />
      </Card>
    </PageContainer>
  );
};

export default OperationsManagement;
