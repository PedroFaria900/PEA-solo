<template>
  <div class="register-wrapper">
    
    <div class="register-header">
      <h1 class="title">Criar Conta</h1>
      <p class="subtitle">Crie a sua conta para começar</p>
    </div>

    <form class="register-form" @submit.prevent="handleRegister">
      
      <div class="input-group" :class="{ 'has-error': nameError }">
        <label for="name">Nome</label>
        <input id="name" v-model="name" type="text" @input="nameError = false" />
        <span v-if="nameError" class="error-message">Nome é necessário</span>
      </div>

      <div class="input-group" :class="{ 'has-error': emailError }">
        <label for="email">Email</label>
        <input id="email" v-model="email" type="email" @input="emailError = false" />
        <span v-if="emailError" class="error-message">Email é necessário</span>
      </div>

      <div class="input-group" :class="{ 'has-error': passwordError }">
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password"
          @input="passwordError = false; checkPasswords()" />
        <span v-if="passwordError" class="error-message">Password é necessário</span>
      </div>

      <div class="input-group" :class="{ 'has-error': confirmPasswordError }">
        <label for="confirmPassword">Confirmar Password</label>
        <input id="confirmPassword" v-model="confirmPassword" type="password"
          @input="confirmPasswordError = false; checkPasswords()" />
        <span v-if="confirmPasswordError" class="error-message">{{ confirmPasswordErrorMsg }}</span>
      </div>

      <div class="input-group" :class="{ 'has-error': phoneError }">
        <label for="phone">Telemóvel</label>
        <input id="phone" v-model="phone" type="tel" @input="phoneError = false" />
        <span v-if="phoneError" class="error-message">Telemóvel é necessário</span>
      </div>

      <!-- Tipo de utente -->
      <div class="input-group" :class="{ 'has-error': tipoError }">
        <label class="label-tipo">Tipo de Utente</label>
        <div class="tipo-selector">
          <button
            v-for="opcao in tipoOpcoes"
            :key="opcao.value"
            type="button"
            class="tipo-btn"
            :class="{ ativo: tipo === opcao.value }"
            @click="tipo = opcao.value; tipoError = false"
          >
            {{ opcao.emoji }} {{ opcao.label }}
          </button>
        </div>
        <span v-if="tipoError" class="error-message">Selecione o tipo de utente</span>
      </div>

      <div v-if="apiError" class="error-message" style="text-align:center; margin-bottom: 4px;">{{ apiError }}</div>

      <div class="actions">
        <button type="submit" class="btn-primary" :disabled="loading">{{ loading ? 'A criar conta...' : 'Criar Conta' }}</button>
      </div>

      <div class="login-link">
        Já tem conta?<br/>
        <router-link to="/login">Clique Aqui</router-link>
      </div>

    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const name = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const phone = ref('')
const tipo = ref('')

const nameError = ref(false)
const emailError = ref(false)
const passwordError = ref(false)
const confirmPasswordError = ref(false)
const confirmPasswordErrorMsg = ref('')
const phoneError = ref(false)
const tipoError = ref(false)
const apiError = ref('')
const loading = ref(false)

const tipoOpcoes = [
  { value: 'NORMAL',    label: 'Normal'},
  { value: 'ESTUDANTE', label: 'Estudante'},
  { value: 'SENIOR',    label: 'Sénior'},
]

const checkPasswords = () => {
  if (confirmPasswordError.value && password.value === confirmPassword.value) {
    confirmPasswordError.value = false
  }
}

const handleRegister = async () => {
  nameError.value = false
  emailError.value = false
  passwordError.value = false
  confirmPasswordError.value = false
  phoneError.value = false
  tipoError.value = false
  apiError.value = ''
  let hasError = false

  if (!name.value)  { nameError.value = true;  hasError = true }
  if (!email.value) { emailError.value = true;  hasError = true }
  if (!password.value) { passwordError.value = true; hasError = true }
  if (!phone.value) { phoneError.value = true;  hasError = true }
  if (!tipo.value)  { tipoError.value = true;   hasError = true }

  if (!confirmPassword.value) {
    confirmPasswordError.value = true
    confirmPasswordErrorMsg.value = 'Confirmação é necessária'
    hasError = true
  } else if (password.value !== confirmPassword.value) {
    confirmPasswordError.value = true
    confirmPasswordErrorMsg.value = 'As passwords não coincidem'
    hasError = true
  }

  if (hasError) return

  loading.value = true
  try {
    await axios.post('/api/auth/register', {
      nome: name.value,
      email: email.value,
      telemovel: phone.value,
      password: password.value,
      perfil: tipo.value
    })
    router.push({ name: 'Login' })
  } catch (err) {
    apiError.value = err.response?.data?.message || err.response?.data || 'Erro ao criar conta. Tente novamente.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #ffffff;
  padding: 40px 24px;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  color: #007AFF;
  font-size: 36px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.subtitle {
  color: #666666;
  font-size: 18px;
  margin: 0;
}

.register-form {
  width: 100%;
  max-width: 340px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Inputs — igual ao original */
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
  pointer-events: none;
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

.input-group.has-error input {
  border-color: #D32F2F;
}

.input-group.has-error label,
.input-group.has-error .label-tipo {
  color: #D32F2F;
}

.error-message {
  color: #D32F2F;
  font-size: 13px;
  margin-top: 6px;
  margin-left: 4px;
}

/* Seletor de tipo */
.label-tipo {
  font-size: 13px;
  color: #666666;
  margin-bottom: 10px;
  padding-left: 2px;
}

.tipo-selector {
  display: flex;
  gap: 8px;
}

.tipo-btn {
  flex: 1;
  padding: 10px 4px;
  border: 1px solid #333333;
  border-radius: 8px;
  background: transparent;
  font-size: 13px;
  font-weight: 600;
  color: #333333;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.18s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.tipo-btn:hover {
  border-color: #007AFF;
  color: #007AFF;
}

.tipo-btn.ativo {
  background: #007AFF;
  border-color: #007AFF;
  color: #ffffff;
}

/* Botões */
.actions {
  display: flex;
  flex-direction: column;
  margin-top: 12px;
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
  font-family: inherit;
}

.btn-primary:hover {
  background-color: #005bb5;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666666;
  line-height: 1.5;
}

.login-link a {
  color: #007AFF;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>