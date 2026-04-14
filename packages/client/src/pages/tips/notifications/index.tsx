import { useState } from 'react'
import { ScrollView, Text, View } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import PageShell from '../../../components/PageShell'
import { getNotifications, isAuthRequiredError, markNotificationsRead, refreshPublicContent } from '../../../services/gameService'
import './index.scss'

const typeLabelMap = {
  system: '系統提醒',
  ugc: '旅人互動',
  activity: '活動消息',
} as const

export default function TipNotificationsPage() {
  const [notifications, setNotifications] = useState(() => getNotifications())

  const refreshNotifications = async () => {
    try {
      await refreshPublicContent()
      await markNotificationsRead()
    } catch (error) {
      if (!isAuthRequiredError(error)) {
        console.warn('Failed to refresh notifications.', error)
      }
    }
    setNotifications(getNotifications())
  }

  useDidShow(() => {
    void refreshNotifications()
  })

  const unreadCount = notifications.filter((item) => item.unread).length

  return (
    <PageShell className='tips-notification-page'>
      <View className='tips-notification-hero'>
        <View className='tips-notification-hero__nav' onClick={() => Taro.navigateBack()}>
          <Text className='tips-notification-hero__back'>← 返回秘笈</Text>
        </View>
        <Text className='tips-notification-hero__eyebrow'>消息中心</Text>
        <Text className='tips-notification-hero__title'>旅途通知</Text>
        <Text className='tips-notification-hero__subtitle'>把系統提醒、旅人互動和活動消息收進一個更安靜也更好讀的頁面裡。</Text>

        <View className='tips-notification-hero__summary'>
          <View className='tips-notification-hero__summaryCard'>
            <Text className='tips-notification-hero__summaryNumber'>{notifications.length}</Text>
            <Text className='tips-notification-hero__summaryLabel'>全部消息</Text>
          </View>
          <View className='tips-notification-hero__summaryCard'>
            <Text className='tips-notification-hero__summaryNumber'>{unreadCount}</Text>
            <Text className='tips-notification-hero__summaryLabel'>未讀</Text>
          </View>
        </View>
      </View>

      <ScrollView className='tips-notification-scroll' scrollY>
        <View className='tips-notification-section'>
          {notifications.map((item) => (
            <View key={item.id} className={`tips-notification-card ${item.unread ? 'tips-notification-card--unread' : ''}`}>
              <View className='tips-notification-card__head'>
                <Text className='tips-notification-card__chip'>{typeLabelMap[item.type]}</Text>
                <Text className='tips-notification-card__time'>{item.timeLabel}</Text>
              </View>
              <Text className='tips-notification-card__title'>{item.title}</Text>
              <Text className='tips-notification-card__content'>{item.content}</Text>
              {item.unread && <Text className='tips-notification-card__dot'>NEW</Text>}
            </View>
          ))}
        </View>
      </ScrollView>
    </PageShell>
  )
}
