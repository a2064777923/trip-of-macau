import { useEffect, useMemo, useRef, useState } from 'react'
import { Button, ScrollView, Text, View, Video } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import LottieAssetPlayer from '../../components/LottieAssetPlayer'
import PageShell from '../../components/PageShell'
import SafeStoryImage from '../../components/SafeStoryImage'
import {
  buildStoryModeRouteContext,
  clearStaleStorylineRuntimeSelection,
  exitStoryModeSession,
  getActiveStoryModeSession,
  getStorylines,
  hasActiveSessionToken,
  isAuthRequiredError,
  isDevBypassAvailable,
  isStorylineUnavailableError,
  loadGameState,
  loginWithDevBypass,
  mapStoryExplorationSummary,
  recordStoryRuntimeEvent,
  refreshPublicContent,
  refreshStorylineCatalog,
  refreshStoryExplorationSummary,
  refreshStorylineRuntime,
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
  StoryModeSessionState,
  StoryRuntimeActionState,
  StoryRuntimeStepItem,
  StorylineItem,
} from '../../types/game'
import './index.scss'

type StoryViewMode = 'intro' | 'chapters' | 'playing'

const FLAGSHIP_STORY_CODE = 'east_west_war_and_coexistence'
const STORYLINE_NAME_KEYWORD = '東西方文明的戰火與共生'
const LEGACY_DUPLICATE_STORY_CODE = 'macau_fire_route'
const STALE_STORY_MESSAGE = '這條故事線已下線或暫時不可用，請返回故事列表選擇最新路線。'
const FLAGSHIP_INTRO_COPY = '你是一名穿越時空的「濠江歷史見證者」，追隨一枚殘缺的海防銅鏡，踏遍澳門的軍事要塞。從明朝葡人登岸的武裝衝突，到明清官軍築台禦敵，再到近代列強環伺的邊境戰火，最終見證戰火落幕、東西方從對抗走向共生。'
const FORBIDDEN_TRAVELER_TEXT_PATTERN = /章節工作台|互動流程|體驗流程|流程配置|配置詳情|綁定地圖|綁定子地圖|錨點類型|後台|后台|運營|运营|開發|开发|runtime|JSON/gi
const DEFAULT_POI_EXPERIENCE_STEP_CODES = new Set([
  'tap_intro',
  'start_route_guidance',
  'release_checkin_tasks',
  'pickup_side_clues',
  'hidden_dwell_achievement',
  'completion_reward_title',
])
const HIDDEN_INTERNAL_STEP_CODES = new Set(['disable_default_arrival_media'])

function hasText(value?: string | null): value is string {
  return typeof value === 'string' && value.trim().length > 0
}

function cleanTravelerText(value?: string | null, fallback = '') {
  const text = (value || fallback || '').trim()
  return text
    .replace(FORBIDDEN_TRAVELER_TEXT_PATTERN, '')
    .replace(/綁定/g, '')
    .replace(/POI/gi, '探索點')
    .replace(/anchor/gi, '探索點')
    .replace(/\s{2,}/g, ' ')
    .trim()
}

function pickStoryTitle(story?: StorylineItem | null) {
  if (!story) {
    return ''
  }
  return story.name.includes(STORYLINE_NAME_KEYWORD)
    ? STORYLINE_NAME_KEYWORD
    : cleanTravelerText(story.name)
}

function isPreferredFlagshipStory(story?: StorylineItem | null) {
  return story?.code === FLAGSHIP_STORY_CODE
}

function isExactFlagshipNameStory(story?: StorylineItem | null) {
  return story?.code !== LEGACY_DUPLICATE_STORY_CODE && story?.name === STORYLINE_NAME_KEYWORD
}

function pickPreferredStory(stories: StorylineItem[], preferredStoryId?: number) {
  return (preferredStoryId ? stories.find((story) => story.id === preferredStoryId) : undefined)
    || stories.find(isPreferredFlagshipStory)
    || stories.find(isExactFlagshipNameStory)
    || stories.find((story) => !story.locked)
    || stories[0]
}

async function refreshStorylineCatalogSafely() {
  try {
    await refreshStorylineCatalog()
  } catch (error) {
    console.warn('Failed to reload public story list after stale storyline.', error)
  }
  return getStorylines()
}

function pickStoryIntro(story?: StorylineItem | null) {
  if (!story) {
    return ''
  }
  if (isPreferredFlagshipStory(story) || isExactFlagshipNameStory(story)) {
    return FLAGSHIP_INTRO_COPY
  }
  return cleanTravelerText(story.description)
}

function toStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return []
  }
  return value.map((item) => String(item || '').trim()).filter(Boolean)
}

function getStepTargets(step?: StoryRuntimeStepItem | null) {
  if (!step) {
    return []
  }
  const effect = step.effectConfig || {}
  const condition = step.conditionConfig || {}
  const directTargets = [
    ...toStringArray(effect.requiredNames),
    ...toStringArray(effect.pickupNames),
    ...toStringArray(effect.targetNames),
    ...toStringArray(condition.requiredNames),
    ...toStringArray(condition.pickupNames),
  ]
  if (directTargets.length) {
    return directTargets.slice(0, 4)
  }
  const code = step.stepCode || ''
  if (code === 'main_overlay_collect_3') {
    return ['明朝水師戰船', '媽閣漁民防線', '葡國武裝商船']
  }
  if (code === 'side_pickups') {
    return ['明朝海防銅令牌', '濠江漁民禦敵漁網殘片', '葡人通商納稅契約殘頁']
  }
  if (code === 'hidden_guardian_quiz') {
    return ['收集本章線索', '現場停留', '回答歷史問答']
  }
  return []
}

