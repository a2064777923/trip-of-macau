import React, { useMemo, useRef, useState } from 'react';
import { PageContainer } from '@ant-design/pro-components';
import ProTable, { type ActionType, type ProColumns } from '@ant-design/pro-table';
import {
  App as AntdApp,
  Button,
  Col,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Row,
  Select,
  Space,
  Tag,
} from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import {
  createAdminGameReward,
  deleteAdminGameReward,
  getAdminGameRewardDetail,
  getAdminGameRewards,
  updateAdminGameReward,
} from '../../services/api';
import type { AdminGameRewardItem } from '../../types/admin';
import {
  buildGameRewardPayload,
  CollectionBindingSection,
  CollectionLocalizedCoreFields,
  CollectionMediaSection,
  GameRewardConfigSection,
  honorRewardTypeOptions,
  rarityOptions,
  RewardRelationSection,
  rewardStatusOptions,
  rewardTypeOptions,
  type GameRewardFormValues,
  useRewardDomainReferenceData,
  withGameRewardDefaults,
} from '../../components/rewards/RewardDomainShared';

function renderStatus(status?: string) {
  if (status === 'published') {
    return <Tag color="green">已發佈</Tag>;
  }
  if (status === 'archived') {
    return <Tag>已封存</Tag>;
  }
  return <Tag color="orange">編輯中</Tag>;
}

function optionLabel(options: Array<{ label: string; value: string | number }>, value?: string | number | null) {
  return options.find((item) => item.value === value)?.label || value || '-';
}

interface GameRewardWorkspaceProps {
  honorsOnly?: boolean;
  title?: string;
  subTitle?: string;
}

