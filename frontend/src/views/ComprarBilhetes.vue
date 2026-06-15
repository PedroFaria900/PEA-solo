<template>
  <div class="page">
    
    <header class="header">
      <button class="back-btn" @click="router.back()">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#15171A" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </button>
    </header>

    <div class="title-section">
      <h1 class="titulo">Nova compra</h1>
    </div>

    <div class="tabs-container">
      <button 
        v-for="tab in tabs" 
        :key="tab"
        class="tab-btn"
        :class="activeTab === tab ? 'active' : 'inactive'"
        @click="changeTab(tab)"
      >
        {{ tab }}
      </button>
    </div>

    <div class="options-list">
      <transition-group name="fade-list">
        <div 
          v-for="item in filteredItems" 
          :key="item.id"
          class="option-card" 
          :class="{ 'is-active': selectedOption?.id === item.id }"
          @click="selectedOption = item"
        >
          <div :class="['icon-box', item.color]">
            <svg v-if="item.icon === 'ticket'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"></path><line x1="4" y1="22" x2="4" y2="15"></line></svg>
            
            <svg v-else-if="item.icon === 'pass'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
            
            <svg v-else-if="item.icon === 'pack'" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 2 7 12 12 22 7 12 2"></polygon><polyline points="2 17 12 22 22 17"></polyline><polyline points="2 12 12 17 22 12"></polyline></svg>
          </div>
          
          <div class="text-box">
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
          
          <div class="check-pill" v-if="selectedOption?.id === item.id">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
          </div>
        </div>
      </transition-group>
    </div>

    <transition name="slide-up">
      <div v-if="selectedOption" class="bottom-bar-container">
        <div class="bottom-bar">
          <div class="action-info">
            <span class="action-label">Selecionaste</span>
            <span class="action-value">{{ selectedOption.title }}</span>
          </div>
          <button class="btn-continuar" @click="avancar">
            Continuar
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
          </button>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Estado dos Filtros
const tabs = ['Todos', 'Bilhetes', 'Passes', 'Packs']
const activeTab = ref('Todos')

// Estado da Seleção
const selectedOption = ref(null)

// Base de dados das opções atualizada
const items = [
  { id: 'b1', type: 'Bilhetes', title: 'Bilhete Simples', desc: 'Válido para uma viagem', icon: 'ticket', color: 'blue' },
  { id: 'p1', type: 'Passes', title: 'Passe Mensal', desc: 'Acesso ilimitado durante 30 dias', icon: 'pass', color: 'green' },
  { id: 'p2', type: 'Passes', title: 'Passe Anual', desc: 'Acesso ilimitado durante 1 ano', icon: 'pass', color: 'green' },
  { id: 'pk1', type: 'Packs', title: 'Pack 10 Viagens', desc: 'Compra 10 viagens com desconto', icon: 'pack', color: 'purple' },
  { id: 'pk2', type: 'Packs', title: 'Pack 20 Viagens', desc: 'Compra 20 viagens com desconto', icon: 'pack', color: 'purple' }
]

// Filtra a lista com base na tab selecionada
const filteredItems = computed(() => {
  if (activeTab.value === 'Todos') {
    return items
  }
  return items.filter(item => item.type === activeTab.value)
})

// Muda a tab e limpa a seleção para não haver confusão de UI
const changeTab = (tab) => {
  activeTab.value = tab
  selectedOption.value = null
}

const avancar = () => {
  if (selectedOption.value.id === 'b1') {
    router.push('/search') // Bilhete Simples
  } else if (selectedOption.value.id === 'p1') {
    router.push('/buy-monthpass') // Passe Mensal
  } else if (selectedOption.value.id === 'p2') {
    router.push('/buy-anualpass') // Passe Anual
  }else if (selectedOption.value.id === 'pk1') {
    router.push('/buy-pack10') // Pack 10 Viagens
  }else if (selectedOption.value.id === 'pk2') {
    router.push('/buy-pack20') // Pack 20 Viagens
  }else {
    alert(`O fluxo para "${selectedOption.value.title}" está em construção!`)
  }
}
</script>

