<template>
  <div class="wallet-page">

    <div class="hero-card blue-hero">
      <button class="back-btn" @click="$router.back()">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
          <line x1="19" y1="12" x2="5" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
      </button>

      <h2 class="hero-page-title">Carteira</h2>

      <div class="balance-section">
        <span class="balance-label">Saldo disponível</span>
        <h1 class="balance-amount">{{ formattedSaldo }}€</h1>
        <span class="balance-desc">Carteira Bilhética</span>
      </div>

      <div class="hero-circle-1"></div>
      <div class="hero-circle-2"></div>
    </div>

    <div class="body-content">
      
      <div class="history-card">
        <div class="history-header">
          <div class="history-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="8" y1="6" x2="21" y2="6"></line>
              <line x1="8" y1="12" x2="21" y2="12"></line>
              <line x1="8" y1="18" x2="21" y2="18"></line>
              <line x1="3" y1="6" x2="3.01" y2="6"></line>
              <line x1="3" y1="12" x2="3.01" y2="12"></line>
              <line x1="3" y1="18" x2="3.01" y2="18"></line>
            </svg>
          </div>
          <div class="history-label-group">
            <span class="history-label">Histórico de Transações</span>
            <span class="history-sublabel">Os teus últimos movimentos</span>
          </div>
        </div>

        <div class="history-divider"></div>

        <div class="transactions-container">
          <div v-if="loadingTransactions" class="status-state">
            <div class="spinner"></div>
            <p>A carregar movimentos...</p>
          </div>
          
          <div v-else-if="transactions.length === 0" class="status-state empty-state">
            <div class="empty-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#9AA0A6" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                <line x1="2" y1="10" x2="22" y2="10"></line>
              </svg>
            </div>
            <h4>Sem transações</h4>
            <p>Ainda não realizaste nenhum movimento na tua carteira.</p>
          </div>

          <TransactionsList v-else :transactions="transactions" :loading="false" />
        </div>
      </div>

    </div>

    <div class="bottom-action">
      <button class="btn-confirmar blue-btn" @click="$router.push('/wallet/charge')">
        Carregar Saldo
      </button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import axios from 'axios'
import TransactionsList from '../components/TransactionsList.vue'

const router = useRouter()
const authStore = useAuthStore()

const transactions = ref([])
const loadingTransactions = ref(true)

// Saldo formatado de forma limpa (ex: 15,50)
const formattedSaldo = computed(() => {
  const saldo = authStore.userSaldo || 0
  return saldo.toFixed(2).replace('.', ',')
})

const fetchTransactions = async () => {
  loadingTransactions.value = true
  try {
    const response = await axios.get('/api/carteira/transacoes')
    transactions.value = response.data || []
  } catch (e) {
    console.error('Erro ao carregar transações:', e)
  } finally {
    loadingTransactions.value = false
  }
}

onMounted(() => {
  authStore.fetchProfile()
  fetchTransactions()
})
</script>

<style scoped>
/* ── Base ── */
.wallet-page {
  background: #F4F5F7;
  min-height: 100vh;
  font-family: 'Roboto', sans-serif;
  padding-bottom: 130px;
  box-sizing: border-box;
}

/* ── Hero Card Azul ── */
.hero-card {
  position: relative;
  padding: 24px 24px 48px 24px;
  overflow: hidden;
  text-align: center;
}

.blue-hero {
  background: linear-gradient(135deg, #0085FF 0%, #0070D6 100%);
}

.back-btn {
  position: absolute;
  top: 20px;
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
  z-index: 10;
}

.hero-page-title {
  font-size: 17px;
  font-weight: 600;
  color: #fff;
  margin: 8px 0 32px 0;
  z-index: 10;
  position: relative;
}

.balance-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  z-index: 10;
  position: relative;
}

.balance-label {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.85);
}

.balance-amount {
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  margin: 0;
  letter-spacing: -0.5px;
}

.balance-desc {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin-top: 4px;
}

/* Decorações do Hero */
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
  bottom: -40px;
  left: -40px;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: rgba(255,255,255,0.06);
  pointer-events: none;
}

/* ── Corpo (Sobreposto ao Hero) ── */
.body-content {
  padding: 0 16px;
  margin-top: -24px; /* Faz o card sobrepor o fundo azul, estilo SVG */
  position: relative;
  z-index: 20;
}

/* ── Card do Histórico ── */
.history-card {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(16,24,40,0.06);
  min-height: 300px;
}

.history-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.history-icon {
  width: 36px;
  height: 36px;
  background: #EAF4FF;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.history-label-group {
  display: flex;
  flex-direction: column;
}

.history-label {
  font-size: 16px;
  font-weight: 700;
  color: #15171A;
}

.history-sublabel {
  font-size: 12px;
  color: #6B7077;
}

.history-divider {
  height: 1px;
  background: #EDEFF2;
  margin-bottom: 16px;
}

/* Estados da Lista (Loading/Vazio) */
.transactions-container {
  display: flex;
  flex-direction: column;
}

.status-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  text-align: center;
}

.status-state p {
  font-size: 14px;
  color: #6B7077;
  margin-top: 12px;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #EAF4FF;
  border-top: 3px solid #0085FF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-state .empty-icon {
  width: 56px;
  height: 56px;
  background: #F4F5F7;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.empty-state h4 {
  font-size: 16px;
  font-weight: 700;
  color: #15171A;
  margin: 0 0 6px 0;
}

.empty-state p {
  margin: 0;
}

/* ── Botão fixo inferior ── */
.bottom-action {
  position: fixed;
  bottom: 80px; 
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
  box-shadow: 0 4px 14px rgba(0, 133, 255, 0.3);
}

.btn-confirmar:active:not(:disabled) {
  transform: scale(0.98);
}

.btn-confirmar:hover:not(:disabled) {
  background: #0073E6;
}
</style>