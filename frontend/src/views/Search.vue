<template>
  <div class="page">

    <h1 class="titulo">Pesquisa rotas</h1>

    <!-- Card de pesquisa -->
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

        <div class="inputs-col">

          <!-- Input Origem com dropdown -->
          <div class="input-wrapper">
            <input
              v-model="origemInput"
              type="text"
              placeholder="De onde?"
              class="campo-input"
              @input="onOrigemInput"
              @blur="fecharDropdownOrigem"
              autocomplete="off"
            />
            <ul v-if="sugestoesOrigem.length > 0" class="dropdown">
              <li
                v-for="s in sugestoesOrigem"
                :key="s.nome"
                class="dropdown-item"
                @mousedown.prevent="selecionarOrigem(s)"
              >
                <span class="dropdown-zona-badge">Z{{ s.zona }}</span>
                {{ s.nome }}
              </li>
            </ul>
          </div>

          <div class="sep"></div>

          <!-- Input Destino com dropdown -->
          <div class="input-wrapper">
            <input
              v-model="destinoInput"
              type="text"
              placeholder="Para onde?"
              class="campo-input"
              @input="onDestinoInput"
              @blur="fecharDropdownDestino"
              autocomplete="off"
            />
            <ul v-if="sugestoesDestino.length > 0" class="dropdown">
              <li
                v-for="s in sugestoesDestino"
                :key="s.nome"
                class="dropdown-item"
                @mousedown.prevent="selecionarDestino(s)"
              >
                <span class="dropdown-zona-badge">Z{{ s.zona }}</span>
                {{ s.nome }}
              </li>
            </ul>
          </div>

          <button class="btn-swap" @click="trocar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="16 3 21 8 16 13"></polyline>
              <line x1="21" y1="8" x2="9" y2="8"></line>
              <polyline points="8 21 3 16 8 11"></polyline>
              <line x1="3" y1="16" x2="15" y2="16"></line>
            </svg>
          </button>
        </div>

      </div>
    </div>

    <!-- Chips de data -->
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

    <!-- Erro de zonas -->
    <div v-if="erroZonas" class="erro-box">
      {{ erroZonas }}
    </div>

    <button class="btn-pesquisar" @click="handleSearch">Pesquisar</button>

    <!-- Estado inicial: rotas populares -->
    <div v-if="!hasSearched" class="rotas-section fade-in">
      <h3 class="subtitulo">Rotas Populares</h3>
      <div class="rotas">
        <div
          class="rota-card"
          v-for="rota in rotasPopulares"
          :key="rota.id"
          @click="preencherRota(rota)"
        >
          <div class="rota-icon">
            <!-- ícone autocarro -->
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="2" y="5" width="20" height="14" rx="2"/>
              <path d="M8 19v2M16 19v2M2 10h20"/>
              <circle cx="7" cy="16" r="1" fill="currentColor"/>
              <circle cx="17" cy="16" r="1" fill="currentColor"/>
            </svg>
          </div>
          <div class="rota-text">
            <div class="rota-paragens">
              <span>{{ rota.origem }}</span>
              <span class="arrow">→</span>
              <span>{{ rota.destino }}</span>
            </div>
            <div class="rota-meta">
              <span class="zona-pill">Z{{ rota.zonaOrigem }}</span>
              <span class="zona-sep">→</span>
              <span class="zona-pill">Z{{ rota.zonaDestino }}</span>
              <span class="rota-preco-hint">{{ formatarPreco(rota.preco) }}€</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Resultados de pesquisa -->
    <div v-else class="resultados-section fade-in">
      <div class="resultados-header">
        <h3 class="subtitulo">Resultados</h3>
        <span class="resultados-count">{{ resultadosPesquisa.length }} viagens</span>
      </div>

      <!-- Resumo de zonas -->
      <div class="zonas-info-bar">
        <div class="zona-tag">
          <span class="zona-pill">Z{{ zonaCalculo.zonaInicio }}</span>
          <span class="zonas-arrow">→</span>
          <span class="zona-pill">Z{{ zonaCalculo.zonaFim }}</span>
        </div>
        <div class="zonas-detalhe">
          {{ zonaCalculo.zonasAtravessadas.length }} zona{{ zonaCalculo.zonasAtravessadas.length > 1 ? 's' : '' }}
          · {{ formatarPreco(zonaCalculo.preco) }}€ por viagem
        </div>
      </div>

      <div class="resultados">
        <div class="resultado-card" v-for="viagem in resultadosPesquisa" :key="viagem.id">

          <div class="resultado-top">
            <div class="time-col">
              <span class="time">{{ viagem.horaPartida }}</span>
              <span class="city">{{ origemSelecionada?.nome }}</span>
            </div>

            <div class="duration-col">
              <span class="duration">{{ viagem.duracao }}</span>
              <div class="duration-line">
                <span class="dot"></span>
                <span class="line"></span>
                <span class="dot"></span>
              </div>
              <span class="transport-type">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="2" y="5" width="20" height="14" rx="2"/>
                  <path d="M2 10h20"/>
                  <circle cx="7" cy="16" r="1" fill="currentColor"/>
                  <circle cx="17" cy="16" r="1" fill="currentColor"/>
                </svg>
                Autocarro
              </span>
            </div>

            <div class="time-col right">
              <span class="time">{{ viagem.horaChegada }}</span>
              <span class="city">{{ destinoSelecionado?.nome }}</span>
            </div>
          </div>

          <div class="resultado-bottom">
            <div class="zonas-badges-row">
              <span
                v-for="z in zonaCalculo.zonasAtravessadas"
                :key="z"
                class="zona-badge-small"
              >Zona {{ z }}</span>
            </div>
            <div class="price-action">
              <span class="price">{{ formatarPreco(viagem.preco) }}€</span>
              <button class="buy-btn" @click="comprar(viagem)">Comprar</button>
            </div>
          </div>

        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()

