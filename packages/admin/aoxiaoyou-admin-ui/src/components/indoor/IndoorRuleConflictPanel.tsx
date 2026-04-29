import React from "react";
import {
  Button,
  Descriptions,
  Drawer,
  Empty,
  List,
  Space,
  Spin,
  Tag,
  Typography,
} from "antd";
import type { AdminIndoorRuleGovernanceDetail } from "../../types/admin";

interface Props {
  open: boolean;
  loading?: boolean;
  detail?: AdminIndoorRuleGovernanceDetail | null;
  onClose: () => void;
  onEnable: () => void;
  onDisable: () => void;
  onOpenAuthoring: () => void;
}

const { Text } = Typography;

function getStatusTag(status?: string) {
  switch (status) {
    case "enabled":
      return <Tag color="green">啟用中</Tag>;
    case "published":
      return <Tag color="blue">已發佈</Tag>;
    case "disabled":
      return <Tag>已停用</Tag>;
    default:
      return <Tag color="gold">草稿</Tag>;
  }
}

const IndoorRuleConflictPanel: React.FC<Props> = ({
  open,
  loading = false,
  detail,
  onClose,
  onEnable,
  onDisable,
  onOpenAuthoring,
}) => {
  return (
    <Drawer
      title="互動規則詳情"
      open={open}
      width={560}
      destroyOnHidden
      onClose={onClose}
      extra={
        <Space wrap>
          <Button onClick={onEnable}>啟用規則</Button>
          <Button onClick={onDisable}>停用規則</Button>
          <Button type="primary" onClick={onOpenAuthoring}>
            前往工作台
          </Button>
        </Space>
      }
    >
      {loading ? (
        <div
          style={{
            minHeight: 240,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <Space direction="vertical" size={8} align="center">
            <Spin />
            <Text type="secondary">正在讀取規則詳情與衝突資料…</Text>
          </Space>
        </div>
      ) : !detail ? (
        <Empty description="尚未選擇任何互動規則。" />
      ) : (
        <Space direction="vertical" size="large" style={{ width: "100%" }}>
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="行為名稱">
              {detail.behaviorNameZht ||
                detail.behaviorNameZh ||
                detail.behaviorCode ||
                "-"}
            </Descriptions.Item>
            <Descriptions.Item label="行為代碼">
              {detail.behaviorCode || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="所屬標記 / 樓層">
              {detail.parentNode?.nodeNameZht ||
                detail.parentNode?.markerCode ||
                detail.markerCode ||
                "-"}
              {" / "}
              {detail.floorCode || "-"}
            </Descriptions.Item>
            <Descriptions.Item label="所屬建築">
              {detail.parentNode?.buildingNameZht ||
                detail.buildingNameZht ||
                "-"}
            </Descriptions.Item>
            <Descriptions.Item label="狀態">
              <Space wrap>
                {getStatusTag(detail.status)}
                {detail.runtimeSupportLevel ? (
                  <Tag color="purple">{detail.runtimeSupportLevel}</Tag>
                ) : null}
              </Space>
            </Descriptions.Item>
            <Descriptions.Item label="規則結構">
              <Space wrap>
                <Tag>出現 {detail.appearanceRuleCount || 0}</Tag>
                <Tag>觸發 {detail.triggerRuleCount || 0}</Tag>
                <Tag>效果 {detail.effectRuleCount || 0}</Tag>
                <Tag color={detail.hasPathGraph ? "cyan" : "default"}>
                  {detail.hasPathGraph ? "含路徑動畫" : "無路徑動畫"}
                </Tag>
              </Space>
            </Descriptions.Item>
          </Descriptions>

          <List
            size="small"
            bordered
            header={<Text strong>衝突與提醒</Text>}
            dataSource={detail.conflicts || []}
            locale={{ emptyText: "目前未發現衝突。" }}
            renderItem={(item) => (
              <List.Item>
                <Space direction="vertical" size={4} style={{ width: "100%" }}>
                  <Space wrap>
                    <Tag color={item.severity === "error" ? "red" : "gold"}>
                      {item.conflictCode}
                    </Tag>
                    {item.relatedBehaviorId ? (
                      <Tag>相關規則 #{item.relatedBehaviorId}</Tag>
                    ) : null}
                    {item.relatedNodeId ? (
                      <Tag>相關標記 #{item.relatedNodeId}</Tag>
                    ) : null}
                  </Space>
                  <Text>{item.message}</Text>
                </Space>
              </List.Item>
            )}
          />

          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="出現條件">
              {detail.appearanceRules?.length || 0} 條
            </Descriptions.Item>
            <Descriptions.Item label="觸發步驟">
              {detail.triggerRules?.length || 0} 條
            </Descriptions.Item>
            <Descriptions.Item label="效果定義">
              {detail.effectRules?.length || 0} 條
            </Descriptions.Item>
          </Descriptions>
        </Space>
      )}
    </Drawer>
  );
};

export default IndoorRuleConflictPanel;

