<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { ref, onMounted, onUnmounted } from 'vue'
import ThemeSelector from './ThemeSelector.vue'
import StorageQuota from './StorageQuota.vue'

const auth = useAuthStore()
const { isAuthenticated, isAdmin, user } = storeToRefs(auth)

const showDropdown = ref(false)

const closeDropdown = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.dropdown-container')) {
    showDropdown.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})

const logout = () => {
  auth.logout()
  showDropdown.value = false
}
</script>

<template>
  <!-- TODO: Criar CSS var para a cor do -->
  <header class="bg-white shadow dark:bg-zinc-900">
    <div class="container mx-auto flex items-center justify-between px-4 py-6">
      <router-link to="/" class="cursor-pointer text-2xl font-bold" data-cy="home-link">
        File Share
      </router-link>

      <nav>
        <ul class="flex items-center space-x-6">
          <li>
            <ThemeSelector data-cy="theme-selector" />
          </li>

          <template v-if="isAdmin">
            <li>
              <router-link to="/admin/videos" data-cy="admin-videos-link">
                Administrar Arquivos
              </router-link>
            </li>
            <li>
              <router-link to="/admin/users" data-cy="admin-users-link">
                Administrar Usuários
              </router-link>
            </li>
          </template>

          <template v-if="isAuthenticated">
            <li>
              <StorageQuota />
            </li>
            <li>
              <router-link to="/list-videos" data-cy="my-videos-link"> Meus Arquivos </router-link>
            </li>
            <li>
              <router-link to="/upload" data-cy="upload-video-link">Enviar Arquivo </router-link>
            </li>
          </template>

          <template v-else>
            <li>
              <router-link to="/login" data-cy="login-link"> Entrar </router-link>
            </li>
            <li>
              <router-link to="/register" data-cy="register-link"> Cadastrar </router-link>
            </li>
          </template>

          <li v-if="isAuthenticated" class="dropdown-container relative">
            <button
              @click.stop="showDropdown = !showDropdown"
              class="flex items-center !bg-transparent !text-[var(--color-primary)] hover:!text-[var(--color-primary-hover)]"
              data-cy="profile-button"
            >
              Olá, {{ user?.name || 'Perfil' }}!
              <svg
                class="ml-1 h-4 w-4"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            <div
              v-if="showDropdown"
              class="absolute right-0 z-10 mt-2 w-40 origin-top-right overflow-hidden rounded-xl border border-gray-200 bg-white shadow-lg dark:border-gray-700 dark:bg-gray-800"
              data-cy="profile-dropdown"
            >
              <router-link
                to="/profile"
                class="block w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 dark:text-gray-200 dark:hover:bg-gray-700"
                @click="showDropdown = false"
                data-cy="profile-link"
              >
                Perfil
              </router-link>

              <button
                class="block w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-800"
                @click="logout"
                data-cy="logout-button"
              >
                Sair
              </button>
            </div>
          </li>
        </ul>
      </nav>
    </div>
  </header>
</template>
