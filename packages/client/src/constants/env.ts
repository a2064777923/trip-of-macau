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

function getPublicApiHostLabel() {
  if (!API_BASE_URL) {
    return 'mock-api'
  }

  const withoutProtocol = API_BASE_URL.replace(/^[a-z][a-z\d+\-.]*:\/\//i, '')
  const withoutCredentials = withoutProtocol.includes('@')
    ? withoutProtocol.slice(withoutProtocol.lastIndexOf('@') + 1)
    : withoutProtocol
  const host = withoutCredentials.split(/[/?#]/)[0]
  if (!host) {
    return 'custom-api'
  }
  return host
}

export const RUNTIME_ENV_LABEL = USE_MOCK ? 'mock' : 'live'
export const PUBLIC_API_HOST_LABEL = getPublicApiHostLabel()
export const STORY_RUNTIME_DIAGNOSTICS_ENABLED =
  USE_MOCK || API_BASE_URL.includes('127.0.0.1') || API_BASE_URL.includes('localhost')
