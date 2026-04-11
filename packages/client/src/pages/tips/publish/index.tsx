import { useMemo, useState } from 'react'
import { Button, Image, Input, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../../components/PageShell'
import { publishTipPost } from '../../../services/gameService'
import './index.scss'

const categories = ['新手攻略', '慢遊推薦', '拍照秘籍']

const initialDraft = {
  title: '',
  summary: '',
  category: '新手攻略',
  locationName: '澳門半島',
  imageUrl: '',
  paragraphOne: '',
  paragraphTwo: '',
}

export default function TipPublishPage() {
  const [draft, setDraft] = useState(initialDraft)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const completion = useMemo(() => {
    const fields = [draft.title, draft.summary, draft.locationName, draft.paragraphOne, draft.paragraphTwo].filter((item) => item.trim())
    return Math.min(100, Math.round((fields.length / 5) * 100))
  }, [draft])

  const handleChooseImage = async () => {
    try {
      const res = await Taro.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['album', 'camera'],
      })
      const tempFilePath = res.tempFiles?.[0]?.tempFilePath || ''
      if (!tempFilePath) return
      setDraft((prev) => ({ ...prev, imageUrl: tempFilePath }))
      Taro.showToast({ title: '旅行照片已放進草稿', icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: '這次先不放照片也可以', icon: 'none' })
    }
  }

  const handlePublish = async () => {
    if (!draft.title || !draft.summary || !draft.paragraphOne) {
      Taro.showToast({ title: '先寫好標題、摘要和正文', icon: 'none' })
      return
    }

    setIsSubmitting(true)
    try {
      publishTipPost({
        title: draft.title,
        summary: draft.summary,
        category: draft.category,
        locationName: draft.locationName,
        imageUrl: draft.imageUrl,
        contentParagraphs: [draft.paragraphOne, draft.paragraphTwo].filter(Boolean),
      })
      Taro.showToast({ title: '旅途分享已發佈', icon: 'success' })
      setDraft(initialDraft)
      setTimeout(() => {
        Taro.navigateBack()
      }, 500)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <PageShell className='tips-publish-page'>
      <View className='tips-publish-hero'>
        <View className='tips-publish-hero__nav' onClick={() => Taro.navigateBack()}>
          <Text className='tips-publish-hero__back'>← 返回秘籍</Text>
        </View>
        <Text className='tips-publish-hero__eyebrow'>創作工作台</Text>
        <Text className='tips-publish-hero__title'>發佈旅人秘籍</Text>
        <Text className='tips-publish-hero__subtitle'>把你的私藏路線、拍照秘訣和慢遊心得，整理成一篇讓別人願意點進去的分享。</Text>

        <View className='tips-publish-hero__progress'>
          <View className='tips-publish-hero__progressTrack'>
            <View className='tips-publish-hero__progressBar' style={{ width: `${completion}%` }} />
          </View>
          <Text className='tips-publish-hero__progressText'>完成度 {completion}%</Text>
        </View>
      </View>

      <ScrollView className='tips-publish-scroll' scrollY>
        <View className='tips-publish-panel'>
          <Text className='tips-publish-panel__title'>先用一句话抓住别人</Text>
          <Text className='tips-publish-panel__hint'>好标题、清楚摘要、具体地点，是一篇旅途分享最重要的前三秒。</Text>

          <Input className='tips-publish-panel__input' value={draft.title} placeholder='標題，例如：大三巴黃昏最好拍的位置' onInput={(e) => setDraft({ ...draft, title: e.detail.value || '' })} />
          <Input className='tips-publish-panel__input' value={draft.summary} placeholder='一句話摘要，吸引其他旅人點開' onInput={(e) => setDraft({ ...draft, summary: e.detail.value || '' })} />
          <Input className='tips-publish-panel__input' value={draft.locationName} placeholder='帶上定位，例如：大三巴牌坊' onInput={(e) => setDraft({ ...draft, locationName: e.detail.value || '' })} />

          <View className='tips-publish-panel__media'>
            <View className='tips-publish-panel__mediaHead'>
              <Text className='tips-publish-panel__mediaTitle'>封面氛圍</Text>
              <Text className='tips-publish-panel__mediaHint'>有圖更容易被收藏，沒有也能先發文。</Text>
            </View>
            <View className='tips-publish-panel__imagePicker' onClick={handleChooseImage}>
              {draft.imageUrl ? (
                <Image className='tips-publish-panel__imagePreview' src={draft.imageUrl} mode='aspectFill' />
              ) : (
                <Text className='tips-publish-panel__imagePlaceholder'>📸 點一下拍照或從相簿挑一張</Text>
              )}
            </View>
          </View>

          <Text className='tips-publish-panel__sectionTitle'>選一個更貼近內容的分類</Text>
          <ScrollView className='tips-publish-categories' scrollX>
            {categories.map((category) => (
              <View key={category} className={`tips-publish-categories__item ${draft.category === category ? 'active' : ''}`} onClick={() => setDraft({ ...draft, category })}>
                <Text className='tips-publish-categories__text'>{category}</Text>
              </View>
            ))}
          </ScrollView>
        </View>

        <View className='tips-publish-panel'>
          <Text className='tips-publish-panel__title'>把你的现场感写出来</Text>
          <Text className='tips-publish-panel__hint'>第一段写感受，第二段补充经验或提醒，会让内容更完整。</Text>
          <Input className='tips-publish-panel__textarea' value={draft.paragraphOne} placeholder='正文第一段：寫下你的旅途感受' onInput={(e) => setDraft({ ...draft, paragraphOne: e.detail.value || '' })} />
          <Input className='tips-publish-panel__textarea' value={draft.paragraphTwo} placeholder='正文第二段：補充拍照或路線提醒' onInput={(e) => setDraft({ ...draft, paragraphTwo: e.detail.value || '' })} />
        </View>

        <View className='tips-publish-actions'>
          <Button className='tips-publish-actions__ghost' onClick={() => Taro.navigateBack()}>稍後再寫</Button>
          <Button className='tips-publish-actions__primary' loading={isSubmitting} onClick={handlePublish}>立即發佈</Button>
        </View>
      </ScrollView>
    </PageShell>
  )
}