function getStepRewardHints(step?: StoryRuntimeStepItem | null) {
  if (!step) {
    return []
  }
  const effect = step.effectConfig || {}
  return [
    effect.baseTitle,
    effect.fullCollectionTitle,
    effect.hiddenTitle,
    effect.rewardTitle,
    effect.titleName,
  ].map((item) => String(item || '').trim()).filter(Boolean).slice(0, 3)
}

function getDifficultyText(difficulty?: StorylineItem['difficulty']) {
  switch (difficulty) {
    case 'hard':
      return '深度挑戰'
    case 'medium':
      return '標準旅程'
    default:
      return '輕鬆探索'
  }
}

function getRuntimeSteps(chapter?: StoryChapterItem | null) {
  if (!chapter) {
    return []
  }

  const arrivalStep = buildArrivalStep(chapter)
  const playableSteps = (chapter.runtimeSteps || chapter.runtime?.runtimeSteps || [])
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .filter((step) => isPlayerVisibleRuntimeStep(step))
    .map((step) => normalizePlayerRuntimeStep(step))

  return [arrivalStep, ...playableSteps]
}

function getChapterMediaAsset(chapter?: StoryChapterItem | null) {
  if (!chapter) {
    return null
  }
  if (chapter.primaryMediaAsset?.url || chapter.primaryMediaAsset?.posterUrl || chapter.primaryMediaAsset?.fallbackUrl) {
    return chapter.primaryMediaAsset
  }
  return (chapter.contentBlocks || [])
    .slice()
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((block) => block.primaryAsset || block.attachmentAssets?.find((asset) => !!asset.url || !!asset.posterUrl || !!asset.fallbackUrl))
    .find((asset) => !!asset?.url || !!asset?.posterUrl || !!asset?.fallbackUrl) || null
}

function getChapterMediaUrl(chapter?: StoryChapterItem | null) {
  const asset = getChapterMediaAsset(chapter)
  return asset?.url || asset?.fallbackUrl || asset?.posterUrl || chapter?.primaryMediaUrl || ''
}

function resolveChapterLocation(chapter?: StoryChapterItem | null) {
  return cleanTravelerText(chapter?.locationName, '故事地點')
}

function buildArrivalStep(chapter: StoryChapterItem): StoryRuntimeStepItem {
  const locationName = resolveChapterLocation(chapter)
  return {
    id: -chapter.id,
    stepCode: `chapter_${chapter.id}_arrival`,
    stepType: 'chapter_arrival',
    displayCategory: 'location',
    displayCategoryLabel: '前往現場',
    eventType: 'proximity_reached',
    elementCode: chapter.anchorTargetCode || `story_chapter_${chapter.id}_arrival`,
    elementId: chapter.anchorTargetId || chapter.id,
    name: `前往${locationName}`,
    description: `先前往「${locationName}」。靠近指定範圍後，主線劇情會接上。`,
    travelerActionLabel: '我已到達附近',
    triggerType: 'proximity',
    requiredForCompletion: true,
    sortOrder: -100,
  }
}

function isArrivalStep(step?: StoryRuntimeStepItem | null) {
  return !!step?.stepCode?.startsWith('chapter_') && step.stepCode.endsWith('_arrival')
}

function isStoryMediaStep(step?: StoryRuntimeStepItem | null) {
  const text = [
    step?.stepCode,
    step?.stepType,
    step?.displayCategory,
    step?.displayCategoryLabel,
  ].filter(Boolean).join(' ').toLowerCase()
  return !!step?.mediaAsset || /story_media|fullscreen_media|core_media|劇情播放/.test(text)
}

function isPlayerVisibleRuntimeStep(step: StoryRuntimeStepItem) {
  const code = step.stepCode || ''
  if (HIDDEN_INTERNAL_STEP_CODES.has(code) || step.stepType === 'override') {
    return false
  }
  if (DEFAULT_POI_EXPERIENCE_STEP_CODES.has(code)) {
    return false
  }
  if (/關閉|後台|后台|工作台|配置詳情|流程配置/.test(`${step.name || ''}${step.description || ''}`)) {
    return false
  }
  if (isStoryMediaStep(step)) {
    return true
  }
  return /^(main_|side_|hidden_|chapter_)/.test(code)
    || /overlay|pickup|challenge|reward|collect/.test(`${step.stepType || ''} ${step.displayCategory || ''}`.toLowerCase())
}

function normalizePlayerRuntimeStep(step: StoryRuntimeStepItem): StoryRuntimeStepItem {
  if (!isStoryMediaStep(step)) {
    return step
  }
  return {
    ...step,
    unsupported: false,
    eventType: step.eventType || 'content_read',
    travelerActionLabel: step.travelerActionLabel || '看完劇情',
  }
}

function resolveChapterOrder(story?: StorylineItem | null, chapter?: StoryChapterItem | null) {
  if (!story || !chapter) {
    return 1
  }
  const index = (story.chapters || []).findIndex((item) => item.id === chapter.id)
  return index >= 0 ? index + 1 : 1
}

