const fs = require('fs')
const path = require('path')

const clientDir = path.resolve(__dirname, '..')
const distDir = path.resolve(clientDir, 'dist')
const target = path.join(distDir, 'project.config.json')
const rootTarget = path.join(clientDir, 'project.config.json')
const privateTarget = path.join(clientDir, 'project.private.config.json')
const appid = process.env.WEAPP_APP_ID || 'wx36e47b792a068165'
const libVersion = process.env.WEAPP_LIB_VERSION || '3.7.12'

function createWeappProjectConfig(miniprogramRoot) {
  return {
    appid,
    projectname: 'aoxiaoyou-client',
    compileType: 'miniprogram',
    libVersion,
    miniprogramRoot,
    srcMiniprogramRoot: miniprogramRoot,
    simulatorPluginLibVersion: {},
    setting: {
      urlCheck: false,
      es6: true,
      enhance: true,
      postcss: true,
      minified: false,
      compileHotReLoad: false,
    },
    condition: {},
  }
}

function writeJson(targetPath, config) {
  fs.writeFileSync(targetPath, JSON.stringify(config, null, 2), 'utf8')
  console.log(`[write-weapp-project-config] wrote ${targetPath}`)
}

function writeWeappProjectConfig() {
  fs.mkdirSync(distDir, { recursive: true })
  writeJson(target, createWeappProjectConfig('./'))
  writeJson(rootTarget, createWeappProjectConfig('./dist/'))
  writeJson(privateTarget, {
    libVersion,
    projectname: 'aoxiaoyou-client',
    condition: {},
    setting: {
      urlCheck: false,
      compileHotReLoad: false,
      useApiHook: true,
      coverView: false,
      lazyloadPlaceholderEnable: false,
      skylineRenderEnable: false,
      preloadBackgroundData: false,
      autoAudits: false,
      showShadowRootInWxmlPanel: false,
      useStaticServer: false,
      useLanDebug: false,
      showES6CompileOption: false,
      checkInvalidKey: true,
      ignoreDevUnusedFiles: true,
      bigPackageSizeSupport: false,
    },
  })
}

module.exports = {
  writeWeappProjectConfig,
  distDir,
  target,
  rootTarget,
  privateTarget,
}

if (require.main === module) {
  writeWeappProjectConfig()
}
