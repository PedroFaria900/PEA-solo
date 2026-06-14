import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: null,
    loading: false,
    error: null,
  }),
  getters: {
    isAuthenticated: (state) => !!state.token,
    userName: (state) => state.user?.nome || '',
    userEmail: (state) => state.user?.email || '',
    userSaldo: (state) => state.user?.saldo || 0,
  },
  actions: {
    setToken(token) {
      this.token = token
      if (token) {
        localStorage.setItem('token', token)
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      } else {
        localStorage.removeItem('token')
        delete axios.defaults.headers.common['Authorization']
      }
    },
    async register(nome, email, telemovel, password) {
      this.loading = true
      this.error = null
      try {
        await axios.post('/api/auth/register', { nome, email, telemovel, password })
        this.loading = false
        return true
      } catch (err) {
        this.error = err.response?.data || 'Erro ao efetuar registo'
        this.loading = false
        throw err
      }
    },
    async login(email, password) {
      this.loading = true
      this.error = null
      try {
        const res = await axios.post('/api/auth/login', { email, password })
        const token = res.data.accessToken
        this.setToken(token)
        await this.fetchProfile()
        this.loading = false
        return true
      } catch (err) {
        this.error = err.response?.data?.message || 'Email ou palavra-passe incorretos'
        this.loading = false
        throw err
      }
    },
    async fetchProfile() {
      if (!this.token) return
      try {
        axios.defaults.headers.common['Authorization'] = `Bearer ${this.token}`
        const res = await axios.get('/api/utentes/perfil')
        this.user = res.data
      } catch (err) {
        if (err.response?.status === 403 || err.response?.status === 401) {
          this.logout()
        }
      }
    },
    async carregarSaldo(valor) {
      try {
        const res = await axios.post('/api/carteira/carregamentos', { valor })
        if (this.user) {
          this.user.saldo = res.data.saldo
        }
        return res.data.saldo
      } catch (err) {
        this.error = err.response?.data?.message || 'Erro ao carregar saldo'
        throw err
      }
    },
    logout() {
      this.user = null
      this.setToken(null)
    }
  }
})
