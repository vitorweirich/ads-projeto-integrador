<template>
  <div class="flex flex-1 items-center justify-center">
    <div class="w-full max-w-md rounded-lg border border-[var(--color-border)] bg-[var(--color-bg)] p-8 text-center shadow-lg">
      <div v-if="loading">
        <div class="mb-4 flex justify-center">
          <div class="h-12 w-12 animate-spin rounded-full border-4 border-[var(--color-primary)] border-t-transparent"></div>
        </div>
        <h3 class="mb-2 text-xl font-semibold">Validando sessão...</h3>
        <p class="text-[var(--color-muted)]">Aguarde enquanto realizamos o login.</p>
      </div>

      <div v-else-if="error">
        <div class="mb-4 flex justify-center">
          <svg class="h-16 w-16 text-[var(--color-danger)]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <h3 class="mb-2 text-xl font-semibold text-[var(--color-danger)]">Link inválido ou expirado</h3>
        <p class="mb-6 text-[var(--color-muted)]">O link de transferência de sessão não é mais válido.</p>
        <router-link :to="loginRoute" class="btn inline-block">
          Ir para Login
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref(false)

const loginRoute = computed(() => {
  const destinationParam = route.query.destination as string | undefined
  const destination = resolveDestination(destinationParam)
  
  return {
    path: '/login',
    query: destination !== '/' ? { redirect: destination } : undefined,
  }
})

function resolveDestination(destination: string | undefined): string {
  if (!destination) return '/'

  try {
    const decoded = decodeURIComponent(destination)

    if (decoded.startsWith('/') && !decoded.startsWith('//') && !decoded.includes('http')) {
      return decoded
    }

    return '/'
  } catch {
    return '/'
  }
}

onMounted(async () => {
  const token = route.params.token as string
  const destinationParam = route.query.destination as string | undefined

  const destination = resolveDestination(destinationParam)

  try {
    if (!authStore.isAuthenticated) {
      await api.post('/v1/api/auth/session-exchange', { token })
    }

    router.replace(destination)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
})
</script>
