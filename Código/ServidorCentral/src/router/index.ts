import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import MonitorView from '@/views/MonitorView.vue'
import ShipmentsView from '@/views/ShipmentsView.vue'
import StoreDashboardView from '@/views/StoreDashboardView.vue'
import ReportsView from '@/views/ReportsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView
    },
    {
      path: '/store-dashboard',
      name: 'store-dashboard',
      component: StoreDashboardView
    },
    {
      path: '/monitor',
      name: 'monitor',
      component: MonitorView
    },
    {
      path: '/shipments',
      name: 'shipments',
      component: ShipmentsView
    },
    {
      path: '/reports',
      name: 'reports',
      component: ReportsView
    }
  ]
})

export default router
