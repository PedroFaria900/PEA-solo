<template>
  <div>
    <!-- App Bar -->
    <div class="app-bar">
      <button @click="$router.back()" style="background:none;border:none;cursor:pointer;font-size:1.3rem;color:var(--text-dark);">‹</button>
      <span class="app-bar-title">Comprar Bilhetes</span>
      <div style="width: 28px;"></div>
    </div>

    <div class="page-content">
      <div v-if="successMsg" class="alert alert-success" style="margin-top: 16px;">✅ {{ successMsg }}</div>
      <div v-if="errorMsg" class="alert alert-error" style="margin-top: 16px;">⚠️ {{ errorMsg }}</div>

      <!-- Tabs -->
      <div class="tabs">
        <button v-for="tab in tabs" :key="tab.key" class="tab-btn" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">
          {{ tab.label }}
        </button>
      </div>

      <div v-if="loading" class="spinner">A carregar catálogo...</div>

      <div v-else>
        <div v-if="filteredCatalog.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <h4>Sem opções disponíveis</h4>
          <p>Não há tarifas disponíveis para este tipo.</p>
        </div>

        <div v-for="item in filteredCatalog" :key="item.id" class="catalog-item">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px;">
            <div>
              <div class="catalog-name">{{ item.nome }}</div>
              <div class="catalog-zone">Zona: {{ item.zona }}</div>
            </div>
            <div class="catalog-price">{{ item.preco.toFixed(2) }}€</div>
          </div>

          <div class="divider"></div>

          <div class="form-group" style="margin-bottom: 10px;">
            <label class="form-label">Data de Validade</label>
            <input type="date" v-model="item.selectedValidade" class="form-control" />
          </div>

          <div v-if="activeTab === 'pack'" class="form-group" style="margin-bottom: 10px;">
            <label class="form-label">Número de Viagens</label>
            <select v-model="item.selectedViagens" class="form-control">
              <option :value="10">10 Viagens</option>
              <option :value="20">20 Viagens</option>
              <option :value="50">50 Viagens</option>
            </select>
          </div>

          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <span style="font-size: 0.85rem; color: var(--text-muted);">Total:</span>
            <span style="font-weight: 800; font-size: 1.1rem; color: var(--text-dark);">{{ calculateTotal(item).toFixed(2) }}€</span>
          </div>

          <button
            @click="handleBuy(item)"
            :disabled="buyingId === item.id || authStore.userSaldo < calculateTotal(item)"
            class="btn btn-primary btn-block"
          >
            {{ buyingId === item.id ? 'A processar...' : authStore.userSaldo < calculateTotal(item) ? 'Saldo Insuficiente' : 'Confirmar Compra' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../store/auth'
import axios from 'axios'

export default {
  name: 'Buy',
  setup() {
    const authStore = useAuthStore()
    const activeTab = ref('bilhete')
    const catalog = ref({ bilhete: [], pack: [], passe: [] })
    const loading = ref(true), buyingId = ref(null), successMsg = ref(''), errorMsg = ref('')
    const tabs = [{ key: 'bilhete', label: 'Bilhete' }, { key: 'pack', label: 'Pack' }, { key: 'passe', label: 'Passe' }]

    const fetchCatalog = async () => {
      loading.value = true
      try {
        const res = await axios.get('/api/catalogo/titulos')
        const today = new Date().toISOString().split('T')[0]
        const nextMonth = new Date(new Date().setMonth(new Date().getMonth() + 1)).toISOString().split('T')[0]
        const nextYear = new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString().split('T')[0]
        const data = res.data || {}
        const normalized = {}
        Object.keys(data).forEach(key => {
          normalized[key.toLowerCase()] = (data[key] || []).map(item => ({
            ...item,
            selectedValidade: key.toLowerCase() === 'passe' ? nextMonth : key.toLowerCase() === 'pack' ? nextYear : today,
            selectedViagens: 10
          }))
        })
        catalog.value = { bilhete: normalized.bilhete || [], pack: normalized.pack || [], passe: normalized.passe || [] }
      } catch (err) {
        errorMsg.value = 'Não foi possível carregar o catálogo.'
      } finally { loading.value = false }
    }

    const filteredCatalog = computed(() => catalog.value[activeTab.value] || [])
    const calculateTotal = (item) => activeTab.value === 'pack' ? item.preco * (item.selectedViagens || 10) : item.preco

    const handleBuy = async (item) => {
      buyingId.value = item.id; errorMsg.value = ''; successMsg.value = ''
      const payload = { tipo: activeTab.value.toUpperCase(), validade: item.selectedValidade }
      if (activeTab.value === 'passe') payload.zonaId = item.zonaId
      else if (activeTab.value === 'pack') { payload.zonaId = item.zonaId; payload.viagens = item.selectedViagens }
      else if (activeTab.value === 'bilhete') payload.zonasIds = item.zonaId ? [item.zonaId] : []
      try {
        await axios.post('/api/titulos', payload)
        successMsg.value = `Compra de ${item.nome} efetuada com sucesso!`
        await authStore.fetchProfile()
        await fetchCatalog()
      } catch (err) {
        errorMsg.value = err.response?.data?.message || 'Erro ao efetuar a compra.'
      } finally { buyingId.value = null }
    }

    onMounted(fetchCatalog)
    return { authStore, activeTab, tabs, filteredCatalog, loading, buyingId, successMsg, errorMsg, calculateTotal, handleBuy }
  }
}
</script>
