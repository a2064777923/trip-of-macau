import Taro from '@tarojs/taro'
import { calculateDistance } from '../utils/location'
import type { PoiItem } from '../types/game'

// 探索點触发状态
export type PoiTriggerState = 'IDLE' | 'APPROACHING' | 'DWELLING' | 'TRIGGERED' | 'COOLDOWN'

// 探索點触发会话
export interface PoiTriggerSession {
  poiId: number
  state: PoiTriggerState
  enteredAt?: number // 进入触发范围的时间戳
  triggeredAt?: number // 触发时间戳
  cooldownUntil?: number // 冷却结束时间戳
  dwellStartTime?: number // 开始停留的时间戳
  lastDistance?: number // 上次检测的距离
}

// 触发配置
export interface TriggerConfig {
  debounceMillis: number // 防抖时间（毫秒）
  cooldownSeconds: number // 冷却时间（秒）
  minDwellSeconds: number // 最小停留时间（秒）
  approachingThreshold: number // 接近阈值（米）
}

const DEFAULT_CONFIG: TriggerConfig = {
  debounceMillis: 2000, // 2秒防抖
  cooldownSeconds: 1800, // 30分钟冷却
  minDwellSeconds: 30, // 30秒最小停留
  approachingThreshold: 1.5, // 触发半径的1.5倍为接近阈值
}

const STORAGE_KEY = 'trip-of-macau-poi-trigger-sessions'

// 获取所有触发会话
function getTriggerSessions(): Record<number, PoiTriggerSession> {
  try {
    const data = Taro.getStorageSync(STORAGE_KEY)
    return data || {}
  } catch (error) {
    console.error('Failed to load trigger sessions:', error)
    return {}
  }
}

// 保存触发会话
function saveTriggerSessions(sessions: Record<number, PoiTriggerSession>) {
  try {
    Taro.setStorageSync(STORAGE_KEY, sessions)
  } catch (error) {
    console.error('Failed to save trigger sessions:', error)
  }
}

// 获取单个探索點的触发会话
export function getPoiTriggerSession(poiId: number): PoiTriggerSession {
  const sessions = getTriggerSessions()
  return sessions[poiId] || {
    poiId,
    state: 'IDLE',
  }
}

// 更新探索點触发会话
function updatePoiTriggerSession(session: PoiTriggerSession) {
  const sessions = getTriggerSessions()
  sessions[session.poiId] = session
  saveTriggerSessions(sessions)
}

// 清除冷却状态
export function clearPoiCooldown(poiId: number) {
  const session = getPoiTriggerSession(poiId)
  if (session.state === 'COOLDOWN') {
    updatePoiTriggerSession({
      ...session,
      state: 'IDLE',
      cooldownUntil: undefined,
    })
  }
}

// 重置所有触发会话（用于测试）
export function resetAllTriggerSessions() {
  saveTriggerSessions({})
}

// 计算动态触发半径（根据 GPS 精度调整）
function calculateDynamicRadius(poi: PoiItem, gpsAccuracy?: number): number {
  const baseRadius = poi.triggerRadius || 50
  
  // 如果没有 GPS 精度信息，使用基础半径
  if (!gpsAccuracy) {
    return baseRadius
  }
  
  // 根据 GPS 精度调整半径
  // 精度越差，半径越大（最大80米，最小30米）
  const adjustedRadius = Math.max(30, Math.min(80, baseRadius + gpsAccuracy * 0.5))
  
  return adjustedRadius
}

// 计算动态停留时间（根据 GPS 精度调整）
function calculateDynamicDwellTime(poi: PoiItem, gpsAccuracy?: number): number {
  const baseSeconds = poi.staySeconds || 30
  
  // 如果没有 GPS 精度信息，使用基础时间
  if (!gpsAccuracy) {
    return baseSeconds
  }
  
  // 根据 GPS 精度调整停留时间
  // 精度越差，需要停留越久（最大120秒，最小30秒）
  const adjustedSeconds = Math.max(30, Math.min(120, baseSeconds + gpsAccuracy * 0.3))
  
  return adjustedSeconds
}

// 检查是否在冷却期
function isInCooldown(session: PoiTriggerSession, config: TriggerConfig): boolean {
  if (session.state !== 'COOLDOWN') {
    return false
  }
  
  const now = Date.now()
  if (session.cooldownUntil && now < session.cooldownUntil) {
    return true
  }
  
  // 冷却期结束，重置状态
  updatePoiTriggerSession({
    ...session,
    state: 'IDLE',
    cooldownUntil: undefined,
  })
  
  return false
}

