import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, Text, Video, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import LottieAssetPlayer from '../LottieAssetPlayer'
import SafeStoryImage from '../SafeStoryImage'
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
  return <Text className='story-block__fallbackNotice'>已用較輕的方式呈現，旅程可以繼續。</Text>
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
  const reason = getStoryMediaFallbackReason(asset, label || '這段媒體可先以文字閱讀，前往現場時再試。')

  useEffect(() => {
    if (viewedRef.current) {
      return
    }
    viewedRef.current = true
    onViewed?.(reason)
  }, [onViewed, reason])

  return (
    <View className='story-block__missingMedia'>
      <Text className='story-block__missingMediaMark'>卷</Text>
      <Text className='story-block__missingMediaTitle'>這段故事先以文字展開</Text>
      <Text className='story-block__missingMediaHint'>
        {reason || '可先閱讀章節內容與前往現場，媒體不會阻止你繼續探索。'}
      </Text>
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
      <Text className='story-block__audioHint'>戴上耳機會更有沉浸感，也可以先閱讀文字劇情。</Text>
      <Button className='story-block__audioButton' onClick={handleToggle}>
        {playing ? '暫停播放' : '播放音訊'}
      </Button>
      <FallbackNotice asset={asset} />
    </View>
  )
}

function VideoAssetCard({
  block,
  asset,
  assetUrl,
  onCompleted,
}: {
  block: StoryContentBlockItem
  asset: StoryMediaAssetItem
  assetUrl: string
  onCompleted?: (block: StoryContentBlockItem, asset: StoryMediaAssetItem, mediaKind: 'audio' | 'video') => void
}) {
  const [activated, setActivated] = useState(false)

  if (!activated) {
    return (
      <View className='story-block__videoPoster'>
        {asset.posterUrl || asset.fallbackUrl ? (
          <SafeStoryImage
            className='story-block__videoPosterImageFrame'
            imageClassName='story-block__videoPosterImage'
            src={asset.posterUrl || asset.fallbackUrl}
            mode='aspectFill'
            title={asset.originalFilename}
            minHeight={260}
          />
        ) : (
          <View className='story-block__videoPosterStage'>
            <Text className='story-block__videoPosterMark'>影</Text>
            <Text className='story-block__videoPosterTitle'>劇情影片片段</Text>
            <Text className='story-block__videoPosterHint'>點擊後播放，讓故事畫面保持順暢。</Text>
          </View>
        )}
        <Button className='story-block__videoPosterButton' onClick={() => setActivated(true)}>
          播放影片
        </Button>
      </View>
    )
  }

  return (
    <View className='story-block__media'>
      <Video
        className='story-block__video'
        src={assetUrl}
        poster={asset.posterUrl || asset.fallbackUrl || ''}
        controls
        objectFit='contain'
        autoplay
        onEnded={() => onCompleted?.(block, asset, 'video')}
      />
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
        .filter((block) => {
          if (!['image', 'gallery', 'audio', 'video', 'lottie', 'attachment_list'].includes(block.blockType || '')) {
            return true
          }
          const asset = pickBlockAsset(block)
          if (resolveStoryMediaUrl(asset)) {
            return true
          }
          return !!block.title || !!block.summary || !!block.body
        })
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
                <SafeStoryImage
                  className='story-block__mediaImageFrame'
                  imageClassName='story-block__mediaImage'
                  src={assetUrl}
                  mode='widthFix'
                  title={block.title || asset?.originalFilename}
                  minHeight={240}
                />
                <FallbackNotice asset={asset} />
              </View>
            ) : null}

            {block.blockType === 'image' && !assetUrl ? (
              <MissingMedia asset={asset} label='圖片暫時未能顯示，可先閱讀劇情。' onViewed={notifyUnavailable(asset, '圖片暫時未能顯示，可先閱讀劇情。')} />
            ) : null}

            {block.blockType === 'gallery' ? (
              [asset, ...attachmentAssets].some((item) => isStoryMediaPlayable(item))
                ? (
                    <View className='story-block__gallery'>
                      {[asset, ...attachmentAssets]
                        .filter((item): item is StoryMediaAssetItem => !!item && isStoryMediaPlayable(item))
                        .map((galleryAsset) => (
                          <SafeStoryImage
                            className='story-block__galleryImageFrame'
                            imageClassName='story-block__galleryImage'
                            key={galleryAsset.id}
                            src={resolveStoryMediaUrl(galleryAsset)}
                            mode='widthFix'
                            title={galleryAsset.originalFilename}
                            minHeight={180}
                          />
                        ))}
                      {[asset, ...attachmentAssets].some((item) => item?.availability === 'fallback' || item?.fallbackUsed)
                        ? <Text className='story-block__fallbackNotice'>部分圖片已用較輕的方式呈現。</Text>
                        : null}
                    </View>
                  )
                : <MissingMedia asset={asset} label='圖集暫時未能顯示，可先閱讀劇情。' onViewed={notifyUnavailable(asset, '圖集暫時未能顯示，可先閱讀劇情。')} />
            ) : null}

            {block.blockType === 'audio' ? (
              isStoryMediaPlayable(asset)
                ? <AudioAssetCard block={block} asset={asset} title={block.title} onCompleted={onMediaCompleted} />
                : <MissingMedia asset={asset} label='音訊暫時未能播放，可先閱讀劇情。' onViewed={notifyUnavailable(asset, '音訊暫時未能播放，可先閱讀劇情。')} />
            ) : null}

            {block.blockType === 'video' && asset && assetUrl ? (
              <VideoAssetCard block={block} asset={asset} assetUrl={assetUrl} onCompleted={onMediaCompleted} />
            ) : null}

            {block.blockType === 'video' && !assetUrl ? (
              <MissingMedia asset={asset} label='影片暫時未能播放，可先閱讀劇情。' onViewed={notifyUnavailable(asset, '影片暫時未能播放，可先閱讀劇情。')} />
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
              ) : <MissingMedia asset={asset} label='動畫暫時未能播放，可先閱讀劇情。' onViewed={notifyUnavailable(asset, '動畫暫時未能播放，可先閱讀劇情。')} />
            ) : null}

            {block.blockType === 'attachment_list' && attachmentAssets.length ? (
              <AttachmentList assets={attachmentAssets} />
            ) : null}

            {block.blockType === 'attachment_list' && !attachmentAssets.length ? (
              <MissingMedia asset={asset} label='附件內容暫時未能顯示。' onViewed={notifyUnavailable(asset, '附件內容暫時未能顯示。')} />
            ) : null}

            {!['image', 'gallery', 'audio', 'video', 'lottie', 'attachment_list', 'quote'].includes(block.blockType || '') && assetUrl ? (
              <View className='story-block__media'>
                <SafeStoryImage
                  className='story-block__mediaImageFrame'
                  imageClassName='story-block__mediaImage'
                  src={assetUrl}
                  mode='widthFix'
                  title={block.title || asset?.originalFilename}
                  minHeight={240}
                />
                <Text className='story-block__mediaCaption'>{block.summary || asset?.originalFilename || '故事媒體'}</Text>
                <FallbackNotice asset={asset} />
              </View>
            ) : null}
          </View>
        )
      })}
    </View>
  )
}
