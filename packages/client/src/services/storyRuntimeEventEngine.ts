import type {
  StoryRuntimeActionState,
  StoryRuntimeEventType,
  StoryRuntimeStepItem,
} from '../types/game'
import type { PublicExperienceEventResponseDto } from './api'

export type StoryRuntimeStepCategory =
  | 'story'
  | 'location'
  | 'pickup'
  | 'task'
  | 'challenge'
  | 'reward'
  | 'unsupported'

export interface StoryRuntimeStepClassification {
  category: StoryRuntimeStepCategory
  label: string
  eventType: StoryRuntimeEventType
  buttonText: string
  stateful: boolean
  unsupported: boolean
}

export interface StoryRuntimeEventPayload {
  eventType: StoryRuntimeEventType
  elementCode?: string
  elementId?: number
  idempotencyScope: string
  payload: Record<string, unknown>
}

const STATEFUL_STORY_RUNTIME_EVENTS = new Set<StoryRuntimeEventType>([
  'chapter_started',
  'click_interacted',
  'proximity_reached',
  'checkin_completed',
  'media_completed',
  'pickup_interacted',
  'task_completed',
  'reward_acquired',
  'story_session_exit',
])

const ALLOWED_STORY_RUNTIME_EVENTS = new Set<StoryRuntimeEventType>([
  'story_opened',
  'chapter_started',
  'content_viewed',
  'media_completed',
  'click_interacted',
  'proximity_reached',
  'checkin_completed',
  'pickup_interacted',
  'task_completed',
  'reward_acquired',
  'unsupported_viewed',
  'story_session_exit',
])

const STORY_RUNTIME_EVENT_ALIASES: Record<string, StoryRuntimeEventType> = {
  story_open: 'story_opened',
  chapter_open: 'chapter_started',
  content_read: 'content_viewed',
  interaction_view: 'click_interacted',
  interaction_click: 'click_interacted',
  tap: 'click_interacted',
  tap_interacted: 'click_interacted',
  click: 'click_interacted',
  click_interaction: 'click_interacted',
  arrival: 'proximity_reached',
  arrived: 'proximity_reached',
  poi_arrival: 'proximity_reached',
  nearby_reached: 'proximity_reached',
  range_reached: 'proximity_reached',
  checkin: 'checkin_completed',
  check_in: 'checkin_completed',
  poi_checkin: 'checkin_completed',
  pickup: 'pickup_interacted',
  collectible_pickup: 'pickup_interacted',
  task_complete: 'task_completed',
  reward_claimed: 'reward_acquired',
  unsupported_interaction_view: 'unsupported_viewed',
}

function normalizeStepSearchText(step: StoryRuntimeStepItem) {
  return [
    step.eventType,
    step.stepType,
    step.displayCategory,
    step.displayCategoryLabel,
    step.stepCode,
    step.triggerType,
    step.travelerActionLabel,
    step.template?.templateType,
    step.template?.category,
    step.template?.code,
  ].filter(Boolean).join(' ').toLowerCase()
}

function resolveCategory(step: StoryRuntimeStepItem): StoryRuntimeStepCategory {
  const text = normalizeStepSearchText(step)
  if (
    step.unsupported
    || /future|unsupported|ar_checkin|voice_input|photo_recognition|puzzle|cannon_defense|route_coverage|indoor_positioning/.test(text)
  ) {
    return 'unsupported'
  }
  if (step.eventType === 'pickup_interacted' || /pickup|collectible|clue|拾取|線索|信物/.test(text)) {
    return 'pickup'
  }
  if (
    step.eventType === 'proximity_reached'
    || /proximity|nearby|range|arrival|arrive|靠近|到達|抵達|範圍/.test(text)
  ) {
    return 'location'
  }
  if (step.eventType === 'checkin_completed' || /checkin|check_in|打卡/.test(text)) {
    return 'location'
  }
  if (step.eventType === 'reward_acquired' || step.rewardRuleIds || /reward|獎勵|稱號|徽章/.test(text)) {
    return 'reward'
  }
  if (/hidden|challenge|puzzle|ar|speech|cannon|route_coverage|quiz|隱藏|挑戰|問答|拼圖|炮台/.test(text)) {
    return 'challenge'
  }
  if (step.eventType === 'task_completed' || /task|mission|任務/.test(text)) {
    return 'task'
  }
  if (/click|tap|interact|overlay|marker|點擊|互動|疊加|標記/.test(text)) {
    return 'story'
  }
  return 'story'
}

function normalizeStoryRuntimeStepEventType(eventType?: string): StoryRuntimeEventType | undefined {
  if (!eventType || eventType === 'manual') {
    return undefined
  }
  const normalized = STORY_RUNTIME_EVENT_ALIASES[eventType] || eventType
  return ALLOWED_STORY_RUNTIME_EVENTS.has(normalized) ? normalized : undefined
}

function resolveEventType(step: StoryRuntimeStepItem, category: StoryRuntimeStepCategory): StoryRuntimeEventType {
  const normalizedEventType = normalizeStoryRuntimeStepEventType(step.eventType)
  if (normalizedEventType) {
    return normalizedEventType
  }
  switch (category) {
    case 'pickup':
      return 'pickup_interacted'
    case 'task':
    case 'challenge':
      return 'task_completed'
    case 'reward':
      return 'reward_acquired'
    case 'location':
      return /checkin|打卡/i.test(normalizeStepSearchText(step)) ? 'checkin_completed' : 'proximity_reached'
    case 'unsupported':
      return 'unsupported_viewed'
    default:
      return 'click_interacted'
  }
}

