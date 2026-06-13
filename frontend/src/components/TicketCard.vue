<template>
  <div class="ticket-card">
    <div style="display: flex; justify-content: space-between; align-items: flex-start;">
      <div>
        <span class="ticket-tag type">{{ typeLabel }}</span>
        <div class="ticket-name">{{ ticket.nome || ticketTitle }}</div>
        <div class="ticket-meta">{{ ticket.areaGeografica || ticket.zona || 'Rede Geral' }}</div>
      </div>
      <span class="ticket-tag" :class="isActive ? 'active' : 'expired'">
        {{ isActive ? '● Ativo' : 'Expirado' }}
      </span>
    </div>

    <div class="ticket-footer">
      <div>
        <div class="ticket-validity">Validade: {{ formatDate(ticket.validade) }}</div>
        <div class="ticket-zone">{{ ticket.zonaDesignacao || '' }}</div>
      </div>
      <!-- Pack: show trips count -->
      <div v-if="ticket.tipo === 'PACK'" style="text-align: center;">
        <div class="ticket-trips">{{ ticket.viagensRestantes ?? '?' }}</div>
        <div class="ticket-trips-label">viagens</div>
      </div>
      <!-- QR code placeholder for single tickets -->
      <div v-else style="width: 44px; height: 44px; background: var(--bg-input); border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 1.4rem;">
        🔲
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'TicketCard',
  props: {
    ticket: { type: Object, required: true }
  },
  setup(props) {
    const typeLabel = computed(() => {
      const map = { BILHETE: 'Bilhete', PACK: 'Pack', PASSE: 'Passe' }
      return map[props.ticket.tipo] || props.ticket.tipo
    })
    const ticketTitle = computed(() => typeLabel.value)
    const isActive = computed(() => {
      if (!props.ticket.validade) return true
      return new Date(props.ticket.validade) >= new Date()
    })
    const formatDate = (d) => {
      if (!d) return '—'
      return new Date(d).toLocaleDateString('pt-PT', { day: '2-digit', month: '2-digit', year: 'numeric' })
    }
    return { typeLabel, ticketTitle, isActive, formatDate }
  }
}
</script>
