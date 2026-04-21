<script setup lang="ts">
import { LogOut } from 'lucide-vue-next'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { computed } from 'vue'
import { logout, utilizadorAtual } from '@/api/authService'

const route = useRoute()
const router = useRouter()

const navigation = [
  { name: 'Dashboard Global', href: '/' },
  { name: 'Dashboard de Loja', href: '/store-dashboard' },
  { name: 'Receção de Dados', href: '/monitor' },
  { name: 'Remessas da Rede', href: '/shipments' },
  { name: 'Relatórios Analíticos', href: '/reports' },
]

const utilizador = computed(() => utilizadorAtual() ?? '—')
const iniciais = computed(() => {
  const u = utilizadorAtual() ?? ''
  return u ? u.slice(0, 2).toUpperCase() : '—'
})

async function terminarSessao() {
  await logout()
  router.replace({ name: 'login' })
}
</script>

<template>
  <aside class="w-64 border-r border-slate-200 bg-white flex flex-col h-screen sticky top-0 z-50 shadow-[1px_0_5px_rgba(0,0,0,0.02)]">
    <!-- Brand -->
    <div class="p-7 pb-6 flex items-center gap-3">
      <img src="/logo.png" alt="TrasmUM" class="w-20 h-20 object-contain" />
      <div class="flex flex-col gap-0.5">
        <h2 class="font-black text-2xl tracking-tighter text-[#0f172a]">TrasmUM</h2>
        <p class="text-[11px] font-bold text-slate-400 uppercase tracking-widest">Analítica Central</p>
      </div>
    </div>
    
    <!-- Navigation -->
    <div class="flex-1 px-3 py-8 flex flex-col gap-8">
      <div class="space-y-4">
        <h3 class="px-4 text-[10px] font-extrabold text-slate-400 uppercase tracking-[0.2em]">Supervisão</h3>
        <nav class="space-y-0.5">
          <RouterLink
            v-for="item in navigation"
            :key="item.name"
            :to="item.href"
            class="flex items-center justify-between px-4 py-2.5 rounded-lg transition-all duration-200 group relative"
            :class="route.path === item.href ? 'text-[#2563eb]' : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900'"
          >
            <div class="flex items-center gap-3.5">
              <!-- Circle Indicator -->
              <span 
                class="w-[18px] h-[18px] rounded-full border-[1.5px] flex items-center justify-center transition-all duration-300"
                :class="route.path === item.href ? 'border-[#2563eb] bg-[#2563eb]/5' : 'border-slate-300 group-hover:border-slate-400'"
              >
                <span v-if="route.path === item.href" class="w-1.5 h-1.5 rounded-full bg-[#2563eb] animate-in zoom-in-50"></span>
              </span>
              
              <!-- Label with wrap fix -->
              <span class="text-[13.5px] font-medium whitespace-nowrap" :class="{ 'font-bold': route.path === item.href }">
                {{ item.name }}
              </span>
            </div>
            
            <!-- Active Arrow -->
            <span v-if="route.path === item.href" class="text-[#2563eb] text-lg font-light leading-none">›</span>
          </RouterLink>
        </nav>
      </div>
    </div>
    
    <!-- User Footer -->
    <div class="p-5 border-t border-slate-100 bg-slate-50/50">
      <div class="flex items-center gap-3">
        <div class="relative">
          <div class="w-10 h-10 rounded-full bg-slate-200 border-2 border-white shadow-sm flex items-center justify-center text-[10px] font-bold text-slate-500 overflow-hidden uppercase">
            {{ iniciais }}
          </div>
          <div class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full"></div>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-[13px] font-bold text-[#0f172a] truncate">{{ utilizador }}</p>
          <button @click="terminarSessao" class="text-[11px] font-bold text-[#ef4444] hover:text-red-700 transition-colors uppercase tracking-tight">Terminar Sessão</button>
        </div>
        <button @click="terminarSessao" class="p-2 hover:bg-red-50 rounded-lg transition-colors group">
          <LogOut :size="18" class="text-slate-400 group-hover:text-[#ef4444]" />
        </button>
      </div>
    </div>
  </aside>
</template>

<style scoped>
/* Transição suave para o active state */
.router-link-active {
  background-color: rgba(37, 99, 235, 0.03);
}
</style>