// ─────────────────────────────────────────────
// BASE DE DADOS DE PARAGENS POR ZONA (MOCK)
// Quando o backend existir: GET /api/paragens
// ─────────────────────────────────────────────
const PRECO_POR_ZONA = 1.50

const PARAGENS = ref([])
const catalogoBilhete = ref([])

const userStatus = computed(() => authStore.user?.perfil || 'NORMAL')

onMounted(async () => {
  try {
    const res = await axios.get('/api/paragens')
    PARAGENS.value = res.data.map((p, index) => ({
      id: p.id,
      nome: p.nome,
      zona: (index % 2) + 1 // Intercalar entre zona 1 e zona 2 para testes
    }))
  } catch (err) {
    console.error('Erro ao carregar paragens', err)
  }

  try {
    const res = await axios.get('/api/catalogo/titulos')
    catalogoBilhete.value = res.data.bilhete || []
  } catch (err) {
    console.error('Erro ao carregar catalogo', err)
  }
})

// ─────────────────────────────────────────────
// CÁLCULO DE PREÇO POR ZONAS
// ─────────────────────────────────────────────
function calcularPreco(paragemInicio, paragemFim) {
  if (!paragemInicio || !paragemFim) return { preco: 0, zonaInicio: 1, zonaFim: 1, zonasAtravessadas: [1] }

  const zonaMin = Math.min(paragemInicio.zona, paragemFim.zona)
  const zonaMax = Math.max(paragemInicio.zona, paragemFim.zona)

  const zonasAtravessadas = []
  for (let z = zonaMin; z <= zonaMax; z++) {
    zonasAtravessadas.push(z)
  }

  const numToLetter = { 1: 'A', 2: 'B', 3: 'C', 4: 'D' }
  const targetZona = zonasAtravessadas.length === 1 ? `Zona ${numToLetter[zonasAtravessadas[0]] || 'A'}` : 'Rede completa'
  const precoObj = catalogoBilhete.value.find(x => x.perfil === userStatus.value && x.zona === targetZona)
  const preco = precoObj ? precoObj.preco : (zonasAtravessadas.length === 1 ? 1.00 : 1.50)

  return {
    preco,
    zonaInicio: paragemInicio.zona,
    zonaFim: paragemFim.zona,
    zonasAtravessadas,
    zonasIds: (precoObj && precoObj.zona_id) ? [precoObj.zona_id] : []
  }
}

