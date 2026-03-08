<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import { useAuthStore } from '@/stores/auth' // sua store de auth
const auth = useAuthStore()

const router = useRouter()
const email = ref('')
const error = ref('')
const isRegistered = ref(false)
const isLoading = ref(false)

const handleSubmit = async () => {
  isLoading.value = true
  try {
    await api.post('/v1/api/auth/forgot-password', {
      email: email.value,
    })
    isRegistered.value = true

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    console.error(err)
    error.value = err?.response?.data?.message || 'Ocorreu um erro inesperado'
  } finally {
    isLoading.value = false
  }
}

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
        <h2 v-if="!isRegistered" class="mt-6 text-center text-3xl font-extrabold">
          Informe seu e-mail
        </h2>
      </div>

      <!-- Success message section -->
      <div v-if="isRegistered" class="space-y-6 text-center">
        <div class="rounded-md p-4">
          <p class="text-base">
            Se sua conta existir enviaremos um e-mail de confirmação para
            <span class="font-semibold">{{ email }}</span
            >.<br />
            Por favor, verifique sua caixa de entrada e siga as instruções para alterar sua senha.
          </p>
        </div>
      </div>

      <!-- Registration form -->
      <form v-else class="mt-8 space-y-6" @submit.prevent="handleSubmit">
        <div class="-space-y-px rounded-md shadow-sm">
          <div>
            <label for="email-address" class="sr-only">E-mail</label>
            <input
              id="email-address"
              name="email"
              type="email"
              required
              v-model="email"
              class="relative block w-full appearance-none rounded-none border border-gray-300 px-3 py-2 placeholder-gray-500 focus:z-10 focus:border-blue-500 focus:ring-blue-500 focus:outline-none sm:text-sm"
              placeholder="E-mail"
            />
          </div>
        </div>
        <div class="text-error text-sm" v-if="error">
          {{ error }}
        </div>
        <div>
          <button
            type="submit"
            :disabled="isLoading"
            class="group relative flex w-full justify-center rounded-md border border-transparent px-4 py-2 text-sm font-medium focus:ring-blue-500 focus:ring-offset-2 focus:outline-none hover:focus:ring-2 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <span v-if="isLoading" class="absolute inset-y-0 left-0 flex items-center pl-3">
              <svg
                class="h-5 w-5 animate-spin"
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
                <path
                  class="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                ></path>
              </svg>
            </span>
            {{ isLoading ? 'Carregando...' : 'Redefinir senha' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>
