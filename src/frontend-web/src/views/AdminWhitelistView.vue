<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import ConfirmationModal from '@/components/ConfirmationModal.vue'
import type { ModalStatus } from '@/types/modal'

interface WhitelistEntry {
  id: number
  email: string
  invitedName: string | null
  status: 'PENDING' | 'ALLOWED' | 'DISALLOWED'
  createdAt: string
}

interface PageResponse {
  content: WhitelistEntry[]
  totalPages: number
}

interface AxiosErrorLike {
  response?: { data?: { message?: string } }
}

const entries = ref<WhitelistEntry[]>([])
const loading = ref(true)
const error = ref('')
const currentPage = ref(0)
const totalPages = ref(1)
const rowsPerPage = 20

const actionStatus = ref<Record<number, 'loading' | 'done' | 'error'>>({})

// Modal de convite
const showInviteModal = ref(false)
const inviteEmail = ref('')
const inviteName = ref('')
const inviteModalStatus = ref<ModalStatus>(undefined)
const inviteError = ref('')

const fetchPending = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get<PageResponse>('/v1/admin/whitelist/pending', {
      params: { page: currentPage.value, rows: rowsPerPage },
    })
    entries.value = data.content
    totalPages.value = data.totalPages || 1
  } catch (e: unknown) {
    error.value = (e as AxiosErrorLike)?.response?.data?.message || 'Erro ao carregar lista.'
  } finally {
    loading.value = false
  }
}

const approve = async (entry: WhitelistEntry) => {
  actionStatus.value[entry.id] = 'loading'
  try {
    await api.post(`/v1/admin/whitelist/${entry.id}/approve`)
    actionStatus.value[entry.id] = 'done'
    entries.value = entries.value.filter((e) => e.id !== entry.id)
  } catch {
    actionStatus.value[entry.id] = 'error'
  }
}

const openInviteModal = () => {
  inviteEmail.value = ''
  inviteName.value = ''
  inviteModalStatus.value = undefined
  inviteError.value = ''
  showInviteModal.value = true
}

const confirmInvite = async () => {
  if (!inviteEmail.value) return
  inviteModalStatus.value = 'loading'
  inviteError.value = ''
  try {
    await api.post('/v1/admin/whitelist/invite', {
      email: inviteEmail.value,
      name: inviteName.value || null,
    })
    inviteModalStatus.value = 'success'
    setTimeout(() => {
      showInviteModal.value = false
    }, 1200)
  } catch (e: unknown) {
    inviteError.value = (e as AxiosErrorLike)?.response?.data?.message || 'Erro ao enviar convite.'
    inviteModalStatus.value = 'error'
  }
}

const changePage = (dir: 'next' | 'prev') => {
  if (dir === 'next' && currentPage.value < totalPages.value - 1) currentPage.value++
  else if (dir === 'prev' && currentPage.value > 0) currentPage.value--
  fetchPending()
}

onMounted(fetchPending)
</script>

<template>
  <main class="container flex flex-col mx-auto px-4 py-8">

    <div class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold">Whitelist de Acesso</h1>
        <p class="mt-1 text-sm opacity-70">Gerencie emails pendentes e envie convites.</p>
      </div>
      <button class="rounded px-4 py-2 text-sm font-medium" @click="openInviteModal">
        + Convidar por Email
      </button>
    </div>

    <!-- Pendentes -->
    <section class="rounded-lg p-6 shadow-xl">
      <h2 class="mb-4 text-xl font-semibold">Emails Pendentes</h2>

      <div v-if="loading" class="py-8 text-center">Carregando...</div>
      <div v-else-if="error" class="py-8 text-center text-red-600">{{ error }}</div>
      <div v-else-if="entries.length === 0" class="py-8 text-center opacity-60">
        Nenhum email pendente.
      </div>
      <div v-else>
        <!-- Desktop -->
        <div class="hidden lg:block">
          <div class="grid grid-cols-12 gap-4 border-b py-2 font-semibold">
            <div class="col-span-4">Email</div>
            <div class="col-span-3">Nome</div>
            <div class="col-span-3">Solicitado em</div>
            <div class="col-span-2 text-right">Ação</div>
          </div>
          <ul class="divide-y">
            <li
              v-for="entry in entries"
              :key="entry.id"
              class="grid grid-cols-12 items-center gap-4 py-3"
            >
              <div class="col-span-4 text-sm">{{ entry.email }}</div>
              <div class="col-span-3 text-sm opacity-70">{{ entry.invitedName || '—' }}</div>
              <div class="col-span-3 text-sm opacity-60">
                {{ new Date(entry.createdAt).toLocaleDateString('pt-BR') }}
              </div>
              <div class="col-span-2 text-right">
                <button
                  :disabled="actionStatus[entry.id] === 'loading' || actionStatus[entry.id] === 'done'"
                  class="rounded px-3 py-1 text-sm disabled:opacity-50"
                  @click="approve(entry)"
                >
                  <span v-if="actionStatus[entry.id] === 'loading'">...</span>
                  <span v-else-if="actionStatus[entry.id] === 'error'" class="text-red-600">Erro</span>
                  <span v-else>Aprovar e Convidar</span>
                </button>
              </div>
            </li>
          </ul>
        </div>

        <!-- Mobile -->
        <ul class="flex flex-col gap-3 lg:hidden">
          <li
            v-for="entry in entries"
            :key="entry.id"
            class="rounded-lg border p-4 shadow-sm"
          >
            <p class="font-medium">{{ entry.email }}</p>
            <p class="text-sm opacity-70">{{ entry.invitedName || '—' }}</p>
            <p class="mb-3 text-xs opacity-50">
              {{ new Date(entry.createdAt).toLocaleDateString('pt-BR') }}
            </p>
            <button
              :disabled="actionStatus[entry.id] === 'loading' || actionStatus[entry.id] === 'done'"
              class="w-full rounded px-3 py-2 text-sm disabled:opacity-50"
              @click="approve(entry)"
            >
              <span v-if="actionStatus[entry.id] === 'loading'">Processando...</span>
              <span v-else-if="actionStatus[entry.id] === 'error'" class="text-red-600">Erro ao aprovar</span>
              <span v-else>Aprovar e Convidar</span>
            </button>
          </li>
        </ul>

        <!-- Paginação -->
        <div class="mt-6 flex items-center justify-between">
          <button class="rounded px-3 py-1" :disabled="currentPage === 0" @click="changePage('prev')">
            Anterior
          </button>
          <span>Página {{ currentPage + 1 }} de {{ totalPages }}</span>
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

    <!-- Modal de Convite -->
    <ConfirmationModal
      :visible="showInviteModal"
      title="Convidar por Email"
      confirm-text="Enviar Convite"
      cancel-text="Cancelar"
      loading-text="Enviando..."
      :status="inviteModalStatus"
      @cancel="showInviteModal = false"
      @confirm="confirmInvite"
    >
      <div class="flex flex-col gap-3">
        <div class="flex flex-col gap-1">
          <label class="text-sm font-medium">Nome (opcional)</label>
          <input
            v-model="inviteName"
            type="text"
            placeholder="Nome do convidado"
            class="rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
        <div class="flex flex-col gap-1">
          <label class="text-sm font-medium">Email</label>
          <input
            v-model="inviteEmail"
            type="email"
            placeholder="email@exemplo.com"
            class="rounded border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>
      <template #success>Convite enviado com sucesso!</template>
      <template #error>{{ inviteError }}</template>
    </ConfirmationModal>

  </main>
</template>
