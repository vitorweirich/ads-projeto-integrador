<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const auth = useAuthStore()

const router = useRouter()
const name = ref('')
const email = ref('')
const password = ref('')
const error = ref('')
const isRegistered = ref(false)
const isLoading = ref(false)

// password validation rules
const hasMinLength = computed(() => password.value.length >= 6)
const hasUppercase = computed(() => /[A-Z]/.test(password.value))
const hasSpecialChar = computed(() => /[^A-Za-z0-9]/.test(password.value))
const passwordValid = computed(
  () => hasMinLength.value && hasUppercase.value && hasSpecialChar.value,
)

const handlePostLoginRedirect = () => {
  const redirect = router.currentRoute.value.query.redirect as string
  router.push(redirect || '/')
}

const handleRegister = async () => {
  if (!passwordValid.value) {
    error.value =
      'A senha deve ter ao menos 6 caracteres, incluir uma letra maiúscula e um caractere especial.'
    return
  }

  isLoading.value = true
  try {
    await api.post('/v1/api/auth/register', {
      name: name.value,
      email: email.value,
      password: password.value,
    })
    isRegistered.value = true

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    console.error(err)
    error.value = err?.response?.data?.message || 'Ocorreu um erro durante o cadastro'
  } finally {
    isLoading.value = false
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
  <div class="flex items-center justify-center px-4 py-12 sm:px-6 lg:px-8" data-cy="register-page">
    <div class="w-full max-w-md space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold" data-cy="register-title">
          {{ isRegistered ? 'Cadastro realizado com sucesso!' : 'Crie sua conta' }}
        </h2>
      </div>

      <!-- Success message section -->
      <div v-if="isRegistered" class="space-y-6 text-center" data-cy="register-success-message">
        <div class="rounded-md p-4">
          <p class="text-base">
            Enviamos um e-mail de confirmação para
            <span class="font-semibold" data-cy="register-success-email">{{ email }}</span
            >.<br />
            Por favor, verifique sua caixa de entrada e siga as instruções para verificar sua conta.
          </p>
        </div>
        <div class="mt-4">
          <p class="mb-4" data-cy="register-success-text">
            Pronto para começar? Verifique seu e-mail e depois faça login.
          </p>
          <button
            @click="router.push('/login')"
            class="group relative flex w-full justify-center rounded-md border border-transparent px-4 py-2 text-sm font-medium focus:ring-blue-500 focus:ring-offset-2 focus:outline-none hover:focus:ring-2"
            data-cy="go-to-login-button"
          >
            Ir para o Login
          </button>
        </div>
      </div>

      <!-- Registration form -->
      <form v-else class="mt-8 space-y-6" @submit.prevent="handleRegister" data-cy="register-form">
        <div class="-space-y-px rounded-md shadow-sm">
          <div>
            <label for="name" class="sr-only">Nome</label>
            <input
              id="name"
              name="name"
              type="text"
              required
              v-model="name"
              class="relative block w-full appearance-none rounded-none rounded-t-md border border-gray-300 px-3 py-2 placeholder-gray-500 focus:z-10 focus:border-blue-500 focus:ring-blue-500 focus:outline-none sm:text-sm"
              placeholder="Nome completo"
              data-cy="input-name"
            />
          </div>
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
              aria-describedby="password-requirements"
            />
            <!-- password requirements feedback -->
            <div
              v-if="password"
              id="password-requirements"
              class="mt-2 text-sm"
              role="region"
              aria-labelledby="password-requirements-title"
              data-cy="password-requirements-box"
            >
              <div
                class="rounded-md border border-gray-100 bg-gray-300 p-3 dark:border-gray-800 dark:bg-gray-700"
              >
                <p id="password-requirements-title" class="mb-2 text-sm font-medium">
                  Requisitos da senha
                </p>
                <ul class="space-y-1">
                  <li
                    :class="hasMinLength ? 'text-green-600' : 'text-red-600'"
                    data-cy="pw-rule-length"
                  >
                    <span v-if="hasMinLength">✔</span><span v-else>✖</span>
                    Mínimo 6 caracteres
                  </li>
                  <li
                    :class="hasUppercase ? 'text-green-600' : 'text-red-600'"
                    data-cy="pw-rule-uppercase"
                  >
                    <span v-if="hasUppercase">✔</span><span v-else>✖</span>
                    Uma letra maiúscula
                  </li>
                  <li
                    :class="hasSpecialChar ? 'text-green-600' : 'text-red-600'"
                    data-cy="pw-rule-special"
                  >
                    <span v-if="hasSpecialChar">✔</span><span v-else>✖</span>
                    Um caractere especial
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
        <div class="text-error text-sm" v-if="error" data-cy="register-error">
          {{ error }}
        </div>
        <div>
          <button
            type="submit"
            :disabled="isLoading || !passwordValid"
            class="group relative flex w-full justify-center rounded-md border border-transparent px-4 py-2 text-sm font-medium focus:ring-blue-500 focus:ring-offset-2 focus:outline-none hover:focus:ring-2 disabled:cursor-not-allowed disabled:opacity-50"
            data-cy="submit-register"
          >
            <span
              v-if="isLoading"
              class="absolute inset-y-0 left-0 flex items-center pl-3"
              data-cy="loading-spinner"
            >
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
            {{ isLoading ? 'Cadastrando...' : 'Cadastrar' }}
          </button>
        </div>
        <div class="text-center text-sm" data-cy="already-have-account">
          <router-link to="/login" class="" data-cy="link-login">
            Já tem uma conta? Faça login
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>
