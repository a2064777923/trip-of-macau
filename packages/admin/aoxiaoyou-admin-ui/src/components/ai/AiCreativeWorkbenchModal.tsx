import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useRequest } from 'ahooks';
import {
  App as AntdApp,
  Button,
  Card,
  Col,
  Divider,
  Empty,
  Form,
  Input,
  Modal,
  Row,
  Select,
  Space,
  Tag,
  Typography,
} from 'antd';
import {
  createAiGenerationJob,
  finalizeAiGenerationCandidate,
  getAiCapabilities,
  getAiGenerationJobDetail,
  getAiInventory,
  getAiPolicies,
  getAiPromptTemplates,
  getAiProviders,
  refreshAiGenerationJob,
  restoreAiGenerationCandidate,
  type AiCandidateFinalizePayload,
  type AiGenerationCandidateItem,
  type AiGenerationJobItem,
  type AiGenerationJobPayload,
} from '../../services/api';
import AiTtsWorkbenchFields from './AiTtsWorkbenchFields';

const { Paragraph, Text, Title } = Typography;

function statusColor(status?: string) {
  if (status === 'completed') {
    return 'green';
  }
  if (status === 'submitted' || status === 'pending') {
    return 'gold';
  }
  if (status === 'failed') {
    return 'red';
  }
  return 'default';
}

export interface AiCreativeWorkbenchModalProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  defaultCapabilityCode?: string;
  defaultGenerationType?: string;
  defaultPromptTitle?: string;
  defaultPromptText?: string;
  defaultSourceScope?: string;
  defaultSourceScopeId?: number;
  defaultAssetKind?: string;
  onFinalized?: (payload: {
    job: AiGenerationJobItem;
    candidate: AiGenerationCandidateItem;
    assetId?: number;
  }) => void;
}

const previewBoxStyle: React.CSSProperties = {
  width: '100%',
  minHeight: 220,
  borderRadius: 18,
  background: '#f5f7fb',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  overflow: 'hidden',
};

