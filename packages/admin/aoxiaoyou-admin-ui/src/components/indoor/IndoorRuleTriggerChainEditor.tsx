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

const triggerCategoryOptions = [
  { label: "點擊 / Tap", value: "tap" },
  { label: "靠近觸發", value: "proximity" },
  { label: "停留觸發", value: "dwell" },
  { label: "拖拉互動", value: "drag" },
  { label: "語音佔位觸發", value: "voice_placeholder" },
  { label: "自定義 Hook", value: "custom" },
];

interface Props {
  form: FormInstance;
  basePath: Array<string | number>;
}

const { Text } = Typography;

const IndoorRuleTriggerChainEditor: React.FC<Props> = ({ form, basePath }) => {
  const watchOptions = React.useMemo(() => ({ form, preserve: true }), [form]);
  const triggerRules =
    (Form.useWatch([...basePath, "triggerRules"], watchOptions) as
      | Array<{ id?: string; label?: string; category?: string }>
      | undefined) || [];

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Row gutter={12}>
        <Col span={12}>
          <Form.Item
            name={[...basePath, "triggerTemplateCode"]}
            label="觸發模板代碼"
          >
            <Input placeholder="double-step-trigger" />
          </Form.Item>
        </Col>
      </Row>

      <Form.List name={[...basePath, "triggerRules"]}>
        {(fields, { add, remove }) => (
          <Space direction="vertical" size="middle" style={{ width: "100%" }}>
            <Space
              style={{ width: "100%", justifyContent: "space-between" }}
              wrap
            >
              <Text type="secondary">
                第二步開始可指定前置觸發，形成清晰的互動鏈。
              </Text>
              <Button
                icon={<PlusOutlined />}
                onClick={() => add({ category: "tap", config: {} })}
              >
                新增觸發步驟
              </Button>
            </Space>

            {!fields.length ? (
              <Empty
                image={Empty.PRESENTED_IMAGE_SIMPLE}
                description="尚未配置任何觸發步驟。"
              />
            ) : null}

            {fields.map((field) => {
              const category = triggerRules[field.name]?.category;

              return (
                <Card
                  key={field.key}
                  size="small"
                  title={`觸發步驟 ${field.name + 1}`}
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
                    <Col span={8}>
                      <Form.Item
                        name={[field.name, "id"]}
                        label="步驟代碼"
                        rules={[{ required: true, message: "請填寫步驟代碼" }]}
                      >
                        <Input placeholder="step-tap-lobby" />
                      </Form.Item>
                    </Col>
                    <Col span={8}>
                      <Form.Item
                        name={[field.name, "category"]}
                        label="觸發類型"
                        rules={[{ required: true, message: "請選擇觸發類型" }]}
                      >
                        <Select options={triggerCategoryOptions} />
                      </Form.Item>
                    </Col>
                    <Col span={8}>
                      <Form.Item
                        name={[field.name, "dependsOnTriggerId"]}
                        label="前置步驟"
                      >
                        <Select
                          allowClear
                          placeholder="沒有前置可留空"
                          options={triggerRules
                            .filter((_, index) => index !== field.name)
                            .filter((item) => item?.id)
                            .map((item) => ({
                              label: item.label || item.id || "",
                              value: item.id || "",
                            }))}
                        />
                      </Form.Item>
                    </Col>
                  </Row>

                  <Form.Item name={[field.name, "label"]} label="步驟名稱">
                    <Input placeholder="例如：先點擊大堂入口光點" />
                  </Form.Item>

                  {category === "tap" ? (
                    <Form.Item
                      name={[field.name, "config", "targetHint"]}
                      label="點擊目標提示"
                    >
                      <Input placeholder="點擊門前光點" />
                    </Form.Item>
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
                          <Input placeholder="overlay-night-garden" />
                        </Form.Item>
                      </Col>
                    </Row>
                  ) : null}

                  {category === "dwell" ? (
                    <Form.Item
                      name={[field.name, "config", "seconds"]}
                      label="停留秒數"
                    >
                      <InputNumber min={1} style={{ width: "100%" }} />
                    </Form.Item>
                  ) : null}

                  {category === "drag" ? (
                    <Form.Item
                      name={[field.name, "config", "axis"]}
                      label="拖拉方向"
                    >
                      <Select
                        options={["x", "y", "free"].map((value) => ({
                          label: value,
                          value,
                        }))}
                      />
                    </Form.Item>
                  ) : null}

                  {category === "voice_placeholder" ? (
                    <Form.Item
                      name={[field.name, "config", "phraseHint"]}
                      label="語音提示語"
                    >
                      <Input placeholder="例如：大聲說出「開啟冒險」" />
                    </Form.Item>
                  ) : null}

                  {category === "custom" ? (
                    <Form.Item
                      name={[field.name, "config", "hookCode"]}
                      label="自定義 Hook 代碼"
                    >
                      <Input placeholder="indoor.custom.trigger.one" />
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

export default IndoorRuleTriggerChainEditor;
