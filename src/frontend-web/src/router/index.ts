import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/upload',
      name: 'upload',
      component: () => import('../views/UploadView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/upload-multiple',
      name: 'upload-multiple',
      component: () => import('../views/MultipleUploadView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('../views/ForgotPasswordView.vue'),
    },
    {
      path: '/reset-password/:token',
      name: 'reset-password',
      component: () => import('../views/ResetPasswordView.vue'),
    },
    {
      path: '/list-videos',
      name: 'list-videos',
      component: () => import('../views/ListVideosView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/view-video/:id',
      name: 'view-video',
      component: () => import('../views/ViewVideoView.vue'),
      meta: { requiresAuth: true },
      props: true,
    },
    {
      path: '/watch/:id',
      name: 'VideoPlayerViewV2',
      component: () => import('../views/VideoPlayerViewV2.vue'),
      props: true,
    },
    {
      path: '/verify-mfa',
      name: 'verify-mfa',
      component: () => import('../views/VerifyMfaView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/confirm-email/:token',
      name: 'confirm-email',
      component: () => import('../views/ConfirmEmailView.vue'),
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('../views/AdminUsersView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      props: true,
    },
    {
      path: '/admin/videos',
      name: 'admin-videos',
      component: () => import('../views/AdminVideosView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      props: true,
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../views/NotFoundView.vue'),
    },
  ],
})

// Navigation guard for protected routes
router.beforeEach(async (to, from, next) => {
  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
  const requiresAdmin = to.matched.some((r) => r.meta.requiresAdmin)

  if (!requiresAuth && !requiresAdmin) {
    return next()
  }

  const authStore = useAuthStore()
  await authStore.waitUntilReady()

  if (!authStore.isAuthenticated) {
    return next({
      path: '/login',
      query: { redirect: to.fullPath },
    })
  }

  if (requiresAdmin && !authStore.isAdmin) {
    // TODO: Talvez adicionar uma mensagem?
    return next({ name: 'home' })
  }

  next()
})

export default router
