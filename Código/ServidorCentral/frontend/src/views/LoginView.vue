<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/authService'

const MAX_TENTATIVAS = 5
const DURACAO_BLOQUEIO_MS = 15 * 60 * 1000

const CHAVE_TENTATIVAS = 'trasmum.login.tentativas'
const CHAVE_DESBLOQUEIO = 'trasmum.login.desbloqueio'

const router = useRouter()
const nomeUtilizador = ref('')
const palavraPasse = ref('')
const erro = ref<string | null>(null)
const loading = ref(false)
const restanteMs = ref(0)

let intervalo: ReturnType<typeof setInterval> | null = null

function tentativasFalhadas(): number {
  return parseInt(localStorage.getItem(CHAVE_TENTATIVAS) ?? '0', 10)
}

function tempoDesbloqueio(): number {
  return parseInt(localStorage.getItem(CHAVE_DESBLOQUEIO) ?? '0', 10)
}

const bloqueado = computed(() => restanteMs.value > 0)

const contagemRegressiva = computed(() => {
  const min = Math.floor(restanteMs.value / 60000)
  const seg = Math.floor((restanteMs.value % 60000) / 1000)
  return `${min}:${String(seg).padStart(2, '0')}`
})

const percentagemRestante = computed(() =>
  Math.max(0, Math.min(100, (restanteMs.value / DURACAO_BLOQUEIO_MS) * 100))
)

function atualizarContagem() {
  const r = tempoDesbloqueio() - Date.now()
  if (r <= 0) {
    restanteMs.value = 0
    localStorage.removeItem(CHAVE_TENTATIVAS)
    localStorage.removeItem(CHAVE_DESBLOQUEIO)
    erro.value = null
    if (intervalo !== null) { clearInterval(intervalo); intervalo = null }
  } else {
    restanteMs.value = r
  }
}

function iniciarCronometro() {
  atualizarContagem()
  intervalo = setInterval(atualizarContagem, 1000)
}

onMounted(() => {
  restanteMs.value = Math.max(0, tempoDesbloqueio() - Date.now())
  if (restanteMs.value > 0) iniciarCronometro()
})

onUnmounted(() => {
  if (intervalo !== null) clearInterval(intervalo)
})

async function submeter() {
  if (bloqueado.value) return
  erro.value = null
  loading.value = true
  try {
    await login(nomeUtilizador.value, palavraPasse.value)
    localStorage.removeItem(CHAVE_TENTATIVAS)
    localStorage.removeItem(CHAVE_DESBLOQUEIO)
    const redirect = (router.currentRoute.value.query.redirect as string) || '/'
    router.replace(redirect)
  } catch (e) {
    const novasTentativas = tentativasFalhadas() + 1
    localStorage.setItem(CHAVE_TENTATIVAS, String(novasTentativas))
    if (novasTentativas >= MAX_TENTATIVAS) {
      localStorage.setItem(CHAVE_DESBLOQUEIO, String(Date.now() + DURACAO_BLOQUEIO_MS))
      iniciarCronometro()
    } else {
      const restantes = MAX_TENTATIVAS - novasTentativas
      const msg = e instanceof Error ? e.message : 'Erro ao iniciar sessão'
      erro.value = `${msg} (${restantes} tentativa(s) restante(s))`
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-[#f8fafc] px-4">
    <div class="w-full max-w-sm bg-white border border-slate-200 rounded-xl shadow-sm p-8">
      <div class="mb-6">
        <h1 class="font-black text-2xl tracking-tighter text-[#0f172a]">TrasmUM</h1>
        <p class="text-[11px] font-bold text-slate-400 uppercase tracking-widest">Analítica Central</p>
      </div>

      <form @submit.prevent="submeter" class="space-y-4">
        <div class="space-y-1">
          <label class="text-xs font-semibold text-slate-600 uppercase tracking-wide">Utilizador</label>
          <input v-model="nomeUtilizador" type="text" required autocomplete="username"
                 :disabled="bloqueado"
                 class="w-full px-3 py-2 border border-slate-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-[#2563eb]/30 focus:border-[#2563eb] disabled:opacity-50 disabled:cursor-not-allowed" />
        </div>
        <div class="space-y-1">
          <label class="text-xs font-semibold text-slate-600 uppercase tracking-wide">Palavra-passe</label>
          <input v-model="palavraPasse" type="password" required autocomplete="current-password"
                 :disabled="bloqueado"
                 class="w-full px-3 py-2 border border-slate-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-[#2563eb]/30 focus:border-[#2563eb] disabled:opacity-50 disabled:cursor-not-allowed" />
        </div>

        <div v-if="bloqueado" class="rounded-lg bg-[#fef2f2] border border-[#fecaca] p-4 space-y-3">
          <div class="flex items-center gap-2 text-[#dc2626]">
            <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <rect x="3" y="11" width="18" height="11" rx="2"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
            </svg>
            <span class="text-xs font-bold uppercase tracking-wide">Acesso temporariamente bloqueado</span>
          </div>
          <p class="text-xs text-[#7f1d1d]">Demasiadas tentativas falhadas. O acesso será restaurado automaticamente.</p>
          <div class="text-center">
            <span class="font-mono text-2xl font-black text-[#dc2626] tabular-nums tracking-tight">{{ contagemRegressiva }}</span>
          </div>
          <div class="w-full bg-[#fecaca] rounded-full h-1.5 overflow-hidden">
            <div class="h-full bg-[#dc2626] rounded-full transition-all duration-1000"
                 :style="{ width: percentagemRestante + '%' }" />
          </div>
        </div>
        <p v-else-if="erro" class="text-xs text-[#ef4444] font-semibold">{{ erro }}</p>

        <button type="submit" :disabled="loading || bloqueado"
                class="w-full bg-[#2563eb] hover:bg-[#1d4ed8] disabled:opacity-60 disabled:cursor-not-allowed text-white text-sm font-semibold py-2 rounded-md transition-colors">
          {{ loading ? 'A entrar…' : 'Entrar' }}
        </button>
      </form>
    </div>
  </div>
</template>
