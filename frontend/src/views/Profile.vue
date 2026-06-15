<template>
  <div class="dashboard-wrapper">
    
    <header class="header">
      <h1 class="greeting">Olá, {{ firstName }}!</h1>
      <button class="notification-btn">
        <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#3A3F45" stroke-width="1.8"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
      </button>
    </header>

    <section class="profile-card">
      <div class="avatar-wrapper">
        <div class="avatar-bg"></div>
        <div class="avatar-initials">{{ userInitials }}</div>
      </div>
      <h2 class="user-name">{{ authStore.userName }}</h2>
      <p class="user-email">{{ authStore.userEmail }}</p>
      <div v-if="hasActivePass" class="badge-active">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
        Passe Mensal ativo
      </div>
    </section>

    <section class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon-wrapper blue-bg">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2"><rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2"/><path d="M6 12h.01M18 12h.01"/></svg>
        </div>
        <div class="stat-value">{{ saldo.toFixed(2).replace('.', ',') }} <span>€</span></div>
        <div class="stat-label">Saldo disponível</div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon-wrapper green-bg">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1F9D4D" stroke-width="2"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/></svg>
        </div>
        <div class="stat-value">{{ tripsThisMonth }}</div>
        <div class="stat-label">Viagens este mês</div>
      </div>
    </section>

    <section class="recent-trips-section">
      <div class="trips-header">
        <h3 class="trips-title">Últimas viagens</h3>
        <router-link to="/trips" class="trips-link">ver todas</router-link>
      </div>

      <div class="trips-card">
        <template v-if="recentTrips.length > 0">
          <div 
            v-for="(trip, index) in recentTrips" 
            :key="trip.id" 
            class="trip-item"
            :class="{ 'border-b': index !== recentTrips.length - 1 }"
          >
            <div class="trip-icon" :class="trip.usedPass ? 'blue-bg-solid' : 'dark-bg'">
              <svg v-if="trip.usedPass" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#FFF" stroke-width="1.5"><rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 7h8"/><path d="M8 11h8"/><path d="M8 15h4"/></svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#FFF" stroke-width="1.5"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M7 7h10"/><path d="M7 11h10"/><path d="M7 15h5"/></svg>
            </div>
            <div class="trip-info">
              <div class="trip-route">{{ trip.origin }} <span class="arrow">→</span> {{ trip.destination }}</div>
              <div class="trip-time">{{ trip.date }}</div>
            </div>
            <div v-if="trip.usedPass" class="trip-badge-blue">Passe</div>
            <div v-else class="trip-price">{{ trip.price.toFixed(2).replace('.', ',') }}€</div>
          </div>
        </template>
        <div v-else class="empty-trips">
          Ainda não fizeste nenhuma viagem.
        </div>
      </div>
    </section>

    <button class="logout-btn-full" @click="handleLogout">
      Terminar sessão
    </button>

    <!-- SEM bottom-navbar aqui — o App.vue já renderiza o Navbar.vue globalmente -->

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()

// ── Reactivo direto da store ──
const firstName = computed(() => (authStore.userName || 'Utilizador').split(' ')[0])
const userInitials = computed(() => {
  const names = (authStore.userName || 'U').trim().split(' ')
  if (names.length >= 2) return (names[0][0] + names[names.length - 1][0]).toUpperCase()
  return names[0][0].toUpperCase()
})
const saldo = computed(() => authStore.userSaldo)

// ── Estado API ──
const hasActivePass = ref(false)
const allTrips = ref([])

onMounted(async () => {
  try {
    const [viagensRes, titulosRes] = await Promise.all([
      axios.get('/api/viagens'),
      axios.get('/api/titulos')
    ])
    
    // Tratar viagens
    allTrips.value = viagensRes.data.map(v => {
      const parts = v.linha ? v.linha.split(' - ') : ['Desconhecida']
      return {
        id: v.id,
        origin: parts[0] || 'Desconhecida',
        destination: parts[1] || '',
        date: v.momento,
        price: v.precoPago || 0,
        usedPass: v.tituloUtilizado && v.tituloUtilizado.tipo === 'PASSE'
      }
    })

    // Tem passe ativo?
    hasActivePass.value = titulosRes.data.some(t => t.tipo === 'PASSE' && t.estado === 'ATIVO')
  } catch (err) {
    console.error('Erro ao carregar dados do perfil', err)
  }
})

// Derivado do array — não hardcoded
const tripsThisMonth = computed(() => {
  const agora = new Date()
  return allTrips.value.filter(t => {
    const d = new Date(t.date)
    return d.getMonth() === agora.getMonth() && d.getFullYear() === agora.getFullYear()
  }).length
})

// Últimas 3 viagens — derivado do mesmo array
const recentTrips = computed(() =>
  [...allTrips.value]
    .sort((a, b) => new Date(b.date) - new Date(a.date))
    .slice(0, 3)
    .map(t => ({
      ...t,
      date: formatDate(t.date)
    }))
)

