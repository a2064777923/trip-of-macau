import { useState, useEffect, useRef } from 'react'
import { Text, View, Image, ScrollView, Button, Camera } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../../components/PageShell'
import './index.scss'

export default function IndoorMapPage() {
  const router = Taro.getCurrentInstance().router
  const poiName = router?.params?.poiName || router?.params?.name || '景點'
  const title = router?.params?.title || `${poiName} 室內地圖`
  
  const [activeFloor, setActiveFloor] = useState('1f')
  const [scale, setScale] = useState(0.8)
  const [offset, setOffset] = useState({ x: 0, y: 0 })
  const [heading, setHeading] = useState(0)
  const [showAR, setShowAR] = useState(false)
  const [isScanning, setIsScanning] = useState(false)
  const [compassEnabled, setCompassEnabled] = useState(false)
  const [detailMode, setDetailMode] = useState(false)
  const [tileLoadError, setTileLoadError] = useState(false)
  const [cameraReady, setCameraReady] = useState(false)
  const [arResult, setArResult] = useState<{floor: string, message: string, x: number, y: number} | null>(null)
  
  const dragRef = useRef({ startX: 0, startY: 0, active: false })
  const scanTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  
  const floors = [
    { id: 'g', label: '地下 / Ground Floor', size: { w: 1280, h: 1280 }, tiles: [0,1,2,3,4].flatMap(x => [0,1,2,3].map(y => `${x}_${y}.png`)) },
    { id: '1f', label: '一樓 / 1st Floor', size: { w: 1792, h: 1280 }, tiles: [0,1,2,3,4,5,6].flatMap(x => [0,1,2,3].map(y => `${x}_${y}.png`)) },
    { id: '2f', label: '二樓 / 2nd Floor', size: { w: 1280, h: 1280 }, tiles: [0,1,2,3,4].flatMap(x => [0,1,2,3].map(y => `${x}_${y}.png`)) }
  ]

  const currentFloorData = floors.find(f => f.id === activeFloor)

  useEffect(() => {
    return () => {
      if (scanTimerRef.current) {
        clearTimeout(scanTimerRef.current)
      }
      if (compassEnabled) {
        Taro.stopCompass()
      }
    }
  }, [compassEnabled])

  const handleTouchStart = (e) => {
    if (e.touches.length === 1) {
      dragRef.current = {
        startX: e.touches[0].clientX - offset.x,
        startY: e.touches[0].clientY - offset.y,
        active: true
      }
    }
  }

  const handleTouchMove = (e) => {
    if (dragRef.current.active && e.touches.length === 1) {
      setOffset({
        x: e.touches[0].clientX - dragRef.current.startX,
        y: e.touches[0].clientY - dragRef.current.startY
      })
    }
  }

  const handleTouchEnd = () => {
    dragRef.current.active = false
  }
  
  const zoomIn = () => setScale(s => Math.min(s * 1.2, 3))
  const zoomOut = () => setScale(s => Math.max(s * 0.8, 0.4))

  const enableDetailMode = () => {
    setTileLoadError(false)
    setDetailMode(true)
  }
  
  const startARScan = () => {
    enableDetailMode()
    setShowAR(true)
    setIsScanning(true)

    if (!compassEnabled) {
      Taro.startCompass({
        success: () => {
          setCompassEnabled(true)
          Taro.onCompassChange((res) => {
            setHeading(res.direction)
          })
        },
        fail: () => {
          Taro.showToast({ title: '羅盤初始化失敗，先使用靜態定位', icon: 'none' })
        },
      })
    }

    scanTimerRef.current = setTimeout(() => {
      setIsScanning(false)
      setArResult({
        floor: '1f',
        message: '定位成功：你在「一樓 / 1st Floor」的中庭附近',
        x: 600,
        y: 600
      })
      setActiveFloor('1f')

      const systemInfo = Taro.getSystemInfoSync()
      const windowWidth = systemInfo.windowWidth
      const windowHeight = systemInfo.windowHeight

      setOffset({
        x: -(600 * scale) + windowWidth / 2,
        y: -(600 * scale) + windowHeight / 2,
      })
    }, 2500)
  }

  const closeAR = () => {
    setShowAR(false)
  }

  const openVoiceAssistant = () => {
    Taro.showToast({ title: 'AI 語音對話功能開發中...', icon: 'none' })
  }

  return (
    <PageShell className='indoor-page'>
      <View className='indoor-header'>
        <View className='indoor-header__title-box'>
          <Text className='indoor-header__title'>{title}</Text>
          <Text className='indoor-header__subtitle'>Lisboeta Macau</Text>
        </View>
        <ScrollView className='floor-selector' scrollX>
          <View className='floor-selector__inner'>
            {floors.map(floor => (
              <View 
                key={floor.id} 
                className={`floor-btn ${activeFloor === floor.id ? 'active' : ''}`}
                onClick={() => {
                  setActiveFloor(floor.id)
                  setOffset({ x: 0, y: 0 })
                  setTileLoadError(false)
                }}
              >
                <Text className='floor-btn__text'>{floor.label.split(' / ')[0]}</Text>
              </View>
            ))}
          </View>
        </ScrollView>
      </View>

      <View className='indoor-map-container'>
        {!detailMode && (
          <View className='indoor-map-hint'>
            <Text className='indoor-map-hint__title'>已用輕量模式打開室內地圖</Text>
            <Text className='indoor-map-hint__desc'>先顯示整層預覽，點擊下方按鈕後再載入細節瓦片。</Text>
            <Button className='indoor-map-hint__button' onClick={enableDetailMode}>載入細節地圖</Button>
          </View>
        )}
        {tileLoadError && (
          <View className='indoor-map-error'>
            <Text className='indoor-map-error__title'>室內地圖資源載入較慢</Text>
            <Text className='indoor-map-error__desc'>當前已切換為雲端資源，若網路不穩可稍後重試。</Text>
          </View>
        )}
        <View 
          className='indoor-map-viewport'
          onTouchStart={handleTouchStart}
          onTouchMove={handleTouchMove}
          onTouchEnd={handleTouchEnd}
        >
          <View 
            className='indoor-map-content'
            style={{
              width: `${currentFloorData?.size.w || 1000}px`,
              height: `${currentFloorData?.size.h || 1000}px`,
              transform: `translate(${offset.x}px, ${offset.y}px) scale(${scale})`,
              transformOrigin: '0 0'
            }}
          >
            {detailMode ? (
              currentFloorData?.tiles.map(tileFile => {
                const [col, row] = tileFile.split('.')[0].split('_').map(Number)
                return (
                  <Image 
                    key={tileFile}
                    className='indoor-map-tile'
                    src={cosAssetManifest.indoor.lisboeta.tileImage(activeFloor, tileFile)}
                    style={{
                      left: `${col * 256}px`,
                      top: `${row * 256}px`,
                      width: '256px',
                      height: '256px'
                    }}
                    mode='aspectFill'
                    lazyLoad
                    showMenuByLongpress={false}
                    onError={() => setTileLoadError(true)}
                  />
                )
              })
            ) : (
              <Image
                className='indoor-map-preview'
                src={`/assets/indoor/floors/${activeFloor}.png`}
                style={{
                  width: `${currentFloorData?.size.w || 1000}px`,
                  height: `${currentFloorData?.size.h || 1000}px`
                }}
                mode='aspectFit'
                lazyLoad
                showMenuByLongpress={false}
              />
            )}
            
            {arResult && arResult.floor === activeFloor && (
              <View 
                className='indoor-user-marker'
                style={{
                  left: `${arResult.x}px`,
                  top: `${arResult.y}px`,
                }}
              >
                <View 
                  className='indoor-user-marker__cone' 
                  style={{ transform: `translate(-50%, -100%) rotate(${heading}deg)`, transformOrigin: 'bottom center' }}
                />
                <View className='indoor-user-marker__dot' />
                <View className='indoor-user-marker__pulse' />
              </View>
            )}
          </View>
        </View>
        
        <View className='map-tools'>
          <View className='map-tools__group'>
            <Button className='map-tool-btn' onClick={zoomIn}>➕</Button>
            <Button className='map-tool-btn' onClick={zoomOut}>➖</Button>
          </View>
          
          <View className='map-tools__group'>
            <Button className='map-tool-btn primary' onClick={startARScan}>
              <Text className='map-tool-icon'>📷</Text>
              <Text className='map-tool-label'>AR定位</Text>
            </Button>
            <Button className='map-tool-btn' onClick={openVoiceAssistant}>
              <Text className='map-tool-icon'>🎙️</Text>
              <Text className='map-tool-label'>AI對話</Text>
            </Button>
          </View>
        </View>
      </View>

      {showAR && (
        <View className='ar-modal'>
          <Camera className='ar-modal__camera' devicePosition='back' flash='off' mode='normal' onInitDone={() => setCameraReady(true)} />
          <View className='ar-modal__mask' />
          <View className='ar-modal__content'>
            {isScanning ? (
              <View className='ar-scanning-view'>
                <View className='ar-scanner'>
                  <View className='ar-scanner__frame' />
                  <View className='ar-scanner__line' />
                </View>
                <Text className='ar-modal__title'>{cameraReady ? '正在識別環境特徵' : '正在啟動相機'}</Text>
                <Text className='ar-modal__desc'>{cameraReady ? '請將手機保持水平，並對準正前方的商舖或指示牌緩慢移動...' : '正在調起後置相機，請稍候...'}</Text>
                <Button className='ar-modal__close-btn' onClick={closeAR}>取消識別</Button>
              </View>
            ) : (
              <View className='ar-success-view'>
                <View className='ar-success-icon'>✅</View>
                <Text className='ar-modal__title'>定位成功</Text>
                <Text className='ar-modal__desc'>{arResult?.message}</Text>
                <Button className='ar-modal__primary-btn' onClick={closeAR}>回到地圖</Button>
              </View>
            )}
          </View>
        </View>
      )}
    </PageShell>
  )
}
