import Taro from '@tarojs/taro'
import api, {
  DEFAULT_PUBLIC_LOCALE,
  type PublicIndoorFloorDto,
  type PublicIndoorMarkerDto,
  type PublicIndoorRuntimeBehaviorDto,
  type PublicIndoorRuntimeFloorDto,
  type PublicIndoorRuntimeInteractionDto,
  type PublicIndoorRuntimeNodeDto,
  type PublicIndoorRuntimeTriggerRuleDto,
  type PublicLocaleCode,
} from './api'

const SESSION_STORAGE_KEY = 'trip-of-macau-indoor-runtime-sessions'

export type IndoorRuntimeTriggerCategory = 'tap' | 'proximity' | 'dwell' | string

export interface IndoorRuntimeTriggerOption {
  nodeId: number
  behaviorId: number
  behaviorCode?: string
  behaviorName?: string
  supported: boolean
  requiresAuth: boolean
  blockedReason?: string
  triggerId?: string
  category: IndoorRuntimeTriggerCategory
  label: string
  dependsOnTriggerId?: string
  dwellSeconds?: number
  radiusMeters?: number
  behavior: PublicIndoorRuntimeBehaviorDto
}

export interface IndoorRuntimeNodeView extends PublicIndoorRuntimeNodeDto {
  visible: boolean
  interactive: boolean
  supportedBehaviorCount: number
  blockedBehaviorCount: number
  triggerOptions: IndoorRuntimeTriggerOption[]
}

export interface IndoorRuntimeFloorView extends Omit<PublicIndoorRuntimeFloorDto, 'nodes'> {
  source: 'runtime' | 'static'
  nodes: IndoorRuntimeNodeView[]
}

function normalizeCategory(value?: string | null) {
  return typeof value === 'string' ? value.trim().toLowerCase() : ''
}

function toServerTimestamp(date = new Date()) {
  return date.toISOString().replace('Z', '')
}

function readRuntimeSessionCache() {
  try {
    const cache = Taro.getStorageSync(SESSION_STORAGE_KEY) as Record<string, string> | undefined
    return cache && typeof cache === 'object' ? cache : {}
  } catch (error) {
    console.warn('Failed to read indoor runtime session cache.', error)
    return {}
  }
}

function writeRuntimeSessionCache(cache: Record<string, string>) {
  try {
    Taro.setStorageSync(SESSION_STORAGE_KEY, cache)
  } catch (error) {
    console.warn('Failed to persist indoor runtime session cache.', error)
  }
}

export function getIndoorRuntimeSessionId(floorId: number) {
  const cache = readRuntimeSessionCache()
  const key = String(floorId)
  if (cache[key]) {
    return cache[key]
  }
  const sessionId = `indoor-${floorId}-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`
  cache[key] = sessionId
  writeRuntimeSessionCache(cache)
  return sessionId
}

function parseScheduleTime(value?: unknown) {
  if (typeof value !== 'string' || !value.trim()) {
    return null
  }
  const match = value.trim().match(/^(\d{1,2}):(\d{2})/)
  if (!match) {
    return null
  }
  const hours = Number(match[1])
  const minutes = Number(match[2])
  if (!Number.isFinite(hours) || !Number.isFinite(minutes)) {
    return null
  }
  return { hours, minutes }
}

function minutesOfDay(value: { hours: number; minutes: number }) {
  return value.hours * 60 + value.minutes
}

function isScheduleWindowActive(config?: Record<string, unknown> | null, at = new Date()) {
  if (!config) {
    return true
  }
  const start = parseScheduleTime((config.startAt as string) || (config.startTime as string))
  const end = parseScheduleTime((config.endAt as string) || (config.endTime as string))
  if (!start || !end) {
    return true
  }
  const current = at.getHours() * 60 + at.getMinutes()
  const startMinutes = minutesOfDay(start)
  const endMinutes = minutesOfDay(end)
  if (endMinutes >= startMinutes) {
    return current >= startMinutes && current <= endMinutes
  }
  return current >= startMinutes || current <= endMinutes
}

export function isBehaviorCurrentlyVisible(behavior?: PublicIndoorRuntimeBehaviorDto | null, at = new Date()) {
  if (!behavior?.appearanceRules?.length) {
    return true
  }

  let evaluatedSchedule = false
  for (const rule of behavior.appearanceRules) {
    const category = normalizeCategory(rule.category)
    if (category === 'always_on' || category === 'manual') {
      return true
    }
    if (category === 'schedule_window') {
      evaluatedSchedule = true
      if (isScheduleWindowActive(rule.config || null, at)) {
        return true
      }
    }
  }

  return !evaluatedSchedule
}

