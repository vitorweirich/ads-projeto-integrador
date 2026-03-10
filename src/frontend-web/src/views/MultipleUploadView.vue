<script setup lang="ts">
import { ref } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const files = ref<File[]>([])
const videoPreviews = ref<string[]>([])
const videoNames = ref<string[]>([]) // nomes editáveis
const uploading = ref(false)
const uploadingProgress = ref<number[]>([])
const uploadError = ref('')
const router = useRouter()

const allowedTypes = [
  'video/mp4',
  'video/x-msvideo',
  'video/x-matroska',
  'video/quicktime',
  'video/x-ms-wmv',
  'video/mp2t',
]

const processFiles = (selectedFiles: FileList | File[]) => {
  const validFiles: File[] = []
  const previews: string[] = []
  const names: string[] = []

  // TODO: Validar storage disponivel + exibir toast informativo

  Array.from(selectedFiles).forEach((selected) => {
    if (!allowedTypes.includes(selected.type)) {
      uploadError.value = `Formato não suportado: ${selected.name}`
      return
    }
    if (selected.size > 2000 * 1024 * 1024) {
      uploadError.value = `O vídeo ${selected.name} é maior que 2 GB`
      return
    }
    validFiles.push(selected)
    previews.push(URL.createObjectURL(selected))
    names.push(selected.name.replace(/\.[^/.]+$/, '').slice(0, 95))
  })

  files.value = [...files.value, ...validFiles]
  videoPreviews.value = [...videoPreviews.value, ...previews]
  videoNames.value = [...videoNames.value, ...names]
  uploadingProgress.value = new Array(files.value.length).fill(0)
  uploadError.value = ''
}

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    processFiles(target.files)
  }
}

const handleDrop = (e: DragEvent) => {
  e.preventDefault()
  e.stopPropagation()
  if (e.dataTransfer?.files && e.dataTransfer.files.length > 0) {
    processFiles(e.dataTransfer.files)
    e.dataTransfer.clearData()
  }
}

const handleDragOver = (e: DragEvent) => {
  e.preventDefault()
  e.stopPropagation()
}

const removeVideo = (index: number) => {
  URL.revokeObjectURL(videoPreviews.value[index])
  files.value.splice(index, 1)
  videoPreviews.value.splice(index, 1)
  videoNames.value.splice(index, 1)
  uploadingProgress.value.splice(index, 1)
}

const handleUpload = async () => {
  if (files.value.length === 0) {
    uploadError.value = 'Selecione pelo menos um arquivo.'
    return
  }

  uploading.value = true
  uploadError.value = ''

  try {
    for (let i = 0; i < files.value.length; i++) {
      const file = files.value[i]

      const { data: signedUrlData } = await api.post('/v1/videos/upload', {
        fileName: videoNames.value[i].slice(0, 95),
        size: file.size,
        contentType: file.type,
        fileSize: file.size,
      })

      await new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest()
        xhr.open('PUT', signedUrlData.signedUrl)
        xhr.setRequestHeader('Content-Type', file.type)
        xhr.upload.onprogress = (event) => {
          if (event.lengthComputable) {
            uploadingProgress.value[i] = Math.round((event.loaded / event.total) * 100)
          }
        }
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve(null)
          } else {
            reject(new Error(`Erro ao enviar ${file.name}`))
          }
        }
        xhr.onerror = () => reject(new Error(`Erro ao enviar ${file.name}`))
        xhr.send(file)
      })

      await api.patch(`/v1/videos/upload/${signedUrlData.fileId}/register-uploaded`)
      URL.revokeObjectURL(videoPreviews.value[i])
    }

    await useAuthStore().fetchUser(true)
    router.push({ name: 'list-videos' })
  } catch (e: any) {
    uploadError.value = e?.response?.data?.message || e?.message || 'Erro ao enviar vídeos.'
  } finally {
    uploading.value = false
    uploadingProgress.value = []
  }
}
</script>

<template>
  <main class="mx-auto inline px-4 py-8">
    <h1 class="mb-4 text-center text-3xl font-bold">Enviar Novos Vídeos</h1>

    <form class="mx-auto max-w-2xl rounded-lg p-6 shadow" @submit.prevent="handleUpload">
      <!-- Drag and Drop -->
      <div
        class="mb-4 flex flex-col items-center justify-center gap-2 rounded border-2 border-dashed p-6 text-center transition-colors hover:border-blue-500"
        @dragover="handleDragOver"
        @drop="handleDrop"
      >
        <p>Arraste e solte os vídeos aqui ou</p>
        <label
          class="relative mx-auto flex cursor-pointer items-center gap-2 rounded border border-blue-400 px-4 py-2 font-semibold shadow"
        >
          <span>Selecionar vídeos</span>
          <input
            type="file"
            multiple
            :disabled="uploading"
            accept="video/mp4,video/x-msvideo,video/x-matroska,video/quicktime,video/x-ms-wmv,video/mp2t"
            @change="handleFileChange"
            class="absolute top-0 left-0 h-full w-full cursor-pointer opacity-0 disabled:cursor-not-allowed"
          />
        </label>
      </div>

      <!-- Lista de vídeos -->
      <div v-if="files.length > 0" class="space-y-4">
        <div
          v-for="(file, index) in files"
          :key="file.name + index"
          class="rounded border p-3 shadow-sm"
        >
          <!-- Nome editável + remover -->
          <div class="flex items-center justify-between gap-2">
            <input
              v-model="videoNames[index]"
              maxlength="95"
              :readonly="uploading"
              class="w-full rounded border px-2 py-1 text-sm"
            />
            <button
              type="button"
              class="rounded bg-red-500 px-2 py-1 text-xs text-white hover:bg-red-600 disabled:opacity-50"
              @click="removeVideo(index)"
              :disabled="uploading"
            >
              Remover
            </button>
          </div>

          <!-- Preview -->
          <video
            v-if="videoPreviews[index]"
            :src="videoPreviews[index]"
            controls
            class="mt-2 max-h-48 rounded border"
          />

          <!-- Barra de progresso -->
          <div v-if="uploading" class="mt-2">
            <div class="h-2 w-full overflow-hidden rounded bg-gray-200 dark:bg-gray-700">
              <div
                class="h-2 bg-blue-500 transition-all duration-200"
                :style="{ width: uploadingProgress[index] + '%' }"
              ></div>
            </div>
            <p class="mt-1 text-center text-xs">{{ uploadingProgress[index] }}%</p>
          </div>
        </div>
      </div>

      <div v-if="uploadError" class="mt-4 text-red-500">{{ uploadError }}</div>

      <button
        type="submit"
        :disabled="uploading"
        class="mt-6 w-full rounded bg-blue-600 py-2 font-semibold text-white disabled:opacity-50"
      >
        {{ uploading ? 'Enviando vídeos...' : 'Enviar Vídeos' }}
      </button>
    </form>
  </main>
</template>
