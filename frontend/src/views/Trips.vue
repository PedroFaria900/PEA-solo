<template>
  <div class="page">
    
    <header class="header">
      <h1 class="titulo">Os meus bilhetes</h1>
      
      <button class="btn-add" @click="router.push('/comprar-bilhetes')">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
      </button>
    </header>

    <div class="tabs-container">
      <div class="tabs-bg">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'ativo' }" 
          @click="activeTab = 'ativo'"
        >
          Ativos
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'historico' }" 
          @click="activeTab = 'historico'"
        >
          Histórico
        </button>
      </div>
    </div>

    <div class="tickets-list">
      
      <div v-if="loading" class="empty-state">
        <p style="color:#A3A8B0;">A carregar...</p>
      </div>

      <div v-else-if="filteredTickets.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#A3A8B0" stroke-width="1.5"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M7 8h10"/><path d="M7 12h10"/><path d="M7 16h5"/></svg>
        <p>Não tens bilhetes nesta secção.</p>
        <button v-if="activeTab === 'ativo'" class="btn-comprar-agora" @click="router.push('/search')">
          Comprar Bilhete
        </button>
      </div>

      <!-- ATIVOS: títulos reais da API -->
      <template v-else-if="activeTab === 'ativo'">
        <div class="ticket-card" v-for="ticket in filteredTickets" :key="ticket.id">
          
          <!-- PASSE -->
          <template v-if="ticket.tipo === 'PASSE'">
            <div class="ticket-header">
              <div class="badge green">Passe {{ ticket.periodo === 'ANUAL' ? 'Anual' : 'Mensal' }}</div>
              <span class="company">{{ ticket.areaGeografica || 'Rede Geral' }}</span>
            </div>
            <div class="ticket-body">
              <h3 class="pass-name">Passe {{ ticket.periodo === 'ANUAL' ? 'Anual' : 'Mensal' }}</h3>
              <p class="validity" v-if="ticket.expiraEm">Válido até {{ formatDate(ticket.expiraEm) }}</p>
              <p class="validity" v-else>Aguarda ativação</p>
            </div>
          </template>

          <!-- PACK -->
          <template v-else-if="ticket.tipo === 'PACK'">
            <div class="ticket-header">
              <div class="badge blue">Pack Viagens</div>
              <span class="company">{{ ticket.areaGeografica || 'Rede Geral' }}</span>
            </div>
            <div class="ticket-body">
              <h3 class="pass-name">Pack {{ ticket.viagensRestantes ?? '?' }} viagens restantes</h3>
              <div class="progress-container">
                <div class="progress-bar">
                  <div class="progress-fill blue-fill" :style="{ width: Math.min(100, ((ticket.viagensRestantes ?? 0) / 20) * 100) + '%' }"></div>
                </div>
              </div>
            </div>
          </template>

          <!-- BILHETE -->
          <template v-else>
            <div class="ticket-header">
              <div class="badge gray">Bilhete Simples</div>
              <span class="company">{{ ticket.areaGeografica || 'Rede Geral' }}</span>
            </div>
            <div class="ticket-body">
              <p class="validity">{{ ticket.estado === 'PENDENTE' ? 'Aguarda ativação' : (ticket.expiraEm ? 'Válido até ' + formatDate(ticket.expiraEm) : 'Ativo') }}</p>
            </div>
          </template>

          <div class="ticket-footer">
            <div class="qr-action">
              <div class="qr-text">
                <span class="qr-title">Mostrar ao Validador</span>
                <span class="qr-subtitle">Clica para abrir o código QR</span>
              </div>
              <button class="qr-btn" @click="router.push('/simulator')">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/></svg>
              </button>
            </div>
          </div>
        </div>
      </template>

      <!-- HISTÓRICO: viagens da API -->
      <template v-else>
        <div class="ticket-card is-historic" v-for="viagem in filteredTickets" :key="viagem.id">
          <div class="ticket-header">
            <div class="badge gray">Viagem</div>
            <span class="company">{{ viagem.linha || 'Linha desconhecida' }}</span>
          </div>
          <div class="ticket-body">
            <div class="datetime-info">{{ formatDate(viagem.momento) }}</div>
          </div>
          <div class="ticket-footer">
            <div class="historic-status">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6B7077" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
              Viagem realizada
            </div>
          </div>
        </div>
      </template>

    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// Estado ativo das abas
const activeTab = ref('ativo') // 'ativo' ou 'historico'
const loading = ref(true)

// Dados reais da API
const tickets = ref([])
const viagens = ref([])

// Tickets ativos (passe, pack, bilhete com estado ATIVO ou PENDENTE)
const activeTickets = computed(() =>
  tickets.value.filter(t => t.estado === 'ATIVO' || t.estado === 'PENDENTE')
)

