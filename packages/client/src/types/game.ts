export interface AppUserProfile {
  userId: string
  nickname: string
  avatarUrl: string
  isGuest?: boolean
  openId?: string
  level: number
  title: string
  totalStamps: number
  currentExp: number
  nextLevelExp: number
  interfaceMode: 'standard' | 'elderly'
  fontScale: number
  highContrast: boolean
  voiceGuideEnabled: boolean
  unlockedStorylines: number
  badges: string[]
}

export interface PoiItem {
  id: number
  name: string
  subtitle: string
  icon: string
  latitude: number
  longitude: number
  gcj02Latitude: number
  gcj02Longitude: number
  address: string
  geofenceRadius: number
  triggerRadius: number
  difficulty: 'easy' | 'medium' | 'hard'
  category: string
  district: string
  storyLineId?: number
  storyName?: string
  description: string
  checkInMethod: 'gps' | 'manual' | 'mock'
  staySeconds: number
  rewardStampId?: number
  tags: string[]
  coverColor: string
}

export interface StorylineItem {
  id: number
  name: string
  nameEn: string
  description: string
  icon: string
  coverColor: string
  totalChapters: number
  completedChapters: number
  estimatedTime: string
  difficulty: 'easy' | 'medium' | 'hard'
  poiIds: number[]
  chapterTitles: string[]
  progress: number
  rewardBadge?: string
}

export interface StampItem {
  id: number
  type: 'location' | 'story' | 'mission' | 'secret'
  name: string
  description: string
  icon: string
  collected: boolean
  collectedAt?: string
  poiId?: number
  storyId?: number
  rarity: 'common' | 'rare' | 'epic'
}

export interface RewardItem {
  id: number
  name: string
  subtitle: string
  icon: string
  stampCost: number
  inventory: number
  status: 'available' | 'coming_soon' | 'redeemed'
  description: string
  highlight: string
}

export interface CheckinResult {
  success: boolean
  poiId: number
  poiName: string
  stampId: number
  stampName: string
  experienceGained: number
  triggerMode: 'gps' | 'manual' | 'mock'
  unlockedStorylineId?: number
}

export interface MapCameraState {
  latitude: number
  longitude: number
  scale: number
}

export interface DiscoverCardItem {
  id: number
  title: string
  subtitle: string
  description: string
  tag: string
  icon: string
  type: 'activity' | 'merchant' | 'checkin'
  district: string
  actionText: string
  coverColor: string
}

export interface TipArticleItem {
  id: number
  title: string
  summary: string
  coverColor: string
  category: string
  author: string
  likes: number
  saves: number
  readMinutes: number
  tags: string[]
}

export interface ArrivalExperience {
  poiId: number
  title: string
  narrative: string
  audioTitle: string
  audioDuration: string
  rewardLabel: string
  canManualCheckin: boolean
}
