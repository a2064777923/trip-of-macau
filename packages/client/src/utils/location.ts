// 位置相关工具函数

// 计算两点之间的距离（米）
export const calculateDistance = (
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number
): number => {
  const R = 6371000 // 地球半径（米）
  const dLat = toRadians(lat2 - lat1)
  const dLng = toRadians(lng2 - lng1)
  
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) *
      Math.cos(toRadians(lat2)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2)
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  const distance = R * c
  
  return Math.round(distance)
}

// 角度转弧度
const toRadians = (degrees: number): number => {
  return degrees * (Math.PI / 180)
}

// 格式化距离显示
export const formatDistance = (distance: number): string => {
  if (distance < 10) {
    return '就在附近'
  } else if (distance < 1000) {
    return `${distance}米`
  } else {
    return `${(distance / 1000).toFixed(1)}公里`
  }
}

// 判断是否在POI触发范围内
export const isWithinTriggerRange = (
  userLat: number,
  userLng: number,
  poiLat: number,
  poiLng: number,
  triggerRadius: number,
  gpsAccuracy: number
): { isInRange: boolean; actualDistance: number; dynamicRadius: number } => {
  const distance = calculateDistance(userLat, userLng, poiLat, poiLng)
  
  // 根据GPS精度动态调整触发半径
  let dynamicRadius = triggerRadius
  if (gpsAccuracy < 10) {
    dynamicRadius = triggerRadius // GPS精度高，使用标准半径
  } else if (gpsAccuracy < 20) {
    dynamicRadius = triggerRadius * 1.5 // GPS精度中等，放宽半径
  } else {
    dynamicRadius = triggerRadius * 2 // GPS精度差，大幅放宽半径
  }
  
  return {
    isInRange: distance <= dynamicRadius,
    actualDistance: distance,
    dynamicRadius
  }
}

// 获取位置描述
export const getLocationDescription = (latitude: number, longitude: number): string => {
  // 简化的区域判断，实际应该根据地图数据
  if (latitude > 22.19 && latitude < 22.2 && longitude > 113.53 && longitude < 113.55) {
    return '澳门半岛 - 历史城区'
  } else if (latitude > 22.15 && latitude < 22.17) {
    return '氹仔'
  } else {
    return '澳门特别行政区'
  }
}

// 防抖函数（用于位置更新）
export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: ReturnType<typeof setTimeout> | null = null
  
  return function (this: any, ...args: Parameters<T>) {
    if (timeout) {
      clearTimeout(timeout)
    }
    timeout = setTimeout(() => {
      func.apply(this, args)
    }, wait)
  }
}

// 节流函数（用于位置更新）
export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  limit: number
): ((...args: Parameters<T>) => void) => {
  let inThrottle = false
  
  return function (this: any, ...args: Parameters<T>) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => {
        inThrottle = false
      }, limit)
    }
  }
}
