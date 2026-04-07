module.exports = {
  logger: {
    quiet: false,
    stats: true
  },
  // 开发环境使用 mock 数据
  defineConstants: {
    API_BASE_URL: '"http://localhost:8080/api/v1"',
    CDN_BASE_URL: '"https://cdn.tripofmacau.com"',
    USE_MOCK: 'true'
  }
}
