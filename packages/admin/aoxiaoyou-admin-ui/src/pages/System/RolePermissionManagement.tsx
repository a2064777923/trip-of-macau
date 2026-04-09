import React, { useEffect, useMemo, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import { Button, Card, Col, Divider, Form, Input, Modal, Row, Space, Table, Tag, Tree, message } from 'antd';
import { PlusOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { createRole, getPermissions, getRolePermissions, getRoles, updateRolePermissions, type PermissionItem, type RoleItem } from '../../services/api';

const moduleLabelMap: Record<string, string> = {
  dashboard: '仪表盘',
  'map-space': '地图与空间',
  content: '故事内容',
  collectible: '收集激励',
  user: '用户与进度',
  operation: '测试与运营',
  system: '系统权限',
};

const RolePermissionManagement: React.FC = () => {
  const [roles, setRoles] = useState<RoleItem[]>([]);
  const [permissions, setPermissions] = useState<PermissionItem[]>([]);
  const [selectedRole, setSelectedRole] = useState<RoleItem | null>(null);
  const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([]);
  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form] = Form.useForm();

  const loadBase = async () => {
    const [rolesRes, permsRes] = await Promise.all([getRoles(), getPermissions()]);
    if (rolesRes.success) {
      setRoles(rolesRes.data || []);
      if (!selectedRole && rolesRes.data?.length) {
        setSelectedRole(rolesRes.data[0]);
      }
    }
    if (permsRes.success) {
      setPermissions(permsRes.data || []);
    }
  };

  useEffect(() => {
    loadBase();
  }, []);

  useEffect(() => {
    const loadRolePerms = async () => {
      if (!selectedRole) return;
      const res = await getRolePermissions(selectedRole.id);
      if (res.success) {
        setCheckedKeys((res.data || []).map((item) => item.id));
      }
    };
    loadRolePerms();
  }, [selectedRole]);

  const treeData = useMemo(() => {
    const grouped = permissions.reduce<Record<string, PermissionItem[]>>((acc, item) => {
      const module = item.module || 'other';
      if (!acc[module]) acc[module] = [];
      acc[module].push(item);
      return acc;
    }, {});
    return Object.entries(grouped).map(([module, items]) => ({
      title: `${moduleLabelMap[module] || module} (${items.length})`,
      key: `module:${module}`,
      children: items.map((item) => ({
        title: `${item.permName} · ${item.permType}`,
        key: item.id,
      })),
    }));
  }, [permissions]);

  return (
    <PageContainer
      title="角色与权限"
      subTitle="管理角色、权限点与角色权限矩阵"
      extra={[
        <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setRoleModalOpen(true); }}>
          新建角色
        </Button>,
      ]}
    >
      <Row gutter={16}>
        <Col xs={24} xl={9}>
          <Card title="角色列表" bordered={false}>
            <Table<RoleItem>
              rowKey="id"
              pagination={false}
              dataSource={roles}
              rowSelection={undefined}
              columns={[
                {
                  title: '角色', dataIndex: 'roleName',
                  render: (_, record) => (
                    <Space direction="vertical" size={0}>
                      <Space>
                        <span style={{ fontWeight: 600 }}>{record.roleName}</span>
                        {record.isSystem ? <Tag color="gold">系统</Tag> : null}
                      </Space>
                      <span style={{ color: '#666', fontSize: 12 }}>{record.roleCode}</span>
                    </Space>
                  ),
                },
                {
                  title: '状态', dataIndex: 'status', width: 90,
                  render: (value: string) => <Tag color={value === '1' ? 'green' : 'default'}>{value === '1' ? '启用' : '停用'}</Tag>,
                },
              ]}
              onRow={(record) => ({
                onClick: () => setSelectedRole(record),
                style: {
                  cursor: 'pointer',
                  background: selectedRole?.id === record.id ? '#f7f3ff' : undefined,
                },
              })}
            />
          </Card>
        </Col>
        <Col xs={24} xl={15}>
          <Card
            title={selectedRole ? `权限矩阵 · ${selectedRole.roleName}` : '权限矩阵'}
            extra={selectedRole ? (
              <Button type="primary" icon={<SafetyCertificateOutlined />} loading={submitting} onClick={async () => {
                if (!selectedRole) return;
                setSubmitting(true);
                try {
                  await updateRolePermissions(selectedRole.id, checkedKeys.filter((item) => typeof item === 'number') as number[]);
                  message.success('权限矩阵已保存');
                } finally {
                  setSubmitting(false);
                }
              }}>
                保存权限
              </Button>
            ) : null}
          >
            <p style={{ color: '#666', marginBottom: 16 }}>
              按模块分组显示菜单、按钮与 API 权限点；当前为角色维度授权入口。
            </p>
            <Tree
              checkable
              selectable={false}
              treeData={treeData}
              checkedKeys={checkedKeys}
              onCheck={(keys) => setCheckedKeys(keys as React.Key[])}
              defaultExpandAll
            />
          </Card>
        </Col>
      </Row>

      <Modal title="新建角色" open={roleModalOpen} onCancel={() => setRoleModalOpen(false)} onOk={async () => {
        const values = await form.validateFields();
        await createRole(values);
        message.success('角色创建成功');
        setRoleModalOpen(false);
        loadBase();
      }}>
        <Form form={form} layout="vertical">
          <Form.Item label="角色编码" name="roleCode" rules={[{ required: true, message: '请输入角色编码' }]}>
            <Input placeholder="如 city_editor、marketing_manager" />
          </Form.Item>
          <Form.Item label="角色名称" name="roleName" rules={[{ required: true, message: '请输入角色名称' }]}>
            <Input placeholder="城市编辑" />
          </Form.Item>
          <Form.Item label="角色描述" name="description">
            <Input.TextArea rows={3} placeholder="用于多城市内容编辑与地图空间维护" />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default RolePermissionManagement;
