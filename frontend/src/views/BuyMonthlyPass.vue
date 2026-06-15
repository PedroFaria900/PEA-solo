<template>
  <div class="buy-page">

    <div class="hero-card blue-hero">
      <button class="back-btn" @click="router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
      </button>

      <div class="hero-badge">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2">
          <rect x="3" y="4" width="18" height="16" rx="2"/>
          <line x1="3" y1="10" x2="21" y2="10"/>
        </svg>
        AUTOCARRO · PASSE MENSAL
      </div>

      <div class="hero-pass-info">
        <h2 class="hero-title">Passe Mensal</h2>
        <p class="hero-subtitle">Viagens ilimitadas durante 30 dias nas zonas escolhidas</p>
      </div>

      <div class="hero-meta">
        <span class="hero-meta-item">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.8)" stroke-width="2">
            <path d="M12 22s-8-4.5-8-11.8A8 8 0 0 1 12 2a8 8 0 0 1 8 8.2c0 7.3-8 11.8-8 11.8z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          {{ selectedZones.length }} zona{{ selectedZones.length > 1 ? 's' : '' }}
        </span>
        <span class="hero-meta-sep">·</span>
        <span class="hero-meta-item">
          <span v-if="discount > 0" class="hero-price-strikethrough">{{ formattedBasePrice }}€</span>
          <span class="hero-price">{{ formattedFinalPrice }}€</span>
        </span>
      </div>

      <div class="hero-circle-1"></div>
      <div class="hero-circle-2"></div>
    </div>

    <div class="body-content">

      <div class="status-card">
        <div class="status-left">
          <div class="status-icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
              <path d="M6 12v5c3 3 9 3 12 0v-5"/>
            </svg>
          </div>
          <div class="status-text">
            <span class="status-label">Estatuto Atual</span>
            <span class="status-value">{{ userStatus }}</span>
          </div>
        </div>
        <div class="status-right" v-if="discount > 0">
          <div class="discount-badge">-{{ discount * 100 }}%</div>
        </div>
      </div>

      <div class="step-card filled">
        <div class="step-header">
          <div class="step-num done">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"/>
            </svg>
          </div>
          <div class="step-label-group">
            <span class="step-label">Validade do Passe</span>
            <span class="step-sublabel">Ativação imediata</span>
          </div>
        </div>

        <div class="validity-summary">
          <div class="validity-node">
            <span class="validity-title">Início</span>
            <span class="validity-date">Hoje ({{ dataInicio }})</span>
          </div>
          <div class="validity-arrow">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round">
              <line x1="5" y1="12" x2="19" y2="12"/>
              <polyline points="12 5 19 12 12 19"/>
            </svg>
          </div>
          <div class="validity-node right">
            <span class="validity-title">Fim</span>
            <span class="validity-date">{{ dataFim }}</span>
          </div>
        </div>
      </div>

      <div class="step-card">
        <div class="step-header">
          <div class="step-num active">2</div>
          <div class="step-label-group">
            <span class="step-label">Zonas Abrangidas</span>
            <span class="step-sublabel">Escolhe as zonas para o teu passe</span>
          </div>
        </div>

        <div class="zone-list">
          <label class="zone-option" v-for="zona in zonasDisponiveis" :key="zona" :class="{ selected: selectedZones.includes(zona) }">
            <div class="zone-info">
              <span class="zone-name">{{ zona }}</span>
              <span class="zone-desc">Acesso ilimitado na {{ zona }}</span>
            </div>
            <div class="checkbox-outer" :class="{ active: selectedZones.includes(zona) }">
              <svg v-if="selectedZones.includes(zona)" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </div>
            <input type="checkbox" :value="zona" v-model="selectedZones" class="sr-only"/>
          </label>
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
              <span class="pay-desc" :class="{ 'text-red': userBalance < finalPrice }">
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
        </div>
      </div>

      <div class="step-card summary-card">
        <div class="summary-header">
          <div class="summary-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
              <line x1="16" y1="13" x2="8" y2="13"></line>
              <line x1="16" y1="17" x2="8" y2="17"></line>
              <polyline points="10 9 9 9 8 9"></polyline>
            </svg>
          </div>
          <h3 class="summary-title">Resumo da Compra</h3>
        </div>

        <div class="summary-content">
          <div class="summary-row">
            <span class="summary-label">Tipo de Passe</span>
            <span class="summary-value">Mensal (30 Dias)</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Estatuto</span>
            <span class="summary-value">{{ userStatus }} ({{ discount * 100 }}%)</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Zonas</span>
            <span class="summary-value">{{ selectedZones.length > 0 ? selectedZones.join(', ') : '-' }}</span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-row">
            <span class="summary-label">Preço Original</span>
            <span class="summary-value">{{ formattedBasePrice }}€</span>
          </div>
          <div class="summary-row text-green" v-if="discount > 0">
            <span class="summary-label">Desconto</span>
            <span class="summary-value">-{{ formattedDiscountValue }}€</span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-row final-price">
            <span class="summary-label">Preço Final</span>
            <span class="summary-value-large">{{ formattedFinalPrice }}€</span>
          </div>
        </div>
      </div>

    </div>

    <div class="bottom-action">
      <button class="btn-confirmar blue-btn" @click="showConfirmModal = true" :disabled="selectedZones.length === 0 || isProcessing">
        Pagar {{ formattedFinalPrice }}€
      </button>
    </div>

    <ConfirmModal 
      :show="showConfirmModal"
      :summaryTitle="'Passe Mensal (Zonas ' + selectedZones.join(', ') + ')'"
      :summaryPrice="formattedFinalPrice + '€'"
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
            <p>O teu Passe Mensal foi ativado com sucesso e já podes viajar.</p>
            
            <div class="receipt-box">
              <div class="receipt-icon blue-bg">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="16" rx="2"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
              <div class="receipt-info">
                <h4>Passe Mensal (Zonas {{ selectedZones.join(', ') }})</h4>
                <span>Total pago: {{ formattedFinalPrice }}€</span>
              </div>
            </div>

            <button class="btn-primary-modal blue-btn" @click="irParaBilhetes">Ver o meu passe</button>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import axios from 'axios'
