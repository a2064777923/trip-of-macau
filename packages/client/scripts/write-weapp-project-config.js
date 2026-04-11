const fs = require('fs')
const path = require('path')

const distDir = path.resolve(__dirname, '../dist')
const target = path.join(distDir, 'project.config.json')
const appid = process.env.WEAPP_APP_ID || 'wx36e47b792a068165'

const config = {
  appid,
  projectname: 'aoxiaoyou-client',
  compileType: 'miniprogram',
  libVersion: 'trial',
  miniprogramRoot: './',
  srcMiniprogramRoot: './',
  simulatorPluginLibVersion: {},
  setting: {
    urlCheck: false,
    es6: true,
    enhance: true,
    postcss: true,
    minified: false,
    compileHotReLoad: true,
  },
  condition: {},
}

fs.mkdirSync(distDir, { recursive: true })
fs.writeFileSync(target, JSON.stringify(config, null, 2), 'utf8')
console.log(`[write-weapp-project-config] wrote ${target}`)
