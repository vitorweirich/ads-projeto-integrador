<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const auth = useAuthStore()
const router = useRouter()
const code = ref('')

const error = ref('')

const verifyMfa = async () => {
  try {
    await api.post(
      `/v1/api/auth/mfa/verify?code=${code.value}`,
      {},
      {
        headers: {
          Authorization: `Bearer ${auth.mfaToken}`,
        },
      },
    )

    auth.setMfaToken(null)
    const redirect = router.currentRoute.value.query.redirect as string
    router.push(redirect || '/')
  } catch (err: any) {
    console.error(err)
    error.value = err?.response?.data?.message || 'Falha na verificação do MFA'
  }
}
</script>

<template>
  <form
    @submit.prevent="verifyMfa"
    class="mx-auto mt-10 w-full max-w-md flex-col items-center justify-center"
  >
    <h1 class="mb-4 text-xl font-bold">Verificação de MFA</h1>
    <input
      data-cy="topt-token-input"
      type="text"
      v-model="code"
      @input="code = code.replace(/\D/g, '').slice(0, 6)"
      maxlength="6"
      inputmode="numeric"
      pattern="[0-9]*"
      placeholder="Digite o código do Google Authenticator"
      class="mb-3 w-full rounded border px-3 py-2"
    />
    <div v-if="error" class="text-error mb-3">{{ error }}</div>
    <button data-cy="verify-mfa" type="submit" class="rounded px-4 py-2">Verificar</button>
  </form>
</template>
