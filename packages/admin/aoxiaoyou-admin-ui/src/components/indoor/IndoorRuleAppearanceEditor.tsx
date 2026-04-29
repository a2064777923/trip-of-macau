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
  Typography,
} from "antd";
import type { FormInstance } from "antd/es/form";

const appearanceCategoryOptions = [
  { label: "時段出現", value: "schedule_window" },
  { label: "週期日曆", value: "recurring_calendar" },
  { label: "用戶進度條件", value: "user_progress" },
  { label: "場景停留", value: "scene_dwell" },
  { label: "靠近觸發顯示", value: "proximity" },
  { label: "常駐顯示", value: "always_on" },
  { label: "手動控制", value: "manual" },
];

interface Props {
  form: FormInstance;
  basePath: Array<string | number>;
}

const { Text } = Typography;

const IndoorRuleAppearanceEditor: React.FC<Props> = ({ form, basePath }) => {
  const watchOptions = React.useMemo(() => ({ form, preserve: true }), [form]);
  const appearanceRules =
    (Form.useWatch([...basePath, "appearanceRules"], watchOptions) as
      | Array<{ category?: string }>
      | undefined) || [];

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Row gutter={12}>
        <Col span={12}>
          <Form.Item
            name={[...basePath, "appearancePresetCode"]}
            label="出現模板代碼"
          >
            <Input placeholder="schedule-window-default" />
          </Form.Item>
        </Col>
        <Col span={12}>
          <Form.Item name={[...basePath, "behaviorNameZh"]} label="簡中名稱">
            <Input placeholder="夜間巡遊浮標" />
          </Form.Item>
        </Col>
      </Row>

      <Form.List name={[...basePath, "appearanceRules"]}>
        {(fields, { add, remove }) => (
          <Space direction="vertical" size="middle" style={{ width: "100%" }}>
            <Space
              style={{ width: "100%", justifyContent: "space-between" }}
              wrap
            >
              <Text type="secondary">
                以表單方式配置出現條件，不需要直接手寫 JSON。
              </Text>
              <Button
                icon={<PlusOutlined />}
                onClick={() => add({ category: "schedule_window", config: {} })}
              >
                新增出現條件
              </Button>
            </Space>

            {!fields.length ? (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="尚未配置任何出現條件。"
              />
            ) : null}

            {fields.map((field) => {
              const category = appearanceRules[field.name]?.category;

              return (
                <Card
                  key={field.key}
                  size="small"
                  title={`出現條件 ${field.name + 1}`}
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
                        label="條件類型"
                        rules={[{ required: true, message: "請選擇條件類型" }]}
                      >
                        <Select options={appearanceCategoryOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={14}>
                      <Form.Item name={[field.name, "label"]} label="條件名稱">
                        <Input placeholder="例如：晚間七點後顯示" />
                      </Form.Item>
                    </Col>
                  </Row>

                  {category === "schedule_window" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "startTime"]}
                          label="開始時間"
                        >
                          <Input placeholder="19:00" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "endTime"]}
                          label="結束時間"
                        >
                          <Input placeholder="22:30" />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "recurring_calendar" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "weekdays"]}
                          label="循環日子"
                        >
                          <Select
                            mode="multiple"
                            options={[
                              "Mon",
                              "Tue",
                              "Wed",
                              "Thu",
                              "Fri",
                              "Sat",
                              "Sun",
                            ].map((value) => ({ label: value, value }))}
                          />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "timeRange"]}
                          label="時間範圍"
                        >
                          <Input placeholder="10:00-18:00" />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "user_progress" ? (
                    <Row gutter={12}>
                      <Col span={8}>
                        <Form.Item
                          name={[field.name, "config", "metric"]}
                          label="進度欄位"
                        >
                          <Input placeholder="city.exploration" />
                        </Form.Item>
                      </Col>
                      <Col span={8}>
                        <Form.Item
                          name={[field.name, "config", "comparator"]}
                          label="比較方式"
                        >
                          <Select
                            options={[">=", ">", "=", "<=", "<"].map(
                              (value) => ({ label: value, value }),
                            )}
                          />
                        </Form.Item>
                      </Col>
                      <Col span={8}>
                        <Form.Item
                          name={[field.name, "config", "value"]}
                          label="目標值"
                        >
                          <InputNumber min={0} style={{ width: "100%" }} />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "scene_dwell" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "sceneCode"]}
                          label="場景代碼"
                        >
                          <Input placeholder="lisboeta-lobby" />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "seconds"]}
                          label="停留秒數"
                        >
                          <InputNumber min={1} style={{ width: "100%" }} />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "proximity" ? (
                    <Row gutter={12}>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "radiusMeters"]}
                          label="靠近半徑 (m)"
                        >
                          <InputNumber min={1} style={{ width: "100%" }} />
                        </Form.Item>
                      </Col>
                      <Col span={12}>
                        <Form.Item
                          name={[field.name, "config", "targetCode"]}
                          label="目標代碼"
                        >
                          <Input placeholder="poi-lisboeta-main-gate" />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "always_on" || category === "manual" ? (
                    <Form.Item
                      name={[field.name, "config", "note"]}
                      label="補充說明"
                    >
                      <Input placeholder="用來補充前端展示或運營說明" />
                    </Form.Item>
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

export default IndoorRuleAppearanceEditor;
