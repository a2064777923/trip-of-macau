import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, Canvas, Image, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import lottie from 'lottie-miniprogram'
import type { StoryMediaAssetItem } from '../../types/game'

interface LottieAssetPlayerProps {
  asset?: StoryMediaAssetItem | null
  className?: string
  height?: number
  autoplay?: boolean
  loop?: boolean
  onReady?: (asset: StoryMediaAssetItem) => void
  onUnavailable?: (asset: StoryMediaAssetItem, reason: string) => void
  lazy?: boolean
}

export default function LottieAssetPlayer({
  asset,
  className,
  height,
  autoplay,
  loop,
  onReady,
  onUnavailable,
  lazy = true,
}: LottieAssetPlayerProps) {
  const [activated, setActivated] = useState(() => !lazy)
  const [status, setStatus] = useState<'idle' | 'loading' | 'ready' | 'unavailable'>('idle')
  const notifiedRef = useRef('')
  const animationRef = useRef<{ destroy?: () => void } | null>(null)
  const canvasIdRef = useRef(`story-lottie-${Math.random().toString(36).slice(2, 10)}`)
  const resolvedHeight = height || 240
  const fallbackUrl = asset?.posterUrl || asset?.fallbackUrl
  const isPlayablePath = !!asset?.url && /^https?:\/\//i.test(asset.url)
  const canvasId = canvasIdRef.current

  const stageTitle = useMemo(() => {
    if (asset?.originalFilename) {
      return asset.originalFilename.replace(/\.[^.]+$/, '')
    }
    return '動畫劇情'
  }, [asset?.originalFilename])

  useEffect(() => {
    animationRef.current?.destroy?.()
    animationRef.current = null
    setActivated(!lazy)
    setStatus('idle')
    notifiedRef.current = ''
  }, [asset?.id, lazy])

  useEffect(() => {
    if (!asset || !activated) {
      return
    }

    let disposed = false

    const notifyUnavailable = (reason: string) => {
      const key = `${asset.id}:${reason}`
      if (notifiedRef.current !== key) {
        notifiedRef.current = key
        onUnavailable?.(asset, reason)
      }
    }

    const loadAnimation = async () => {
      if (!isPlayablePath || !asset.url) {
        setStatus('unavailable')
        notifyUnavailable(fallbackUrl ? '已顯示劇情畫面' : '動畫暫時以劇情舞台展示')
        return
      }

      setStatus('loading')
      await new Promise<void>((resolve) => Taro.nextTick(resolve))

      Taro.createSelectorQuery()
        .select(`#${canvasId}`)
        .node((result) => {
          if (disposed) {
            return
          }
          const canvas = result?.node
          const context = canvas?.getContext?.('2d')
          if (!canvas || !context) {
            setStatus('unavailable')
            notifyUnavailable(fallbackUrl ? '已顯示劇情畫面' : '動畫暫時以劇情舞台展示')
            return
          }

          const windowInfo = Taro.getWindowInfo ? Taro.getWindowInfo() : Taro.getSystemInfoSync()
          const pixelRatio = Math.max(1, windowInfo.pixelRatio || 1)
          const width = Math.max(240, (windowInfo.windowWidth || 375) - 48)
          canvas.width = width * pixelRatio
          canvas.height = resolvedHeight * pixelRatio
          context.scale(pixelRatio, pixelRatio)

          try {
            lottie.setup(canvas)
            animationRef.current?.destroy?.()
            animationRef.current = lottie.loadAnimation({
              loop: loop ?? asset.defaultLoop ?? true,
              autoplay: autoplay ?? asset.defaultAutoplay ?? true,
              path: asset.url,
              rendererSettings: { context },
            })
            setStatus('ready')
            onReady?.(asset)
          } catch (error) {
            console.warn('Failed to initialize story animation.', error)
            setStatus('unavailable')
            notifyUnavailable(fallbackUrl ? '已顯示劇情畫面' : '動畫暫時以劇情舞台展示')
          }
        })
        .exec()
    }

    void loadAnimation()

    return () => {
      disposed = true
      animationRef.current?.destroy?.()
      animationRef.current = null
    }
  }, [activated, asset, autoplay, canvasId, fallbackUrl, isPlayablePath, loop, onReady, onUnavailable, resolvedHeight])

  if (!asset) {
    return null
  }

  const showCanvas = activated && isPlayablePath
  const showPlaceholder = !showCanvas || status !== 'ready'

  return (
    <View className={className}>
      {showPlaceholder && fallbackUrl && activated ? (
        <Image
          src={fallbackUrl}
          mode='aspectFit'
          style={{ width: '100%', height: `${resolvedHeight}px`, borderRadius: '20px', background: '#f4f1ea' }}
        />
      ) : showPlaceholder ? (
        <View
          style={{
            width: '100%',
            height: `${resolvedHeight}px`,
            borderRadius: '20px',
            background: 'linear-gradient(135deg, #303a54 0%, #c67950 58%, #f3c16f 100%)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '24px',
            boxSizing: 'border-box',
            overflow: 'hidden',
          }}
        >
          <Text style={{ color: '#fffaf2', fontSize: '28px', fontWeight: 800, textAlign: 'center' }}>
            {activated ? stageTitle : '動畫劇情已準備'}
          </Text>
          <Text style={{ marginTop: '10px', color: 'rgba(255,250,242,0.86)', fontSize: '22px', lineHeight: 1.6, textAlign: 'center' }}>
            {activated
              ? '先以故事舞台呈現，正式動畫會在現場體驗中接上。'
              : '點擊後展開劇情舞台，避免小程序一次載入過多動畫。'}
          </Text>
          {!activated ? (
            <Button
              style={{
                marginTop: '18px',
                minWidth: '180px',
                height: '58px',
                borderRadius: '999px',
                background: 'rgba(255,255,255,0.9)',
                color: '#9c5d38',
                fontSize: '22px',
                fontWeight: 700,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
              onClick={() => setActivated(true)}
            >
              展開劇情
            </Button>
          ) : null}
        </View>
      ) : null}
      {showCanvas ? (
        <Canvas
          id={canvasId}
          type='2d'
          style={{
            width: '100%',
            height: `${resolvedHeight}px`,
            borderRadius: '20px',
            display: status === 'ready' ? 'block' : 'none',
            background: '#f4f1ea',
          }}
        />
      ) : null}
      <Text style={{ display: 'block', marginTop: '12px', color: '#8b6e54', fontSize: '24px' }}>
        {status === 'ready'
          ? '動畫播放中'
          : status === 'loading'
            ? '動畫載入中'
            : fallbackUrl && activated
              ? '已顯示劇情畫面'
              : isPlayablePath
                ? '點擊展開後播放動畫'
                : '可先閱讀劇情，動畫不會阻止你繼續探索'}
      </Text>
    </View>
  )
}
