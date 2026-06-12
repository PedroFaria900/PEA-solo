<template>
  <div>
    <div style="margin-bottom: 24px;">
      <router-link to="/" style="font-size: 0.9rem; font-weight: 600; display: inline-flex; align-items: center; gap: 4px;">
        ← Voltar ao Painel
      </router-link>
      <h1 style="font-size: 2.2rem; margin-top: 8px;">Histórico de Viagens</h1>
      <p style="color: var(--text-secondary);">Registo de todas as suas validações efetuadas nos transportes.</p>
    </div>

    <div class="glass-card">
      <div v-if="loading" style="text-align: center; padding: 40px 0; color: var(--text-secondary);">
        Carregando histórico...
      </div>

      <div v-else-if="trips.length === 0" style="text-align: center; padding: 40px 0; color: var(--text-secondary);">
        <span style="font-size: 3rem; display: block; margin-bottom: 12px;">🚌</span>
        <p>Ainda não efetuou nenhuma viagem.</p>
        <router-link to="/simulator" class="btn btn-primary" style="margin-top: 16px;">
          Validar Primeiro Bilhete
        </router-link>
      </div>

      <div v-else style="display: flex; flex-direction: column; gap: 12px;">
        <div v-for="trip in trips" :key="trip.id" class="history-item">
          <div class="history-info">
            <span class="history-title">Viagem na {{ trip.linha || 'Linha Não Definida' }}</span>
            <span class="history-date">
              ID Viagem: {{ trip.id }} | {{ formatDateTime(trip.momento) }}
            </span>
          </div>
          <div class="history-amount positive">
            ✓ Validado
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import axios from 'axios'

export default {
  name: 'Trips',
  setup() {
    const trips = ref([])
    const loading = ref(true)

    const fetchTrips = async () => {
      loading.value = true
      try {
        const res = await axios.get('/api/viagens')
        // Sort trips by date descending
        trips.value = (res.data || []).sort((a, b) => new Date(b.momento) - new Date(a.momento))
      } catch (err) {
        console.error('Erro ao obter histórico de viagens:', err)
      } finally {
        loading.value = false
      }
    }

    const formatDateTime = (dateTimeStr) => {
      if (!dateTimeStr) return '-'
      const date = new Date(dateTimeStr)
      return date.toLocaleDateString('pt-PT', { 
        day: '2-digit', 
        month: '2-digit', 
        year: 'numeric' 
      }) + ' às ' + date.toLocaleTimeString('pt-PT', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    onMounted(() => {
      fetchTrips()
    })

    return {
      trips,
      loading,
      formatDateTime
    }
  }
}
</script>
