import React from "react";
import { Alert, List, Space, Tag, Typography } from "antd";
import type { AdminIndoorRuleValidationResponse } from "../../types/admin";

interface Props {
  validation?: AdminIndoorRuleValidationResponse | null;
}

const { Text } = Typography;

const IndoorRuleValidationSummary: React.FC<Props> = ({ validation }) => {
  if (!validation) {
    return (
      <Alert
        type="info"
        showIcon
        message="尚未執行規則校驗"
        description="在套用到表單前，系統會先檢查出現條件、觸發鏈、效果與路徑設定是否完整可用。"
      />
    );
  }

  const errors = validation.errors || [];
  const warnings = validation.warnings || [];
  const type = errors.length
    ? "error"
    : warnings.length
      ? "warning"
      : "success";
  const title = errors.length
    ? "規則校驗未通過"
    : warnings.length
      ? "規則校驗已通過，但仍有提醒"
      : "規則校驗通過";

  return (
    <Space direction="vertical" size="small" style={{ width: "100%" }}>
      <Alert
        type={type}
        showIcon
        message={title}
        description={
          <Space wrap>
            <Tag color={errors.length ? "red" : "green"}>
              錯誤 {errors.length}
            </Tag>
            <Tag color={warnings.length ? "gold" : "default"}>
              提醒 {warnings.length}
            </Tag>
            {validation.behaviorCount != null ? (
              <Tag color="blue">行為 {validation.behaviorCount}</Tag>
            ) : null}
          </Space>
        }
      />

      {errors.length ? (
        <List
          size="small"
          bordered
          header={<Text strong>阻塞錯誤</Text>}
          dataSource={errors}
          renderItem={(item) => <List.Item>{item}</List.Item>}
        />
      ) : null}

      {warnings.length ? (
        <List
          size="small"
          bordered
          header={<Text strong>提醒</Text>}
          dataSource={warnings}
          renderItem={(item) => <List.Item>{item}</List.Item>}
        />
      ) : null}
    </Space>
  );
};

export default IndoorRuleValidationSummary;
