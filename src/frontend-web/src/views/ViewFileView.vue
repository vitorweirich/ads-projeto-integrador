<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/services/api'
import ConfirmationModal from '@/components/ConfirmationModal.vue'
import type { ModalStatus } from '@/types/modal'
import FileVisualizer from '@/components/FileVisualizer.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const error = ref('')
const fileData = ref<{
  signedUrl: string
  watchUrl: string
  metadata: {
    fileName?: string
    fileType: string
  }
  fileId: number
  expirationDate: string
} | null>(null)
const copied = ref(false)

const showDeleteModal = ref(false)
const deleteStatus = ref<ModalStatus>(undefined)

const fetchFile = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await api.get(`/v1/files/${route.params.id}`)
    fileData.value = {
      ...data,
      watchUrl: `${window.location.origin}/watch/${data.signedUrl?.split('/').at(-1)}`,
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (e: any) {
    error.value = e?.response?.data?.message || 'Erro ao carregar arquivo.'
  } finally {
    loading.value = false
  }
}

const confirmDeleteFile = async () => {
  deleteStatus.value = 'loading'
  try {
    await api.delete(`/v1/files/${fileData?.value?.fileId}`)
    deleteStatus.value = 'success'

    setTimeout(() => {
      router.replace({ name: 'list-files' })
      showDeleteModal.value = false
    }, 1000)
  } catch (e: any) {
    deleteStatus.value = 'error'
  }
}

const openLinkInNewTab = (link: string) => {
  window.open(link, '_blank')
}

const copyLink = async (signedUrl: string) => {
  if (signedUrl) {
    try {
      await navigator.clipboard.writeText(signedUrl)
      copied.value = true
      setTimeout(() => (copied.value = false), 2000)
    } catch {
      copied.value = false
    }
  }
}

function formatExpiration(dateStr: string) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

onMounted(fetchFile)
</script>

<template>
  <main class="container mx-auto w-full flex-col items-center justify-start px-4 py-8">
    <section class="mb-8 text-center">
      <h1 class="mb-4 text-3xl font-bold">Visualizar Arquivo</h1>
      <p class="mb-6 text-lg">Compartilhe o arquivo com os links abaixo abaixo.</p>
    </section>
    <section class="w-full rounded-lg p-6 text-center shadow">
      <div v-if="loading" class="py-8">Carregando arquivo...</div>
      <div v-else-if="error" class="text-error py-8">{{ error }}</div>
      <div v-else>
        <h2 class="mb-4 text-xl font-semibold">{{ fileData?.metadata?.fileName || 'Arquivo' }}</h2>

        <div class="mb-4 flex flex-col items-center gap-6">
          <div class="flex w-full max-w-xl flex-col gap-2 rounded-lg border p-4 shadow">
            <button class="bg-danger rounded px-3 py-1" @click="showDeleteModal = true">
              Deletar
            </button>
          </div>
        </div>

        <div v-if="fileData?.signedUrl" class="mb-4 flex flex-col items-center gap-6">
          <!-- Card: Link direto para download -->
          <div class="flex w-full max-w-xl flex-col gap-2 rounded-lg border p-4 shadow">
            <div class="mb-2 flex items-center gap-2">
              <svg
                class="h-5 w-5"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M12 16v-8m0 8l-4-4m4 4l4-4M4 20h16"
                />
              </svg>
              <span class="font-medium"> Link direto para download </span>
            </div>
            <p class="mb-2 text-sm">Use este link para baixar o arquivo.</p>
            <div class="flex items-center gap-2">
              <input
                type="text"
                :value="fileData.signedUrl"
                readonly
                class="flex-1 rounded border px-2 py-1 text-sm focus:outline-none"
                @focus="(e: any) => e.target.select()"
              />
              <button
                class="rounded px-3 py-1 text-sm transition"
                @click="copyLink(fileData.signedUrl)"
              >
                Copiar
              </button>
              <button
                class="rounded px-3 py-1 text-sm transition"
                @click="openLinkInNewTab(fileData.signedUrl)"
              >
                Ver em nova aba
              </button>
            </div>
          </div>

          <!-- Card: Link para assistir no navegador -->
          <template v-if="fileData?.metadata?.fileType.startsWith('video') && fileData?.watchUrl">
            <div class="flex w-full max-w-xl flex-col gap-2 rounded-lg border p-4 shadow">
              <div class="mb-2 flex items-center gap-2">
                <svg
                  class="h-5 w-5"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M14.752 11.168l-6.518-3.651A1 1 0 007 8.618v6.764a1 1 0 001.234.97l6.518-1.868a1 1 0 00.748-.97V12.138a1 1 0 00-.748-.97z"
                  />
                </svg>
                <span class="font-medium"> Link para assistir no navegador </span>
              </div>
              <p class="mb-2 text-sm">
                Use este link para assistir ao vídeo diretamente no navegador.
              </p>
              <div class="flex items-center gap-2">
                <input
                  type="text"
                  :value="fileData.watchUrl"
                  readonly
                  class="flex-1 rounded border px-2 py-1 text-sm focus:outline-none"
                  @focus="(e: any) => e.target.select()"
                />
                <button
                  class="rounded px-3 py-1 text-sm transition"
                  @click="copyLink(fileData.watchUrl)"
                >
                  Copiar
                </button>
                <button
                  class="rounded px-3 py-1 text-sm transition"
                  @click="openLinkInNewTab(fileData.watchUrl)"
                >
                  Ver em nova aba
                </button>
              </div>
            </div>
          </template>

          <span v-if="copied" class="mt-2 text-sm">Link copiado!</span>
          <div
            v-if="fileData?.expirationDate"
            class="mt-2 flex items-center gap-2 rounded border px-3 py-2 text-sm"
          >
            <svg
              class="h-4 w-4"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            <span>
              Os links estarão acessíveis até
              <span class="font-semibold">{{ formatExpiration(fileData.expirationDate) }}</span>
            </span>
          </div>
        </div>
        <div class="mb-6 flex justify-center">
          <template v-if="fileData?.metadata?.fileType.startsWith('video') && fileData?.signedUrl">
            <div
              class="flex max-w-2xl items-center gap-3 rounded-lg border border-amber-300 px-6 py-4 shadow-sm"
            >
              <svg
                class="h-6 w-6"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M13 16h-1v-4h-1m1-4h.01M12 20a8 8 0 100-16 8 8 0 000 16z"
                />
              </svg>
              <span class="text-base font-medium">
                A seção abaixo é um <span class="font-semibold">preview</span> para você.<br />
                Para compartilhar com outras pessoas, utilize um dos links acima.
              </span>
            </div>
          </template>
        </div>
        <FileVisualizer :file-type="fileData?.metadata?.fileType" :file-url="fileData?.signedUrl" />
      </div>
    </section>

    <!-- Modal de Deleção -->
    <ConfirmationModal
      :visible="showDeleteModal"
      title="Confirmar exclusão"
      :status="deleteStatus"
      cancelText="Cancelar"
      confirmText="Confirmar"
      loadingText="Deletando..."
      @cancel="showDeleteModal = false"
      @confirm="confirmDeleteFile"
    >
      <p>
        Deseja realmente <span class="font-semibold text-red-600">excluir</span> o arquivo
        <strong>{{ fileData?.metadata?.fileName }}</strong
        >?
      </p>
    </ConfirmationModal>
  </main>
</template>
