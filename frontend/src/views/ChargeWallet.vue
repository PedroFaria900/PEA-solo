<template>
  <div>
    <!-- Dark purple header (matches carregar saldoi.png top section) -->
    <div style="background: linear-gradient(160deg, #4a1b8c 0%, #2d0f5e 100%); padding: 0 0 28px; position: relative; overflow: hidden;">
      <!-- Decorative circles -->
      <div style="position: absolute; top: -40px; right: -40px; width: 160px; height: 160px; border-radius: 50%; background: rgba(255,255,255,0.05);"></div>
      <div style="position: absolute; bottom: -30px; left: -30px; width: 120px; height: 120px; border-radius: 50%; background: rgba(255,255,255,0.04);"></div>

      <!-- Back button -->
      <div style="display: flex; align-items: center; padding: 16px 16px 8px; position: relative; z-index: 1;">
        <button @click="$router.back()" style="background: rgba(255,255,255,0.15); border: none; border-radius: 8px; width: 34px; height: 34px; cursor: pointer; color: #fff; font-size: 1.2rem; display: flex; align-items: center; justify-content: center;">‹</button>
        <span style="margin-left: 12px; color: #fff; font-size: 1rem; font-weight: 700;">Carregar Saldo</span>
      </div>

      <!-- Current balance -->
      <div style="text-align: center; padding: 16px 0 8px; position: relative; z-index: 1;">
        <div style="font-size: 0.8rem; color: rgba(255,255,255,0.65); font-weight: 500; margin-bottom: 4px;">Saldo Atual</div>
        <div style="font-size: 2.8rem; font-weight: 800; color: #fff; letter-spacing: -0.02em;">{{ authStore.userSaldo.toFixed(2) }}€</div>
      </div>
    </div>

    <!-- White content area -->
    <div class="page-content" style="background: var(--bg-page); margin-top: 0; border-radius: 20px 20px 0 0; margin-top: -16px; padding-top: 24px;">

      <!-- Amount selection -->
      <div style="font-size: 0.9rem; font-weight: 700; color: var(--text-dark); margin-bottom: 14px;">Selecione o montante</div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 20px;">
        <button
          v-for="amt in [5, 10, 20, 50, 100, 200]"
          :key="amt"
          class="charge-btn"
          :class="{ selected: selectedAmt === amt }"
          @click="selectedAmt = amt; customAmt = ''"
          style="padding: 16px;"
        >
          {{ amt }}€
        </button>
      </div>

      <!-- Custom amount -->
      <div class="form-group">
        <label class="form-label">Ou introduza outro valor</label>
        <div style="display: flex; align-items: center; gap: 0;">
          <span style="background: var(--bg-input); border: 1.5px solid var(--border); border-right: none; border-radius: 8px 0 0 8px; padding: 13px 14px; font-weight: 600; color: var(--text-muted);">€</span>
          <input
            type="number"
            v-model="customAmt"
            @input="selectedAmt = null"
            class="form-control"
            placeholder="0.00"
            min="1"
            step="0.01"
            style="border-radius: 0 8px 8px 0; border-left: none;"
          />
        </div>
      </div>

      <!-- Payment method selector -->
      <div class="form-group">
        <label class="form-label">Método de Pagamento</label>
        <div
          @click="$router.push('/wallet/methods')"
          style="display: flex; align-items: center; justify-content: space-between; background: var(--bg-input); border: 1.5px solid var(--border); border-radius: 8px; padding: 13px 14px; cursor: pointer;"
        >
          <div style="display: flex; align-items: center; gap: 10px;">
            <span style="font-size: 1.2rem;">💳</span>
            <span style="font-size: 0.9rem; color: var(--text-body);">Gerir métodos de pagamento</span>
          </div>
          <span style="color: var(--text-muted); font-size: 1rem;">›</span>
        </div>
      </div>

      <div v-if="successMsg" class="alert alert-success">✅ {{ successMsg }}</div>
      <div v-if="errorMsg" class="alert alert-error">⚠️ {{ errorMsg }}</div>

      <!-- Confirm button (blue, bottom area like mockup) -->
      <button
        @click="handleCharge"
        class="btn btn-primary btn-block"
        :disabled="charging || (!selectedAmt && !customAmt)"
        style="padding: 16px; font-size: 1rem;"
      >
        {{ charging ? 'A processar...' : `Carregar ${effectiveAmt ? effectiveAmt.toFixed(2) + '€' : ''}` }}
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '../store/auth'

export default {
  name: 'ChargeWallet',
  setup() {
    const authStore = useAuthStore()
    const selectedAmt = ref(null)
    const customAmt = ref('')
    const charging = ref(false)
    const successMsg = ref('')
    const errorMsg = ref('')

    const effectiveAmt = computed(() => {
      if (selectedAmt.value) return selectedAmt.value
      const v = parseFloat(customAmt.value)
      return isNaN(v) ? null : v
    })

    const handleCharge = async () => {
      const amt = effectiveAmt.value
      if (!amt || amt <= 0) return
      charging.value = true
      errorMsg.value = ''
      successMsg.value = ''
      try {
        await authStore.carregarSaldo(amt)
        successMsg.value = `Saldo carregado com +${amt.toFixed(2)}€!`
        selectedAmt.value = null
        customAmt.value = ''
        setTimeout(() => { successMsg.value = '' }, 3000)
      } catch (e) {
        errorMsg.value = 'Não foi possível carregar o saldo. Tente novamente.'
      } finally {
        charging.value = false
      }
    }

    onMounted(() => authStore.fetchProfile())

    return { authStore, selectedAmt, customAmt, effectiveAmt, charging, successMsg, errorMsg, handleCharge }
  }
}
</script>
