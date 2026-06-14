<template>
  <nav v-if="authStore.isAuthenticated && !isAuthRoute" class="bottom-navbar">
    
    <router-link to="/profile" class="nav-item" active-class="active">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
      <span>Conta</span>
    </router-link>
    
    <router-link to="/search" class="nav-item" active-class="active">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
      <span>Mapa</span>
    </router-link>
    
    <router-link to="/simulator" class="nav-item" active-class="active">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><rect x="7" y="7" width="3" height="3"/><rect x="14" y="7" width="3" height="3"/><rect x="7" y="14" width="3" height="3"/><rect x="14" y="14" width="3" height="3"/></svg>
      <span>Validar</span>
    </router-link>
    
    <router-link to="/trips" class="nav-item" active-class="active">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
      <span>Bilhetes</span>
    </router-link>

  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'

const authStore = useAuthStore()
const route = useRoute()

// Lógica para esconder a navbar caso estejas numa destas rotas
const isAuthRoute = computed(() => ['Login', 'Register'].includes(route.name))
</script>

<style scoped>
.bottom-navbar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 63px; /* Exatamente a altura do teu SVG */
  background: #FFFFFF;
  box-shadow: 0px -4px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 0 10px;
  box-sizing: border-box;
  z-index: 100; /* Garante que a nav está sempre acima de tudo o resto na página */
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 86.5px;
  height: 51px;
  border-radius: 12px;
  color: #6B7077;
  font-family: 'Roboto', sans-serif;
  font-weight: 500;
  font-size: 10px;
  text-decoration: none; /* Remove o sublinhado padrão dos links HTML */
  transition: all 0.2s ease;
}

.nav-item svg {
  width: 24px;
  height: 24px;
  margin-bottom: 4px;
  stroke: #6B7077; /* Cor base dos ícones */
}

/* O Vue Router injeta a classe "active" automaticamente na rota atual */
.nav-item.active {
  background: #0085FF;
  color: #FFFFFF;
}

.nav-item.active svg {
  stroke: #FFFFFF;
}
</style>