import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import { Button, Form, Image, Input, message, Modal, Select, Space, Tag } from 'antd';
import { EyeInvisibleOutlined, PlusOutlined } from '@ant-design/icons';
import { createBadge, getBadges, type BadgeItem } from '../../services/api';

const BadgeManagement: React.FC = () => {
  const [form] = Form.useForm();
  const [modalOpen, setModalOpen] = React.useState(false);

  const columns = [
    { title: 'Code', dataIndex: 'badgeCode', width: 160 },
    { title: 'Name', dataIndex: 'nameZh', width: 160 },
    { title: 'Type', dataIndex: 'badgeType', width: 110, render: (value: string) => <Tag>{value}</Tag> },
    { title: 'Rarity', dataIndex: 'rarity', width: 90, render: (value: string) => <Tag>{value}</Tag> },
    { title: 'Hidden', dataIndex: 'isHidden', width: 70, render: (value: number) => value ? <EyeInvisibleOutlined style={{ color: '#eb2f96' }} /> : '-' },
    { title: 'Icon', dataIndex: 'iconUrl', width: 72, render: (url: string | null) => url ? <Image src={url} width={32} height={32} style={{ borderRadius: 4 }} /> : '-' },
    { title: 'Status', dataIndex: 'status', width: 80, render: (value: string) => <Tag color={value === '1' ? 'green' : 'default'}>{value === '1' ? 'active' : value || 'draft'}</Tag> },
    { title: 'Actions', width: 100, fixed: 'right' as const, render: () => <Button type="link" size="small">Rules</Button> },
  ];

  return (
    <PageContainer title="Badge Management" subTitle="Badge catalog and acquisition rule placeholders for collectible surfaces">
      <ProTable<BadgeItem>
        columns={columns}
        rowKey="id"
        request={async (params) => {
          const res = await getBadges({ pageNum: params.current, pageSize: params.pageSize });
          return { data: res.data?.list || [], success: res.success, total: res.data?.total || 0 };
        }}
        toolBarRender={() => [
          <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>
            New Badge
          </Button>,
        ]}
        options={{ density: false, setting: false }}
      />
      <Modal
        title="Create Badge"
        open={modalOpen}
        onOk={async () => {
          const values = await form.validateFields();
          await createBadge(values);
          message.success('Badge created');
          setModalOpen(false);
        }}
        onCancel={() => setModalOpen(false)}
        width={520}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="badgeCode" label="Badge Code" rules={[{ required: true }]}><Input placeholder="badge_xxx" /></Form.Item>
          <Form.Item name="nameZh" label="Name" rules={[{ required: true }]}><Input placeholder="First Visit" /></Form.Item>
          <Form.Item name="description_zh" label="Description"><Input.TextArea rows={2} /></Form.Item>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="badgeType" label="Type">
              <Select options={[{ value: 'storyline', label: 'storyline' }, { value: 'city_exploration', label: 'city_exploration' }, { value: 'collection', label: 'collection' }, { value: 'activity', label: 'activity' }, { value: 'special', label: 'special' }, { value: 'hidden', label: 'hidden' }]} defaultValue="special" />
            </Form.Item>
            <Form.Item name="rarity" label="Rarity">
              <Select options={[{ value: 'common', label: 'common' }, { value: 'rare', label: 'rare' }, { value: 'epic', label: 'epic' }, { value: 'legendary', label: 'legendary' }, { value: 'hidden', label: 'hidden' }]} defaultValue="common" />
            </Form.Item>
          </Space>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="iconUrl" label="Icon URL"><Input /></Form.Item>
            <Form.Item name="imageUrl" label="Image URL"><Input /></Form.Item>
          </Space>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default BadgeManagement;