function formatarPreco(valor) {
  return valor.toFixed(2).replace('.', ',')
}

// ─────────────────────────────────────────────
// ESTADO
// ─────────────────────────────────────────────
const origemInput    = ref('')
const destinoInput   = ref('')
const origemSelecionada  = ref(null)   // objecto { nome, zona }
const destinoSelecionado = ref(null)   // objecto { nome, zona }
const sugestoesOrigem  = ref([])
const sugestoesDestino = ref([])
const dataSelecionada  = ref(1)
const hasSearched      = ref(false)
const erroZonas        = ref('')
const resultadosPesquisa = ref([])
const zonaCalculo      = ref({ preco: 0, zonaInicio: 1, zonaFim: 1, zonasAtravessadas: [1] })

const datasDisponiveis = ref([
  { id: 1, label: 'Hoje' },
  { id: 2, label: 'Amanhã' },
  { id: 3, label: '15 Jun' },
  { id: 4, label: '16 Jun' },
])

// Rotas populares dinâmicas
const rotasPopulares = computed(() => {
  if (PARAGENS.value.length < 6) return []
  
  const pares = [
    { origem: PARAGENS.value[0].nome, destino: PARAGENS.value[2].nome }, // Ex: Zona 1 -> Zona 1
    { origem: PARAGENS.value[1].nome, destino: PARAGENS.value[3].nome }, // Ex: Zona 2 -> Zona 2
    { origem: PARAGENS.value[0].nome, destino: PARAGENS.value[1].nome }, // Ex: Zona 1 -> Zona 2
  ]
  
  return pares.map((r, i) => {
    const pInicio = PARAGENS.value.find(p => p.nome === r.origem)
    const pFim    = PARAGENS.value.find(p => p.nome === r.destino)
    const calc    = calcularPreco(pInicio, pFim)
    return {
      id: i + 1,
      origem: r.origem,
      destino: r.destino,
      zonaOrigem: pInicio ? pInicio.zona : 1,
      zonaDestino: pFim ? pFim.zona : 1,
      preco: calc.preco,
    }
  })
})

// ─────────────────────────────────────────────
// AUTOCOMPLETE
// ─────────────────────────────────────────────
function filtrarParagens(texto) {
  if (!texto || texto.length < 2) return []
  const q = texto.toLowerCase()
  return PARAGENS.value.filter(p => p.nome.toLowerCase().includes(q)).slice(0, 5)
}

function onOrigemInput() {
  erroZonas.value = ''
  origemSelecionada.value = null
  hasSearched.value = false
  sugestoesOrigem.value = filtrarParagens(origemInput.value)
}

function onDestinoInput() {
  erroZonas.value = ''
  destinoSelecionado.value = null
  hasSearched.value = false
  sugestoesDestino.value = filtrarParagens(destinoInput.value)
}

function selecionarOrigem(paragem) {
  origemSelecionada.value = paragem
  origemInput.value = paragem.nome
  sugestoesOrigem.value = []
}

function selecionarDestino(paragem) {
  destinoSelecionado.value = paragem
  destinoInput.value = paragem.nome
  sugestoesDestino.value = []
}

function fecharDropdownOrigem()  { setTimeout(() => { sugestoesOrigem.value  = [] }, 150) }
function fecharDropdownDestino() { setTimeout(() => { sugestoesDestino.value = [] }, 150) }

// ─────────────────────────────────────────────
// LÓGICA
// ─────────────────────────────────────────────
const trocar = () => {
  // Troca os objectos e os textos
  const tmpObj   = origemSelecionada.value
  const tmpInput = origemInput.value
  origemSelecionada.value  = destinoSelecionado.value
  origemInput.value        = destinoInput.value
  destinoSelecionado.value = tmpObj
  destinoInput.value       = tmpInput
  hasSearched.value = false
}

