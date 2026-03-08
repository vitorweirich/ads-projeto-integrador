<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api'

const route = useRoute()
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const token = route.params.token as string
    await api.get(`/v1/api/auth/confirm/${token}`)
    loading.value = false

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    loading.value = false
    error.value =
      err.response?.data?.message ||
      'Erro ao confirmar email. Token inválido ou expirado. Crie sua conta novamente!'
  }
})
</script>

<template>
  <div class="flex items-center justify-center">
    <div class="w-full max-w-md p-6">
      <div class="mt-7 rounded-xl border">
        <div class="p-4 sm:p-7">
          <div class="text-center">
            <h1 class="dark: block text-2xl font-bold">Confirmação de Email</h1>
          </div>
          <div v-if="loading" class="mt-5 text-center">
            <div class="">Verificando seu email...</div>
          </div>
          <div v-else-if="error" class="mt-5 text-center">
            <div class="text-error">
              {{ error }}
            </div>
          </div>
          <div v-else class="mt-5 text-center">
            <div class="">Email confirmado com sucesso!</div>
            <router-link
              to="/login"
              class="mt-3 inline-block"
              data-cy="after-register-to-login-button"
            >
              Ir para o login
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
