<template>
  <!-- Auth pages: no nav bar -->
  <template v-if="isAuthRoute" />
  <!-- Authenticated pages: show bottom nav -->
  <nav v-else-if="authStore.isAuthenticated" class="bottom-nav">
    <router-link to="/profile" class="nav-item" active-class="active">
      <span class="nav-icon">👤</span>
      <span class="nav-label">Perfil</span>
    </router-link>
    <router-link to="/search" class="nav-item" active-class="active">
      <span class="nav-icon">📍</span>
      <span class="nav-label">Rota</span>
    </router-link>
    <router-link to="/simulator" class="nav-item" active-class="active">
      <span class="nav-icon">📲</span>
      <span class="nav-label">Validar</span>
    </router-link>
    <router-link to="/" class="nav-item" exact-active-class="active">
      <span class="nav-icon">🎫</span>
      <span class="nav-label">Bilhetes</span>
    </router-link>
  </nav>
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'

export default {
  name: 'Navbar',
  setup() {
    const authStore = useAuthStore()
    const route = useRoute()
    const isAuthRoute = computed(() => ['Login', 'Register'].includes(route.name))
    return { authStore, isAuthRoute }
  }
}
</script>
