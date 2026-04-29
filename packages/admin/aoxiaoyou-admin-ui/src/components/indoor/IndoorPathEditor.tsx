import React from "react";
import { AimOutlined, DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import {
  Button,
  Card,
  Empty,
  Form,
  InputNumber,
  Select,
  Space,
  Switch,
  Table,
  Typography,
} from "antd";
import type { FormInstance } from "antd/es/form";
import type { AdminIndoorNodePoint } from "../../types/admin";

interface Props {
  form: FormInstance;
  basePath: Array<string | number>;
  currentPoint?: { x?: number | null; y?: number | null };
  onArmPick?: () => void;
  onAppendCurrentPoint?: () => void;
  onClearAll?: () => void;
  picking?: boolean;
}

const { Text } = Typography;

const IndoorPathEditor: React.FC<Props> = ({
  form,
  basePath,
  currentPoint,
  onArmPick,
  onAppendCurrentPoint,
  onClearAll,
  picking = false,
}) => {
  const watchOptions = React.useMemo(() => ({ form, preserve: true }), [form]);
  const points =
    (Form.useWatch([...basePath, "pathGraph", "points"], watchOptions) as
      | AdminIndoorNodePoint[]
      | undefined) || [];

  const hasPoints = points.length > 0;

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Space style={{ width: "100%", justifyContent: "space-between" }} wrap>
        <Text type="secondary">
          當前座標：{currentPoint?.x?.toFixed?.(3) ?? "-"} /{" "}
          {currentPoint?.y?.toFixed?.(3) ?? "-"}
          。路徑點只會保存到目前選中的互動行為；
          切換行為後，只顯示該行為自己的路徑。
        </Text>
        <Space wrap>
          <Button
            icon={<AimOutlined />}
            type={picking ? "primary" : "default"}
            onClick={onArmPick}
          >
            {picking ? "停止取點" : "啟用路徑取點"}
          </Button>
          <Button icon={<PlusOutlined />} onClick={onAppendCurrentPoint}>
            加入目前座標
          </Button>
          <Button danger disabled={!hasPoints} onClick={onClearAll}>
            清空路徑點
          </Button>
        </Space>
      </Space>

      <Card size="small">
        <Space direction="vertical" size="middle" style={{ width: "100%" }}>
          <Space wrap size={[12, 12]}>
            <Form.Item
              name={[...basePath, "pathGraph", "durationMs"]}
              label="移動時間 (ms)"
              style={{ marginBottom: 0 }}
            >
              <InputNumber min={100} step={100} style={{ width: 180 }} />
            </Form.Item>
            <Form.Item
              name={[...basePath, "pathGraph", "holdMs"]}
              label="停留時間 (ms)"
              style={{ marginBottom: 0 }}
            >
              <InputNumber min={0} step={100} style={{ width: 180 }} />
            </Form.Item>
            <Form.Item
              name={[...basePath, "pathGraph", "easing"]}
              label="緩動曲線"
              style={{ marginBottom: 0 }}
            >
              <Select
                style={{ width: 180 }}
                options={["linear", "ease-in", "ease-out", "ease-in-out"].map(
                  (value) => ({ label: value, value }),
                )}
              />
            </Form.Item>
            <Form.Item
              name={[...basePath, "pathGraph", "loop"]}
              label="循環播放"
              valuePropName="checked"
              style={{ marginBottom: 0 }}
            >
              <Switch />
            </Form.Item>
          </Space>

          {hasPoints ? (
            <Table
              size="small"
              pagination={false}
              rowKey={(_, index) => String(index)}
              dataSource={points.map((point, index) => ({
                ...point,
                key: index,
                index,
              }))}
              columns={[
                {
                  title: "順序",
                  dataIndex: "index",
                  width: 72,
                  render: (value) => value + 1,
                },
                {
                  title: "X",
                  dataIndex: "x",
                  render: (value) => Number(value || 0).toFixed(3),
                },
                {
                  title: "Y",
                  dataIndex: "y",
                  render: (value) => Number(value || 0).toFixed(3),
                },
                {
                  title: "操作",
                  key: "action",
                  width: 96,
                  render: (_, __, index) => (
                    <Button
                      type="text"
                      danger
                      icon={<DeleteOutlined />}
                      onClick={() => {
                        const nextPoints = points
                          .filter((_, pointIndex) => pointIndex !== index)
                          .map((point, pointIndex) => ({
                            ...point,
                            order: pointIndex,
                          }));
                        form.setFieldValue(
                          [...basePath, "pathGraph", "points"],
                          nextPoints,
                        );
                      }}
                    />
                  ),
                },
              ]}
            />
          ) : (
            <Empty
              image={Empty.PRESENTED_IMAGE_SIMPLE}
              description="尚未新增任何路徑點。"
            />
          )}
        </Space>
      </Card>
    </Space>
  );
};

export default IndoorPathEditor;
