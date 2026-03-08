import { createApp } from 'vue'
import './style.css'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { UnauthorizedException } from './exceptions/UnauthorizedException'

const app = createApp(App)

/**
 * Manipulador global de erros do app.
 *
 * Centraliza o tratamento de exceções para garantir
 * comportamento consistente tanto para erros síncronos
 * quanto para rejeições de Promises não tratadas.
 *
 * @param err - O erro lançado ou motivo da rejeição.
 * @param info - Informação adicional sobre o contexto (Vue errorHandler).
 */
function handleGlobalError(err: unknown, info?: string) {
  console.error('Erro global capturado:', err, info)

  if (err instanceof UnauthorizedException) {
    // Redireciona para login, preservando rota original para possível retorno
    const redirectTo = err.backTo || router.currentRoute.value.fullPath
    router.push({
      path: '/login',
      query: { redirect: redirectTo },
    })
    return
  }

  // Aqui você pode adicionar tratamento para outros tipos de erro
  // ex: erro de rede, erro genérico, logger externo, etc.
}

app.config.errorHandler = (err, _instance, info) => {
  handleGlobalError(err, info)
}

window.addEventListener('unhandledrejection', (event) => {
  handleGlobalError(event.reason, 'Unhandled Promise rejection')
})

app.use(createPinia())
app.use(router)

app.mount('#app')
