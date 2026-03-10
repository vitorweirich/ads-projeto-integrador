describe('Header Navigation', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  context('Usuário não autenticado', () => {
    it('deve exibir links de login e cadastro', () => {
      cy.get('[data-cy="home-link"]').should('exist')
      cy.get('[data-cy="login-link"]').should('exist')
      cy.get('[data-cy="register-link"]').should('exist')
      cy.get('[data-cy="theme-selector"]').should('exist')

      cy.get('[data-cy="my-videos-link"]').should('not.exist')
      cy.get('[data-cy="upload-video-link"]').should('not.exist')
      cy.get('[data-cy="profile-button"]').should('not.exist')
    })

    it('clicar no link de login redireciona para a página de login', () => {
      cy.get('[data-cy="login-link"]').click()
      cy.url().should('include', '/login')
    })

    it('clicar no link de cadastro redireciona para a página de registro', () => {
      cy.get('[data-cy="register-link"]').click()
      cy.url().should('include', '/register')
    })

    it('clicar no botão theme-selector alterna a classe dark no html', () => {
      cy.get('html').should('have.class', 'dark')
      cy.get('[data-cy="theme-selector"]').click()
      cy.get('html').should('not.have.class', 'dark')
      cy.get('[data-cy="theme-selector"]').click()
      cy.get('html').should('have.class', 'dark')
    })
  })

  context('Usuário autenticado comum', () => {
    let email: string
    before(() => {
      email = `user_${Date.now()}@test.com`
      cy.registerUser({ name: 'Teste', email, password: 'ABC@123' })
    })

    beforeEach(() => {
      cy.reload()
      cy.loginUser({ email, password: 'ABC@123' })
    })

    it('deve exibir links de usuário autenticado', () => {
      cy.get('[data-cy="my-videos-link"]').should('exist')
      cy.get('[data-cy="upload-video-link"]').should('exist')
      cy.get('[data-cy="profile-button"]').contains('Olá, Teste!')
      cy.get('[data-cy="admin-videos-link"]').should('not.exist')

      cy.get('[data-cy="admin-videos-link"]').should('not.exist')
      cy.get('[data-cy="admin-users-link"]').should('not.exist')
    })

    it('deve abrir o dropdown de perfil e fazer logout', () => {
      cy.get('[data-cy="profile-button"]').click()
      cy.get('[data-cy="profile-dropdown"]').should('exist')
      cy.get('[data-cy="profile-link"]').should('exist')
      cy.get('[data-cy="logout-button"]').click()

      cy.get('[data-cy="login-link"]').should('exist')
    })

    it('deve abrir o dropdown de perfil e navegar para perfil', () => {
      cy.get('[data-cy="profile-button"]').click()
      cy.get('[data-cy="profile-dropdown"]').should('exist').should('be.visible')
      cy.get('[data-cy="profile-link"]').should('be.visible')
      cy.wait(500)
      cy.get('[data-cy="profile-link"]').should('exist').should('be.visible').click()

      cy.url().should('include', '/profile')
    })
  })

  context('Usuário administrador', () => {
    const email: string = 'user_teste2@teste.com'
    before(() => {
      cy.registerUser({ name: 'Teste', email, password: 'ABC@123' })

      cy.loginUser({ email, password: 'ABC@123' })

      cy.get('[data-cy="profile-button"]').click()
      cy.get('[data-cy="profile-dropdown"]').should('exist').should('be.visible')
      cy.get('[data-cy="profile-link"]').should('be.visible')
      cy.wait(500)
      cy.get('[data-cy="profile-link"]').should('exist').should('be.visible').click()

      cy.url().should('include', '/profile')

      cy.get('[data-cy="enable-mfa"]').should('exist').should('be.visible').click()

      cy.extractTotpSecretFromQr('[data-cy=qr-code]').then((data) => {
        console.log('Token:', data)

        cy.wrap(data.secret).as('totpSecret')

        Cypress.env('totpSecret', data.secret)

        cy.get('[data-cy=token-input]').type(data.token)
        cy.get('[data-cy=confirm-mfa]').click()
        cy.get('[data-cy="enabled-mfa-span"]')
          .should('exist')
          .should('be.visible')
          .should('have.text', 'MFA habilitado')

        cy.get('[data-cy=close-mfa-modal-button]').click()

        cy.get('[data-cy="profile-button"]').click()
        cy.get('[data-cy="profile-dropdown"]').should('exist')
        cy.get('[data-cy="profile-link"]').should('exist')
        cy.get('[data-cy="logout-button"]').click()

        cy.get('[data-cy="login-link"]').should('exist')
      })
    })

    beforeEach(() => {
      cy.reload()
      cy.loginUser({ email, password: 'ABC@123' })
    })

    it('deve exibir links de admin', () => {
      cy.get('[data-cy="admin-videos-link"]').should('exist')
      cy.get('[data-cy="admin-users-link"]').should('exist')
    })

    // TODO: Ajustar testIds
    it('clicar no link de administrar arquivos redireciona para a página de administrar arquivos', () => {
      cy.get('[data-cy="admin-videos-link"]').click()
      cy.url().should('include', '/admin/videos')
    })

    it('clicar no link de administrar usuarios redireciona para a página de administrar usuarios', () => {
      cy.get('[data-cy="admin-users-link"]').click()
      cy.url().should('include', '/admin/users')
    })
  })
})
