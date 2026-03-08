<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute } from 'vue-router'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import '@videojs/themes/dist/forest/index.css'

const route = useRoute()
const videoElement = ref<HTMLVideoElement | null>(null)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const player = ref<any>(null)

const videoUrl = computed(() => {
  const videoId = route.params.id
  return `https://videos.vitorweirich.com/v1/share-url/${videoId}`
})

const initializePlayer = () => {
  if (!videoElement.value) return

  // Configurações otimizadas do Video.js
  const options = {
    controls: true,
    responsive: true,
    fluid: true,
    loop: true, // Ativa o loop
    preload: 'metadata', // Carrega apenas metadados inicialmente para performance
    playbackRates: [0.5, 1, 1.25, 1.5, 2], // Opções de velocidade
    sources: [
      {
        src: videoUrl.value,
        type: 'video/mp4',
      },
    ],
    // Configurações para melhor performance
    techOrder: ['html5'],
    html5: {
      vhs: {
        overrideNative: true,
      },
    },
  }

  // Inicializa o player
  player.value = videojs(videoElement.value, options, () => {
    console.log('Video.js player inicializado')
  })

  // Otimizações adicionais
  player.value.ready(() => {
    // Define qualidade automática se disponível
    if (player.value.qualityLevels) {
      player.value.qualityLevels().on('addqualitylevel', () => {
        player.value.qualityLevels().selectedIndex = -1 // Auto quality
      })
    }
  })
}

const destroyPlayer = () => {
  if (player.value) {
    player.value.dispose()
    player.value = null
  }
}

onMounted(() => {
  initializePlayer()
})

onBeforeUnmount(() => {
  destroyPlayer()
})
</script>

<template>
  <main class="flex h-full items-center justify-center bg-black p-4">
    <div class="w-full max-w-6xl">
      <!-- Container do Video.js -->
      <div class="relative w-full">
        <video
          ref="videoElement"
          class="video-js vjs-theme-forest video-js w-full"
          data-setup="{}"
          preload="metadata"
        >
          <p class="vjs-no-js p-8 text-center">
            Para assistir este vídeo, por favor ative o JavaScript e considere atualizar para um
            <a href="https://videojs.com/html5-video-support/" target="_blank" class="underline">
              navegador que suporte HTML5 video </a
            >.
          </p>
        </video>
      </div>
    </div>
  </main>
</template>

<style>
.video-js .vjs-time-tooltip {
  font-size: 1.3em !important;
  font-weight: 500 !important;
  color: green !important;
}

.video-js .vjs-remaining-time {
  font-size: 1.3em !important;
  font-weight: 500 !important;
}
</style>
