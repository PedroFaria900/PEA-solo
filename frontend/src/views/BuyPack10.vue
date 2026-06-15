<template>
  <div class="buy-page">

    <div class="hero-card pack-hero">
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
        </svg>
        AUTOCARRO · PACK DE BILHETES
      </div>

      <div class="hero-pass-info">
        <h2 class="hero-title">Pack 10 Bilhetes</h2>
        <p class="hero-subtitle">Válido para as zonas selecionadas</p>
      </div>

      <div class="hero-meta">
        <span class="hero-meta-item">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="rgba(255,255,255,0.8)" stroke-width="2">
            <path d="M12 22s-8-4.5-8-11.8A8 8 0 0 1 12 2a8 8 0 0 1 8 8.2c0 7.3-8 11.8-8 11.8z"/>
            <circle cx="12" cy="10" r="3"/>
          </svg>
          {{ selectedZone }}
        </span>
        <span class="hero-meta-sep">·</span>
        <span class="hero-meta-item">
          <span class="hero-price-strikethrough">{{ formattedBasePrice }}€</span>
          <span class="hero-price">{{ formattedFinalPrice }}€</span>
        </span>
      </div>

      <div class="hero-circle-1"></div>
      <div class="hero-circle-2"></div>
    </div>

    <div class="body-content">

      <div class="step-card">
        <div class="step-header">
          <div class="step-num active">1</div>
          <div class="step-label-group">
            <span class="step-label">Zonas Abrangidas</span>
            <span class="step-sublabel">Escolhe as zonas para os teus 10 bilhetes</span>
          </div>
        </div>

        <div class="zone-grid">
          <div class="zone-card" v-for="zona in zonasDisponiveis" :key="zona" :class="{ active: selectedZone === zona }" @click="selectedZone = zona">
            <div class="z-badge" :class="{ active: selectedZone === zona }">{{ zona.slice(-1) }}</div>
            <div class="z-title">{{ zona }}</div>
          </div>
        </div>
      </div>

      <div class="step-card">
        <div class="step-header">
          <div class="step-num active">2</div>
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
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#7A3FF2" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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
            <span class="summary-label">Tipo de Pack</span>
            <span class="summary-value">10 Bilhetes</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Zonas Abrangidas</span>
            <span class="summary-value">{{ selectedZone }}</span>
          </div>
          
          <div class="summary-divider"></div>
          
          <div class="summary-row">
            <span class="summary-label">Preço Base (10x)</span>
            <span class="summary-value">{{ formattedBasePrice }}€</span>
          </div>
          <div class="summary-row text-green">
            <span class="summary-label">Desconto de Pack (10%)</span>
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
      <button class="btn-confirmar blue-btn" @click="handlePagarClick" :disabled="isProcessing || !selectedZone">
        {{ isProcessing ? 'A processar...' : `Pagar ${formattedFinalPrice}€` }}
      </button>
    </div>

    <ConfirmModal 
      :show="showConfirmModal"
      :summaryTitle="'Pack 10 Bilhetes (' + selectedZone + ')'"
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

    <!-- Pass Warning Modal -->
    <transition name="modal-fade">
      <div v-if="showPassWarningModal" class="modal-overlay">
        <div class="modal-card">
          <div class="modal-top warning-bg" style="background: linear-gradient(135deg, #FFF3E0 0%, #FFE0B2 100%); padding-top: 44px; padding-bottom: 24px; text-align: center;">
            <div class="icon-overlap" style="top: -28px;">
              <div class="circle-icon" style="background: #F5A623; box-shadow: 0px 8px 24px rgba(245, 166, 35, 0.3);">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                  <line x1="12" y1="9" x2="12" y2="13"/>
                  <line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
              </div>
            </div>
            <h2 style="color: #15171A; font-size: 20px; font-weight: 700; margin: 0 0 8px 0; letter-spacing: -0.5px;">Passe equivalente a este pack</h2>
            <p style="color: #6B7077; font-size: 14px; line-height: 1.5; margin: 0; padding: 0 20px;">
              Já tens um passe ativo para {{ activePass?.areaGeografica || 'esta zona' }}.<br>Queres mesmo continuar com a compra?
            </p>
          </div>
          
          <div class="modal-body" style="padding-top: 24px;">
            <div class="receipt-box" style="margin-top: 0; margin-bottom: 24px; border: 1px solid #FFE0B2;">
              <div class="receipt-icon" style="background: #FFF3E0;">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#F5A623" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
              <div class="receipt-info">
                <h4 style="color: #15171A;">Passe ativo · {{ activePass?.areaGeografica || 'Rede Geral' }}</h4>
                <span v-if="activePass?.expiraEm" style="color: #6B7077;">Válido até {{ new Date(activePass.expiraEm).toLocaleDateString('pt-PT', { day: 'numeric', month: 'short', year: 'numeric' }) }}</span>
                <span v-else style="color: #6B7077;">Aguarda ativação</span>
              </div>
            </div>

            <button class="btn-primary-modal" style="background: #F5A623; border-color: #F5A623;" @click="showPassWarningModal = false; showConfirmModal = true">Continuar mesmo assim</button>
            <button style="width: 100%; margin-top: 12px; padding: 14px; background: none; border: none; font-weight: 600; font-size: 15px; color: #6B7077; cursor: pointer; border-radius: 12px; transition: background 0.2s;" @mouseover="e => e.target.style.background='#F2F4F7'" @mouseout="e => e.target.style.background='transparent'" @click="showPassWarningModal = false">Cancelar</button>
          </div>
        </div>
      </div>
    </transition>

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
            <p>O teu Pack de 10 Bilhetes foi adicionado à tua conta com sucesso e já o podes utilizar.</p>
            
            <div class="receipt-box">
              <div class="receipt-icon blue-bg">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#7A3FF2" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="16" rx="2"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
              </div>
              <div class="receipt-info">
                <h4>Pack 10 Bilhetes ({{ selectedZone }})</h4>
                <span>Total pago: {{ formattedFinalPrice }}€</span>
              </div>
            </div>

            <button class="btn-primary-modal blue-btn" @click="irParaBilhetes">Ver os meus bilhetes</button>
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
const selectedZone = ref('Zona A')
const selectedMethod = ref('wallet')
const isProcessing = ref(false)
const paymentStatus = ref(null)
const showConfirmModal = ref(false)
const showPassWarningModal = ref(false)
const activePass = ref(null)

