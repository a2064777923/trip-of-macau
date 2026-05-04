import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, Image, ScrollView, Text, View } from '@tarojs/components'
import Taro from '@tarojs/taro'
import LottieAssetPlayer from '../../components/LottieAssetPlayer'
import PageShell from '../../components/PageShell'
import StoryContentBlockRenderer from '../../components/StoryContentBlockRenderer'
import {
  PUBLIC_API_HOST_LABEL,
  RUNTIME_ENV_LABEL,
  STORY_RUNTIME_DIAGNOSTICS_ENABLED,
} from '../../constants/env'
import {
  buildStoryModeRouteContext,
  exitStoryModeSession,
  getActiveStoryModeSession,
  isAuthRequiredError,
  getStorylines,
  mapStoryExplorationSummary,
  recordStoryRuntimeEvent,
  refreshPublicContent,
  refreshStoryExplorationSummary,
  refreshStorylineRuntime,
  resolveStoryRouteDestination,
  saveStoryModeRouteContext,
  startStoryModeSession,
} from '../../services/gameService'
import {
  buildStoryRuntimeEventPayload,
  classifyStoryRuntimeStep,
  getStoryRuntimeActionKey,
  resolveStoryRuntimeFeedback,
} from '../../services/storyRuntimeEventEngine'
import type {
  StoryChapterItem,
  StoryExplorationSummaryItem,
  StoryMediaAssetItem,
  StoryModeRouteChapter,
  StorylineItem,
  StoryModeSessionState,
  StoryRuntimeActionState,
  StoryRuntimeEventType,
  StoryRuntimeStepItem,
} from '../../types/game'
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

function pickStepMediaUrl(step: StoryRuntimeStepItem) {
  return step.mediaAsset?.url || step.mediaAsset?.fallbackUrl || step.mediaAsset?.posterUrl || ''
}

function renderRuntimeStepMedia(step: StoryRuntimeStepItem) {
  const asset = step.mediaAsset
  const assetUrl = pickStepMediaUrl(step)
  if (!asset) {
    return null
  }

  if (asset.assetKind === 'lottie') {
    return (
      <View className='story-runtime-step__media'>
        <LottieAssetPlayer asset={asset} />
      </View>
    )
  }

  if (assetUrl && (asset.assetKind === 'image' || asset.assetKind === 'icon')) {
    return (
      <View className='story-runtime-step__media'>
        <Image className='story-runtime-step__image' src={assetUrl} mode='widthFix' />
      </View>
    )
  }

  return (
    <Text className='story-runtime-step__mediaText'>
      已掛載媒體：{asset.originalFilename || assetUrl || `資源 #${asset.id}`}
    </Text>
  )
}

function getRuntimeSteps(chapter: StoryChapterItem) {
  return (chapter.runtimeSteps || chapter.runtime?.runtimeSteps || [])
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
}

function getRouteStatusText(status: StoryModeRouteChapter['status']) {
  switch (status) {
    case 'current':
      return '目前章節'
    case 'completed':
      return '已完成章節'
    case 'locked':
      return '尚未解鎖'
    default:
      return '待前往'
  }
}

function getActionStateText(actionState?: StoryRuntimeActionState) {
  switch (actionState?.status) {
    case 'syncing':
      return '同步中'
    case 'synced':
      return '已同步'
    case 'already_synced':
      return '已記錄'
    case 'failed':
      return '同步失敗，可重試'
    case 'blocked':
      return '請先開始故事模式'
    case 'unsupported':
      return '稍後開放'
    default:
      return ''
  }
}

function renderActionFeedback(actionState?: StoryRuntimeActionState) {
  if (!actionState?.title && !actionState?.message && !actionState?.outcomeLabels?.length) {
    return null
  }
  return (
    <View className='story-runtime-feedback'>
      {actionState.title ? <Text className='story-runtime-feedback__title'>{actionState.title}</Text> : null}
      {actionState.message ? <Text className='story-runtime-feedback__message'>{actionState.message}</Text> : null}
      {!!actionState.outcomeLabels?.length ? (
        <View className='story-runtime-feedback__labels'>
          {actionState.outcomeLabels.map((label) => (
            <Text key={label} className='story-runtime-feedback__label'>{label}</Text>
          ))}
        </View>
      ) : null}
    </View>
  )
}

