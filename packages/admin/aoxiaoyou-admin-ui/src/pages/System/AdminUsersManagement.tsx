import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import {
  Avatar,
  Button,
  Descriptions,
  Drawer,
  Form,
  Input,
  Select,
  Space,
  Switch,
  Tag,
  Typography,
  message,
} from 'antd';
import {
  EditOutlined,
  EyeOutlined,
  SafetyCertificateOutlined,
  UserOutlined,
} from '@ant-design/icons';
import {
  getAdminUsersRbac,
  updateAdminUserRbac,
  type AdminUserItem,
} from '../../services/api';

const { Text } = Typography;

const AdminUsersManagement: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [form] = Form.useForm();
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [current, setCurrent] = useState<AdminUserItem | null>(null);

  const columns = useMemo<ProColumns<AdminUserItem>[]>(() => [
    {
      title: '管理員',
      dataIndex: 'username',
      render: (_, record) => (
        <Space>
          <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#5b66b5' }} />
          <div>
            <Space size={8}>
              <Text strong>{record.displayName || record.username}</Text>
              {record.isSuperuser ? <Tag color="gold">超級管理員</Tag> : null}
            </Space>
            <div>
              <Text type="secondary" style={{ fontSize: 12 }}>
                {record.username}
              </Text>
            </div>
          </div>
        </Space>
      ),
    },
    {
      title: '聯絡資訊',
      key: 'contact',
      hideInSearch: true,
      render: (_, record) => (
        <Space direction="vertical" size={2}>
          <Text>{record.email || '未設定信箱'}</Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {record.phone || '未設定電話'}
          </Text>
        </Space>
      ),
    },
    {
      title: '無損上傳',
      dataIndex: 'allowLosslessUpload',
      width: 120,
      hideInSearch: true,
      render: (value: boolean | undefined) =>
        value ? <Tag color="success">已開啟</Tag> : <Tag>未開啟</Tag>,
    },
    {
      title: '狀態',
      dataIndex: 'status',
      width: 100,
      render: (value: string) => (
        <Tag color={value === 'active' || value === '1' ? 'green' : 'default'}>
          {value === 'active' || value === '1' ? '啟用' : '停用'}
        </Tag>
      ),
    },
    {
      title: '最後登入',
      dataIndex: 'lastLoginAt',
      valueType: 'dateTime',
      hideInSearch: true,
      width: 180,
    },
    {
      title: '操作',
      key: 'option',
      valueType: 'option',
      width: 150,
      render: (_, record) => [
        <Button
          key="detail"
          type="link"
          icon={<EyeOutlined />}
          onClick={() => {
            setCurrent(record);
            form.setFieldsValue({
              displayName: record.displayName,
              email: record.email,
              phone: record.phone,
              status: record.status === '1' ? 'active' : record.status,
              allowLosslessUpload: !!record.allowLosslessUpload,
            });
            setDrawerOpen(true);
          }}
        >
          查看 / 編輯
        </Button>,
      ],
    },
  ], [form]);

  return (
    <PageContainer
      title="管理員帳號"
      subTitle="管理後台帳號狀態與媒體無損上傳權限。這個權限會直接影響後端媒體處理策略，不由前端自行決定。"
      extra={[
        <Tag key="policy" color="blue" icon={<SafetyCertificateOutlined />}>
          媒體策略由後端強制執行
        </Tag>,
      ]}
    >
      <ProTable<AdminUserItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        request={async (params) => {
          const response = await getAdminUsersRbac({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.username as string,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        headerTitle="管理員列表"
        search={{ labelWidth: 'auto' }}
      />

      <Drawer
        title="管理員設定"
        open={drawerOpen}
        width={560}
        onClose={() => setDrawerOpen(false)}
        destroyOnHidden
        extra={
          <Space>
            <Button onClick={() => setDrawerOpen(false)}>取消</Button>
            <Button
              type="primary"
              loading={saving}
              onClick={() => form.submit()}
              icon={<EditOutlined />}
            >
              儲存
            </Button>
          </Space>
        }
      >
        {current ? (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions column={1} bordered size="small">
              <Descriptions.Item label="帳號">{current.username}</Descriptions.Item>
              <Descriptions.Item label="是否超級管理員">
                {current.isSuperuser ? '是' : '否'}
              </Descriptions.Item>
              <Descriptions.Item label="部門">{current.department || '-'}</Descriptions.Item>
              <Descriptions.Item label="最後登入時間">{current.lastLoginAt || '-'}</Descriptions.Item>
              <Descriptions.Item label="最後登入 IP">{current.lastLoginIp || '-'}</Descriptions.Item>
            </Descriptions>

            <Form
              form={form}
              layout="vertical"
              onFinish={async (values) => {
                if (!current) {
                  return;
                }
                setSaving(true);
                try {
                  const response = await updateAdminUserRbac(current.id, values);
                  if (!response.success || !response.data) {
                    message.error(response.message || '更新管理員設定失敗');
                    return;
                  }
                  message.success('管理員設定已更新');
                  setDrawerOpen(false);
                  actionRef.current?.reload();
                } finally {
                  setSaving(false);
                }
              }}
            >
              <Form.Item name="displayName" label="顯示名稱">
                <Input placeholder="輸入顯示名稱" />
              </Form.Item>
              <Form.Item name="email" label="電子郵件">
                <Input placeholder="輸入電子郵件" />
              </Form.Item>
              <Form.Item name="phone" label="聯絡電話">
                <Input placeholder="輸入聯絡電話" />
              </Form.Item>
              <Form.Item name="status" label="帳號狀態">
                <Select
                  options={[
                    { value: 'active', label: '啟用' },
                    { value: 'disabled', label: '停用' },
                  ]}
                />
              </Form.Item>
              <Form.Item
                name="allowLosslessUpload"
                label="允許無損上傳"
                valuePropName="checked"
                extra="開啟後，該管理員在符合系統策略設定時可直接走無損上傳流程；關閉時由後端自動降級為壓縮或直通策略。"
              >
                <Switch checkedChildren="允許" unCheckedChildren="關閉" />
              </Form.Item>
            </Form>
          </Space>
        ) : null}
      </Drawer>
    </PageContainer>
  );
};

export default AdminUsersManagement;

