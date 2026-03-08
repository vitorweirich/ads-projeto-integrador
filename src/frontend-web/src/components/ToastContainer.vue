<template>
  <div class="fixed top-4 right-4 z-50 flex flex-col gap-2">
    <transition-group name="toast" tag="div">
      <div
        v-for="toast in toastStore.toasts"
        :key="toast.id"
        class="cursor-pointer rounded-lg px-4 py-2 text-white shadow"
        :class="{
          'bg-green-500': toast.type === 'success',
          'bg-red-500': toast.type === 'error',
          'bg-yellow-500': toast.type === 'warning',
          'bg-blue-500': toast.type === 'info',
        }"
        @mouseenter="toastStore.pause(toast.id)"
        @mouseleave="toastStore.resume(toast.id)"
        @click="toastStore.remove(toast.id)"
      >
        {{ toast.message }}
      </div>
    </transition-group>
  </div>
</template>

<script setup lang="ts">
import { useToastStore } from '@/stores/toast'
const toastStore = useToastStore()
</script>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}
.toast-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
