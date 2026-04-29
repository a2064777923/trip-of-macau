import { View, Text } from '@tarojs/components'
import { useState, useRef, useEffect } from 'react'
import './index.scss'

interface TestJoystickProps {
  currentLocation: { latitude: number; longitude: number }
  onLocationChange: (location: { latitude: number; longitude: number }) => void
  onReset: () => void
}

export default function TestJoystick({ currentLocation, onLocationChange, onReset }: TestJoystickProps) {
  const [isOpen, setIsOpen] = useState(false)
  const [isDragging, setIsDragging] = useState(false)
  const [stickPosition, setStickPosition] = useState({ x: 0, y: 0 })
  const joystickRef = useRef<HTMLDivElement>(null)
  const animationFrameRef = useRef<number>()

  // 摇杆移动速度（每秒移动的度数）
  const MOVE_SPEED = 0.0001 // 约11米/秒

  useEffect(() => {
    if (!isDragging || (stickPosition.x === 0 && stickPosition.y === 0)) {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current)
      }
      return
    }

    let lastTime = Date.now()

    const updateLocation = () => {
      const now = Date.now()
      const deltaTime = (now - lastTime) / 1000 // 转换为秒
      lastTime = now

      // 计算移动距离
      const deltaLat = -stickPosition.y * MOVE_SPEED * deltaTime * 60
      const deltaLng = stickPosition.x * MOVE_SPEED * deltaTime * 60

      // 更新位置
      onLocationChange({
        latitude: currentLocation.latitude + deltaLat,
        longitude: currentLocation.longitude + deltaLng,
      })

      animationFrameRef.current = requestAnimationFrame(updateLocation)
    }

    animationFrameRef.current = requestAnimationFrame(updateLocation)

    return () => {
      if (animationFrameRef.current) {
        cancelAnimationFrame(animationFrameRef.current)
      }
    }
  }, [isDragging, stickPosition, currentLocation, onLocationChange])

  const handleTouchStart = (e: any) => {
    e.stopPropagation()
    setIsDragging(true)
  }

  const handleTouchMove = (e: any) => {
    if (!isDragging || !joystickRef.current) return

    e.stopPropagation()
    const touch = e.touches[0]
    const rect = joystickRef.current.getBoundingClientRect()
    const centerX = rect.left + rect.width / 2
    const centerY = rect.top + rect.height / 2

    // 计算相对于中心的偏移
    let offsetX = touch.clientX - centerX
    let offsetY = touch.clientY - centerY

    // 限制在圆形范围内
    const maxRadius = 50 // 最大半径
    const distance = Math.sqrt(offsetX * offsetX + offsetY * offsetY)
    if (distance > maxRadius) {
      offsetX = (offsetX / distance) * maxRadius
      offsetY = (offsetY / distance) * maxRadius
    }

    // 归一化到 -1 到 1
    setStickPosition({
      x: offsetX / maxRadius,
      y: offsetY / maxRadius,
    })
  }

  const handleTouchEnd = (e: any) => {
    e.stopPropagation()
    setIsDragging(false)
    setStickPosition({ x: 0, y: 0 })
  }

  const handleReset = () => {
    onReset()
    setStickPosition({ x: 0, y: 0 })
  }

  return (
    <View className="test-joystick">
      {/* 切换按钮 */}
      <View
        className={`test-joystick__toggle ${isOpen ? 'test-joystick__toggle--open' : ''}`}
        onClick={() => setIsOpen(!isOpen)}
      >
        <Text className="test-joystick__toggle-icon">{isOpen ? '✕' : '🎮'}</Text>
      </View>

      {/* 摇杆面板 */}
      {isOpen && (
        <View className="test-joystick__panel">
          <View className="test-joystick__header">
            <Text className="test-joystick__title">測試模式</Text>
            <View className="test-joystick__close" onClick={() => setIsOpen(false)}>
              <Text className="test-joystick__close-icon">✕</Text>
            </View>
          </View>

          {/* 位置信息 */}
          <View className="test-joystick__info">
            <View className="test-joystick__info-row">
              <Text className="test-joystick__info-label">緯度</Text>
              <Text className="test-joystick__info-value">
                {currentLocation.latitude.toFixed(6)}
              </Text>
            </View>
            <View className="test-joystick__info-row">
              <Text className="test-joystick__info-label">經度</Text>
              <Text className="test-joystick__info-value">
                {currentLocation.longitude.toFixed(6)}
              </Text>
            </View>
          </View>

          {/* 摇杆 */}
          <View className="test-joystick__joystick-container" ref={joystickRef}>
            <View className="test-joystick__joystick-base">
              <View className="test-joystick__joystick-center" />
            </View>
            <View
              className={`test-joystick__joystick-stick ${isDragging ? 'test-joystick__joystick-stick--active' : ''}`}
              style={{
                transform: `translate(calc(-50% + ${stickPosition.x * 50}px), calc(-50% + ${stickPosition.y * 50}px))`,
              }}
              onTouchStart={handleTouchStart}
              onTouchMove={handleTouchMove}
              onTouchEnd={handleTouchEnd}
            />
          </View>

          {/* 控制按钮 */}
          <View className="test-joystick__controls">
            <View className="test-joystick__button" onClick={handleReset}>
              <Text>重置位置</Text>
            </View>
            <View className="test-joystick__button test-joystick__button--primary" onClick={() => setIsOpen(false)}>
              <Text>關閉</Text>
            </View>
          </View>
        </View>
      )}
    </View>
  )
}
