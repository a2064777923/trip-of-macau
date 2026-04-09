import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import { Modal, Form, Input, Select, Button, Space, Tag, Image, message } from 'antd';
import { PlusOutlined, EyeInvisibleOutlined } from '@ant-design/icons';
import { getBadges, createBadge, type BadgeItem } from '../../services/api';

const BadgeManagement: React.FC = () => {
  const [form] = Form.useForm();
  const [drawerVisible, setDrawerVisible] = React.useState(false);

  const badgeTypeLabels: Record<string, string> = {
    storyline: '故事线', city_exploration: '城市探索', collection: '收集',
    activity: '活动', special: '特别', hidden: '隐藏',
  };
  const rarityColors: Record<string, string> = {
    common: '#999999', rare: '#1677ff', epic: '#722ed1', legendary: '#faad14', hidden: '#eb2f96',
  };

  const columns = [
    { title: '编码', dataIndex: 'badgeCode', width: 160 },
    { title: '名称', dataIndex: 'nameZh', width: 160 },
    {
      title: '类型', dataIndex: 'badgeType', width: 110,
      render: (v: string) => <Tag>{badgeTypeLabels[v] || v}</Tag>,
    },
    {
      title: '稀有度', dataIndex: 'rarity', width: 85,
      render: (v: string) => <Tag color={rarityColors[v]}>{v.toUpperCase()}</Tag>,
    },
    { title: '隐藏', dataIndex: 'isHidden', width: 60,
      render: (v: number) => v ? <EyeInvisibleOutlined style={{ color: '#eb2f96' }} /> : '-',
    },
    {
      title: '图标', dataIndex: 'iconUrl', width: 72,
      render: (url: string | null) => url ? <Image src={url} width={32} height={32} style={{ borderRadius: 4 }} /> : '-',
    },
    { title: '状态', dataIndex: 'status', width: 60, render: (v: string) => v === '1' ? <Tag color="green">启</Tag> : '-' },
    {
      title: '操作', width: 100, fixed: 'right' as const,
      render: () => <Button type="link" size="small">规则</Button>,
    },
  ];

  return (
    <PageContainer title="徽章管理" subtitle="可获得的成就徽章与获取规则配置">
      <ProTable<BadgeItem> columns={columns} rowKey="id"
        request={async (params) => {
          const res = await getBadges({ pageNum: params.current, pageSize: params.pageSize });
          return { data: res.data?.list || [], success: res.success, total: res.data?.total || 0 };
        }}
        toolBarRender={() => [
          <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { form.resetFields(); setDrawerVisible(true); }}>
            新增徽章
          </Button>,
        ]}
        options={{ density: false, setting: false }}
      />
      <Modal title="新增徽章" open={drawerVisible} onOk={async () => {
        try {
          const vals = await form.validateFields();
          await createBadge(vals);
          message.success('创建成功');
          setDrawerVisible(false);
        } catch (e) {}
      }} onCancel={() => setDrawerVisible(false)} width={520}>
        <Form form={form} layout="vertical">
          <Form.Item name="badgeCode" label="徽章编码" rules={[{ required: true }]}><Input placeholder="badge_xxx" /></Form.Item>
          <Form.Item name="nameZh" label="名称" rules={[{ required: true }]}><Input placeholder="初次到访" /></Form.Item>
          <Form.Item name="description_zh" label="描述"><Input.TextArea rows={2} /></Form.Item>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="badgeType" label="类型">
              <Select options={[
                { value: 'storyline', label: '故事线' }, { value: 'city_exploration', label: '城市探索' },
                { value: 'collection', label: '收集' }, { value: 'activity', label: '活动' },
                { value: 'special', label: '特别' }, { value: 'hidden', label: '隐藏' },
              ]} defaultValue="special" />
            </Form.Item>
            <Form.Item name="rarity" label="稀有度">
              <Select options={[
                { value: 'common', label: '普通' }, { value: 'rare', label: '稀有' },
                { value: 'epic', label: '史诗' }, { value: 'legendary', label: '传说' }, { value: 'hidden', label: '隐藏' },
              ]} defaultValue="common" />
            </Form.Item>
          </Space>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="iconUrl" label="图标 URL"><Input /></Form.Item>
            <Form.Item name="imageUrl" label="展示图 URL"><Input /></Form.Item>
          </Space>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default BadgeManagement;
