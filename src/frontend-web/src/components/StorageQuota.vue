<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { calculateUsedQuotaPercentage, formatBytes } from '@/utils/filesUtils'

const auth = useAuthStore()

const quotaPercentage = computed(() =>
  calculateUsedQuotaPercentage(
    auth.user?.storage.totalQuota || 0,
    auth.user?.storage.usedQuota || 0,
  ),
)

const usedFormatted = computed(() => formatBytes(auth.user?.storage.usedQuota || 0))
const totalFormatted = computed(() => formatBytes(auth.user?.storage.totalQuota || 0))
</script>

<template>
  <div
    v-if="auth.user?.storage"
    class="flex items-center gap-2"
    :title="`${quotaPercentage.toFixed(1)}% usado`"
  >
    <!-- Texto -->
    <span class="text-sm text-gray-700 dark:text-gray-300">
      {{ usedFormatted }} / {{ totalFormatted }}
    </span>

    <!-- Barra de progresso -->
    <div class="h-2 w-24 overflow-hidden rounded-full bg-gray-300 dark:bg-gray-700">
      <div
        class="h-full bg-blue-600 transition-all"
        :style="{ width: quotaPercentage + '%' }"
      ></div>
    </div>
  </div>
</template>
