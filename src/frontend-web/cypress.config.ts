import { defineConfig } from 'cypress'
import sharp from 'sharp'
import { createCanvas, loadImage } from 'canvas'
import jsQR from 'jsqr'
import { authenticator } from 'otplib'

export default defineConfig({
  video: true,
  e2e: {
    baseUrl: 'http://localhost:5173',
    setupNodeEvents(on) {
      // implement node event listeners here
      on('task', {
        async decodeQrFromSvg(svgContent: string) {
          const pngBuffer = await sharp(Buffer.from(svgContent)).png().toBuffer()
          const image = await loadImage(pngBuffer)

          const canvas = createCanvas(image.width, image.height)
          const ctx = canvas.getContext('2d')
          ctx.drawImage(image, 0, 0)

          const imageData = ctx.getImageData(0, 0, image.width, image.height)

          const result = jsQR(imageData.data, imageData.width, imageData.height)

          if (!result) {
            throw new Error('QR code not found')
          }
          if (!result.data.startsWith('otpauth://')) {
            throw new Error('QR code does not contain otpauth:// data')
          }

          const url = new URL(result.data)
          const secret = url.searchParams.get('secret')

          if (!secret) {
            throw new Error('Não foi possível extrair o segredo do QR code.')
          }

          const token = authenticator.generate(secret)

          return { token, secret }
        },
        async generateTokenWithSecret(secret: string) {
          return authenticator.generate(secret)
        },
      })
    },
  },
})
