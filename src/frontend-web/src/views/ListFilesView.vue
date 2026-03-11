<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { formatBytes } from '@/utils/filesUtils'
import Pagination from '@/components/Pagination.vue'
import { useToastStore } from '@/stores/toast'

type File = {
  id: number
  name: string
  uploaded: boolean
  createdAt: string
  watchUrl: string
  shareUrl: string
  expiresIn: string
  size: number
  contentType: string
}

const files = ref<File[]>([])
const loading = ref(true)
const linkLoading = ref(false)
const error = ref('')

const currentPage = ref(0)
const totalPages = ref(1)
const rowsPerPage = 10

const copyLink = async (signedUrl: string) => {
  if (signedUrl) {
    try {
      await navigator.clipboard.writeText(signedUrl)
      useToastStore().show('Link copiado com sucesso!', 'success')
    } catch {}
  }
}

const fetchFiles = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get('/v1/files/me', {
      params: {
        page: currentPage.value,
        rows: rowsPerPage,
      },
    })
    files.value = data.content.map((file: File) => ({
      ...file,
      watchUrl:
        file.shareUrl && file.contentType.startsWith('video/')
          ? `${window.location.origin}/watch/${file.shareUrl?.split('/')?.pop()}`
          : undefined,
    }))
    totalPages.value = data.totalPages || 1
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Erro ao carregar arquivos.'
  } finally {
    loading.value = false
  }
}

const fetchFile = async (id: number) => {
  linkLoading.value = true
  error.value = ''
  try {
    const { data } = await api.get(`/v1/files/${id}`)
    const file = {
      ...data,
      shareUrl: data.signedUrl,
      watchUrl: data.metadata?.contentType?.startsWith('video/')
        ? `${window.location.origin}/watch/${data.signedUrl?.split('/').at(-1)}`
        : undefined,
    }
    const index = files.value.findIndex((v) => v.id === id)
    if (index !== -1) {
      files.value[index] = { ...files.value[index], ...file }
      copyLink(file.watchUrl || file.shareUrl)
    }
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Erro ao gerar link para o file.'
  } finally {
    linkLoading.value = false
  }
}

onMounted(fetchFiles)
</script>

<template>
  <main class="container mx-auto flex-col px-4 py-8">
    <section class="mb-8 text-center">
      <h1 class="mb-4 text-3xl font-bold">Meus Arquivos Temporários</h1>
      <p class="mb-6 text-lg">Veja e gerencie os arquivos enviados.</p>
      <router-link to="/upload" class="btn inline-block rounded px-4 py-2 font-semibold"
        >Enviar Novo Arquivo</router-link
      >
    </section>
    <section class="rounded-lg p-6 shadow-xl">
      <div v-if="loading" class="py-8 text-center">Carregando...</div>
      <div v-else-if="error" class="text-error py-8 text-center">{{ error }}</div>
      <div v-else>
        <div v-if="files.length === 0" class="text-center">Nenhum arquivo encontrado.</div>
        <ul v-else class="divide-y">
          <li v-for="file in files" :key="file.id" class="grid grid-cols-7 items-center gap-2 py-4">
            <div class="col-span-4">
              <div class="group relative max-w-full">
                <div class="truncate text-lg font-bold" :title="file.name">
                  {{ file.name }}
                </div>
                <div
                  class="absolute top-full left-0 z-10 mt-1 hidden w-max max-w-screen-md rounded bg-gray-800 px-2 py-1 text-sm text-white shadow group-hover:block dark:bg-gray-500"
                >
                  {{ file.name }}
                </div>
              </div>
            </div>
            <div class="col-span-1">
              <span class="text-sm">
                {{ file.uploaded ? 'Upload realizado' : 'Upload pendente' }}
              </span>
            </div>
            <div class="col-span-1">
              <span class="text-sm">
                {{ formatBytes(file.size) }}
              </span>
            </div>
            <div class="col-span-1 flex flex-col justify-end gap-2">
              <div
                v-if="file.watchUrl || file.shareUrl"
                class="flex flex-1 cursor-pointer items-center gap-2 rounded border bg-gray-50 px-2 py-1 text-sm hover:bg-gray-100 dark:bg-gray-800 dark:hover:bg-gray-700"
                @click="copyLink(file.watchUrl || file.shareUrl)"
              >
                <span class="flex-1 truncate">{{ file.watchUrl || file.shareUrl }}</span>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-4 w-4"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path d="M8 3a1 1 0 011-1h2a1 1 0 110 2H9a1 1 0 01-1-1z" />
                  <path
                    d="M6 3a2 2 0 00-2 2v11a2 2 0 002 2h8a2 2 0 002-2V5a2 2 0 00-2-2 3 3 0 01-3 3H9a3 3 0 01-3-3z"
                  />
                </svg>
              </div>
              <button
                v-else
                class="btn-secondary"
                @click="fetchFile(file.id)"
                :disabled="linkLoading"
              >
                <template v-if="linkLoading">
                  <svg
                    class="mr-1 inline h-4 w-4 animate-spin"
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
                  Gerando...
                </template>
                <template v-else> Gerar Link </template>
              </button>
              <router-link
                :to="{ name: 'view-file', params: { id: file.id } }"
                class="btn rounded px-3 py-1 text-center"
                :class="{ 'pointer-events-none cursor-not-allowed opacity-50': !file.uploaded }"
              >
                Ver
              </router-link>
            </div>
          </li>
        </ul>

        <!-- Paginação -->
        <Pagination
          :currentPage="currentPage"
          :totalPages="totalPages"
          @page-change="
            (page: number) => {
              currentPage = page
              fetchFiles()
            }
          "
        />
      </div>
    </section>
  </main>
</template>
