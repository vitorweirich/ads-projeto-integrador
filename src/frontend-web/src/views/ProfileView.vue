<script setup lang="ts">
import { computed, watchEffect } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import { ref } from 'vue'
import api from '@/services/api'
import { formatBytes } from '@/utils/filesUtils'

const auth = useAuthStore()
const router = useRouter()

const showMfaModal = ref(false)
const mfaQrCode = ref<string | null>(null)
const mfaSecret = ref<string | null>(null)
const mfaLoading = ref(false)
const mfaError = ref<string | null>(null)
const mfaCode = ref('')
const mfaStep = ref<'init' | 'qr' | 'confirm' | 'success'>('init')

const showDeleteModal = ref(false)
const deleteLoading = ref(false)
const deleteError = ref<string | null>(null)

async function confirmDeleteAccount() {
  deleteLoading.value = true
  deleteError.value = null
  try {
    await api.delete('/v1/api/auth/account')
    await auth.logout()
    router.replace({ name: 'home' })
  } catch (err: any) {
    deleteError.value = 'Erro ao excluir a conta. Tente novamente.'
  } finally {
    deleteLoading.value = false
  }
}

// Computed para calcular percentual usado da quota
const quotaPercentage = computed(() => {
  if (!auth.user?.storage) return 0
  const { usedQuota, totalQuota } = auth.user.storage
  if (!totalQuota || totalQuota === 0) return 0
  return Math.min((usedQuota / totalQuota) * 100, 100)
})

async function openMfaModal() {
  showMfaModal.value = true
  mfaStep.value = 'init'
  mfaQrCode.value = null
  mfaSecret.value = null
  mfaError.value = null
  mfaCode.value = ''
  await initiateMfa()
}

async function initiateMfa() {
  mfaLoading.value = true
  mfaError.value = null
  try {
    // Inicia o setup e pega o token
    const resp = await api.post('/v1/api/auth/mfa/setup/initiate')
    const { token } = resp.data
    // Busca o QR code
    const qrResp = await api.post('/v1/api/auth/mfa/setup', null, {
      responseType: 'text',
      headers: { Authorization: `Bearer ${token}` },
    })

    mfaQrCode.value = `data:image/svg+xml;base64,${btoa(qrResp.data)}`
    mfaSecret.value = token
    mfaStep.value = 'qr'
  } catch (err: any) {
    mfaError.value = 'Erro ao iniciar setup do MFA.'
  } finally {
    mfaLoading.value = false
  }
}

async function confirmMfa() {
  mfaLoading.value = true
  mfaError.value = null
  try {
    await api.post('/v1/api/auth/mfa/confirm', null, {
      params: { code: mfaCode.value },
      headers: { Authorization: `Bearer ${mfaSecret.value}` },
    })
    mfaStep.value = 'success'
    await auth.fetchUser(true)
  } catch (err: any) {
    mfaError.value = 'Código inválido ou erro ao confirmar MFA.'
  } finally {
    mfaLoading.value = false
  }
}

watchEffect(() => {
  if (!auth.loading && !auth.user) {
    router.replace({ name: 'login' })
  }
})
</script>

