import { useMemo } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../../components/PageShell'
import { getTipArticleById } from '../../../services/gameService'
import './detail.scss'

export default function TipDetailPage() {
  const router = Taro.getCurrentInstance().router
  const articleId = Number(router?.params?.id || 0)
  const article = useMemo(() => getTipArticleById(articleId), [articleId])

  if (!article) {
    return (
      <PageShell className='tip-detail-page'>
        <View className='tip-detail-empty'>
          <Text className='tip-detail-empty__emoji'>📝</Text>
          <Text className='tip-detail-empty__title'>這篇秘籍暫時找不到了</Text>
          <Text className='tip-detail-empty__desc'>先回到秘籍列表看看其他靈感吧。</Text>
        </View>
      </PageShell>
    )
  }

  return (
    <PageShell className='tip-detail-page'>
      <View className='tip-detail-hero' style={{ background: article.coverColor }}>
        <Text className='tip-detail-hero__category'>{article.category}</Text>
        <Text className='tip-detail-hero__title'>{article.title}</Text>
        <Text className='tip-detail-hero__meta'>{article.locationName || article.author} · {article.readMinutes} 分鐘</Text>
      </View>

      <ScrollView className='tip-detail-scroll' scrollY>
        <View className='tip-detail-card'>
          <Text className='tip-detail-card__summary'>{article.summary}</Text>
          <View className='tip-detail-card__stats'>
            <Text className='tip-detail-card__stat'>👍 {article.likes}</Text>
            <Text className='tip-detail-card__stat'>⭐ {article.saves}</Text>
            <Text className='tip-detail-card__stat'>{article.author}</Text>
          </View>
          <View className='tip-detail-card__content'>
            {(article.contentParagraphs || []).map((paragraph) => (
              <Text key={paragraph} className='tip-detail-card__paragraph'>{paragraph}</Text>
            ))}
          </View>
          <View className='tip-detail-card__tags'>
            {(article.tags || []).map((tag) => (
              <Text key={tag} className='tip-detail-card__tag'>#{tag}</Text>
            ))}
          </View>
        </View>
      </ScrollView>
    </PageShell>
  )
}