const preencherRota = (rota) => {
  const pOrigem  = PARAGENS.value.find(p => p.nome === rota.origem)
  const pDestino = PARAGENS.value.find(p => p.nome === rota.destino)
  origemSelecionada.value  = pOrigem
  destinoSelecionado.value = pDestino
  origemInput.value  = pOrigem.nome
  destinoInput.value = pDestino.nome
  handleSearch()
}

const handleSearch = () => {
  erroZonas.value = ''

  // Validação — tenta associar o texto digitado se o user não clicou na sugestão
  if (!origemSelecionada.value) {
    const match = PARAGENS.value.find(p => p.nome.toLowerCase() === origemInput.value.trim().toLowerCase())
    if (match) origemSelecionada.value = match
    else {
      erroZonas.value = 'Seleciona a origem na lista de sugestões.'
      return
    }
  }
  if (!destinoSelecionado.value) {
    const match = PARAGENS.value.find(p => p.nome.toLowerCase() === destinoInput.value.trim().toLowerCase())
    if (match) destinoSelecionado.value = match
    else {
      erroZonas.value = 'Seleciona o destino na lista de sugestões.'
      return
    }
  }
  if (origemSelecionada.value.nome === destinoSelecionado.value.nome) {
    erroZonas.value = 'A origem e o destino não podem ser iguais.'
    return
  }

  // Calcular preço com base nas zonas
  const calc = calcularPreco(origemSelecionada.value, destinoSelecionado.value)
  zonaCalculo.value = calc

  // Gerar horários (mock) — preço é REAL calculado por zonas
  // Quando ligar ao backend: GET /api/rotas?origem=...&destino=...&data=...
  resultadosPesquisa.value = [
    { id: 1, horaPartida: '08:45', horaChegada: calcularChegada('08:45', 55), duracao: '55 min', preco: calc.preco },
    { id: 2, horaPartida: '10:20', horaChegada: calcularChegada('10:20', 55), duracao: '55 min', preco: calc.preco },
    { id: 3, horaPartida: '13:00', horaChegada: calcularChegada('13:00', 55), duracao: '55 min', preco: calc.preco },
    { id: 4, horaPartida: '16:30', horaChegada: calcularChegada('16:30', 55), duracao: '55 min', preco: calc.preco },
  ]

  hasSearched.value = true
}

// Utilitário para calcular hora de chegada
function calcularChegada(horaPartida, minutos) {
  const [h, m] = horaPartida.split(':').map(Number)
  const total  = h * 60 + m + minutos
  const hh     = String(Math.floor(total / 60) % 24).padStart(2, '0')
  const mm     = String(total % 60).padStart(2, '0')
  return `${hh}:${mm}`
}

const comprar = (viagem) => {
  router.push({
    name: 'Buy',
    query: {
      origin:     origemSelecionada.value.nome,
      dest:       destinoSelecionado.value.nome,
      originId:   origemSelecionada.value.id,
      destId:     destinoSelecionado.value.id,
      price:      viagem.preco,
      zonaInicio: zonaCalculo.value.zonaInicio,
      zonaFim:    zonaCalculo.value.zonaFim,
      zonas:      zonaCalculo.value.zonasAtravessadas.join(','),
      zonasIds:   zonaCalculo.value.zonasIds.join(',')
    }
  })
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

/* ── Search Card ── */
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
  margin-top: 16px;
  flex-shrink: 0;
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
  height: 28px;
  background: #EDEFF2;
  margin: 4px 0;
}

