<script setup lang="ts">
import { ref } from 'vue'
import api from '@/services/api'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { allowedMimeTypes } from '@/constants/fileTypes'
import UploadingFilePreview from '@/components/UploadingFilePreview.vue'

const file = ref<File | null>(null)
const filePreview = ref<string | null>(null)
const title = ref<string>('')
const uploading = ref(false)
const uploadingProgress = ref(0)
const uploadError = ref('')
const router = useRouter()

const authStore = useAuthStore()

// Tipos de arquivos permitidos e preview definidos em src/constants/fileTypes.ts

const processFile = async (selected: File) => {
  if (!allowedMimeTypes.includes(selected.type)) {
    uploadError.value =
      'Formato de arquivo não suportado. Use mp4, avi, mkv, mov, wmv, ts, jpg ou jpeg.'
    file.value = null
    filePreview.value = null
    return
  }

  if (selected.size > 2000 * 1024 * 1024) {
    uploadError.value = 'O arquivo não pode ser maior que 2 GB.'
    file.value = null
    filePreview.value = null
    return
  }

  const loggedInUser = await authStore.requireLoggedInUser()

  const userStorage = loggedInUser.storage

  if (userStorage?.totalQuota - userStorage?.usedQuota < selected.size) {
    const message = 'Cota de armazenamento insuficiente. Exclua alguns arquivos para liberar espaço'
    uploadError.value = message
    file.value = null
    filePreview.value = null
    useToastStore().show(message, 'warning')
    return
  }

  if (!title.value) {
    title.value = selected.name
      .replace(/\.[^/.]+$/g, '')
      .replace(/\./g, '_')
      .slice(0, 70)
  }

  file.value = selected
  uploadError.value = ''
  if (filePreview.value) {
    URL.revokeObjectURL(filePreview.value)
  }
  filePreview.value = URL.createObjectURL(selected)
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
    const { data: signedUrlData } = await api.post('/v1/files/upload', {
      fileName: title.value.slice(0, 70),
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
          reject(new Error('Erro ao enviar arquivo.'))
        }
      }
      xhr.onerror = () => reject(new Error('Erro ao enviar arquivo.'))
      xhr.send(file.value)
    })
    await api.patch(`/v1/files/upload/${signedUrlData.fileId}/register-uploaded`)
    if (filePreview.value) {
      URL.revokeObjectURL(filePreview.value)
      filePreview.value = null
    }

    router.push({ name: 'list-files' })
  } catch (e: any) {
    uploadError.value = e?.response?.data?.message || e?.message || 'Erro ao enviar arquivo.'
  } finally {
    uploading.value = false
    uploadingProgress.value = 0
  }
}
</script>

<template>
  <main class="mx-auto inline px-4 py-8">
    <section class="mb-8 text-center">
      <h1 class="mb-4 text-3xl font-bold">Enviar Novo Arquivo</h1>
      <p class="mb-6 text-lg">Preencha o título e selecione o arquivo para upload temporário.</p>
    </section>

    <form class="mx-auto max-w-lg rounded-lg p-6 shadow" @submit.prevent="handleUpload">
      <div class="mb-4">
        <label class="mb-1 block font-semibold">Título</label>
        <input
          :readonly="uploading"
          v-model="title"
          @input="title = title.slice(0, 70)"
          class="w-full rounded border px-3 py-2"
          required
        />
      </div>

      <div class="mb-4">
        <label class="mb-1 block font-semibold">Arquivo</label>

        <!-- Área de drag-and-drop -->
        <div
          class="flex flex-col items-center justify-center gap-2 rounded border-2 border-dashed p-6 text-center transition-colors hover:border-blue-500"
          @dragover="handleDragOver"
          @drop="handleDrop"
        >
          <p class="mb-2">Arraste e solte o arquivo aqui, ou</p>

          <label
            class="relative mx-auto flex cursor-pointer items-center gap-2 rounded border border-blue-400 px-4 py-2 font-semibold shadow"
          >
            <span>Selecionar arquivo</span>
            <input
              type="file"
              :disabled="uploading"
              :accept="allowedMimeTypes.join(',')"
              @change="handleFileChange"
              class="absolute top-0 left-0 h-full w-full cursor-pointer opacity-0 disabled:cursor-not-allowed"
            />
          </label>

          <span v-if="file" class="w-full truncate text-sm" :title="file?.name">{{
            file?.name
          }}</span>
        </div>

        <UploadingFilePreview :file="file" />
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
        {{ uploading ? 'Enviando...' : 'Enviar Arquivo' }}
      </button>
    </form>

    <div class="flex gap-2 text-center text-sm">
      Precisa enviar multiplos arquivos?<router-link
        to="/upload-multiple"
        class=""
        data-cy="link-forgot-password"
        >Enviar múltiplos arquivos</router-link
      >
    </div>
  </main>
</template>
