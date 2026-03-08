/* eslint-disable @typescript-eslint/no-require-imports */
const { version } = require('./package.json')
const { execSync } = require('child_process')

// Apenas para pegar o arquivo de env correto, o build ainda sera otimizado para produção
const MODE = 'development'
const IMAGE_NAME = 'file-share-ui'
const TAG = `${IMAGE_NAME}:test-${version}`

console.log(`📦 Buildando Docker image: ${TAG} com MODE=${MODE}...`)

try {
  execSync(`docker build --build-arg MODE=${MODE} -t ${TAG} .`, { stdio: 'inherit' })
  console.log(`✅ Imagem criada e tag 'test' adicionada.`)
} catch (err) {
  console.error('❌ Erro ao buildar a imagem Docker:', err)
  process.exit(1)
}
