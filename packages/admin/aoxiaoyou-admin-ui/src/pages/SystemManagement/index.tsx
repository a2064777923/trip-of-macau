import React, { useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Card, Col, Row, Table, Tag, Typography, Space, Button, Drawer, Form, Input, InputNumber, DatePicker, Select, Popconfirm, message } from 'antd';
import { GiftOutlined, SettingOutlined, AuditOutlined, GlobalOutlined, PlusOutlined } from '@ant-design/icons';
import { useRequest } from 'ahooks';
import dayjs from 'dayjs';
import {
  createAdminReward,
  deleteAdminReward,
  getAdminAuditLogs,
  getAdminMapTiles,
  getAdminRewards,
  getAdminSystemConfigs,
  updateAdminReward,
} from '../../services/api';
import type { AdminRewardItem } from '../../types/admin';

const { Text } = Typography;

const SystemManagement: React.FC = () => {
  const [rewardDrawerOpen, setRewardDrawerOpen] = useState(false);
  const [rewardEditing, setRewardEditing] = useState<AdminRewardItem | null>(null);
  const [rewardSubmitting, setRewardSubmitting] = useState(false);
  const [form] = Form.useForm();

  const rewards = useRequest(() => getAdminRewards({ pageNum: 1, pageSize: 20 }));
  const configs = useRequest(() => getAdminSystemConfigs({ pageNum: 1, pageSize: 6 }));
  const auditLogs = useRequest(() => getAdminAuditLogs({ pageNum: 1, pageSize: 8 }));
  const mapTiles = useRequest(() => getAdminMapTiles({ pageNum: 1, pageSize: 4 }));

  const rewardColumns = useMemo(() => ([
    { title: '奖励', dataIndex: 'name' },
    { title: '印章门槛', dataIndex: 'stampsRequired', width: 100 },
    { title: '总量', dataIndex: 'totalQuantity', width: 90 },
    { title: '已兑换', dataIndex: 'redeemedCount', width: 90 },
    { title: '余量', dataIndex: 'remainingQuantity', width: 80 },
    {
      title: '状态',
      dataIndex: 'status',
      width: 90,
      render: (value: string) => <Tag color={value === 'active' ? 'success' : 'default'}>{value || 'inactive'}</Tag>,
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: unknown, record: AdminRewardItem) => (
        <Space>
          <Button
            type="link"
            onClick={() => {
              setRewardEditing(record);
              form.setFieldsValue({
                ...record,
                startTime: record.startTime ? dayjs(record.startTime) : undefined,
                endTime: record.endTime ? dayjs(record.endTime) : undefined,
              });
              setRewardDrawerOpen(true);
            }}
          >
            编辑
          </Button>
          <Popconfirm
            title="确认删除该奖励吗？"
            onConfirm={async () => {
              await deleteAdminReward(record.id);
              message.success('奖励已删除');
              rewards.refresh();
            }}
          >
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]), [form, rewards]);

  return (
    <PageContainer
      title="系统管理"
      subTitle="集中查看奖励、审计日志、地图资源与触发配置"
      extra={[
        <Button
          key="add-reward"
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setRewardEditing(null);
            form.resetFields();
            form.setFieldsValue({ status: 'inactive', stampsRequired: 1, totalQuantity: 100, redeemedCount: 0 });
            setRewardDrawerOpen(true);
          }}
        >
          新建奖励
        </Button>,
      ]}
    >
      <Row gutter={[16, 16]}>
        <Col xs={24} xl={14}>
          <Card title={<Space><GiftOutlined />奖励配置</Space>} loading={rewards.loading}>
            <Table
              size="small"
              rowKey="id"
              pagination={false}
              dataSource={rewards.data?.data?.list || []}
              columns={rewardColumns}
            />
          </Card>
        </Col>
        <Col xs={24} xl={10}>
          <Card title={<Space><SettingOutlined />系统配置</Space>} loading={configs.loading}>
            <Table
              size="small"
              rowKey="id"
              pagination={false}
              dataSource={configs.data?.data?.list || []}
              columns={[
                { title: '配置键', dataIndex: 'configKey' },
                {
                  title: '配置值',
                  dataIndex: 'configValue',
                  render: (value: string) => <Text code>{value}</Text>,
                },
                { title: '说明', dataIndex: 'description' },
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} xl={14}>
          <Card title={<Space><AuditOutlined />审计日志</Space>} loading={auditLogs.loading}>
            <Table
              size="small"
              rowKey="id"
              pagination={false}
              dataSource={auditLogs.data?.data?.list || []}
              columns={[
                { title: '模块 / 操作', dataIndex: 'operationTypeName' },
                { title: '操作人', dataIndex: 'adminName', width: 100 },
                { title: '说明', dataIndex: 'operationDesc' },
                { title: '时间', dataIndex: 'createTime', width: 180 },
              ]}
            />
          </Card>
        </Col>
        <Col xs={24} xl={10}>
          <Card title={<Space><GlobalOutlined />地图资源</Space>} loading={mapTiles.loading}>
            <Space direction="vertical" style={{ width: '100%' }} size="middle">
              {(mapTiles.data?.data?.list || []).map((item) => (
                <Card key={item.id} size="small" variant="borderless" style={{ background: '#f7f8ff' }}>
                  <Space direction="vertical" size={4} style={{ width: '100%' }}>
                    <Space style={{ justifyContent: 'space-between', width: '100%' }}>
                      <Text strong>{item.mapId}</Text>
                      <Tag color={item.status === 'active' ? 'success' : 'default'}>{item.status}</Tag>
                    </Space>
                    <Text type="secondary">样式：{item.style || 'cartoon'}</Text>
                    <Text ellipsis={{ tooltip: item.cdnBase }}>CDN：{item.cdnBase}</Text>
                    <Text type="secondary">缩放级别：{item.zoomLevels}</Text>
                  </Space>
                </Card>
              ))}
            </Space>
          </Card>
        </Col>
      </Row>

      <Drawer
        open={rewardDrawerOpen}
        title={rewardEditing ? '编辑奖励' : '新建奖励'}
        width={520}
        onClose={() => setRewardDrawerOpen(false)}
        destroyOnClose
      >
        <Form
          layout="vertical"
          form={form}
          onFinish={async (values) => {
            setRewardSubmitting(true);
            try {
              const payload = {
                ...values,
                startTime: values.startTime ? values.startTime.format('YYYY-MM-DDTHH:mm:ss') : undefined,
                endTime: values.endTime ? values.endTime.format('YYYY-MM-DDTHH:mm:ss') : undefined,
              };
              if (rewardEditing) {
                await updateAdminReward(rewardEditing.id, payload);
                message.success('奖励更新成功');
              } else {
                await createAdminReward(payload);
                message.success('奖励创建成功');
              }
              setRewardDrawerOpen(false);
              rewards.refresh();
            } finally {
              setRewardSubmitting(false);
            }
          }}
        >
          <Form.Item label="奖励名称" name="name" rules={[{ required: true, message: '请输入奖励名称' }]}><Input /></Form.Item>
          <Form.Item label="奖励描述" name="description"><Input.TextArea rows={3} /></Form.Item>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="印章门槛" name="stampsRequired" style={{ width: '100%' }}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
            <Form.Item label="总库存" name="totalQuantity" style={{ width: '100%' }}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
            <Form.Item label="已兑换" name="redeemedCount" style={{ width: '100%' }}><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
          </Space>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="开始时间" name="startTime" style={{ width: '100%' }}><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
            <Form.Item label="结束时间" name="endTime" style={{ width: '100%' }}><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
          </Space>
          <Form.Item label="状态" name="status"><Select options={[{ label: '启用', value: 'active' }, { label: '停用', value: 'inactive' }, { label: '结束', value: 'expired' }]} /></Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" loading={rewardSubmitting}>保存</Button>
            <Button onClick={() => setRewardDrawerOpen(false)}>取消</Button>
          </Space>
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default SystemManagement;
