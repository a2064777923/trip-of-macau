import { useMemo, useState } from 'react'
import { Input, ScrollView, Text, View } from '@tarojs/components'
import { getTipArticles } from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

const categories = ['全部', '新手攻略', '慢遊推薦', '拍照秘籍']

export default function TipsPage() {
  const [keyword, setKeyword] = useState('')
  const [activeCategory, setActiveCategory] = useState('全部')
  const articles = useMemo(() => getTipArticles(), [])

  const filtered = useMemo(() => {
    return articles.filter((article) => {
      const matchKeyword = !keyword || article.title.includes(keyword) || article.summary.includes(keyword) || article.tags.some((tag) => tag.includes(keyword))
      const matchCategory = activeCategory === '全部' || article.category === activeCategory
      return matchKeyword && matchCategory
    })
  }, [articles, keyword, activeCategory])

  return (
    <PageShell className='tips-page'>
      <View className='tips-hero'>
        <Text className='tips-hero__title'>旅人秘籍</Text>
        <Text className='tips-hero__subtitle'>收藏大家的路線靈感、拍照祕訣與慢遊心法，找到最適合你的澳門玩法。</Text>
      </View>

      <View className='tips-search'>
        <Input
          className='tips-search__input'
          value={keyword}
          placeholder='搜尋路線、拍照地點、慢遊靈感'
          onInput={(event) => setKeyword(event.detail.value || '')}
        />
      </View>

      <ScrollView className='tips-categories' scrollX>
        {categories.map((category) => (
          <View
            key={category}
            className={`tips-categories__item ${activeCategory === category ? 'active' : ''}`}
            onClick={() => setActiveCategory(category)}
          >
            <Text className='tips-categories__text'>{category}</Text>
          </View>
        ))}
      </ScrollView>

      <View className='tips-waterfall'>
        {filtered.map((article) => (
          <View key={article.id} className='tip-card'>
            <View className='tip-card__cover' style={{ background: article.coverColor }}>
              <Text className='tip-card__category'>{article.category}</Text>
              <Text className='tip-card__coverText'>{article.author}</Text>
            </View>
            <View className='tip-card__body'>
              <Text className='tip-card__title'>{article.title}</Text>
              <Text className='tip-card__summary'>{article.summary}</Text>
              <View className='tip-card__tags'>
                {article.tags.map((tag) => (
                  <Text key={tag} className='tip-card__tag'>#{tag}</Text>
                ))}
              </View>
              <View className='tip-card__meta'>
                <Text className='tip-card__metaText'>👍 {article.likes}</Text>
                <Text className='tip-card__metaText'>⭐ {article.saves}</Text>
                <Text className='tip-card__metaText'>{article.readMinutes} 分钟</Text>
              </View>
            </View>
          </View>
        ))}
      </View>
    </PageShell>
  )
}

