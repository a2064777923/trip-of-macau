import Taro from '@tarojs/taro'
import { amapConfig } from './gameMock'

export interface AmapInputTipsItem {
  id: string
  name: string
  address: string
  location?: string
  district?: string
}

export interface AmapWalkingRouteSummary {
  distance: string
  duration: string
  steps: string[]
}

const AMAP_BASE_URL = 'https://restapi.amap.com/v3'
const AMAP_TIMEOUT = 8000

async function requestAmap<T>(path: string, params: Record<string, string | number | undefined>) {
  const query = Object.entries({ ...params, key: amapConfig.key })
    .filter(([, value]) => value !== undefined && value !== '')
    .map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
    .join('&')

  try {
    const response = await Taro.request<T & { status?: string; info?: string }>({
      url: `${AMAP_BASE_URL}${path}?${query}`,
      method: 'GET',
      timeout: AMAP_TIMEOUT,
    })

    const data = response.data as any
    if (data?.status && data.status !== '1') {
      throw new Error(data.info || '高德服務暫時不可用')
    }
    return data as T
  } catch (error) {
    const message = error?.errMsg || error?.message || ''
    if (message.toLowerCase().includes('timeout')) {
      throw new Error('高德服务请求超时，已自动回退到 Mock 数据')
    }
    throw error
  }
}

export async function fetchAmapInputTips(keywords: string, city = amapConfig.defaultCity): Promise<AmapInputTipsItem[]> {
  if (!keywords.trim()) return []

  try {
    const data = await requestAmap<{ tips?: any[] }>('/assistant/inputtips', {
      keywords,
      city,
      citylimit: true,
      datatype: 'poi',
    })

    return (data.tips || [])
      .filter((item) => item.name)
      .slice(0, 8)
      .map((item, index) => ({
        id: item.id || `${item.name}-${index}`,
        name: item.name,
        address: item.address || item.district || city,
        location: item.location,
        district: item.district,
      }))
  } catch (error) {
    console.error('高德输入提示失败', error)
    return []
  }
}

export async function fetchAmapWalkingRoute(origin: string, destination: string): Promise<AmapWalkingRouteSummary | null> {
  try {
    const data = await requestAmap<{ route?: { paths?: Array<{ distance: string; duration: string; steps?: Array<{ instruction: string }> }> } }>('/direction/walking', {
      origin,
      destination,
      output: 'json',
    })

    const path = data.route?.paths?.[0]
    if (!path) return null

    return {
      distance: path.distance,
      duration: path.duration,
      steps: (path.steps || []).slice(0, 4).map((step) => step.instruction),
    }
  } catch (error) {
    console.error('高德步行路线失败', error)
    return null
  }
}

export function buildAmapLocation(lat: number, lng: number) {
  return `${lng},${lat}`
}