// Historico: viagens realizadas
const filteredTickets = computed(() => {
  if (activeTab.value === 'ativo') return activeTickets.value
  return viagens.value
})

const ticketTypeLabel = (t) => {
  const map = { PASSE: 'Passe', PACK: 'Pack', BILHETE: 'Bilhete' }
  return map[t.tipo] || t.tipo
}

const formatDate = (dt) => {
  if (!dt) return '-'
  return new Date(dt).toLocaleString('pt-PT', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

// Carrega dados ao montar
const fetchData = async () => {
  loading.value = true
  try {
    const [titRes, viRes] = await Promise.all([
      axios.get('/api/titulos'),
      axios.get('/api/viagens')
    ])
    tickets.value = titRes.data || []
    viagens.value = viRes.data || []
  } catch { /* ignore */ } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page {
  background: #F7F8FA;
  min-height: 100vh;
  padding: 38px 28px 100px 28px;
  font-family: 'Roboto', sans-serif;
  box-sizing: border-box;
}

/* --- HEADER FLEXBOX --- */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.titulo {
  font-weight: 700;
  font-size: 30px;
  color: #15171A;
  margin: 0; /* Margem removida daqui para manter o alinhamento com o botão */
  letter-spacing: -0.4px;
}

/* Botão Adicionar no Topo */
.btn-add {
  width: 42px;
  height: 42px;
  background: #0085FF;
  border-radius: 12px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0px 4px 10px rgba(0, 133, 255, 0.25);
  cursor: pointer;
  flex-shrink: 0; /* Garante que o botão não é esmagado se o ecrã for pequeno */
  transition: transform 0.2s ease, background 0.2s ease;
}

.btn-add:active {
  transform: scale(0.92);
}

.btn-add:hover {
  background: #0073E6;
}

/* Tabs */
.tabs-container {
  margin-bottom: 24px;
}

.tabs-bg {
  background: #E4E7EB;
  border-radius: 12px;
  display: flex;
  padding: 4px;
}

.tab-btn {
  flex: 1;
  padding: 10px 0;
  border: none;
  background: transparent;
  font-size: 15px;
  font-weight: 600;
  color: #6B7077;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  background: #FFFFFF;
  color: #15171A;
  box-shadow: 0px 2px 4px rgba(0,0,0,0.05);
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  text-align: center;
  color: #6B7077;
}

.empty-state p { margin: 16px 0; font-size: 15px; }

.btn-comprar-agora {
  background: #0085FF;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 999px;
  font-weight: 600;
  cursor: pointer;
}

/* Tickets List */
.tickets-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ticket-card {
  background: #FFFFFF;
  border: 1px solid #E4E7EB;
  border-radius: 20px;
  box-shadow: 0px 4px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
  transition: opacity 0.3s;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.ticket-card.is-historic {
  opacity: 0.65;
}

/* Header do Ticket */
.ticket-header {
  padding: 16px 20px;
  border-bottom: 1px dashed #EDEFF2;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 6px;
}
.badge.gray { background: #F2F4F7; color: #3A3F45; }
.badge.green { background: #E7F6EC; color: #1F9D4D; }
.badge.blue { background: #EAF4FF; color: #0085FF; }

.company {
  font-size: 13px;
  font-weight: 500;
  color: #6B7077;
}

/* Body do Ticket */
.ticket-body {
  padding: 20px;
}

.route-info {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #15171A;
  margin-bottom: 6px;
}
.route-info .arrow { color: #A3A8B0; }

.datetime-info {
  font-size: 14px;
  color: #6B7077;
}

.pass-name {
  font-size: 18px;
  font-weight: 700;
  color: #15171A;
  margin: 0 0 4px 0;
}

.validity {
  font-size: 14px;
  color: #6B7077;
  margin: 0 0 16px 0;
}

/* Progress Bars */
.progress-container { margin-top: 12px; }
.progress-labels {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  font-weight: 500;
  color: #3A3F45;
  margin-bottom: 8px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #EDEFF2;
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.5s ease-out;
}
.green-fill { background: #1F9D4D; }
.blue-fill { background: #0085FF; }

/* Footer do Ticket */
.ticket-footer {
  padding: 16px 20px;
  background: #F9FAFB;
  border-top: 1px solid #EDEFF2;
}

.qr-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.qr-text {
  display: flex;
  flex-direction: column;
}

.qr-title { font-size: 14px; font-weight: 600; color: #15171A; }
.qr-subtitle { font-size: 12px; color: #6B7077; }

.qr-btn {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #0085FF;
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform 0.1s;
}
.qr-btn:active { transform: scale(0.95); }

.historic-status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #6B7077;
}
</style>