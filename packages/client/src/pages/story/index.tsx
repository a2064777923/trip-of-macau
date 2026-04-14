import { useEffect, useMemo, useState } from 'react'

import { Button, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../components/PageShell'
import { getStorylines, refreshPublicContent } from '../../services/gameService'
import './index.scss'

export default function StoryPage() {
  const router = Taro.getCurrentInstance().router
  const initialStoryId = Number(router?.params?.storyId || 1)
  const initialChapterId = router?.params?.chapterId ? Number(router.params.chapterId) : 1011
  const [stories, setStories] = useState(() => getStorylines())
  const [expandedStoryId, setExpandedStoryId] = useState(initialStoryId)
  const [expandedChapterId, setExpandedChapterId] = useState<number | null>(initialChapterId)


  const unlockedStories = useMemo(() => stories.filter((story) => !story.locked), [stories])
  const lockedStories = useMemo(() => stories.filter((story) => story.locked), [stories])
  const activeStory = useMemo(() => stories.find((story) => story.id === expandedStoryId) || unlockedStories[0] || stories[0], [stories, expandedStoryId, unlockedStories])

  useEffect(() => {
    let cancelled = false

    const hydrateStories = async () => {
      try {
        await refreshPublicContent()
      } catch (error) {
        console.warn('Failed to refresh story content.', error)
      }

      if (!cancelled) {
        setStories(getStorylines())
      }
    }

    void hydrateStories()

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {

    if (!router?.params?.storyId) return
    const targetStoryId = Number(router.params.storyId)
    const targetChapterId = router.params.chapterId ? Number(router.params.chapterId) : null
    if (targetStoryId) {
      setExpandedStoryId(targetStoryId)
    }
    if (targetChapterId) {
      setExpandedChapterId(targetChapterId)
    }
  }, [router?.params?.storyId, router?.params?.chapterId])



  const handleToggleStory = (storyId: number, locked?: boolean) => {
    if (locked) {
      Taro.showToast({ title: '這條主線還在等待你解鎖', icon: 'none' })
      return
    }
    setExpandedStoryId((prev) => (prev === storyId ? storyId : storyId))
    const selectedStory = stories.find((story) => story.id === storyId)
    const firstUnlockedChapter = selectedStory?.chapters?.find((chapter) => !chapter.locked)
    setExpandedChapterId(firstUnlockedChapter?.id || null)
  }

  const handleToggleChapter = (chapterId: number, locked?: boolean) => {
    if (locked) {
      Taro.showToast({ title: '完成上一章後，這一章就會亮起', icon: 'none' })
      return
    }
    setExpandedChapterId((prev) => (prev === chapterId ? null : chapterId))
  }

  const renderStoryTabs = (items, sectionTitle) => (
    <View className='story-tab-section'>
      <Text className='story-tab-section__title'>{sectionTitle}</Text>
      <ScrollView className='story-tab-scroll' scrollX>
        {items.map((story) => {
          const active = activeStory?.id === story.id
          return (
            <View
              key={story.id}
              className={`story-tab-card ${active ? 'story-tab-card--active' : ''} ${story.locked ? 'story-tab-card--locked' : ''}`}
              onClick={() => handleToggleStory(story.id, story.locked)}
            >
              <View className='story-tab-card__cover' style={{ background: story.coverColor }}>
                <Text className='story-tab-card__icon'>{story.icon}</Text>
              </View>
              <View className='story-tab-card__content'>
                <Text className='story-tab-card__name'>{story.name}</Text>
                <Text className='story-tab-card__meta'>
                  {story.locked ? '等待解鎖' : `${story.progress}% · ${story.estimatedTime}`}
                </Text>
              </View>
            </View>
          )
        })}
      </ScrollView>
    </View>
  )

  return (
    <PageShell className='story-page'>
      <View className='page-header'>
        <Text className='page-title'>故事主線</Text>
        <Text className='page-subtitle'>先挑一條已點亮的主線，再往下展開章節、成就與收藏；未解鎖內容也會提前告訴你下一步去哪裡。</Text>
      </View>

      <View className='story-shell'>
        {renderStoryTabs(unlockedStories, '已解鎖主線')}
        {lockedStories.length > 0 && renderStoryTabs(lockedStories, '待解鎖主線')}

        {activeStory && (
          <View className={`story-focus-card ${activeStory.locked ? 'story-focus-card--locked' : ''}`}>
            <View className='story-focus-card__hero' style={{ background: activeStory.coverColor }}>
              <View>
                <Text className='story-focus-card__eyebrow'>{activeStory.locked ? '尚未解鎖' : '正在閱讀這條主線'}</Text>
                <Text className='story-focus-card__title'>{activeStory.name}</Text>
                <Text className='story-focus-card__subtitle'>{activeStory.nameEn}</Text>
              </View>
              <Text className='story-focus-card__icon'>{activeStory.icon}</Text>
            </View>

            <View className='story-focus-card__body'>
              <Text className='story-focus-card__desc'>{activeStory.description}</Text>

              {!!activeStory.moodTags?.length && (
                <View className='story-tags'>
                  {activeStory.moodTags.map((tag) => (
                    <Text key={tag} className='story-tag'>{tag}</Text>
                  ))}
                </View>
              )}

              <View className='story-overview-grid'>
                <View className='story-overview-item'>
                  <Text className='story-overview-item__label'>旅程時長</Text>
                  <Text className='story-overview-item__value'>{activeStory.estimatedTime}</Text>
                </View>
                <View className='story-overview-item'>
                  <Text className='story-overview-item__label'>途經地點</Text>
                  <Text className='story-overview-item__value'>{activeStory.poiIds.length} 個</Text>
                </View>
                <View className='story-overview-item'>
                  <Text className='story-overview-item__label'>主線獎勵</Text>
                  <Text className='story-overview-item__value'>{activeStory.rewardBadge || '旅程印章'}</Text>
                </View>
              </View>

              <View className='story-progress-card'>
                <View className='story-progress-card__top'>
                  <Text className='story-progress-card__label'>目前進度</Text>
                  <Text className='story-progress-card__percent'>{activeStory.locked ? '未解鎖' : `${activeStory.progress}%`}</Text>
                </View>
                <View className='progress-bar'>
                  <View className='progress-fill' style={{ width: `${activeStory.locked ? 0 : activeStory.progress}%` }} />
                </View>
                <Text className='story-progress-card__hint'>
                  {activeStory.locked
                    ? activeStory.unlockHint || '繼續探索新的城市與地標後，即可點亮這條主線。'
                    : `已完成 ${activeStory.completedChapters}/${activeStory.totalChapters} 章，點開下方章節可查看詳情。`}
                </Text>
              </View>

              {!activeStory.locked ? (
                <View className='chapter-list'>
                  <Text className='chapter-list__title'>章節一覽</Text>
                  {(activeStory.chapters || []).map((chapter, index) => {
                    const expanded = expandedChapterId === chapter.id && !chapter.locked
                    return (
                      <View key={chapter.id} className={`chapter-card ${chapter.locked ? 'chapter-card--locked' : ''} ${expanded ? 'chapter-card--expanded' : ''}`}>
                        <View className='chapter-card__header' onClick={() => handleToggleChapter(chapter.id, chapter.locked)}>
                          <View className='chapter-card__headerMain'>
                            <Text className='chapter-card__index'>第 {index + 1} 章</Text>
                            <Text className='chapter-card__title'>{chapter.title}</Text>
                            <Text className='chapter-card__summary'>{chapter.summary}</Text>
                          </View>
                          <View className='chapter-card__side'>
                            <Text className={`chapter-card__badge ${chapter.locked ? 'locked' : 'ready'}`}>
                              {chapter.locked ? '未解鎖' : expanded ? '收起' : '展開'}
                            </Text>
                          </View>
                        </View>

                        {expanded ? (
                          <View className='chapter-card__detailWrap'>
                            <Text className='chapter-card__detail'>{chapter.detail}</Text>
                            <View className='chapter-card__infoGrid'>
                              <View className='chapter-card__infoItem'>
                                <Text className='chapter-card__infoLabel'>達成成就</Text>
                                <Text className='chapter-card__infoValue'>{chapter.achievement}</Text>
                              </View>
                              <View className='chapter-card__infoItem'>
                                <Text className='chapter-card__infoLabel'>收集品</Text>
                                <Text className='chapter-card__infoValue'>{chapter.collectible}</Text>
                              </View>
                              <View className='chapter-card__infoItem chapter-card__infoItem--full'>
                                <Text className='chapter-card__infoLabel'>關聯地點</Text>
                                <Text className='chapter-card__infoValue'>{chapter.locationName}</Text>
                              </View>
                            </View>
                            <View className='chapter-card__actions'>
                              <Button className='chapter-card__primary' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>去地圖查看</Button>
                              <Button className='chapter-card__secondary' onClick={() => Taro.navigateTo({ url: '/pages/stamps/index' })}>查看收藏</Button>
                            </View>
                          </View>
                        ) : null}

                        {chapter.locked ? (
                          <View className='chapter-card__lockedTip'>
                            <Text className='chapter-card__lockedText'>完成上一章並靠近對應地標後，這一章就會亮起。</Text>
                          </View>
                        ) : null}
                      </View>
                    )
                  })}
                </View>
              ) : (
                <View className='story-locked-panel'>
                  <Text className='story-locked-panel__title'>解鎖提示</Text>
                  <Text className='story-locked-panel__desc'>{activeStory.unlockHint || '繼續探索新的城市與地標後，即可點亮這條主線。'}</Text>
                  <Button className='story-locked-panel__button' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>先去探索地圖</Button>
                </View>
              )}
            </View>
          </View>
        )}

        <View className='bottom-spacer' />
      </View>
    </PageShell>
  )
}