// 状态机核心逻辑
export function updatePoiTriggerState(
  poi: PoiItem,
  userLocation: { latitude: number; longitude: number; accuracy?: number },
  config: TriggerConfig = DEFAULT_CONFIG
): {
  session: PoiTriggerSession
  shouldTrigger: boolean
  distance: number
  dynamicRadius: number
  requiredDwellSeconds: number
  currentDwellSeconds: number
} {
  const session = getPoiTriggerSession(poi.id)
  const now = Date.now()
  
  // 计算距离
  const distance = calculateDistance(
    userLocation.latitude,
    userLocation.longitude,
    poi.latitude,
    poi.longitude
  )
  
  // 计算动态参数
  const dynamicRadius = calculateDynamicRadius(poi, userLocation.accuracy)
  const requiredDwellSeconds = calculateDynamicDwellTime(poi, userLocation.accuracy)
  const approachingRadius = dynamicRadius * config.approachingThreshold
  
  let shouldTrigger = false
  let newSession = { ...session, lastDistance: distance }
  
  // 检查冷却期
  if (isInCooldown(session, config)) {
    return {
      session: newSession,
      shouldTrigger: false,
      distance,
      dynamicRadius,
      requiredDwellSeconds,
      currentDwellSeconds: 0,
    }
  }
  
  // 状态机逻辑
  switch (session.state) {
    case 'IDLE':
      if (distance <= approachingRadius) {
        // 进入接近状态
        newSession = {
          ...newSession,
          state: 'APPROACHING',
          enteredAt: now,
        }
      }
      break
      
    case 'APPROACHING':
      if (distance > approachingRadius) {
        // 离开接近范围，回到 IDLE
        newSession = {
          poiId: poi.id,
          state: 'IDLE',
          lastDistance: distance,
        }
      } else if (distance <= dynamicRadius) {
        // 进入触发范围，开始停留
        newSession = {
          ...newSession,
          state: 'DWELLING',
          dwellStartTime: now,
        }
      }
      break
      
    case 'DWELLING':
      if (distance > dynamicRadius) {
        // 离开触发范围，回到 APPROACHING 或 IDLE
        if (distance <= approachingRadius) {
          newSession = {
            ...newSession,
            state: 'APPROACHING',
            dwellStartTime: undefined,
          }
        } else {
          newSession = {
            poiId: poi.id,
            state: 'IDLE',
            lastDistance: distance,
          }
        }
      } else {
        // 检查停留时间
        const dwellSeconds = (now - (session.dwellStartTime || now)) / 1000
        if (dwellSeconds >= requiredDwellSeconds) {
          // 触发！
          newSession = {
            ...newSession,
            state: 'TRIGGERED',
            triggeredAt: now,
          }
          shouldTrigger = true
        }
      }
      break
      
    case 'TRIGGERED':
      // 触发后立即进入冷却期
      newSession = {
        ...newSession,
        state: 'COOLDOWN',
        cooldownUntil: now + config.cooldownSeconds * 1000,
      }
      break
      
    case 'COOLDOWN':
      // 已在 isInCooldown 中处理
      break
  }
  
  // 保存新状态
  updatePoiTriggerSession(newSession)
  
  // 计算当前停留时间
  const currentDwellSeconds = newSession.state === 'DWELLING' && newSession.dwellStartTime
    ? (now - newSession.dwellStartTime) / 1000
    : 0
  
  return {
    session: newSession,
    shouldTrigger,
    distance,
    dynamicRadius,
    requiredDwellSeconds,
    currentDwellSeconds,
  }
}

// 获取所有探索點的触发状态摘要
export function getAllPoiTriggerStates(): Array<{
  poiId: number
  state: PoiTriggerState
  cooldownRemaining?: number
}> {
  const sessions = getTriggerSessions()
  const now = Date.now()
  
  return Object.values(sessions).map(session => ({
    poiId: session.poiId,
    state: session.state,
    cooldownRemaining: session.cooldownUntil && session.cooldownUntil > now
      ? Math.ceil((session.cooldownUntil - now) / 1000)
      : undefined,
  }))
}

// 手动触发探索點（用于手动打卡）
export function manualTriggerPoi(poiId: number, config: TriggerConfig = DEFAULT_CONFIG) {
  const now = Date.now()
  const session: PoiTriggerSession = {
    poiId,
    state: 'COOLDOWN',
    triggeredAt: now,
    cooldownUntil: now + config.cooldownSeconds * 1000,
  }
  updatePoiTriggerSession(session)
}
