import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import { Button, Drawer, Form, Input, InputNumber, message, Select, Space, Tag, Typography } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EnvironmentOutlined } from '@ant-design/icons';
import type { ProColumns, ActionType } from '@ant-design/pro-table';
import { createAdminPoi, deleteAdminPoi, getAdminPoiDetail, getAdminPois, getAdminStorylines, updateAdminPoi } from '../../services/api';
import type { AdminPoiDetail, AdminPoiListItem, AdminStorylineListItem } from '../../types/admin';

const { Text } = Typography;

const regionOptions = [
  { label: '澳门半岛', value: 'macau_central' },
  { label: '氹仔', value: 'macau_taipa' },
  { label: '路氹', value: 'macau_cotai' },
];

const poiTypeOptions = [
  { label: '故事点', value: 'story_point' },
  { label: '地标', value: 'landmark' },
  { label: '博物馆', value: 'museum' },
  { label: '餐饮', value: 'restaurant' },
  { label: '活动场地', value: 'event_venue' },
];

const POIManagement: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [editorOpen, setEditorOpen] = useState(false);
  const [editing, setEditing] = useState<AdminPoiDetail | null>(null);
  const [storylineOptions, setStorylineOptions] = useState<AdminStorylineListItem[]>([]);
  const [form] = Form.useForm();

  const loadStorylines = async () => {
    const response = await getAdminStorylines({ pageNum: 1, pageSize: 100 });
    setStorylineOptions(response.data?.list || []);
  };

  const openCreate = async () => {
    await loadStorylines();
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({
      regionCode: 'macau_central',
      poiType: 'story_point',
      checkInMethod: 'gps_only',
      importance: 'normal',
      triggerRadius: 50,
      difficulty: 'easy',
      suggestedVisitMinutes: 30,
      status: 'published',
      openTime: '09:00-18:00',
    });
    setEditorOpen(true);
  };

  const openEdit = async (record: AdminPoiListItem) => {
    await loadStorylines();
    const response = await getAdminPoiDetail(record.poiId);
    if (!response.success || !response.data) return;
    const detail = response.data;
    setEditing(detail);
    form.setFieldsValue({
      ...detail,
      nameZh: detail.name,
      nameEn: detail.subtitle,
      storyLineId: detail.storylineId,
      tagsText: detail.tags?.join(', '),
      imageUrlsText: detail.imageUrls?.join('\n'),
    });
    setEditorOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteAdminPoi(id);
      message.success('删除成功');
      actionRef.current?.reload();
    } catch {
      message.error('删除失败，请重试');
    }
  };

  const columns = useMemo<ProColumns<AdminPoiListItem>[]>(() => [
    {
      title: 'POI 名称',
      dataIndex: 'name',
      copyable: true,
      render: (_, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{record.name}</Text>
          <Text type="secondary">{record.subtitle || '未配置副标题'}</Text>
        </Space>
      ),
    },
    {
      title: '区域',
      dataIndex: 'regionName',
      hideInSearch: true,
      render: (value) => value || '澳门半岛',
    },
    {
      title: '分类',
      dataIndex: 'categoryName',
      hideInSearch: true,
      render: (value) => value || '-',
    },
    {
      title: '重要性',
      dataIndex: 'importance',
      valueType: 'select',
      valueEnum: {
        very_important: { text: '非常重要' },
        important: { text: '重要' },
        normal: { text: '普通' },
      },
      render: (_, record) => <Tag color={record.importance === 'very_important' ? 'red' : record.importance === 'important' ? 'orange' : 'green'}>{record.importance || 'normal'}</Tag>,
    },
    {
      title: '故事线',
      dataIndex: 'storylineName',
      hideInSearch: true,
      render: (value) => value || '-',
    },
    {
      title: '触发半径',
      dataIndex: 'geofenceRadius',
      hideInSearch: true,
      render: (_, record) => (
        <Space>
          <EnvironmentOutlined />
          {record.geofenceRadius || 0} 米
        </Space>
      ),
    },
    {
      title: '坐标',
      dataIndex: 'latitude',
      hideInSearch: true,
      render: (_, record) => <Text type="secondary">{record.latitude?.toFixed?.(4)}, {record.longitude?.toFixed?.(4)}</Text>,
    },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      render: (_, record) => [
        <Button key="edit" size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>编辑</Button>,
        <Button key="delete" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.poiId)}>删除</Button>,
      ],
    },
  ], []);

  return (
    <PageContainer
      title="POI 管理"
      subTitle="管理景点位置信息、地理围栏、多媒体资源与故事线挂载关系"
      extra={[<Button key="add" type="primary" icon={<PlusOutlined />} onClick={openCreate}>添加 POI</Button>]}
    >
      <ProTable<AdminPoiListItem>
        actionRef={actionRef}
        rowKey="poiId"
        columns={columns}
        request={async (params) => {
          const response = await getAdminPois({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.name as string,
          });
          return {
            data: response.data?.list || [],
            total: response.data?.total || 0,
            success: response.success,
          };
        }}
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
        dateFormatter="string"
        headerTitle="POI 列表"
        toolBarRender={() => [<Button key="export">导出数据</Button>]}
      />

      <Drawer
        open={editorOpen}
        title={editing ? '编辑 POI' : '新建 POI'}
        width={680}
        onClose={() => setEditorOpen(false)}
        destroyOnClose
      >
        <Form
          layout="vertical"
          form={form}
          onFinish={async (values) => {
            const payload = {
              ...values,
              imageUrls: values.imageUrlsText ? JSON.stringify(values.imageUrlsText.split('\n').map((item: string) => item.trim()).filter(Boolean)) : undefined,
              tags: values.tagsText ? JSON.stringify(values.tagsText.split(',').map((item: string) => item.trim()).filter(Boolean)) : undefined,
            };
            delete payload.tagsText;
            delete payload.imageUrlsText;

            if (editing) {
              await updateAdminPoi(editing.poiId, payload);
              message.success('POI 更新成功');
            } else {
              await createAdminPoi(payload);
              message.success('POI 创建成功');
            }
            setEditorOpen(false);
            actionRef.current?.reload();
          }}
        >
          <Form.Item label="中文名称" name="nameZh" rules={[{ required: true, message: '请输入中文名称' }]}><Input /></Form.Item>
          <Form.Item label="副标题 / 英文名" name="subtitle"><Input /></Form.Item>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="纬度" name="latitude" rules={[{ required: true, message: '请输入纬度' }]} style={{ width: '100%' }}><InputNumber style={{ width: '100%' }} step={0.000001} /></Form.Item>
            <Form.Item label="经度" name="longitude" rules={[{ required: true, message: '请输入经度' }]} style={{ width: '100%' }}><InputNumber style={{ width: '100%' }} step={0.000001} /></Form.Item>
          </Space>
          <Form.Item label="地址" name="address"><Input /></Form.Item>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="区域" name="regionCode" style={{ width: '100%' }}><Select options={regionOptions} /></Form.Item>
            <Form.Item label="点位类型" name="poiType" style={{ width: '100%' }}><Select options={poiTypeOptions} /></Form.Item>
          </Space>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="故事线" name="storyLineId" style={{ width: '100%' }}><Select allowClear options={storylineOptions.map((item) => ({ label: item.name, value: item.storylineId }))} /></Form.Item>
            <Form.Item label="重要性" name="importance" style={{ width: '100%' }}><Select options={[{ label: '普通', value: 'normal' }, { label: '重要', value: 'important' }, { label: '非常重要', value: 'very_important' }]} /></Form.Item>
          </Space>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="打卡方式" name="checkInMethod" style={{ width: '100%' }}><Select options={[{ label: '仅 GPS', value: 'gps_only' }, { label: '管理员补签', value: 'manual_admin' }, { label: '二维码', value: 'qr_code' }]} /></Form.Item>
            <Form.Item label="触发半径（米）" name="triggerRadius" style={{ width: '100%' }}><InputNumber style={{ width: '100%' }} min={10} max={300} /></Form.Item>
          </Space>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="难度" name="difficulty" style={{ width: '100%' }}><Select options={[{ label: '简单', value: 'easy' }, { label: '中等', value: 'medium' }, { label: '困难', value: 'hard' }]} /></Form.Item>
            <Form.Item label="建议游览时长（分钟）" name="suggestedVisitMinutes" style={{ width: '100%' }}><InputNumber style={{ width: '100%' }} min={5} max={240} /></Form.Item>
          </Space>
          <Form.Item label="开放时间" name="openTime"><Input placeholder="例如：09:00-18:00" /></Form.Item>
          <Form.Item label="封面图 URL" name="coverImageUrl"><Input /></Form.Item>
          <Form.Item label="图片列表（每行一个 URL）" name="imageUrlsText"><Input.TextArea rows={3} /></Form.Item>
          <Form.Item label="语音导览 URL" name="audioGuideUrl"><Input /></Form.Item>
          <Form.Item label="视频 URL" name="videoUrl"><Input /></Form.Item>
          <Form.Item label="AR 内容 URL" name="arContentUrl"><Input /></Form.Item>
          <Form.Item label="标签（逗号分隔）" name="tagsText"><Input placeholder="世遗, 打卡, 夜游" /></Form.Item>
          <Form.Item label="印章类型" name="stampType"><Input placeholder="location / story / mission" /></Form.Item>
          <Form.Item label="描述" name="description"><Input.TextArea rows={5} /></Form.Item>
          <Space>
            <Button type="primary" htmlType="submit">保存</Button>
            <Button onClick={() => setEditorOpen(false)}>取消</Button>
          </Space>
        </Form>
      </Drawer>
    </PageContainer>
  );
};

export default POIManagement;
