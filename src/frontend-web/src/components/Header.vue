<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { storeToRefs } from 'pinia'
import { ref, onMounted, onUnmounted } from 'vue'
import ThemeSelector from './ThemeSelector.vue'
import StorageQuota from './StorageQuota.vue'

const auth = useAuthStore()
const { isAuthenticated, isAdmin, user } = storeToRefs(auth)

const showDropdown = ref(false)
const mobileMenuOpen = ref(false)

const closeDropdown = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  if (!target.closest('.dropdown-container')) {
    showDropdown.value = false
  }
  if (!target.closest('.mobile-menu-container')) {
    mobileMenuOpen.value = false
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

const mobileLogout = () => {
  mobileMenuOpen.value = false
  auth.logout()
}
</script>

<template>
  <header class="bg-white shadow dark:bg-zinc-900">
    <div class="container mx-auto flex items-center justify-between px-4 py-4 xl:py-6">
      <router-link to="/" class="cursor-pointer text-2xl font-bold" data-cy="home-link">
        File Share
      </router-link>

      <!-- ===== Mobile only (<lg): hamburger + theme ===== -->
      <div class="mobile-menu-container flex items-center gap-2 md:hidden">
        <ThemeSelector data-cy="theme-selector" />
        <button
          @click.stop="mobileMenuOpen = !mobileMenuOpen"
          class="!bg-transparent p-1"
          aria-label="Abrir menu"
        >
          <svg
            class="h-6 w-6"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            viewBox="0 0 24 24"
          >
            <path
              v-if="!mobileMenuOpen"
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M4 6h16M4 12h16M4 18h16"
            />
            <path v-else stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- ===== Intermediate (lg to xl): core links + hamburger for extras ===== -->
      <div class="hidden items-center gap-4 md:flex xl:hidden">
        <nav>
          <ul class="flex items-center space-x-5">
            <li><ThemeSelector /></li>
            <template v-if="isAuthenticated">
              <li>
                <router-link to="/list-files" data-cy="my-files-link">Meus Arquivos</router-link>
              </li>
              <li>
                <router-link to="/upload" data-cy="upload-file-link">Enviar Arquivo</router-link>
              </li>
            </template>
            <template v-else>
              <li><router-link to="/login" data-cy="login-link">Entrar</router-link></li>
              <li><router-link to="/register" data-cy="register-link">Cadastrar</router-link></li>
            </template>
            <!-- Profile dropdown (intermediate) -->
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
                  >Perfil</router-link
                >
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
        <!-- Hamburger for extras (admin, storage) -->
        <div
          v-if="isAuthenticated && (isAdmin || user?.storage)"
          class="mobile-menu-container relative"
        >
          <button
            @click.stop="mobileMenuOpen = !mobileMenuOpen"
            class="!bg-transparent p-1"
            aria-label="Mais opções"
          >
            <svg
              class="h-5 w-5"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              viewBox="0 0 24 24"
            >
              <path
                v-if="!mobileMenuOpen"
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M4 6h16M4 12h16M4 18h16"
              />
              <path
                v-else
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </div>
      </div>

      <!-- ===== Full desktop (xl+): all links ===== -->
      <nav class="hidden xl:block">
        <ul class="flex items-center space-x-6">
          <li><ThemeSelector data-cy="theme-selector" /></li>
          <template v-if="isAdmin">
            <li>
              <router-link to="/admin/files" data-cy="admin-files-link"
                >Administrar Arquivos</router-link
              >
            </li>
            <li>
              <router-link to="/admin/users" data-cy="admin-users-link"
                >Administrar Usuários</router-link
              >
            </li>
          </template>
          <template v-if="isAuthenticated">
            <li><StorageQuota /></li>
            <li>
              <router-link to="/list-files" data-cy="my-files-link">Meus Arquivos</router-link>
            </li>
            <li>
              <router-link to="/upload" data-cy="upload-file-link">Enviar Arquivo</router-link>
            </li>
          </template>
          <template v-else>
            <li><router-link to="/login" data-cy="login-link">Entrar</router-link></li>
            <li><router-link to="/register" data-cy="register-link">Cadastrar</router-link></li>
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
                >Perfil</router-link
              >
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

    <!-- ===== Dropdown menu (hamburger content) ===== -->
    <!-- Mobile (<lg): all links -->
    <nav
      v-if="mobileMenuOpen"
      class="mobile-menu-container border-t border-gray-200 px-4 pb-4 dark:border-gray-700"
    >
      <!-- Full menu for <lg -->
      <ul class="flex flex-col items-end gap-3 pt-3 md:hidden">
        <template v-if="isAdmin">
          <li>
            <router-link to="/admin/files" @click="mobileMenuOpen = false"
              >Administrar Arquivos</router-link
            >
          </li>
          <li>
            <router-link to="/admin/users" @click="mobileMenuOpen = false"
              >Administrar Usuários</router-link
            >
          </li>
        </template>
        <template v-if="isAuthenticated">
          <li><StorageQuota /></li>
          <li>
            <router-link to="/list-files" @click="mobileMenuOpen = false"
              >Meus Arquivos</router-link
            >
          </li>
          <li>
            <router-link to="/upload" @click="mobileMenuOpen = false">Enviar Arquivo</router-link>
          </li>
          <li>
            <router-link to="/profile" @click="mobileMenuOpen = false">Perfil</router-link>
          </li>
          <li>
            <button class="text-sm text-red-600 dark:text-red-400" @click="mobileLogout">
              Sair
            </button>
          </li>
        </template>
        <template v-else>
          <li><router-link to="/login" @click="mobileMenuOpen = false">Entrar</router-link></li>
          <li>
            <router-link to="/register" @click="mobileMenuOpen = false">Cadastrar</router-link>
          </li>
        </template>
      </ul>

      <!-- Extras only for lg-xl (admin + storage) -->
      <ul class="hidden flex-col items-end gap-3 pt-3 md:flex xl:hidden">
        <template v-if="isAdmin">
          <li>
            <router-link to="/admin/files" @click="mobileMenuOpen = false"
              >Administrar Arquivos</router-link
            >
          </li>
          <li>
            <router-link to="/admin/users" @click="mobileMenuOpen = false"
              >Administrar Usuários</router-link
            >
          </li>
        </template>
        <li v-if="isAuthenticated">
          <StorageQuota />
        </li>
      </ul>
    </nav>
  </header>
</template>