const handlePagarClick = () => {
  // Simplificamos a assumir que se houver qualquer passe, damos aviso
  if (activePass.value) {
    showPassWarningModal.value = true
  } else {
    showConfirmModal.value = true
  }
}

// Estatuto real do utilizador
const userStatus = computed(() => authStore.user?.perfil || 'NORMAL')

const catalogoPack = ref([])

const zonasDisponiveis = computed(() => {
  const z = new Set()
  catalogoPack.value.forEach(t => {
    if (t.zona && t.zona !== 'Rede completa') z.add(t.zona)
  })
  return Array.from(z).sort()
})

onMounted(async () => {
  try {
    const res = await axios.get('/api/catalogo/titulos')
    catalogoPack.value = res.data.pack || []
  } catch (err) {
    console.error('Erro ao carregar catalogo', err)
  }

  // Verificar se já tem passe
  try {
    const resT = await axios.get('/api/titulos')
    const passes = resT.data.filter(t => t.tipo === 'PASSE' && (t.estado === 'ATIVO' || t.estado === 'PENDENTE'))
    if (passes.length > 0) activePass.value = passes[0]
  } catch (err) {
    // ignore
  }
})

const basePrice = computed(() => {
  const t = catalogoPack.value.find(x => x.perfil === 'NORMAL' && x.zona === selectedZone.value)
  return t ? t.preco * 10 : 0
})

const finalPrice = computed(() => {
  const t = catalogoPack.value.find(x => x.perfil === userStatus.value && x.zona === selectedZone.value)
  return t ? t.preco * 10 : 0
})

const discountValue = computed(() => Math.max(0, basePrice.value - finalPrice.value))

const formattedBasePrice = computed(() => basePrice.value.toFixed(2).replace('.', ','))
const formattedDiscountValue = computed(() => discountValue.value.toFixed(2).replace('.', ','))
const formattedFinalPrice = computed(() => finalPrice.value.toFixed(2).replace('.', ','))

// Saldo do Utilizador
const userBalance = computed(() => authStore.userSaldo || 0)

