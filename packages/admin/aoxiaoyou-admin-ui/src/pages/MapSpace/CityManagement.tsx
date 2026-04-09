import React from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable from '@ant-design/pro-table';
import { Modal, Form, Input, Select, Button, Space, Tag, message } from 'antd';
import { PlusOutlined, EditOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { getCities, getCityDetail, createCity, updateCity, publishCity, type CityItem } from '../../services/api';

const CityManagement: React.FC = () => {
  const [form] = Form.useForm();
  const [drawerVisible, setDrawerVisible] = React.useState(false);
  const [editingId, setEditingId] = React.useState<number | null>(null);
  const [loadingDetail, setLoadingDetail] = React.useState(false);

  const columns = [
    {
      title: '编码',
      dataIndex: 'code',
      width: 100,
    },
    {
      title: '中文名',
      dataIndex: 'nameZh',
      width: 140,
    },
    {
      title: '英文名',
      dataIndex: 'nameEn',
      width: 120,
    },
    {
      title: '国家/地区',
      dataIndex: 'countryCode',
      width: 90,
    },
    {
      title: '中心坐标',
      render: (_: any, record: CityItem) =>
        record.centerLat && record.centerLng ? `${record.centerLat}, ${record.centerLng}` : '-',
      width: 160,
    },
    {
      title: '默认缩放',
      dataIndex: 'defaultZoom',
      width: 80,
    },
    {
      title: '解锁方式',
      dataIndex: 'unlockType',
      width: 100,
      render: (val: string) => ({ auto: '自动', manual: '手动', condition: '条件' }[val] || val),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 80,
      render: (val: string) => (
        <Tag color={val === '1' ? 'green' : val === '2' ? 'red' : 'default'}>
          {{ '1': '已发布', '0': '草稿', '2': '下线' }[val] || val}
        </Tag>
      ),
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right' as const,
      render: (_: any, record: CityItem) => (
        <Space>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEditor(record.id)}>
            编辑
          </Button>
          {record.status !== '1' && (
            <Button type="link" size="small" icon={<CheckCircleOutlined />} onClick={() => handlePublish(record.id)}>
              发布
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const openEditor = async (id?: number) => {
    if (id) {
      setEditingId(id);
      setLoadingDetail(true);
      const res = await getCityDetail(id);
      if (res.success && res.data) {
        form.setFieldsValue({
          code: res.data.code,
          nameZh: res.data.nameZh,
          nameEn: res.data.nameEn,
          countryCode: res.data.countryCode || 'MO',
          centerLat: res.data.centerLat,
          centerLng: res.data.centerLng,
          defaultZoom: res.data.defaultZoom || 14,
          unlockType: res.data.unlockType || 'auto',
          coverImageUrl: res.data.coverImageUrl,
        });
      }
      setLoadingDetail(false);
    } else {
      setEditingId(null);
      form.resetFields();
      form.setFieldsValue({ countryCode: 'MO', defaultZoom: 14, unlockType: 'auto' });
    }
    setDrawerVisible(true);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingId) {
        await updateCity(editingId, values);
        message.success('城市更新成功');
      } else {
        await createCity(values);
        message.success('城市创建成功');
      }
      setDrawerVisible(false);
    } catch (e) {}
  };

  const handlePublish = async (id: number) => {
    await publishCity(id);
    message.success('城市已发布');
  };

  return (
    <PageContainer title="城市管理" subtitle="多城市地图与空间管理的基础配置">
      <ProTable<CityItem> columns={columns} rowKey="id"
        request={async (params) => {
          const res = await getCities({ pageNum: params.current, pageSize: params.pageSize, keyword: params.nameZh as string, status: params.status as string });
          return { data: res.data?.list || [], success: res.success, total: res.data?.total || 0 };
        }}
        toolBarRender={() => [
          <Button key="add" type="primary" icon={<PlusOutlined />} onClick={() => openEditor()}>
            新建城市
          </Button>,
        ]}
        search={{
          labelWidth: 'auto',
          defaultCollapsed: false,
        }}
        options={{ density: false, setting: false }}
      />

      <Modal title={editingId ? '编辑城市' : '新建城市'} open={drawerVisible}
        onOk={handleSubmit} onCancel={() => setDrawerVisible(false)} width={640}
        confirmLoading={loadingDetail}>
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="城市编码" rules={[{ required: true }]}>
            <Input placeholder="如 macau, hongkong, zhuhai" />
          </Form.Item>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="nameZh" label="中文名" rules={[{ required: true }]}>
              <Input placeholder="澳门" />
            </Form.Item>
            <Form.Item name="nameEn" label="英文名">
              <Input placeholder="Macau" />
            </Form.Item>
          </Space>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="countryCode" label="国家/地区">
              <Select options={[{ value: 'MO', label: '澳门 MO' }, { value: 'HK', label: '香港 HK' }, { value: 'CN', label: '中国大陆 CN' }]} />
            </Form.Item>
            <Form.Item name="unlockType" label="解锁方式">
              <Select options={[{ value: 'auto', label: '自动' }, { value: 'manual', label: '手动' }, { value: 'condition', label: '条件' }]} />
            </Form.Item>
          </Space>
          <Space style={{ display: 'flex', width: '100%' }} size={16}>
            <Form.Item name="centerLat" label="中心纬度">
              <Input type="number" step="0.000001" placeholder="22.198000" />
            </Form.Item>
            <Form.Item name="centerLng" label="中心经度">
              <Input type="number" step="0.000001" placeholder="113.556000" />
            </Form.Item>
            <Form.Item name="defaultZoom" label="默认缩放">
              <Input type="number" min={1} max={22} placeholder="14" />
            </Form.Item>
          </Space>
          <Form.Item name="coverImageUrl" label="封面图 URL">
            <Input.TextArea rows={2} placeholder="https://..." />
          </Form.Item>
        </Form>
      </Modal>
    </PageContainer>
  );
};

export default CityManagement;
