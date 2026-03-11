<template>
  <div v-if="file" class="mt-2">
    <template v-if="filePreviewSrc && file">
      <template v-if="file.type.startsWith('video')">
        <video :src="filePreviewSrc" controls class="max-h-64 rounded border shadow" />
      </template>
      <template v-else-if="file.type === 'image/jpg' || file.type === 'image/jpeg'">
        <img
          :src="filePreviewSrc"
          alt="Pré-visualização da imagem"
          class="max-h-64 rounded border shadow"
        />
      </template>
      <template v-else-if="file.type === 'application/pdf'">
        <iframe
          :src="filePreviewSrc"
          class="mx-auto mb-4 max-w-full rounded shadow"
          style="width: 100%; height: 500px; border: none"
          title="Pré-visualização do PDF"
        />
      </template>
    </template>
    <template v-else>
      <span class="text-sm">Pré-visualização não disponível para este tipo de arquivo.</span>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onUnmounted } from 'vue'

const { file } = defineProps<{
  file: File | null | undefined
}>()

const filePreviewSrc = computed(() => {
  if (file) {
    return URL.createObjectURL(file)
  }
  return null
})

console.log({ file, filePreviewSrc: filePreviewSrc.value, bool: file !== null })

onUnmounted(() => {
  if (filePreviewSrc.value) {
    URL.revokeObjectURL(filePreviewSrc.value)
  }
})
</script>
