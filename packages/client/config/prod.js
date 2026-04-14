const apiBaseUrl = JSON.stringify(process.env.API_BASE_URL || 'http://127.0.0.1:8080/api/v1')
const cdnBaseUrl = JSON.stringify(process.env.CDN_BASE_URL || 'https://cdn.tripofmacau.com')
const useMock = JSON.stringify(process.env.USE_MOCK || 'false')

module.exports = {
  defineConstants: {
    WECHAT_DEV_BYPASS_ENABLED: '"false"',
    API_BASE_URL: apiBaseUrl,
    CDN_BASE_URL: cdnBaseUrl,
    USE_MOCK: useMock,
    __WECHAT_DEV_BYPASS_ENABLED__: '"false"',
    __API_BASE_URL__: apiBaseUrl,
    __CDN_BASE_URL__: cdnBaseUrl,
    __USE_MOCK__: useMock,
    'process.env.WECHAT_DEV_BYPASS_ENABLED': '"false"',
    'process.env.API_BASE_URL': apiBaseUrl,
    'process.env.CDN_BASE_URL': cdnBaseUrl,
    'process.env.USE_MOCK': useMock
  }
}
