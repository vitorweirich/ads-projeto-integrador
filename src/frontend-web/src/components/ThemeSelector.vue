<script lang="ts" setup>
import { useTheme } from '@/composables/useTheme'
import { onMounted } from 'vue'

const { isDark, setDark } = useTheme()

onMounted(() => {
  setDark(isDark.value)
})
</script>

<template>
  <button
    @click="setDark(!isDark)"
    class="relative inline-flex h-8 w-16 items-center rounded-full transition-colors duration-300 focus:outline-none"
    :class="isDark ? 'bg-gray-700' : 'bg-yellow-400'"
  >
    <span
      class="absolute top-1 left-1 h-6 w-6 rounded-full bg-white transition-transform duration-300"
      :class="isDark ? 'translate-x-8' : 'translate-x-0'"
    ></span>
    <span class="absolute right-0 left-0 flex justify-between px-2 text-xs font-medium">
      <span v-if="!isDark">☀️</span>
      <span v-if="isDark" class="ml-auto">🌙</span>
    </span>
  </button>
</template>
