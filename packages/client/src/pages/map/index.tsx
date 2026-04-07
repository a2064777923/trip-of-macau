import { useEffect, useState, useRef } from 'react'
import { View, Text, Canvas, CoverView, Button } from '@tarojs/components'
import Taro from '@tarojs/taro'
import './index.scss'

// 地图页面 - 探索主入口
export default function MapPage() {
  const canvasRef = useRef<any>(null)
  const [location, setLocation] = useState<any>(null)
  const [nearbyPOIs, setNearbyPOIs] = useState<any[]>([])
  const [showTriggerPopup, setShowTriggerPopup] = useState(false)
  const [triggeredPOI, setTriggeredPOI] = useState<any>(null)

  useEffect(() => {
    // 页面加载时获取位置
    initLocation()
    
    // 加载地图
    loadMap()
    
    // 开始位置监听
    startLocationListening()

    return () => {
      // 清理工作
      stopLocationListening()
    }
  }, [])

  // 初始化位置
  const initLocation = async () => {
    try {
      const res = await Taro.getLocation({
        type: 'gcj02',
        altitude: false
      })
      
      setLocation({
        latitude: res.latitude,
        longitude: res.longitude,
        accuracy: res.accuracy
      })
      
      console.log('当前位置:', res)
      
      // 获取附近POI
      fetchNearbyPOIs(res.latitude, res.longitude)
    } catch (err) {
      console.error('获取位置失败:', err)
      Taro.showToast({
        title: '请授权位置信息',
        icon: 'none'
      })
    }
  }

  // 加载地图
  const loadMap = () => {
    // 使用 Canvas 2D 渲染手绘地图
    const query = Taro.createSelectorQuery()
    query.select('#mapCanvas')
      .fields({ node: true, size: true })
      .exec((res) => {
        if (res[0]) {
          const canvas = res[0].node
          const ctx = canvas.getContext('2d')
          
          // 设置Canvas尺寸
          const { width, height } = res[0]
          canvas.width = width
          canvas.height = height
          
          // 绘制地图（简化版，实际应从CDN加载瓦片）
          drawMap(ctx, width, height)
        }
      })
  }

  // 绘制地图
  const drawMap = (ctx: any, width: number, height: number) => {
    // 背景色
    ctx.fillStyle = '#FFF8E7'
    ctx.fillRect(0, 0, width, height)
    
    // 绘制简化地图内容（实际应从CDN加载手绘地图瓦片）
    ctx.fillStyle = '#FFE4C4'
    ctx.beginPath()
    ctx.arc(width / 2, height / 2, 100, 0, Math.PI * 2)
    ctx.fill()
    
    // 地图标题
    ctx.fillStyle = '#C8102E'
    ctx.font = 'bold 24px sans-serif'
    ctx.textAlign = 'center'
    ctx.fillText('澳门半岛', width / 2, height / 2)
  }

  // 获取附近POI
  const fetchNearbyPOIs = async (lat: number, lng: number) => {
    // Mock数据，后续对接API
    const mockPOIs = [
      {
        id: 1,
        name: '大三巴牌坊',
        distance: 120,
        icon: '🏛️',
        triggerRadius: 50
      },
      {
        id: 2,
        name: '议事亭前地',
        distance: 280,
        icon: '🏢',
        triggerRadius: 50
      },
      {
        id: 3,
        name: '妈阁庙',
        distance: 450,
        icon: '🛕',
        triggerRadius: 50
      }
    ]
    setNearbyPOIs(mockPOIs)
  }

  // 开始位置监听
  const startLocationListening = () => {
    // 使用微信小程序的位置监听API
    wx.startLocationUpdateBackground({
      success: () => {
        console.log('开始后台位置监听')
      },
      fail: (err) => {
        console.error('位置监听失败:', err)
      }
    })
  }

  // 停止位置监听
  const stopLocationListening = () => {
    wx.stopLocationUpdate()
  }

  // 手动打卡
  const handleManualCheckin = () => {
    Taro.navigateTo({
      url: '/pages/pois/list'
    })
  }

  return (
    <View className='map-page'>
      {/* 地图 Canvas */}
      <Canvas
        type='2d'
        id='mapCanvas'
        className='map-canvas'
      />
      
      {/* 地图覆盖层 - 定位和POI标记 */}
      <CoverView className='map-overlay'>
        {/* 当前位置标记 */}
        {location && (
          <CoverView className='location-marker'>
            <CoverView className='marker-pulse' />
            <CoverView className='marker-dot' />
          </CoverView>
        )}
        
        {/* 附近POI标记 */}
        {nearbyPOIs.map((poi, index) => (
          <CoverView
            key={poi.id}
            className='poi-marker'
            style={{ top: `${150 + index * 80}px`, right: `${50 + index * 30}px` }}
          >
            <CoverView className='poi-icon'>{poi.icon}</CoverView>
            <CoverView className='poi-name'>{poi.name}</CoverView>
            <CoverView className='poi-distance'>{poi.distance}m</CoverView>
          </CoverView>
        ))}
      </CoverView>

      {/* 底部信息面板 */}
      <View className='bottom-panel'>
        <View className='location-info'>
          {location ? (
            <>
              <Text className='location-text'>
                当前位置: {location.latitude.toFixed(6)}, {location.longitude.toFixed(6)}
              </Text>
              <Text className='accuracy-text'>精度: ±{Math.round(location.accuracy)}米</Text>
            </>
          ) : (
            <Text className='location-text'>正在获取位置...</Text>
          )}
        </View>
        
        <View className='action-buttons'>
          <Button className='btn-primary' onClick={handleStartExplore}>
            刷新位置
          </Button>
          <Button className='btn-secondary' onClick={handleManualCheckin}>
            手动打卡
          </Button>
        </View>
      </View>

      {/* 触发弹窗（到达POI时显示） */}
      {showTriggerPopup && triggeredPOI && (
        <View className='trigger-popup'>
          <View className='popup-content'>
            <Text className='popup-title'>🎉 到达 {triggeredPOI.name}</Text>
            <Text className='popup-desc'>恭喜！你获得了新的足迹印章</Text>
            <View className='popup-actions'>
              <Button className='btn-primary' onClick={() => setShowTriggerPopup(false)}>
                查看详情
              </Button>
              <Button className='btn-text' onClick={() => setShowTriggerPopup(false)}>
                知道了
              </Button>
            </View>
          </View>
        </View>
      )}
    </View>
  )
}
