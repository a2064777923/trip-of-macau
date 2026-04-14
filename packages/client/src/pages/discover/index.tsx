import { useEffect, useState } from 'react'
import { Button, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getDiscoverCards, refreshPublicContent } from '../../services/gameService'
import PageShell from '../../components/PageShell'
import './index.scss'

export default function DiscoverPage() {
  const [cards, setCards] = useState(() => getDiscoverCards())

  useEffect(() => {
    let cancelled = false

    const hydrateDiscover = async () => {
      try {
        await refreshPublicContent()
      } catch (error) {
        console.warn('Failed to refresh discover cards.', error)
      }

      if (!cancelled) {
        setCards(getDiscoverCards())
      }
    }

    void hydrateDiscover()

    return () => {
      cancelled = true
    }
  }, [])

  return (
    <PageShell className='discover-page'>
      <View className='discover-hero'>
        <Text className='discover-hero__title'>發現澳門新玩法</Text>
        <Text className='discover-hero__subtitle'>看看城中的活動靈感、旅途彩蛋與熱門足跡，把今天的散步安排得剛剛好。</Text>
      </View>

      <View className='discover-list'>
        {cards.map((card) => (
          <View key={card.id} className='discover-card'>
            <View className='discover-card__cover' style={{ background: card.coverColor }}>
              <Text className='discover-card__tag'>{card.tag}</Text>
              <Text className='discover-card__icon'>{card.icon}</Text>
            </View>
            <View className='discover-card__body'>
              <Text className='discover-card__title'>{card.title}</Text>
              <Text className='discover-card__subtitle'>{card.subtitle} · {card.district}</Text>
              <Text className='discover-card__description'>{card.description}</Text>
            </View>
            <Button
              className='discover-card__button'
              onClick={() => {
                if (card.type === 'checkin') {
                  Taro.switchTab({ url: '/pages/map/index' })
                  return
                }
                if (card.type === 'merchant') {
                  Taro.navigateTo({ url: '/pages/rewards/index' })
                  return
                }
                Taro.navigateTo({ url: '/pages/tips/index' })
              }}
            >
              {card.actionText}
            </Button>
          </View>
        ))}
      </View>
    </PageShell>
  )
}

