/// <reference types="cypress" />

Cypress.Commands.add(
  'extractTotpSecretFromQr',
  (qrSelector: string): Cypress.Chainable<{ token: string; secret: string }> => {
    return cy
      .get(qrSelector)
      .invoke('attr', 'src')
      .then((src: string | undefined) => {
        if (!src || !src.startsWith('data:image/svg+xml;base64,')) {
          throw new Error('QR code src inválido ou não é SVG base64.')
        }

        const base64 = src.replace('data:image/svg+xml;base64,', '')
        const svgContent = Buffer.from(base64, 'base64').toString('utf-8')

        return cy.task('decodeQrFromSvg', svgContent)
      })
  },
)

Cypress.Commands.add('registerUser', ({ name, email, password }) => {
  cy.visit('/register')

  cy.get('input[name="name"]').clear().type(name)
  cy.get('input[name="email"]').clear().type(email)
  cy.get('input[name="password"]').clear().type(password)

  cy.get('button[type="submit"]').click()

  // Aguarda o texto de sucesso (exemplo do seu template)
  cy.contains('Cadastro realizado com sucesso!', { timeout: 10000 }).should('be.visible')

  // function decodeQuotedPrintable(str: string): string {
  //   return str
  //     .replace(/=\r?\n/g, '') // remove quebras de linha forçadas
  //     .replace(/=3D/g, '=') // converte =3D para =
  //     .replace(/=([A-F0-9]{2})/gi, (_match, hex) => {
  //       return String.fromCharCode(parseInt(hex, 16))
  //     })
  // }

  cy.request('http://localhost:8025/api/v1/messages')
    .its('body')
    .then((body) => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const msg = body.messages.find((m: any) => m.To.some((x: any) => x.Address === email))

      if (!msg) {
        throw new Error(`Email para ${email} não encontrado. Verifique o MailHog.`)
      }

      cy.request(`http://localhost:8025/api/v1/message/${msg.ID}`)
        .its('body')
        .then((msgDetails) => {
          const activationLink = /http:\/\/localhost:5173\/confirm-email\/[a-z0-9-]+/i.exec(
            msgDetails.Text,
          )?.[0]
          if (!activationLink) {
            console.log(msgDetails.Text)
            const bodySnippet = msgDetails.Text || 'Email body missing'
            throw new Error(
              `Activation link not found.\nRegex: /http:\\/\\/localhost:5173\\/confir-email\\/[a-z0-9]+/\nEmail Snippet: ${bodySnippet}`,
            )
          }

          cy.visit(activationLink)
        })
    })

  // Espera carregar
  cy.contains(/Verificando seu email/i).should('not.exist')
  cy.contains(/Ir para o Login/i).should('be.visible')
})

Cypress.Commands.add('loginUser', ({ email, password, pathAfterLogin }) => {
  cy.visit('/login')

  cy.get('input[name="email"]').clear().type(email)
  cy.get('input[name="password"]').clear().type(password)

  cy.get('button[type="submit"]').click()

  cy.get('[data-cy="submit-login"]', { timeout: 10000 }).should('not.exist')
  cy.get('[data-cy="profile-button"], [data-cy="topt-token-input"]').should('be.visible')

  cy.url({ timeout: 5000 }).then((url) => {
    if (url.includes('/verify-mfa')) {
      // Só executa se a URL tiver o trecho esperado
      cy.log('Página de MFA detectada')

      const secret = Cypress.env('totpSecret')

      cy.task('generateTokenWithSecret', secret).then((code) => {
        cy.get('[data-cy="topt-token-input"]').type(code as string)
        cy.get('[data-cy="verify-mfa"]').click()
      })
    } else {
      cy.log('Página de MFA não apareceu — seguindo fluxo normalmente')
    }
  })

  cy.url({ timeout: 10000 }).should('eq', Cypress.config('baseUrl') + (pathAfterLogin || '/'))

  cy.get('[data-cy="profile-button"]', { timeout: 10000 }).should('be.visible')
})