function resolveCurrentStepIndex(
  chapter: StoryChapterItem | null,
  actionStates: Record<string, StoryRuntimeActionState>,
) {
  const steps = getRuntimeSteps(chapter)
  if (!steps.length) {
    return 0
  }
  const pendingIndex = steps.findIndex((step, index) => {
    const classification = classifyStoryRuntimeStep(step)
    const key = getStoryRuntimeActionKey({
      chapterId: chapter!.id,
      step,
      eventType: classification.eventType,
      fallbackIndex: index,
    })
    const status = actionStates[key]?.status
    return status !== 'synced' && status !== 'already_synced'
  })
  return pendingIndex >= 0 ? pendingIndex : steps.length - 1
}

function getStepInstruction(step?: StoryRuntimeStepItem | null, chapter?: StoryChapterItem | null) {
  if (!step) {
    return cleanTravelerText(chapter?.detail || chapter?.summary, '閱讀本章故事後，前往現場觸發下一段劇情。')
  }
  const code = step.stepCode || ''
  if (isArrivalStep(step)) {
    return `先前往「${resolveChapterLocation(chapter)}」。抵達附近後，主線劇情、聲音與地圖線索會按順序展開。`
  }
  if (code === 'chapter_core_media') {
    return cleanTravelerText(chapter?.detail, '抵達附近後觀看主線劇情。劇情結束後，地圖上會亮起下一組線索。')
  }
  if (code === 'main_overlay_collect_3') {
    return '地圖上會出現「明朝水師戰船」「媽閣漁民防線」「葡國武裝商船」。依次點擊三個線索，完成本章主線任務。'
  }
  if (code === 'side_pickups') {
    return '主線完成後，可繼續尋找明朝海防銅令牌、漁網殘片與通商契約殘頁。找到後點擊拾取，線索會存入旅程紀錄。'
  }
  if (code === 'hidden_guardian_quiz') {
    return cleanTravelerText(step.description, '收集本章線索並在現場停留後，隱藏問答挑戰會打開。')
  }
  const classification = classifyStoryRuntimeStep(step)
  switch (classification.category) {
    case 'location':
      return `前往「${resolveChapterLocation(chapter)}」。靠近指定範圍後，下一段劇情會自動接上。`
    case 'pickup':
      return cleanTravelerText(step.description, '地圖上會浮現線索，找到後點擊拾取。')
    case 'challenge':
      return cleanTravelerText(step.description, '完成前置線索後，隱藏挑戰會打開。')
    case 'reward':
      return cleanTravelerText(step.description, '完成本章後，稱號、徽章與收集物會保存到你的旅程紀錄。')
    default:
      return cleanTravelerText(step.description || step.travelerActionLabel || step.name, '依照當前提示完成這一步。')
  }
}

function getStepPrompt(step?: StoryRuntimeStepItem | null, chapter?: StoryChapterItem | null) {
  if (!step) {
    return '先閱讀本章開場。'
  }
  const code = step.stepCode || ''
  if (isArrivalStep(step)) {
    return `打開故事地圖，前往「${resolveChapterLocation(chapter)}」。`
  }
  if (code === 'chapter_core_media' || isStoryMediaStep(step)) {
    return '觀看這段劇情後再繼續。'
  }
  if (code === 'main_overlay_collect_3') {
    return '本輪只需要找齊三個亮起的主線線索。'
  }
  if (code === 'side_pickups') {
    return '主線完成後，再決定是否留下來找支線拾取物。'
  }
  if (code === 'hidden_guardian_quiz') {
    return '符合條件後，隱藏挑戰才會打開。'
  }
  return '完成當前提示即可推進下一步。'
}

function getStepTitle(step?: StoryRuntimeStepItem | null, chapter?: StoryChapterItem | null) {
  if (!step) {
    return '閱讀本章劇情'
  }
  const code = step.stepCode || ''
  if (isArrivalStep(step)) {
    return `前往${resolveChapterLocation(chapter)}`
  }
  if (code === 'chapter_core_media') {
    return '核心歷史劇情'
  }
  if (code === 'main_overlay_collect_3') {
    return '收集三個主線線索'
  }
  if (code === 'side_pickups') {
    return '尋找支線拾取物'
  }
  if (code === 'hidden_guardian_quiz') {
    return '隱藏挑戰：鏡海守護者'
  }
  return cleanTravelerText(step.travelerActionLabel || step.name, '完成當前一步')
}

function getStepStageLabel(step?: StoryRuntimeStepItem | null) {
  if (!step) {
    return '劇情'
  }
  const code = step.stepCode || ''
  if (isArrivalStep(step)) {
    return '第一步'
  }
  if (code === 'chapter_core_media' || isStoryMediaStep(step)) {
    return '主線劇情'
  }
  if (code.startsWith('main_')) {
    return '主線任務'
  }
  if (code.startsWith('side_')) {
    return '支線探索'
  }
  if (code.startsWith('hidden_')) {
    return '隱藏挑戰'
  }
  return classifyStoryRuntimeStep(step).label
}

