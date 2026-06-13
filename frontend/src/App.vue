<template>
  <div id="app-root" style="display:flex;flex-direction:column;height:100%;">
    <!-- Only show content if not loading -->
    <template v-if="!authStore.loading">
      <div class="page">
        <router-view />
      </div>
      <Navbar />
    </template>
    <div v-else class="spinner">A carregar...</div>
  </div>
</template>

<script>
import { useAuthStore } from './store/auth'
import Navbar from './components/Navbar.vue'

export default {
  name: 'App',
  components: { Navbar },
  setup() {
    const authStore = useAuthStore()
    return { authStore }
  }
}
</script>
