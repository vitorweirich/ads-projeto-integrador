<template>
  <div v-if="fileUrl && fileType" class="mb-6 flex justify-center">
    <template v-if="fileUrl && fileType.startsWith('video')">
      <video :src="fileUrl" controls class="mx-auto mb-4 max-h-[70vh] max-w-full rounded shadow" />
    </template>
    <template v-else-if="fileUrl && fileType.startsWith('image')">
      <img
        :src="fileUrl"
        alt="Pré-visualização da imagem"
        class="mx-auto mb-4 max-h-[70vh] max-w-full rounded shadow"
      />
    </template>
    <template v-else-if="fileUrl && fileType === 'application/pdf'">
      <iframe
        :src="fileUrl"
        class="mx-auto mb-4 max-w-full rounded shadow"
        style="width: 100%; height: 500px; border: none"
        title="Pré-visualização do PDF"
      />
    </template>
    <template v-else>
      <span class="text-sm">Visualização não disponível para este tipo de arquivo.</span>
    </template>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  fileUrl: string | null | undefined
  fileType: string | null | undefined
}>()
</script>
