<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import type { AdminUserProjection, ListUsersAdminResponse } from '@/types/user'
import { calculateUsedQuotaPercentage, formatBytes } from '@/utils/filesUtils'

// TODO: Implementar pesquisa/filtros
// TODO: Implementar relatorios de usuarios
// TODO: Implementar visualização de uso de armazenamente de cada usuario

const users = ref<AdminUserProjection[]>([])
const loading = ref(true)
const error = ref('')

const currentPage = ref(0)
const totalPages = ref(1)
const rowsPerPage = 10

const showConfirmModal = ref(false)
const userToReset = ref<AdminUserProjection | null>(null)
const resetStatus = ref('')

const fetchUsers = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get<ListUsersAdminResponse>('/v1/admin/users', {
      params: {
        sort: 'asc',
        rows: rowsPerPage,
        page: currentPage.value,
      },
    })
    users.value = data.content.map((content) => ({
      ...content,
      usedQuota: calculateUsedQuotaPercentage(
        content.settings.storageLimitBytes,
        content.totalSize,
      ).toFixed(1),
    }))
    totalPages.value = data.totalPages || 1
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Erro ao carregar usuários.'
  } finally {
    loading.value = false
  }
}

const openResetModal = (user: AdminUserProjection) => {
  userToReset.value = user
  resetStatus.value = ''
  showConfirmModal.value = true
}

const confirmResetMfa = async () => {
  if (!userToReset.value) return
  resetStatus.value = 'loading'
  try {
    await api.post(`/v1/admin/users/${userToReset.value.id}/reset-mfa`)
    resetStatus.value = 'success'
    fetchUsers()
    setTimeout(() => {
      showConfirmModal.value = false
    }, 1000)
  } catch (e: any) {
    resetStatus.value = 'error'
  }
}

const changePage = (direction: 'next' | 'prev') => {
  if (direction === 'next' && currentPage.value < totalPages.value - 1) {
    currentPage.value++
    fetchUsers()
  } else if (direction === 'prev' && currentPage.value > 0) {
    currentPage.value--
    fetchUsers()
  }
}

onMounted(fetchUsers)
</script>

<template>
  <main class="container mx-auto flex-col px-4 py-8">
    <section class="mb-8 text-center">
      <h1 class="mb-4 text-3xl font-bold">Gerenciar Usuários</h1>
      <p class="mb-6 text-lg">Veja e gerencie os usuários cadastrados.</p>
    </section>

    <section class="rounded-lg p-6 shadow-xl">
      <div v-if="loading" class="py-8 text-center">Carregando...</div>
      <div v-else-if="error" class="text-error py-8 text-center">{{ error }}</div>
      <div v-else>
        <div v-if="users.length === 0" class="text-center">Nenhum usuário encontrado.</div>

        <div v-else>
          <div class="grid grid-cols-13 gap-4 border-b py-2 font-semibold text-gray-700">
            <div class="col-span-3">Nome</div>
            <div class="col-span-4">Email</div>
            <div class="col-span-2">MFA Status</div>
            <div class="col-span-2">Storage Quota</div>
            <div class="col-span-2 text-right">Ações</div>
          </div>
          <ul class="divide-y">
            <li
              v-for="user in users"
              :key="user.id"
              class="grid grid-cols-13 items-center gap-4 py-4"
            >
              <div class="col-span-3 font-medium">{{ user.name }}</div>
              <div class="col-span-4 text-sm text-gray-600">{{ user.email }}</div>
              <div class="col-span-2 text-sm">
                MFA:
                <span :class="user.mfaEnabled ? 'text-green-600' : 'text-red-600'">
                  {{ user.mfaEnabled ? 'Ativo' : 'Desativado' }}
                </span>
              </div>
              <div
                class="col-span-2 flex flex-col items-start gap-2"
                :title="`${user.usedQuota}% usado`"
              >
                <span class="text-sm text-gray-700 dark:text-gray-300">
                  {{ formatBytes(user.totalSize) }} /
                  {{ formatBytes(user.settings.storageLimitBytes) }}
                </span>

                <div class="h-2 w-24 overflow-hidden rounded-full bg-gray-300 dark:bg-gray-700">
                  <div
                    class="h-full bg-blue-600 transition-all"
                    :style="{
                      width: user.usedQuota + '%',
                    }"
                  ></div>
                </div>
              </div>
              <div class="col-span-2 text-right">
                <button
                  :disabled="!user.mfaEnabled"
                  class="rounded px-3 py-1"
                  @click="openResetModal(user)"
                >
                  Resetar MFA
                </button>
              </div>
            </li>
          </ul>
        </div>

        <!-- Paginação -->
        <div class="mt-6 flex items-center justify-between">
          <button
            class="rounded px-3 py-1"
            :disabled="currentPage === 0"
            @click="changePage('prev')"
          >
            Anterior
          </button>
          <div>Página {{ currentPage + 1 }} de {{ totalPages }}</div>
          <button
            class="rounded px-3 py-1"
            :disabled="currentPage >= totalPages - 1"
            @click="changePage('next')"
          >
            Próxima
          </button>
        </div>
      </div>
    </section>

    <!-- Modal de Confirmação -->
    <div
      v-if="showConfirmModal"
      class="bg-opacity-50 fixed inset-0 flex items-center justify-center bg-black"
    >
      <div class="w-96 rounded-lg bg-fuchsia-700 p-6 shadow-lg">
        <h2 class="mb-4 text-xl font-semibold">Confirmar redefinição do MFA</h2>
        <p class="mb-4">
          Tem certeza que deseja resetar o MFA de <strong>{{ userToReset?.name }}</strong
          >?
        </p>

        <div v-if="resetStatus === 'success'" class="mb-4 text-green-600">
          MFA resetado com sucesso.
        </div>
        <div v-if="resetStatus === 'error'" class="mb-4 text-red-600">Erro ao resetar MFA.</div>

        <div class="flex justify-end gap-2">
          <button
            class="rounded px-3 py-1"
            @click="showConfirmModal = false"
            :disabled="resetStatus === 'loading'"
          >
            Cancelar
          </button>
          <button
            class="bg-secondary rounded px-3 py-1"
            @click="confirmResetMfa"
            :disabled="resetStatus === 'loading'"
          >
            {{ resetStatus === 'loading' ? 'Processando...' : 'Confirmar' }}
          </button>
        </div>
      </div>
    </div>
  </main>
</template>
