<template>
  <div class="buy-page">

    <div class="hero-card">
      <button class="back-btn" @click="router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
      </button>

      <div class="hero-badge">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <rect x="2" y="5" width="20" height="14" rx="2"/>
          <path d="M2 10h20"/>
          <circle cx="7" cy="16" r="1" fill="#fff"/>
          <circle cx="17" cy="16" r="1" fill="#fff"/>
        </svg>
        AUTOCARRO · BILHETE SIMPLES
      </div>

      <div class="hero-route">
        <div class="hero-city">
          <span class="hero-zone-pill">Z{{ zonaInicio }}</span>
          <span class="hero-city-name">{{ origin }}</span>
        </div>
        <div class="hero-arrow">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.7)" stroke-width="2" stroke-linecap="round">
            <line x1="5" y1="12" x2="19" y2="12"/>
            <polyline points="12 5 19 12 12 19"/>
          </svg>
        </div>
        <div class="hero-city right">
          <span class="hero-zone-pill">Z{{ zonaFim }}</span>
          <span class="hero-city-name">{{ dest }}</span>
        </div>
      </div>

      <div class="hero-meta">
        <span class="hero-meta-item">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.8)" stroke-width="2">
            <path d="M12 22s-8-4.5-8-11.8A8 8 0 0 1 12 2a8 8 0 0 1 8 8.2c0 7.3-8 11.8-8 11.8z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          {{ zonasLista.length }} zona{{ zonasLista.length > 1 ? 's' : '' }}
        </span>
        <span class="hero-meta-sep">·</span>
        <span class="hero-meta-item hero-price">{{ formattedPrice }}€</span>
      </div>

      <div class="hero-circle-1"></div>
      <div class="hero-circle-2"></div>
    </div>

    <div class="body-content">

      <div class="step-card filled">
        <div class="step-header">
          <div class="step-num done">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
          <div class="step-label-group">
            <span class="step-label">Escolha da Rota</span>
            <span class="step-sublabel">Preenchida automaticamente</span>
          </div>
          <button class="step-change-btn" @click="router.back()">Alterar</button>
        </div>

        <div class="route-summary">
          <div class="route-node">
            <div class="node-dot origem"></div>
            <span class="node-text">{{ origin }}</span>
            <span class="node-zone">Zona {{ zonaInicio }}</span>
          </div>
          <div class="route-connector">
            <div class="connector-line"></div>
            <div class="connector-icon">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2">
                <rect x="2" y="5" width="20" height="14" rx="2"/>
                <path d="M2 10h20"/>
                <circle cx="7" cy="16" r="1" fill="#0085FF"/>
                <circle cx="17" cy="16" r="1" fill="#0085FF"/>
              </svg>
            </div>
            <div class="connector-line"></div>
          </div>
          <div class="route-node">
            <div class="node-dot destino"></div>
            <span class="node-text">{{ dest }}</span>
            <span class="node-zone">Zona {{ zonaFim }}</span>
          </div>
        </div>
      </div>

      <div class="info-banner">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 8v4M12 16h.01"/>
        </svg>
        <p class="info-text">
          O preço final é calculado com base nas zonas atravessadas e no teu perfil de utilizador (Normal, Estudante, Sénior).
        </p>
      </div>

      <div class="step-card">
        <div class="step-header">
          <div class="step-num active">2</div>
          <div class="step-label-group">
            <span class="step-label">Detalhes da Viagem</span>
            <span class="step-sublabel">Zonas atravessadas</span>
          </div>
        </div>

        <div class="zones-breakdown">
          <div class="zone-item" v-for="z in zonasLista" :key="z">
            <div class="zone-item-left">
              <div class="zone-icon-small">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2">
                  <path d="M12 22s-8-4.5-8-11.8A8 8 0 0 1 12 2a8 8 0 0 1 8 8.2c0 7.3-8 11.8-8 11.8z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
              </div>
              <span class="zone-name">Zona {{ z }}</span>
            </div>
            <!-- Custo por zona removido -->
          </div>

          <div class="zones-total-divider"></div>

          <div class="zones-total">
            <span class="zones-total-label">Total</span>
            <span class="zones-total-value">{{ formattedPrice }}€</span>
          </div>
        </div>
      </div>

      <div class="step-card">
        <div class="step-header">
          <div class="step-num active">3</div>
          <div class="step-label-group">
            <span class="step-label">Método de Pagamento</span>
            <span class="step-sublabel">Escolhe como queres pagar</span>
          </div>
        </div>

        <div class="payment-list">

          <label class="pay-option" :class="{ selected: selectedMethod === 'wallet' }">
            <div class="pay-icon blue-bg">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2">
                <rect x="2" y="6" width="20" height="12" rx="2"/>
                <circle cx="12" cy="12" r="2"/>
                <path d="M6 12h.01M18 12h.01"/>
              </svg>
            </div>
            <div class="pay-info">
              <span class="pay-name">Saldo da Carteira</span>
              <span class="pay-desc" :class="{ 'text-red': userBalance < price }">
                Disponível: {{ userBalance.toFixed(2).replace('.', ',') }}€
              </span>
            </div>
            <div class="radio-outer" :class="{ active: selectedMethod === 'wallet' }">
              <div class="radio-inner" v-if="selectedMethod === 'wallet'"></div>
            </div>
            <input type="radio" value="wallet" v-model="selectedMethod" class="sr-only"/>
          </label>

          <label class="pay-option" :class="{ selected: selectedMethod === 'mbway' }">
            <div class="pay-icon dark-bg">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
                <rect x="5" y="2" width="14" height="20" rx="2"/>
                <line x1="12" y1="18" x2="12.01" y2="18"/>
              </svg>
            </div>
            <div class="pay-info">
              <span class="pay-name">MB WAY</span>
              <span class="pay-desc">Pagamento via telemóvel</span>
            </div>
            <div class="radio-outer" :class="{ active: selectedMethod === 'mbway' }">
              <div class="radio-inner" v-if="selectedMethod === 'mbway'"></div>
            </div>
            <input type="radio" value="mbway" v-model="selectedMethod" class="sr-only"/>
          </label>

          <label class="pay-option" :class="{ selected: selectedMethod === 'card' }">
            <div class="pay-icon dark-bg">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
                <rect x="1" y="4" width="22" height="16" rx="2"/>
                <line x1="1" y1="10" x2="23" y2="10"/>
              </svg>
            </div>
            <div class="pay-info">
              <span class="pay-name">Cartão de Crédito / Débito</span>
              <span class="pay-desc">Visa, Mastercard</span>
            </div>
            <div class="radio-outer" :class="{ active: selectedMethod === 'card' }">
              <div class="radio-inner" v-if="selectedMethod === 'card'"></div>
            </div>
            <input type="radio" value="card" v-model="selectedMethod" class="sr-only"/>
          </label>

        </div>
      </div>

    </div>

    <div class="bottom-action">
      <button class="btn-confirmar" @click="showConfirmModal = true" :disabled="isProcessing">
        Pagar {{ formattedPrice }}€
      </button>
    </div>

    <ConfirmModal 
      :show="showConfirmModal"
      summaryTitle="Bilhete Simples"
      :summaryPrice="formattedPrice + '€'"
      :isProcessing="isProcessing"
      @confirm="confirmarCompra"
      @cancel="showConfirmModal = false"
    />

    <InsufficientBalanceModal 
      :show="paymentStatus === 'error'"
      @close="fecharModal"
      @charge="irParaCarregarCarteira"
    />

    <transition name="modal-fade">
      <div v-if="paymentStatus === 'success'" class="modal-overlay">
        <div class="modal-card">
          <div class="modal-top success-bg">
            <div class="icon-overlap">
              <div class="circle-icon green-circle">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
              </div>
            </div>
          </div>
          
          <div class="modal-body">
            <h2>Pagamento Concluído!</h2>
            <p>A tua compra foi processada com sucesso e o teu bilhete já está disponível.</p>
            
            <div class="receipt-box">
              <div class="receipt-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"></path>
                  <line x1="4" y1="22" x2="4" y2="15"></line>
                </svg>
              </div>
              <div class="receipt-info">
                <h4>Bilhete Simples</h4>
                <span>Total: {{ formattedPrice }}€</span>
              </div>
            </div>

            <button class="btn-primary-modal" @click="irParaBilhetes">Ver os meus bilhetes</button>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import axios from 'axios'
