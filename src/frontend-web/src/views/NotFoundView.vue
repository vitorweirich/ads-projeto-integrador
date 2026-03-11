<script setup lang="ts">
const props = defineProps<{ message?: string }>()
</script>

<template>
  <div class="flex items-center justify-center px-4 py-12 sm:px-6 lg:px-8">
    <div class="w-full max-w-md space-y-8 text-center">
      <div>
        <h1 class="mt-6 text-center text-4xl font-extrabold">404</h1>
        <h2 class="mt-2 text-center text-2xl font-bold">Página não encontrada</h2>
      </div>
      <div class="mt-4">
        <p class="">{{ props.message || 'Verifique se a URL está correta.' }}</p>
      </div>
      <div class="mt-6">
        <router-link
          to="/"
          class="inline-flex justify-center rounded-md border border-transparent px-6 py-3 text-base font-medium focus:ring-blue-500 focus:ring-offset-2 focus:outline-none hover:focus:ring-2"
        >
          Voltar para a Home
        </router-link>
      </div>
    </div>
  </div>
</template>
