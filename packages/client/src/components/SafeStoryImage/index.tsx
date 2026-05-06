import { useEffect, useMemo, useState } from 'react'
import { Image, Text, View } from '@tarojs/components'
import './index.scss'

interface SafeStoryImageProps {
  src?: string
  className?: string
  imageClassName?: string
  mode?: 'aspectFill' | 'aspectFit' | 'widthFix'
  title?: string
  minHeight?: number
}

const svgDataUrlPrefix = 'data:image/svg+xml'

export default function SafeStoryImage({
  src,
  className = '',
  imageClassName = '',
  mode = 'aspectFill',
  title,
  minHeight = 220,
}: SafeStoryImageProps) {
  const [resolvedSrc, setResolvedSrc] = useState(src || '')
  const [failed, setFailed] = useState(false)
  const isInlineSvg = useMemo(() => !!src?.startsWith(svgDataUrlPrefix), [src])

  useEffect(() => {
    setFailed(false)
    if (!src) {
      setResolvedSrc('')
      return
    }
    setResolvedSrc(src)
  }, [src])

  const showImage = !!resolvedSrc && !failed

  return (
    <View className={`safe-story-image ${className}`} style={{ minHeight: `${minHeight}px` }}>
      {showImage && isInlineSvg ? (
        <View
          className={`safe-story-image__image safe-story-image__image--background ${imageClassName}`}
          style={{ backgroundImage: `url("${resolvedSrc}")` }}
        />
      ) : showImage ? (
        <Image
          className={`safe-story-image__image ${imageClassName}`}
          src={resolvedSrc}
          mode={mode}
          onError={() => setFailed(true)}
        />
      ) : (
        <View className='safe-story-image__fallback'>
          <Text className='safe-story-image__mark'>卷</Text>
          <Text className='safe-story-image__title'>{title || '故事素材準備中'}</Text>
          <Text className='safe-story-image__hint'>內容不會中斷，可先閱讀章節與前往現場。</Text>
        </View>
      )}
    </View>
  )
}
