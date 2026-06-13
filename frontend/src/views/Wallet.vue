<template>
  <div style="display: flex; flex-direction: column; height: 100%;">
    <!-- App Bar -->
    <div class="app-bar">
      <button @click="$router.back()" style="background:none;border:none;cursor:pointer;font-size:1.3rem;color:var(--text-dark);">‹</button>
      <span class="app-bar-title">Carteira</span>
      <div style="width: 28px;"></div>
    </div>

    <!-- Scrollable content -->
    <div style="flex: 1; overflow-y: auto; padding: 16px 16px 0;">
      <!-- Purple wallet card -->
      <div class="wallet-hero">
        <div style="display: flex; justify-content: space-between; align-items: flex-start;">
          <div>
            <div class="wallet-label">Saldo disponível</div>
            <div class="wallet-amount">{{ authStore.userSaldo.toFixed(2) }}€</div>
            <div class="wallet-sub">Carteira Bilhética</div>
          </div>
          <div style="font-size: 2.2rem; opacity: 0.5;">💳</div>
        </div>
      </div>

      <!-- Transaction history -->
      <div class="section-header" style="margin-top: 20px;">
        <span class="section-title">Histórico de Transações</span>
      </div>

      <div class="card" style="margin-bottom: 16px;">
        <div v-if="loadingTransactions" class="spinner" style="padding: 24px 0;">A carregar...</div>
        <div v-else-if="transactions.length === 0" class="empty-state" style="padding: 24px 0;">
          <div class="empty-icon">📋</div>
          <h4>Sem transações</h4>
          <p>Ainda não realizaste nenhuma transação.</p>
        </div>
        <TransactionsList v-else :transactions="transactions" :loading="false" />
      </div>
    </div>

    <!-- Fixed bottom button like carteira.png -->
    <div style="padding: 12px 16px 16px; background: var(--bg-page); border-top: 1px solid var(--border);">
      <button
        @click="$router.push('/wallet/charge')"
        class="btn btn-primary btn-block"
        style="padding: 16px; font-size: 1rem;"
      >
        ↑ Carregar Saldo
      </button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../store/auth'
import axios from 'axios'
import TransactionsList from '../components/TransactionsList.vue'

export default {
  name: 'Wallet',
  components: { TransactionsList },
  setup() {
    const authStore = useAuthStore()
    const transactions = ref([])
    const loadingTransactions = ref(true)

    const fetchTransactions = async () => {
      loadingTransactions.value = true
      try {
        transactions.value = (await axios.get('/api/carteira/transacoes')).data || []
      } catch (e) {
        console.error(e)
      } finally {
        loadingTransactions.value = false
      }
    }

    onMounted(() => {
      authStore.fetchProfile()
      fetchTransactions()
    })

    return { authStore, transactions, loadingTransactions }
  }
}
</script>
