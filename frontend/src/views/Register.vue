<template>
  <div class="auth-page">
    <div class="auth-logo">
      <h1>🚇 Bilhética</h1>
      <p>Cria a tua conta de utente</p>
    </div>

    <div v-if="authStore.error" class="alert alert-error">⚠️ {{ authStore.error }}</div>
    <div v-if="successMsg" class="alert alert-success">✅ {{ successMsg }}</div>

    <form @submit.prevent="handleRegister">
      <div class="form-group">
        <label class="form-label" for="nome">Nome Completo</label>
        <input type="text" id="nome" v-model="nome" class="form-control" placeholder="Maria Silva" required />
      </div>
      <div class="form-group">
        <label class="form-label" for="email">E-mail</label>
        <input type="email" id="email" v-model="email" class="form-control" placeholder="exemplo@email.com" required />
      </div>
      <div class="form-group">
        <label class="form-label" for="telemovel">Telemóvel</label>
        <input type="tel" id="telemovel" v-model="telemovel" class="form-control" placeholder="+351 912 345 678" required />
      </div>
      <div class="form-group">
        <label class="form-label" for="password">Palavra-passe</label>
        <input type="password" id="password" v-model="password" class="form-control" placeholder="••••••••" required />
      </div>
      <button type="submit" class="btn btn-primary btn-block" :disabled="authStore.loading" style="margin-top: 8px;">
        {{ authStore.loading ? 'A criar conta...' : 'Criar Conta' }}
      </button>
    </form>

    <p style="text-align: center; margin-top: 24px; font-size: 0.9rem; color: var(--text-muted);">
      Já tens conta?
      <router-link to="/login" style="color: var(--blue); font-weight: 600;">Entrar aqui</router-link>
    </p>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'

export default {
  name: 'Register',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()
    const nome = ref(''), email = ref(''), telemovel = ref(''), password = ref(''), successMsg = ref('')

    const handleRegister = async () => {
      try {
        await authStore.register(nome.value, email.value, telemovel.value, password.value)
        successMsg.value = 'Conta criada! Redirecionando...'
        setTimeout(() => router.push({ name: 'Login' }), 1500)
      } catch (err) { /* handled in store */ }
    }

    return { nome, email, telemovel, password, successMsg, authStore, handleRegister }
  }
}
</script>
