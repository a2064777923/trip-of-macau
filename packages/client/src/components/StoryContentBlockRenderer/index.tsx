import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, Image, Text, Video, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import LottieAssetPlayer from '../LottieAssetPlayer'
import type { StoryContentBlockItem, StoryMediaAssetItem } from '../../types/game'
import {
  getStoryMediaFallbackReason,
  isStoryMediaPlayable,
  resolveStoryMediaUrl,
} from '../../services/gameService'
import './index.scss'

interface StoryContentBlockRendererProps {
  blocks?: StoryContentBlockItem[]
  onMediaCompleted?: (block: StoryContentBlockItem, asset: StoryMediaAssetItem, mediaKind: 'audio' | 'video') => void
  onUnavailableMediaViewed?: (block: StoryContentBlockItem, asset: StoryMediaAssetItem | null | undefined, reason: string) => void
}

function pickBlockAsset(block: StoryContentBlockItem) {
  return block.primaryAsset || block.attachmentAssets?.[0]
}

function FallbackNotice({ asset }: { asset?: StoryMediaAssetItem | null }) {
  if (asset?.availability !== 'fallback' && asset?.fallbackUsed !== true) {
    return null
  }
  const kind = asset.assetKind || asset.runtimeKind || '媒體'
  return <Text className='story-block__fallbackNotice'>已使用備用媒體播放：{kind}</Text>
}

function MissingMedia({
  asset,
  label,
  onViewed,
}: {
  asset?: StoryMediaAssetItem | null
  label?: string
  onViewed?: (reason: string) => void
}) {
  const viewedRef = useRef(false)
  const reason = getStoryMediaFallbackReason(asset, label || '此媒體暫時未能播放，請稍後再試。')

  useEffect(() => {
    if (viewedRef.current) {
      return
    }
    viewedRef.current = true
    onViewed?.(reason)
  }, [onViewed, reason])

  return (
    <View className='story-block__missingMedia'>
      <Text className='story-block__missingMediaTitle'>媒體資源暫時未能載入</Text>
      <Text className='story-block__missingMediaHint'>{reason}</Text>
      {asset?.id ? (
        <Text className='story-block__missingMediaId'>資源編號：{asset.id}</Text>
      ) : null}
    </View>
  )
}

function AudioAssetCard({
  block,
  asset,
  title,
  onCompleted,
}: {
  block: StoryContentBlockItem
  asset?: StoryMediaAssetItem | null
  title?: string
  onCompleted?: (block: StoryContentBlockItem, asset: StoryMediaAssetItem, mediaKind: 'audio' | 'video') => void
}) {
  const audioRef = useRef<Taro.InnerAudioContext | null>(null)
  const [playing, setPlaying] = useState(false)
  const assetUrl = resolveStoryMediaUrl(asset)

  useEffect(() => {
    return () => {
      audioRef.current?.destroy()
      audioRef.current = null
    }
  }, [])

  const handleToggle = () => {
    if (!asset || !assetUrl) {
      return
    }

    if (!audioRef.current) {
      const context = Taro.createInnerAudioContext()
      context.src = assetUrl
      context.onPlay(() => setPlaying(true))
      context.onPause(() => setPlaying(false))
      context.onStop(() => setPlaying(false))
      context.onEnded(() => {
        setPlaying(false)
        onCompleted?.(block, asset, 'audio')
      })
      audioRef.current = context
    }

    if (playing) {
      audioRef.current.pause()
      return
    }

    audioRef.current.play()
  }

  return (
    <View className='story-block__audioCard'>
      <Text className='story-block__audioTitle'>{title || asset?.originalFilename || '語音片段'}</Text>
      <Text className='story-block__audioHint'>可播放景點旁白、章節音效或故事導覽。</Text>
      <Button className='story-block__audioButton' onClick={handleToggle}>
        {playing ? '暫停播放' : '播放音訊'}
      </Button>
      <FallbackNotice asset={asset} />
    </View>
  )
}

function AttachmentList({ assets }: { assets: StoryMediaAssetItem[] }) {
  return (
    <View className='story-block__attachmentList'>
      {assets.map((asset) => (
        <View className='story-block__attachmentItem' key={asset.id}>
          <Text className='story-block__attachmentText'>
            {asset.originalFilename || asset.url || `附件 #${asset.id}`}
          </Text>
          {asset.url ? (
            <Text
              className='story-block__attachmentLink'
              onClick={() => {
                void Taro.setClipboardData({ data: asset.url || '' })
              }}
            >
              複製連結
            </Text>
          ) : null}
        </View>
      ))}
    </View>
  )
}

