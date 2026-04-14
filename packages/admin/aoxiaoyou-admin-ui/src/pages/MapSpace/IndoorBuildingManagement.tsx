import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import type { ActionType } from '@ant-design/pro-table';
import { Button, Form, Input, InputNumber, message, Modal, Select, Space, Tag } from 'antd';
import { EditOutlined, PlusOutlined } from '@ant-design/icons';
import { createBuilding, getBuildings, updateBuilding, type BuildingItem } from '../../services/api';

const IndoorBuildingManagement: React.FC = () => {
  const [form] = Form.useForm();
  const actionRef = React.useRef<ActionType>();
  const [modalOpen, setModalOpen] = React.useState(false);
  const [editingId, setEditingId] = React.useState<number | null>(null);

  const columns = [
    { title: 'Code', dataIndex: 'buildingCode', width: 150 },
    { title: 'Name', dataIndex: 'nameZh', width: 200 },
    { title: 'Address', dataIndex: 'addressZh', ellipsis: true },
    { title: 'City', dataIndex: 'cityCode', width: 90 },
    { title: 'Coordinates', width: 150, render: (_: any, record: BuildingItem) => (record.lat && record.lng ? `${record.lat}, ${record.lng}` : '-') },
    { title: 'Floors', dataIndex: 'totalFloors', width: 80 },
    { title: 'Status', dataIndex: 'status', width: 80, render: (value: string) => <Tag color={value === '1' ? 'green' : 'red'}>{value === '1' ? 'active' : 'inactive'}</Tag> },
    {
      title: 'Actions',
      width: 140,
      fixed: 'right' as const,
      render: (_: any, record: BuildingItem) => (
        <Button type="link" size="small" icon={<EditOutlined />} onClick={() => { setEditingId(record.id); form.setFieldsValue(record); setModalOpen(true); }}>
          Edit
        </Button>
      ),
    },
  ];

  return (
    <PageContainer title="Indoor Building Management" subTitle="Indoor map and floor-node entry point for large buildings">
      <ProTable<BuildingItem>
        columns={columns}
        rowKey="id"
        actionRef={actionRef}
        request={async (params) => {
          const res = await getBuildings({ pageNum: params.current, pageSize: params.pageSize, cityCode: params.cityCode as string });
          return { data: res.data?.list || [], success: res.success, total: res.data?.total || 0 };
        }}
        toolBarRender={() => [
          <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => { setEditingId(null); form.resetFields(); setModalOpen(true); }}>
            New Building
          </Button>,
        ]}
        search={{ labelWidth: 'auto' }}
        options={{ density: false, setting: false }}
      />
      <Modal
        title={editingId ? 'Edit Building' : 'Create Building'}
        open={modalOpen}
        onOk={async () => {
          const values = await form.validateFields();
          if (editingId) {
            await updateBuilding(editingId, values);
          } else {
            await createBuilding(values);
          }
          message.success(editingId ? 'Building updated' : 'Building created');
          setModalOpen(false);
          actionRef.current?.reload();
        }}
        onCancel={() => setModalOpen(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="buildingCode" label="Building Code" rules={[{ required: true }]}><Input placeholder="venetian_macau" /></Form.Item>
          <Form.Item name="nameZh" label="Name" rules={[{ required: true }]}><Input placeholder="Indoor building name" /></Form.Item>
          <Form.Item name="addressZh" label="Address"><Input /></Form.Item>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="cityCode" label="City">
              <Select options={[{ value: 'macau', label: 'Macau' }, { value: 'hongkong', label: 'Hong Kong' }, { value: 'zhuhai', label: 'Zhuhai' }]} defaultValue="macau" />
            </Form.Item>
            <Form.Item name="totalFloors" label="Total Floors">
              <InputNumber min={1} max={200} defaultValue={1} style={{ width: '100%' }} />
            </Form.Item>
          </Space>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="lat" label="Latitude"><Input type="number" step="0.000001" /></Form.Item>
            <Form.Item name="lng" label="Longitude"><Input type="number" step="0.000001" /></Form.Item>
          </Space>
          <Form.Item name="coverImageUrl" label="Cover Image URL"><Input /></Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default IndoorBuildingManagement;
