import React from "react";
import {
  ArrowDownOutlined,
  ArrowUpOutlined,
  CopyOutlined,
  DeleteOutlined,
  PlusOutlined,
} from "@ant-design/icons";
import { Button, Card, Empty, Space, Tag, Typography } from "antd";
import type { AdminIndoorBehaviorProfile } from "../../types/admin";

interface Props {
  behaviors: AdminIndoorBehaviorProfile[];
  activeIndex: number;
  onSelect: (index: number) => void;
  onAdd: () => void;
  onDuplicate: (index: number) => void;
  onMoveUp: (index: number) => void;
  onMoveDown: (index: number) => void;
  onDelete: (index: number) => void;
  onStatusChange: (index: number, status: string) => void;
}

const { Text, Paragraph } = Typography;

function getBehaviorLabel(behavior: AdminIndoorBehaviorProfile, index: number) {
  return (
    behavior.behaviorNameZht ||
    behavior.behaviorNameZh ||
    behavior.behaviorNameEn ||
    behavior.behaviorCode ||
    `互動行為 ${index + 1}`
  );
}

function getStatusMeta(status?: string) {
  switch (status) {
    case "enabled":
      return { label: "啟用中", color: "green" as const };
    case "published":
      return { label: "已發佈", color: "blue" as const };
    case "disabled":
      return { label: "已停用", color: "default" as const };
    default:
      return { label: "草稿", color: "gold" as const };
  }
}

const IndoorBehaviorRail: React.FC<Props> = ({
  behaviors,
  activeIndex,
  onSelect,
  onAdd,
  onDuplicate,
  onMoveUp,
  onMoveDown,
  onDelete,
  onStatusChange,
}) => {
  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Card size="small">
        <Space direction="vertical" size={8} style={{ width: "100%" }}>
          <Space style={{ width: "100%", justifyContent: "space-between" }}>
            <Text strong>互動行為清單</Text>
            <Button icon={<PlusOutlined />} type="primary" onClick={onAdd}>
              新增行為
            </Button>
          </Space>
          <Paragraph type="secondary" style={{ marginBottom: 0 }}>
            每一條行為代表一組出現條件、觸發鏈與效果。先在左側建立行為，再到中間分頁細化規則。
          </Paragraph>
        </Space>
      </Card>

      {!behaviors.length ? (
        <Card size="small">
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="尚未建立任何互動行為。"
          >
            <Button type="primary" icon={<PlusOutlined />} onClick={onAdd}>
              建立第一條行為
            </Button>
          </Empty>
        </Card>
      ) : null}

      {behaviors.map((behavior, index) => {
        const statusMeta = getStatusMeta(behavior.status);
        const active = activeIndex === index;

        return (
          <Card
            key={`${behavior.behaviorCode || "behavior"}-${index}`}
            size="small"
            hoverable
            onClick={() => onSelect(index)}
            styles={{ body: { padding: 12 } }}
            style={{
              cursor: "pointer",
              borderColor: active ? "#1677ff" : undefined,
              boxShadow: active ? "0 0 0 2px rgba(22,119,255,0.14)" : undefined,
            }}
          >
            <Space direction="vertical" size={10} style={{ width: "100%" }}>
              <Space
                align="start"
                style={{ width: "100%", justifyContent: "space-between" }}
              >
                <div style={{ minWidth: 0 }}>
                  <Text
                    strong
                    ellipsis={{ tooltip: getBehaviorLabel(behavior, index) }}
                  >
                    {getBehaviorLabel(behavior, index)}
                  </Text>
                  <br />
                  <Text
                    type="secondary"
                    ellipsis={{
                      tooltip: behavior.behaviorCode || `behavior-${index + 1}`,
                    }}
                  >
                    {behavior.behaviorCode || `behavior-${index + 1}`}
                  </Text>
                </div>
                <Tag color={statusMeta.color}>{statusMeta.label}</Tag>
              </Space>

              <Space wrap size={[6, 6]}>
                <Tag>{behavior.appearanceRules?.length || 0} 出現</Tag>
                <Tag>{behavior.triggerRules?.length || 0} 觸發</Tag>
                <Tag>{behavior.effectRules?.length || 0} 效果</Tag>
                <Tag
                  color={
                    behavior.pathGraph?.points?.length ? "purple" : "default"
                  }
                >
                  {behavior.pathGraph?.points?.length ? "含路徑" : "無路徑"}
                </Tag>
              </Space>

              <Space wrap size={[6, 6]}>
                <Button
                  size="small"
                  icon={<CopyOutlined />}
                  onClick={(event) => {
                    event.stopPropagation();
                    onDuplicate(index);
                  }}
                >
                  複製
                </Button>
                <Button
                  size="small"
                  icon={<ArrowUpOutlined />}
                  disabled={index === 0}
                  onClick={(event) => {
                    event.stopPropagation();
                    onMoveUp(index);
                  }}
                >
                  上移
                </Button>
                <Button
                  size="small"
                  icon={<ArrowDownOutlined />}
                  disabled={index === behaviors.length - 1}
                  onClick={(event) => {
                    event.stopPropagation();
                    onMoveDown(index);
                  }}
                >
                  下移
                </Button>
                <Button
                  size="small"
                  onClick={(event) => {
                    event.stopPropagation();
                    onStatusChange(
                      index,
                      behavior.status === "enabled" ||
                        behavior.status === "published"
                        ? "disabled"
                        : "enabled",
                    );
                  }}
                >
                  {behavior.status === "enabled" ||
                  behavior.status === "published"
                    ? "停用"
                    : "啟用"}
                </Button>
                <Button
                  size="small"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={(event) => {
                    event.stopPropagation();
                    onDelete(index);
                  }}
                >
                  刪除
                </Button>
              </Space>
            </Space>
          </Card>
        );
      })}
    </Space>
  );
};

export default IndoorBehaviorRail;