import ConfirmModal from '../components/ConfirmModal.vue'
import InsufficientBalanceModal from '../components/InsufficientBalanceModal.vue'

const route    = useRoute()
const router   = useRouter()
const authStore = useAuthStore()

// ── Dados vindos do Search via query params ──
const origin = computed(() => route.query.origin || 'Origem')
const dest   = computed(() => route.query.dest   || 'Destino')
const price  = computed(() => Number(route.query.price || 0))

const zonaInicio = computed(() => Number(route.query.zonaInicio || 1))
const zonaFim    = computed(() => Number(route.query.zonaFim    || 1))

// Zonas: "1,2,3" → [1, 2, 3]
const zonasLista = computed(() => {
  const raw = route.query.zonas || String(zonaInicio.value)
  return raw.split(',').map(Number)
})

const formattedPrice = computed(() => price.value.toFixed(2).replace('.', ','))

// ── Saldo do store ──
const userBalance = computed(() => authStore.userSaldo)

// ── Estado local ──
const selectedMethod = ref('wallet')
const isProcessing   = ref(false)
const paymentStatus  = ref(null) // null | 'success' | 'error'
const showConfirmModal = ref(false)

// ── Confirmar compra ──
const confirmarCompra = async () => {
  if (selectedMethod.value === 'wallet' && userBalance.value < price.value) {
    paymentStatus.value = 'error'
    showConfirmModal.value = false
    return
  }

  isProcessing.value = true

  try {
    const zIds = route.query.zonasIds ? route.query.zonasIds.split(',').filter(id => id.trim().length > 0) : []

    await axios.post('/api/titulos', {
      tipo: 'BILHETE',
      zonasIds: zIds
    })
    
    // Atualiza o saldo do utilizador localmente ou pedindo novo perfil
    await authStore.fetchProfile()
    
    paymentStatus.value = 'success'
  } catch (err) {
    console.error('Erro na compra', err)
    paymentStatus.value = 'error' // Or you could show a specific error if you want
  } finally {
    isProcessing.value = false
    showConfirmModal.value = false
  }
}

