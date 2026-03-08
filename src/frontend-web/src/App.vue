<script setup lang="ts">
import { RouterView } from 'vue-router'
import AppHeader from './components/Header.vue'

import { useAuthStore } from '@/stores/auth'
import { onMounted } from 'vue'
import GlobalLoading from './components/GlobalLoading.vue'
import ThemeCustomizer from './components/ThemeCustomizer.vue'
import ToastContainer from './components/ToastContainer.vue'

const auth = useAuthStore()

onMounted(() => {
  auth.fetchUser()
})
</script>

<template>
  <div class="flex min-h-screen flex-col">
    <AppHeader />
    <GlobalLoading />
    <RouterView class="flex flex-1" :key="$route.fullPath" />
    <ToastContainer />
    <ThemeCustomizer />
  </div>
</template>
