const fs = require('fs')
const path = require('path')

const distDir = path.resolve(__dirname, '../dist')

function removeEntry(targetPath) {
  const stats = fs.lstatSync(targetPath)

  if (stats.isDirectory()) {
    for (const entry of fs.readdirSync(targetPath)) {
      removeEntry(path.join(targetPath, entry))
    }
    fs.rmdirSync(targetPath)
    return
  }

  fs.unlinkSync(targetPath)
}

function clearOutputDirectory() {
  if (!fs.existsSync(distDir)) {
    console.log(`[clean-output] skipped missing ${distDir}`)
    return
  }

  let removedEntries = 0

  for (const entry of fs.readdirSync(distDir)) {
    const targetPath = path.join(distDir, entry)

    try {
      removeEntry(targetPath)
      removedEntries += 1
    } catch (error) {
      console.warn(`[clean-output] failed to remove ${targetPath}: ${error.message}`)
    }
  }

  console.log(`[clean-output] cleared ${removedEntries} entr${removedEntries === 1 ? 'y' : 'ies'} from ${distDir}`)
}

module.exports = {
  clearOutputDirectory,
  distDir,
}

if (require.main === module) {
  clearOutputDirectory()
}