import ConfirmModal from '../components/ConfirmModal.vue'
import InsufficientBalanceModal from '../components/InsufficientBalanceModal.vue'

const router = useRouter()
const authStore = useAuthStore()

// ── Estado ──
const selectedZones = ref(['Zona A']) 
const selectedMethod = ref('wallet')
const isProcessing = ref(false)
const paymentStatus = ref(null)
const showConfirmModal = ref(false)

// ── Lógica do Estatuto e Preços ──
const userBalance = computed(() => authStore.userSaldo || 0)

// Estatuto real do utilizador
const userStatus = computed(() => authStore.user?.perfil || 'NORMAL')

const catalogoPasse = ref([])

const zonasDisponiveis = computed(() => {
  const z = new Set()
  catalogoPasse.value.forEach(t => {
    if (t.zona && t.zona !== 'Rede completa') z.add(t.zona)
  })
  return Array.from(z).sort()
})

onMounted(async () => {
  try {
    const res = await axios.get('/api/catalogo/titulos')
    catalogoPasse.value = res.data.passe || []
  } catch (err) {
    console.error('Erro ao carregar catalogo', err)
  }
})

// Mapeamento simplificado: 1 zona selecionada -> A própria zona, >1 zona -> 'Rede completa'
const targetZona = computed(() => {
  if (selectedZones.value.length === 0) return ''
  if (selectedZones.value.length === 1) return selectedZones.value[0]
  return 'Rede completa'
})

const basePrice = computed(() => {
  const t = catalogoPasse.value.find(x => x.perfil === 'NORMAL' && x.zona === targetZona.value && x.periodo === 'MENSAL')
  return t ? t.preco : 0
})

const finalPrice = computed(() => {
  const t = catalogoPasse.value.find(x => x.perfil === userStatus.value && x.zona === targetZona.value && x.periodo === 'MENSAL')
  return t ? t.preco : 0
})

const discountValue = computed(() => Math.max(0, basePrice.value - finalPrice.value))
const discount = computed(() => basePrice.value > 0 ? discountValue.value / basePrice.value : 0)

const formattedBasePrice = computed(() => basePrice.value.toFixed(2).replace('.', ','))
const formattedDiscountValue = computed(() => discountValue.value.toFixed(2).replace('.', ','))
const formattedFinalPrice = computed(() => finalPrice.value.toFixed(2).replace('.', ','))

// ── Lógica de Datas (Hoje até Hoje + 30 Dias) ──
const today = new Date()
const nextMonth = new Date(today)
nextMonth.setDate(today.getDate() + 30)

const dateOptions = { day: 'numeric', month: 'short', year: 'numeric' }
const dataInicio = new Intl.DateTimeFormat('pt-PT', dateOptions).format(today)
const dataFim = new Intl.DateTimeFormat('pt-PT', dateOptions).format(nextMonth)

