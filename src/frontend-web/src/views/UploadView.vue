<script setup lang="ts">
import { ref } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'

const file = ref<File | null>(null)
const videoPreview = ref<string | null>(null)
const title = ref<string>('')
const uploading = ref(false)
const uploadingProgress = ref(0)
const uploadError = ref('')
const router = useRouter()

const authStore = useAuthStore()

const allowedTypes = [
  'video/mp4',
  'video/x-msvideo',
  'video/x-matroska',
  'video/quicktime',
  'video/x-ms-wmv',
  'video/mp2t',
]

const processFile = async (selected: File) => {
  if (!allowedTypes.includes(selected.type)) {
    uploadError.value = 'Formato de vídeo não suportado. Use mp4, avi, mkv, mov, wmv ou ts.'
    file.value = null
    videoPreview.value = null
    return
  }

  if (selected.size > 2000 * 1024 * 1024) {
    uploadError.value = 'O vídeo não pode ser maior que 2 GB.'
    file.value = null
    videoPreview.value = null
    return
  }

  const loggedInUser = await authStore.requireLoggedInUser()

  const userStorage = loggedInUser.storage

  if (userStorage?.totalQuota - userStorage?.usedQuota < selected.size) {
    const message = 'Cota de armazenamento insuficiente. Exclua alguns vídeos para liberar espaço'
    uploadError.value = message
    file.value = null
    videoPreview.value = null
    useToastStore().show(message, 'warning')
    return
  }

  if (!title.value) {
    title.value = selected.name.replace(/\.[^/.]+$/, '').slice(0, 95)
  }

  file.value = selected
  uploadError.value = ''
  if (videoPreview.value) {
    URL.revokeObjectURL(videoPreview.value)
  }
  videoPreview.value = URL.createObjectURL(selected)
}

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    processFile(target.files[0])
  }
}

const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  e.stopPropagation()
  if (e.dataTransfer?.files && e.dataTransfer.files.length > 0) {
    processFile(e.dataTransfer.files[0])
    e.dataTransfer.clearData()
  }
}

const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  e.stopPropagation()
}

const handleUpload = async () => {
  if (!file.value) {
    uploadError.value = 'Selecione um arquivo.'
    return
  }
  uploading.value = true
  uploadingProgress.value = 0
  uploadError.value = ''
  try {
    const { data: signedUrlData } = await api.post('/v1/videos/upload', {
      fileName: title.value.slice(0, 95),
      size: file.value.size,
      contentType: file.value.type,
      fileSize: file.value.size,
    })
    await new Promise((resolve, reject) => {
      if (!file.value) {
        reject(new Error('Arquivo não selecionado.'))
        return
      }
      const xhr = new XMLHttpRequest()
      xhr.open('PUT', signedUrlData.signedUrl)
      xhr.setRequestHeader('Content-Type', file.value.type)
      xhr.upload.onprogress = (event) => {
        if (event.lengthComputable) {
          uploadingProgress.value = Math.round((event.loaded / event.total) * 95)
        }
      }
      xhr.onload = () => {
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve(null)
        } else {
          reject(new Error('Erro ao enviar vídeo.'))
        }
      }
      xhr.onerror = () => reject(new Error('Erro ao enviar vídeo.'))
      xhr.send(file.value)
    })
    await api.patch(`/v1/videos/upload/${signedUrlData.videoId}/register-uploaded`)
    if (videoPreview.value) {
      URL.revokeObjectURL(videoPreview.value)
      videoPreview.value = null
    }

    router.push({ name: 'list-videos' })
  } catch (e: any) {
    uploadError.value = e?.response?.data?.message || e?.message || 'Erro ao enviar vídeo.'
  } finally {
    uploading.value = false
    uploadingProgress.value = 0
  }
}
</script>

<template>
  <main class="mx-auto inline px-4 py-8">
    <section class="mb-8 text-center">
      <h1 class="mb-4 text-3xl font-bold">Enviar Novo Vídeo</h1>
      <p class="mb-6 text-lg">Preencha o título e selecione o vídeo para upload temporário.</p>
    </section>

    <form class="mx-auto max-w-lg rounded-lg p-6 shadow" @submit.prevent="handleUpload">
      <div class="mb-4">
        <label class="mb-1 block font-semibold">Título</label>
        <input
          :readonly="uploading"
          v-model="title"
          @input="title = title.slice(0, 95)"
          class="w-full rounded border px-3 py-2"
          required
        />
      </div>

      <div class="mb-4">
        <label class="mb-1 block font-semibold">Arquivo de Vídeo</label>

        <!-- Área de drag-and-drop -->
        <div
          class="flex flex-col items-center justify-center gap-2 rounded border-2 border-dashed p-6 text-center transition-colors hover:border-blue-500"
          @dragover="handleDragOver"
          @drop="handleDrop"
        >
          <p class="mb-2">Arraste e solte o vídeo aqui, ou</p>

          <label
            class="relative mx-auto flex cursor-pointer items-center gap-2 rounded border border-blue-400 px-4 py-2 font-semibold shadow"
          >
            <span>Selecionar vídeo</span>
            <input
              type="file"
              :disabled="uploading"
              accept="video/mp4,video/x-msvideo,video/x-matroska,video/quicktime,video/x-ms-wmv,video/mp2t"
              @change="handleFileChange"
              class="absolute top-0 left-0 h-full w-full cursor-pointer opacity-0 disabled:cursor-not-allowed"
            />
          </label>

          <span v-if="file" class="w-full truncate text-sm" :title="file?.name">{{
            file?.name
          }}</span>
        </div>

        <div v-if="videoPreview" class="mt-4 flex justify-center">
          <video :src="videoPreview" controls class="max-h-64 rounded border shadow" />
        </div>
      </div>

      <div v-if="uploading" class="mb-4">
        <div class="h-4 w-full overflow-hidden rounded bg-gray-200 dark:bg-gray-700">
          <div
            class="h-4 bg-blue-500 transition-all duration-200"
            :style="{ width: uploadingProgress + '%' }"
          ></div>
        </div>
        <div class="mt-1 text-center text-sm">{{ uploadingProgress }}%</div>
      </div>

      <div v-if="uploadError" class="mb-4 text-red-500">{{ uploadError }}</div>

      <button
        type="submit"
        :disabled="uploading"
        class="w-full rounded bg-blue-600 py-2 font-semibold text-white disabled:opacity-50 hover:disabled:cursor-not-allowed"
      >
        {{ uploading ? 'Enviando...' : 'Enviar Vídeo' }}
      </button>
    </form>
  </main>
</template>
