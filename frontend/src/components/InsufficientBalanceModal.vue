<template>
  <transition name="modal-fade">
    <div v-if="show" class="modal-overlay">
      <div class="modal-card">
        
        <div class="modal-top error-bg">
          <div class="icon-overlap">
            <div class="icon-outer">
              <div class="icon-inner red-circle">
                <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-body">
          <h2 class="modal-title">Saldo Insuficiente</h2>
          <p class="modal-desc">
            A tua carteira não tem saldo suficiente para concluir esta transação. Verifica o teu saldo ou carrega a carteira.
          </p>
          
          <div class="modal-actions">
            <button class="btn-action blue-action" @click="$emit('charge')">
              Carregar Carteira
            </button>
            <button class="btn-action cancel-action" @click="$emit('close')">
              Tentar novamente
            </button>
          </div>
        </div>

      </div>
    </div>
  </transition>
</template>

<script setup>
defineProps({
  show: Boolean
})

// Emite 'close' para fechar o modal, e 'charge' para ir para ChargeWallet.vue
defineEmits(['close', 'charge'])
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(11, 13, 16, 0.5); /* Fundo escuro do SVG */
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

.modal-card {
  background: #FFFFFF;
  border-radius: 24px;
  width: 100%;
  max-width: 368px;
  box-shadow: 0px 24px 60px rgba(16, 24, 40, 0.4);
  animation: popUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}

.modal-top {
  height: 140px;
  border-top-left-radius: 24px;
  border-top-right-radius: 24px;
  position: relative;
}

.error-bg {
  background: #FBEAE8;
}

.icon-overlap {
  position: absolute;
  bottom: -38px;
  left: 50%;
  transform: translateX(-50%);
}

.icon-outer {
  width: 76px;
  height: 76px;
  background: #FFFFFF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(214, 58, 46, 0.2);
}

.icon-inner.red-circle {
  width: 54px;
  height: 54px;
  background: #D63A2E;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 60px 24px 24px 24px;
  text-align: center;
}

.modal-title {
  font-size: 22px;
  font-weight: 800;
  color: #15171A;
  margin: 0 0 8px 0;
}

.modal-desc {
  font-size: 14px;
  color: #6B7077;
  margin: 0 0 28px 0;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.btn-action {
  width: 100%;
  height: 52px;
  border-radius: 99px;
  font-size: 16px;
  font-weight: 700;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.blue-action {
  background: #0085FF;
  color: white;
}

.blue-action:active {
  background: #0070D6;
  transform: scale(0.98);
}

.cancel-action {
  background: transparent;
  color: #6B7077;
}

.cancel-action:active {
  background: #F4F5F7;
}

@keyframes popUp {
  0% { transform: scale(0.95) translateY(20px); opacity: 0; }
  100% { transform: scale(1) translateY(0); opacity: 1; }
}

.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.3s ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }
</style>