const AiCreativeWorkbenchModal: React.FC<AiCreativeWorkbenchModalProps> = ({
  open,
  onClose,
  title = 'AI 創作工作台',
  defaultCapabilityCode,
  defaultGenerationType,
  defaultPromptTitle,
  defaultPromptText,
  defaultSourceScope,
  defaultSourceScopeId,
  defaultAssetKind,
  onFinalized,
}) => {
  const { message } = AntdApp.useApp();
  const [form] = Form.useForm<AiGenerationJobPayload>();
  const [activeJob, setActiveJob] = useState<AiGenerationJobItem | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const submittingRef = useRef(false);

  const capabilitiesReq = useRequest(() => getAiCapabilities());
  const providersReq = useRequest(() => getAiProviders());
  const capabilityCode = Form.useWatch('capabilityCode', form);
  const providerId = Form.useWatch('providerId', form);

  const policiesReq = useRequest(() => getAiPolicies({ capabilityCode }), {
    ready: !!capabilityCode,
    refreshDeps: [capabilityCode],
  });
  const templatesReq = useRequest(
    () => getAiPromptTemplates({ capabilityCode, templateType: undefined }),
    {
      ready: !!capabilityCode,
      refreshDeps: [capabilityCode],
    },
  );
  const inventoryReq = useRequest(() => getAiInventory({ providerId, capabilityCode }), {
    ready: !!capabilityCode || !!providerId,
    refreshDeps: [providerId, capabilityCode],
  });

  useEffect(() => {
    if (!open) {
      setActiveJob(null);
      form.resetFields();
      return;
    }

    form.setFieldsValue({
      capabilityCode: defaultCapabilityCode,
      generationType:
        defaultGenerationType ||
        (defaultAssetKind === 'audio' ? 'tts' : defaultAssetKind === 'image' ? 'image' : 'text'),
      promptTitle: defaultPromptTitle,
      promptText: defaultPromptText,
      sourceScope: defaultSourceScope,
      sourceScopeId: defaultSourceScopeId,
    });
  }, [
    defaultAssetKind,
    defaultCapabilityCode,
    defaultGenerationType,
    defaultPromptText,
    defaultPromptTitle,
    defaultSourceScope,
    defaultSourceScopeId,
    form,
    open,
  ]);

  const capabilities = capabilitiesReq.data?.data || [];
  const providers = providersReq.data?.data || [];
  const policies = policiesReq.data?.data || [];
  const templates = templatesReq.data?.data || [];
  const inventory = inventoryReq.data?.data || [];

  const capabilityOptions = useMemo(
    () =>
      capabilities.map((item) => ({
        value: item.capabilityCode,
        label: item.displayNameZht,
      })),
    [capabilities],
  );

  const providerOptions = useMemo(
    () =>
      providers.map((item) => ({
        value: item.id,
        label: `${item.displayName} (${item.providerName})`,
      })),
    [providers],
  );

  const policyOptions = useMemo(
    () =>
      policies.map((item) => ({
        value: item.id,
        label: item.policyName,
      })),
    [policies],
  );

  const templateOptions = useMemo(
    () =>
      templates.map((item) => ({
        value: item.id,
        label: item.templateName,
      })),
    [templates],
  );

  const inventoryOptions = useMemo(
    () =>
      inventory.map((item) => ({
        value: item.id,
        label: `${item.displayName} (${item.providerDisplayName || item.providerName || '未命名供應商'})`,
      })),
    [inventory],
  );

  const refreshJobDetail = async (jobId: number) => {
    const response = await getAiGenerationJobDetail(jobId);
    if (!response.success || !response.data) {
      throw new Error(response.message || '刷新作業詳情失敗');
    }
    setActiveJob(response.data);
    return response.data;
  };

  const submitJob = async () => {
    if (submittingRef.current) {
      return;
    }

    submittingRef.current = true;
    setSubmitting(true);
    try {
      const values = await form.validateFields();
      const response = await createAiGenerationJob(values);
      if (!response.success || !response.data) {
        throw new Error(response.message || '建立生成作業失敗');
      }

      setActiveJob(response.data);
      message.success('已建立生成作業');

      if (response.data.jobStatus !== 'completed') {
        await refreshJobDetail(response.data.id);
      }
    } catch (error: any) {
      message.error(error?.message || '建立生成作業失敗');
    } finally {
      submittingRef.current = false;
      setSubmitting(false);
    }
  };

  const triggerRefresh = async () => {
    if (!activeJob) {
      return;
    }

    const response = await refreshAiGenerationJob(activeJob.id);
    if (!response.success || !response.data) {
      message.error(response.message || '刷新生成結果失敗');
      return;
    }
    setActiveJob(response.data);
    message.success('已刷新生成結果');
  };

  const finalizeCandidate = async (candidate: AiGenerationCandidateItem) => {
    if (!activeJob) {
      return;
    }

    const payload: AiCandidateFinalizePayload = {
      assetKind:
        defaultAssetKind ||
        (activeJob.generationType === 'image' ? 'image' : activeJob.generationType === 'tts' ? 'audio' : 'text'),
      status: 'published',
    };

    const response = await finalizeAiGenerationCandidate(candidate.id, payload);
    if (!response.success || !response.data) {
      message.error(response.message || '確認資源失敗');
      return;
    }

    setActiveJob(response.data);
    const finalizedCandidate = response.data.candidates?.find((item) => item.id === candidate.id) || candidate;
    message.success('已把候選結果回填成正式資產');
    onFinalized?.({
      job: response.data,
      candidate: finalizedCandidate,
      assetId: finalizedCandidate.finalizedAssetId,
    });
  };

  const restoreCandidate = async (candidate: AiGenerationCandidateItem) => {
    const response = await restoreAiGenerationCandidate(candidate.id);
    if (!response.success || !response.data) {
      message.error(response.message || '恢復候選版本失敗');
      return;
    }
    setActiveJob(response.data);
    message.success('已恢復為目前候選版本');
  };

  const renderCandidatePreview = (candidate: AiGenerationCandidateItem) => {
    if (candidate.storageUrl && (candidate.mimeType?.startsWith('image/') || candidate.candidateType === 'image')) {
      return (
        <img
          src={candidate.storageUrl}
          alt={candidate.previewText || 'AI candidate'}
          style={{ width: '100%', height: 240, objectFit: 'cover' }}
        />
      );
    }

    if (
      candidate.storageUrl &&
      (candidate.mimeType?.startsWith('audio/') || candidate.candidateType === 'tts' || candidate.candidateType === 'audio')
    ) {
      return <audio controls style={{ width: '100%' }} src={candidate.storageUrl} />;
    }

    return (
      <div style={{ padding: 16 }}>
        <Paragraph style={{ marginBottom: 0 }}>
          {candidate.previewText || candidate.transcriptText || '此候選結果沒有可顯示的預覽內容。'}
        </Paragraph>
      </div>
    );
  };

  return (
    <Modal
      open={open}
      width={1220}
      title={title}
      onCancel={submitting ? undefined : onClose}
      footer={null}
      destroyOnHidden
      maskClosable={!submitting}
      keyboard={!submitting}
      closable={!submitting}
    >
      <Row gutter={[20, 20]}>
        <Col xs={24} xl={10}>
          <Card style={{ borderRadius: 22 }}>
            <Space direction="vertical" size={16} style={{ width: '100%' }}>
              <div>
                <Title level={4} style={{ margin: 0 }}>
                  生成設定
                </Title>
                <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                  先選能力、策略、模型或端點，再提交可編輯的提示詞與變量。
                </Paragraph>
              </div>

              <Form form={form} layout="vertical" disabled={submitting}>
                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item
                      name="capabilityCode"
                      label="能力"
                      rules={[{ required: true, message: '請選擇能力' }]}
                    >
                      <Select options={capabilityOptions} placeholder="選擇要調用的能力" />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item
                      name="generationType"
                      label="輸出類型"
                      rules={[{ required: true, message: '請選擇輸出類型' }]}
                    >
                      <Select
                        options={[
                          { value: 'text', label: '文字' },
                          { value: 'image', label: '圖片' },
                          { value: 'tts', label: '語音' },
                        ]}
                      />
                    </Form.Item>
                  </Col>
                </Row>

                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name="providerId" label="指定供應商">
                      <Select allowClear options={providerOptions} placeholder="可留空，由策略自動選擇" />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="inventoryId" label="指定模型 / 端點">
                      <Select allowClear options={inventoryOptions} placeholder="可留空，交由策略決定" />
                    </Form.Item>
                  </Col>
                </Row>

                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name="policyId" label="策略">
                      <Select allowClear options={policyOptions} />
                    </Form.Item>
                  </Col>
                  <Col span={12}>
                    <Form.Item name="promptTemplateId" label="提示詞模板">
                      <Select allowClear options={templateOptions} />
                    </Form.Item>
                  </Col>
                </Row>

                <Row gutter={16}>
                  <Col span={12}>
                    <Form.Item name="promptTitle" label="任務標題">
                      <Input placeholder="例如：澳門半島主視覺封面" />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="sourceScope" label="來源範圍">
                      <Input placeholder="例如 city / poi / storyline" />
                    </Form.Item>
                  </Col>
                  <Col span={6}>
                    <Form.Item name="sourceScopeId" label="來源 ID">
                      <Input placeholder="若有具體內容主體可填入 ID" />
                    </Form.Item>
                  </Col>
                </Row>

                <Form.Item
                  name="promptText"
                  label="提示詞"
                  rules={[{ required: true, message: '請輸入提示詞' }]}
                >
                  <Input.TextArea
                    rows={6}
                    placeholder="可以根據當前表單上下文拼裝，也可以手動再潤色後提交。"
                  />
                </Form.Item>

                <Form.Item name="promptVariablesJson" label="模板變量（可選）">
                  <Input.TextArea rows={3} placeholder='例如 {"cityName":"澳門","aspectRatio":"3:2"}' />
                </Form.Item>

                <AiTtsWorkbenchFields form={form} capabilityCode={capabilityCode} />

                <Form.Item name="requestPayloadJson" label="進階請求覆寫（可選）">
                  <Input.TextArea rows={3} placeholder='例如 {"size":"1536x1024"}' />
                </Form.Item>

                <Button type="primary" onClick={() => void submitJob()} loading={submitting} disabled={submitting}>
                  開始生成
                </Button>
              </Form>
            </Space>
          </Card>
        </Col>

        <Col xs={24} xl={14}>
          <Card
            title="候選結果與歷史"
            extra={
              activeJob ? (
                <Space>
                  <Tag color={statusColor(activeJob.jobStatus)}>{activeJob.jobStatus}</Tag>
                  <Button onClick={() => void triggerRefresh()}>刷新結果</Button>
                </Space>
              ) : null
            }
            style={{ borderRadius: 22 }}
          >
            {!activeJob ? (
              <Empty description="尚未建立生成作業。" />
            ) : (
              <Space direction="vertical" size={16} style={{ width: '100%' }}>
                <Card size="small" style={{ borderRadius: 18 }}>
                  <Row gutter={[16, 16]}>
                    <Col xs={24} md={8}>
                      <Space direction="vertical" size={4}>
                        <Text strong>作業資訊</Text>
                        <Text type="secondary">能力：{activeJob.capabilityNameZht || activeJob.capabilityCode || '-'}</Text>
                        <Text type="secondary">類型：{activeJob.generationType}</Text>
                        <Text type="secondary">供應商：{activeJob.providerName || '自動路由'}</Text>
                      </Space>
                    </Col>
                    <Col xs={24} md={8}>
                      <Space direction="vertical" size={4}>
                        <Text strong>候選狀態</Text>
                        <Text type="secondary">最新候選：{activeJob.latestCandidateId || '-'}</Text>
                        <Text type="secondary">正式資產：{activeJob.finalizedCandidateId || '-'}</Text>
                        <Text type="secondary">建立時間：{activeJob.createdAt?.replace('T', ' ').slice(0, 19) || '-'}</Text>
                      </Space>
                    </Col>
                    <Col xs={24} md={8}>
                      <Space direction="vertical" size={4}>
                        <Text strong>結果摘要</Text>
                        <Text type="secondary">{activeJob.resultSummary || activeJob.errorMessage || '尚未產出摘要。'}</Text>
                      </Space>
                    </Col>
                  </Row>
                </Card>

                <Divider style={{ margin: 0 }} />

                {activeJob.candidates?.length ? (
                  <Row gutter={[16, 16]}>
                    {activeJob.candidates.map((candidate) => (
                      <Col xs={24} md={12} key={candidate.id}>
                        <Card
                          size="small"
                          style={{ borderRadius: 18, height: '100%' }}
                          actions={[
                            <Button type="link" key="restore" onClick={() => void restoreCandidate(candidate)}>
                              恢復此版本
                            </Button>,
                            <Button type="link" key="finalize" onClick={() => void finalizeCandidate(candidate)}>
                              確認回填
                            </Button>,
                          ]}
                        >
                          <Space direction="vertical" size={12} style={{ width: '100%' }}>
                            <Space wrap>
                              <Tag color="blue">候選 #{candidate.candidateIndex ?? candidate.id}</Tag>
                              {candidate.isSelected ? <Tag color="processing">目前版本</Tag> : null}
                              {candidate.isFinalized ? <Tag color="green">已成正式資產</Tag> : null}
                            </Space>
                            <div style={previewBoxStyle}>{renderCandidatePreview(candidate)}</div>
                            <Space direction="vertical" size={2}>
                              <Text strong>{candidate.previewText || candidate.transcriptText || '未命名候選結果'}</Text>
                              <Text type="secondary">
                                {candidate.finalizedAssetId ? `已回填資產 #${candidate.finalizedAssetId}` : '尚未回填正式資產'}
                              </Text>
                            </Space>
                          </Space>
                        </Card>
                      </Col>
                    ))}
                  </Row>
                ) : (
                  <Empty description="此作業暫時還沒有候選結果，可稍後點擊刷新。" />
                )}
              </Space>
            )}
          </Card>
        </Col>
      </Row>
    </Modal>
  );
};

export default AiCreativeWorkbenchModal;

