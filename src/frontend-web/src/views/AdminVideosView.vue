<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import ConfirmationModal from '@/components/ConfirmationModal.vue'
import type { ModalStatus } from '@/types/modal'

type Video = {
  id: number
  name: string
  uploaded: boolean
  createdAt: string
  shareUrl: string
  expiresIn: string
}

// TODO: Implementar pesquisa/filtros
// TODO: Implementar relatorios de videos
// TODO: Verificação periódica de uso de armazenamento global no storage
// TODO: Implementar limite/controle de armazenamento por usuario?

const videos = ref<Video[]>([])
const loading = ref(true)
const error = ref('')

const currentPage = ref(0)
const totalPages = ref(1)
const rowsPerPage = 10

const showModal = ref(false)
const selectedVideoUrl = ref('')
const selectedVideoName = ref('')

const showDeleteModal = ref(false)
const videoToDelete = ref<Video | null>(null)
const deleteStatus = ref<ModalStatus>(undefined)

const fetchVideos = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get('/v1/admin/videos', {
      params: {
        sort: 'asc',
        rows: rowsPerPage,
        page: currentPage.value,
      },
    })
    videos.value = data.content
    totalPages.value = data.totalPages || 1
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Erro ao carregar vídeos.'
  } finally {
    loading.value = false
  }
}

const viewVideo = async (video: Video) => {
  selectedVideoName.value = video.name
  try {
    const expiresAt = new Date(video.expiresIn)
    if (expiresAt > new Date()) {
      selectedVideoUrl.value = video.shareUrl
    } else {
      const { data } = await api.get(`/v1/admin/videos/${video.id}`)
      selectedVideoUrl.value = data.signedUrl
    }
    showModal.value = true
  } catch (e: any) {
    console.error('Erro ao buscar URL do vídeo:', e)
    alert('Não foi possível carregar o vídeo.')
  }
}

const openDeleteModal = (video: Video) => {
  videoToDelete.value = video
  deleteStatus.value = undefined
  showDeleteModal.value = true
}

const confirmDeleteVideo = async () => {
  if (!videoToDelete.value) return
  deleteStatus.value = 'loading'
  try {
    await api.delete(`/v1/admin/videos/${videoToDelete.value.id}`)
    deleteStatus.value = 'success'
    await fetchVideos()
    setTimeout(() => {
      showDeleteModal.value = false
    }, 1000)
  } catch (e: any) {
    deleteStatus.value = 'error'
  }
}

const changePage = (direction: 'next' | 'prev') => {
  if (direction === 'next' && currentPage.value < totalPages.value - 1) {
    currentPage.value++
    fetchVideos()
  } else if (direction === 'prev' && currentPage.value > 0) {
    currentPage.value--
    fetchVideos()
  }
}

onMounted(fetchVideos)
</script>

<template>
  <main class="container mx-auto flex-col px-4 py-8">
    <section class="mb-8 text-center">
      <h1 class="mb-4 text-3xl font-bold">Gerenciar Vídeos</h1>
      <p class="mb-6 text-lg">Liste e gerencie todos os vídeos da plataforma.</p>
    </section>

    <section class="rounded-lg p-6 shadow-xl">
      <div v-if="loading" class="py-8 text-center">Carregando...</div>
      <div v-else-if="error" class="text-error py-8 text-center">{{ error }}</div>
      <div v-else>
        <div v-if="videos.length === 0" class="text-center">Nenhum vídeo encontrado.</div>

        <div v-else>
          <div class="grid grid-cols-4 gap-4 border-b py-2 font-semibold text-gray-700">
            <div>ID</div>
            <div>Nome</div>
            <div>Status</div>
            <div class="text-right">Ações</div>
          </div>

          <ul class="divide-y">
            <li
              v-for="video in videos"
              :key="video.id"
              class="grid grid-cols-4 items-center gap-4 py-4"
            >
              <div class="font-semibold">#{{ video.id }}</div>
              <div class="group relative max-w-full">
                <div class="truncate text-lg font-bold" :title="video.name">
                  {{ video.name }}
                </div>
                <!-- TODO: Usar css vars para background -->
                <div
                  class="absolute top-full left-0 z-10 mt-1 hidden w-max max-w-screen-md rounded bg-gray-800 px-2 py-1 text-sm text-white shadow group-hover:block dark:bg-gray-500"
                >
                  {{ video.name }}
                </div>
              </div>
              <div>
                <span :class="video.uploaded ? 'text-green-600' : 'text-red-600'">
                  {{ video.uploaded ? 'Upload completo' : 'Pendente' }}
                </span>
              </div>
              <div class="flex justify-end gap-2">
                <button class="rounded px-3 py-1" @click="viewVideo(video)">Visualizar</button>
                <button class="bg-danger rounded px-3 py-1" @click="openDeleteModal(video)">
                  Deletar
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

    <!-- Modal de vídeo -->
    <div
      v-if="showModal"
      class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black"
    >
      <div class="relative w-full max-w-3xl rounded-lg bg-black p-6 shadow-lg">
        <h2 class="mb-4 text-xl font-bold">{{ selectedVideoName }}</h2>
        <video
          v-if="selectedVideoUrl"
          class="max-h-[70vh] w-full rounded"
          controls
          :src="selectedVideoUrl"
        />
        <button
          class="bg-secondary absolute top-2 right-2 flex h-8 w-8 items-center justify-center rounded-full text-white focus:outline-none"
          @click="showModal = false"
          aria-label="Fechar modal"
        >
          <span class="mb-[2px] leading-[1]">&times;</span>
        </button>
      </div>
    </div>

    <!-- Modal de Deleção -->
    <ConfirmationModal
      :visible="showDeleteModal"
      title="Confirmar exclusão"
      :status="deleteStatus"
      cancelText="Cancelar"
      confirmText="Confirmar"
      loadingText="Deletando..."
      @cancel="showDeleteModal = false"
      @confirm="confirmDeleteVideo"
    >
      <p>
        Deseja realmente <span class="font-semibold text-red-600">excluir</span> o vídeo
        <strong>{{ videoToDelete?.name }}</strong
        >?
      </p>
    </ConfirmationModal>
  </main>
</template>
