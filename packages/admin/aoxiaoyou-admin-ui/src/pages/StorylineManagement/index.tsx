import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-layout';
import ProTable from '@ant-design/pro-table';
import { Button, DatePicker, Drawer, Form, Input, InputNumber, Popconfirm, Select, Space, Table, Tag, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import type { ActionType, ProColumns } from '@ant-design/pro-table';
import dayjs from 'dayjs';
import {
  createAdminStoryline,
  createStorylineChapter,
  deleteAdminStoryline,
  deleteStorylineChapter,
  getAdminStorylineDetail,
  getAdminStorylines,
  getStorylineChapters,
  updateAdminStoryline,
  updateStorylineChapter,
} from '../../services/api';
import type { AdminStoryChapterItem, AdminStorylineDetail, AdminStorylineListItem } from '../../types/admin';

const StorylineManagement: React.FC = () => {
  const actionRef = useRef<ActionType>();
  const [editorOpen, setEditorOpen] = useState(false);
  const [chapterDrawerOpen, setChapterDrawerOpen] = useState(false);
  const [editing, setEditing] = useState<AdminStorylineDetail | null>(null);
  const [chapterEditing, setChapterEditing] = useState<AdminStoryChapterItem | null>(null);
  const [chapterStoryline, setChapterStoryline] = useState<AdminStorylineListItem | null>(null);
  const [chapterList, setChapterList] = useState<AdminStoryChapterItem[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [chapterSubmitting, setChapterSubmitting] = useState(false);
  const [form] = Form.useForm();
  const [chapterForm] = Form.useForm();

  const loadChapters = async (storylineId: number) => {
    const response = await getStorylineChapters(storylineId, { pageNum: 1, pageSize: 100 });
    if (response.success) {
      setChapterList(response.data?.list || []);
    }
  };

  const openChapterDrawer = async (record: AdminStorylineListItem) => {
    setChapterStoryline(record);
    setChapterEditing(null);
    chapterForm.resetFields();
    await loadChapters(record.storylineId);
    setChapterDrawerOpen(true);
  };

  const columns = useMemo<ProColumns<AdminStorylineListItem>[]>(() => [
    { title: '名称', dataIndex: 'name' },
    { title: '编码', dataIndex: 'code', copyable: true, hideInSearch: true },
    {
      title: '分类',
      dataIndex: 'category',
      hideInSearch: true,
      render: (value) => <Tag>{value || 'historical'}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      valueType: 'select',
      valueEnum: {
        draft: { text: '草稿' },
        published: { text: '已发布' },
        archived: { text: '已归档' },
      },
      render: (_, record) => <Tag color={record.status === 'published' ? 'success' : 'default'}>{record.status}</Tag>,
    },
    { title: '章节数', dataIndex: 'poiCount', hideInSearch: true },
    { title: '参与人数', dataIndex: 'participationCount', hideInSearch: true },
    { title: '创建时间', dataIndex: 'createdAt', valueType: 'dateTime', hideInSearch: true },
    {
      title: '操作',
      key: 'action',
      valueType: 'option',
      render: (_, record) => [
        <Button
          key="edit"
          type="link"
          onClick={async () => {
            const response = await getAdminStorylineDetail(record.storylineId);
            if (response.success) {
              setEditing(response.data);
              form.setFieldsValue({
                code: response.data.code,
                nameZh: response.data.name,
                nameEn: response.data.code,
                description: response.data.description,
                coverUrl: response.data.coverImageUrl,
                bannerUrl: response.data.bannerImageUrl,
                totalChapters: response.data.totalChapters,
                category: response.data.category,
                difficulty: response.data.difficulty,
                estimatedDurationMinutes: response.data.estimatedDurationMinutes,
                tagsText: response.data.tags?.join(', '),
                status: response.data.status,
                publishAt: response.data.publishAt ? dayjs(response.data.publishAt) : undefined,
                startAt: response.data.startAt ? dayjs(response.data.startAt) : undefined,
                endAt: response.data.endAt ? dayjs(response.data.endAt) : undefined,
              });
              setEditorOpen(true);
            }
          }}
        >
          编辑
        </Button>,
        <Button key="chapters" type="link" onClick={() => openChapterDrawer(record)}>
          章节管理
        </Button>,
        <Popconfirm
          key="delete"
          title="确认删除该故事线吗？"
          onConfirm={async () => {
            await deleteAdminStoryline(record.storylineId);
            message.success('故事线已删除');
            actionRef.current?.reload();
          }}
        >
          <Button type="link" danger>删除</Button>
        </Popconfirm>,
      ],
    },
  ], [form]);

  return (
    <PageContainer
      title="故事线管理"
      subTitle="维护小程序故事线、运营节奏与章节内容配置"
      extra={[
        <Button
          key="add"
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => {
            setEditing(null);
            form.resetFields();
            form.setFieldsValue({ status: 'draft', totalChapters: 0, category: 'historical', difficulty: 'easy', estimatedDurationMinutes: 60 });
            setEditorOpen(true);
          }}
        >
          新建故事线
        </Button>,
      ]}
    >
      <ProTable<AdminStorylineListItem>
        actionRef={actionRef}
        rowKey="storylineId"
        columns={columns}
        request={async (params) => {
          const response = await getAdminStorylines({
            pageNum: params.current,
            pageSize: params.pageSize,
            keyword: params.name as string,
            status: params.status as string,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        search={{ labelWidth: 'auto' }}
        pagination={{ pageSize: 10 }}
      />

      <Drawer
        open={editorOpen}
        title={editing ? '编辑故事线' : '新建故事线'}
        width={620}
        onClose={() => setEditorOpen(false)}
        destroyOnClose
      >
        <Form
          layout="vertical"
          form={form}
          onFinish={async (values) => {
            setSubmitting(true);
            try {
              const payload = {
                ...values,
                tags: values.tagsText ? JSON.stringify(values.tagsText.split(',').map((item: string) => item.trim()).filter(Boolean)) : undefined,
                publishAt: values.publishAt ? values.publishAt.format('YYYY-MM-DDTHH:mm:ss') : undefined,
                startAt: values.startAt ? values.startAt.format('YYYY-MM-DDTHH:mm:ss') : undefined,
                endAt: values.endAt ? values.endAt.format('YYYY-MM-DDTHH:mm:ss') : undefined,
              };
              delete payload.tagsText;

              if (editing) {
                await updateAdminStoryline(editing.storylineId, payload);
                message.success('故事线更新成功');
              } else {
                await createAdminStoryline(payload);
                message.success('故事线创建成功');
              }
              setEditorOpen(false);
              actionRef.current?.reload();
            } finally {
              setSubmitting(false);
            }
          }}
        >
          <Form.Item label="故事线编码" name="code" rules={[{ required: true, message: '请输入编码' }]}><Input /></Form.Item>
          <Form.Item label="中文名称" name="nameZh" rules={[{ required: true, message: '请输入名称' }]}><Input /></Form.Item>
          <Form.Item label="英文名称" name="nameEn"><Input /></Form.Item>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="分类" name="category" style={{ width: '100%' }}><Select options={[{ label: '历史', value: 'historical' }, { label: '文化', value: 'cultural' }, { label: '美食', value: 'food' }, { label: '亲子', value: 'family' }]} /></Form.Item>
            <Form.Item label="难度" name="difficulty" style={{ width: '100%' }}><Select options={[{ label: '简单', value: 'easy' }, { label: '中等', value: 'medium' }, { label: '困难', value: 'hard' }]} /></Form.Item>
          </Space>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="章节数" name="totalChapters" style={{ width: '100%' }}><InputNumber min={0} style={{ width: '100%' }} disabled /></Form.Item>
            <Form.Item label="预计时长（分钟）" name="estimatedDurationMinutes" style={{ width: '100%' }}><InputNumber min={10} max={600} style={{ width: '100%' }} /></Form.Item>
          </Space>
          <Form.Item label="故事简介" name="description"><Input.TextArea rows={4} /></Form.Item>
          <Form.Item label="封面地址" name="coverUrl"><Input /></Form.Item>
          <Form.Item label="Banner 地址" name="bannerUrl"><Input /></Form.Item>
          <Form.Item label="标签（逗号分隔）" name="tagsText"><Input placeholder="海上丝路, 世界遗产, 文化探索" /></Form.Item>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="定时发布时间" name="publishAt" style={{ width: '100%' }}><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
            <Form.Item label="活动开始" name="startAt" style={{ width: '100%' }}><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
          </Space>
          <Space style={{ width: '100%' }} align="start">
            <Form.Item label="活动结束" name="endAt" style={{ width: '100%' }}><DatePicker showTime style={{ width: '100%' }} /></Form.Item>
            <Form.Item label="状态" name="status" style={{ width: '100%' }}>
              <Select options={[{ label: '草稿', value: 'draft' }, { label: '已发布', value: 'published' }, { label: '已归档', value: 'archived' }]} />
            </Form.Item>
          </Space>
          <Space>
            <Button type="primary" htmlType="submit" loading={submitting}>保存</Button>
            <Button onClick={() => setEditorOpen(false)}>取消</Button>
          </Space>
        </Form>
      </Drawer>

      <Drawer
        open={chapterDrawerOpen}
        title={chapterStoryline ? `章节管理 · ${chapterStoryline.name}` : '章节管理'}
        width={920}
        onClose={() => setChapterDrawerOpen(false)}
        destroyOnClose
      >
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <Button
            type="primary"
            onClick={() => {
              setChapterEditing(null);
              chapterForm.resetFields();
              chapterForm.setFieldsValue({ chapterOrder: chapterList.length + 1, mediaType: 'image', unlockType: 'sequential', duration: 180 });
            }}
          >
            新建章节
          </Button>

          <Table<AdminStoryChapterItem>
            rowKey="id"
            pagination={false}
            dataSource={chapterList}
            columns={[
              { title: '顺序', dataIndex: 'chapterOrder', width: 80 },
              { title: '章节标题', dataIndex: 'titleZh' },
              { title: '媒体类型', dataIndex: 'mediaType', width: 100, render: (value) => <Tag>{value || 'image'}</Tag> },
              { title: '解锁方式', dataIndex: 'unlockType', width: 120 },
              { title: '时长', dataIndex: 'duration', width: 90, render: (value) => `${value || 0}s` },
              {
                title: '操作',
                key: 'action',
                width: 180,
                render: (_, record) => (
                  <Space>
                    <Button
                      type="link"
                      onClick={() => {
                        setChapterEditing(record);
                        chapterForm.setFieldsValue(record);
                      }}
                    >
                      编辑
                    </Button>
                    <Popconfirm
                      title="确认删除该章节吗？"
                      onConfirm={async () => {
                        if (!chapterStoryline) return;
                        await deleteStorylineChapter(chapterStoryline.storylineId, record.id);
                        message.success('章节已删除');
                        await loadChapters(chapterStoryline.storylineId);
                        actionRef.current?.reload();
                      }}
                    >
                      <Button type="link" danger>删除</Button>
                    </Popconfirm>
                  </Space>
                ),
              },
            ]}
          />

          <Form
            layout="vertical"
            form={chapterForm}
            onFinish={async (values) => {
              if (!chapterStoryline) return;
              setChapterSubmitting(true);
              try {
                if (chapterEditing) {
                  await updateStorylineChapter(chapterStoryline.storylineId, chapterEditing.id, values);
                  message.success('章节更新成功');
                } else {
                  await createStorylineChapter(chapterStoryline.storylineId, values);
                  message.success('章节创建成功');
                }
                setChapterEditing(null);
                chapterForm.resetFields();
                await loadChapters(chapterStoryline.storylineId);
                actionRef.current?.reload();
              } finally {
                setChapterSubmitting(false);
              }
            }}
          >
            <Space style={{ width: '100%' }} align="start">
              <Form.Item label="章节顺序" name="chapterOrder" style={{ width: '100%' }} rules={[{ required: true, message: '请输入顺序' }]}>
                <InputNumber min={1} style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item label="媒体类型" name="mediaType" style={{ width: '100%' }}>
                <Select options={[{ label: '图片', value: 'image' }, { label: '音频', value: 'audio' }, { label: '视频', value: 'video' }]} />
              </Form.Item>
              <Form.Item label="时长（秒）" name="duration" style={{ width: '100%' }}>
                <InputNumber min={30} max={3600} style={{ width: '100%' }} />
              </Form.Item>
            </Space>
            <Form.Item label="章节标题" name="titleZh" rules={[{ required: true, message: '请输入章节标题' }]}><Input /></Form.Item>
            <Form.Item label="媒体地址" name="mediaUrl"><Input /></Form.Item>
            <Space style={{ width: '100%' }} align="start">
              <Form.Item label="解锁方式" name="unlockType" style={{ width: '100%' }}>
                <Select options={[{ label: '顺序解锁', value: 'sequential' }, { label: '印章解锁', value: 'stamp_count' }, { label: '时间解锁', value: 'time' }]} />
              </Form.Item>
              <Form.Item label="解锁参数" name="unlockParam" style={{ width: '100%' }}><Input /></Form.Item>
            </Space>
            <Form.Item label="章节脚本" name="scriptZh"><Input.TextArea rows={4} /></Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={chapterSubmitting}>{chapterEditing ? '更新章节' : '创建章节'}</Button>
              <Button onClick={() => { setChapterEditing(null); chapterForm.resetFields(); }}>清空</Button>
            </Space>
          </Form>
        </Space>
      </Drawer>
    </PageContainer>
  );
};

export default StorylineManagement;
