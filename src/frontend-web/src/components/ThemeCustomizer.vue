<script setup lang="ts">
import { useTheme } from '@/composables/useTheme'
import { ref, onMounted, watch } from 'vue'

const { isDark, setDark } = useTheme()

const visible = ref(false)
const activeTheme = ref<'light' | 'dark'>(isDark.value ? 'dark' : 'light')

watch(isDark, (isDark) => {
  activeTheme.value = isDark ? 'dark' : 'light'
})

// Temas padrão
const defaultThemes = {
  light: {
    '--color-primary': '#10b981',
    '--color-secondary': '#0c4a6e',
    '--color-danger': '#dc2626',
    '--color-bg': '#ffffff',
    '--color-text': '#1f2937',
  },
  dark: {
    '--color-primary': '#1d7856',
    '--color-secondary': '#0c4a6e',
    '--color-danger': '#dc2626',
    '--color-bg': '#1f2021',
    '--color-text': '#f3f4f6',
  },
}

const themes = ref<Record<'light' | 'dark', Record<string, string>>>({
  light: { ...defaultThemes.light },
  dark: { ...defaultThemes.dark },
})

watch(
  [themes, activeTheme],
  ([newTheme, newActive]) => {
    Object.entries(newTheme[newActive]).forEach(([key, val]) => {
      document.documentElement?.style.setProperty(key, val)
    })
  },
  { deep: true },
)

const persistThemes = () => {
  localStorage.setItem('theme-custom', JSON.stringify(themes.value))
}

const resetTheme = (mode: 'light' | 'dark') => {
  themes.value[mode] = { ...defaultThemes[mode] }
}

const handleChangeTab = (mode: string) => {
  activeTheme.value = mode as 'light' | 'dark'

  setDark(mode === 'dark')
}

onMounted(() => {
  const saved = localStorage.getItem('theme-custom')
  if (saved) {
    const parsed = JSON.parse(saved)
    themes.value = { ...themes.value, ...parsed }

    // Aplicar temas salvos
    for (const mode of ['light', 'dark'] as const) {
      if (document.documentElement) continue

      Object.entries(themes.value[mode]).forEach(([key, val]) => {
        document.documentElement.style.setProperty(key, val)
      })
    }
  }
})
</script>

<template>
  <!-- Botão flutuante -->
  <button
    @click="visible = true"
    class="fixed right-4 bottom-4 z-50 flex h-12 w-12 items-center justify-center rounded-full bg-[var(--color-primary)] text-white shadow-lg transition hover:scale-105"
    title="Personalizar tema"
  >
    🎨
  </button>

  <!-- Modal -->
  <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center">
    <div
      class="w-full max-w-xl rounded-2xl bg-white p-6 shadow-2xl transition-all duration-300 dark:bg-gray-900"
    >
      <!-- Título -->
      <div class="mb-6 flex items-center justify-between">
        <h2 class="text-2xl font-semibold text-gray-800 dark:text-white">🎨 Personalizar Tema</h2>
        <button
          @click="visible = false"
          class="h-8 w-8 rounded-full text-gray-500 hover:text-gray-800 dark:text-gray-400 dark:hover:text-white"
        >
          ✕
        </button>
      </div>

      <!-- Tabs -->
      <div class="mb-6 flex gap-2 rounded-lg bg-gray-100 p-1 dark:bg-gray-800">
        <button
          v-for="mode in ['light', 'dark']"
          :key="mode"
          @click="handleChangeTab(mode)"
          :class="[
            'flex-1 rounded py-2 text-sm font-medium shadow-sm transition',
            activeTheme === mode
              ? 'bg-[var(--color-primary)] text-white'
              : 'text-gray-600 hover:bg-gray-200 dark:text-gray-300 dark:hover:bg-gray-700',
          ]"
        >
          {{ mode === 'light' ? 'Claro' : 'Escuro' }}
        </button>
      </div>

      <!-- Formulário de edição -->
      <div class="grid grid-cols-2 gap-4 sm:grid-cols-3">
        <div
          v-for="(_value, key) in themes[activeTheme]"
          :key="key"
          class="flex flex-col items-start gap-1"
        >
          <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
            {{ key.replace('--color-', '') }}
          </label>
          <input
            type="color"
            v-model="themes[activeTheme][key]"
            class="h-10 w-16 cursor-pointer rounded border border-gray-300 !bg-transparent p-0 dark:border-gray-600"
          />
        </div>
      </div>

      <!-- Ações -->
      <div class="mt-8 flex flex-wrap justify-end gap-3">
        <button
          @click="resetTheme(activeTheme)"
          class="btn-secondary flex items-center gap-2 rounded-md border border-gray-300 px-4 py-2 text-sm text-gray-700 transition hover:bg-gray-100 dark:border-gray-600 dark:text-gray-200 dark:hover:bg-gray-700"
        >
          Voltar
          <span class="text-md text-lg font-medium text-orange-700">{{ activeTheme }}</span> para os
          valores padrão
        </button>
        <button
          @click="persistThemes()"
          class="rounded-md bg-[var(--color-primary)] px-4 py-2 text-sm font-medium text-white shadow hover:opacity-90"
        >
          Salvar configuração de temas
        </button>
      </div>
    </div>
  </div>
</template>
