import { useEffect, useMemo, useState } from 'react'
import { Button, Image, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import PageShell from '../../components/PageShell'
import StoryContentBlockRenderer from '../../components/StoryContentBlockRenderer'
import { getStorylines, refreshPublicContent } from '../../services/gameService'
import type { StoryChapterItem, StorylineItem } from '../../types/game'
import './index.scss'

function describeRule(rule?: StoryChapterItem['unlock']) {
  if (!rule?.type) {
    return ''
  }

  switch (rule.type) {
    case 'sequence':
      return '依章節順序解鎖'
    case 'time_window':
      return '依時間窗口解鎖'
    case 'exploration_progress':
      return '達到探索度門檻'
    case 'stamp_collectible_gate':
      return '需滿足印章或收集物條件'
    case 'completed_previous_chapter':
      return '完成前一章'
    case 'reach_poi':
      return '抵達指定 POI'
    case 'collectible_owned':
      return '擁有指定收集物'
    case 'badge_owned':
      return '擁有指定徽章或稱號'
    case 'stay_duration':
      return '停留達標'
    case 'read_story':
      return '閱讀完成'
    case 'tap_anchor':
      return '點擊章節錨點'
    case 'check_in_poi':
      return '完成景點打卡'
    case 'trigger_interaction':
      return '觸發指定互動'
    case 'unlock_next_chapter':
      return '完成後解鎖下一章'
    case 'grant_collectible':
      return '完成後發放收集物'
    case 'grant_badge':
      return '完成後發放徽章'
    case 'grant_reward':
      return '完成後發放遊戲獎勵'
    case 'fullscreen_media':
      return '完成後播放全屏媒體'
    case 'play_audio':
      return '完成後播放音效'
    case 'show_modal':
      return '完成後顯示彈窗'
    case 'progress_update':
      return '完成後更新進度值'
    default:
      return '自定義規則'
  }
}

function renderStoryTabs(
  items: StorylineItem[],
  sectionTitle: string,
  activeStoryId: number | undefined,
  onSelect: (story: StorylineItem) => void,
) {
  return (
    <View className='story-tab-section'>
      <Text className='story-tab-section__title'>{sectionTitle}</Text>
      <ScrollView className='story-tab-scroll' scrollX>
        {items.map((story) => {
          const active = activeStoryId === story.id
          return (
            <View
              key={story.id}
              className={`story-tab-card ${active ? 'story-tab-card--active' : ''} ${story.locked ? 'story-tab-card--locked' : ''}`}
              onClick={() => onSelect(story)}
            >
              {story.coverImageUrl ? (
                <Image className='story-tab-card__image' src={story.coverImageUrl} mode='aspectFill' />
              ) : (
                <View className='story-tab-card__cover' style={{ background: story.coverColor }}>
                  <Text className='story-tab-card__icon'>{story.icon}</Text>
                </View>
              )}
              <View className='story-tab-card__content'>
                <Text className='story-tab-card__name'>{story.name}</Text>
                <Text className='story-tab-card__meta'>
                  {story.locked ? '尚待解鎖' : `${story.progress}% 完成 / ${story.estimatedTime}`}
                </Text>
              </View>
            </View>
          )
        })}
      </ScrollView>
    </View>
  )
}

export default function StoryPage() {
  const router = Taro.getCurrentInstance().router
  const initialStoryId = router?.params?.storyId ? Number(router.params.storyId) : undefined
  const initialChapterId = router?.params?.chapterId ? Number(router.params.chapterId) : undefined
  const [stories, setStories] = useState(() => getStorylines())
  const [expandedStoryId, setExpandedStoryId] = useState<number | undefined>(initialStoryId)
  const [expandedChapterId, setExpandedChapterId] = useState<number | null>(initialChapterId || null)

  const unlockedStories = useMemo(() => stories.filter((story) => !story.locked), [stories])
  const lockedStories = useMemo(() => stories.filter((story) => story.locked), [stories])
  const activeStory = useMemo(
    () => stories.find((story) => story.id === expandedStoryId) || unlockedStories[0] || stories[0],
    [stories, expandedStoryId, unlockedStories],
  )

  useEffect(() => {
    let cancelled = false

    const hydrateStories = async () => {
      try {
        await refreshPublicContent()
      } catch (error) {
        console.warn('Failed to refresh story content.', error)
      }

      if (!cancelled) {
        const nextStories = getStorylines()
        setStories(nextStories)
        if (!expandedStoryId && nextStories[0]) {
          setExpandedStoryId(nextStories[0].id)
        }
      }
    }

    void hydrateStories()

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (!activeStory) {
      return
    }

    if (activeStory.locked) {
      setExpandedChapterId(null)
      return
    }

    const nextChapter = activeStory.chapters?.find((chapter) => !chapter.locked) || activeStory.chapters?.[0]
    if (nextChapter && !expandedChapterId) {
      setExpandedChapterId(nextChapter.id)
    }
  }, [activeStory, expandedChapterId])

  const handleStorySelect = (story: StorylineItem) => {
    if (story.locked) {
      Taro.showToast({ title: story.unlockHint || '此故事線仍未解鎖', icon: 'none' })
      return
    }
    setExpandedStoryId(story.id)
    const nextChapter = story.chapters?.find((chapter) => !chapter.locked) || story.chapters?.[0]
    setExpandedChapterId(nextChapter?.id || null)
  }

  const handleToggleChapter = (chapter: StoryChapterItem) => {
    if (chapter.locked) {
      Taro.showToast({ title: '請先完成前置章節或條件', icon: 'none' })
      return
    }
    setExpandedChapterId((prev) => (prev === chapter.id ? null : chapter.id))
  }

  return (
    <PageShell className='story-page'>
      <View className='page-header'>
        <Text className='page-title'>故事探索</Text>
        <Text className='page-subtitle'>
          在這裡切換故事線、展開章節、閱讀多媒體內容，並查看每一章的解鎖條件與完成效果。
        </Text>
      </View>

      <View className='story-shell'>
        {renderStoryTabs(unlockedStories, '可立即探索', activeStory?.id, handleStorySelect)}
        {lockedStories.length > 0
          ? renderStoryTabs(lockedStories, '待解鎖故事線', activeStory?.id, handleStorySelect)
          : null}

        {activeStory ? (
          <View className={`story-focus-card ${activeStory.locked ? 'story-focus-card--locked' : ''}`}>
            <View
              className='story-focus-card__hero'
              style={{
                backgroundImage: activeStory.bannerImageUrl
                  ? `linear-gradient(135deg, rgba(255, 247, 241, 0.35), rgba(255, 255, 255, 0.75)), url(${activeStory.bannerImageUrl})`
                  : undefined,
                backgroundColor: activeStory.coverColor,
              }}
            >
              <View className='story-focus-card__heroContent'>
                <Text className='story-focus-card__eyebrow'>
                  {activeStory.locked ? '尚未解鎖的故事線' : '正在編排中的旅程'}
                </Text>
                <Text className='story-focus-card__title'>{activeStory.name}</Text>
                <Text className='story-focus-card__subtitle'>{activeStory.nameEn}</Text>
              </View>
              <Text className='story-focus-card__icon'>{activeStory.icon}</Text>
            </View>

            <View className='story-focus-card__body'>
              <Text className='story-focus-card__desc'>{activeStory.description}</Text>

              {!!activeStory.moodTags?.length ? (
                <View className='story-tags'>
                  {activeStory.moodTags.map((tag) => (
                    <Text key={tag} className='story-tag'>{tag}</Text>
                  ))}
                </View>
              ) : null}

              <View className='story-overview-grid'>
                <View className='story-overview-item'>
                  <Text className='story-overview-item__label'>預估時長</Text>
                  <Text className='story-overview-item__value'>{activeStory.estimatedTime}</Text>
                </View>
                <View className='story-overview-item'>
                  <Text className='story-overview-item__label'>章節數量</Text>
                  <Text className='story-overview-item__value'>{activeStory.totalChapters} 章</Text>
                </View>
                <View className='story-overview-item'>
                  <Text className='story-overview-item__label'>綁定地圖</Text>
                  <Text className='story-overview-item__value'>
                    {activeStory.cityBindingCodes?.join(' / ') || '未指定'}
                  </Text>
                </View>
                <View className='story-overview-item'>
                  <Text className='story-overview-item__label'>主線獎勵</Text>
                  <Text className='story-overview-item__value'>{activeStory.rewardBadge || '待設定'}</Text>
                </View>
              </View>

              <View className='story-progress-card'>
                <View className='story-progress-card__top'>
                  <Text className='story-progress-card__label'>目前進度</Text>
                  <Text className='story-progress-card__percent'>
                    {activeStory.locked ? '未解鎖' : `${activeStory.progress}%`}
                  </Text>
                </View>
                <View className='progress-bar'>
                  <View className='progress-fill' style={{ width: `${activeStory.locked ? 0 : activeStory.progress}%` }} />
                </View>
                <Text className='story-progress-card__hint'>
                  {activeStory.locked
                    ? activeStory.unlockHint || '請先探索綁定地圖與前置內容，解鎖這條故事線。'
                    : `已完成 ${activeStory.completedChapters} / ${activeStory.totalChapters} 章，展開下方章節可閱讀完整內容。`}
                </Text>
              </View>

              {activeStory.locked ? (
                <View className='story-locked-panel'>
                  <Text className='story-locked-panel__title'>解鎖提示</Text>
                  <Text className='story-locked-panel__desc'>
                    {activeStory.unlockHint || '請先前往對應地圖完成探索，再回來開啟這條故事線。'}
                  </Text>
                  <Button className='story-locked-panel__button' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>
                    先去探索地圖
                  </Button>
                </View>
              ) : (
                <View className='chapter-list'>
                  <Text className='chapter-list__title'>章節工作台</Text>
                  {(activeStory.chapters || []).map((chapter, index) => {
                    const expanded = expandedChapterId === chapter.id && !chapter.locked
                    return (
                      <View
                        key={chapter.id}
                        className={`chapter-card ${chapter.locked ? 'chapter-card--locked' : ''} ${expanded ? 'chapter-card--expanded' : ''}`}
                      >
                        <View className='chapter-card__header' onClick={() => handleToggleChapter(chapter)}>
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
                                <Text className='chapter-card__infoLabel'>章節成就</Text>
                                <Text className='chapter-card__infoValue'>{chapter.achievement}</Text>
                              </View>
                              <View className='chapter-card__infoItem'>
                                <Text className='chapter-card__infoLabel'>收集目標</Text>
                                <Text className='chapter-card__infoValue'>{chapter.collectible}</Text>
                              </View>
                              <View className='chapter-card__infoItem chapter-card__infoItem--full'>
                                <Text className='chapter-card__infoLabel'>錨點位置</Text>
                                <Text className='chapter-card__infoValue'>{chapter.locationName}</Text>
                              </View>
                            </View>

                            <View className='chapter-rule-list'>
                              {chapter.unlock ? <Text className='chapter-rule-chip'>解鎖：{describeRule(chapter.unlock)}</Text> : null}
                              {chapter.prerequisite ? <Text className='chapter-rule-chip'>前置：{describeRule(chapter.prerequisite)}</Text> : null}
                              {chapter.completion ? <Text className='chapter-rule-chip'>完成：{describeRule(chapter.completion)}</Text> : null}
                              {chapter.effect ? <Text className='chapter-rule-chip'>效果：{describeRule(chapter.effect)}</Text> : null}
                            </View>

                            <StoryContentBlockRenderer blocks={chapter.contentBlocks} />

                            <View className='chapter-card__actions'>
                              <Button className='chapter-card__primary' onClick={() => Taro.switchTab({ url: '/pages/map/index' })}>
                                前往地圖
                              </Button>
                              <Button className='chapter-card__secondary' onClick={() => Taro.navigateTo({ url: '/pages/stamps/index' })}>
                                查看收集
                              </Button>
                            </View>
                          </View>
                        ) : null}

                        {chapter.locked ? (
                          <View className='chapter-card__lockedTip'>
                            <Text className='chapter-card__lockedText'>請先完成前置章節、地點或互動條件。</Text>
                          </View>
                        ) : null}
                      </View>
                    )
                  })}
                </View>
              )}
            </View>
          </View>
        ) : null}

        <View className='bottom-spacer' />
      </View>
    </PageShell>
  )
}
