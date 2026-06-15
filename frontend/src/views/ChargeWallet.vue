<template>
  <div class="charge-page">
    <div class="backdrop-click" @click="router.back()"></div>

    <div class="bottom-sheet">
      <div class="drag-pill"></div>

      <div class="sheet-header">
        <h2 class="sheet-title">Carregar Carteira</h2>
        <button class="close-btn" @click="router.back()">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#15171A" stroke-width="2" stroke-linecap="round">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>

      <div class="sheet-body">
        
        <div class="amount-section">
          <label class="section-label">Valor a carregar</label>
          <div class="input-wrapper">
            <input 
              type="number" 
              v-model="amount" 
              class="amount-input" 
              min="1" 
              step="1"
            />
            <span class="currency">€</span>
          </div>
        </div>

        <div class="quick-amounts">
          <button 
            v-for="val in [5, 10, 15, 20]" 
            :key="val" 
            class="quick-btn" 
            :class="{ active: amount === val }"
            @click="amount = val"
          >
            +{{ val }}€
          </button>
        </div>

        <div class="divider"></div>

        <div class="payment-section">
          <h3 class="section-label">Método de Pagamento</h3>
          
          <div class="payment-list">
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
        <button 
          class="btn-confirmar blue-btn" 
          @click="processarCarregamento" 
          :disabled="isProcessing || !amount || amount <= 0"
        >
          {{ isProcessing ? 'A processar...' : `Carregar ${formattedAmount}€` }}
        </button>
      </div>

    </div>

    <transition name="modal-fade">
      <div v-if="paymentStatus" class="modal-overlay-inner">
        
        <div v-if="paymentStatus === 'success'" class="modal-card">
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
            <h2>Carregamento Concluído!</h2>
            <p>Foram adicionados <strong>{{ formattedAmount }}€</strong> ao saldo da tua carteira.</p>
            <button class="btn-primary-modal blue-btn" @click="voltarParaWallet">Voltar à Carteira</button>
          </div>
        </div>

        <div v-if="paymentStatus === 'error'" class="modal-card">
          <div class="modal-top error-bg">
            <div class="icon-overlap">
              <div class="circle-icon red-circle">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </div>
            </div>
          </div>
          <div class="modal-body">
            <h2>Carregamento Falhou</h2>
            <p>Não foi possível comunicar com a entidade de pagamento. Tenta novamente mais tarde.</p>
            <button class="btn-primary-modal blue-btn" @click="paymentStatus = null">Tentar novamente</button>
          </div>
        </div>

      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import axios from 'axios'

const router = useRouter()
const authStore = useAuthStore()

// ── Estado ──
const amount = ref(10) // Valor padrão
const selectedMethod = ref('mbway')
const isProcessing = ref(false)
const paymentStatus = ref(null)

const formattedAmount = computed(() => {
  if (!amount.value || amount.value <= 0) return '0,00'
  return Number(amount.value).toFixed(2).replace('.', ',')
})

// ── Ações ──
const processarCarregamento = async () => {
  isProcessing.value = true

  try {
    if (amount.value > 0) {
      await axios.post('/api/carteira/carregamentos', {
        valor: Number(amount.value)
      })
      
      await authStore.fetchProfile()
      paymentStatus.value = 'success'
    } else {
      paymentStatus.value = 'error'
    }
  } catch (err) {
    console.error('Erro ao carregar carteira', err)
    paymentStatus.value = 'error'
  } finally {
    isProcessing.value = false
  }
}

const voltarParaWallet = () => {
  paymentStatus.value = null
  router.push('/wallet') // Redireciona de volta para a carteira
}
</script>

<style scoped>
/* A página inteira tem posição fixa para cobrir tudo (incluindo a navbar se existir) */
.charge-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 999;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  background: rgba(16, 20, 28, 0.42); /* Escuro com opacidade */
  font-family: 'Roboto', sans-serif;
}

.backdrop-click {
  flex: 1;
  width: 100%;
  cursor: pointer;
}

/* ── Cartão Deslizante ── */
.bottom-sheet {
  background: #FFFFFF;
  border-radius: 24px 24px 0 0;
  width: 100%;
  height: 85vh; /* Altura do Bottom Sheet */
  display: flex;
  flex-direction: column;
  position: relative;
  animation: slideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1);
  box-shadow: 0 -4px 24px rgba(0,0,0,0.1);
}

@keyframes slideUp {
  0% { transform: translateY(100%); }
  100% { transform: translateY(0); }
}