// ── Ações dos Modais ──
const fecharModal = () => {
  paymentStatus.value = null
}

const irParaBilhetes = () => {
  paymentStatus.value = null
  router.push('/trips')
}

// 2. FUNÇÃO PARA CARREGAR A CARTEIRA
const irParaCarregarCarteira = () => {
  paymentStatus.value = null
  router.push('/ChargeWallet')
}
</script>

<style scoped>
/* ── Base ── */
.buy-page {
  background: #F4F5F7;
  min-height: 100vh;
  font-family: 'Roboto', sans-serif;
  padding-bottom: 130px;
  box-sizing: border-box;
}

/* ── Hero Card Verde ── */
.hero-card {
  position: relative;
  background: linear-gradient(135deg, #1F9D4D 0%, #23A857 58%, #34C46E 100%);
  padding: 52px 28px 36px 28px;
  overflow: hidden;
}

.back-btn {
  position: absolute;
  top: 16px;
  left: 20px;
  width: 38px;
  height: 38px;
  background: rgba(255,255,255,0.18);
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 2;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255,255,255,0.18);
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.5px;
  margin-bottom: 20px;
}

.hero-route {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 16px;
}

.hero-city {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.hero-city.right {
  align-items: flex-end;
}

.hero-zone-pill {
  display: inline-block;
  background: rgba(255,255,255,0.25);
  border-radius: 6px;
  padding: 3px 8px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  width: fit-content;
}

.hero-city-name {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  line-height: 1.1;
}

.hero-arrow {
  flex-shrink: 0;
}

.hero-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: rgba(255,255,255,0.85);
  font-weight: 500;
}

.hero-price {
  font-size: 15px;
  font-weight: 700;
  color: #fff;
}

.hero-meta-sep {
  color: rgba(255,255,255,0.5);
}

/* Decoração */
.hero-circle-1 {
  position: absolute;
  top: -60px;
  right: -60px;
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: rgba(255,255,255,0.08);
  pointer-events: none;
}

.hero-circle-2 {
  position: absolute;
  bottom: -80px;
  right: -30px;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: rgba(255,255,255,0.07);
  pointer-events: none;
}

/* ── Corpo ── */
.body-content {
  padding: 20px 16px 0 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* ── Step Card ── */
.step-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(16,24,40,0.04), 0 4px 14px rgba(16,24,40,0.06);
}

.step-card.filled {
  border: 1.5px solid #E7F6EC;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.step-num {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.step-num.done {
  background: #1F9D4D;
}

.step-num.active {
  background: #0085FF;
  color: #fff;
}

