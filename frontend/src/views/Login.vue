<template>
  <div class="auth-page">
    <div class="auth-logo">
      <h1>🚇 Bilhética</h1>
      <p>Sistema de Transportes Públicos</p>
    </div>

    <div v-if="authStore.error" class="alert alert-error">
      ⚠️ {{ authStore.error }}
    </div>

    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label class="form-label" for="email">E-mail</label>
        <input
          type="email"
          id="email"
          v-model="email"
          class="form-control"
          placeholder="exemplo@email.com"
          required
        />
      </div>

      <div class="form-group">
        <label class="form-label" for="password">Palavra-passe</label>
        <input
          type="password"
          id="password"
          v-model="password"
          class="form-control"
          placeholder="••••••••"
          required
        />
      </div>

      <button type="submit" class="btn btn-primary btn-block" :disabled="authStore.loading" style="margin-top: 8px;">
        {{ authStore.loading ? 'A entrar...' : 'Entrar' }}
      </button>
    </form>

    <p style="text-align: center; margin-top: 24px; font-size: 0.9rem; color: var(--text-muted);">
      Não tens conta?
      <router-link to="/register" style="color: var(--blue); font-weight: 600;">Criar conta</router-link>
    </p>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useAuthStore } from '../store/auth'
import { useRouter } from 'vue-router'

export default {
  name: 'Login',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()
    const email = ref('')
    const password = ref('')

    const handleLogin = async () => {
      try {
        await authStore.login(email.value, password.value)
        router.push({ name: 'Dashboard' })
      } catch (err) { /* handled in store */ }
    }

    return { email, password, authStore, handleLogin }
  }
}
</script>