function getStepActionLabel(step?: StoryRuntimeStepItem | null) {
  if (!step) {
    return '我已閱讀'
  }
  const code = step.stepCode || ''
  if (isArrivalStep(step)) {
    return '我已到達附近'
  }
  if (code === 'chapter_core_media' || isStoryMediaStep(step)) {
    return '看完劇情，繼續'
  }
  if (code === 'main_overlay_collect_3') {
    return '已收集三個線索'
  }
  if (code === 'side_pickups') {
    return '已拾取線索'
  }
  if (code === 'hidden_guardian_quiz') {
    return '完成挑戰'
  }
  const classification = classifyStoryRuntimeStep(step)
  return classification.buttonText === '記錄進度' ? '完成這一步' : classification.buttonText
}

function shouldAutoOpenCinematic(step?: StoryRuntimeStepItem | null) {
  return !!step && !isArrivalStep(step) && isStoryMediaStep(step)
}

function shouldShowMediaTeaser(step?: StoryRuntimeStepItem | null) {
  return shouldAutoOpenCinematic(step)
}

function shouldShowDetailedInstruction(step?: StoryRuntimeStepItem | null) {
  return !!step && !isArrivalStep(step)
}

function getActiveMediaAsset(step?: StoryRuntimeStepItem | null, chapter?: StoryChapterItem | null) {
  if (!step || isArrivalStep(step)) {
    return null
  }
  if (step?.mediaAsset?.url || step?.mediaAsset?.posterUrl || step?.mediaAsset?.fallbackUrl) {
    return step.mediaAsset
  }
  return isStoryMediaStep(step) ? getChapterMediaAsset(chapter) : null
}

function resolveStepMediaUrl(step?: StoryRuntimeStepItem | null, chapter?: StoryChapterItem | null) {
  if (!step || !shouldShowMediaTeaser(step)) {
    return ''
  }
  const asset = getActiveMediaAsset(step, chapter)
  return asset?.url || asset?.fallbackUrl || asset?.posterUrl || (isStoryMediaStep(step) ? getChapterMediaUrl(chapter) : '')
}

function getReadableAssetTitle(asset?: { originalFilename?: string } | null, fallback = '劇情素材') {
  const name = asset?.originalFilename?.replace(/\.[^.]+$/, '')
  if (!name) {
    return fallback
  }
  return cleanTravelerText(name.replace(/[-_]/g, ' '), fallback)
}

function renderMediaStage(step?: StoryRuntimeStepItem | null, chapter?: StoryChapterItem | null, title = '劇情素材') {
  const asset = getActiveMediaAsset(step, chapter)
  const url = resolveStepMediaUrl(step, chapter)
  if (!asset || !url) {
    return (
      <View className='cinematic-stage cinematic-stage--empty'>
        <Text className='cinematic-stage__kicker'>劇情舞台</Text>
        <Text className='cinematic-stage__title'>{title}</Text>
        <Text className='cinematic-stage__desc'>到達現場後，畫面、聲音與地圖線索會按順序展開。</Text>
      </View>
    )
  }
  if (asset.assetKind === 'video') {
    return (
      <View className='cinematic-video-wrap'>
        <Video
          className='cinematic-video'
          src={url}
          controls
          autoplay
          showFullscreenBtn
          showPlayBtn
          objectFit='cover'
        />
        <View className='cinematic-video-caption'>
          <Text className='cinematic-stage__kicker'>主線短片</Text>
          <Text className='cinematic-stage__desc'>看完後，下一組地圖線索會解鎖。</Text>
        </View>
      </View>
    )
  }
  if (asset.assetKind === 'lottie') {
    return (
      <View className='cinematic-lottie'>
        <LottieAssetPlayer asset={asset} lazy={false} height={320} />
      </View>
    )
  }
  if (asset.assetKind === 'image' || asset.assetKind === 'icon') {
    return (
      <SafeStoryImage
        className='cinematic-image'
        imageClassName='cinematic-image__image'
        src={url}
        mode='aspectFill'
        title={getReadableAssetTitle(asset, title)}
        minHeight={320}
      />
    )
  }
  return (
    <View className='cinematic-stage cinematic-stage--audio'>
      <Text className='cinematic-stage__kicker'>聲音線索</Text>
      <Text className='cinematic-stage__title'>{getReadableAssetTitle(asset, title)}</Text>
      <Text className='cinematic-stage__desc'>聲音內容會在故事模式中接上，聽完後繼續前往下一個線索。</Text>
    </View>
  )
}

