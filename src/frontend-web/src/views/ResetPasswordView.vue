<script setup lang="ts">
import { ref, watch } from 'vue'
import api from '@/services/api'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth' // sua store de auth
const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const password = ref('')
const passwordConfirm = ref('')
const error = ref('')
const loading = ref(false)

const handleRedirectToLogin = () => {
  router.push('/login')
}

const handleResetPassword = async () => {
  error.value = ''
  loading.value = true
  try {
    await api.post('/v1/api/auth/reset-password', {
      token: route.params.token,
      newPassword: password.value,
    })

    handleRedirectToLogin()

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    console.error(err)
    error.value = err?.response?.data?.message || 'Falha ao redefinir a senha.'
  } finally {
    loading.value = false
  }
}

watch([password, passwordConfirm], () => {
  if (password.value && passwordConfirm.value && password.value !== passwordConfirm.value) {
    error.value = 'As senhas não coincidem'
  } else {
    error.value = ''
  }
})

watch(
  () => auth.isAuthenticated,
  (isAuth) => {
    if (isAuth) {
      router.push('/')
    }
  },
)
</script>

<template>
  <div class="flex items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
    <div class="w-full max-w-md space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold">Redefina sua senha</h2>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleResetPassword">
        <div class="-space-y-px rounded-md shadow-sm">
          <div>
            <label for="password" class="sr-only">Nova senha</label>
            <input
              id="password"
              name="password"
              type="password"
              required
              v-model="password"
              class="relative block w-full appearance-none rounded-none rounded-t-md border border-gray-300 px-3 py-2 placeholder-gray-500 focus:z-10 focus:border-blue-500 focus:ring-blue-500 focus:outline-none sm:text-sm"
              placeholder="Nova senha"
            />
          </div>
          <div>
            <label for="password" class="sr-only">Confirme sua senha</label>
            <input
              id="password"
              name="password"
              type="password"
              required
              v-model="passwordConfirm"
              class="relative block w-full appearance-none rounded-none rounded-b-md border border-gray-300 px-3 py-2 placeholder-gray-500 focus:z-10 focus:border-blue-500 focus:ring-blue-500 focus:outline-none sm:text-sm"
              placeholder="Confirme sua senha"
            />
          </div>
        </div>
        <div class="text-error text-sm" v-if="error">
          {{ error }}
        </div>
        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative flex w-full justify-center rounded-md border border-transparent px-4 py-2 text-sm font-medium focus:ring-blue-500 focus:ring-offset-2 focus:outline-none hover:focus:ring-2 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <span v-if="loading" class="mr-2">
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
            <span v-if="!loading">Redefinir senha</span>
            <span v-else>Carregando...</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