function buildTriggerOptions(node: PublicIndoorRuntimeNodeDto, at = new Date()): IndoorRuntimeTriggerOption[] {
  const behaviors = node.behaviors || []
  return behaviors.flatMap((behavior) => {
    if (!isBehaviorCurrentlyVisible(behavior, at)) {
      return []
    }
    return (behavior.triggerRules || []).map((trigger) => ({
      nodeId: node.nodeId,
      behaviorId: behavior.behaviorId,
      behaviorCode: behavior.behaviorCode,
      behaviorName: behavior.name,
      supported: !!behavior.supported,
      requiresAuth: !!behavior.requiresAuth,
      blockedReason: behavior.blockedReason || undefined,
      triggerId: trigger.id,
      category: normalizeCategory(trigger.category) || 'tap',
      label: trigger.label || behavior.name || node.name || node.markerCode || '互動',
      dependsOnTriggerId: trigger.dependsOnTriggerId,
      dwellSeconds: Number(trigger.config?.seconds ?? trigger.config?.dwellSeconds ?? 0) || undefined,
      radiusMeters: Number(trigger.config?.radiusMeters ?? 0) || undefined,
      behavior,
    }))
  })
}

function sortTriggerOptions(options: IndoorRuntimeTriggerOption[]) {
  const priority: Record<string, number> = {
    tap: 0,
    proximity: 1,
    dwell: 2,
  }
  return [...options].sort((left, right) => {
    const leftPriority = priority[left.category] ?? 99
    const rightPriority = priority[right.category] ?? 99
    if (leftPriority !== rightPriority) {
      return leftPriority - rightPriority
    }
    return (left.behaviorName || left.behaviorCode || '').localeCompare(right.behaviorName || right.behaviorCode || '')
  })
}

function normalizeRuntimeNode(node: PublicIndoorRuntimeNodeDto, at = new Date()): IndoorRuntimeNodeView {
  const behaviors = node.behaviors || []
  const triggerOptions = sortTriggerOptions(buildTriggerOptions(node, at))
  const visible = behaviors.length === 0 || behaviors.some((behavior) => isBehaviorCurrentlyVisible(behavior, at))
  const supportedBehaviorCount = behaviors.filter((behavior) => !!behavior.supported).length
  const blockedBehaviorCount = behaviors.filter((behavior) => behavior.supported === false).length
  return {
    ...node,
    visible,
    interactive: triggerOptions.length > 0,
    supportedBehaviorCount,
    blockedBehaviorCount,
    triggerOptions,
  }
}

function normalizeStaticMarker(marker: PublicIndoorMarkerDto): IndoorRuntimeNodeView {
  return {
    nodeId: marker.id,
    markerCode: marker.markerCode,
    nodeType: marker.nodeType,
    name: marker.name,
    description: marker.description,
    relativeX: marker.relativeX,
    relativeY: marker.relativeY,
    relatedPoiId: marker.relatedPoiId,
    iconUrl: marker.iconUrl,
    animationUrl: marker.animationUrl,
    linkedEntityType: marker.linkedEntityType,
    linkedEntityId: marker.linkedEntityId,
    popupConfigJson: marker.popupConfigJson,
    displayConfigJson: marker.displayConfigJson,
    sortOrder: marker.sortOrder,
    status: marker.status,
    behaviors: [],
    visible: true,
    interactive: false,
    supportedBehaviorCount: 0,
    blockedBehaviorCount: 0,
    triggerOptions: [],
  }
}

export function normalizeIndoorRuntimeFloor(snapshot: PublicIndoorRuntimeFloorDto, at = new Date()): IndoorRuntimeFloorView {
  return {
    ...snapshot,
    source: 'runtime',
    nodes: (snapshot.nodes || [])
      .map((node) => normalizeRuntimeNode(node, at))
      .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0)),
  }
}

export function createStaticIndoorRuntimeFloor(floor: PublicIndoorFloorDto): IndoorRuntimeFloorView {
  return {
    source: 'static',
    floorId: floor.id,
    floorCode: floor.floorCode,
    floorNumber: floor.floorNumber,
    buildingId: 0,
    buildingCode: '',
    name: floor.name,
    description: floor.description,
    coverImageUrl: floor.coverImageUrl,
    floorPlanUrl: floor.floorPlanUrl,
    tileSourceType: floor.tileSourceType,
    tilePreviewImageUrl: floor.tilePreviewImageUrl,
    tileRootUrl: floor.tileRootUrl,
    tileManifestJson: floor.tileManifestJson,
    tileZoomDerivationJson: floor.tileZoomDerivationJson,
    imageWidthPx: floor.imageWidthPx,
    imageHeightPx: floor.imageHeightPx,
    tileSizePx: floor.tileSizePx,
    gridCols: floor.gridCols,
    gridRows: floor.gridRows,
    tileLevelCount: floor.tileLevelCount,
    tileEntryCount: floor.tileEntryCount,
    importStatus: floor.importStatus,
    importNote: floor.importNote,
    altitudeMeters: floor.altitudeMeters,
    areaSqm: floor.areaSqm,
    zoomMin: floor.zoomMin,
    zoomMax: floor.zoomMax,
    defaultZoom: floor.defaultZoom,
    popupConfigJson: floor.popupConfigJson,
    displayConfigJson: floor.displayConfigJson,
    runtimeVersion: undefined,
    nodes: (floor.markers || []).map(normalizeStaticMarker),
  }
}

