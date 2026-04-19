<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import Breadcrumb from '@/components/layout/Breadcrumb.vue'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { fetchMonitor } from '@/api/networkService'
import type { RawMonitorRede } from '@/api/types'
import { formatDataHora } from '@/lib/format'

const monitor = ref<RawMonitorRede | null>(null)
const loading = ref(true)
const erro = ref<string | null>(null)

async function carregar() {
  loading.value = true
  erro.value = null
  try {
    monitor.value = await fetchMonitor()
  } catch (e: any) {
    erro.value = e?.message ?? 'Falha ao carregar monitor'
  } finally {
    loading.value = false
  }
}

onMounted(carregar)

const percentagemRecebidas = computed(() => {
  if (!monitor.value || monitor.value.lojasEsperadas === 0) return 0
  return Math.round((monitor.value.lojasRecebidas / monitor.value.lojasEsperadas) * 100)
})
</script>

<template>
  <div class="flex flex-col min-h-screen bg-[#f8fafc]">
    <main class="p-10 pt-8 space-y-8">
      <Breadcrumb :items="['Supervisão', 'Receção de Dados']" class="mb-0" />

      <header class="flex items-start justify-between">
        <div>
          <h1 class="text-[28px] font-bold text-[#0f172a] leading-tight">Monitor de Receção de Dados</h1>
          <p class="text-sm text-[#64748b] font-medium">
            Estado da sincronização diária de {{ monitor?.lojasEsperadas ?? 0 }} lojas
          </p>
        </div>
        <button
          @click="carregar"
          class="px-4 py-2 bg-white border border-slate-200 rounded-md text-xs font-bold text-[#0f172a] hover:bg-slate-50 shadow-sm"
        >
          Atualizar
        </button>
      </header>

      <p v-if="erro" class="text-sm text-red-600">{{ erro }}</p>
      <p v-else-if="loading" class="text-sm text-slate-500">A carregar…</p>

      <template v-else-if="monitor">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Lojas Esperadas</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[32px] font-bold text-[#0f172a]">{{ monitor.lojasEsperadas }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Recebidas com Sucesso</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[32px] font-bold text-[#0f172a]">{{ monitor.lojasRecebidas }}</div>
              <div class="mt-2">
                <span class="bg-[#dcfce7] text-[#16a34a] text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-tight">
                  {{ percentagemRecebidas }}%
                </span>
              </div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Em Falta</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[32px] font-bold text-[#0f172a]">{{ monitor.lojasPendentes }}</div>
              <div v-if="monitor.lojasPendentes > 0" class="mt-2">
                <span class="bg-[#fee2e2] text-[#ef4444] text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-tight">
                  Requer Atenção
                </span>
              </div>
            </CardContent>
          </Card>
        </div>

        <div class="border rounded-lg bg-white shadow-none overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow class="bg-[#f4f4f5] hover:bg-[#f4f4f5] border-b border-slate-200">
                <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Loja</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Hora Receção</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Estado</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow
                v-for="l in monitor.estadoPorLoja"
                :key="l.idLoja"
                class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12"
              >
                <TableCell class="text-sm font-bold text-[#0f172a]">{{ l.nomeLoja }}</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatDataHora(l.horaRecepcao) }}</TableCell>
                <TableCell>
                  <Badge
                    v-if="l.estado === 'received'"
                    class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-2 py-0.5 border-none shadow-none"
                  >
                    Recebido
                  </Badge>
                  <Badge
                    v-else
                    class="bg-[#fee2e2] text-[#ef4444] text-[11px] font-bold px-2 py-0.5 border-none shadow-none"
                  >
                    Pendente
                  </Badge>
                </TableCell>
              </TableRow>
              <TableRow v-if="monitor.estadoPorLoja.length === 0">
                <TableCell colspan="3" class="text-sm text-slate-400 text-center py-6">Sem lojas registadas</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </template>
    </main>
  </div>
</template>
