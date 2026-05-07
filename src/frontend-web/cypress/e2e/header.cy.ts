describe('Header Navigation', () => {
  beforeEach(() => {
    cy.visit('/')
  })

  context('Usuário não autenticado', () => {
    it('deve exibir links de login e cadastro', () => {
      cy.get('[data-cy="home-link"]:visible').should('exist')
      cy.get('[data-cy="login-link"]:visible').should('exist')
      cy.get('[data-cy="register-link"]:visible').should('exist')
      cy.get('[data-cy="theme-selector"]:visible').should('exist')

      cy.get('[data-cy="my-files-link"]:visible').should('not.exist')
      cy.get('[data-cy="upload-file-link"]:visible').should('not.exist')
      cy.get('[data-cy="profile-button"]:visible').should('not.exist')
    })

    it('clicar no link de login redireciona para a página de login', () => {
      cy.get('[data-cy="login-link"]:visible').click()
      cy.url().should('include', '/login')
    })

    it('clicar no link de cadastro redireciona para a página de registro', () => {
      cy.get('[data-cy="register-link"]:visible').click()
      cy.url().should('include', '/register')
    })

    it('clicar no botão theme-selector alterna a classe dark no html', () => {
      cy.get('html').should('have.class', 'dark')
      cy.get('[data-cy="theme-selector"]:visible').click()
      cy.get('html').should('not.have.class', 'dark')
      cy.get('[data-cy="theme-selector"]:visible').click()
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
      cy.get('[data-cy="my-files-link"]:visible').should('exist')
      cy.get('[data-cy="upload-file-link"]:visible').should('exist')
      cy.get('[data-cy="profile-button"]:visible').contains('Olá, Teste!')
      cy.get('[data-cy="admin-files-link"]:visible').should('not.exist')

      cy.get('[data-cy="admin-files-link"]:visible').should('not.exist')
      cy.get('[data-cy="admin-users-link"]:visible').should('not.exist')
    })

    it('deve abrir o dropdown de perfil e fazer logout', () => {
      cy.get('[data-cy="profile-button"]:visible').click()
      cy.get('[data-cy="profile-dropdown"]').should('exist')
      cy.get('[data-cy="profile-link"]:visible').should('exist')
      cy.get('[data-cy="logout-button"]:visible').click()

      cy.get('[data-cy="login-link"]:visible').should('exist')
    })

    it('deve abrir o dropdown de perfil e navegar para perfil', () => {
      cy.get('[data-cy="profile-button"]:visible').click()
      cy.get('[data-cy="profile-dropdown"]').should('exist').should('be.visible')
      cy.get('[data-cy="profile-link"]:visible').should('be.visible')
      cy.wait(500)
      cy.get('[data-cy="profile-link"]:visible').should('exist').should('be.visible').click()

      cy.url().should('include', '/profile')
    })
  })

  context('Usuário administrador', () => {
    const email: string = 'user_teste2@teste.com'
    before(() => {
      cy.registerUser({ name: 'Teste', email, password: 'ABC@123' })

      cy.loginUser({ email, password: 'ABC@123' })

      cy.get('[data-cy="profile-button"]:visible').click()
      cy.get('[data-cy="profile-dropdown"]').should('exist').should('be.visible')
      cy.get('[data-cy="profile-link"]:visible').should('be.visible')
      cy.wait(500)
      cy.get('[data-cy="profile-link"]:visible').should('exist').should('be.visible').click()

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

        cy.get('[data-cy="profile-button"]:visible').click()
        cy.get('[data-cy="profile-dropdown"]').should('exist')
        cy.get('[data-cy="profile-link"]:visible').should('exist')
        cy.get('[data-cy="logout-button"]:visible').click()

        cy.get('[data-cy="login-link"]:visible').should('exist')
      })
    })

    beforeEach(() => {
      cy.reload()
      cy.loginUser({ email, password: 'ABC@123' })
    })

    it('deve exibir links de admin', () => {
      cy.get('[data-cy="admin-files-link"]:visible').should('exist')
      cy.get('[data-cy="admin-users-link"]:visible').should('exist')
    })

    it('clicar no link de administrar arquivos redireciona para a página de administrar arquivos', () => {
      cy.get('[data-cy="admin-files-link"]:visible').click()
      cy.url().should('include', '/admin/files')
    })

    it('clicar no link de administrar usuarios redireciona para a página de administrar usuarios', () => {
      cy.get('[data-cy="admin-users-link"]:visible').click()
      cy.url().should('include', '/admin/users')
    })
  })
})
