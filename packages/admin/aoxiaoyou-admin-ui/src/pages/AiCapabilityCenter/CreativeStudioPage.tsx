import React, { useMemo, useState } from 'react';
import { useRequest } from 'ahooks';
import { Button, Card, Col, Empty, Row, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { getAiGenerationJobs, type AiGenerationJobItem } from '../../services/api';
import AiCreativeWorkbenchModal from '../../components/ai/AiCreativeWorkbenchModal';

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

const jobColumns: ColumnsType<AiGenerationJobItem> = [
  {
    title: '任務',
    dataIndex: 'promptTitle',
    render: (value) => value || '未命名生成作業',
  },
  {
    title: '能力',
    dataIndex: 'capabilityNameZht',
    width: 180,
    render: (_, job) => job.capabilityNameZht || job.capabilityCode || '-',
  },
  {
    title: '類型',
    dataIndex: 'generationType',
    width: 100,
  },
  {
    title: '供應商',
    dataIndex: 'providerName',
    width: 180,
    render: (value) => value || '自動路由',
  },
  {
    title: '狀態',
    dataIndex: 'jobStatus',
    width: 120,
    render: (value) => <Tag color={statusColor(value)}>{value || 'unknown'}</Tag>,
  },
  {
    title: '候選數',
    width: 100,
    render: (_, job) => job.candidates?.length || 0,
  },
];

const CreativeStudioPage: React.FC = () => {
  const [workbenchOpen, setWorkbenchOpen] = useState(false);
  const [workbenchPreset, setWorkbenchPreset] = useState<{
    capabilityCode?: string;
    generationType?: string;
    promptTitle?: string;
    promptText?: string;
    assetKind?: string;
  }>({});

  const jobsReq = useRequest(() => getAiGenerationJobs({ pageNum: 1, pageSize: 20 }), {
    refreshDeps: [workbenchOpen],
  });

  const jobs = useMemo(() => jobsReq.data?.data?.list || [], [jobsReq.data?.data?.list]);

  const openPreset = (preset: typeof workbenchPreset) => {
    setWorkbenchPreset(preset);
    setWorkbenchOpen(true);
  };

  return (
    <>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Card style={{ borderRadius: 22 }}>
          <Title level={4} style={{ marginTop: 0 }}>
            創作工作台
          </Title>
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            編輯者可從此處直接生成圖片、語音或提示詞草稿，也可在媒體欄位內啟動同一個工作台並回填正式資產。
          </Paragraph>
        </Card>

        <Row gutter={[16, 16]}>
          <Col xs={24} md={8}>
            <Card style={{ borderRadius: 22, height: '100%' }}>
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                <Title level={5} style={{ margin: 0 }}>
                  城市封面與橫幅
                </Title>
                <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                  針對城市、大地圖與子地圖主視覺，快速拼裝高品質生圖提示詞。
                </Paragraph>
                <Button
                  type="primary"
                  onClick={() =>
                    openPreset({
                      capabilityCode: 'admin_image_generation',
                      generationType: 'image',
                      promptTitle: '城市封面圖生成',
                      assetKind: 'image',
                    })
                  }
                >
                  開啟圖片工作台
                </Button>
              </Space>
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card style={{ borderRadius: 22, height: '100%' }}>
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                <Title level={5} style={{ margin: 0 }}>
                  POI 疊加圖示
                </Title>
                <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                  專為地圖疊加層與小程序地圖表現產生透明背景圖示或小插畫。
                </Paragraph>
                <Button
                  type="primary"
                  onClick={() =>
                    openPreset({
                      capabilityCode: 'admin_image_generation',
                      generationType: 'image',
                      promptTitle: 'POI 疊加圖示生成',
                      assetKind: 'image',
                    })
                  }
                >
                  生成圖示候選
                </Button>
              </Space>
            </Card>
          </Col>
          <Col xs={24} md={8}>
            <Card style={{ borderRadius: 22, height: '100%' }}>
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                <Title level={5} style={{ margin: 0 }}>
                  旁白與導覽音軌
                </Title>
                <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                  針對景點旁白、活動播報與 NPC 對話，生成可回填為音訊資產的候選音軌。
                </Paragraph>
                <Button
                  type="primary"
                  onClick={() =>
                    openPreset({
                      capabilityCode: 'admin_tts_generation',
                      generationType: 'tts',
                      promptTitle: '導覽旁白生成',
                      assetKind: 'audio',
                    })
                  }
                >
                  開啟語音工作台
                </Button>
              </Space>
            </Card>
          </Col>
        </Row>

        <Card
          title="最近生成作業"
          extra={
            <Button onClick={() => openPreset({ capabilityCode: 'admin_prompt_drafting', generationType: 'text' })}>
              新增生成作業
            </Button>
          }
          style={{ borderRadius: 22 }}
        >
          {jobs.length ? (
            <Table
              rowKey="id"
              columns={jobColumns}
              dataSource={jobs}
              pagination={false}
              expandable={{
                expandedRowRender: (job) =>
                  job.candidates?.length ? (
                    <Space direction="vertical" size={12} style={{ width: '100%' }}>
                      {job.candidates.map((candidate) => (
                        <Card key={candidate.id} size="small" style={{ borderRadius: 16 }}>
                          <Space direction="vertical" size={4} style={{ width: '100%' }}>
                            <Space wrap>
                              <Text strong>候選 #{candidate.candidateIndex ?? candidate.id}</Text>
                              {candidate.isSelected ? <Tag color="processing">目前版本</Tag> : null}
                              {candidate.isFinalized ? <Tag color="green">已轉正式資產</Tag> : null}
                            </Space>
                            <Text type="secondary">
                              {candidate.previewText || candidate.transcriptText || candidate.storageUrl || '此候選尚未產出預覽內容。'}
                            </Text>
                          </Space>
                        </Card>
                      ))}
                    </Space>
                  ) : (
                    <Empty description="此作業尚無候選結果。" />
                  ),
              }}
            />
          ) : (
            <Empty description="目前沒有生成作業，可直接從上方工作台建立。" />
          )}
        </Card>
      </Space>

      <AiCreativeWorkbenchModal
        open={workbenchOpen}
        onClose={() => setWorkbenchOpen(false)}
        defaultCapabilityCode={workbenchPreset.capabilityCode}
        defaultGenerationType={workbenchPreset.generationType}
        defaultPromptTitle={workbenchPreset.promptTitle}
        defaultPromptText={workbenchPreset.promptText}
        defaultAssetKind={workbenchPreset.assetKind}
        onFinalized={() => {
          setWorkbenchOpen(false);
          void jobsReq.refresh();
        }}
      />
    </>
  );
};

export default CreativeStudioPage;