export default function StoryContentBlockRenderer({
  blocks,
  onMediaCompleted,
  onUnavailableMediaViewed,
}: StoryContentBlockRendererProps) {
  const normalizedBlocks = useMemo(
    () =>
      (blocks || [])
        .slice()
        .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0)),
    [blocks],
  )

  if (!normalizedBlocks.length) {
    return null
  }

  return (
    <View className='story-block-list'>
      {normalizedBlocks.map((block) => {
        const asset = pickBlockAsset(block)
        const assetUrl = resolveStoryMediaUrl(asset)
        const attachmentAssets = block.attachmentAssets || []
        const notifyUnavailable = (targetAsset: StoryMediaAssetItem | null | undefined, label: string) => (reason: string) => {
          onUnavailableMediaViewed?.(block, targetAsset, reason || label)
        }

        if (block.blockType === 'quote') {
          return (
            <View key={block.id} className='story-block story-block--quote'>
              <Text className='story-block__quoteMark'>“</Text>
              {block.title ? <Text className='story-block__title'>{block.title}</Text> : null}
              {block.body ? <Text className='story-block__body'>{block.body}</Text> : null}
            </View>
          )
        }

        return (
          <View key={block.id} className='story-block'>
            {block.title ? <Text className='story-block__title'>{block.title}</Text> : null}
            {block.summary ? <Text className='story-block__summary'>{block.summary}</Text> : null}
            {block.body ? <Text className='story-block__body'>{block.body}</Text> : null}

            {block.blockType === 'image' && assetUrl ? (
              <View className='story-block__media'>
                <Image className='story-block__mediaImage' src={assetUrl} mode='widthFix' />
                <FallbackNotice asset={asset} />
              </View>
            ) : null}

            {block.blockType === 'image' && !assetUrl ? (
              <MissingMedia asset={asset} label='圖片資源未配置可用連結。' onViewed={notifyUnavailable(asset, '圖片資源未配置可用連結。')} />
            ) : null}

            {block.blockType === 'gallery' ? (
              [asset, ...attachmentAssets].some((item) => isStoryMediaPlayable(item))
                ? (
                    <View className='story-block__gallery'>
                      {[asset, ...attachmentAssets]
                        .filter((item): item is StoryMediaAssetItem => !!item && isStoryMediaPlayable(item))
                        .map((galleryAsset) => (
                          <Image
                            className='story-block__galleryImage'
                            key={galleryAsset.id}
                            src={resolveStoryMediaUrl(galleryAsset)}
                            mode='widthFix'
                          />
                        ))}
                      {[asset, ...attachmentAssets].some((item) => item?.availability === 'fallback' || item?.fallbackUsed)
                        ? <Text className='story-block__fallbackNotice'>已使用備用媒體播放：圖片</Text>
                        : null}
                    </View>
                  )
                : <MissingMedia asset={asset} label='圖片資源未配置可用連結。' onViewed={notifyUnavailable(asset, '圖片資源未配置可用連結。')} />
            ) : null}

            {block.blockType === 'audio' ? (
              isStoryMediaPlayable(asset)
                ? <AudioAssetCard block={block} asset={asset} title={block.title} onCompleted={onMediaCompleted} />
                : <MissingMedia asset={asset} label='音訊資源未配置可用連結。' onViewed={notifyUnavailable(asset, '音訊資源未配置可用連結。')} />
            ) : null}

            {block.blockType === 'video' && asset && assetUrl ? (
              <View className='story-block__media'>
                <Video
                  className='story-block__video'
                  src={assetUrl}
                  poster={asset?.posterUrl || asset?.fallbackUrl || ''}
                  controls
                  objectFit='contain'
                  autoplay={false}
                  onEnded={() => onMediaCompleted?.(block, asset, 'video')}
                />
                <FallbackNotice asset={asset} />
              </View>
            ) : null}

            {block.blockType === 'video' && !assetUrl ? (
              <MissingMedia asset={asset} label='影片資源未配置可用連結。' onViewed={notifyUnavailable(asset, '影片資源未配置可用連結。')} />
            ) : null}

            {block.blockType === 'lottie' ? (
              asset && isStoryMediaPlayable(asset) ? (
                <View className='story-block__media'>
                  <LottieAssetPlayer
                    asset={asset}
                    onUnavailable={(_, reason) => onUnavailableMediaViewed?.(block, asset, reason)}
                  />
                  <FallbackNotice asset={asset} />
                </View>
              ) : <MissingMedia asset={asset} label='Lottie 動畫資源未配置。' onViewed={notifyUnavailable(asset, 'Lottie 動畫資源未配置。')} />
            ) : null}

            {block.blockType === 'attachment_list' && attachmentAssets.length ? (
              <AttachmentList assets={attachmentAssets} />
            ) : null}

            {block.blockType === 'attachment_list' && !attachmentAssets.length ? (
              <MissingMedia asset={asset} label='附件列表暫無可顯示資源。' onViewed={notifyUnavailable(asset, '附件列表暫無可顯示資源。')} />
            ) : null}

            {!['image', 'gallery', 'audio', 'video', 'lottie', 'attachment_list', 'quote'].includes(block.blockType || '') && assetUrl ? (
              <View className='story-block__media'>
                <Image className='story-block__mediaImage' src={assetUrl} mode='widthFix' />
                <Text className='story-block__mediaCaption'>{asset?.originalFilename || '已掛載媒體資產'}</Text>
                <FallbackNotice asset={asset} />
              </View>
            ) : null}
          </View>
        )
      })}
    </View>
  )
}