function formatDate(isoString) {
  const d = new Date(isoString)
  const agora = new Date()
  const ontem = new Date(agora)
  ontem.setDate(agora.getDate() - 1)

  const hora = d.toTimeString().slice(0, 5)
  if (d.toDateString() === agora.toDateString()) return `Hoje, ${hora}`
  if (d.toDateString() === ontem.toDateString()) return `Ontem, ${hora}`
  return `${d.getDate()} ${d.toLocaleString('pt-PT', { month: 'short' })}, ${hora}`
}

const handleLogout = () => {
  authStore.logout()
  router.push({ name: 'Login' })
}
</script>

<style scoped>
.dashboard-wrapper {
  background: #FFFFFF;
  min-height: 100vh;
  padding: 38px 28px 100px 28px;
  font-family: 'Roboto', -apple-system, sans-serif;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.greeting {
  font-weight: 700;
  font-size: 30px;
  line-height: 32px;
  letter-spacing: -0.4px;
  color: #0085FF;
  margin: 0;
}

.notification-btn {
  width: 42px;
  height: 42px;
  background: #F2F4F7;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
}

.profile-card {
  background: #FFFFFF;
  border: 1px solid #E4E7EB;
  box-shadow: 0px 1px 2px rgba(16, 24, 40, 0.04), 0px 6px 18px rgba(16, 24, 40, 0.07);
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
  width: 84px;
  height: 84px;
  margin-bottom: 12px;
}

.avatar-bg {
  width: 100%;
  height: 100%;
  background: #EAF4FF;
  border-radius: 999px;
}

.avatar-initials {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 66px;
  height: 66px;
  background: #0085FF;
  border-radius: 50%;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 24px;
}

.user-name {
  font-weight: 700;
  font-size: 19px;
  color: #15171A;
  margin: 0 0 4px 0;
}

.user-email {
  font-weight: 400;
  font-size: 14px;
  color: #6B7077;
  margin: 0 0 16px 0;
}

.badge-active {
  background: #E7F6EC;
  border-radius: 999px;
  padding: 6px 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 12px;
  color: #1F9D4D;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #FFFFFF;
  border: 1px solid #E4E7EB;
  box-shadow: 0px 1px 2px rgba(16, 24, 40, 0.04), 0px 6px 18px rgba(16, 24, 40, 0.07);
  border-radius: 20px;
  padding: 16px;
}

.stat-icon-wrapper {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
}

.stat-icon-wrapper.blue-bg  { background: rgba(0, 133, 255, 0.1); }
.stat-icon-wrapper.green-bg { background: rgba(31, 157, 77, 0.1); }

.stat-value {
  font-weight: 700;
  font-size: 21px;
  color: #15171A;
  margin-bottom: 4px;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-value span { font-size: 18px; }

.stat-label {
  font-weight: 400;
  font-size: 12.5px;
  color: #6B7077;
}

.recent-trips-section { margin-bottom: 24px; }

.trips-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.trips-title {
  font-weight: 700;
  font-size: 20px;
  letter-spacing: -0.2px;
  color: #15171A;
  margin: 0;
}

.trips-link {
  font-weight: 500;
  font-size: 16px;
  color: #0085FF;
  text-decoration: none;
}

.trips-card {
  background: #FFFFFF;
  border: 1px solid #E4E7EB;
  box-shadow: 0px 1px 2px rgba(16, 24, 40, 0.04), 0px 6px 18px rgba(16, 24, 40, 0.07);
  border-radius: 20px;
  padding: 8px 16px;
}

.trip-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
}

.border-b { border-bottom: 1px solid #EDEFF2; }

.trip-icon {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.trip-icon.dark-bg      { background: #15171A; }
.trip-icon.blue-bg-solid { background: #0085FF; }

.trip-info { flex-grow: 1; }

.trip-route {
  font-weight: 600;
  font-size: 15px;
  color: #15171A;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.trip-route .arrow { color: #6B7077; font-size: 12px; }

.trip-time {
  font-weight: 400;
  font-size: 13px;
  color: #6B7077;
}

.trip-price {
  font-size: 16px;
  font-weight: 700;
  color: #1A1A1A;
}

.empty-trips {
  padding: 24px;
  text-align: center;
  color: #A3A8B0;
  font-size: 14px;
}

.trip-badge-blue {
  background: #EAF4FF;
  border-radius: 999px;
  padding: 4px 10px;
  font-weight: 600;
  font-size: 12px;
  color: #0085FF;
}

.logout-btn-full {
  width: 100%;
  height: 54px;
  background: #EAF4FF;
  border-radius: 999px;
  border: none;
  font-weight: 700;
  font-size: 17px;
  color: #0085FF;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  transition: background 0.2s;
}

.logout-btn-full:hover { background: #D5E8FF; }
</style>