.step-label-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.step-label {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.step-sublabel {
  font-size: 12px;
  color: #9AA0A6;
}

.step-change-btn {
  font-size: 13px;
  font-weight: 600;
  color: #0085FF;
  background: #EAF4FF;
  border: none;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
  white-space: nowrap;
}

/* ── Route Summary ── */
.route-summary {
  display: flex;
  align-items: center;
  gap: 0;
  background: #F7F9FC;
  border-radius: 14px;
  padding: 16px;
  gap: 8px;
}

.route-node {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  flex: 1;
}

.node-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.node-dot.origem {
  background: #0085FF;
}

.node-dot.destino {
  background: #D63A2E;
}

.node-text {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.node-zone {
  font-size: 12px;
  color: #6B7077;
  font-weight: 500;
}

.route-connector {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 0 8px;
}

.connector-line {
  width: 1px;
  height: 12px;
  background: #EDEFF2;
}

.connector-icon {
  width: 28px;
  height: 28px;
  background: #EAF4FF;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ── Info Banner ── */
.info-banner {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #EAF4FF;
  border: 1px solid #C8E2FF;
  border-radius: 14px;
  padding: 14px 16px;
}

.info-text {
  font-size: 13px;
  color: #3A4658;
  line-height: 1.5;
  margin: 0;
}

.info-text strong {
  color: #0073E6;
}

/* ── Zones Breakdown ── */
.zones-breakdown {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.zone-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.zone-item-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.zone-icon-small {
  width: 28px;
  height: 28px;
  background: #EAF4FF;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.zone-name {
  font-size: 14px;
  font-weight: 500;
  color: #15171A;
}

.zone-cost {
  font-size: 14px;
  font-weight: 600;
  color: #3A3F45;
}

.zones-total-divider {
  height: 1px;
  background: #EDEFF2;
  margin: 4px 0;
}

.zones-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.zones-total-label {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.zones-total-value {
  font-size: 22px;
  font-weight: 700;
  color: #0085FF;
}

/* ── Payment Options ── */
.payment-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pay-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border: 1.5px solid #E4E7EB;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-option.selected {
  border-color: #0085FF;
  background: #F5FAFF;
}

.pay-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pay-icon.blue-bg { background: rgba(0,133,255,0.1); }
.pay-icon.dark-bg { background: #15171A; }

.pay-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.pay-name {
  font-size: 15px;
  font-weight: 600;
  color: #15171A;
}

.pay-desc {
  font-size: 12px;
  color: #6B7077;
}

.text-red { color: #D63A2E !important; }

.radio-outer {
  width: 20px;
  height: 20px;
  border: 2px solid #D0D5DD;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: border-color 0.2s;
}

.radio-outer.active {
  border-color: #0085FF;
}

.radio-inner {
  width: 10px;
  height: 10px;
  background: #0085FF;
  border-radius: 50%;
}

.sr-only { display: none; }

/* ── Botão fixo ── */
.bottom-action {
  position: fixed;
  bottom: 63px;
  left: 0;
  width: 100%;
  padding: 14px 16px 18px 16px;
  background: linear-gradient(to top, rgba(244,245,247,1) 80%, rgba(244,245,247,0));
  z-index: 50;
  box-sizing: border-box;
}

.btn-confirmar {
  width: 100%;
  height: 56px;
  background: #0085FF;
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(0,133,255,0.3);
  transition: background 0.2s, transform 0.1s;
  letter-spacing: 0.2px;
}

.btn-confirmar:active:not(:disabled) {
  transform: scale(0.98);
}

.btn-confirmar:hover:not(:disabled) {
  background: #0073E6;
}

.btn-confirmar:disabled {
  background: #A3CFFF;
  cursor: not-allowed;
  box-shadow: none;
}

/* =========================================
   MODAIS DE SUCESSO (Originais simplificados)
   ========================================= */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(11, 16, 24, 0.5);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

.modal-card {
  background: #FFFFFF;
  border-radius: 24px;
  width: 100%;
  max-width: 340px;
  overflow: hidden;
  box-shadow: 0px 24px 60px rgba(0, 0, 0, 0.28);
  animation: popUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes popUp {
  0% { transform: scale(0.95) translateY(20px); opacity: 0; }
  100% { transform: scale(1) translateY(0); opacity: 1; }
}

.modal-top {
  height: 160px;
  position: relative;
  display: flex;
  justify-content: center;
}

.success-bg { background: #E7F6EC; }

.icon-overlap {
  position: absolute;
  bottom: -42px; 
  background: #FFFFFF;
  width: 84px;
  height: 84px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.circle-icon {
  width: 62px;
  height: 62px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.green-circle { background: #1F9D4D; }

.modal-body {
  padding: 60px 24px 24px 24px;
  text-align: center;
}

.modal-body h2 {
  font-size: 22px;
  font-weight: 700;
  color: #15171A;
  margin: 0 0 8px 0;
}

.modal-body p {
  font-size: 14px;
  color: #6B7077;
  line-height: 1.5;
  margin: 0 0 24px 0;
}

.receipt-box {
  background: #F7F8FA;
  border-radius: 14px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
  text-align: left;
}

.receipt-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: #EAF4FF;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.receipt-info h4 {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
  margin: 0 0 2px 0;
}

.receipt-info span {
  font-size: 13px;
  color: #6B7077;
}

.btn-primary-modal {
  width: 100%;
  background: #0085FF;
  color: #FFFFFF;
  border: none;
  border-radius: 27px;
  height: 54px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary-modal:active {
  transform: scale(0.96);
  background: #0073E6;
}

.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.3s ease;
}
.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
}
</style>