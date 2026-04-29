import React from "react";
import { AimOutlined, DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import {
  Button,
  Card,
  Empty,
  Form,
  Select,
  Space,
  Table,
  Typography,
} from "antd";
import type { FormInstance } from "antd/es/form";
import type { AdminIndoorNodePoint } from "../../types/admin";

interface Props {
  form: FormInstance;
  name?: Array<string | number>;
  currentPoint?: { x?: number | null; y?: number | null };
  onArmPick?: () => void;
  onAppendCurrentPoint?: () => void;
  onClearAll?: () => void;
  picking?: boolean;
}

const { Text } = Typography;

const geometryTypeOptions = [
  { label: "點", value: "point" },
  { label: "折線", value: "polyline" },
  { label: "多邊形", value: "polygon" },
];

const IndoorOverlayGeometryEditor: React.FC<Props> = ({
  form,
  name = ["overlayGeometry"],
  currentPoint,
  onArmPick,
  onAppendCurrentPoint,
  onClearAll,
  picking = false,
}) => {
  const watchOptions = React.useMemo(() => ({ form, preserve: true }), [form]);
  const points =
    (Form.useWatch([...name, "points"], watchOptions) as
      | AdminIndoorNodePoint[]
      | undefined) || [];

  const hasPoints = points.length > 0;

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Space style={{ width: "100%", justifyContent: "space-between" }} wrap>
        <Text type="secondary">
          當前座標：{currentPoint?.x?.toFixed?.(3) ?? "-"} /{" "}
          {currentPoint?.y?.toFixed?.(3) ?? "-"}
          。疊加物幾何只會保存到目前選中的互動行為；
          切換行為後，只顯示該行為自己的幾何。若為點狀疊加物，重新取點會覆蓋上一點。
        </Text>
        <Space wrap>
          <Button
            icon={<AimOutlined />}
            type={picking ? "primary" : "default"}
            onClick={onArmPick}
          >
            {picking ? "停止取點" : "啟用疊加物取點"}
          </Button>
          <Button icon={<PlusOutlined />} onClick={onAppendCurrentPoint}>
            加入目前座標
          </Button>
          <Button danger disabled={!hasPoints} onClick={onClearAll}>
            清空疊加物
          </Button>
        </Space>
      </Space>

      <Card size="small">
        <Form.Item name={[...name, "geometryType"]} label="疊加物幾何類型">
          <Select options={geometryTypeOptions} />
        </Form.Item>

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
                      form.setFieldValue([...name, "points"], nextPoints);
                    }}
                  />
                ),
              },
            ]}
          />
        ) : (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description="尚未新增任何疊加物幾何點。"
          />
        )}
      </Card>
    </Space>
  );
};

export default IndoorOverlayGeometryEditor;
