module.exports = {
  logger: {
    quiet: false,
    stats: true
  },
  defineConstants: {
    WECHAT_DEV_BYPASS_ENABLED: JSON.stringify(process.env.WECHAT_DEV_BYPASS_ENABLED || 'true'),
    API_BASE_URL: '"http://127.0.0.1:8080/api/v1"',
    CDN_BASE_URL: '"https://cdn.tripofmacau.com"',
    USE_MOCK: 'false',
    __WECHAT_DEV_BYPASS_ENABLED__: JSON.stringify(process.env.WECHAT_DEV_BYPASS_ENABLED || 'true'),
    __API_BASE_URL__: '"http://127.0.0.1:8080/api/v1"',
    __CDN_BASE_URL__: '"https://cdn.tripofmacau.com"',
    __USE_MOCK__: '"false"',
    'process.env.WECHAT_DEV_BYPASS_ENABLED': JSON.stringify(process.env.WECHAT_DEV_BYPASS_ENABLED || 'true'),
    'process.env.API_BASE_URL': '"http://127.0.0.1:8080/api/v1"',
    'process.env.CDN_BASE_URL': '"https://cdn.tripofmacau.com"',
    'process.env.USE_MOCK': '"false"'
  }
}
