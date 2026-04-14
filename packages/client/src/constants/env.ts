declare const __USE_MOCK__: string
declare const __API_BASE_URL__: string
declare const __CDN_BASE_URL__: string
declare const __WECHAT_DEV_BYPASS_ENABLED__: string

export const USE_MOCK = __USE_MOCK__ === 'true'
export const WECHAT_DEV_BYPASS_ENABLED = __WECHAT_DEV_BYPASS_ENABLED__ === 'true'

export const API_BASE_URL = USE_MOCK
  ? ''
  : (__API_BASE_URL__ || 'http://127.0.0.1:8080/api/v1')

export const CDN_BASE_URL = __CDN_BASE_URL__ || 'https://cdn.tripofmacau.com'