export const GameRewardWorkspace: React.FC<GameRewardWorkspaceProps> = ({
  honorsOnly = false,
  title = '遊戲內獎勵配置',
  subTitle = '配置徽章、稱號、城市限定貨幣、碎片、語音包與其他遊戲內獎勵，並共用獲得規則與演出。',
}) => {
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<GameRewardFormValues>();
  const actionRef = useRef<ActionType>();
  const [modalOpen, setModalOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingItem, setEditingItem] = useState<AdminGameRewardItem | null>(null);
  const references = useRewardDomainReferenceData(form);

  const rewardTypeSource = honorsOnly ? honorRewardTypeOptions : rewardTypeOptions;

  const columns = useMemo<ProColumns<AdminGameRewardItem>[]>(
    () => [
      {
        title: '名稱',
        dataIndex: 'nameZht',
        width: 260,
        render: (_, record) => record.nameZht || record.nameZh || record.code,
      },
      {
        title: '代碼',
        dataIndex: 'code',
        width: 220,
      },
      {
        title: '類型 / 稀有度',
        width: 220,
        render: (_, record) => (
          <Space wrap>
            <Tag color="blue">{optionLabel(rewardTypeSource, record.rewardType)}</Tag>
            <Tag>{optionLabel(rarityOptions, record.rarity)}</Tag>
          </Space>
        ),
      },
      {
        title: '持有規則',
        width: 220,
        render: (_, record) => (
          <Space wrap>
            <Tag>{record.stackable ? '可堆疊' : '單次持有'}</Tag>
            <Tag>{record.canEquip ? '可裝備' : '不可裝備'}</Tag>
            <Tag>{record.canConsume ? '可消耗' : '不可消耗'}</Tag>
          </Space>
        ),
      },
      {
        title: '規則 / 演出',
        width: 220,
        render: (_, record) => (
          <Space wrap>
            <Tag>{record.linkedRules?.length || 0} 條規則</Tag>
            <Tag color="purple">{record.presentation?.presentationType || '未設定演出'}</Tag>
          </Space>
        ),
      },
      {
        title: '狀態',
        dataIndex: 'status',
        width: 120,
        render: (_, record) => renderStatus(record.status),
      },
      {
        title: '操作',
        width: 160,
        valueType: 'option',
        render: (_, record) => [
          <a
            key="edit"
            onClick={async () => {
              const response = await getAdminGameRewardDetail(record.id);
              if (!response.success || !response.data) {
                message.error(response.message || '載入遊戲內獎勵詳情失敗');
                return;
              }
              setEditingItem(response.data);
              form.setFieldsValue(withGameRewardDefaults(response.data));
              setModalOpen(true);
            }}
          >
            編輯
          </a>,
          <Popconfirm
            key="delete"
            title={`確定刪除此${honorsOnly ? '榮譽獎勵' : '遊戲內獎勵'}？`}
            onConfirm={async () => {
              const response = await deleteAdminGameReward(record.id);
              if (!response.success) {
                message.error(response.message || '刪除失敗');
                return;
              }
              message.success(`已刪除${honorsOnly ? '榮譽獎勵' : '遊戲內獎勵'}`);
              actionRef.current?.reload();
            }}
          >
            <a>刪除</a>
          </Popconfirm>,
        ],
      },
    ],
    [form, honorsOnly, message, rewardTypeSource],
  );

  const openCreateModal = () => {
    setEditingItem(null);
    form.resetFields();
    form.setFieldsValue(
      withGameRewardDefaults({
        rewardType: honorsOnly ? 'badge' : 'city_fragment',
        maxOwned: honorsOnly ? 1 : 99,
      }),
    );
    setModalOpen(true);
  };

  const submitForm = async () => {
    setSubmitting(true);
    try {
      const values = await form.validateFields();
      const payload = buildGameRewardPayload(values);
      const response = editingItem
        ? await updateAdminGameReward(editingItem.id, payload)
        : await createAdminGameReward(payload);
      if (!response.success) {
        message.error(response.message || '儲存遊戲內獎勵失敗');
        return;
      }
      message.success(editingItem ? '遊戲內獎勵已更新' : '遊戲內獎勵已建立');
      setModalOpen(false);
      actionRef.current?.reload();
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <PageContainer title={title} subTitle={subTitle}>
      <ProTable<AdminGameRewardItem>
        actionRef={actionRef}
        rowKey="id"
        columns={columns}
        search={false}
        request={async (params) => {
          const response = await getAdminGameRewards({
            pageNum: params.current,
            pageSize: params.pageSize,
            honorsOnly,
          });
          return {
            data: response.data?.list || [],
            success: response.success,
            total: response.data?.total || 0,
          };
        }}
        toolBarRender={() => [
          <Button key="create" type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            {honorsOnly ? '新增榮譽 / 稱號' : '新增遊戲內獎勵'}
          </Button>,
        ]}
      />

      <Modal
        title={editingItem ? `編輯：${editingItem.nameZht || editingItem.nameZh || editingItem.code}` : honorsOnly ? '新增榮譽 / 稱號' : '新增遊戲內獎勵'}
        open={modalOpen}
        onOk={submitForm}
        confirmLoading={submitting}
        onCancel={() => setModalOpen(false)}
        width={1280}
        destroyOnHidden
      >
        <Form form={form} layout="vertical">
          <Row gutter={[16, 16]}>
            <Col xs={24} md={10}>
              <Form.Item
                name="code"
                label="獎勵代碼"
                rules={[{ required: true, message: '請輸入獎勵代碼' }]}
              >
                <Input placeholder={honorsOnly ? 'honor_macau_archivist' : 'reward_macau_fragment_fire'} />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="status" label="狀態">
                <Select options={rewardStatusOptions} />
              </Form.Item>
            </Col>
            <Col xs={24} md={4}>
              <Form.Item name="sortOrder" label="排序">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col xs={24} md={5}>
              <Form.Item name="publishStartAt" label="上線時間">
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={[16, 16]}>
            <Col xs={24} md={8}>
              <Form.Item name="publishEndAt" label="下線時間">
                <DatePicker showTime style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <CollectionLocalizedCoreFields
            form={form}
            translationDefaults={references.translationDefaults}
            entityLabel={honorsOnly ? '榮譽獎勵' : '遊戲內獎勵'}
            includeSubtitle
            includeHighlight
            includeExampleContent={false}
          />

          <GameRewardConfigSection form={form} rewardTypeScope={honorsOnly ? 'honor' : 'all'} />
          <RewardRelationSection form={form} references={references} rewardFamily="game" />
          <CollectionBindingSection form={form} options={references} />
          <CollectionMediaSection
            includeIcon
            includeAnimation
            attachmentHelp={honorsOnly ? '可補充稱號故事、徽章動效、全屏影片、語音播報或其他展示素材。' : '可補充碎片圖集、語音包說明、獲得影片與展示素材。'}
          />
        </Form>
      </Modal>
    </PageContainer>
  );
};

const GameRewardManagement: React.FC = () => (
  <GameRewardWorkspace />
);

export default GameRewardManagement;