.inputs-col {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* Wrapper para cada input (necessário para o dropdown) */
.input-wrapper {
  position: relative;
}

.campo-input {
  border: none;
  font-size: 16px;
  font-weight: 500;
  color: #15171A;
  padding: 12px 40px 12px 0;
  width: 100%;
  outline: none;
  background: transparent;
  box-sizing: border-box;
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

/* Dropdown de sugestões */
.dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: -16px;
  right: -56px;
  background: white;
  border: 1px solid #E4E7EB;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(16,24,40,0.12);
  list-style: none;
  margin: 0;
  padding: 6px 0;
  z-index: 100;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  font-size: 15px;
  font-weight: 500;
  color: #15171A;
  cursor: pointer;
  transition: background 0.15s;
}

.dropdown-item:hover { background: #F5F8FF; }

.dropdown-zona-badge {
  font-size: 11px;
  font-weight: 700;
  color: #0085FF;
  background: #EAF4FF;
  border-radius: 6px;
  padding: 2px 6px;
  flex-shrink: 0;
}

/* Botão swap */
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
  z-index: 1;
}
.btn-swap:hover { background: #E4E7EB; }

/* ── Chips de data ── */
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
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

/* ── Erro ── */
.erro-box {
  background: #FFF2F2;
  border: 1px solid #FFCDD2;
  border-radius: 12px;
  padding: 12px 16px;
  font-size: 14px;
  color: #D63A2E;
  font-weight: 500;
  margin-bottom: 16px;
}

/* ── Botão Pesquisar ── */
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

/* ── Subtítulo ── */
.subtitulo {
  font-weight: 700;
  font-size: 20px;
  color: #15171A;
  margin: 0 0 16px 0;
}

/* ── Rotas Populares ── */
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
  padding: 14px 16px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  gap: 12px;
}
.rota-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.07); }

.rota-icon {
  width: 36px;
  height: 36px;
  background: #EAF4FF;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0085FF;
  flex-shrink: 0;
}

.rota-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rota-paragens {
  font-weight: 600;
  font-size: 15px;
  color: #15171A;
  display: flex;
  align-items: center;
  gap: 6px;
}

.rota-paragens .arrow { color: #6B7077; font-size: 13px; }

.rota-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.zona-pill {
  font-size: 11px;
  font-weight: 700;
  color: #0085FF;
  background: #EAF4FF;
  border-radius: 6px;
  padding: 2px 7px;
}

.zona-sep { font-size: 11px; color: #9AA0A6; }

.rota-preco-hint {
  font-size: 12px;
  font-weight: 600;
  color: #6B7077;
  margin-left: 4px;
}

/* ── Resultados ── */
.resultados-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.resultados-count {
  font-size: 13px;
  color: #0085FF;
  font-weight: 600;
  background: #EAF4FF;
  padding: 6px 12px;
  border-radius: 999px;
}

/* Barra de resumo de zonas */
.zonas-info-bar {
  background: #F5F8FF;
  border: 1px solid #D5E8FF;
  border-radius: 12px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.zona-tag {
  display: flex;
  align-items: center;
  gap: 6px;
}

.zonas-arrow { font-size: 13px; color: #9AA0A6; }

.zonas-detalhe {
  font-size: 13px;
  color: #6B7077;
  font-weight: 500;
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

/* Topo do card */
.resultado-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.time-col { display: flex; flex-direction: column; }
.time-col.right { text-align: right; }
.time-col .time { font-size: 22px; font-weight: 700; color: #15171A; font-variant-numeric: tabular-nums; }
.time-col .city { font-size: 13px; color: #6B7077; font-weight: 500; margin-top: 2px; }

.duration-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-grow: 1;
  padding: 0 16px;
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

.transport-type {
  font-size: 11px;
  color: #0085FF;
  margin-top: 4px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Fundo do card */
.resultado-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #EDEFF2;
  gap: 12px;
}

.zonas-badges-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  flex: 1;
}

.zona-badge-small {
  font-size: 11px;
  font-weight: 600;
  color: #1F9D4D;
  background: #E7F6EC;
  border-radius: 6px;
  padding: 3px 8px;
  white-space: nowrap;
}

.price-action {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.price-action .price { font-size: 18px; font-weight: 700; color: #15171A; }

.buy-btn {
  background: #0085FF;
  color: white;
  border: none;
  border-radius: 999px;
  padding: 9px 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}
.buy-btn:hover { background: #0073E6; }

/* ── Animação ── */
.fade-in { animation: fadeIn 0.3s ease-in-out; }
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>