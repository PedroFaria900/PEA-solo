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
      
      <div v-if="filteredTickets.length === 0" class="empty-state">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#A3A8B0" stroke-width="1.5"><rect x="3" y="4" width="18" height="16" rx="2"/><path d="M7 8h10"/><path d="M7 12h10"/><path d="M7 16h5"/></svg>
        <p>Não tens bilhetes nesta secção.</p>
        <button v-if="activeTab === 'ativo'" class="btn-comprar-agora" @click="router.push('/search')">
          Comprar Bilhete
        </button>
      </div>

      <div v-else class="ticket-card" v-for="ticket in filteredTickets" :key="ticket.id" :class="{ 'is-historic': activeTab === 'historico' }">
        
        <template v-if="ticket.type === 'viagem'">
          <div class="ticket-header">
            <div class="badge gray">Viagem Simples</div>
            <span class="company">{{ ticket.companhia }}</span>
          </div>
          
          <div class="ticket-body">
            <div class="route-info">
              <span class="city">{{ ticket.origem }}</span>
              <span class="arrow">→</span>
              <span class="city">{{ ticket.destino }}</span>
            </div>
            <div class="datetime-info">
              {{ ticket.data }} • {{ ticket.hora }}
            </div>
          </div>
        </template>

        <template v-else-if="ticket.type === 'passe'">
          <div class="ticket-header">
            <div class="badge green">Passe Mensal</div>
            <span class="company">{{ ticket.zonas }}</span>
          </div>
          
          <div class="ticket-body">
            <h3 class="pass-name">{{ ticket.nome }}</h3>
            <p class="validity">{{ ticket.validade }}</p>
            
            <div class="progress-container" v-if="activeTab === 'ativo'">
              <div class="progress-labels">
                <span>{{ ticket.diasRestantes }} dias restantes</span>
                <span>{{ ticket.totalDias }} dias</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill green-fill" :style="{ width: (ticket.diasRestantes / ticket.totalDias) * 100 + '%' }"></div>
              </div>
            </div>
          </div>
        </template>

        <template v-else-if="ticket.type === 'pack'">
          <div class="ticket-header">
            <div class="badge blue">Pack Viagens</div>
            <span class="company">{{ ticket.zonas }}</span>
          </div>
          
          <div class="ticket-body">
            <h3 class="pass-name">{{ ticket.nome }}</h3>
            
            <div class="progress-container" v-if="activeTab === 'ativo'">
              <div class="progress-labels">
                <span>{{ ticket.viagensRestantes }} viagens disponíveis</span>
                <span>{{ ticket.totalViagens }} total</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill blue-fill" :style="{ width: (ticket.viagensRestantes / ticket.totalViagens) * 100 + '%' }"></div>
              </div>
            </div>
          </div>
        </template>

        <div class="ticket-footer">
          <div v-if="activeTab === 'ativo'" class="qr-action">
            <div class="qr-text">
              <span class="qr-title">Mostrar ao Validador</span>
              <span class="qr-subtitle">Clica para abrir o código QR</span>
            </div>
            <button class="qr-btn" @click="abrirQR(ticket)">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><path d="M9 11v2"/><path d="M15 11v2"/><path d="M11 9h2"/><path d="M11 15h2"/></svg>
            </button>
          </div>
          
          <div v-else class="historic-status">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6B7077" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
            Bilhete Utilizado / Expirado
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Estado ativo das abas
const activeTab = ref('ativo') // 'ativo' ou 'historico'

// Dados Mockados dos Bilhetes
const tickets = ref([
  {
    id: 1,
    type: 'viagem',
    status: 'ativo',
    origem: 'Guimarães',
    destino: 'Braga',
    data: '15 Jun 2026',
    hora: '08:30 - 09:15',
    companhia: 'Ave Mobilidade'
  },
  {
    id: 2,
    type: 'passe',
    status: 'ativo',
    nome: 'Passe Estudante (Sub23)',
    zonas: 'Zonas Braga e Guimarães',
    validade: 'Válido até 30 Jun 2026',
    diasRestantes: 16,
    totalDias: 30
  },
  {
    id: 3,
    type: 'pack',
    status: 'ativo',
    nome: 'Pack 10 Viagens',
    zonas: 'Guimarães Urbano',
    viagensRestantes: 3,
    totalViagens: 10
  },
  {
    id: 4,
    type: 'viagem',
    status: 'historico',
    origem: 'Porto (Campanhã)',
    destino: 'Guimarães',
    data: '10 Jun 2026',
    hora: '18:00 - 19:10',
    companhia: 'Rede Expresso'
  }
])

// Computed Property para filtrar bilhetes com base na aba selecionada
const filteredTickets = computed(() => {
  return tickets.value.filter(t => t.status === activeTab.value)
})

// Função simulada para abrir o QR Code
const abrirQR = (ticket) => {
  alert(`A abrir o código QR para: ${ticket.type === 'viagem' ? ticket.destino : ticket.nome}`)
}
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