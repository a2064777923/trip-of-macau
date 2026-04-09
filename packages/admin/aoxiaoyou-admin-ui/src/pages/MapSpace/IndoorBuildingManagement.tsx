import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import type { ActionType } from '@ant-design/pro-table';
import { Modal, Form, Input, InputNumber, Select, Button, Space, Tag, message } from 'antd';
import { PlusOutlined, EditOutlined } from '@ant-design/icons';
import { getBuildings, createBuilding, updateBuilding, type BuildingItem } from '../../services/api';

const IndoorBuildingManagement: React.FC = () => {
  const [form] = Form.useForm();
  const actionRef = React.useRef<ActionType>();
  const [drawerVisible, setDrawerVisible] = React.useState(false);
  const [editingId, setEditingId] = React.useState<number | null>(null);

  const columns = [
    { title: '编码', dataIndex: 'buildingCode', width: 150 },
    { title: '名称', dataIndex: 'nameZh', width: 200 },
    { title: '地址', dataIndex: 'addressZh', ellipsis: true },
    { title: '所在城市', dataIndex: 'cityCode', width: 90 },
    {
      title: '坐标',
      render: (_: any, r: BuildingItem) => (r.lat && r.lng ? `${r.lat}, ${r.lng}` : '-'),
      width: 150,
    },
    { title: '总楼层数', dataIndex: 'totalFloors', width: 80 },
    { title: '状态', dataIndex: 'status', width: 70, render: (v: string) => v === '1' ? <Tag color="green">启用</Tag> : <Tag color="red">停用</Tag> },
    {
      title: '操作', width: 140, fixed: 'right' as const,
      render: (_: any, r: BuildingItem) => (
        <Button type="link" size="small" icon={<EditOutlined />} onClick={() => { setEditingId(r.id); form.setFieldsValue(r); setDrawerVisible(true); }}>
          编辑
        </Button>
      ),
    },
  ];

  return (
    <PageContainer title="建筑物管理" subtitle="大型建筑的室内地图与楼层节点配置入口">
      <ProTable<BuildingItem> columns={columns} rowKey="id" actionRef={actionRef}
        request={async (params) => {
          const res = await getBuildings({ pageNum: params.current, pageSize: params.pageSize, cityCode: params.cityCode as string });
          return { data: res.data?.list || [], success: res.success, total: res.data?.total || 0 };
        }}
        toolBarRender={() => [
          <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { setEditingId(null); form.resetFields(); setDrawerVisible(true); }}>
            新建建筑
          </Button>,
        ]}
        search={{
          labelWidth: 'auto',
        }}
        options={{ density: false, setting: false }}
      />
      <Modal title={editingId ? '编辑建筑' : '新建建筑'} open={drawerVisible}
        onOk={async () => {
          try {
            const values = await form.validateFields();
            editingId ? await updateBuilding(editingId, values) : await createBuilding(values);
            message.success(editingId ? '更新成功' : '创建成功');
            setDrawerVisible(false);
            actionRef.current?.reload();
          } catch (e) {}
        }}
        onCancel={() => setDrawerVisible(false)} width={600}>
        <Form form={form} layout="vertical">
          <Form.Item name="buildingCode" label="建筑编码" rules={[{ required: true }]}>
            <Input placeholder="如 venetian_macau, grand_lisboa" />
          </Form.Item>
          <Form.Item name="nameZh" label="名称" rules={[{ required: true }]}>
            <Input placeholder="威尼斯人" />
          </Form.Item>
          <Form.Item name="addressZh" label="地址">
            <Input placeholder="路氹金光大道" />
          </Form.Item>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="cityCode" label="城市">
              <Select options={[
                { value: 'macau', label: '澳门' },
                { value: 'hongkong', label: '香港' },
                { value: 'zhuhai', label: '珠海' },
              ]} defaultValue="macau" />
            </Form.Item>
            <Form.Item name="totalFloors" label="总楼层数">
              <InputNumber min={1} max={200} defaultValue={1} style={{ width: '100%' }} />
            </Form.Item>
          </Space>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="lat" label="入口纬度"><Input type="number" step="0.000001" /></Form.Item>
            <Form.Item name="lng" label="入口经度"><Input type="number" step="0.000001" /></Form.Item>
          </Space>
          <Form.Item name="coverImageUrl" label="外观图 URL"><Input /></Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default IndoorBuildingManagement;