<template>
  <main class="flex items-center justify-center">
    <div class="w-full max-w-md p-6">
      <div class="mt-7 rounded-xl border shadow-sm">
        <div class="p-4 sm:p-7">
          <div class="text-center">
            <h1 class="dark: block text-2xl font-bold">Perfil do Usuário</h1>
          </div>
          <div
            v-if="auth.user && auth.user.storage"
            class="mt-6 rounded border bg-gray-50 p-4 dark:bg-gray-800"
          >
            <h2 class="mb-2 text-center text-lg font-semibold">Uso de Armazenamento</h2>

            <div class="mb-1 flex justify-between text-sm text-gray-600 dark:text-gray-300">
              <span>Usado: {{ formatBytes(auth.user.storage.usedQuota) }}</span>
              <span>Total: {{ formatBytes(auth.user.storage.totalQuota) }}</span>
            </div>

            <div class="h-4 w-full overflow-hidden rounded-full bg-gray-300 dark:bg-gray-700">
              <div
                class="h-4 rounded-full bg-blue-600 transition-all"
                :style="{ width: quotaPercentage + '%' }"
              ></div>
            </div>

            <p class="mt-2 text-center text-sm text-gray-700 dark:text-gray-400">
              {{ quotaPercentage.toFixed(1) }}% da cota usada
            </p>
          </div>
          <div v-if="auth.user && auth.user.mfaEnabled" class="mt-4 text-center">
            <span
              data-cy="enabled-mfa-span"
              class="inline-block rounded px-3 py-1 text-sm font-medium"
              >MFA habilitado</span
            >
          </div>
          <div v-if="auth.user && !auth.user.mfaEnabled" class="mt-4 text-center">
            <button
              data-cy="enable-mfa"
              @click="openMfaModal"
              class="rounded px-3 py-1 text-sm font-medium"
            >
              Habilitar MFA
            </button>
          </div>
          <div v-if="auth.loading" class="mt-5 text-center">
            <div class="">Carregando informações...</div>
          </div>
          <div v-else-if="!auth.user" class="mt-5 text-center">
            <div class="">Usuário não autenticado.</div>
          </div>
          <div v-else class="mt-5">
            <div class="mb-4 flex flex-col items-center">
              <div class="mb-2 text-lg font-semibold">Nome:</div>
              <div class="dark:">{{ auth.user.name }}</div>
            </div>
            <div class="mb-4 flex flex-col items-center">
              <div class="mb-2 text-lg font-semibold">Email:</div>
              <div class="dark:">{{ auth.user.email }}</div>
            </div>
            <div class="mt-6 flex justify-center gap-2 text-center">
              <button
                @click="showDeleteModal = true"
                class="bg-danger rounded px-4 py-2 font-semibold"
              >
                Excluir conta
              </button>
              <button @click="auth.logout()" class="rounded px-4 py-2 font-semibold">Sair</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal MFA -->
    <div
      v-if="showMfaModal"
      class="bg-opacity-60 dark:bg-opacity-80 fixed inset-0 z-50 flex items-center justify-center bg-black"
    >
      <div
        class="relative w-full max-w-lg rounded-lg bg-[var(--color-bg)] p-6 text-[var(--color-text)] shadow-lg dark:shadow-[0_4px_15px_rgba(255,255,255,0.15)]"
      >
        <button
          @click="showMfaModal = false"
          class="absolute top-2 right-2 flex h-8 w-8 items-center justify-center rounded-full bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-hover)] focus:ring-2 focus:ring-[var(--color-primary-focus)] focus:outline-none"
          aria-label="Fechar modal"
        >
          <span class="mb-[2px] leading-[1]">&times;</span>
        </button>

        <h2 class="mt-2 mb-4 text-center text-xl font-bold">
          Ativar Autenticação em 2 Fatores (MFA)
        </h2>

        <div v-if="mfaLoading" class="py-4 text-center">Carregando...</div>

        <div v-else>
          <form v-if="mfaStep === 'qr'" @submit.prevent="confirmMfa" class="w-full">
            <p class="mb-2 text-center">Escaneie o QR code abaixo no seu app autenticador:</p>

            <div class="mb-4 flex justify-center">
              <img
                data-cy="qr-code"
                :src="mfaQrCode || ''"
                alt="QR Code MFA"
                class="h-[250px] w-[250px]"
              />
            </div>

            <input
              data-cy="token-input"
              v-model="mfaCode"
              type="text"
              maxlength="6"
              placeholder="Código do app"
              class="mb-2 w-full rounded border border-[var(--color-border)] bg-[var(--color-bg)] px-3 py-2 text-[var(--color-text)] transition-colors duration-200 focus:border-[var(--color-primary-focus)] focus:ring-1 focus:ring-[var(--color-primary-focus)] focus:outline-none"
            />

            <button
              data-cy="confirm-mfa"
              type="submit"
              :disabled="mfaLoading || !mfaCode"
              class="w-full rounded bg-[var(--color-primary)] py-2 font-semibold text-white transition-colors duration-200 hover:bg-[var(--color-primary-hover)] focus:ring-2 focus:ring-[var(--color-primary-focus)] focus:outline-none disabled:cursor-not-allowed disabled:opacity-50"
            >
              Confirmar
            </button>

            <div v-if="mfaError" class="mt-2 text-center text-red-500">
              {{ mfaError }}
            </div>
          </form>

          <div v-else-if="mfaStep === 'success'" class="text-center">
            <div class="mb-2 font-semibold">MFA ativado com sucesso!</div>

            <button
              data-cy="close-mfa-modal-button"
              @click="showMfaModal = false"
              class="mt-2 rounded bg-[var(--color-primary)] px-4 py-2 font-semibold text-white transition-colors duration-200 hover:bg-[var(--color-primary-hover)] focus:ring-2 focus:ring-[var(--color-primary-focus)] focus:outline-none"
            >
              Fechar
            </button>
          </div>

          <div v-else-if="mfaError" class="text-center text-red-500">
            {{ mfaError }}
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de confirmação de exclusão -->
    <div
      v-if="showDeleteModal"
      class="bg-opacity-60 dark:bg-opacity-80 fixed inset-0 z-50 flex items-center justify-center bg-black"
    >
      <div
        class="relative w-full max-w-md rounded-lg bg-[var(--color-bg)] p-6 text-[var(--color-text)] shadow-lg dark:shadow-[0_4px_15px_rgba(255,255,255,0.15)]"
      >
        <button
          @click="showDeleteModal = false"
          class="absolute top-2 right-2 flex h-8 w-8 items-center justify-center rounded-full bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-hover)] focus:ring-2 focus:ring-[var(--color-primary-focus)] focus:outline-none"
          aria-label="Fechar modal"
        >
          <span class="mb-[2px] leading-[1]">&times;</span>
        </button>

        <h2 class="mt-2 mb-4 text-center text-xl font-bold">Confirmar exclusão</h2>
        <p class="mb-4 text-center text-sm">
          Tem certeza que deseja excluir sua conta? Esta ação é irreversível.
        </p>

        <div class="flex justify-center gap-4">
          <button
            @click="showDeleteModal = false"
            class="rounded bg-gray-300 px-4 py-2 font-semibold text-black hover:bg-gray-400 dark:bg-gray-700 dark:text-white dark:hover:bg-gray-600"
          >
            Cancelar
          </button>
          <button
            @click="confirmDeleteAccount"
            :disabled="deleteLoading"
            class="bg-danger rounded px-4 py-2 font-semibold"
          >
            {{ deleteLoading ? 'Excluindo...' : 'Confirmar' }}
          </button>
        </div>

        <div v-if="deleteError" class="mt-3 text-center text-red-500">
          {{ deleteError }}
        </div>
      </div>
    </div>
  </main>
</template>
