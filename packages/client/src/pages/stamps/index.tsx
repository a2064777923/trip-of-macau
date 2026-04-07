import { useState, useEffect } from 'react'
import { View, Text, Image, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import './index.scss'

// 印章收集页面
export default function StampsPage() {
  const [stamps, setStamps] = useState<any[]>([])
  const [categories, setCategories] = useState<any[]>([])
  const [selectedCategory, setSelectedCategory] = useState('all')
  const [stats, setStats] = useState({ total: 0, collected: 0 })

  useEffect(() => {
    loadStamps()
    loadCategories()
  }, [])

  // 加载印章数据
  const loadStamps = () => {
    // Mock数据
    const mockStamps = [
      {
        id: 1,
        type: 'location',
        name: '大三巴足迹',
        description: '到达大三巴牌坊',
        icon: '🏛️',
        collected: true,
        collectedAt: '2026-04-01T10:30:00Z',
        poiId: 1,
        rarity: 'common'
      },
      {
        id: 2,
        type: 'location',
        name: '议事亭足迹',
        description: '到达议事亭前地',
        icon: '🏢',
        collected: true,
        collectedAt: '2026-04-01T11:15:00Z',
        poiId: 2,
        rarity: 'common'
      },
      {
        id: 3,
        type: 'location',
        name: '妈阁庙足迹',
        description: '到达妈阁庙',
        icon: '🛕',
        collected: true,
        collectedAt: '2026-04-02T09:20:00Z',
        poiId: 3,
        rarity: 'common'
      },
      {
        id: 4,
        type: 'story',
        name: '海上丝路 · 序章',
        description: '完成海上丝路序章',
        icon: '📖',
        collected: true,
        collectedAt: '2026-04-02T14:30:00Z',
        storyId: 1,
        rarity: 'rare'
      },
      {
        id: 5,
        type: 'story',
        name: '海上丝路 · 第一章',
        description: '完成海上丝路第一章',
        icon: '📜',
        collected: true,
        collectedAt: '2026-04-03T16:45:00Z',
        storyId: 1,
        rarity: 'rare'
      },
      {
        id: 6,
        type: 'mission',
        name: '初探澳门',
        description: '收集3个足迹印章',
        icon: '🎯',
        collected: true,
        collectedAt: '2026-04-02T10:00:00Z',
        rarity: 'common'
      },
      {
        id: 7,
        type: 'location',
        name: '港务局大楼足迹',
        description: '到达港务局大楼',
        icon: '🏰',
        collected: false,
        poiId: 4,
        rarity: 'common'
      },
      {
        id: 8,
        type: 'location',
        name: '玫瑰堂足迹',
        description: '到达玫瑰堂',
        icon: '⛪',
        collected: false,
        poiId: 5,
        rarity: 'common'
      },
      {
        id: 9,
        type: 'secret',
        name: '隐藏的秘密',
        description: '发现隐藏地点',
        icon: '🌟',
        collected: false,
        rarity: 'epic'
      }
    ]

    setStamps(mockStamps)
    setStats({
      total: mockStamps.length,
      collected: mockStamps.filter(s => s.collected).length
    })
  }

  // 加载分类
  const loadCategories = () => {
    setCategories([
      { id: 'all', name: '全部', icon: '🏆' },
      { id: 'location', name: '足迹', icon: '📍' },
      { id: 'story', name: '故事', icon: '📖' },
      { id: 'mission', name: '任务', icon: '🎯' },
      { id: 'secret', name: '秘密', icon: '🌟' }
    ])
  }

  // 获取分类名称
  const getCategoryName = (type: string) => {
    const category = categories.find(c => c.id === type)
    return category?.name || type
  }

  // 筛选印章
  const filteredStamps = stamps.filter(stamp => {
    if (selectedCategory === 'all') return true
    return stamp.type === selectedCategory
  })

  // 查看印章详情
  const handleStampClick = (stamp: any) => {
    if (!stamp.collected) {
      Taro.showToast({ 
        title: '还未收集此印章', 
        icon: 'none' 
      })
      return
    }
    
    Taro.navigateTo({
      url: `/pages/stamps/detail/index?id=${stamp.id}`
    })
  }

  return (
    <View className='stamps-page'>
      {/* 头部统计 */}
      <View className='header-stats'>
        <View className='stats-card'>
          <View className='stats-content'>
            <View className='stats-item'>
              <Text className='stats-number'>{stats.collected}</Text>
              <Text className='stats-label'>已收集</Text>
            </View>
            <View className='stats-divider' />
            <View className='stats-item'>
              <Text className='stats-number'>{stats.total}</Text>
              <Text className='stats-label'>总印章</Text>
            </View>
            <View className='stats-divider' />
            <View className='stats-item'>
              <Text className='stats-number'>
                {Math.round((stats.collected / stats.total) * 100)}%
              </Text>
              <Text className='stats-label'>完成度</Text>
            </View>
          </View>
        </View>
      </View>

      {/* 分类筛选 */}
      <View className='category-filter'>
        <ScrollView className='category-scroll' scrollX>
          {categories.map((category, index) => (
            <View
              key={category.id}
              className={`category-item ${selectedCategory === category.id ? 'active' : ''}`}
              onClick={() => setSelectedCategory(category.id)}
              style={{ animationDelay: `${index * 0.05}s` }}
            >
              <Text className='category-icon'>{category.icon}</Text>
              <Text className='category-name'>{category.name}</Text>
            </View>
          ))}
        </ScrollView>
      </View>

      {/* 印章网格 */}
      <ScrollView className='stamps-grid' scrollY>
        {filteredStamps.length === 0 ? (
          <View className='empty-state'>
            <Text className='empty-icon'>🔍</Text>
            <Text className='empty-text'>暂无此类印章</Text>
          </View>
        ) : (
          <View className='grid-container'>
            {filteredStamps.map((stamp, index) => (
              <View
                key={stamp.id}
                className={`stamp-card ${stamp.collected ? 'collected' : 'locked'}`}
                onClick={() => handleStampClick(stamp)}
                style={{ animationDelay: `${index * 0.05}s` }}
              >
                {/* 印章图标 */}
                <View className='stamp-icon-wrap'>
                  <Text className='stamp-icon'>{stamp.collected ? stamp.icon : '🔒'}</Text>
                  {stamp.collected && (
                    <View className='collected-badge'>✓</View>
                  )}
                </View>

                {/* 印章信息 */}
                <View className='stamp-info'>
                  <Text className='stamp-name'>{stamp.name}</Text>
                  <Text className='stamp-type'>{getCategoryName(stamp.type)}</Text>
                  {stamp.collected && stamp.collectedAt && (
                    <Text className='stamp-date'>
                      {new Date(stamp.collectedAt).toLocaleDateString('zh-CN')}
                    </Text>
                  )}
                </View>
              </View>
            ))}
          </View>
        )}

        {/* 底部留白 */}
        <View className='bottom-spacer' />
      </ScrollView>
    </View>
  )
}