function resolveLabel(category: StoryRuntimeStepCategory) {
  switch (category) {
    case 'unsupported':
      return '稍後開放'
    case 'pickup':
      return '拾取線索'
    case 'task':
      return '任務'
    case 'reward':
      return '獎勵'
    case 'challenge':
      return '隱藏挑戰'
    case 'location':
      return '地點互動'
    default:
      return '劇情互動'
  }
}

function resolveButtonText(eventType: StoryRuntimeEventType, category: StoryRuntimeStepCategory) {
  if (category === 'unsupported') {
    return '查看玩法說明'
  }
  switch (eventType) {
    case 'pickup_interacted':
      return '拾取線索'
    case 'proximity_reached':
      return '標記已到達'
    case 'checkin_completed':
      return '同步打卡完成'
    case 'task_completed':
      return '標記任務完成'
    case 'reward_acquired':
      return '領取後端獎勵'
    case 'click_interacted':
      return '同步點擊互動'
    default:
      return '同步互動進度'
  }
}

export function isStoryRuntimeStatefulEvent(eventType: StoryRuntimeEventType) {
  return STATEFUL_STORY_RUNTIME_EVENTS.has(eventType)
}

export function classifyStoryRuntimeStep(step: StoryRuntimeStepItem): StoryRuntimeStepClassification {
  const category = resolveCategory(step)
  const eventType = resolveEventType(step, category)
  const unsupported = category === 'unsupported'
  return {
    category,
    label: resolveLabel(category),
    eventType,
    buttonText: resolveButtonText(eventType, category),
    stateful: isStoryRuntimeStatefulEvent(eventType),
    unsupported,
  }
}

export function getStoryRuntimeActionKey(input: {
  chapterId: number
  step: StoryRuntimeStepItem
  eventType?: StoryRuntimeEventType
  fallbackIndex?: number
}) {
  const eventType = input.eventType || classifyStoryRuntimeStep(input.step).eventType
  return `${input.chapterId}:${input.step.stepCode || input.step.id || input.fallbackIndex || 'step'}:${eventType}`
}

export function buildStoryRuntimeEventPayload(input: {
  storylineId: number
  sessionId?: string
  chapterId: number
  step: StoryRuntimeStepItem
  classification?: StoryRuntimeStepClassification
}) {
  const classification = input.classification || classifyStoryRuntimeStep(input.step)
  const stepIdentity = input.step.stepCode || input.step.id || input.step.elementCode || 'step'
  const payload: StoryRuntimeEventPayload = {
    eventType: classification.eventType,
    elementCode: input.step.elementCode || input.step.stepCode || `story_step_${input.step.id || stepIdentity}`,
    elementId: input.step.elementId || input.step.id,
    idempotencyScope: `${input.sessionId || 'read'}:${input.chapterId}:${stepIdentity}:${classification.eventType}`,
    payload: {
      category: classification.category,
      storylineId: input.storylineId,
      storyChapterId: input.chapterId,
      stepType: input.step.stepType,
      stepId: input.step.id,
      stepCode: input.step.stepCode,
      templateType: input.step.template?.templateType,
      templateCode: input.step.template?.code,
      triggerType: input.step.triggerType,
      requiredForCompletion: input.step.requiredForCompletion,
      explorationWeightLevel: input.step.explorationWeightLevel,
      unsupportedReason: input.step.unsupportedReason,
      rewardRuleIds: input.step.rewardRuleIds,
    },
  }
  return payload
}

export function resolveStoryRuntimeFeedback(
  response: PublicExperienceEventResponseDto | undefined,
  fallback?: Partial<StoryRuntimeActionState>,
): StoryRuntimeActionState {
  if (!response) {
    return {
      status: fallback?.status || 'synced',
      title: fallback?.title || '本機已記錄',
      message: fallback?.message || '目前使用本機或只讀模式，未向後端寫入互動事件。',
      outcomeType: fallback?.outcomeType,
      outcomeLabels: fallback?.outcomeLabels,
      updatedAt: new Date().toISOString(),
    }
  }
  const alreadySynced = response.duplicate || response.eventStatus === 'already_synced'
  const status: StoryRuntimeActionState['status'] = alreadySynced ? 'already_synced' : 'synced'
  return {
    status,
    title: response.feedbackTitle || fallback?.title || (alreadySynced ? '已記錄過' : '進度已同步'),
    message: response.feedbackMessage
      || response.message
      || fallback?.message
      || (alreadySynced ? '已記錄過，不會重複發放' : '互動事件已同步到後端。'),
    outcomeType: response.outcomeType || fallback?.outcomeType,
    outcomeLabels: response.outcomeLabels || fallback?.outcomeLabels,
    eventId: response.eventId,
    currentChapterId: response.currentChapterId,
    acceptedAt: response.acceptedAt,
    updatedAt: response.acceptedAt || new Date().toISOString(),
  }
}