<style scoped>
.page {
  background: #FFFFFF;
  min-height: 100vh;
  padding: 38px 28px 180px 28px;
  font-family: 'Roboto', sans-serif;
  box-sizing: border-box;
}

/* --- HEADER & TÍTULO --- */
.header {
  margin-bottom: 24px;
}

.back-btn {
  background: #F2F4F7;
  border: none;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}
.back-btn:active { background: #E4E7EB; }

.title-section {
  margin-bottom: 24px;
}

.titulo {
  font-weight: 700;
  font-size: 30px;
  color: #0085FF; /* Azul conforme pedido */
  margin: 0;
  letter-spacing: -0.4px;
}

/* --- TABS HORIZONTAIS --- */
.tabs-container {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
  margin-bottom: 24px;
  scrollbar-width: none; /* Firefox */
}
.tabs-container::-webkit-scrollbar {
  display: none; /* Safari e Chrome */
}

.tab-btn {
  height: 47px;
  padding: 0 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.tab-btn.active {
  background: #0085FF;
  color: #FFFFFF;
  border: 1px solid #0085FF;
}

.tab-btn.inactive {
  background: transparent;
  color: #15171A;
  border: 1px solid #0085FF;
}

/* --- LISTA DE OPÇÕES --- */
.options-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-card {
  background: #F2F4F7;
  border: 2px solid transparent;
  border-radius: 18px;
  padding: 16px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.option-card.is-active {
  background: #FFFFFF;
  border-color: #0085FF;
  box-shadow: 0px 8px 24px rgba(0, 133, 255, 0.08);
  transform: scale(1.02);
}

.icon-box {
  width: 46px;
  height: 46px;
  border-radius: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: 16px;
}
.icon-box.blue   { background: #EAF4FF; color: #0085FF; }
.icon-box.green  { background: #EAF7EE; color: #1F9D4D; }
.icon-box.purple { background: #F1ECFB; color: #7A3FF2; }

.text-box {
  flex-grow: 1;
}

.text-box h3 {
  font-size: 16px;
  font-weight: 700;
  color: #15171A;
  margin: 0 0 4px 0;
}

.text-box p {
  font-size: 13px;
  color: #6B7077;
  margin: 0;
}

.check-pill {
  width: 46px;
  height: 24px;
  background: #E7F6EC;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1F9D4D;
  animation: popIn 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes popIn {
  0% { transform: scale(0.5); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

/* Animações de Filtragem da Lista */
.fade-list-move,
.fade-list-enter-active,
.fade-list-leave-active {
  transition: all 0.3s ease;
}
.fade-list-enter-from,
.fade-list-leave-to {
  opacity: 0;
  transform: translateY(15px);
}
.fade-list-leave-active {
  position: absolute;
}

/* --- BARRA FLUTUANTE DE AÇÃO --- */
.bottom-bar-container {
  position: fixed;
  bottom: 100px;
  left: 20px;
  right: 20px;
  z-index: 999;
}

.bottom-bar {
  background: #FFFFFF;
  border: 1px solid #E4E7EB;
  box-shadow: 0px 12px 32px rgba(16, 24, 40, 0.12);
  border-radius: 20px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-info {
  display: flex;
  flex-direction: column;
}

.action-label {
  font-size: 12px;
  color: #6B7077;
  margin-bottom: 4px;
}

.action-value {
  font-size: 15px;
  font-weight: 700;
  color: #15171A;
}

.btn-continuar {
  background: #FFFFFF;
  color: #0085FF;
  border: 2px solid #0085FF;
  padding: 12px 20px;
  border-radius: 999px;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-continuar:active {
  background: #EAF4FF;
  transform: scale(0.95);
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(40px);
}
</style>