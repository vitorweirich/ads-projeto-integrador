import { ref } from 'vue'

const isDark = ref(
  localStorage.getItem('theme') === 'dark' || localStorage.getItem('theme') === null,
)

function setDark(val: boolean) {
  isDark.value = val
  localStorage.setItem('theme', val ? 'dark' : 'light')
  document.documentElement.classList.toggle('dark', val)
}

// Aplicar no início para manter o estado sincronizado com o DOM
setDark(isDark.value)

export function useTheme() {
  return { isDark, setDark }
}
