declare namespace Cypress {
  interface RegisterUserOptions {
    name: string
    email: string
    password: string
  }

  interface LoginUserOptions {
    email: string
    password: string
    pathAfterLogin?: string
  }

  interface Chainable<Subject = any> {
    registerUser(options: RegisterUserOptions): Chainable<void>
    loginUser(options: LoginUserOptions): Chainable<void>

    /**
     * Extrai a secret TOTP a partir de um QR Code SVG base64.
     * @param qrSelector seletor do elemento <img> contendo o QR.
     * @returns a secret extraída da otpauth URL.
     */
    extractTotpSecretFromQr(qrSelector: string): Chainable<{ token: string; secret: string }>
  }
}
