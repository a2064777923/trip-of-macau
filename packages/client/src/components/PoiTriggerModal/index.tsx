import { View, Image, Text } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { useState } from 'react'
import type { PoiItem } from '../../types/game'
import './index.scss'

interface PoiTriggerModalProps {
  poi: PoiItem
  visible: boolean
  onClose: () => void
  onCheckin: () => void
}

export default function PoiTriggerModal({ poi, visible, onClose, onCheckin }: PoiTriggerModalProps) {
  const [isChecking, setIsChecking] = useState(false)

  if (!visible) {
    return null
  }

  const handleCheckin = async () => {
    if (isChecking) return
    
    setIsChecking(true)
    try {
      await onCheckin()
      Taro.showToast({
        title: '打卡成功！',
        icon: 'success',
        duration: 2000,
      })
      setTimeout(() => {
        onClose()
      }, 2000)
    } catch (error: any) {
      Taro.showToast({
        title: error.message || '打卡失敗',
        icon: 'none',
        duration: 2000,
      })
    } finally {
      setIsChecking(false)
    }
  }

  return (
    <View className="poi-trigger-modal">
      <View className="poi-trigger-modal__overlay" onClick={onClose} />
      <View className="poi-trigger-modal__content">
        {/* 封面图片 */}
        {poi.mapIconUrl && (
          <View className="poi-trigger-modal__cover">
            <Image
              className="poi-trigger-modal__cover-image"
              src={poi.mapIconUrl}
              mode="aspectFill"
            />
            <View className="poi-trigger-modal__cover-gradient" />
          </View>
        )}

        {/* 内容区域 */}
        <View className="poi-trigger-modal__body">
          {/* 图标和标题 */}
          <View className="poi-trigger-modal__header">
            <View className="poi-trigger-modal__icon">{poi.icon}</View>
            <View className="poi-trigger-modal__title-group">
              <Text className="poi-trigger-modal__title">{poi.name}</Text>
              {poi.subtitle && (
                <Text className="poi-trigger-modal__subtitle">{poi.subtitle}</Text>
              )}
            </View>
          </View>

          {/* 描述 */}
          {poi.description && (
            <View className="poi-trigger-modal__description">
              <Text className="poi-trigger-modal__description-text">{poi.description}</Text>
            </View>
          )}

          {/* 标签 */}
          {poi.tags && poi.tags.length > 0 && (
            <View className="poi-trigger-modal__tags">
              {poi.tags.slice(0, 3).map((tag, index) => (
                <View key={index} className="poi-trigger-modal__tag">
                  <Text className="poi-trigger-modal__tag-text">{tag}</Text>
                </View>
              ))}
            </View>
          )}

          {/* 奖励信息 */}
          {poi.rewardStampId && (
            <View className="poi-trigger-modal__reward">
              <View className="poi-trigger-modal__reward-icon">🏅</View>
              <Text className="poi-trigger-modal__reward-text">
                打卡可獲得印章獎勵
              </Text>
            </View>
          )}

          {/* 故事线信息 */}
          {poi.storyName && (
            <View className="poi-trigger-modal__story">
              <View className="poi-trigger-modal__story-icon">📖</View>
              <Text className="poi-trigger-modal__story-text">
                {poi.storyName}
              </Text>
            </View>
          )}
        </View>

        {/* 操作按钮 */}
        <View className="poi-trigger-modal__actions">
          <View className="poi-trigger-modal__button poi-trigger-modal__button--secondary" onClick={onClose}>
            <Text className="poi-trigger-modal__button-text">稍後再說</Text>
          </View>
          <View
            className={`poi-trigger-modal__button poi-trigger-modal__button--primary ${isChecking ? 'poi-trigger-modal__button--loading' : ''}`}
            onClick={handleCheckin}
          >
            <Text className="poi-trigger-modal__button-text">
              {isChecking ? '打卡中...' : '立即打卡'}
            </Text>
          </View>
        </View>
      </View>
    </View>
  )
}
