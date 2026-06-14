<template>
  <div class="page">

    <h1 class="titulo">Pesquisa rotas</h1>

    <div class="search-card">
      <div class="search-body">

        <div class="icons-col">
          <div class="dot-origem"></div>
          <div class="conector"></div>
          <svg width="14" height="18" viewBox="0 0 16 22" fill="none">
            <path d="M8 21C8 21 14.5 13.5 14.5 8A6.5 6.5 0 0 0 1.5 8C1.5 13.5 8 21 8 21Z" stroke="#D63A2E" stroke-width="1.5" stroke-linejoin="round"/>
            <circle cx="8" cy="8" r="2.5" stroke="#D63A2E" stroke-width="1.5"/>
          </svg>
        </div>

        <div class="inputs-col relative">
          <input v-model="origem" type="text" placeholder="De onde?" class="campo-input" @input="hasSearched = false" />
          <div class="sep"></div>
          <input v-model="destino" type="text" placeholder="Para onde?" class="campo-input" @input="hasSearched = false" />
          
          <button class="btn-swap" @click="trocar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="16 3 21 8 16 13"></polyline><line x1="21" y1="8" x2="9" y2="8"></line><polyline points="8 21 3 16 8 11"></polyline><line x1="3" y1="16" x2="15" y2="16"></line></svg>
          </button>
        </div>

      </div>
    </div>

    <div class="chips">
      <button 
        v-for="data in datasDisponiveis" 
        :key="data.id"
        class="chip" 
        :class="{ active: dataSelecionada === data.id }"
        @click="dataSelecionada = data.id"
      >
        {{ data.label }}
      </button>
    </div>

    <button class="btn-pesquisar" @click="handleSearch">Pesquisar</button>

    <div v-if="!hasSearched" class="rotas-section fade-in">
      <h3 class="subtitulo">Rotas Populares</h3>
      <div class="rotas">
        <div class="rota-card" v-for="rota in rotasPopulares" :key="rota.id" @click="preencherRota(rota)">
          <div class="rota-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s-8-4.5-8-11.8A8 8 0 0 1 12 2a8 8 0 0 1 8 8.2c0 7.3-8 11.8-8 11.8z"/><circle cx="12" cy="10" r="3"/></svg>
          </div>
          <div class="rota-text">
            <span>{{ rota.origem }}</span>
            <span class="arrow">→</span>
            <span>{{ rota.destino }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="resultados-section fade-in">
      <div class="resultados-header">
        <h3 class="subtitulo">Resultados</h3>
        <span class="resultados-count">{{ resultadosPesquisa.length }} viagens</span>
      </div>

      <div class="resultados">
        <div class="resultado-card" v-for="viagem in resultadosPesquisa" :key="viagem.id">
           
          <div class="resultado-top">
            <div class="time-col">
              <span class="time">{{ viagem.horaPartida }}</span>
              <span class="city">{{ origem }}</span>
            </div>
            
            <div class="duration-col">
              <span class="duration">{{ viagem.duracao }}</span>
              <div class="duration-line">
                <span class="dot"></span>
                <span class="line"></span>
                <span class="dot"></span>
              </div>
              <span class="transport-type">Direto</span>
            </div>

            <div class="time-col right">
              <span class="time">{{ viagem.horaChegada }}</span>
              <span class="city">{{ destino }}</span>
            </div>
          </div>

          <div class="resultado-bottom">
            <div class="company">
               <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#6B7077" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M7 7h10"/><path d="M7 11h10"/><path d="M7 15h5"/></svg>
               Rede Expresso
            </div>
            <div class="price-action">
              <span class="price">{{ viagem.preco.toFixed(2).replace('.', ',') }}€</span>
              <button class="buy-btn" @click="comprar(viagem)">Comprar</button>
            </div>
          </div>

        </div>
      </div>
    </div>

    

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Variáveis de Estado Reativas
const origem = ref('')
const destino = ref('')
const dataSelecionada = ref(1)
const hasSearched = ref(false)

// Mock Data para Chips de Data
const datasDisponiveis = ref([
  { id: 1, label: 'Hoje' },
  { id: 2, label: 'Amanhã' },
  { id: 3, label: '14 Jun' },
  { id: 4, label: '15 Jun' }
])

// Mock Data para Rotas Populares
const rotasPopulares = ref([
  { id: 1, origem: 'Viana', destino: 'Alameda' },
  { id: 2, origem: 'Braga', destino: 'Porto' },
  { id: 3, origem: 'Guimarães', destino: 'Vila Real' }
])

const resultadosPesquisa = ref([])

// Funções Lógicas
const trocar = () => {
  const temp = origem.value
  origem.value = destino.value
  destino.value = temp
  hasSearched.value = false // reseta a pesquisa ao trocar
}

const preencherRota = (rota) => {
  origem.value = rota.origem
  destino.value = rota.destino
  handleSearch() // Pesquisa imediatamente
}

const handleSearch = () => {
  if (!origem.value || !destino.value) {
    alert('Preenche a origem e o destino primeiro!')
    return
  }
  
  // Simulação de pesquisa ao Backend (Gera horários falsos para a origem/destino escolhidos)
  resultadosPesquisa.value = [
    { id: 1, horaPartida: '09:00', horaChegada: '10:15', duracao: '1h 15m', preco: 4.50 },
    { id: 2, horaPartida: '11:30', horaChegada: '12:50', duracao: '1h 20m', preco: 5.00 },
    { id: 3, horaPartida: '16:00', horaChegada: '17:10', duracao: '1h 10m', preco: 4.00 }
  ]
  
  hasSearched.value = true
}

const comprar = (viagem) => {
  // Passamos os dados do bilhete para o router (para a página de Buy)
  router.push({ name: 'Buy', query: { origin: origem.value, dest: destino.value, price: viagem.preco } })
}
</script>

<style scoped>
.page {
  background: #FFFFFF;
  min-height: 100vh;
  padding: 38px 28px 100px 28px;
  font-family: 'Roboto', sans-serif;
  box-sizing: border-box;
}

.titulo {
  font-weight: 700;
  font-size: 30px;
  color: #15171A;
  margin: 0 0 24px 0;
  letter-spacing: -0.4px;
}

/* Card Principal da Pesquisa */
.search-card {
  background: white;
  border: 1px solid #E4E7EB;
  border-radius: 20px;
  box-shadow: 0 1px 2px rgba(16,24,40,0.04), 0 6px 18px rgba(16,24,40,0.07);
  padding: 16px;
  margin-bottom: 20px;
}

.search-body {
  display: flex;
  gap: 16px;
}

.icons-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 14px;
}

