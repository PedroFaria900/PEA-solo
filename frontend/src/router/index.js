import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import Dashboard from '../views/Dashboard.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Buy from '../views/Buy.vue'
import ComprarBilhetes from '../views/ComprarBilhetes.vue'
import Simulator from '../views/Simulator.vue'
import Trips from '../views/Trips.vue'
import Search from '../views/Search.vue'
import Profile from '../views/Profile.vue'
import Wallet from '../views/Wallet.vue'
import ChargeWallet from '../views/ChargeWallet.vue'

const routes = [
  {
    path: '/',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { guest: true }
  },
  {
    path: '/buy',
    name: 'Buy',
    component: Buy,
    meta: { requiresAuth: true }
  },
  {
    path: '/simulator',
    name: 'Simulator',
    component: Simulator,
    meta: { requiresAuth: true }
  },
  {
    path: '/trips',
    name: 'Trips',
    component: Trips,
    meta: { requiresAuth: false }
  },
  {
    path: '/search',
    name: 'Search',
    component: Search,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { requiresAuth: true }
  },
  {
    path: '/wallet',
    name: 'Wallet',
    component: Wallet,
    meta: { requiresAuth: true }
  },
  {
    path: '/wallet/charge',
    name: 'ChargeWallet',
    component: ChargeWallet,
    meta: { requiresAuth: true }
  },
  {
    path: '/comprar-bilhetes',
    name: 'ComprarBilhetes',
    component: ComprarBilhetes,
    meta: { requiresAuth: true }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // Try to load profile if token exists but user is null
  if (authStore.token && !authStore.user) {
    await authStore.fetchProfile()
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login' })
  } else if (to.meta.guest && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
