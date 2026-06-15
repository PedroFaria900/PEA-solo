<template>
  <transition name="modal-fade">
    <div v-if="show" class="modal-overlay">
      <div class="confirm-card-modal">
        
        <div class="confirm-top-bg">
          <div class="confirm-icon-wrapper">
            <div class="confirm-icon-outer">
              <div class="confirm-icon-inner">
                <slot name="icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
                  </svg>
                </slot>
              </div>
            </div>
          </div>
        </div>

        <div class="confirm-modal-body">
          <h2 class="confirm-title">{{ title }}</h2>
          <p class="confirm-desc">{{ description }}</p>

          <div class="confirm-summary-box">
            <div class="cs-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#0085FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="4" width="20" height="16" rx="2"></rect>
                <line x1="2" y1="10" x2="22" y2="10"></line>
              </svg>
            </div>
            <div class="cs-info">
              <span class="cs-name">{{ summaryTitle }}</span>
              <span class="cs-price">Total: {{ summaryPrice }}</span>
            </div>
          </div>

          <div class="confirm-actions">
            <button class="btn-action blue-action" @click="$emit('confirm')" :disabled="isProcessing">
              {{ isProcessing ? 'A processar...' : confirmText }}
            </button>
            <button class="btn-action cancel-action" @click="$emit('cancel')" :disabled="isProcessing">
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
defineProps({
  show: Boolean,
  title: { type: String, default: 'Confirmar Compra' },
  description: { type: String, default: 'Estás prestes a efetuar o pagamento. Confirma os detalhes antes de avançar.' },
  summaryTitle: String,
  summaryPrice: String,
  isProcessing: Boolean,
  confirmText: { type: String, default: 'Efetuar Pagamento' }
})

defineEmits(['confirm', 'cancel'])
</script>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(11, 13, 16, 0.5); z-index: 9999; display: flex; align-items: center; justify-content: center; padding: 20px; box-sizing: border-box; }
.confirm-card-modal { background: #FFFFFF; border-radius: 24px; width: 100%; max-width: 368px; box-shadow: 0px 24px 60px rgba(16, 24, 40, 0.4); animation: popUp 0.3s cubic-bezier(0.16, 1, 0.3, 1); position: relative; }
.confirm-top-bg { background: #EAF4FF; height: 140px; border-top-left-radius: 24px; border-top-right-radius: 24px; }
.confirm-icon-wrapper { position: absolute; bottom: -38px; left: 50%; transform: translateX(-50%); }
.confirm-icon-outer { width: 76px; height: 76px; background: #FFFFFF; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 6px 18px rgba(0, 133, 255, 0.2); }
.confirm-icon-inner { width: 54px; height: 54px; background: #0085FF; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.confirm-modal-body { padding: 60px 24px 24px 24px; text-align: center; }
.confirm-title { font-size: 22px; font-weight: 800; color: #15171A; margin: 0 0 8px 0; }
.confirm-desc { font-size: 14px; color: #6B7077; margin: 0 0 24px 0; line-height: 1.5; }
.confirm-summary-box { background: #F7F8FA; border-radius: 16px; padding: 16px; display: flex; align-items: center; gap: 16px; margin-bottom: 28px; text-align: left; }
.cs-icon { width: 44px; height: 44px; background: #EAF4FF; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.cs-info { display: flex; flex-direction: column; gap: 4px; }
.cs-name { font-size: 15px; font-weight: 700; color: #15171A; }
.cs-price { font-size: 14px; color: #0085FF; font-weight: 600; }
.confirm-actions { display: flex; flex-direction: column; gap: 12px; }
.btn-action { width: 100%; height: 52px; border-radius: 99px; font-size: 16px; font-weight: 700; border: none; cursor: pointer; transition: all 0.2s; }
.blue-action { background: #0085FF; color: white; }
.blue-action:active { background: #0070D6; transform: scale(0.98); }
.cancel-action { background: transparent; color: #6B7077; }
.cancel-action:active { background: #F4F5F7; }
@keyframes popUp { 0% { transform: scale(0.95) translateY(20px); opacity: 0; } 100% { transform: scale(1) translateY(0); opacity: 1; } }
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.3s ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style>