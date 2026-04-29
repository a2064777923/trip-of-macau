import React, { useMemo } from "react";
import { AimOutlined } from "@ant-design/icons";
import { Alert, Button, Empty, Space, Spin, Tag, Typography } from "antd";
import type {
  AdminIndoorFloorItem,
  AdminIndoorNodeItem,
  AdminIndoorNodePoint,
  AdminIndoorOverlayGeometry,
} from "../../types/admin";

interface Props {
  floor?: AdminIndoorFloorItem | null;
  nodes?: AdminIndoorNodeItem[];
  loading?: boolean;
  pickMode: "marker" | "path" | "overlay";
  currentPoint?: { x?: number | null; y?: number | null };
  currentPathPoints?: AdminIndoorNodePoint[];
  currentOverlayGeometry?: AdminIndoorOverlayGeometry | null;
  onChangePickMode: (mode: "marker" | "path" | "overlay") => void;
  onPick: (point: { x: number; y: number }) => void;
}

const { Text } = Typography;

const svgSize = 1000;

function getModeLabel(pickMode: "marker" | "path" | "overlay") {
  switch (pickMode) {
    case "path":
      return "路徑取點";
    case "overlay":
      return "疊加物取點";
    default:
      return "標記取點";
  }
}

const IndoorRuleWorkbenchMapPanel: React.FC<Props> = ({
  floor,
  nodes = [],
  loading = false,
  pickMode,
  currentPoint,
  currentPathPoints = [],
  currentOverlayGeometry,
  onChangePickMode,
  onPick,
}) => {
  const previewImageUrl = floor?.tilePreviewImageUrl || floor?.floorPlanUrl;
  const overlayPoints = currentOverlayGeometry?.points || [];

  const pathPolyline = useMemo(
    () =>
      currentPathPoints.length
        ? currentPathPoints
            .map(
              (point) =>
                `${(Number(point.x || 0) * svgSize).toFixed(1)},${(Number(point.y || 0) * svgSize).toFixed(1)}`,
            )
            .join(" ")
        : "",
    [currentPathPoints],
  );

  const overlayPointString = useMemo(
    () =>
      overlayPoints.length
        ? overlayPoints
            .map(
              (point) =>
                `${(Number(point.x || 0) * svgSize).toFixed(1)},${(Number(point.y || 0) * svgSize).toFixed(1)}`,
            )
            .join(" ")
        : "",
    [overlayPoints],
  );

  const handlePickAtClientPosition = (
    clientX: number,
    clientY: number,
    rect: DOMRect,
  ) => {
    if (!rect.width || !rect.height) {
      return;
    }
    const x = Number(
      Math.max(0, Math.min(1, (clientX - rect.left) / rect.width)).toFixed(6),
    );
    const y = Number(
      Math.max(0, Math.min(1, (clientY - rect.top) / rect.height)).toFixed(6),
    );
    onPick({ x, y });
  };

  const handleWrapperClick = (event: React.MouseEvent<HTMLElement>) => {
    handlePickAtClientPosition(
      event.clientX,
      event.clientY,
      event.currentTarget.getBoundingClientRect(),
    );
  };

  if (loading) {
    return (
      <div
        style={{
          minHeight: 360,
          borderRadius: 16,
          border: "1px solid #f0f0f0",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          background: "#fafafa",
        }}
      >
        <Space direction="vertical" size={8} align="center">
          <Spin />
          <Text type="secondary">正在同步縮略地圖、標記與路徑資料…</Text>
        </Space>
      </div>
    );
  }

  if (!previewImageUrl) {
    return <Empty description="目前樓層尚未配置縮略圖或平面圖。" />;
  }

  return (
    <Space direction="vertical" size="middle" style={{ width: "100%" }}>
      <Alert
        type="info"
        showIcon
        message="縮略圖取點"
        description="座標會依照圖片實際顯示範圍計算，不再使用 contain 背景整框換算，避免標點偏移。"
      />

      <Space wrap>
        <Button
          icon={<AimOutlined />}
          type={pickMode === "marker" ? "primary" : "default"}
          onClick={() => onChangePickMode("marker")}
        >
          標記取點
        </Button>
        <Button
          icon={<AimOutlined />}
          type={pickMode === "path" ? "primary" : "default"}
          onClick={() => onChangePickMode("path")}
        >
          路徑取點
        </Button>
        <Button
          icon={<AimOutlined />}
          type={pickMode === "overlay" ? "primary" : "default"}
          onClick={() => onChangePickMode("overlay")}
        >
          疊加物取點
        </Button>
        <Tag
          color={
            pickMode === "marker"
              ? "blue"
              : pickMode === "path"
                ? "purple"
                : "green"
          }
        >
          目前模式：{getModeLabel(pickMode)}
        </Tag>
        <Tag>
          既有標記{" "}
          {
            nodes.filter(
              (node) => node.relativeX != null && node.relativeY != null,
            ).length
          }
        </Tag>
        {currentPoint?.x != null && currentPoint?.y != null ? (
          <>
            <Tag color="gold">X {Number(currentPoint.x).toFixed(3)}</Tag>
            <Tag color="gold">Y {Number(currentPoint.y).toFixed(3)}</Tag>
          </>
        ) : null}
        {currentPathPoints.length ? (
          <Tag color="purple">PATH {currentPathPoints.length}</Tag>
        ) : null}
        {overlayPoints.length ? (
          <Tag color="green">OVERLAY {overlayPoints.length}</Tag>
        ) : null}
      </Space>

      <div
        style={{
          minHeight: 420,
          borderRadius: 16,
          border: "1px solid #e5e7eb",
          background: "linear-gradient(180deg, #f8fafc 0%, #eef2ff 100%)",
          padding: 16,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          overflow: "auto",
        }}
      >
        <div
          style={{
            position: "relative",
            display: "inline-block",
            cursor: "crosshair",
            lineHeight: 0,
            maxWidth: "100%",
          }}
        >
          <img
            src={previewImageUrl}
            alt={
              floor?.floorNameZht ||
              floor?.floorNameZh ||
              floor?.floorCode ||
              "floor-preview"
            }
            style={{
              display: "block",
              maxWidth: "100%",
              maxHeight: 560,
              width: "auto",
              height: "auto",
              borderRadius: 12,
              boxShadow: "0 16px 40px rgba(15, 23, 42, 0.12)",
              pointerEvents: "none",
            }}
          />

          <button
            type="button"
            aria-label="pick-indoor-map-point"
            onClick={handleWrapperClick}
            style={{
              position: "absolute",
              inset: 0,
              zIndex: 1,
              border: "none",
              background: "transparent",
              padding: 0,
              cursor: "crosshair",
            }}
          />

          <svg
            viewBox={`0 0 ${svgSize} ${svgSize}`}
            style={{
              position: "absolute",
              inset: 0,
              zIndex: 2,
              width: "100%",
              height: "100%",
              pointerEvents: "none",
            }}
          >
            {pathPolyline ? (
              <polyline
                points={pathPolyline}
                fill="none"
                stroke="#7c3aed"
                strokeWidth={12}
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            ) : null}
            {overlayPointString &&
            currentOverlayGeometry?.geometryType === "polygon" ? (
              <polygon
                points={overlayPointString}
                fill="rgba(22,119,255,0.14)"
                stroke="#1677ff"
                strokeWidth={10}
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            ) : null}
            {overlayPointString &&
            currentOverlayGeometry?.geometryType === "polyline" ? (
              <polyline
                points={overlayPointString}
                fill="none"
                stroke="#1677ff"
                strokeWidth={10}
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            ) : null}
          </svg>

          {nodes
            .filter((node) => node.relativeX != null && node.relativeY != null)
            .map((node) => (
              <div
                key={node.id}
                style={{
                  position: "absolute",
                  left: `${Number(node.relativeX) * 100}%`,
                  top: `${Number(node.relativeY) * 100}%`,
                  transform: "translate(-50%, -50%)",
                  width: 14,
                  height: 14,
                  borderRadius: 999,
                  background: "#ef4444",
                  border: "2px solid #fff",
                  boxShadow: "0 4px 12px rgba(15, 23, 42, 0.25)",
                  zIndex: 3,
                  pointerEvents: "none",
                }}
                title={node.nodeNameZht || node.nodeNameZh || node.markerCode}
              />
            ))}

          {currentPoint?.x != null && currentPoint?.y != null ? (
            <div
              style={{
                position: "absolute",
                left: `${Number(currentPoint.x) * 100}%`,
                top: `${Number(currentPoint.y) * 100}%`,
                transform: "translate(-50%, -50%)",
                width: 18,
                height: 18,
                borderRadius: 999,
                border: "3px solid #f59e0b",
                background: "#fff",
                boxShadow: "0 0 0 6px rgba(245, 158, 11, 0.2)",
                zIndex: 3,
                pointerEvents: "none",
              }}
            />
          ) : null}
        </div>
      </div>
    </Space>
  );
};

export default IndoorRuleWorkbenchMapPanel;
