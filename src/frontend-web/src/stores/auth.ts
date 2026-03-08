import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import api from '@/services/api'
import router from '@/router'
import { UnauthorizedException } from '@/exceptions/UnauthorizedException'

export type LoggedInUser = {
  name: string
  email: string
  mfaEnabled: boolean
  hasAdminPrivileges: boolean
  storage: {
    totalQuota: number
    usedQuota: number
  }
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoggedInUser | null>(null)
  const loading = ref(true)
  const mfaToken = ref<string | null>(null)

  const isAuthenticated = computed(() => !!user.value)

  // Variável interna para controlar carregamento único
  let readyPromise: Promise<void> | null = null

  async function fetchUser(refetch = true) {
    // Se já está carregando, apenas retorne a promise existente
    if (!refetch && readyPromise) return readyPromise

    loading.value = true

    // Cria nova promise de carregamento
    readyPromise = new Promise(async (resolve) => {
      const start = performance.now()
      const maxDelay = 400 // Tempo máximo de espera para simular carregamento
      try {
        const res = await api.get('/v1/api/auth/me')
        user.value = res.data
      } catch (err: any) {
        if (err.response?.status === 401) {
          user.value = null
        } else {
          console.error('Erro ao verificar sessão:', err)
        }
      } finally {
        const elapsed = performance.now() - start
        const remaining = maxDelay - elapsed

        if (remaining > 0) {
          // Simula um pequeno atraso para evitar flicker na UI
          await new Promise((r) => setTimeout(r, remaining))
        }

        loading.value = false
        resolve()
      }
    })

    return readyPromise
  }

  async function waitUntilReady() {
    if (readyPromise) {
      await readyPromise
    } else {
      await fetchUser(false)
    }
  }

  /**
   * Obtém o usuário atualmente autenticado, garantindo que exista.
   *
   * @throws {UnauthorizedException}
   * Lançada caso não exista um usuário logado no momento.
   *
   * @remarks
   * Use este método quando a lógica de negócio **depender** de um usuário autenticado
   * e não fizer sentido continuar a execução sem ele.
   *
   * Ao ser chamada sem um usuário autenticado, a função lança uma {@link UnauthorizedException},
   * que pode ser interceptada pelo tratamento global de erros para, por exemplo,
   * redirecionar automaticamente para a página de login.
   *
   * Caso você precise apenas consultar o usuário sem interromper o fluxo,
   * acesse a prop `user` diretamente ou crie um método `getLoggedInUser()` que retorne `null` se não houver login.
   *
   * @example
   * ```ts
   * const currentUser = authStore.requireLoggedInUser()
   * console.log(currentUser.name) // Com certeza existe usuário aqui
   * ```
   */
  async function requireLoggedInUser(): Promise<LoggedInUser> {
    await waitUntilReady()
    if (!isAuthenticated.value || !user.value) {
      throw new UnauthorizedException('Usuário não esta logado no sistema')
    }

    return user.value
  }

  async function logout() {
    try {
      await api.post('/v1/api/auth/logout')
    } finally {
      user.value = null
      router.push({ name: 'home' })
    }
  }

  function clearUserAndRedirectToLogin() {
    const wasAuthenticated = !!user.value

    user.value = null

    if (wasAuthenticated) {
      // redireciona para login com o path original
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath },
      })
    }
  }

  function setMfaToken(token: string | null) {
    mfaToken.value = token
  }

  return {
    user,
    loading,
    fetchUser,
    waitUntilReady,
    logout,
    isAuthenticated: computed(() => !!user.value),
    isAdmin: computed(() => user.value?.hasAdminPrivileges || false),
    mfaToken,
    setMfaToken,
    clearUserAndRedirectToLogin,
    requireLoggedInUser,
  }
})
