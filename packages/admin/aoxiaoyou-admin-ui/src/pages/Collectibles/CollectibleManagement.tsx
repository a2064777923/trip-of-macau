import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import { Modal, Form, Input, Select, Button, Space, Tag, Image, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { getCollectibles, createCollectible, type CollectibleItem } from '../../services/api';

const CollectibleManagement: React.FC = () => {
  const [form] = Form.useForm();
  const [drawerVisible, setDrawerVisible] = React.useState(false);

  const rarityColors: Record<string, string> = {
    common: '#999999', uncommon: '#52c41a', rare: '#1677ff', epic: '#722ed1', legendary: '#faad14',
  };
  const rarityLabels: Record<string, string> = {
    common: '普通', uncommon: '优秀', rare: '稀有', epic: '史诗', legendary: '传说',
  };
  const typeLabels: Record<string, string> = {
    item: '物品', stamp_card: '印章卡', fragment: '碎片', costume: '装扮',
  };

  const columns = [
    { title: '编码', dataIndex: 'collectibleCode', width: 170 },
    { title: '名称', dataIndex: 'nameZh', width: 160 },
    {
      title: '类型', dataIndex: 'collectibleType', width: 90,
      render: (v: string) => <Tag>{typeLabels[v] || v}</Tag>,
    },
    {
      title: '稀有度', dataIndex: 'rarity', width: 90,
      render: (v: string) => <Tag color={rarityColors[v]}>{rarityLabels[v] || v}</Tag>,
    },
    {
      title: '图片', dataIndex: 'imageUrl', width: 80,
      render: (url: string | null) => url ? <Image src={url} width={40} height={40} style={{ borderRadius: 4 }} /> : '-',
    },
    { title: '系列ID', dataIndex: 'seriesId', width: 80 },
    { title: '来源', dataIndex: 'acquisitionSource', width: 100 },
    {
      title: '可重复', dataIndex: 'isRepeatable', width: 75,
      render: (v: number) => v ? <Tag color="green">是</Tag> : <Tag>否</Tag>,
    },
    {
      title: '限时', dataIndex: 'isLimited', width: 65,
      render: (v: number) => v ? <Tag color="orange">限</Tag> : '-',
    },
    { title: '状态', dataIndex: 'status', width: 65, render: (v: string) => v === '1' ? <Tag color="green">启</Tag> : <Tag>停</Tag> },
    {
      title: '操作', width: 120, fixed: 'right' as const,
      render: () => <Space><Button type="link" size="small">编辑</Button><Button type="link" size="small" danger icon={<DeleteOutlined />} /> </Space>,
    },
  ];

  return (
    <PageContainer title="收集物管理" subtitle="用户可收集的物品、碎片与特殊道具配置">
      <ProTable<CollectibleItem> columns={columns} rowKey="id"
        request={async (params) => {
          const res = await getCollectibles({
            pageNum: params.current, pageSize: params.pageSize,
            keyword: params.nameZh as string, rarity: params.rarity as string,
          });
          return { data: res.data?.list || [], success: res.success, total: res.data?.total || 0 };
        }}
        toolBarRender={() => [
          <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setDrawerVisible(true); }}>
            新增收集物
          </Button>,
        ]}
        search={{
          labelWidth: 'auto',
        }}
        options={{ density: false, setting: false }}
      />
      <Modal title="新增收集物" open={drawerVisible} onOk={async () => {
        try {
          const vals = await form.validateFields();
          await createCollectible(vals);
          message.success('创建成功');
          setDrawerVisible(false);
        } catch (e) {}
      }} onCancel={() => setDrawerVisible(false)} width={560}>
        <Form form={form} layout="vertical">
          <Form.Item name="collectibleCode" label="编码" rules={[{ required: true }]}><Input placeholder="item_xxx" /></Form.Item>
          <Form.Item name="nameZh" label="名称" rules={[{ required: true }]}><Input placeholder="葡式蛋挞" /></Form.Item>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="collectibleType" label="类型">
              <Select options={[
                { value: 'item', label: '物品' }, { value: 'stamp_card', label: '印章卡' },
                { value: 'fragment', label: '碎片' }, { value: 'costume', label: '装扮' },
              ]} defaultValue="item" />
            </Form.Item>
            <Form.Item name="rarity" label="稀有度">
              <Select options={[
                { value: 'common', label: '普通' }, { value: 'uncommon', label: '优秀' },
                { value: 'rare', label: '稀有' }, { value: 'epic', label: '史诗' }, { value: 'legendary', label: '传说' },
              ]} defaultValue="common" />
            </Form.Item>
          </Space>
          <Form.Item name="image_url" label="图片 URL"><Input placeholder="https://..." /></Form.Item>
          <Form.Item name="description_zh" label="描述"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default CollectibleManagement;
