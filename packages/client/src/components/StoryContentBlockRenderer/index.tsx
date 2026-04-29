import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, Image, Text, Video, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import LottieAssetPlayer from '../LottieAssetPlayer'
import type { StoryContentBlockItem, StoryMediaAssetItem } from '../../types/game'
import './index.scss'

function pickBlockAsset(block: StoryContentBlockItem) {
  return block.primaryAsset || block.attachmentAssets?.[0]
}

function pickAssetUrl(asset?: StoryMediaAssetItem | null) {
  return asset?.url || asset?.fallbackUrl || asset?.posterUrl || ''
}

function AudioAssetCard({
  asset,
  title,
}: {
  asset?: StoryMediaAssetItem | null
  title?: string
}) {
  const audioRef = useRef<Taro.InnerAudioContext | null>(null)
  const [playing, setPlaying] = useState(false)

  useEffect(() => {
    return () => {
      audioRef.current?.destroy()
      audioRef.current = null
    }
  }, [])

  const handleToggle = () => {
    if (!asset?.url) {
      return
    }

    if (!audioRef.current) {
      const context = Taro.createInnerAudioContext()
      context.src = asset.url
      context.onPlay(() => setPlaying(true))
      context.onPause(() => setPlaying(false))
      context.onStop(() => setPlaying(false))
      context.onEnded(() => setPlaying(false))
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

export default function StoryContentBlockRenderer({ blocks }: { blocks?: StoryContentBlockItem[] }) {
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
        const assetUrl = pickAssetUrl(asset)
        const attachmentAssets = block.attachmentAssets || []

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
              </View>
            ) : null}

            {block.blockType === 'gallery' ? (
              <View className='story-block__gallery'>
                {[asset, ...attachmentAssets]
                  .filter((item): item is StoryMediaAssetItem => !!item)
                  .map((galleryAsset) => (
                    <Image
                      className='story-block__galleryImage'
                      key={galleryAsset.id}
                      src={pickAssetUrl(galleryAsset)}
                      mode='widthFix'
                    />
                  ))}
              </View>
            ) : null}

            {block.blockType === 'audio' ? <AudioAssetCard asset={asset} title={block.title} /> : null}

            {block.blockType === 'video' && assetUrl ? (
              <Video
                className='story-block__video'
                src={assetUrl}
                controls
                objectFit='contain'
                autoplay={false}
              />
            ) : null}

            {block.blockType === 'lottie' ? (
              <View className='story-block__media'>
                <LottieAssetPlayer asset={asset} />
              </View>
            ) : null}

            {block.blockType === 'attachment_list' && attachmentAssets.length ? (
              <AttachmentList assets={attachmentAssets} />
            ) : null}

            {!['image', 'gallery', 'audio', 'video', 'lottie', 'attachment_list', 'quote'].includes(block.blockType || '') && assetUrl ? (
              <View className='story-block__media'>
                <Image className='story-block__mediaImage' src={assetUrl} mode='widthFix' />
                <Text className='story-block__mediaCaption'>{asset?.originalFilename || '已掛載媒體資產'}</Text>
              </View>
            ) : null}
          </View>
        )
      })}
    </View>
  )
}
