<template>
  <div>
    <div class="app-bar">
      <button @click="$router.back()" style="background:none;border:none;cursor:pointer;font-size:1.3rem;color:var(--text-dark);">‹</button>
      <span class="app-bar-title">Validar Título</span>
      <div style="width: 28px;"></div>
    </div>

    <div class="page-content">
      <!-- Validation form -->
      <div class="card" style="margin-top: 16px;">
        <div style="font-size: 0.85rem; font-weight: 700; color: var(--text-dark); margin-bottom: 14px;">📲 Simular passagem em validador</div>

        <div class="form-group">
          <label class="form-label">Selecionar Título</label>
          <select v-model="selectedTicketId" class="form-control" :disabled="loading">
            <option value="" disabled>-- Escolha um título ativo --</option>
            <option v-for="ticket in tickets" :key="ticket.id" :value="ticket.id">
              {{ ticketLabel(ticket) }}
            </option>
          </select>
          <p v-if="tickets.length === 0 && !loading" style="font-size: 0.82rem; color: var(--red); margin-top: 6px;">
            ⚠️ Sem títulos disponíveis. Compre um primeiro.
          </p>
        </div>

        <div class="form-group">
          <label class="form-label">Selecionar Validador</label>
          <select v-model="selectedReaderId" class="form-control" :disabled="loading">
            <option value="" disabled>-- Escolha um validador --</option>
            <option v-for="reader in readers" :key="reader.id" :value="reader.id">
              {{ reader.codigo }} — Linha: {{ reader.linhaNome }}
            </option>
          </select>
        </div>

        <button
          @click="handleValidate"
          class="btn btn-success btn-block"
          :disabled="loading || validating || !selectedTicketId || !selectedReaderId"
          style="padding: 15px;"
        >
          {{ validating ? 'A validar...' : '⚡ Aproximar Título (Validar)' }}
        </button>
      </div>

      <!-- Result -->
      <div v-if="result" style="margin-top: 16px;">
        <div :class="result.resultado === 'VALIDO' ? 'result-success' : 'result-fail'">
          <div class="result-icon">{{ result.resultado === 'VALIDO' ? '✅' : '❌' }}</div>
          <div class="result-title" :style="result.resultado === 'VALIDO' ? 'color: var(--green)' : 'color: var(--red)'">
            {{ result.resultado === 'VALIDO' ? 'Acesso Autorizado' : 'Acesso Recusado' }}
          </div>
          <div class="result-message">{{ result.mensagem }}</div>
        </div>
      </div>

      <div v-else class="empty-state" style="padding: 36px 0;">
        <div class="empty-icon">🔒</div>
        <h4>Aguardando validação</h4>
        <p>Selecione o título e o validador para simular a passagem nos torniquetes.</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

export default {
  name: 'Simulator',
  setup() {
    const route = useRoute()
    const tickets = ref([]), readers = ref([])
    const loading = ref(true), validating = ref(false)
    const selectedTicketId = ref(''), selectedReaderId = ref('')
    const result = ref(null)

    const ticketLabel = (t) => {
      const types = { PASSE: 'Passe', PACK: 'Pack', BILHETE: 'Bilhete' }
      const extra = t.tipo === 'PACK' ? ` [${t.viagensRestantes ?? '?'} viagens]` : ''
      return `${types[t.tipo] || t.tipo} — ${t.areaGeografica || 'Rede Geral'}${extra}`
    }

    const fetchAll = async () => {
      loading.value = true
      try {
        const [tR, rR] = await Promise.all([axios.get('/api/titulos'), axios.get('/api/leitores')])
        tickets.value = tR.data || []
        readers.value = rR.data || []
        if (route.query.ticketId) selectedTicketId.value = route.query.ticketId
      } catch (err) { console.error(err) } finally { loading.value = false }
    }

    const handleValidate = async () => {
      validating.value = true; result.value = null
      try {
        const res = await axios.post('/api/validacoes', { tituloId: selectedTicketId.value, leitorId: selectedReaderId.value })
        result.value = res.data
        await fetchAll()
      } catch (err) {
        result.value = { resultado: 'ERRO', mensagem: err.response?.data?.message || 'Falha na comunicação com o servidor.' }
      } finally { validating.value = false }
    }

    onMounted(fetchAll)
    return { tickets, readers, loading, validating, selectedTicketId, selectedReaderId, result, ticketLabel, handleValidate }
  }
}
</script>
