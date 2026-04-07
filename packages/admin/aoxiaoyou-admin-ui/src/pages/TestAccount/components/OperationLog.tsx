import React, { useState, useEffect } from 'react';
import { Card, Table, Tag, Timeline, Pagination, Space, Typography, Empty, Spin, DatePicker, Button, Select } from 'antd';
import { HistoryOutlined, EnvironmentOutlined, TrophyOutlined, UserOutlined, SyncOutlined, ToolOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;

// 操作日志数据类型
interface OperationLog {
  id: number;
  operationType: string;
  operationTypeName: string;
  operationDesc: string;
  adminName: string;
  operationData?: any;
  beforeData?: any;
  afterData?: any;
  ipAddress: string;
  createTime: string;
}

// 模拟操作日志数据
const MOCK_OPERATION_LOGS: OperationLog[] = [
  {
    id: 1,
    operationType: 'set_mock_location',
    operationTypeName: '设置模拟定位',
    operationDesc: '设置模拟位置到 澳门半岛大三巴牌坊 (22.1973, 113.5408)',
    adminName: '管理员小王',
    operationData: {
      latitude: 22.1973,
      longitude: 113.5408,
      address: '澳门半岛大三巴牌坊',
    },
    beforeData: { mockLocation: null },
    afterData: {
      mockLocation: {
        latitude: 22.1973,
        longitude: 113.5408,
        address: '澳门半岛大三巴牌坊',
      },
    },
    ipAddress: '192.168.1.100',
    createTime: '2026-04-07T10:30:00+08:00',
  },
  {
    id: 2,
    operationType: 'grant_stamp',
    operationTypeName: '获得印章',
    operationDesc: '快速获得印章 "大三巴"',
    adminName: '管理员小王',
    operationData: {
      stampId: 1,
      stampName: '大三巴',
      triggerLocation: { latitude: 22.1973, longitude: 113.5408 },
    },
    beforeData: { stampCount: 7, experience: 300 },
    afterData: { stampCount: 8, experience: 320, levelUp: false },
    ipAddress: '192.168.1.100',
    createTime: '2026-04-07T10:35:00+08:00',
  },
  {
    id: 3,
    operationType: 'adjust_level',
    operationTypeName: '调整等级',
    operationDesc: '调整等级从 Lv.3 澳门探索者 到 Lv.4 澳门达人',
    adminName: '管理员小李',
    operationData: { level: 4, experience: 320 },
    beforeData: { level: 3, levelName: '澳门探索者', experience: 80 },
    afterData: { level: 4, levelName: '澳门达人', experience: 320 },
    ipAddress: '192.168.1.105',
    createTime: '2026-04-06T16:20:00+08:00',
  },
  {
    id: 4,
    operationType: 'reset_progress',
    operationTypeName: '重置进度',
    operationDesc: '重置所有游戏进度（印章、等级、经验值）',
    adminName: '管理员小王',
    operationData: { resetType: 'all' },
    beforeData: { stampCount: 8, level: 4, experience: 320 },
    afterData: { stampCount: 0, level: 1, experience: 0 },
    ipAddress: '192.168.1.100',
    createTime: '2026-04-05T14:30:00+08:00',
  },
  {
    id: 5,
    operationType: 'enable_mock',
    operationTypeName: '启用模拟定位',
    operationDesc: '启用模拟定位功能',
    adminName: '管理员小李',
    operationData: { enabled: true },
    beforeData: { isMockEnabled: false },
    afterData: { isMockEnabled: true },
    ipAddress: '192.168.1.105',
    createTime: '2026-04-07T09:15:00+08:00',
  },
];

interface OperationLogProps {
  testAccountId: number;
}

const OperationLog: React.FC<OperationLogProps> = ({ testAccountId }) => {
  const [loading, setLoading] = useState(false);
  const [logs, setLogs] = useState<OperationLog[]>(MOCK_OPERATION_LOGS);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [filterType, setFilterType] = useState<string>('all');

  // 获取操作类型图标
  const getOperationIcon = (type: string) => {
    switch (type) {
      case 'set_mock_location':
      case 'enable_mock':
      case 'disable_mock':
        return <EnvironmentOutlined />;
      case 'grant_stamp':
      case 'delete_stamp':
      case 'clear_stamps':
        return <TrophyOutlined />;
      case 'adjust_level':
        return <UserOutlined />;
      case 'reset_progress':
        return <SyncOutlined />;
      default:
        return <ToolOutlined />;
    }
  };

  // 获取操作类型颜色
  const getOperationColor = (type: string) => {
    switch (type) {
      case 'set_mock_location':
      case 'enable_mock':
      case 'disable_mock':
        return 'blue';
      case 'grant_stamp':
        return 'green';
      case 'delete_stamp':
      case 'clear_stamps':
        return 'orange';
      case 'adjust_level':
        return 'purple';
      case 'reset_progress':
        return 'red';
      default:
        return 'default';
    }
  };

  // 表格列定义
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
        <Text style={{ fontSize: 13 }}>{text}</Text>
      ),
    },
    {
      title: '操作人',
      dataIndex: 'adminName',
      key: 'adminName',
      width: 120,
      render: (text) => (
        <Tag color="default" style={{ fontSize: 12 }}>{text}</Tag>
      ),
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 130,
      render: (text) => (
        <Text type="secondary" style={{ fontSize: 12, fontFamily: 'monospace' }}>
          {text}
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
          {dayjs(text).format('YYYY-MM-DD HH:mm:ss')}
        </Text>
      ),
    },
  ];

  // 过滤后的日志
  const filteredLogs = logs.filter(log => {
    if (filterType === 'all') return true;
    return log.operationType === filterType;
  });

  // 分页数据
  const paginatedLogs = filteredLogs.slice(
    (currentPage - 1) * pageSize,
    currentPage * pageSize
  );

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
            <Radio.Button value="set_mock_location">模拟定位</Radio.Button>
            <Radio.Button value="grant_stamp">获得印章</Radio.Button>
            <Radio.Button value="adjust_level">等级调整</Radio.Button>
            <Radio.Button value="reset_progress">进度重置</Radio.Button>
          </Radio.Group>
        </Space>
      </Card>

      {/* 统计信息 */}
      <Card size="small">
        <Space split={<Divider type="vertical" />}>
          <Text>总操作数：<Tag color="blue">{filteredLogs.length}</Tag></Text>
          <Text>今日操作：<Tag color="green">{filteredLogs.filter(log => dayjs(log.createTime).isSame(dayjs(), 'day')).length}</Tag></Text>
          <Text>本周操作：<Tag color="orange">{filteredLogs.filter(log => dayjs(log.createTime).isAfter(dayjs().subtract(7, 'day'))).length}</Tag></Text>
        </Space>
      </Card>

      {/* 操作日志表格 */}
      <Card size="small">
        <Table
          columns={columns}
          dataSource={paginatedLogs}
          rowKey="id"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: filteredLogs.length,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (page, size) => {
              setCurrentPage(page);
              setPageSize(size || 10);
            },
          }}
          size="small"
          scroll={{ x: 1200 }}
        />
      </Card>
    </Space>
  );
};

export default OperationLog;
