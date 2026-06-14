<template>
  <div class="buy-page">
    
    <header class="top-bar">
      <button class="back-btn" @click="router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#15171A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"></line>
          <polyline points="12 19 5 12 12 5"></polyline>
        </svg>
      </button>
      <h1 class="page-title">Pagamento</h1>
      <div class="spacer"></div>
    </header>

    <section class="section">
      <h2 class="section-title">Resumo da Viagem</h2>
      
      <div class="summary-card">
        <div class="route-info">
          <span class="city">{{ origin }}</span>
          <span class="arrow">→</span>
          <span class="city">{{ dest }}</span>
        </div>
        
        <div class="ticket-type">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6B7077" stroke-width="2">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <path d="M7 7h10"/><path d="M7 11h10"/><path d="M7 15h5"/>
          </svg>
          Bilhete Simples (Ida)
        </div>

        <div class="divider"></div>

        <div class="price-row">
          <span class="price-label">Total a pagar</span>
          <span class="price-value">{{ formattedPrice }}€</span>
        </div>
      </div>
    </section>

    <section class="section">
      <h2 class="section-title">Método de Pagamento</h2>
      
      <div class="payment-methods">
        
        <label class="method-card" :class="{ selected: selectedMethod === 'wallet' }">
          <div class="method-icon blue-bg">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2"><rect x="2" y="6" width="20" height="12" rx="2"/><circle cx="12" cy="12" r="2"/><path d="M6 12h.01M18 12h.01"/></svg>
          </div>
          <div class="method-details">
            <span class="method-name">Saldo da Carteira</span>
            <span class="method-desc" :class="{ 'text-red-500': userBalance < price }">
              Disponível: {{ userBalance.toFixed(2).replace('.', ',') }}€
            </span>
          </div>
          <div class="radio-circle">
            <div class="radio-dot" v-if="selectedMethod === 'wallet'"></div>
          </div>
          <input type="radio" value="wallet" v-model="selectedMethod" class="hidden-radio" />
        </label>

        <label class="method-card" :class="{ selected: selectedMethod === 'mbway' }">
          <div class="method-icon dark-bg">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FFF" stroke-width="2"><rect x="5" y="2" width="14" height="20" rx="2" ry="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>
          </div>
          <div class="method-details">
            <span class="method-name">MB WAY</span>
            <span class="method-desc">Pagamento via telemóvel</span>
          </div>
          <div class="radio-circle">
            <div class="radio-dot" v-if="selectedMethod === 'mbway'"></div>
          </div>
          <input type="radio" value="mbway" v-model="selectedMethod" class="hidden-radio" />
        </label>

        <label class="method-card" :class="{ selected: selectedMethod === 'card' }">
          <div class="method-icon dark-bg">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#FFF" stroke-width="2"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
          </div>
          <div class="method-details">
            <span class="method-name">Cartão de Crédito / Débito</span>
            <span class="method-desc">Visa, Mastercard</span>
          </div>
          <div class="radio-circle">
            <div class="radio-dot" v-if="selectedMethod === 'card'"></div>
          </div>
          <input type="radio" value="card" v-model="selectedMethod" class="hidden-radio" />
        </label>

      </div>
    </section>

    <div class="bottom-action">
      <button class="btn-confirmar" @click="confirmarCompra" :disabled="isProcessing">
        {{ isProcessing ? 'A processar...' : `Pagar ${formattedPrice}€` }}
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// Captura os dados que vieram do URL
const origin = computed(() => route.query.origin || 'Origem')
const dest = computed(() => route.query.dest || 'Destino')
const price = computed(() => Number(route.query.price || 0))

const formattedPrice = computed(() => price.value.toFixed(2).replace('.', ','))

// Lê o saldo diretamente do teu GETTER no store!
const userBalance = computed(() => authStore.userSaldo)

const selectedMethod = ref('wallet') 
const isProcessing = ref(false)

