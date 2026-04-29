const { spawn } = require('child_process')
const fs = require('fs')
const path = require('path')

const { clearOutputDirectory } = require('./clean-output')
const { writeWeappProjectConfig, target } = require('./write-weapp-project-config')

clearOutputDirectory()
writeWeappProjectConfig()

const taroCommand = process.platform === 'win32' ? 'npx.cmd' : 'npx'
const child = spawn(taroCommand, ['taro', 'build', '--type', 'weapp', '--watch'], {
  cwd: path.resolve(__dirname, '..'),
  stdio: 'inherit',
  shell: true,
  env: {
    ...process.env,
    NODE_ENV: process.env.NODE_ENV || 'development',
  },
})

const ensureConfigTimer = setInterval(() => {
  if (!fs.existsSync(target)) {
    writeWeappProjectConfig()
  }
}, 1000)

const stopTimer = () => {
  clearInterval(ensureConfigTimer)
}

child.on('exit', (code) => {
  stopTimer()
  process.exit(code || 0)
})

child.on('error', (error) => {
  stopTimer()
  console.error('[dev-weapp] failed to start taro watch:', error)
  process.exit(1)
})

process.on('SIGINT', () => {
  stopTimer()
  child.kill('SIGINT')
})

process.on('SIGTERM', () => {
  stopTimer()
  child.kill('SIGTERM')
})