// ── Lógica de Confirmação ──
const confirmarCompra = async () => {
  if (selectedMethod.value === 'wallet' && userBalance.value < finalPrice.value) {
    paymentStatus.value = 'error'
    showConfirmModal.value = false
    return
  }

  isProcessing.value = true

  try {
    const t = catalogoPack.value.find(x => x.zona === selectedZone.value)
    const ids = t && t.zona_id ? [t.zona_id] : []

    await axios.post('/api/titulos', {
      tipo: 'PACK',
      viagens: 10,
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

/* ── Hero Card Roxo (Sem radius inferior) ── */
.pack-hero {
  background: linear-gradient(135deg, #5B2EC2 0%, #7A3FF2 60%, #9D6BFF 100%);
  padding: 52px 28px 36px 28px;
  border-radius: 0;
  color: #fff;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(122, 63, 242, 0.15);
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
  backdrop-filter: blur(4px);
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

.hero-pass-info { margin-bottom: 16px; }
.hero-title { font-size: 28px; font-weight: 700; color: #fff; margin: 0 0 4px 0; }
.hero-subtitle { font-size: 14px; color: rgba(255,255,255,0.85); margin: 0; }

.hero-meta { display: flex; align-items: center; gap: 8px; }
.hero-meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: rgba(255,255,255,0.9); font-weight: 500; }
.hero-price { font-size: 16px; font-weight: 700; color: #fff; }
.hero-price-strikethrough { font-size: 13px; color: rgba(255, 255, 255, 0.6); text-decoration: line-through; margin-right: 6px; font-weight: 500; }
.hero-meta-sep { color: rgba(255,255,255,0.5); }

.hero-circle-1 { position: absolute; top: -60px; right: -60px; width: 220px; height: 220px; border-radius: 50%; background: rgba(255,255,255,0.08); pointer-events: none; }
.hero-circle-2 { position: absolute; bottom: -80px; right: -30px; width: 180px; height: 180px; border-radius: 50%; background: rgba(255,255,255,0.07); pointer-events: none; }

.body-content { padding: 20px 16px 0 16px; display: flex; flex-direction: column; gap: 12px; }

.step-card { background: #fff; border-radius: 20px; padding: 20px; box-shadow: 0 1px 2px rgba(16,24,40,0.04), 0 4px 14px rgba(16,24,40,0.06); }
.step-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.step-num { width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; flex-shrink: 0; }
.step-num.active { background: #7A3FF2; color: #fff; }
.step-label-group { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.step-label { font-size: 15px; font-weight: 700; color: #15171A; }
.step-sublabel { font-size: 12px; color: #9AA0A6; }

.zone-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.zone-card { background: #fff; border-radius: 14px; padding: 16px 12px; border: 2px solid #E4E7EB; cursor: pointer; transition: all 0.2s ease; display: flex; flex-direction: column; align-items: center; text-align: center; }
.zone-card.active { border-color: #7A3FF2; background: #F9F7FF; }
.z-badge { background: #F4F5F7; color: #6B7077; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 8px; display: inline-block; margin-bottom: 10px; transition: all 0.2s; }
.z-badge.active { background: #7A3FF2; color: #fff; }
.z-title { font-size: 12px; font-weight: 600; color: #6B7077; margin-bottom: 4px; }
.zone-card.active .z-title { color: #1A1A1A; }
.z-price { font-size: 16px; font-weight: 800; color: #1A1A1A; }
.zone-card.active .z-price { color: #7A3FF2; }

.payment-list { display: flex; flex-direction: column; gap: 10px; }
.pay-option { display: flex; align-items: center; gap: 14px; padding: 14px; border: 1.5px solid #E4E7EB; border-radius: 14px; cursor: pointer; transition: all 0.2s; }
.pay-option.selected { border-color: #7A3FF2; background: #F9F7FF; }
.pay-icon { width: 38px; height: 38px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.pay-icon.blue-bg { background: rgba(0,133,255,0.1); }
.pay-icon.dark-bg { background: #15171A; }
.pay-info { flex: 1; display: flex; flex-direction: column; gap: 3px; }
.pay-name { font-size: 15px; font-weight: 600; color: #15171A; }
.pay-desc { font-size: 12px; color: #6B7077; }
.text-red { color: #EF4444 !important; }
.radio-outer { width: 20px; height: 20px; border: 2px solid #D0D5DD; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; transition: border-color 0.2s; }
.radio-outer.active { border-color: #7A3FF2; }
.radio-inner { width: 10px; height: 10px; background: #7A3FF2; border-radius: 50%; }
.sr-only { display: none; }

.summary-card { margin-bottom: 24px; }
.summary-header { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.summary-icon { width: 32px; height: 32px; background: #F9F7FF; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.summary-title { font-size: 16px; font-weight: 700; color: #15171A; margin: 0; }
.summary-content { display: flex; flex-direction: column; gap: 10px; }
.summary-row { display: flex; justify-content: space-between; align-items: center; }
.summary-label { font-size: 13px; color: #6B7077; }
.summary-value { font-size: 14px; font-weight: 600; color: #15171A; }
.summary-divider { height: 1px; background: #E4E7EB; margin: 4px 0; }
.text-green .summary-label, .text-green .summary-value { color: #1F9D4D; }
.final-price { margin-top: 4px; }
.final-price .summary-label { font-size: 15px; font-weight: 700; color: #15171A; }
.summary-value-large { font-size: 18px; font-weight: 800; color: #7A3FF2; }

.bottom-action { position: fixed; bottom: 80px; left: 0; width: 100%; padding: 14px 16px 18px 16px; background: linear-gradient(to top, rgba(244,245,247,1) 80%, rgba(244,245,247,0)); z-index: 50; box-sizing: border-box; }
.btn-confirmar { width: 100%; height: 56px; color: #fff; border: none; border-radius: 999px; font-size: 16px; font-weight: 700; cursor: pointer; transition: background 0.2s, transform 0.1s; }
.btn-confirmar.blue-btn { background: #7A3FF2; box-shadow: 0 4px 14px rgba(122, 63, 242, 0.3); }
.btn-confirmar:active:not(:disabled) { transform: scale(0.98); }
.btn-confirmar:hover:not(:disabled) { background: #622CC2; }
.btn-confirmar:disabled { background: #D8C3FF; cursor: not-allowed; box-shadow: none; }

.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(11, 16, 24, 0.5); z-index: 9999; display: flex; align-items: center; justify-content: center; padding: 20px; box-sizing: border-box; }
.modal-card { background: #FFFFFF; border-radius: 24px; width: 100%; max-width: 340px; overflow: hidden; box-shadow: 0px 24px 60px rgba(0, 0, 0, 0.28); animation: popUp 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
@keyframes popUp { 0% { transform: scale(0.95) translateY(20px); opacity: 0; } 100% { transform: scale(1) translateY(0); opacity: 1; } }
.modal-top { height: 160px; position: relative; display: flex; justify-content: center; }
.success-bg { background: #E7F6EC; }
.icon-overlap { position: absolute; bottom: -42px; background: #FFFFFF; width: 84px; height: 84px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.circle-icon { width: 62px; height: 62px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.green-circle { background: #1F9D4D; }
.modal-body { padding: 60px 24px 24px 24px; text-align: center; }
.modal-body h2 { font-size: 22px; font-weight: 700; color: #15171A; margin: 0 0 8px 0; }
.modal-body p { font-size: 14px; color: #6B7077; line-height: 1.5; margin: 0 0 24px 0; }
.receipt-box { background: #F7F8FA; border-radius: 14px; padding: 16px; display: flex; align-items: center; gap: 14px; margin-bottom: 24px; text-align: left; }
.receipt-icon { width: 38px; height: 38px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.receipt-icon.blue-bg { background: #F9F7FF; }
.receipt-info h4 { font-size: 15px; font-weight: 700; color: #15171A; margin: 0 0 2px 0; }
.receipt-info span { font-size: 13px; color: #6B7077; }
.btn-primary-modal { width: 100%; border: none; border-radius: 27px; height: 54px; font-size: 16px; font-weight: 700; cursor: pointer; transition: all 0.2s; }
.btn-primary-modal.blue-btn { background: #7A3FF2; color: #FFFFFF; }
.btn-primary-modal.blue-btn:active { transform: scale(0.96); background: #622CC2; }
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.3s ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style>
