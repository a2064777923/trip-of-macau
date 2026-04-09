module.exports = {
  presets: [['taro', {
    framework: 'react',
    ts: true
  }]],
  plugins: [
    ['@babel/plugin-proposal-decorators', { legacy: true }],
    ['@babel/plugin-proposal-class-properties', { loose: true }],
    ['@babel/plugin-transform-private-methods', { loose: true }],
    ['@babel/plugin-transform-private-property-in-object', { loose: true }],
    '@babel/plugin-proposal-object-rest-spread',
    '@babel/plugin-syntax-dynamic-import',
    ['@babel/plugin-transform-runtime', {
      helpers: true,
      regenerator: true
    }]
  ]
}

