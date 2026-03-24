import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import axios from 'axios'

// Cria a instância
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true, // envia cookies (inclusive os HttpOnly)
})

// Flag para evitar múltiplos refresh ao mesmo tempo
let isRefreshing = false
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let failedQueue: any[] = []

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })

  failedQueue = []
}

// Intercepta respostas
api.interceptors.response.use(
  async (response) => {
    const actionsHeader = response.headers['x-client-action'] as string
    console.log({ actionsHeader, req: response?.request, res: response })

    if (actionsHeader) {
      const actions = actionsHeader
        .split(',')
        .map((a) => a.trim().toLowerCase())
        .filter(Boolean)

      for (const action of actions) {
        switch (action) {
          case 'refresh-user':
            await useAuthStore().fetchUser(true)
            break
          default:
            console.warn(`Ação desconhecida recebida: ${action}`)
        }
      }
    }

    return response
  },
  async (error) => {
    const originalRequest = error.config

    const ignoredUrlsRegexp = [
      '/v1/api/auth/login',
      '/v1/api/auth/refresh',
      '/v1/api/auth/register',
      '/v1/api/auth/logout',
      '/v1/api/auth/mfa/verify',
      '/v1/api/auth/mfa/setup',
      '/v1/api/auth/mfa/confirm',
      '/v1/api/auth/login/generate-magic-link',
      '/v1/api/auth/login/magic-link',
      '/v1/api/auth/session-exchange',
      '/v1/api/auth/confirm/*',
    ]

    // Detecta token expirado
    if (
      !ignoredUrlsRegexp.some((regexp) => new RegExp(regexp).test(originalRequest.url)) &&
      error.response?.status === 401 &&
      !originalRequest._retry
    ) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(() => api(originalRequest))
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        await api.post('/v1/api/auth/refresh')

        // Após refresh, repete a requisição original
        processQueue(null)
        return api(originalRequest)
      } catch (refreshError) {
        const authStore = useAuthStore()
        authStore.clearUserAndRedirectToLogin()

        processQueue(refreshError, null)
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    if (
      !ignoredUrlsRegexp.some((regexp) => new RegExp(regexp).test(originalRequest.url)) &&
      error.response?.data?.message
    ) {
      useToastStore().show(error.response?.data?.message, 'error')
    }

    return Promise.reject(error)
  },
)

export default api
