<!-- TODO: Padronizar estilos e atualizar lugares que usam -->
<template>
  <div
    v-if="visible"
    class="bg-opacity-50 fixed inset-0 z-50 flex items-center justify-center bg-black"
  >
    <div class="relative w-full max-w-md rounded-lg p-6 shadow-lg">
      <h2 v-if="title" class="mb-4 text-xl font-bold">{{ title }}</h2>

      <div class="mb-4">
        <slot />
      </div>

      <div v-if="status === 'success'" class="mb-4 text-green-600">
        <slot name="success">Operação concluída com sucesso.</slot>
      </div>
      <div v-if="status === 'error'" class="mb-4 text-red-600">
        <slot name="error">Ocorreu um erro.</slot>
      </div>

      <div class="flex justify-end gap-2">
        <button class="rounded px-3 py-1" @click="$emit('cancel')" :disabled="status === 'loading'">
          {{ cancelText }}
        </button>
        <button
          class="bg-danger rounded px-3 py-1 text-white"
          @click="$emit('confirm')"
          :disabled="status === 'loading'"
        >
          {{ status === 'loading' ? loadingText : confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ModalStatus } from '@/types/modal'

defineProps<{
  visible: boolean
  title?: string
  status?: ModalStatus
  cancelText?: string
  confirmText?: string
  loadingText?: string
}>()

defineEmits<{
  (e: 'cancel'): void
  (e: 'confirm'): void
}>()
</script>
