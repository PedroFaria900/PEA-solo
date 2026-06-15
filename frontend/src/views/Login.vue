<template>
  <div class="login-wrapper">
    
    <div class="login-header">
      <h1 class="title">Bem Vindo</h1>
      <p class="subtitle">Login para entrar na conta</p>
    </div>

    <form class="login-form" @submit.prevent="handleLogin">
      
      <div class="input-group" :class="{ 'has-error': emailError }">
        <label for="email">Email</label>
        <input 
          id="email" 
          v-model="email" 
          type="email" 
          @input="emailError = false; apiError = ''"
        />
        <span v-if="emailError" class="error-message">Email é necessário</span>
      </div>

      <div class="input-group" :class="{ 'has-error': passwordError }">
        <label for="password">Password</label>
        <input 
          id="password" 
          v-model="password" 
          type="password" 
          @input="passwordError = false; apiError = ''"
        />
        <span v-if="passwordError" class="error-message">Password é necessário</span>
      </div>

      <div v-if="apiError" class="api-error-box">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
        </svg>
        {{ apiError }}
      </div>

      <div class="actions">
        <button type="submit" class="btn-primary" :disabled="loading">{{ loading ? 'A entrar...' : 'Entrar' }}</button>
        <router-link to="/register" class="btn-secondary">Criar Conta</router-link>
      </div>

      <div class="forgot-password">
        Esqueceu-se da senha?<br/>
        <a href="#">Clique Aqui</a>
      </div>

    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const emailError = ref(false)
const passwordError = ref(false)
const apiError = ref('')
const loading = ref(false)

const handleLogin = async () => {
  emailError.value = !email.value
  passwordError.value = !password.value
  apiError.value = ''
  if (!email.value || !password.value) return

  loading.value = true
  try {
    await authStore.login(email.value, password.value)
    router.push('/profile')
  } catch (err) {
    apiError.value = authStore.error || 'Email ou password incorretos.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* Contentor principal da página */
.login-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #ffffff;
  padding: 0 24px;
}

/* Cabeçalho */
.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  color: #007AFF; /* Azul do mockup */
  font-size: 36px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.subtitle {
  color: #666666;
  font-size: 18px;
  margin: 0;
}

/* Formulário */
.login-form {
  width: 100%;
  max-width: 340px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* Grupo de Inputs (Label flutuante) */
.input-group {
  position: relative;
  display: flex;
  flex-direction: column;
}

.input-group label {
  position: absolute;
  top: -9px;
  left: 12px;
  background-color: #ffffff;
  padding: 0 4px;
  font-size: 13px;
  color: #666666;
  pointer-events: none; /* Para não bloquear o clique no input */
}

.input-group input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #333333;
  border-radius: 8px;
  font-size: 16px;
  outline: none;
  background: transparent;
  transition: border-color 0.2s;
}

.input-group input:focus {
  border-color: #007AFF;
}

/* Estilos de Erro */
.input-group.has-error input {
  border-color: #D32F2F;
}

.input-group.has-error label {
  color: #D32F2F;
}

.error-message {
  color: #D32F2F;
  font-size: 13px;
  margin-top: 6px;
  margin-left: 4px;
}

.api-error-box {
  background-color: #FEECEB;
  color: #D32F2F;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  margin-bottom: 8px;
  border: 1px solid #F8D7DA;
}

/* Botões */
.actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 8px;
}

.btn-primary {
  background-color: #007AFF;
  color: #ffffff;
  border: none;
  border-radius: 30px;
  padding: 14px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  text-align: center;
  transition: background-color 0.2s;
}

.btn-primary:hover {
  background-color: #005bb5;
}

.btn-secondary {
  background-color: #ffffff;
  color: #007AFF;
  border: 1px solid #007AFF;
  border-radius: 30px;
  padding: 14px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  text-decoration: none;
  text-align: center;
  transition: background-color 0.2s;
}

.btn-secondary:hover {
  background-color: #f0f7ff;
}

/* Footer Links */
.forgot-password {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #666666;
  line-height: 1.5;
}

.forgot-password a {
  color: #007AFF;
  text-decoration: none;
}

.forgot-password a:hover {
  text-decoration: underline;
}
</style>