export async function fetchIndoorRuntimeFloor(params: {
  floorId: number
  locale?: PublicLocaleCode
}) {
  const runtime = await api.public.getPublicIndoorFloorRuntime({
    floorId: params.floorId,
    locale: params.locale || DEFAULT_PUBLIC_LOCALE,
  })
  return normalizeIndoorRuntimeFloor(runtime)
}

export async function fetchIndoorRuntimeFloorWithFallback(params: {
  floorId: number
  locale?: PublicLocaleCode
}) {
  try {
    return await fetchIndoorRuntimeFloor(params)
  } catch (runtimeError) {
    console.warn('Failed to load indoor runtime snapshot, falling back to static floor.', runtimeError)
    const floor = await api.public.getPublicIndoorFloor({
      floorId: params.floorId,
      locale: params.locale || DEFAULT_PUBLIC_LOCALE,
    })
    return createStaticIndoorRuntimeFloor(floor)
  }
}

export function getRunnableTriggerOptions(node?: IndoorRuntimeNodeView | null, completedTriggerIds: string[] = [], at = new Date()) {
  if (!node) {
    return []
  }
  return sortTriggerOptions(
    (node.triggerOptions || []).filter((option) => {
      if (!option.dependsOnTriggerId) {
        return true
      }
      return completedTriggerIds.includes(option.dependsOnTriggerId)
    }).filter((option) => isBehaviorCurrentlyVisible(option.behavior, at)),
  )
}

export function getDefaultTriggerOption(node?: IndoorRuntimeNodeView | null, completedTriggerIds: string[] = [], at = new Date()) {
  return getRunnableTriggerOptions(node, completedTriggerIds, at)[0]
}

export function findIndoorRuntimeNode(floor?: IndoorRuntimeFloorView | null, nodeId?: number | null) {
  if (!floor || !nodeId) {
    return null
  }
  return floor.nodes.find((node) => node.nodeId === nodeId) || null
}

export function describeIndoorBlockedReason(reason?: string | null) {
  switch (normalizeCategory(reason)) {
    case 'auth_required':
      return '這個互動會改變你的個人進度，需要先使用微信登入。'
    case 'unsupported_effect_category':
      return '這個互動的部分效果尚未在小程序室內 runtime 開放，已為你安全停用。'
    case 'unsupported_trigger_category':
      return '這個互動使用了尚未開放的觸發方式，暫時不能執行。'
    case 'unsupported_appearance_category':
      return '這個互動的顯示條件尚未開放，暫時只能查看說明。'
    case 'runtime_support_level_unsupported':
      return '這個互動仍在後台治理流程中，尚未對外開放。'
    case 'behavior_not_visible':
      return '這個互動目前不在顯示時段內，請稍後再試。'
    case 'trigger_not_matched':
      return '互動條件未滿足，請依照提示重新操作。'
    default:
      return '這個互動暫時無法執行，請稍後再試。'
  }
}

export async function submitIndoorRuntimeInteraction(params: {
  floorId: number
  nodeId: number
  behaviorId: number
  triggerId?: string
  eventType: IndoorRuntimeTriggerCategory
  relativeX?: number
  relativeY?: number
  dwellMs?: number
  locale?: PublicLocaleCode
  clientSessionId?: string
}) {
  return api.public.submitPublicIndoorRuntimeInteraction({
    floorId: params.floorId,
    nodeId: params.nodeId,
    behaviorId: params.behaviorId,
    triggerId: params.triggerId,
    eventType: params.eventType,
    eventTimestamp: toServerTimestamp(),
    relativeX: params.relativeX,
    relativeY: params.relativeY,
    dwellMs: params.dwellMs,
    locale: params.locale || DEFAULT_PUBLIC_LOCALE,
    clientSessionId: params.clientSessionId || getIndoorRuntimeSessionId(params.floorId),
  })
}

export type { PublicIndoorRuntimeInteractionDto }
