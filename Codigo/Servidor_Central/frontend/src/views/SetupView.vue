<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { criarCEO, logout } from '@/api/authService'

const router = useRouter()
const nomeUtilizador = ref('')
const palavraPasse = ref('')
const confirmarPalavraPasse = ref('')
const erro = ref<string | null>(null)
const loading = ref(false)

async function submeter() {
  erro.value = null
  if (palavraPasse.value !== confirmarPalavraPasse.value) {
    erro.value = 'As palavras-passe não coincidem.'
    return
  }
  loading.value = true
  try {
    await criarCEO(nomeUtilizador.value, palavraPasse.value)
    await logout()
    router.replace({ name: 'login', query: { setup: 'ok' } })
  } catch (e) {
    erro.value = e instanceof Error ? e.message : 'Erro ao criar conta.'
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
        <p class="text-[11px] font-bold text-slate-400 uppercase tracking-widest">Configuração Inicial</p>
      </div>

      <div class="mb-5 rounded-lg bg-[#eff6ff] border border-[#bfdbfe] p-3">
        <p class="text-xs text-[#1e40af] leading-relaxed">
          Está a utilizar a conta de instalação temporária. Crie uma conta definitiva para continuar — após este passo a conta temporária será eliminada.
        </p>
      </div>

      <form @submit.prevent="submeter" class="space-y-4">
        <div class="space-y-1">
          <label class="text-xs font-semibold text-slate-600 uppercase tracking-wide">Utilizador</label>
          <input v-model="nomeUtilizador" type="text" required autocomplete="username"
                 class="w-full px-3 py-2 border border-slate-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-[#2563eb]/30 focus:border-[#2563eb]" />
        </div>
        <div class="space-y-1">
          <label class="text-xs font-semibold text-slate-600 uppercase tracking-wide">Palavra-passe</label>
          <input v-model="palavraPasse" type="password" required autocomplete="new-password"
                 class="w-full px-3 py-2 border border-slate-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-[#2563eb]/30 focus:border-[#2563eb]" />
        </div>
        <div class="space-y-1">
          <label class="text-xs font-semibold text-slate-600 uppercase tracking-wide">Confirmar palavra-passe</label>
          <input v-model="confirmarPalavraPasse" type="password" required autocomplete="new-password"
                 class="w-full px-3 py-2 border border-slate-200 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-[#2563eb]/30 focus:border-[#2563eb]" />
        </div>

        <p v-if="erro" class="text-xs text-[#ef4444] font-semibold">{{ erro }}</p>

        <button type="submit" :disabled="loading"
                class="w-full bg-[#2563eb] hover:bg-[#1d4ed8] disabled:opacity-60 disabled:cursor-not-allowed text-white text-sm font-semibold py-2 rounded-md transition-colors">
          {{ loading ? 'A criar conta…' : 'Criar conta' }}
        </button>
      </form>
    </div>
  </div>
</template>
