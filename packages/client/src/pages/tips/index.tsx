import { useEffect, useMemo, useState } from 'react'
import { Input, ScrollView, Text, View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import PageShell from '../../components/PageShell'
import { getTipArticles, getUnreadNotificationCount, refreshPublicContent } from '../../services/gameService'
import './index.scss'

const categories = ['全部', '新手攻略', '慢遊推薦', '拍照秘籍']

export default function TipsPage() {
  const [keyword, setKeyword] = useState('')
  const [activeCategory, setActiveCategory] = useState('全部')
  const [articles, setArticles] = useState(() => getTipArticles())
  const [unreadCount, setUnreadCount] = useState(() => getUnreadNotificationCount())

  const refreshPageState = async () => {
    try {
      await refreshPublicContent()
    } catch (error) {
      console.warn('Failed to refresh tips content.', error)
    }
    setArticles(getTipArticles())
    setUnreadCount(getUnreadNotificationCount())
  }

  useEffect(() => {
    void refreshPageState()
  }, [])

  useDidShow(() => {
    void refreshPageState()
  })

  const filtered = useMemo(() => {
    return articles.filter((article) => {
      const matchKeyword = !keyword || article.title.includes(keyword) || article.summary.includes(keyword) || (article.tags || []).some((tag) => tag.includes(keyword))
      const matchCategory = activeCategory === '全部' || article.category === activeCategory
      return matchKeyword && matchCategory
    })
  }, [articles, keyword, activeCategory])

  return (
    <PageShell className='tips-page'>
      <View className='tips-hero'>
        <View className='tips-hero__top'>
          <View className='tips-hero__copy'>
            <Text className='tips-hero__eyebrow'>旅途靈感站</Text>
            <Text className='tips-hero__title'>旅人秘籍</Text>
            <Text className='tips-hero__subtitle'>收藏大家的路線靈感、拍照祕訣與慢遊心法，找到最適合你的澳門玩法。</Text>
          </View>
          <View className='tips-actions'>
            <View className='tips-actions__iconBtn' onClick={() => Taro.navigateTo({ url: '/pages/tips/notifications/index' })}>
              <Text className='tips-actions__icon'>🔔</Text>
              <Text className='tips-actions__label'>通知</Text>
              {!!unreadCount && <Text className='tips-actions__badge'>{unreadCount}</Text>}
            </View>
            <View className='tips-actions__iconBtn tips-actions__iconBtn--primary' onClick={() => Taro.navigateTo({ url: '/pages/tips/publish/index' })}>
              <Text className='tips-actions__icon'>✍️</Text>
              <Text className='tips-actions__label'>發佈</Text>
            </View>
          </View>
        </View>

        <View className='tips-hero__panel'>
          <View className='tips-hero__panelMain'>
            <Text className='tips-hero__panelTitle'>今天想找哪一種旅行靈感？</Text>
            <Text className='tips-hero__panelDesc'>通知和發佈都獨立成頁了，入口更清楚，瀏覽秘籍時也不會再被大面板打斷節奏。</Text>
          </View>
          <View className='tips-hero__panelStats'>
            <View className='tips-hero__statCard'>
              <Text className='tips-hero__statNumber'>{articles.length}</Text>
              <Text className='tips-hero__statLabel'>篇旅人分享</Text>
            </View>
            <View className='tips-hero__statCard'>
              <Text className='tips-hero__statNumber'>{unreadCount}</Text>
              <Text className='tips-hero__statLabel'>則待看通知</Text>
            </View>
          </View>
        </View>
      </View>

      <View className='tips-search'>
        <Input className='tips-search__input' value={keyword} placeholder='搜尋路線、拍照地點、慢遊靈感' onInput={(event) => setKeyword(event.detail.value || '')} />
      </View>

      <ScrollView className='tips-categories' scrollX>
        {categories.map((category) => (
          <View key={category} className={`tips-categories__item ${activeCategory === category ? 'active' : ''}`} onClick={() => setActiveCategory(category)}>
            <Text className='tips-categories__text'>{category}</Text>
          </View>
        ))}
      </ScrollView>

      <View className='tips-waterfall'>
        {filtered.map((article) => (
          <View key={article.id} className='tip-card' onClick={() => Taro.navigateTo({ url: `/pages/tips/detail/index?id=${article.id}` })}>
            <View className='tip-card__cover' style={{ background: article.coverColor }}>
              <Text className='tip-card__category'>{article.category}</Text>
              <Text className='tip-card__coverText'>{article.locationName || article.author}</Text>
            </View>
            <View className='tip-card__body'>
              <Text className='tip-card__title'>{article.title}</Text>
              <Text className='tip-card__summary'>{article.summary}</Text>
              {!!article.contentParagraphs?.length ? (
                <View className='tip-card__content'>
                  {article.contentParagraphs.slice(0, 2).map((paragraph) => (
                    <Text key={paragraph} className='tip-card__paragraph'>{paragraph}</Text>
                  ))}
                </View>
              ) : (
                <Text className='tip-card__empty'>這篇靈感先收下來了，等你到現場再把故事補得更完整。</Text>
              )}

              <View className='tip-card__tags'>
                {(article.tags || []).map((tag) => (
                  <Text key={tag} className='tip-card__tag'>#{tag}</Text>
                ))}
              </View>
              <View className='tip-card__meta'>
                <Text className='tip-card__metaText'>👍 {article.likes}</Text>
                <Text className='tip-card__metaText'>⭐ {article.saves}</Text>
                <Text className='tip-card__metaText'>{article.readMinutes} 分鐘</Text>
              </View>
            </View>
          </View>
        ))}
      </View>
    </PageShell>
  )
}