const confirmarCompra = () => {
  // Validação usando o teu getter de saldo
  if (selectedMethod.value === 'wallet') {
    if (userBalance.value < price.value) {
      alert(`Erro: Saldo insuficiente!\nO teu saldo é ${userBalance.value.toFixed(2)}€ mas o bilhete custa ${price.value.toFixed(2)}€.\nPor favor, carrega a carteira.`)
      return 
    }
  }

  isProcessing.value = true
  
  // Simulação de delay do backend
  setTimeout(() => {
    isProcessing.value = false
    
    // Desconta o saldo (Mockup Visual)
    // Nota: Como o authStore.user pode ser null se o login falhar, verificamos se existe
    if (selectedMethod.value === 'wallet' && authStore.user) {
      authStore.user.saldo -= price.value
    }

    alert(`Compra de bilhete para ${dest.value} confirmada com sucesso!`)
    
    // Volta para as rotas
    router.push('/search') 
  }, 1500)
}
</script>

<style scoped>
.buy-page {
  background: #FFFFFF;
  min-height: 100vh;
  /* Espaço em baixo gigante (navbar + tamanho do botão) para que se consiga fazer scroll até ao fim */
  padding: 38px 28px 160px 28px; 
  font-family: 'Roboto', sans-serif;
  box-sizing: border-box;
}

/* Header */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.back-btn {
  background: none;
  border: none;
  padding: 0;
  width: 24px;
  height: 24px;
  cursor: pointer;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #15171A;
  margin: 0;
}

.spacer { width: 24px; }

/* Secções */
.section { margin-bottom: 32px; }

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #15171A;
  margin: 0 0 16px 0;
  letter-spacing: -0.2px;
}

/* Cartão de Resumo */
.summary-card {
  background: #FFFFFF;
  border: 1px solid #E4E7EB;
  box-shadow: 0px 1px 2px rgba(16, 24, 40, 0.04), 0px 6px 18px rgba(16, 24, 40, 0.07);
  border-radius: 20px;
  padding: 20px;
}

.route-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 700;
  color: #15171A;
  margin-bottom: 8px;
}

.route-info .arrow { color: #6B7077; font-weight: 500; }

.ticket-type {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6B7077;
  margin-bottom: 16px;
}

.divider {
  height: 1px;
  background: #EDEFF2;
  margin: 16px 0;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  color: #15171A;
}

.price-row .price-value {
  font-size: 24px;
  font-weight: 700;
  color: #0085FF;
}

/* Métodos de Pagamento */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1.5px solid #E4E7EB;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.method-card.selected {
  border-color: #0085FF;
  background: #F5FAFF;
}

.method-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.method-icon.blue-bg { background: rgba(0, 133, 255, 0.1); }
.method-icon.dark-bg { background: #15171A; }

.method-details {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.method-name {
  font-size: 16px;
  font-weight: 600;
  color: #15171A;
}

.method-desc { font-size: 13px; color: #6B7077; }
.text-red-500 { color: #EF4444 !important; }

/* Círculo do Radio Button */
.hidden-radio { display: none; }

.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #E4E7EB;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.method-card.selected .radio-circle { border-color: #0085FF; }

.radio-dot {
  width: 10px;
  height: 10px;
  background: #0085FF;
  border-radius: 50%;
}

/* Ação de Fundo (A correção principal) */
.bottom-action {
  position: fixed;
  bottom: 63px; /* <-- Empurra a barra de ação exatamente para cima da NavBar global */
  left: 0;
  width: 100%;
  padding: 16px 28px 20px 28px;
  background: linear-gradient(to top, rgba(255,255,255,1) 80%, rgba(255,255,255,0));
  z-index: 50; /* Garante que fica por cima de outros elementos ao fazer scroll */
  box-sizing: border-box;
}

.btn-confirmar {
  width: 100%;
  height: 54px;
  background: #0085FF;
  color: #FFFFFF;
  border: none;
  border-radius: 999px;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0px 4px 12px rgba(0, 133, 255, 0.25);
  transition: background 0.2s;
}

.btn-confirmar:hover:not(:disabled) { background: #0073E6; }

.btn-confirmar:disabled {
  background: #A3CFFF;
  cursor: not-allowed;
  box-shadow: none;
}
</style>