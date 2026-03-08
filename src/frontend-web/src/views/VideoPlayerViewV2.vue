<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, nextTick, onBeforeMount } from 'vue'
import { useRoute } from 'vue-router'
import videojs from 'video.js'
import 'video.js/dist/video-js.css'
import '@videojs/themes/dist/forest/index.css'

const route = useRoute()
const videoElement = ref<HTMLVideoElement | null>(null)
const player = ref<any>(null)

// Busca a URL real do vídeo via encurtador e trata redirect
import { ref as vueRef } from 'vue'
import api from '../services/api'
const resolvedVideoUrl = vueRef<string | null>(null)
const videoError = vueRef<string | null | true>(null)

const fetchRedirectUrl = async () => {
  const videoId = route.params.id
  try {
    const response = await api.get(`/v1/share-url/${videoId}`, {
      maxRedirects: 0,
      params: {
        jsonResponse: true,
      },
    })

    const responseData = response.data

    if (response.status === 200 && responseData.url) {
      resolvedVideoUrl.value = responseData.url
      videoError.value = null
    } else {
      videoError.value = 'Esse video não existe ou o link expirou'
      resolvedVideoUrl.value = null
    }
  } catch (err) {
    videoError.value = 'Esse video não existe ou o link expirou'
    resolvedVideoUrl.value = null
  }
}

const initializePlayer = () => {
  if (!videoElement.value || !resolvedVideoUrl.value) return

  const options = {
    controls: true,
    responsive: true,
    fluid: true,
    loop: true,
    preload: 'metadata',
    playbackRates: [0.5, 1, 1.25, 1.5, 2],
    sources: [
      {
        src: resolvedVideoUrl.value,
        type: 'video/mp4',
      },
    ],
    techOrder: ['html5'],
    html5: {
      vhs: {
        overrideNative: true,
      },
    },
    userActions: {
      hotkeys: (event: KeyboardEvent) => {
        const playerInstance = player.value

        switch (event.key.toLowerCase()) {
          case 'arrowright':
            playerInstance.currentTime(playerInstance.currentTime() + 5)
            break
          case 'arrowleft':
            playerInstance.currentTime(playerInstance.currentTime() - 5)
            break
          case 'arrowup':
            event.preventDefault()
            playerInstance.volume(Math.min(playerInstance.volume() + 0.1, 1))
            break
          case 'arrowdown':
            event.preventDefault()
            playerInstance.volume(Math.max(playerInstance.volume() - 0.1, 0))
            break
          case 'f':
            if (playerInstance.isFullscreen()) {
              playerInstance.exitFullscreen()
            } else {
              playerInstance.requestFullscreen()
            }
            break
          case ' ':
            event.preventDefault()
            if (playerInstance.paused()) {
              playerInstance.play()
            } else {
              playerInstance.pause()
            }
            break
        }
      },
    },
  }

  player.value = videojs(videoElement.value, options, () => {
    console.log('Video.js player inicializado')
  })

  player.value.ready(() => {
    if (player.value.qualityLevels) {
      player.value.qualityLevels().on('addqualitylevel', () => {
        player.value.qualityLevels().selectedIndex = -1
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

onMounted(async () => {
  await fetchRedirectUrl()
  if (resolvedVideoUrl.value) {
    initializePlayer()
  }
})

onBeforeUnmount(() => {
  destroyPlayer()
})
</script>

<template>
  <main class="flex h-full items-center justify-center bg-black p-4">
    <div class="w-full max-w-6xl">
      <div v-if="!resolvedVideoUrl && !videoError" class="text-center">
        <div class="py-8 text-center text-xl">Carregando vídeo...</div>
      </div>
      <div v-else-if="videoError" class="text-center">
        <div class="py-8 text-center text-xl">
          {{ videoError }}
        </div>
        <router-link
          to="/"
          class="inline-flex justify-center rounded-md border border-transparent px-6 py-3 text-base font-medium focus:ring-blue-500 focus:ring-offset-2 focus:outline-none hover:focus:ring-2"
        >
          Voltar para a Home
        </router-link>
      </div>
      <div v-else class="relative w-full">
        <video
          ref="videoElement"
          class="video-js vjs-theme-forest vjs-16-9 h-auto w-full"
          data-setup=""
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
