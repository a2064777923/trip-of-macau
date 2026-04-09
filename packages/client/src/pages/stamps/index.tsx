import { useMemo } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getStamps, loadGameState } from '../../services/gameService'
import './index.scss'

const categoryMeta = [
  { id: 'all', name: '全部', icon: '🏆' },
  { id: 'location', name: '足跡', icon: '📍' },
  { id: 'story', name: '故事', icon: '📖' },
  { id: 'mission', name: '任務', icon: '🎯' },
  { id: 'secret', name: '秘密', icon: '🌟' },
] as const

export default function StampsPage() {
  const state = useMemo(() => loadGameState(), [])
  const stamps = useMemo(() => getStamps(), [])
  const collected = stamps.filter((stamp) => stamp.collected)
  const progress = Math.round((collected.length / stamps.length) * 100)

  return (
    <View className='stamps-page'>
      <View className='header-stats'>
        <View className='stats-card'>
          <View className='stats-content'>
            <View className='stats-item'>
              <Text className='stats-number'>{collected.length}</Text>
              <Text className='stats-label'>已收集</Text>
            </View>
            <View className='stats-divider' />
            <View className='stats-item'>
              <Text className='stats-number'>{stamps.length}</Text>
              <Text className='stats-label'>總印章</Text>
            </View>
            <View className='stats-divider' />
            <View className='stats-item'>
              <Text className='stats-number'>{progress}%</Text>
              <Text className='stats-label'>完成度</Text>
            </View>
          </View>
          <Text className='stats-tip'>當前等級 Lv.{state.user.level} · 稱號 {state.user.title}</Text>
        </View>
      </View>

      <View className='category-filter'>
        <ScrollView className='category-scroll' scrollX>
          {categoryMeta.map((category) => (
            <View key={category.id} className='category-item active'>
              <Text className='category-icon'>{category.icon}</Text>
              <Text className='category-name'>{category.name}</Text>
            </View>
          ))}
        </ScrollView>
      </View>

      <ScrollView className='stamps-grid' scrollY>
        <View className='grid-container'>
          {stamps.map((stamp) => (
            <View
              key={stamp.id}
              className={`stamp-card ${stamp.collected ? 'collected' : 'locked'}`}
              onClick={() => {
                if (!stamp.collected) {
                  Taro.showToast({ title: '繼續探索即可解鎖', icon: 'none' })
                }
              }}
            >
              <View className='stamp-icon-wrap'>
                <Text className='stamp-icon'>{stamp.collected ? stamp.icon : '🔒'}</Text>
                {stamp.collected && <View className='collected-badge'>✓</View>}
              </View>

              <View className='stamp-info'>
                <Text className='stamp-name'>{stamp.name}</Text>
                <Text className='stamp-type'>{categoryMeta.find((item) => item.id === stamp.type)?.name || stamp.type}</Text>
                <Text className='stamp-rarity'>{stamp.rarity === 'epic' ? '史詩' : stamp.rarity === 'rare' ? '稀有' : '普通'}</Text>
                <Text className='stamp-desc'>{stamp.description}</Text>
              </View>
            </View>
          ))}
        </View>
        <View className='bottom-spacer' />
      </ScrollView>
    </View>
  )
}
