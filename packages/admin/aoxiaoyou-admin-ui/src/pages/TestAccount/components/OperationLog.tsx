import React, { useState, useEffect } from 'react';
import { Card, Table, Tag, Space, Typography, Empty, Spin, Radio, Divider } from 'antd';
import { HistoryOutlined, EnvironmentOutlined, TrophyOutlined, UserOutlined, SyncOutlined, ToolOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import { getTestAccountOperationLogs } from '../../../services/api';

const { Text } = Typography;

interface OperationLog {
  id: number;
  operationType: string;
  operationTypeName: string;
  operationDesc?: string;
  adminName?: string;
  ipAddress?: string;
  createTime?: string;
}

interface OperationLogProps {
  testAccountId: number;
}

const OperationLog: React.FC<OperationLogProps> = ({ testAccountId }) => {
  const [loading, setLoading] = useState(false);
  const [logs, setLogs] = useState<OperationLog[]>([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [filterType, setFilterType] = useState<string>('all');

  useEffect(() => {
    if (testAccountId) {
      loadLogs();
    }
  }, [testAccountId, currentPage, pageSize, filterType]);

  const loadLogs = async () => {
    setLoading(true);
    try {
      const response = await getTestAccountOperationLogs(testAccountId, {
        pageNum: currentPage,
        pageSize,
      });
      if (response.success && response.data) {
        let filtered = response.data.list || [];
        if (filterType !== 'all') {
          filtered = filtered.filter(log => log.operationType === filterType);
        }
        setLogs(filtered);
        setTotal(response.data.total || 0);
      }
    } catch (error) {
      console.error('Failed to load operation logs:', error);
    } finally {
      setLoading(false);
    }
  };

  const getOperationIcon = (type: string) => {
    switch (type) {
      case 'SET_MOCK_LOCATION':
      case 'DISABLE_MOCK_LOCATION':
        return <EnvironmentOutlined />;
      case 'GRANT_STAMP':
        return <TrophyOutlined />;
      case 'ADJUST_LEVEL':
        return <UserOutlined />;
      case 'RESET_PROGRESS':
        return <SyncOutlined />;
      default:
        return <ToolOutlined />;
    }
  };

  const getOperationColor = (type: string) => {
    switch (type) {
      case 'SET_MOCK_LOCATION':
      case 'DISABLE_MOCK_LOCATION':
        return 'blue';
      case 'GRANT_STAMP':
        return 'green';
      case 'ADJUST_LEVEL':
        return 'purple';
      case 'RESET_PROGRESS':
        return 'red';
      default:
        return 'default';
    }
  };

  const columns: ColumnsType<OperationLog> = [
    {
      title: '操作类型',
      dataIndex: 'operationTypeName',
      key: 'operationTypeName',
      width: 180,
      render: (text, record) => (
        <Tag
          icon={getOperationIcon(record.operationType)}
          color={getOperationColor(record.operationType)}
          style={{ fontSize: 13, padding: '4px 8px' }}
        >
          {text}
        </Tag>
      ),
    },
    {
      title: '操作描述',
      dataIndex: 'operationDesc',
      key: 'operationDesc',
      render: (text) => (
        <Text style={{ fontSize: 13 }}>{text || '-'}</Text>
      ),
    },
    {
      title: '操作人',
      dataIndex: 'adminName',
      key: 'adminName',
      width: 120,
      render: (text) => (
        <Tag color="default" style={{ fontSize: 12 }}>{text || '-'}</Tag>
      ),
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 130,
      render: (text) => (
        <Text type="secondary" style={{ fontSize: 12, fontFamily: 'monospace' }}>
          {text || '-'}
        </Text>
      ),
    },
    {
      title: '操作时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 170,
      render: (text) => (
        <Text style={{ fontSize: 12 }}>
          {text ? dayjs(text).format('YYYY-MM-DD HH:mm:ss') : '-'}
        </Text>
      ),
    },
  ];

  return (
    <Space direction="vertical" style={{ width: '100%' }} size="middle">
      {/* 筛选栏 */}
      <Card size="small">
        <Space wrap>
          <Text strong>操作类型筛选：</Text>
          <Radio.Group
            value={filterType}
            onChange={(e) => {
              setFilterType(e.target.value);
              setCurrentPage(1);
            }}
            optionType="button"
            buttonStyle="solid"
            size="small"
          >
            <Radio.Button value="all">全部</Radio.Button>
            <Radio.Button value="SET_MOCK_LOCATION">模拟定位</Radio.Button>
            <Radio.Button value="GRANT_STAMP">获得印章</Radio.Button>
            <Radio.Button value="ADJUST_LEVEL">等级调整</Radio.Button>
            <Radio.Button value="RESET_PROGRESS">进度重置</Radio.Button>
          </Radio.Group>
        </Space>
      </Card>

      {/* 操作日志表格 */}
      <Card size="small">
        <Table
          columns={columns}
          dataSource={logs}
          rowKey="id"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (t) => `共 ${t} 条记录`,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size || 10);
            },
          }}
          size="small"
          scroll={{ x: 900 }}
          locale={{ emptyText: <Empty description="暂无操作日志" /> }}
        />
      </Card>
    </Space>
  );
};

export default OperationLog;
