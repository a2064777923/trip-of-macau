import { useEffect, useState } from 'react'
import { Button, Input, Switch, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import {
  getEmergencyContact,
  isAuthRequiredError,
  loadGameState,
  requireAuth,
  updateEmergencyContact,
  updateUserPreference,
} from '../../services/gameService'
import './index.scss'

export default function SeniorPage() {
  const initialState = loadGameState()
  const initialContact = getEmergencyContact()
  const [user, setUser] = useState(initialState.user)
  const [name, setName] = useState(initialContact.name)
  const [phone, setPhone] = useState(initialContact.phone)
  const [voiceGuideEnabled, setVoiceGuideEnabled] = useState(initialState.user.voiceGuideEnabled)

  useEffect(() => {
    if (user.authStatus === 'anonymous') {
      void requireAuth('開啟長者模式與緊急聯絡人前，請先使用微信登入。')
    }
  }, [user.authStatus])

  const saveEmergencyContact = async () => {
    try {
      const savedContact = await updateEmergencyContact({ name, phone })
      setName(savedContact.name)
      setPhone(savedContact.phone)
      Taro.showToast({ title: '已保存緊急聯絡人', icon: 'success' })
    } catch (error) {
      if (!isAuthRequiredError(error)) {
        Taro.showToast({ title: error instanceof Error ? error.message : '保存緊急聯絡人失敗', icon: 'none' })
      }
    }
  }

  const toggleElderMode = async (value: boolean) => {
    try {
      const nextUser = await updateUserPreference({
        interfaceMode: value ? 'elderly' : 'standard',
        fontScale: value ? Math.max(1.5, user.fontScale) : 1,
        highContrast: value,
        voiceGuideEnabled: value ? true : voiceGuideEnabled,
      })
      setUser(nextUser)
      setVoiceGuideEnabled(nextUser.voiceGuideEnabled)
      Taro.showToast({ title: value ? '已切換長者模式' : '已返回標準模式', icon: 'none' })
    } catch (error) {
      if (!isAuthRequiredError(error)) {
        Taro.showToast({ title: error instanceof Error ? error.message : '切換模式失敗', icon: 'none' })
      }
    }
  }

  const toggleVoiceGuide = async (value: boolean) => {
    const previous = voiceGuideEnabled
    setVoiceGuideEnabled(value)
    try {
      const nextUser = await updateUserPreference({ voiceGuideEnabled: value })
      setUser(nextUser)
      setVoiceGuideEnabled(nextUser.voiceGuideEnabled)
    } catch (error) {
      setVoiceGuideEnabled(previous)
      if (!isAuthRequiredError(error)) {
        Taro.showToast({ title: error instanceof Error ? error.message : '語音導覽更新失敗', icon: 'none' })
      }
    }
  }

  if (user.authStatus === 'anonymous') {
    return (
      <View className='senior-page'>
        <View className='senior-hero'>
          <Text className='senior-hero__title'>需要登入</Text>
          <Text className='senior-hero__subtitle'>正在前往「我的」頁面完成登入。</Text>
        </View>
      </View>
    )
  }

  return (
    <View className='senior-page'>
      <View className='senior-hero'>
        <Text className='senior-hero__title'>長者模式</Text>
        <Text className='senior-hero__subtitle'>更大的字體、更少的操作與需要時可用的求助入口，讓旅途更安心。</Text>
      </View>

      <View className='senior-card primary'>
        <Text className='senior-card__title'>目前主操作</Text>
        <Button className='senior-card__button' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>開始慢遊探索</Button>
        <Text className='senior-card__desc'>先走到附近景點，再用語音與大按鈕一步步完成旅程。</Text>
      </View>

      <View className='senior-card'>
        <View className='senior-row'>
          <View>
            <Text className='senior-label'>啟用長者模式</Text>
            <Text className='senior-desc'>自動提升字體、對比與易讀性</Text>
          </View>
          <Switch checked={user.interfaceMode === 'elderly'} color='#FFB6C1' onChange={(event) => void toggleElderMode(event.detail.value)} />
        </View>

        <View className='senior-row'>
          <View>
            <Text className='senior-label'>自動語音導覽</Text>
            <Text className='senior-desc'>到達景點後自動播報介紹</Text>
          </View>
          <Switch checked={voiceGuideEnabled} color='#98FB98' onChange={(event) => void toggleVoiceGuide(event.detail.value)} />
        </View>
      </View>

      <View className='senior-card'>
        <Text className='senior-card__title'>緊急聯絡人</Text>
        <Input className='senior-input' value={name} placeholder='聯絡人姓名' onInput={(event) => setName(event.detail.value || '')} />
        <Input className='senior-input' value={phone} placeholder='聯絡電話' type='number' onInput={(event) => setPhone(event.detail.value || '')} />
        <View className='senior-actions'>
          <Button className='senior-outline' onClick={() => void saveEmergencyContact()}>保存聯絡人</Button>
          <Button className='senior-solid' onClick={() => Taro.makePhoneCall({ phoneNumber: phone || '10086' })}>一鍵求助</Button>
        </View>
      </View>

      <View className='senior-card light'>
        <Text className='senior-card__title'>慢遊建議</Text>
        <Text className='senior-tip'>- 先前往步行距離較短的景點開始探索。</Text>
        <Text className='senior-tip'>- 開啟語音導覽後，到站會自動播放介紹。</Text>
        <Text className='senior-tip'>- 如果定位不準，可在支援的景點使用手動補簽。</Text>
      </View>
    </View>
  )
}
