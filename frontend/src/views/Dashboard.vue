<template>
  <div>
    <!-- App Bar -->
    <div class="app-bar">
      <div>
        <div style="font-size: 0.75rem; color: var(--text-muted); font-weight: 500;">Bem-vindo,</div>
        <div class="app-bar-title">{{ authStore.userName || 'Utente' }}</div>
      </div>
      <div class="app-bar-avatar">{{ initials }}</div>
    </div>

    <div class="page-content">
      <!-- Wallet summary strip -->
      <div style="background: var(--blue); border-radius: 14px; padding: 14px 16px; margin: 16px 0; display: flex; justify-content: space-between; align-items: center; color: #fff;">
        <div>
          <div style="font-size: 0.75rem; opacity: 0.8; font-weight: 500;">Carteira</div>
          <div style="font-size: 1.6rem; font-weight: 800; letter-spacing: -0.02em;">{{ authStore.userSaldo.toFixed(2) }}€</div>
        </div>
        <router-link to="/profile" style="background: rgba(255,255,255,0.2); border-radius: 8px; padding: 8px 14px; color: #fff; font-size: 0.82rem; font-weight: 600; text-decoration: none;">
          Carregar
        </router-link>
      </div>

      <!-- Tickets section -->
      <div class="section-header">
        <span class="section-title">Os meus bilhetes</span>
        <router-link to="/buy" class="section-link">+ Comprar</router-link>
      </div>

      <div v-if="loadingTickets" class="spinner">A carregar bilhetes...</div>

      <div v-else-if="tickets.length === 0" class="empty-state">
        <div class="empty-icon">🎫</div>
        <h4>Sem títulos ativos</h4>
        <p>Ainda não tens nenhum bilhete ou passe.</p>
        <router-link to="/buy" class="btn btn-primary" style="margin-top: 16px; display: inline-flex;">Comprar Agora</router-link>
      </div>

      <div v-else>
        <TicketCard v-for="ticket in tickets" :key="ticket.id" :ticket="ticket" />
      </div>

      <!-- Quick actions -->
      <div class="section-header" style="margin-top: 8px;">
        <span class="section-title">Acesso rápido</span>
      </div>
      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px;">
        <router-link to="/simulator" class="card" style="text-decoration: none; text-align: center; padding: 18px 12px;">
          <div style="font-size: 1.8rem; margin-bottom: 6px;">📲</div>
          <div style="font-size: 0.85rem; font-weight: 600; color: var(--text-dark);">Validar título</div>
        </router-link>
        <router-link to="/trips" class="card" style="text-decoration: none; text-align: center; padding: 18px 12px;">
          <div style="font-size: 1.8rem; margin-bottom: 6px;">🕒</div>
          <div style="font-size: 0.85rem; font-weight: 600; color: var(--text-dark);">Viagens</div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../store/auth'
import axios from 'axios'
import TicketCard from '../components/TicketCard.vue'

export default {
  name: 'Dashboard',
  components: { TicketCard },
  setup() {
    const authStore = useAuthStore()
    const tickets = ref([])
    const loadingTickets = ref(true)

    const initials = computed(() => {
      const name = authStore.userName || ''
      return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() || '?'
    })

    const fetchTickets = async () => {
      loadingTickets.value = true
      try {
        const res = await axios.get('/api/titulos')
        tickets.value = res.data
      } catch (err) {
        console.error('Erro ao ir buscar títulos:', err)
      } finally {
        loadingTickets.value = false
      }
    }

    onMounted(() => {
      authStore.fetchProfile()
      fetchTickets()
    })

    return { authStore, tickets, loadingTickets, initials }
  }
}
</script>
