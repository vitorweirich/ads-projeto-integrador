<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handlePostLoginRedirect = () => {
  const redirect = router.currentRoute.value.query.redirect as string
  router.push(redirect || '/')
}

const handleLogin = async () => {
  error.value = ''
  loading.value = true
  try {
    const response = await api.post('/v1/api/auth/login', {
      email: email.value,
      password: password.value,
    })

    const data = response.data

    if (data?.type === 'mfa_token') {
      // Guardar o token MFA temporariamente na store
      auth.setMfaToken(data.token)
      // Mantém os query parameters existentes
      router.push({ path: '/verify-mfa', query: router.currentRoute.value.query })
    } else {
      handlePostLoginRedirect()
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    console.error(err)
    error.value = err?.response?.data?.message || 'Falha no login'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (auth.isAuthenticated) {
    handlePostLoginRedirect()
  }
})

watch(
  () => auth.isAuthenticated,
  (isAuth) => {
    if (isAuth) {
      handlePostLoginRedirect()
    }
  },
)
</script>

<template>
  <div class="flex items-center justify-center px-4 py-12 sm:px-6 lg:px-8" data-cy="login-page">
    <div class="w-full max-w-md space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold" data-cy="login-title">
          Entrar na sua conta
        </h2>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleLogin" data-cy="login-form">
        <div class="-space-y-px rounded-md shadow-sm">
          <div>
            <label for="email-address" class="sr-only">E-mail</label>
            <input
              id="email-address"
              name="email"
              type="email"
              required
              v-model="email"
              class="relative block w-full appearance-none rounded-none rounded-t-md border border-gray-300 px-3 py-2 placeholder-gray-500 focus:z-10 focus:border-blue-500 focus:ring-blue-500 focus:outline-none sm:text-sm"
              placeholder="E-mail"
              data-cy="input-email"
            />
          </div>
          <div>
            <label for="password" class="sr-only">Senha</label>
            <input
              id="password"
              name="password"
              type="password"
              required
              v-model="password"
              class="relative block w-full appearance-none rounded-none rounded-b-md border border-gray-300 px-3 py-2 placeholder-gray-500 focus:z-10 focus:border-blue-500 focus:ring-blue-500 focus:outline-none sm:text-sm"
              placeholder="Senha"
              data-cy="input-password"
            />
          </div>
        </div>
        <div class="text-error text-sm" v-if="error" data-cy="login-error">
          {{ error }}
        </div>
        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative flex w-full justify-center rounded-md border border-transparent px-4 py-2 text-sm font-medium focus:ring-blue-500 focus:ring-offset-2 focus:outline-none hover:focus:ring-2 disabled:cursor-not-allowed disabled:opacity-50"
            data-cy="submit-login"
          >
            <span v-if="loading" class="mr-2" data-cy="loading-spinner">
              <svg
                class="inline h-5 w-5 animate-spin"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  class="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  stroke-width="4"
                ></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"></path>
              </svg>
            </span>
            <span v-if="!loading">Entrar na conta</span>
            <span v-else>Entrando...</span>
          </button>
        </div>
        <div class="flex flex-row justify-center gap-4" data-cy="login-links">
          <div class="text-center text-sm">
            <router-link to="/register" class="" data-cy="link-register">Criar conta</router-link>
          </div>
          <div class="text-center text-sm">
            <router-link to="/forgot-password" class="" data-cy="link-forgot-password"
              >Esqueci minha senha</router-link
            >
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
