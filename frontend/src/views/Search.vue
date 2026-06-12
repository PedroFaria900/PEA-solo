<template>
  <div>
    <!-- App Bar -->
    <div class="app-bar">
      <button @click="$router.back()" style="background:none;border:none;cursor:pointer;font-size:1.3rem;color:var(--text-dark);">‹</button>
      <span class="app-bar-title">Pesquisa de Rede</span>
      <div style="width: 28px;"></div>
    </div>

    <div class="page-content">
      <!-- Route Search Card -->
      <div class="card" style="margin-top: 16px;">
        <div style="font-size: 0.85rem; font-weight: 700; color: var(--text-dark); margin-bottom: 14px;">📍 Planear Trajeto</div>

        <div class="route-points">
          <div class="route-point">
            <div class="route-dot origin" style="margin-top: 16px;"></div>
            <div class="form-group" style="margin-bottom: 0; flex: 1;">
              <label class="form-label">Origem</label>
              <select v-model="origemId" class="form-control">
                <option value="" disabled>Selecionar paragem...</option>
                <option v-for="p in stops" :key="p.id" :value="p.id">{{ p.nome }}</option>
              </select>
            </div>
          </div>
          <div class="route-line-connector" style="margin-left: 4px;"></div>
          <div class="route-point">
            <div class="route-dot destination" style="margin-top: 16px;"></div>
            <div class="form-group" style="margin-bottom: 0; flex: 1;">
              <label class="form-label">Destino</label>
              <select v-model="destinoId" class="form-control">
                <option value="" disabled>Selecionar paragem...</option>
                <option v-for="p in stops" :key="p.id" :value="p.id">{{ p.nome }}</option>
              </select>
            </div>
          </div>
        </div>

        <button @click="findRoutes" class="btn btn-primary btn-block" :disabled="loadingRoutes || !origemId || !destinoId" style="margin-top: 14px;">
          {{ loadingRoutes ? 'A pesquisar...' : 'Encontrar Rotas' }}
        </button>
      </div>

      <!-- Route results -->
      <div v-if="searched">
        <div v-if="suggestedRoutes.length === 0" class="empty-state" style="padding: 32px 0;">
          <div class="empty-icon">🔍</div>
          <h4>Sem rotas diretas</h4>
          <p>Não encontrámos ligação direta entre estas paragens.</p>
        </div>
        <div v-else>
          <div class="section-header">
            <span class="section-title">Rotas disponíveis</span>
          </div>
          <div v-for="r in suggestedRoutes" :key="r.linhaId" class="route-card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
              <span class="badge badge-blue">{{ r.linhaDesignacao }}</span>
              <span style="font-weight: 700; color: var(--blue);">{{ formatTime(r.tempoEstimadoSeg) }}</span>
            </div>
            <div style="font-size: 0.85rem; color: var(--text-body);">
              {{ r.paragemEmbarque?.nome }} → {{ r.paragemSaida?.nome }}
            </div>
            <div style="font-size: 0.78rem; color: var(--text-muted); margin-top: 4px;">{{ r.numerParagens }} paragens</div>
          </div>
        </div>
      </div>

      <!-- Line Browser -->
      <div class="section-header" style="margin-top: 8px;">
        <span class="section-title">Explorar Linhas</span>
      </div>

      <div class="form-group">
        <select v-model="selectedLineId" @change="fetchLineStops" class="form-control">
          <option value="" disabled>Escolha uma linha...</option>
          <option v-for="l in lines" :key="l.id" :value="l.id">{{ l.designacao }}</option>
        </select>
      </div>

      <div v-if="loadingStops" class="spinner">A carregar paragens...</div>

      <div v-else-if="lineStops.length > 0" class="card">
        <div style="font-size: 0.85rem; font-weight: 700; color: var(--text-dark); margin-bottom: 14px;">Paragens (IDA)</div>
        <div v-for="(stop, idx) in lineStops" :key="stop.paragemId" class="list-item">
          <div class="list-icon blue">{{ idx + 1 }}</div>
          <div class="list-body">
            <div class="list-title">{{ stop.paragemNome }}</div>
            <div class="list-subtitle">Código: {{ stop.paragemCodigo }}</div>
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
  name: 'Search',
  setup() {
    const lines = ref([]), stops = ref([]), selectedLineId = ref(''), lineStops = ref([])
    const origemId = ref(''), destinoId = ref(''), suggestedRoutes = ref([])
    const loadingStops = ref(false), loadingRoutes = ref(false), searched = ref(false)

    const fetchNetworkData = async () => {
      try {
        const [lR, sR] = await Promise.all([axios.get('/api/linhas'), axios.get('/api/paragens')])
        lines.value = lR.data || []
        stops.value = (sR.data || []).sort((a, b) => a.nome.localeCompare(b.nome))
      } catch (err) { console.error(err) }
    }

    const fetchLineStops = async () => {
      if (!selectedLineId.value) return
      loadingStops.value = true
      try {
        const res = await axios.get(`/api/linhas/${selectedLineId.value}/paragens`, { params: { sentido: 'IDA' } })
        lineStops.value = (res.data || []).sort((a, b) => a.sequencia - b.sequencia)
      } catch (err) { console.error(err) } finally { loadingStops.value = false }
    }

    const findRoutes = async () => {
      loadingRoutes.value = true; searched.value = true
      try {
        const res = await axios.get('/api/rotas', { params: { origemId: origemId.value, destinoId: destinoId.value } })
        suggestedRoutes.value = res.data || []
      } catch (err) { console.error(err) } finally { loadingRoutes.value = false }
    }

    const formatTime = (secs) => {
      if (!secs) return '0 min'
      const m = Math.round(secs / 60)
      return m < 60 ? `${m} min` : `${Math.floor(m/60)}h ${m%60}m`
    }

    onMounted(fetchNetworkData)
    return { lines, stops, selectedLineId, lineStops, origemId, destinoId, suggestedRoutes, loadingStops, loadingRoutes, searched, fetchLineStops, findRoutes, formatTime }
  }
}
</script>