export default function StoryPage() {
  const router = Taro.getCurrentInstance().router
  const initialStoryId = router?.params?.storyId ? Number(router.params.storyId) : undefined
  const initialChapterId = router?.params?.chapterId ? Number(router.params.chapterId) : undefined
  const [stories, setStories] = useState(() => getStorylines())
  const [activeStoryId, setActiveStoryId] = useState<number | undefined>(initialStoryId)
  const [activeChapterId, setActiveChapterId] = useState<number | null>(initialChapterId || null)
  const [viewMode, setViewMode] = useState<StoryViewMode>(initialChapterId ? 'playing' : 'intro')
  const [loading, setLoading] = useState(false)
  const [loadError, setLoadError] = useState('')
  const [storyModeSession, setStoryModeSession] = useState<StoryModeSessionState | null>(() => (
    initialStoryId ? getActiveStoryModeSession(initialStoryId) : getActiveStoryModeSession()
  ))
  const [storyModeBusy, setStoryModeBusy] = useState(false)
  const [explorationSummary, setExplorationSummary] = useState<StoryExplorationSummaryItem | null>(null)
  const [actionStates, setActionStates] = useState<Record<string, StoryRuntimeActionState>>({})
  const [cinematicOpen, setCinematicOpen] = useState(false)
  const hydrateSeqRef = useRef(0)
  const mountedRef = useRef(false)
  const didInitialHydrateRef = useRef(false)
  const autoOpenedStepRef = useRef('')

  const unlockedStories = useMemo(() => stories.filter((story) => !story.locked), [stories])
  const activeStory = useMemo(() => {
    if (activeStoryId) {
      const matched = stories.find((story) => story.id === activeStoryId)
      if (matched) {
        return matched
      }
    }
    return pickPreferredStory(stories) || unlockedStories[0]
  }, [activeStoryId, stories, unlockedStories])

  const activeChapter = useMemo(() => {
    const chapters = activeStory?.chapters || []
    if (activeChapterId) {
      const matched = chapters.find((chapter) => chapter.id === activeChapterId)
      if (matched) {
        return matched
      }
    }
    return chapters.find((chapter) => !chapter.locked) || chapters[0] || null
  }, [activeChapterId, activeStory])

  const steps = useMemo(() => getRuntimeSteps(activeChapter), [activeChapter])
  const activeStepIndex = useMemo(
    () => resolveCurrentStepIndex(activeChapter, actionStates),
    [activeChapter, actionStates],
  )
  const activeStep = steps[activeStepIndex]
  const routeContext = useMemo(
    () => (activeStory
      ? buildStoryModeRouteContext(activeStory, activeChapter?.id, storyModeSession?.sessionId)
      : null),
    [activeChapter?.id, activeStory, storyModeSession?.sessionId],
  )
  const currentActionKey = activeStep && activeChapter
    ? getStoryRuntimeActionKey({
        chapterId: activeChapter.id,
        step: activeStep,
        eventType: classifyStoryRuntimeStep(activeStep).eventType,
        fallbackIndex: activeStepIndex,
      })
    : ''
  const currentActionState = currentActionKey ? actionStates[currentActionKey] : undefined
  const currentActionBusy = currentActionState?.status === 'syncing'
  const stepTargets = useMemo(() => getStepTargets(activeStep), [activeStep])
  const stepRewardHints = useMemo(() => getStepRewardHints(activeStep), [activeStep])
  const nextChapter = useMemo(() => {
    if (!activeStory || !activeChapter) {
      return null
    }
    const chapters = activeStory.chapters || []
    const index = chapters.findIndex((chapter) => chapter.id === activeChapter.id)
    return index >= 0 ? chapters[index + 1] || null : null
  }, [activeChapter, activeStory])

  const applyStoryCatalog = (nextStories: StorylineItem[], preferredStoryId?: number) => {
    setStories(nextStories)
    if (!nextStories.length) {
      return
    }
    const preferredStory = pickPreferredStory(
      nextStories,
      preferredStoryId || (activeStoryId ? activeStoryId : undefined),
    )
    if (preferredStory) {
      setActiveStoryId(preferredStory.id)
      const next = preferredStory.chapters?.find((chapter) => !chapter.locked) || preferredStory.chapters?.[0]
      if (!activeChapterId && next) {
        setActiveChapterId(next.id)
      }
    }
  }

  const syncActiveStoryRuntime = async (storyId: number, shouldCommit = () => true) => {
    if (shouldCommit()) {
      setLoading(true)
      setLoadError('')
    }
    try {
      await refreshStorylineRuntime(storyId)
      if (!shouldCommit()) {
        return
      }
      applyStoryCatalog(getStorylines(), storyId)
    } catch (error) {
      console.warn('Failed to synchronize story runtime.', error)
      if (shouldCommit()) {
        if (isStorylineUnavailableError(error)) {
          clearStaleStorylineRuntimeSelection(storyId)
          const nextStories = await refreshStorylineCatalogSafely()
          applyStoryCatalog(nextStories)
          setLoadError(STALE_STORY_MESSAGE)
        } else {
          setLoadError('旅程內容暫時未能載入，請稍後重試。')
        }
      }
    } finally {
      if (shouldCommit()) {
        setLoading(false)
      }
    }
  }

  const hydrateStoryContent = async () => {
    const hydrateSeq = hydrateSeqRef.current + 1
    hydrateSeqRef.current = hydrateSeq
    setLoading(true)
    setLoadError('')
    try {
      await refreshPublicContent()
      if (hydrateSeqRef.current !== hydrateSeq) {
        return
      }
      const nextStories = getStorylines()
      applyStoryCatalog(nextStories)
      const preferred = pickPreferredStory(nextStories)
      if (preferred) {
        await syncActiveStoryRuntime(preferred.id, () => hydrateSeqRef.current === hydrateSeq)
      }
    } catch (error) {
      console.warn('Failed to refresh public story content.', error)
      if (hydrateSeqRef.current === hydrateSeq) {
        setLoadError(isStorylineUnavailableError(error) ? STALE_STORY_MESSAGE : '旅程內容暫時未能載入，請稍後重試。')
      }
    } finally {
      if (hydrateSeqRef.current === hydrateSeq) {
        setLoading(false)
      }
    }
  }

  const hydrateOnce = () => {
    if (didInitialHydrateRef.current) {
      return
    }
    didInitialHydrateRef.current = true
    void hydrateStoryContent()
  }

  useEffect(() => {
    mountedRef.current = true
    hydrateOnce()
    return () => {
      mountedRef.current = false
      hydrateSeqRef.current += 1
    }
  }, [])

  useDidShow(() => {
    if (!mountedRef.current) {
      return
    }
    hydrateOnce()
  })

  useEffect(() => {
    if (!activeStory?.id) {
      setStoryModeSession(null)
      setExplorationSummary(null)
      return
    }
    setStoryModeSession(getActiveStoryModeSession(activeStory.id))
    void refreshStoryExplorationSummary(activeStory.id)
      .then(setExplorationSummary)
      .catch(() => setExplorationSummary(null))
  }, [activeStory?.id])

  useEffect(() => {
    const key = currentActionKey || `${activeChapter?.id || 'chapter'}:${activeStepIndex}`
    if (viewMode === 'playing' && shouldAutoOpenCinematic(activeStep) && autoOpenedStepRef.current !== key) {
      autoOpenedStepRef.current = key
      setCinematicOpen(true)
      return
    }
    setCinematicOpen(false)
  }, [activeChapter?.id, activeStep, activeStepIndex, currentActionKey, viewMode])

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

  const handleStorySelect = (story: StorylineItem) => {
    if (story.locked) {
      Taro.showToast({ title: story.unlockHint || '此故事線仍未解鎖', icon: 'none' })
      return
    }
    setActiveStoryId(story.id)
    const next = story.chapters?.find((chapter) => !chapter.locked) || story.chapters?.[0]
    setActiveChapterId(next?.id || null)
    setViewMode('intro')
    setActionStates({})
    void syncActiveStoryRuntime(story.id)
    void recordStoryRuntimeEvent({
      storylineId: story.id,
      eventType: 'story_opened',
      elementCode: story.code || `storyline_${story.id}`,
      elementId: story.id,
    }).catch((error) => console.warn('Failed to report story selection.', error))
  }

  const handleViewChapter = (chapter: StoryChapterItem) => {
    if (chapter.locked) {
      Taro.showToast({ title: '請先完成前置章節', icon: 'none' })
      return
    }
    setActiveChapterId(chapter.id)
    setViewMode('playing')
    setActionStates({})
    if (activeStory) {
      saveStoryModeRouteContext(buildStoryModeRouteContext(activeStory, chapter.id, storyModeSession?.sessionId))
    }
  }

  const handleStartStoryMode = async () => {
    if (!activeStory?.id) {
      return
    }
    const chapterId = activeChapter?.id || activeStory.chapters?.find((chapter) => !chapter.locked)?.id
    setStoryModeBusy(true)
    try {
      if (isDevBypassAvailable() && (!hasActiveSessionToken() || loadGameState().user.authStatus === 'anonymous')) {
        await loginWithDevBypass()
      }
      const session = await startStoryModeSession(activeStory.id, chapterId)
      setStoryModeSession(session)
      if (activeStory && chapterId) {
        saveStoryModeRouteContext(buildStoryModeRouteContext(activeStory, chapterId, session.sessionId))
      }
      setViewMode('playing')
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
    } catch (error) {
      Taro.showToast({
        title: isAuthRequiredError(error) ? '請先登入後開始故事' : '旅程記錄暫時失敗',
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
      const exited = await exitStoryModeSession(activeStory.id)
      setStoryModeSession(exited?.active ? exited : null)
      setViewMode('intro')
      setActionStates({})
      Taro.showToast({ title: '已離開故事，本次路線進度已清除', icon: 'success' })
    } catch (error) {
      Taro.showToast({ title: '暫時未能離開故事', icon: 'none' })
    } finally {
      setStoryModeBusy(false)
    }
  }

  const handleOpenStoryMap = () => {
    if (!activeStory || !activeChapter) {
      return
    }
    saveStoryModeRouteContext(buildStoryModeRouteContext(activeStory, activeChapter.id, storyModeSession?.sessionId))
    void Taro.switchTab({ url: '/pages/map/index' })
  }

  const handleStepAction = async () => {
    if (!activeStory?.id || !activeChapter) {
      return
    }
    if (!activeStep) {
      await recordStoryRuntimeEvent({
        storylineId: activeStory.id,
        sessionId: storyModeSession?.sessionId,
        eventType: 'content_viewed',
        chapterId: activeChapter.id,
        elementCode: `story_chapter_${activeChapter.id}`,
        elementId: activeChapter.id,
        idempotencyScope: `chapter-read:${activeChapter.id}`,
      }).catch((error) => console.warn('Failed to report chapter read.', error))
      return
    }
    const classification = classifyStoryRuntimeStep(activeStep)
    const stateKey = getStoryRuntimeActionKey({
      chapterId: activeChapter.id,
      step: activeStep,
      eventType: classification.eventType,
      fallbackIndex: activeStepIndex,
    })
    if (classification.stateful && !storyModeSession?.sessionId) {
      Taro.showToast({ title: '請先開始故事', icon: 'none' })
      return
    }

    setActionStates((previous) => ({
      ...previous,
        [stateKey]: {
          status: 'syncing',
          title: '記錄中',
          message: '正在保存你的旅程進度。',
          updatedAt: new Date().toISOString(),
        },
    }))

    try {
      const eventPayload = buildStoryRuntimeEventPayload({
        storylineId: activeStory.id,
        sessionId: storyModeSession?.sessionId,
        chapterId: activeChapter.id,
        step: activeStep,
        classification,
      })
      const response = await recordStoryRuntimeEvent({
        storylineId: activeStory.id,
        sessionId: storyModeSession?.sessionId,
        eventType: eventPayload.eventType,
        chapterId: activeChapter.id,
        stepId: activeStep.id,
        elementCode: eventPayload.elementCode,
        elementId: eventPayload.elementId,
        idempotencyScope: eventPayload.idempotencyScope,
        payload: eventPayload.payload,
      })
      setActionStates((previous) => ({
        ...previous,
        [stateKey]: resolveStoryRuntimeFeedback(response, {
          title: '這一步已完成',
          message: '下一段線索已準備好。',
        }),
      }))
      if (response?.currentChapterId) {
        setActiveChapterId(response.currentChapterId)
        setStoryModeSession(getActiveStoryModeSession(activeStory.id))
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
          title: '記錄失敗',
          message: '請稍後重試這一步。',
          updatedAt: new Date().toISOString(),
        },
      }))
      Taro.showToast({ title: '旅程記錄暫時失敗', icon: 'none' })
    }
  }

  return (
    <PageShell className='story-page'>
      <View className='story-orbit' />
      <View className='story-hero'>
        <Text className='story-hero__eyebrow'>主線故事</Text>
        <Text className='story-hero__title'>沿著濠江線索出發</Text>
        <Text className='story-hero__desc'>故事不會一次攤開所有任務。每到一站，只給你當下該做的一步。</Text>
      </View>

      <View className='story-shell'>
        {loadError ? (
          <View className='story-error'>
            <Text className='story-error__title'>旅程暫時未能載入</Text>
            <Text className='story-error__desc'>{loadError}</Text>
            <Button className='story-error__button' loading={loading} onClick={() => void hydrateStoryContent()}>
              重新載入
            </Button>
          </View>
        ) : null}

        {!activeStory && !loadError ? (
          <View className='story-loading-card'>
            <Text className='story-loading-card__title'>{loading ? '正在整理故事線索' : '暫時沒有可開始的故事'}</Text>
            <Text className='story-loading-card__desc'>旅程內容載入後，會在這裡出現可開始的主線。</Text>
          </View>
        ) : null}

        {activeStory ? (
          <View className='story-card'>
            <View
              className='story-card__cover'
              style={{
                backgroundImage: activeStory.bannerImageUrl
                  ? `linear-gradient(180deg, rgba(19, 24, 36, 0.12), rgba(19, 24, 36, 0.78)), url(${activeStory.bannerImageUrl})`
                  : undefined,
                backgroundColor: activeStory.coverColor,
              }}
            >
              <Text className='story-card__tag'>推薦主線</Text>
              <Text className='story-card__title'>{pickStoryTitle(activeStory)}</Text>
              <Text className='story-card__meta'>
                {activeStory.estimatedTime} · {getDifficultyText(activeStory.difficulty)} · {activeStory.totalChapters} 章
              </Text>
            </View>

            {viewMode === 'intro' ? (
              <View className='story-intro'>
                {activeStory.coverImageUrl ? (
                  <SafeStoryImage
                    className='story-intro__imageFrame'
                    imageClassName='story-intro__image'
                    src={activeStory.coverImageUrl}
                    mode='aspectFill'
                    title={activeStory.name}
                    minHeight={300}
                  />
                ) : (
                  <View className='intro-cinematic'>
                    <Text className='intro-cinematic__title'>濠江歷史見證者</Text>
                    <Text className='intro-cinematic__desc'>海防銅鏡、炮台與廣場記憶正在等待你走進現場。</Text>
                  </View>
                )}
                <Text className='story-intro__copy'>{pickStoryIntro(activeStory)}</Text>
                <View className='story-intro__actions'>
                  <Button className='story-button story-button--primary' loading={storyModeBusy || loading} onClick={handleStartStoryMode}>
                    直接開始故事線
                  </Button>
                  <Button className='story-button story-button--ghost' onClick={() => setViewMode('chapters')}>
                    查看章節
                  </Button>
                </View>
              </View>
            ) : null}

            {viewMode === 'chapters' ? (
              <View className='chapter-list'>
                <View className='story-section-head'>
                  <Text className='story-section-head__title'>章節路線</Text>
                  <Text className='story-section-head__desc'>只顯示將前往的地點。支線可在兩站之間插入，也可以跳過。</Text>
                </View>
                {(activeStory.chapters || []).map((chapter, index) => (
                  <View key={chapter.id}>
                    <View
                      className={`chapter-route-card ${chapter.id === activeChapter?.id ? 'chapter-route-card--active' : ''}`}
                      onClick={() => handleViewChapter(chapter)}
                    >
                      <Text className='chapter-route-card__order'>第 {index + 1} 章</Text>
                      <Text className='chapter-route-card__title'>{cleanTravelerText(chapter.title)}</Text>
                      <Text className='chapter-route-card__place'>{resolveChapterLocation(chapter)}</Text>
                    </View>
                    {index < (activeStory.chapters || []).length - 1 ? (
                      <View className='branch-slot-card'>
                        <Text className='branch-slot-card__label'>可選支線</Text>
                        <Text className='branch-slot-card__text'>沿途附近探索，可稍後選擇加入路線。</Text>
                      </View>
                    ) : null}
                  </View>
                ))}
                {unlockedStories.length > 1 ? (
                  <View className='other-story-entry'>
                    <Text className='other-story-entry__title'>想換一條主線？</Text>
                    <Text className='other-story-entry__desc'>回到首頁的推薦探索路線，可以選擇其他故事。</Text>
                  </View>
                ) : null}
                <View className='story-intro__actions'>
                  <Button className='story-button story-button--primary' loading={storyModeBusy || loading} onClick={handleStartStoryMode}>
                    從第一章開始
                  </Button>
                  <Button className='story-button story-button--ghost' onClick={() => setViewMode('intro')}>
                    返回介紹
                  </Button>
                </View>
              </View>
            ) : null}

            {viewMode === 'playing' && activeChapter ? (
              <View className='story-play'>
                <View className='mission-header'>
                  <View>
                    <Text className='mission-header__kicker'>第 {resolveChapterOrder(activeStory, activeChapter)} 章</Text>
                    <Text className='mission-header__title'>{cleanTravelerText(activeChapter.title)}</Text>
                    <Text className='mission-header__place'>{resolveChapterLocation(activeChapter)}</Text>
                  </View>
                  <Text className='mission-header__progress'>
                    {explorationSummary?.progressPercent !== undefined
                      ? `${Math.round(explorationSummary.progressPercent)}%`
                      : steps.length
                        ? `${Math.min(activeStepIndex + 1, steps.length)}/${steps.length}`
                        : '開始'}
                  </Text>
                </View>

                <View className='mission-card'>
                  <Text className='mission-card__label'>{getStepStageLabel(activeStep)}</Text>
                  <Text className='mission-card__title'>{getStepTitle(activeStep, activeChapter)}</Text>
                  <Text className='mission-card__prompt'>{getStepPrompt(activeStep, activeChapter)}</Text>
                  {shouldShowDetailedInstruction(activeStep) ? (
                    <Text className='mission-card__desc'>{getStepInstruction(activeStep, activeChapter)}</Text>
                  ) : null}
                  {stepTargets.length ? (
                    <View className='mission-targets'>
                      <Text className='mission-targets__label'>現在只找這些線索</Text>
                      <View className='mission-targets__chips'>
                        {stepTargets.map((target) => (
                          <Text key={target} className='mission-targets__chip'>{cleanTravelerText(target)}</Text>
                        ))}
                      </View>
                    </View>
                  ) : null}
                  {stepRewardHints.length ? (
                    <View className='mission-reward-hints'>
                      <Text className='mission-reward-hints__label'>完成後可能解鎖</Text>
                      {stepRewardHints.map((hint) => (
                        <Text key={hint} className='mission-reward-hints__item'>{cleanTravelerText(hint)}</Text>
                      ))}
                    </View>
                  ) : null}
                  {shouldShowMediaTeaser(activeStep) ? (
                    <View className='mission-media-teaser' onClick={() => setCinematicOpen(true)}>
                      <Text className='mission-media-teaser__kicker'>主線劇情</Text>
                      <Text className='mission-media-teaser__title'>
                        {resolveStepMediaUrl(activeStep, activeChapter) ? '重播當前劇情' : '查看劇情舞台'}
                      </Text>
                      <Text className='mission-media-teaser__hint'>點擊展開</Text>
                    </View>
                  ) : null}
                  {currentActionState?.title || currentActionState?.message ? (
                    <View className={`mission-feedback mission-feedback--${currentActionState.status}`}>
                      {currentActionState.title ? <Text className='mission-feedback__title'>{currentActionState.title}</Text> : null}
                      {currentActionState.message ? <Text className='mission-feedback__text'>{currentActionState.message}</Text> : null}
                    </View>
                  ) : null}
                  <View className='mission-actions'>
                    <Button className='story-button story-button--primary' loading={currentActionBusy} disabled={currentActionBusy} onClick={handleStepAction}>
                      {getStepActionLabel(activeStep)}
                    </Button>
                    <Button className='story-button story-button--ghost' onClick={handleOpenStoryMap}>
                      打開故事地圖
                    </Button>
                  </View>
                </View>

                <View className='story-next-card'>
                  <Text className='story-next-card__label'>下一站</Text>
                  <Text className='story-next-card__title'>{nextChapter ? resolveChapterLocation(nextChapter) : '完成目前章節後揭曉'}</Text>
                </View>

                {storyModeSession?.active ? (
                  <Button className='story-exit-button' loading={storyModeBusy} onClick={handleExitStoryMode}>
                    離開故事
                  </Button>
                ) : null}
              </View>
            ) : null}
          </View>
        ) : null}
      </View>

      {cinematicOpen && activeChapter ? (
        <View className='cinematic-modal'>
          <View className='cinematic-modal__mask' onClick={() => setCinematicOpen(false)} />
          <View className='cinematic-modal__panel'>
            <Text className='cinematic-modal__chapter'>第 {resolveChapterOrder(activeStory, activeChapter)} 章 · {resolveChapterLocation(activeChapter)}</Text>
            <Text className='cinematic-modal__title'>{getStepTitle(activeStep, activeChapter)}</Text>
            {renderMediaStage(activeStep, activeChapter, getStepTitle(activeStep, activeChapter))}
            <Text className='cinematic-modal__desc'>{getStepInstruction(activeStep, activeChapter)}</Text>
            <Button className='story-button story-button--primary' onClick={() => setCinematicOpen(false)}>
              收起，繼續探索
            </Button>
          </View>
        </View>
      ) : null}
    </PageShell>
  )
}
