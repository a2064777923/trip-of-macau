import { useEffect, useMemo, useRef, useState } from 'react'
import { Canvas, Image, Text, View } from '@tarojs/components'
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
}

type PlayerStatus = 'idle' | 'loading' | 'ready' | 'error'

function parseAnimationData(data: unknown) {
  if (typeof data === 'string') {
    return JSON.parse(data) as Record<string, unknown>
  }
  if (data && typeof data === 'object') {
    return data as Record<string, unknown>
  }
  throw new Error('Invalid lottie payload')
}

export default function LottieAssetPlayer({
  asset,
  className,
  height,
  autoplay,
  loop,
  onReady,
  onUnavailable,
}: LottieAssetPlayerProps) {
  const [status, setStatus] = useState<PlayerStatus>('idle')
  const readyNotifiedRef = useRef('')
  const unavailableNotifiedRef = useRef('')
  const canvasId = useMemo(() => `story-lottie-${Math.random().toString(36).slice(2, 10)}`, [])
  const systemInfo = useMemo(() => Taro.getSystemInfoSync(), [])
  const resolvedHeight = useMemo(() => {
    if (height) {
      return height
    }
    if (asset?.widthPx && asset?.heightPx) {
      const width = Math.max(220, systemInfo.windowWidth - 48)
      return Math.max(180, Math.round((width * asset.heightPx) / asset.widthPx))
    }
    return 240
  }, [asset?.heightPx, asset?.widthPx, height, systemInfo.windowWidth])

  const fallbackUrl = asset?.posterUrl || asset?.fallbackUrl

  const notifyReady = (target: StoryMediaAssetItem) => {
    const key = `${target.id}:ready`
    if (readyNotifiedRef.current === key) {
      return
    }
    readyNotifiedRef.current = key
    onReady?.(target)
  }

  const notifyUnavailable = (target: StoryMediaAssetItem, reason: string) => {
    const key = `${target.id}:${reason}`
    if (unavailableNotifiedRef.current === key) {
      return
    }
    unavailableNotifiedRef.current = key
    onUnavailable?.(target, reason)
  }

  useEffect(() => {
    if (!asset) {
      setStatus('idle')
      return
    }

    if (!asset.url) {
      setStatus('error')
      notifyUnavailable(asset, 'Lottie 動畫資源未配置。')
      return
    }

    let disposed = false
    let animation: { destroy?: () => void } | null = null

    const loadAnimation = async () => {
      setStatus('loading')
      try {
        const response = await Taro.request({
          url: asset.url!,
          method: 'GET',
        })
        const animationData = parseAnimationData(response.data)

        await new Promise<void>((resolve) => {
          Taro.nextTick(resolve)
        })

        const dpr = systemInfo.pixelRatio || 1
        await new Promise<void>((resolve, reject) => {
          Taro.createSelectorQuery()
            .select(`#${canvasId}`)
            .node((result) => {
              try {
                const canvas = result?.node
                if (!canvas) {
                  throw new Error('Canvas node not found')
                }
                const context = canvas.getContext('2d')
                if (!context) {
                  throw new Error('Canvas context not found')
                }
                const width = Math.max(220, systemInfo.windowWidth - 48)
                canvas.width = width * dpr
                canvas.height = resolvedHeight * dpr
                context.scale(dpr, dpr)
                lottie.setup(canvas)
                animation = lottie.loadAnimation({
                  loop: loop ?? asset.defaultLoop ?? true,
                  autoplay: autoplay ?? asset.defaultAutoplay ?? true,
                  animationData,
                  rendererSettings: { context },
                })
                resolve()
              } catch (error) {
                reject(error)
              }
            })
            .exec()
        })

        if (!disposed) {
          setStatus('ready')
          notifyReady(asset)
        }
      } catch (error) {
        console.warn('Failed to initialize lottie asset.')
        if (!disposed) {
          setStatus('error')
          notifyUnavailable(asset, '動畫暫時無法播放')
        }
      }
    }

    void loadAnimation()

    return () => {
      disposed = true
      animation?.destroy?.()
    }
  }, [asset, autoplay, canvasId, loop, resolvedHeight, systemInfo.pixelRatio, systemInfo.windowWidth])

  if (!asset) {
    return null
  }

  return (
    <View className={className}>
      {status === 'error' && fallbackUrl ? (
        <Image
          src={fallbackUrl}
          mode='aspectFit'
          style={{ width: '100%', height: `${resolvedHeight}px`, borderRadius: '20px', background: '#f4f1ea' }}
        />
      ) : (
        <Canvas
          id={canvasId}
          type='2d'
          style={{
            width: '100%',
            height: `${resolvedHeight}px`,
            display: 'block',
            borderRadius: '20px',
            background: '#f8f5ef',
          }}
        />
      )}
      {status === 'loading' ? (
        <Text style={{ display: 'block', marginTop: '12px', color: '#7d6c61', fontSize: '24px' }}>
          動畫載入中...
        </Text>
      ) : null}
      {status === 'error' && !fallbackUrl ? (
        <Text style={{ display: 'block', marginTop: '12px', color: '#9e6d6d', fontSize: '24px' }}>
          動畫暫時無法播放
        </Text>
      ) : null}
      {status === 'error' && fallbackUrl ? (
        <Text style={{ display: 'block', marginTop: '12px', color: '#8b6e54', fontSize: '24px' }}>
          動畫無法播放，已顯示備用圖片
        </Text>
      ) : null}
    </View>
  )
}
