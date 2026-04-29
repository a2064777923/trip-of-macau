import React from "react";
import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import {
  Button,
  Card,
  Col,
  Empty,
  Form,
  Input,
  InputNumber,
  Row,
  Select,
  Space,
  Switch,
  Typography,
} from "antd";
import type { FormInstance } from "antd/es/form";

const effectCategoryOptions = [
  { label: "彈窗內容", value: "popup" },
  { label: "氣泡內容", value: "bubble" },
  { label: "媒體播放", value: "media" },
  { label: "路徑移動", value: "path_motion" },
  { label: "發放獎勵", value: "reward_grant" },
  { label: "發放收集物", value: "collectible_grant" },
  { label: "發放徽章", value: "badge_grant" },
  { label: "任務更新", value: "task_update" },
  { label: "帳戶數值調整", value: "account_adjustment" },
];

interface Props {
  form: FormInstance;
  basePath: Array<string | number>;
}

const { Text } = Typography;

const IndoorRuleEffectEditor: React.FC<Props> = ({ form, basePath }) => {
  const watchOptions = React.useMemo(() => ({ form, preserve: true }), [form]);
  const effectRules =
    (Form.useWatch([...basePath, "effectRules"], watchOptions) as
      | Array<{ category?: string }>
      | undefined) || [];

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Row gutter={12}>
        <Col span={12}>
          <Form.Item
            name={[...basePath, "effectTemplateCode"]}
            label="效果模板代碼"
          >
            <Input placeholder="popup-and-reward" />
          </Form.Item>
        </Col>
      </Row>

      <Form.List name={[...basePath, "effectRules"]}>
        {(fields, { add, remove }) => (
          <Space direction="vertical" size="middle" style={{ width: "100%" }}>
            <Space
              style={{ width: "100%", justifyContent: "space-between" }}
              wrap
            >
              <Text type="secondary">
                每個觸發至少應對應一個效果，也可以按順序疊加多個效果。
              </Text>
              <Button
                icon={<PlusOutlined />}
                onClick={() => add({ category: "popup", config: {} })}
              >
                新增效果
              </Button>
            </Space>

            {!fields.length ? (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="尚未配置任何效果。"
              />
            ) : null}

            {fields.map((field) => {
              const category = effectRules[field.name]?.category;

              return (
                <Card
                  key={field.key}
                  size="small"
                  title={`效果 ${field.name + 1}`}
                  extra={
                    <Button
                      type="text"
                      danger
                      icon={<DeleteOutlined />}
                      onClick={() => remove(field.name)}
                    >
                      刪除
                    </Button>
                  }
                >
                  <Row gutter={12}>
                    <Col span={10}>
                      <Form.Item
                        name={[field.name, "category"]}
                        label="效果類型"
                        rules={[{ required: true, message: "請選擇效果類型" }]}
                      >
                        <Select options={effectCategoryOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={14}>
                      <Form.Item name={[field.name, "label"]} label="效果名稱">
                        <Input placeholder="例如：顯示故事卡片" />
                      </Form.Item>
                    </Col>
                  </Row>

                  {category === "popup" || category === "bubble" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "title"]}
                          label="標題"
                        >
                          <Input placeholder="夜航故事" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "body"]}
                          label="內容"
                        >
                          <Input placeholder="顯示摘要或提示文字" />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "media" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "mediaAssetId"]}
                          label="媒體資源 ID"
                        >
                          <InputNumber min={1} style={{ width: "100%" }} />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "autoplay"]}
                          label="自動播放"
                          valuePropName="checked"
                        >
                          <Switch />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "path_motion" ? <PathMotionHint /> : null}

                  {category === "reward_grant" ||
                  category === "collectible_grant" ||
                  category === "badge_grant" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "entityId"]}
                          label="目標資源 ID"
                        >
                          <InputNumber min={1} style={{ width: "100%" }} />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "quantity"]}
                          label="數量"
                        >
                          <InputNumber min={1} style={{ width: "100%" }} />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "task_update" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "taskId"]}
                          label="任務 ID"
                        >
                          <InputNumber min={1} style={{ width: "100%" }} />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "status"]}
                          label="任務狀態"
                        >
                          <Select
                            options={["pending", "active", "completed"].map(
                              (value) => ({ label: value, value }),
                            )}
                          />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "account_adjustment" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "metric"]}
                          label="帳戶欄位"
                        >
                          <Input placeholder="user.energy" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "delta"]}
                          label="調整值"
                        >
                          <InputNumber style={{ width: "100%" }} />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}
                </Card>
              );
            })}
          </Space>
        )}
      </Form.List>
    </Space>
  );
};

const PathMotionHint: React.FC = () => (
  <Card size="small" variant="borderless" style={{ background: "#f7fbff" }}>
    路徑移動效果會直接引用下方「路徑編排」分頁中的 <code>pathGraph</code>{" "}
    設定，不需要另外填寫座標 JSON。
  </Card>
);

export default IndoorRuleEffectEditor;
