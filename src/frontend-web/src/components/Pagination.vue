<script setup lang="ts">
import { defineEmits } from 'vue'

const props = defineProps({
  currentPage: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  disabled: { type: Boolean, default: false },
})

const emit = defineEmits(['page-change'])

const goPrev = () => {
  if (props.currentPage > 0 && !props.disabled) {
    emit('page-change', props.currentPage - 1)
  }
}

const goNext = () => {
  if (props.currentPage < props.totalPages - 1 && !props.disabled) {
    emit('page-change', props.currentPage + 1)
  }
}
</script>

<template>
  <div class="flex items-center justify-between">
    <button
      class="rounded px-3 py-1"
      :disabled="props.currentPage === 0 || props.disabled"
      @click="goPrev"
    >
      Anterior
    </button>
    <div>Página {{ props.currentPage + 1 }} de {{ props.totalPages }}</div>
    <button
      class="rounded px-3 py-1"
      :disabled="props.currentPage >= props.totalPages - 1 || props.disabled"
      @click="goNext"
    >
      Próxima
    </button>
  </div>
</template>