function renderRuntimeFlow({
  chapter,
  storyModeSession,
  actionStates,
  onStepAction,
}: {
  chapter: StoryChapterItem
  storyModeSession: StoryModeSessionState | null
  actionStates: Record<string, StoryRuntimeActionState>
  onStepAction: (chapter: StoryChapterItem, step: StoryRuntimeStepItem) => void
}) {
  const steps = getRuntimeSteps(chapter)
  return (
    <View className='story-runtime-flow'>
      <Text className='story-runtime-flow__title'>故事互動流程</Text>
      {steps.length ? (
        steps.map((step, stepIndex) => {
          const classification = classifyStoryRuntimeStep(step)
          const category = classification.category
          const stateKey = getStoryRuntimeActionKey({
            chapterId: chapter.id,
            step,
            eventType: classification.eventType,
            fallbackIndex: stepIndex,
          })
          const actionState = actionStates[stateKey]
          const disabled = classification.stateful && !storyModeSession?.sessionId
          const stateText = actionState ? getActionStateText(actionState) : disabled ? '請先開始故事模式' : ''
          const stateStatus = actionState?.status || (disabled ? 'blocked' : undefined)
          return (
            <View
              key={step.id || step.stepCode || stepIndex}
              className={`story-runtime-action-card story-runtime-action-card--${category} ${disabled ? 'story-runtime-action-card--disabled' : 'story-runtime-action-card--active'} ${category === 'unsupported' ? 'story-runtime-action-card--unsupported' : ''}`}
            >
              <View className='story-runtime-step__top'>
                <Text className='story-runtime-step__badge'>{classification.label}</Text>
                {step.requiredForCompletion ? (
                  <Text className='story-runtime-step__badge story-runtime-step__badge--required'>主線必做</Text>
                ) : null}
                {step.explorationWeightLevel ? (
                  <Text className='story-runtime-step__badge story-runtime-step__badge--weight'>
                    探索權重：{step.explorationWeightLevel}
                  </Text>
                ) : null}
                {category === 'unsupported' ? (
                  <Text className='story-runtime-step__badge story-runtime-step__badge--pending'>稍後開放</Text>
                ) : null}
                {stateText ? (
                  <Text className={`story-runtime-state-chip story-runtime-state-chip--${stateStatus}`}>
                    {stateText}
                  </Text>
                ) : null}
              </View>
              <Text className='story-runtime-step__name'>{step.name || `互動 ${stepIndex + 1}`}</Text>
              {step.description ? <Text className='story-runtime-step__desc'>{step.description}</Text> : null}
              {step.travelerActionLabel ? (
                <Text className='story-runtime-step__action'>{step.travelerActionLabel}</Text>
              ) : null}
              {renderRuntimeStepMedia(step)}
              {category === 'unsupported' ? (
                <View className='story-runtime-step__unsupportedCopy'>
                  <Text className='story-runtime-step__unsupportedText'>
                    此互動玩法已由後台配置，將在後續小程序玩法版本中開放。
                  </Text>
                  <Text className='story-runtime-step__debug'>
                    玩法類型：{step.stepType || step.template?.templateType || '未指定'}
                  </Text>
                </View>
              ) : null}
              {disabled ? (
                <Text className='story-runtime-action-card__status'>開始故事模式後可同步此互動。</Text>
              ) : null}
              {renderActionFeedback(actionState)}
              <Button
                className='story-runtime-action-card__button'
                disabled={actionState?.status === 'syncing'}
                onClick={() => onStepAction(chapter, step)}
              >
                {classification.buttonText}
              </Button>
            </View>
          )
        })
      ) : (
        <Text className='story-runtime-flow__empty'>此章節暫未配置互動流程。</Text>
      )}
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
  const [runtimeLoading, setRuntimeLoading] = useState(false)
  const [runtimeAlert, setRuntimeAlert] = useState('')
  const [storyModeSession, setStoryModeSession] = useState<StoryModeSessionState | null>(() => (
    initialStoryId ? getActiveStoryModeSession(initialStoryId) : getActiveStoryModeSession()
  ))
  const [storyModeBusy, setStoryModeBusy] = useState(false)
  const [explorationSummary, setExplorationSummary] = useState<StoryExplorationSummaryItem | null>(null)
  const [actionStates, setActionStates] = useState<Record<string, StoryRuntimeActionState>>({})
  const reportedContentEventsRef = useRef<Set<string>>(new Set())
  const reportedUnsupportedEventsRef = useRef<Set<string>>(new Set())

  const unlockedStories = useMemo(() => stories.filter((story) => !story.locked), [stories])
  const lockedStories = useMemo(() => stories.filter((story) => story.locked), [stories])
  const activeStory = useMemo(
    () => stories.find((story) => story.id === expandedStoryId) || unlockedStories[0] || stories[0],
    [stories, expandedStoryId, unlockedStories],
  )
  const firstUnlockedChapter = useMemo(
    () => activeStory?.chapters?.find((chapter) => !chapter.locked) || activeStory?.chapters?.[0] || null,
    [activeStory?.chapters],
  )
  const routeContext = useMemo(
    () => (activeStory
      ? buildStoryModeRouteContext(activeStory, expandedChapterId || undefined, storyModeSession?.sessionId)
      : null),
    [activeStory, expandedChapterId, storyModeSession?.sessionId],
  )
  const routeDestination = useMemo(() => resolveStoryRouteDestination(routeContext), [routeContext])
  const currentRouteChapter = routeContext?.chapters.find((chapter) => chapter.status === 'current')
    || routeContext?.chapters.find((chapter) => chapter.status !== 'locked')
  const nextRouteChapter = routeContext?.chapters
    .filter((chapter) => chapter.status !== 'locked')
    .find((chapter) => (chapter.chapterOrder || 0) > (currentRouteChapter?.chapterOrder || 0))

  const syncActiveStoryRuntime = async (storyId: number, shouldCommit = () => true) => {
    if (shouldCommit()) {
      setRuntimeLoading(true)
      setRuntimeAlert('')
    }
    try {
      const syncedStory = await refreshStorylineRuntime(storyId)
      if (!shouldCommit()) {
        return
      }
      const nextStories = getStorylines()
      setStories(nextStories)
      const nextStory = syncedStory || nextStories.find((story) => story.id === storyId)
      if (nextStory?.runtimeSource === 'fallback') {
        setRuntimeAlert('故事資料暫時未能同步，已顯示本機快取內容。')
      }
    } catch (error) {
      console.warn('Failed to synchronize story runtime.', error)
      if (!shouldCommit()) {
        return
      }
      setStories(getStorylines())
      setRuntimeAlert('故事資料暫時未能同步，已顯示本機快取內容。')
    } finally {
      if (shouldCommit()) {
        setRuntimeLoading(false)
      }
    }
  }

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
    if (!activeStory?.id || activeStory.locked) {
      return
    }

    let cancelled = false

    const syncRuntime = async () => {
      try {
        await syncActiveStoryRuntime(activeStory.id, () => !cancelled)
      } catch (error) {
        if (!cancelled) {
          setRuntimeAlert('故事資料暫時未能同步，已顯示本機快取內容。')
        }
      }
    }

    void syncRuntime()

    return () => {
      cancelled = true
    }
  }, [activeStory?.id, activeStory?.locked])

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

  useEffect(() => {
    if (!activeStory?.id) {
      setStoryModeSession(null)
      setExplorationSummary(null)
      return
    }
    const storedSession = getActiveStoryModeSession(activeStory.id)
    setStoryModeSession(storedSession)
    void refreshStoryExplorationSummary(activeStory.id)
      .then(setExplorationSummary)
      .catch(() => setExplorationSummary(null))
  }, [activeStory?.id])

  const refreshExploration = async () => {
    if (!activeStory?.id) {
      return
    }
    try {
      setExplorationSummary(await refreshStoryExplorationSummary(activeStory.id))
    } catch (error) {
      console.warn('Failed to refresh story exploration summary.', error)
    }
  }

  const reportStoryEvent = async (input: {
    eventType: StoryRuntimeEventType
    sessionId?: string
    clientEventId?: string
    idempotencyScope?: string
    chapterId?: number
    stepId?: number
    blockId?: number
    elementCode?: string
    elementId?: number
    mediaKind?: string
    payload?: Record<string, unknown>
  }) => {
    if (!activeStory?.id) {
      return undefined
    }
    return recordStoryRuntimeEvent({
      storylineId: activeStory.id,
      sessionId: input.sessionId || storyModeSession?.sessionId,
      ...input,
    })
  }

  useEffect(() => {
    if (!activeStory?.id || !expandedChapterId) {
      return
    }
    const chapter = activeStory.chapters?.find((item) => item.id === expandedChapterId)
    if (!chapter || chapter.locked) {
      return
    }

    ;(chapter.contentBlocks || []).forEach((block) => {
      const key = `${activeStory.id}:${chapter.id}:block:${block.id}:content_viewed`
      if (reportedContentEventsRef.current.has(key)) {
        return
      }
      reportedContentEventsRef.current.add(key)
      void reportStoryEvent({
        eventType: 'content_viewed',
        chapterId: chapter.id,
        blockId: block.id,
        elementCode: block.code || `story_block_${block.id}`,
        elementId: block.id,
        payload: {
          source: 'story_page',
          blockType: block.blockType,
        },
      }).catch((error) => {
        console.warn('Failed to report story content view.', error)
      })
    })
  }, [activeStory, expandedChapterId])

  const handleStorySelect = (story: StorylineItem) => {
    if (story.locked) {
      Taro.showToast({ title: story.unlockHint || '此故事線仍未解鎖', icon: 'none' })
      return
    }
    void recordStoryRuntimeEvent({
      storylineId: story.id,
      eventType: 'story_opened',
      elementCode: `storyline_${story.id}`,
      elementId: story.id,
    }).catch((error) => {
      console.warn('Failed to report story selection.', error)
    })
    setExpandedStoryId(story.id)
    const nextChapter = story.chapters?.find((chapter) => !chapter.locked) || story.chapters?.[0]
    setExpandedChapterId(nextChapter?.id || null)
  }

  const handleToggleChapter = (chapter: StoryChapterItem) => {
    if (chapter.locked) {
      Taro.showToast({ title: '請先完成前置章節或條件', icon: 'none' })
      return
    }
    const willExpand = expandedChapterId !== chapter.id
    setExpandedChapterId((prev) => (prev === chapter.id ? null : chapter.id))
    if (willExpand) {
      void reportStoryEvent({
        eventType: 'chapter_started',
        chapterId: chapter.id,
        elementCode: chapter.anchorTargetCode || `story_chapter_${chapter.id}`,
        elementId: chapter.anchorTargetId || chapter.id,
        idempotencyScope: `chapter:${chapter.id}`,
      }).catch((error) => {
        if (!isAuthRequiredError(error)) {
          console.warn('Failed to report chapter start.', error)
        }
      })
    }
  }

  const handleSelectRouteChapter = (chapter: StoryModeRouteChapter) => {
    if (chapter.status === 'locked') {
      Taro.showToast({ title: '請先完成前置章節或條件', icon: 'none' })
      return
    }
    const willBecomeCurrent = expandedChapterId !== chapter.chapterId
    setExpandedChapterId(chapter.chapterId)
    if (willBecomeCurrent) {
      void reportStoryEvent({
        eventType: 'chapter_started',
        chapterId: chapter.chapterId,
        elementCode: chapter.anchorTargetCode || `story_chapter_${chapter.chapterId}`,
        elementId: chapter.anchorTargetId || chapter.chapterId,
        idempotencyScope: `route:${chapter.chapterId}`,
      }).catch((error) => {
        if (!isAuthRequiredError(error)) {
          console.warn('Failed to report route chapter start.', error)
        }
      })
    }
  }

  const handleOpenStoryMap = (chapter: StoryChapterItem) => {
    if (!activeStory?.id) {
      return
    }
    const context = buildStoryModeRouteContext(activeStory, chapter.id, storyModeSession?.sessionId)
    saveStoryModeRouteContext(context)
    void Taro.switchTab({ url: '/pages/map/index' })
    Taro.showToast({ title: '已切換至故事地圖', icon: 'success' })
  }

  const handleStartStoryMode = async () => {
    if (!activeStory?.id) {
      return
    }
    const chapterId = expandedChapterId || firstUnlockedChapter?.id
    setStoryModeBusy(true)
    try {
      const session = await startStoryModeSession(activeStory.id, chapterId)
      setStoryModeSession(session)
      await recordStoryRuntimeEvent({
        storylineId: activeStory.id,
        eventType: 'story_opened',
        chapterId,
        sessionId: session.sessionId,
        elementCode: activeStory.code || `storyline_${activeStory.id}`,
        elementId: activeStory.id,
        idempotencyScope: 'start',
      })
      await refreshExploration()
      Taro.showToast({ title: '故事模式已開始', icon: 'success' })
    } catch (error) {
      Taro.showToast({
        title: isAuthRequiredError(error) ? '請先登入後開始故事模式' : '進度同步暫時失敗，可稍後重試',
        icon: 'none',
      })
    } finally {
      setStoryModeBusy(false)
    }
  }

  const handleExitStoryMode = async () => {
    if (!activeStory?.id) {
      return
    }
    setStoryModeBusy(true)
    try {
      await reportStoryEvent({
        eventType: 'story_session_exit',
        sessionId: storyModeSession?.sessionId,
        idempotencyScope: 'exit',
      })
      const exited = await exitStoryModeSession(activeStory.id)
      setStoryModeSession(exited?.active ? exited : null)
      setActionStates({})
      Taro.showToast({ title: '已離開故事模式，已獲得的探索與獎勵紀錄會保留', icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: '進度同步暫時失敗，可稍後重試', icon: 'none' })
    } finally {
      setStoryModeBusy(false)
    }
  }

  const handleRuntimeStepAction = async (chapter: StoryChapterItem, step: StoryRuntimeStepItem) => {
    if (!activeStory?.id) {
      return
    }
    const classification = classifyStoryRuntimeStep(step)
    const eventPayload = buildStoryRuntimeEventPayload({
      storylineId: activeStory.id,
      sessionId: storyModeSession?.sessionId,
      chapterId: chapter.id,
      step,
      classification,
    })
    const stateKey = getStoryRuntimeActionKey({
      chapterId: chapter.id,
      step,
      eventType: classification.eventType,
    })
    if (classification.unsupported) {
      setActionStates((previous) => ({
        ...previous,
        [stateKey]: {
          status: 'unsupported',
          title: '玩法稍後開放',
          message: '此玩法已配置，將於後續小程序玩法版本開放。',
          updatedAt: new Date().toISOString(),
        },
      }))
      const unsupportedKey = `${activeStory.id}:${storyModeSession?.sessionId || 'read'}:${chapter.id}:${step.stepCode || step.id}:unsupported_viewed`
      if (reportedUnsupportedEventsRef.current.has(unsupportedKey)) {
        return
      }
      reportedUnsupportedEventsRef.current.add(unsupportedKey)
      try {
        await reportStoryEvent({
          eventType: 'unsupported_viewed',
          chapterId: chapter.id,
          stepId: step.id,
          elementCode: eventPayload.elementCode,
          elementId: eventPayload.elementId,
          sessionId: storyModeSession?.sessionId,
          idempotencyScope: `unsupported:${step.stepCode || step.id}`,
          payload: {
            ...eventPayload.payload,
            unsupportedReason: step.unsupportedReason,
          },
        })
      } catch (error) {
        console.warn('Failed to report unsupported step view.', error)
      }
      return
    }
    if (classification.stateful && !storyModeSession?.sessionId) {
      setActionStates((previous) => ({
        ...previous,
        [stateKey]: {
          status: 'blocked',
          title: '請先開始故事模式',
          message: '匿名或只讀瀏覽可以查看故事內容，但拾取、打卡、任務與獎勵需要先開始故事模式。',
          updatedAt: new Date().toISOString(),
        },
      }))
      Taro.showToast({ title: '請先開始故事模式', icon: 'none' })
      return
    }

    setActionStates((previous) => ({
      ...previous,
      [stateKey]: {
        status: 'syncing',
        title: '同步中',
        message: '正在把互動事件寫入後端。',
        updatedAt: new Date().toISOString(),
      },
    }))
    try {
      const response = await reportStoryEvent({
        eventType: eventPayload.eventType,
        chapterId: chapter.id,
        stepId: step.id,
        elementCode: eventPayload.elementCode,
        elementId: eventPayload.elementId,
        idempotencyScope: eventPayload.idempotencyScope,
        payload: eventPayload.payload,
      })
      setActionStates((previous) => ({
        ...previous,
        [stateKey]: resolveStoryRuntimeFeedback(response, {
          title: classification.eventType === 'click_interacted' ? '互動已同步' : undefined,
        }),
      }))
      if (response?.currentChapterId) {
        setExpandedChapterId(response.currentChapterId)
        const storedSession = getActiveStoryModeSession(activeStory.id)
        setStoryModeSession(storedSession)
      }
      if (response?.explorationSummary) {
        setExplorationSummary(mapStoryExplorationSummary(response.explorationSummary))
      } else {
        await refreshExploration()
      }
    } catch (error) {
      setActionStates((previous) => ({
        ...previous,
        [stateKey]: {
          status: 'failed',
          title: '同步失敗',
          message: '服務器開小差了，請稍後重試。',
          updatedAt: new Date().toISOString(),
        },
      }))
      Taro.showToast({ title: '進度同步暫時失敗，可稍後重試', icon: 'none' })
    }
  }

  const handleMediaCompleted = async (
    chapter: StoryChapterItem,
    block: NonNullable<StoryChapterItem['contentBlocks']>[number],
    asset: StoryMediaAssetItem,
    mediaKind: 'audio' | 'video',
  ) => {
    try {
      await reportStoryEvent({
        eventType: 'media_completed',
        chapterId: chapter.id,
        blockId: block.id,
        elementCode: block.code || asset.usageHint?.materialItemKey || `story_block_${block.id}`,
        elementId: block.id,
        mediaKind,
        idempotencyScope: `${mediaKind}:${asset.id || block.id}`,
        payload: {
          mediaKind,
          assetId: asset.id,
          availability: asset.availability,
          durationMs: asset.durationMs,
        },
      })
      if (storyModeSession?.sessionId) {
        await refreshExploration()
      }
    } catch (error) {
      if (storyModeSession?.sessionId) {
        Taro.showToast({ title: '進度同步暫時失敗，可稍後重試', icon: 'none' })
      }
    }
  }

  const handleUnavailableMediaViewed = (
    chapter: StoryChapterItem,
    block: NonNullable<StoryChapterItem['contentBlocks']>[number],
    asset: StoryMediaAssetItem | null | undefined,
    reason: string,
  ) => {
    const key = `${activeStory?.id || 'story'}:${chapter.id}:${block.id}:${asset?.id || 'missing'}:${reason}`
    if (reportedUnsupportedEventsRef.current.has(key)) {
      return
    }
    reportedUnsupportedEventsRef.current.add(key)
    void reportStoryEvent({
      eventType: 'unsupported_viewed',
      chapterId: chapter.id,
      blockId: block.id,
      elementCode: block.code || asset?.usageHint?.materialItemKey || `story_block_${block.id}`,
      elementId: block.id,
      idempotencyScope: `media-unavailable:${block.id}:${asset?.id || 'missing'}`,
      payload: {
        reason,
        availability: asset?.availability,
        blockType: block.blockType,
      },
    }).catch((error) => console.warn('Failed to report unavailable media view.', error))
  }

  const runtimeStatusText = runtimeLoading
    ? '故事資料同步中...'
    : activeStory?.runtimeStatusText || (activeStory?.runtimeSource === 'live' ? '即時故事資料已同步' : '使用本機快取')
  const runtimeStatusClass = !runtimeLoading && activeStory?.runtimeSource === 'live'
    ? 'story-runtime-status--live'
    : 'story-runtime-status--fallback'
  const runtimeSourceLabel = activeStory?.runtimeSource === 'live' ? '資料源：即時後端' : '資料源：本機快取'
  const storyIdentifier = activeStory?.code || activeStory?.id
  const diagnosticsChapterCount = activeStory?.totalChapters || activeStory?.chapters?.length || 0

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
              <View className='story-runtime-summary'>
                <Text className={`story-runtime-status ${runtimeStatusClass}`}>
                  {runtimeStatusText}
                </Text>
                {runtimeAlert ? <Text className='story-runtime-alert'>{runtimeAlert}</Text> : null}
                {STORY_RUNTIME_DIAGNOSTICS_ENABLED ? (
                  <View className='story-runtime-diagnostics'>
                    <Text className='story-runtime-diagnostics__item'>{runtimeSourceLabel}</Text>
                    <Text className='story-runtime-diagnostics__item'>API：{PUBLIC_API_HOST_LABEL}</Text>
                    <Text className='story-runtime-diagnostics__item'>模式：{RUNTIME_ENV_LABEL}</Text>
                    <Text className='story-runtime-diagnostics__item'>章節：{diagnosticsChapterCount}</Text>
                    <Text className='story-runtime-diagnostics__item'>目前故事：{storyIdentifier}</Text>
                    <Button
                      className='story-runtime-diagnostics__button'
                      loading={runtimeLoading}
                      disabled={runtimeLoading}
                      onClick={() => activeStory?.id && void syncActiveStoryRuntime(activeStory.id)}
                    >
                      重新同步故事資料
                    </Button>
                  </View>
                ) : null}
              </View>

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
                <>
                  <View className='story-mode-panel'>
                    <View className='story-mode-panel__header'>
                      <View>
                        <Text className='story-mode-panel__title'>主線故事模式</Text>
                        <Text className='story-mode-panel__hint'>
                          {storyModeSession?.active
                            ? '故事模式進行中'
                            : '你可以先閱讀故事內容；開始故事模式後，章節、媒體與互動進度會同步到後端。'}
                        </Text>
                      </View>
                      <Text className={`story-mode-panel__status ${storyModeSession?.active ? 'active' : ''}`}>
                        {storyModeSession?.active ? '故事模式進行中' : '只讀瀏覽'}
                      </Text>
                    </View>
                    <View className='story-mode-panel__progress'>
                      <Text className='story-mode-panel__progressLabel'>故事探索進度</Text>
                      <Text className='story-mode-panel__progressValue'>
                        {explorationSummary?.progressPercent !== undefined ? `${Math.round(explorationSummary.progressPercent)}%` : '尚未同步'}
                      </Text>
                    </View>
                    <Text className='story-mode-panel__exitHint'>
                      離開只會清除本次路線強調，已獲得的探索、拾取與獎勵紀錄會保留。
                    </Text>
                    <View className='story-mode-panel__actions'>
                      {!storyModeSession?.active ? (
                        <Button className='story-mode-panel__primary' loading={storyModeBusy} onClick={handleStartStoryMode}>
                          開始故事模式
                        </Button>
                      ) : (
                        <Button className='story-mode-panel__secondary' loading={storyModeBusy} onClick={handleExitStoryMode}>
                          離開故事模式
                        </Button>
                      )}
                    </View>
                  </View>

                  {routeContext ? (
                    <View className='story-route-panel'>
                      <View className='story-route-panel__header'>
                        <View>
                          <Text className='story-route-panel__eyebrow'>主線路線</Text>
                          <Text className='story-route-panel__title'>
                            目前章節：{currentRouteChapter?.title || '尚未選定'}
                          </Text>
                        </View>
                        <Text className='story-route-panel__status'>支線稍後開放</Text>
                      </View>
                      <View className='story-route-panel__metaGrid'>
                        <View className='story-route-panel__metaItem'>
                          <Text className='story-route-panel__metaLabel'>下一站</Text>
                          <Text className='story-route-panel__metaValue'>
                            {nextRouteChapter?.locationName || nextRouteChapter?.title || '已到達目前終點'}
                          </Text>
                        </View>
                        <View className='story-route-panel__metaItem'>
                          <Text className='story-route-panel__metaLabel'>目前目的地</Text>
                          <Text className='story-route-panel__metaValue'>
                            {currentRouteChapter?.locationName || routeDestination?.name || '尚未配置'}
                          </Text>
                        </View>
                        <View className='story-route-panel__metaItem'>
                          <Text className='story-route-panel__metaLabel'>錨點類型</Text>
                          <Text className='story-route-panel__metaValue'>{currentRouteChapter?.anchorType || '未指定'}</Text>
                        </View>
                        <View className='story-route-panel__metaItem'>
                          <Text className='story-route-panel__metaLabel'>目的地代碼</Text>
                          <Text className='story-route-panel__metaValue'>
                            {currentRouteChapter?.anchorTargetCode || currentRouteChapter?.anchorTargetId || '未指定'}
                          </Text>
                        </View>
                      </View>
                      {!routeDestination ? (
                        <Text className='story-route-panel__fallback'>
                          目前未配置精準路線座標，先依章節順序前往下一站。
                        </Text>
                      ) : null}
                      <ScrollView className='story-route-strip' scrollX>
                        {(routeContext.chapters || []).map((chapter) => (
                          <View
                            key={chapter.chapterId}
                            className={`story-route-step story-route-step--${chapter.status}`}
                            onClick={() => handleSelectRouteChapter(chapter)}
                          >
                            <Text className='story-route-step__order'>第 {chapter.chapterOrder} 章</Text>
                            <Text className='story-route-step__title'>{chapter.title}</Text>
                            <Text className='story-route-step__place'>{chapter.locationName || '手動錨點'}</Text>
                            <Text className='story-route-step__status'>{getRouteStatusText(chapter.status)}</Text>
                          </View>
                        ))}
                      </ScrollView>
                    </View>
                  ) : null}

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

                              {renderRuntimeFlow({
                                chapter,
                                storyModeSession,
                                actionStates,
                                onStepAction: handleRuntimeStepAction,
                              })}

                              <StoryContentBlockRenderer
                                blocks={chapter.contentBlocks}
                                onMediaCompleted={(block, asset, mediaKind) => {
                                  void handleMediaCompleted(chapter, block, asset, mediaKind)
                                }}
                                onUnavailableMediaViewed={(block, asset, reason) => {
                                  handleUnavailableMediaViewed(chapter, block, asset, reason)
                                }}
                              />

                              <View className='chapter-card__actions'>
                                <Button className='chapter-card__primary' onClick={() => handleOpenStoryMap(chapter)}>
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
                </>
              )}
            </View>
          </View>
        ) : null}

        <View className='bottom-spacer' />
      </View>
    </PageShell>
  )
}
