import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import MonitorView from '@/views/MonitorView.vue'
import ShipmentsView from '@/views/ShipmentsView.vue'
import StoreDashboardView from '@/views/StoreDashboardView.vue'
import ReportsView from '@/views/ReportsView.vue'
import LoginView from '@/views/LoginView.vue'
import { isAuthenticated } from '@/api/authService'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', name: 'login', component: LoginView, meta: { publica: true } },
    { path: '/', name: 'dashboard', component: DashboardView },
    { path: '/store-dashboard', name: 'store-dashboard', component: StoreDashboardView },
    { path: '/monitor', name: 'monitor', component: MonitorView },
    { path: '/shipments', name: 'shipments', component: ShipmentsView },
    { path: '/reports', name: 'reports', component: ReportsView },
  ],
})

router.beforeEach((to) => {
  if (to.meta.publica) return true
  if (!isAuthenticated()) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