.drag-pill {
  width: 42px;
  height: 5px;
  background: #DFE3E8;
  border-radius: 4px;
  margin: 12px auto;
}

.sheet-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 24px 20px 24px;
}

.sheet-title {
  font-size: 20px;
  font-weight: 800;
  color: #15171A;
  margin: 0;
}

.close-btn {
  background: #F4F5F7;
  border: none;
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}

.close-btn:active {
  background: #E4E7EB;
}

/* ── Body ── */
.sheet-body {
  padding: 0 24px 100px 24px;
  overflow-y: auto;
  flex: 1;
}

.section-label {
  font-size: 14px;
  font-weight: 700;
  color: #6B7077;
  display: block;
  margin-bottom: 12px;
}

/* Input Amount */
.amount-section {
  text-align: center;
  margin-top: 10px;
  margin-bottom: 24px;
}

.amount-section .section-label {
  text-align: center;
  margin-bottom: 8px;
}

.input-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #F4F5F7;
  border-radius: 20px;
  padding: 10px 24px;
  border: 2px solid transparent;
  transition: border 0.2s;
}

.input-wrapper:focus-within {
  border: 2px solid #0085FF;
  background: #F9FAFC;
}

.amount-input {
  background: transparent;
  border: none;
  font-size: 42px;
  font-weight: 800;
  color: #15171A;
  width: 120px;
  text-align: center;
  outline: none;
}

/* Remover setas do input number */
.amount-input::-webkit-outer-spin-button,
.amount-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.currency {
  font-size: 32px;
  font-weight: 700;
  color: #6B7077;
  margin-left: 4px;
}

/* Botões Rápidos */
.quick-amounts {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 28px;
}

.quick-btn {
  background: #FFFFFF;
  border: 1.5px solid #E4E7EB;
  color: #6B7077;
  font-size: 15px;
  font-weight: 700;
  border-radius: 999px;
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-btn.active {
  border-color: #0085FF;
  background: #EAF4FF;
  color: #0085FF;
}

.divider {
  height: 1px;
  background: #EDEFF2;
  margin-bottom: 24px;
}

/* Pagamentos */
.payment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pay-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border: 1.5px solid #E4E7EB;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.pay-option.selected {
  border-color: #0085FF;
  background: #F9FAFC;
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

.pay-icon.dark-bg { background: #15171A; }

.pay-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.pay-name {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.pay-desc {
  font-size: 13px;
  color: #6B7077;
}

.radio-outer {
  width: 22px;
  height: 22px;
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
  width: 12px;
  height: 12px;
  background: #0085FF;
  border-radius: 50%;
}

.sr-only { display: none; }

/* ── Botão fixo na Bottom Sheet ── */
.bottom-action {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 16px 24px 24px 24px;
  background: linear-gradient(to top, rgba(255,255,255,1) 80%, rgba(255,255,255,0));
}

.btn-confirmar {
  width: 100%;
  height: 56px;
  border: none;
  border-radius: 999px;
  font-size: 17px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
}

.btn-confirmar.blue-btn {
  background: #0085FF;
  color: #fff;
  box-shadow: 0 4px 14px rgba(0,133,255,0.3);
}

.btn-confirmar:active:not(:disabled) {
  transform: scale(0.98);
}

.btn-confirmar:disabled {
  background: #A3CFFF;
  cursor: not-allowed;
  box-shadow: none;
}

/* ── Modais de Feedback (Dentro da Página Fixa) ── */
.modal-overlay-inner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(11, 13, 16, 0.7);
  z-index: 1000;
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

.modal-top {
  height: 160px;
  position: relative;
  display: flex;
  justify-content: center;
}
.success-bg { background: #E7F6EC; }
.error-bg { background: #FBEAE8; }

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
.red-circle { background: #D63A2E; }

.modal-body {
  padding: 60px 24px 24px 24px;
  text-align: center;
}

.modal-body h2 {
  font-size: 22px;
  font-weight: 800;
  color: #15171A;
  margin: 0 0 8px 0;
}

.modal-body p {
  font-size: 14px;
  color: #6B7077;
  line-height: 1.5;
  margin: 0 0 24px 0;
}

.modal-body strong {
  color: #15171A;
}

.btn-primary-modal {
  width: 100%;
  border: none;
  border-radius: 999px;
  height: 52px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary-modal.blue-btn {
  background: #0085FF;
  color: #FFFFFF;
}

.btn-primary-modal.blue-btn:active {
  transform: scale(0.96);
  background: #0070D6;
}
</style>