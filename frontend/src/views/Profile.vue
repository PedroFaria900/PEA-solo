<template>
  <div>
    <!-- App Bar -->
    <div class="app-bar">
      <span class="app-bar-title">Perfil</span>
      <button @click="logout" style="background:none;border:none;cursor:pointer;font-size:0.82rem;font-weight:600;color:var(--red);">Sair</button>
    </div>

    <div class="page-content" v-if="authStore.user">
      <!-- Avatar + name -->
      <div style="text-align: center; padding: 24px 0 16px;">
        <div class="profile-avatar">{{ initials }}</div>
        <div class="profile-name">{{ authStore.user.nome }}</div>
        <div class="profile-email">{{ authStore.user.email }}</div>
      </div>

      <!-- Info card -->
      <div class="card">
        <div class="profile-info-row">
          <span class="profile-info-label">📧 E-mail</span>
          <span class="profile-info-value">{{ authStore.user.email }}</span>
        </div>
        <div class="profile-info-row">
          <span class="profile-info-label">📱 Telemóvel</span>
          <span class="profile-info-value">{{ authStore.user.telemovel || '—' }}</span>
        </div>
        <div class="profile-info-row" style="border-bottom: none;">
          <span class="profile-info-label">👤 Tipo</span>
          <span class="badge badge-blue">Utente</span>
        </div>
      </div>

      <!-- Wallet card (purple gradient like carteira.png) -->
      <div class="wallet-hero" style="margin-top: 16px; cursor: pointer;" @click="$router.push('/wallet')">
        <div style="display: flex; justify-content: space-between; align-items: flex-start;">
          <div>
            <div class="wallet-label">Saldo disponível</div>
            <div class="wallet-amount">{{ authStore.userSaldo.toFixed(2) }}€</div>
            <div class="wallet-sub">Toque para gerir a carteira</div>
          </div>
          <div style="background: rgba(255,255,255,0.15); border-radius: 10px; padding: 8px 12px; font-size: 0.82rem; font-weight: 600; color: #fff;">
            Ver › 
          </div>
        </div>
      </div>

      <!-- Quick links row -->
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 4px;">
        <button @click="$router.push('/wallet/charge')" class="card" style="border: none; cursor: pointer; text-align: center; padding: 16px 12px; display: flex; flex-direction: column; align-items: center; gap: 6px;">
          <span style="font-size: 1.6rem;">💰</span>
          <span style="font-size: 0.82rem; font-weight: 600; color: var(--text-dark);">Carregar Saldo</span>
        </button>
        <button @click="$router.push('/wallet')" class="card" style="border: none; cursor: pointer; text-align: center; padding: 16px 12px; display: flex; flex-direction: column; align-items: center; gap: 6px;">
          <span style="font-size: 1.6rem;">📋</span>
          <span style="font-size: 0.82rem; font-weight: 600; color: var(--text-dark);">Extrato</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'

export default {
  name: 'Profile',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()

    const initials = computed(() => {
      const n = authStore.user?.nome || ''
      return n.split(' ').map(x => x[0]).join('').substring(0, 2).toUpperCase() || '?'
    })

    const logout = () => { authStore.logout(); router.push({ name: 'Login' }) }

    onMounted(() => authStore.fetchProfile())

    return { authStore, initials, logout }
  }
}
</script>