.dot-origem {
  width: 12px;
  height: 12px;
  border: 2px solid #0085FF;
  border-radius: 50%;
  background: white;
}

.conector {
  width: 2px;
  height: 24px;
  background: #EDEFF2;
  margin: 4px 0;
}

.inputs-col {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

.campo-input {
  border: none;
  font-size: 16px;
  font-weight: 500;
  color: #15171A;
  padding: 12px 0;
  width: 90%;
  outline: none;
  background: transparent;
}

.campo-input::placeholder {
  color: #6B7077;
  font-weight: 400;
}

.sep {
  height: 1px;
  background: #EDEFF2;
  width: 100%;
}

.btn-swap {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 38px;
  height: 38px;
  background: #F2F4F7;
  border-radius: 10px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-swap:hover { background: #E4E7EB; }

/* Chips */
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.chip {
  padding: 9px 16px;
  border: 1px solid #E4E7EB;
  border-radius: 999px;
  background: white;
  font-size: 14px;
  font-weight: 500;
  color: #3A3F45;
  cursor: pointer;
  transition: all 0.2s;
}

.chip.active {
  background: #0085FF;
  color: white;
  border-color: #0085FF;
}

.btn-pesquisar {
  width: 100%;
  background: #0085FF;
  color: white;
  border: none;
  border-radius: 999px;
  padding: 16px;
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 32px;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-pesquisar:hover { background: #0073E6; }

.subtitulo {
  font-weight: 700;
  font-size: 20px;
  color: #15171A;
  margin: 0 0 16px 0;
}

/* Rotas Populares */
.rotas {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rota-card {
  display: flex;
  align-items: center;
  background: white;
  border: 1px solid #E4E7EB;
  border-radius: 16px;
  padding: 12px 16px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.rota-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.05); }

.rota-icon {
  width: 32px;
  height: 32px;
  background: #F2F4F7;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #6B7077;
}

.rota-text {
  font-weight: 600;
  font-size: 15px;
  color: #15171A;
  display: flex;
  align-items: center;
  gap: 8px;
}

.rota-text .arrow { color: #6B7077; }

/* Resultados de Pesquisa */
.resultados-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.resultados-count {
  font-size: 13px;
  color: #0085FF;
  font-weight: 600;
  background: #EAF4FF;
  padding: 6px 12px;
  border-radius: 999px;
}

.resultados {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.resultado-card {
  background: white;
  border: 1px solid #E4E7EB;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(16,24,40,0.04);
}

.resultado-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.time-col { display: flex; flex-direction: column; }
.time-col.right { text-align: right; }
.time-col .time { font-size: 22px; font-weight: 700; color: #15171A; }
.time-col .city { font-size: 13px; color: #6B7077; font-weight: 500; margin-top: 2px; }

.duration-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-grow: 1;
  padding: 0 20px;
}

.duration-col .duration { font-size: 12px; color: #6B7077; margin-bottom: 4px; }
.duration-line {
  display: flex;
  align-items: center;
  width: 100%;
}
.duration-line .dot {
  width: 6px; height: 6px; border-radius: 50%; border: 1.5px solid #0085FF; background: white;
}
.duration-line .line { flex-grow: 1; height: 2px; background: #EAF4FF; }
.duration-col .transport-type { font-size: 11px; color: #0085FF; margin-top: 4px; font-weight: 600; }

.resultado-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #EDEFF2;
}

.company {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #6B7077;
}

.price-action {
  display: flex;
  align-items: center;
  gap: 14px;
}

.price-action .price { font-size: 18px; font-weight: 700; color: #15171A; }
.buy-btn {
  background: #0085FF;
  color: white;
  border: none;
  border-radius: 999px;
  padding: 8px 18px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.buy-btn:hover { background: #0073E6; }

/* Animações */
.fade-in { animation: fadeIn 0.3s ease-in-out; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* NavBar Inferior Global */
.bottom-navbar {
  position: fixed; bottom: 0; left: 0; width: 100%; height: 63px;
  background: #FFFFFF; box-shadow: 0px -4px 20px rgba(0, 0, 0, 0.05);
  display: flex; justify-content: space-around; align-items: center;
  padding: 0 10px; box-sizing: border-box;
}

.nav-item {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  width: 86.5px; height: 51px; border-radius: 4px; color: #15171A;
  font-weight: 500; font-size: 10px; cursor: pointer; transition: all 0.2s;
}

.nav-item svg { width: 24px; height: 24px; margin-bottom: 4px; }
.nav-item.active { background: #0085FF; color: #FFFFFF; }
.nav-item.active svg { stroke: #FFFFFF; }
</style>