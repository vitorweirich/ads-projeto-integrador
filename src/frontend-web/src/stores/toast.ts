import { defineStore } from 'pinia'

export type ToastType = 'success' | 'error' | 'info' | 'warning'

interface Toast {
  id: number
  message: string
  type: ToastType
  duration: number
  timeoutId?: ReturnType<typeof setTimeout>
}

export const useToastStore = defineStore('toast', {
  state: () => ({
    toasts: [] as Toast[],
    counter: 0,
  }),
  actions: {
    show(message: string, type: ToastType = 'info', duration = 3000) {
      const id = this.counter++
      const toast: Toast = { id, message, type, duration }

      // inicia o timeout
      toast.timeoutId = setTimeout(() => {
        this.remove(id)
      }, duration)

      this.toasts.push(toast)
    },
    remove(id: number) {
      const toast = this.toasts.find((t) => t.id === id)
      if (toast?.timeoutId) clearTimeout(toast.timeoutId)
      this.toasts = this.toasts.filter((t) => t.id !== id)
    },
    pause(id: number) {
      const toast = this.toasts.find((t) => t.id === id)
      if (toast?.timeoutId) {
        clearTimeout(toast.timeoutId)
        toast.timeoutId = undefined
      }
    },
    resume(id: number) {
      const toast = this.toasts.find((t) => t.id === id)
      if (toast && !toast.timeoutId) {
        toast.timeoutId = setTimeout(() => {
          this.remove(id)
        }, toast.duration)
      }
    },
  },
})
