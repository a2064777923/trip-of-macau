import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import { Button, Form, Image, Input, message, Modal, Select, Space, Tag } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { createCollectible, getCollectibles, type CollectibleItem } from '../../services/api';

const CollectibleManagement: React.FC = () => {
  const [form] = Form.useForm();
  const [modalOpen, setModalOpen] = React.useState(false);

  const columns = [
    { title: 'Code', dataIndex: 'collectibleCode', width: 170 },
    { title: 'Name', dataIndex: 'nameZh', width: 160 },
    { title: 'Type', dataIndex: 'collectibleType', width: 90, render: (value: string) => <Tag>{value}</Tag> },
    { title: 'Rarity', dataIndex: 'rarity', width: 90, render: (value: string) => <Tag>{value}</Tag> },
    { title: 'Image', dataIndex: 'imageUrl', width: 80, render: (url: string | null) => url ? <Image src={url} width={40} height={40} style={{ borderRadius: 4 }} /> : '-' },
    { title: 'Series ID', dataIndex: 'seriesId', width: 90 },
    { title: 'Source', dataIndex: 'acquisitionSource', width: 100 },
    { title: 'Repeatable', dataIndex: 'isRepeatable', width: 95, render: (value: number) => <Tag color={value ? 'green' : 'default'}>{value ? 'yes' : 'no'}</Tag> },
    { title: 'Limited', dataIndex: 'isLimited', width: 80, render: (value: number) => value ? <Tag color="orange">yes</Tag> : '-' },
    { title: 'Status', dataIndex: 'status', width: 80, render: (value: string) => <Tag color={value === '1' ? 'green' : 'default'}>{value === '1' ? 'active' : value || 'draft'}</Tag> },
  ];

  return (
    <PageContainer title="Collectible Management" subTitle="Collectible catalog and series management for profile and reward surfaces">
      <ProTable<CollectibleItem>
        columns={columns}
        rowKey="id"
        request={async (params) => {
          const res = await getCollectibles({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.nameZh as string,
            rarity: params.rarity as string,
          });
          return { data: res.data?.list || [], success: res.success, total: res.data?.total || 0 };
        }}
        toolBarRender={() => [
          <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setModalOpen(true); }}>
            New Collectible
          </Button>,
        ]}
        search={{ labelWidth: 'auto' }}
        options={{ density: false, setting: false }}
      />
      <Modal
        title="Create Collectible"
        open={modalOpen}
        onOk={async () => {
          const values = await form.validateFields();
          await createCollectible(values);
          message.success('Collectible created');
          setModalOpen(false);
        }}
        onCancel={() => setModalOpen(false)}
        width={560}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="collectibleCode" label="Code" rules={[{ required: true }]}><Input placeholder="item_xxx" /></Form.Item>
          <Form.Item name="nameZh" label="Name" rules={[{ required: true }]}><Input placeholder="Collectible name" /></Form.Item>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="collectibleType" label="Type">
              <Select options={[{ value: 'item', label: 'item' }, { value: 'stamp_card', label: 'stamp_card' }, { value: 'fragment', label: 'fragment' }, { value: 'costume', label: 'costume' }]} defaultValue="item" />
            </Form.Item>
            <Form.Item name="rarity" label="Rarity">
              <Select options={[{ value: 'common', label: 'common' }, { value: 'uncommon', label: 'uncommon' }, { value: 'rare', label: 'rare' }, { value: 'epic', label: 'epic' }, { value: 'legendary', label: 'legendary' }]} defaultValue="common" />
            </Form.Item>
          </Space>
          <Form.Item name="image_url" label="Image URL"><Input placeholder="https://..." /></Form.Item>
          <Form.Item name="description_zh" label="Description"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default CollectibleManagement;
