<template>
  <div v-if="loading" class="spinner">A carregar...</div>
  <div v-else-if="transactions.length === 0" style="text-align: center; padding: 24px 0; color: var(--text-muted); font-size: 0.88rem;">
    Sem transações registadas.
  </div>
  <div v-else>
    <div v-for="t in transactions" :key="t.id" class="list-item">
      <div class="list-icon" :class="t.tipo === 'CARREGAMENTO' ? 'green' : 'blue'">
        {{ t.tipo === 'CARREGAMENTO' ? '💳' : '🎫' }}
      </div>
      <div class="list-body">
        <div class="list-title">{{ t.descricao || (t.tipo === 'CARREGAMENTO' ? 'Carregamento' : 'Compra de Título') }}</div>
        <div class="list-subtitle">{{ formatDateTime(t.momento) }}</div>
      </div>
      <div class="list-amount" :class="t.tipo === 'CARREGAMENTO' ? 'credit' : 'debit'">
        {{ t.tipo === 'CARREGAMENTO' ? '+' : '-' }}{{ Math.abs(t.valor).toFixed(2) }}€
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TransactionsList',
  props: {
    transactions: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false }
  },
  setup() {
    const formatDateTime = (d) => {
      if (!d) return '—'
      return new Date(d).toLocaleString('pt-PT', {
        day: '2-digit', month: '2-digit', year: 'numeric',
        hour: '2-digit', minute: '2-digit'
      })
    }
    return { formatDateTime }
  }
}
</script>