// ── Confirmar compra ──
const confirmarCompra = async () => {
  if (selectedMethod.value === 'wallet' && userBalance.value < finalPrice.value) {
    paymentStatus.value = 'error'
    showConfirmModal.value = false
    return
  }

  isProcessing.value = true

  try {
    const ids = targetZona.value === 'Rede completa' ? [] : selectedZones.value.map(zNome => {
      const t = catalogoPasse.value.find(x => x.zona === zNome)
      return t ? t.zona_id : null
    }).filter(id => id)

    await axios.post('/api/titulos', {
      tipo: 'PASSE',
      periodo: 'MENSAL',
      zonasIds: ids
    })
    
    await authStore.fetchProfile()
    
    paymentStatus.value = 'success'
  } catch (err) {
    console.error('Erro na compra', err)
    paymentStatus.value = 'error' 
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

const irParaCarregarCarteira = () => {
  paymentStatus.value = null
  const missingAmount = Math.max(0, finalPrice.value - userBalance.value)
  if (missingAmount > 0) {
    router.push(`/wallet/charge?amount=${missingAmount.toFixed(2)}`)
  } else {
    router.push('/wallet/charge')
  }
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

/* ── Hero Card Azul ── */
.hero-card {
  position: relative;
  padding: 52px 28px 36px 28px;
  overflow: hidden;
}

.blue-hero {
  background: linear-gradient(135deg, #0085FF 0%, #0070D6 100%);
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
  margin-bottom: 16px;
}

.hero-pass-info {
  margin-bottom: 16px;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 4px 0;
}

.hero-subtitle {
  font-size: 14px;
  color: rgba(255,255,255,0.85);
  margin: 0;
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
  color: rgba(255,255,255,0.9);
  font-weight: 500;
}

.hero-price {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
}

.hero-price-strikethrough {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  text-decoration: line-through;
  margin-right: 6px;
  font-weight: 500;
}

.hero-meta-sep {
  color: rgba(255,255,255,0.5);
}

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

/* ── Status Card (Estatuto & Desconto) ── */
.status-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FFFFFF;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 1px 2px rgba(16,24,40,0.04), 0 4px 14px rgba(16,24,40,0.06);
}

.status-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-icon {
  width: 40px;
  height: 40px;
  background: #EAF4FF;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.status-text {
  display: flex;
  flex-direction: column;
}

.status-label {
  font-size: 12px;
  color: #6B7077;
}

.status-value {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.discount-badge {
  background: #E7F6EC;
  color: #1F9D4D;
  font-size: 13px;
  font-weight: 700;
  padding: 6px 12px;
  border-radius: 20px;
}

/* ── Step Card ── */
.step-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(16,24,40,0.04), 0 4px 14px rgba(16,24,40,0.06);
}

.step-card.filled {
  border: 1.5px solid #EAF4FF;
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
  background: #0085FF;
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

/* ── Resumo Validade ── */
.validity-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #F7F9FC;
  border-radius: 14px;
  padding: 16px;
}

.validity-node {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.validity-node.right {
  text-align: right;
}

.validity-title {
  font-size: 12px;
  color: #6B7077;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.validity-date {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.validity-arrow {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #EAF4FF;
  border-radius: 50%;
}

/* ── Seleção de Zonas ── */
.zone-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.zone-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
  border: 1.5px solid #E4E7EB;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.zone-option.selected {
  border-color: #0085FF;
  background: #F5FAFF;
}

.zone-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.zone-name {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.zone-desc {
  font-size: 13px;
  color: #6B7077;
}

.checkbox-outer {
  width: 22px;
  height: 22px;
  border: 2px solid #D0D5DD;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.checkbox-outer.active {
  background: #0085FF;
  border-color: #0085FF;
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

.text-red { color: #EF4444 !important; }

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

/* ── Summary Card (Resumo da Compra) ── */
.summary-card {
  margin-bottom: 24px;
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.summary-icon {
  width: 32px;
  height: 32px;
  background: #EAF4FF;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-title {
  font-size: 16px;
  font-weight: 700;
  color: #15171A;
  margin: 0;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  font-size: 13px;
  color: #6B7077;
}

.summary-value {
  font-size: 14px;
  font-weight: 600;
  color: #15171A;
}

.summary-divider {
  height: 1px;
  background: #E4E7EB;
  margin: 4px 0;
}

.text-green .summary-label, 
.text-green .summary-value {
  color: #1F9D4D;
}

.final-price {
  margin-top: 4px;
}

.final-price .summary-label {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.summary-value-large {
  font-size: 18px;
  font-weight: 800;
  color: #0085FF;
}

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
  color: #fff;
  border: none;
  border-radius: 999px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}

.btn-confirmar.blue-btn {
  background: #0085FF;
  box-shadow: 0 4px 14px rgba(0,133,255,0.3);
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
   MODAL DE SUCESSO (Apenas este, já que erro está no componente)
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