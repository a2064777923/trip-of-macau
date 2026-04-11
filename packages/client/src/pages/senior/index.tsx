import { useMemo, useState } from 'react'
import { Button, Input, Switch, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { getEmergencyContact, loadGameState, updateEmergencyContact, updateUserPreference } from '../../services/gameService'
import './index.scss'

export default function SeniorPage() {
  const state = useMemo(() => loadGameState(), [])
  const contact = useMemo(() => getEmergencyContact(), [])
  const [name, setName] = useState(contact.name)
  const [phone, setPhone] = useState(contact.phone)
  const [voiceGuideEnabled, setVoiceGuideEnabled] = useState(state.user.voiceGuideEnabled)

  const saveEmergencyContact = () => {
    updateEmergencyContact({ name, phone })
    Taro.showToast({ title: '已保存聯絡人', icon: 'success' })
  }

  const toggleElderMode = (value) => {
    updateUserPreference({
      interfaceMode: value ? 'elderly' : 'standard',
      fontScale: value ? Math.max(1.5, state.user.fontScale) : 1,
      highContrast: value,
      voiceGuideEnabled: value ? true : voiceGuideEnabled,
    })
    Taro.showToast({ title: value ? '已切換長者模式' : '已返回標準模式', icon: 'none' })
  }

  const toggleVoiceGuide = (value) => {
    setVoiceGuideEnabled(value)
    updateUserPreference({ voiceGuideEnabled: value })
  }

  return (
    <View className='senior-page'>
      <View className='senior-hero'>
        <Text className='senior-hero__title'>長者模式</Text>
        <Text className='senior-hero__subtitle'>更大的字體、更少的操作與隨時可用的求助入口，讓旅途輕鬆又安心。</Text>
      </View>

      <View className='senior-card primary'>
        <Text className='senior-card__title'>當前主操作</Text>
        <Button className='senior-card__button' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>開始慢遊探索</Button>
        <Text className='senior-card__desc'>每屏只做一件事，先走到附近任務點，再用語音與獎勵引導下一步。</Text>
      </View>

      <View className='senior-card'>
        <View className='senior-row'>
          <View>
            <Text className='senior-label'>開啟長者模式</Text>
            <Text className='senior-desc'>自動提升字體、觸摸面積與高對比度</Text>
          </View>
          <Switch checked={state.user.interfaceMode === 'elderly'} color='#FFB6C1' onChange={(event) => toggleElderMode(event.detail.value)} />
        </View>

        <View className='senior-row'>
          <View>
            <Text className='senior-label'>自動語音導覽</Text>
            <Text className='senior-desc'>到達地點後優先播放語音介紹</Text>
          </View>
          <Switch checked={voiceGuideEnabled} color='#98FB98' onChange={(event) => toggleVoiceGuide(event.detail.value)} />
        </View>
      </View>

      <View className='senior-card'>
        <Text className='senior-card__title'>緊急聯絡人</Text>
        <Input className='senior-input' value={name} placeholder='聯絡人姓名' onInput={(event) => setName(event.detail.value || '')} />
        <Input className='senior-input' value={phone} placeholder='聯絡電話' type='number' onInput={(event) => setPhone(event.detail.value || '')} />
        <View className='senior-actions'>
          <Button className='senior-outline' onClick={saveEmergencyContact}>保存聯絡人</Button>
          <Button className='senior-solid' onClick={() => Taro.makePhoneCall({ phoneNumber: phone || '10086' })}>一鍵求助</Button>
        </View>
      </View>

      <View className='senior-card light'>
        <Text className='senior-card__title'>慢遊建議</Text>
        <Text className='senior-tip'>- 優先前往媽閣廟與議事亭前地，步行短、節奏慢</Text>
        <Text className='senior-tip'>- 開啟語音導覽，抵達後自動播放介紹</Text>
        <Text className='senior-tip'>- 若定位不穩，可使用手動補簽與二次確認</Text>
      </View>
    </View>
  )
}
