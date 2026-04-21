<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import Breadcrumb from '@/components/layout/Breadcrumb.vue'
import SalesChart from '@/components/features/dashboard/SalesChart.vue'
import DoughnutChart from '@/components/features/dashboard/DoughnutChart.vue'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { fetchLojas, fetchDashboardLoja } from '@/api/networkService'
import type { RawLoja, DashboardLoja } from '@/api/types'
import { formatEuro, formatInt, mensaisOrdenados, topCategorias, formatData, formatDataHora } from '@/lib/format'

const lojas = ref<RawLoja[]>([])
const selectedStore = ref<string>('')
const dados = ref<DashboardLoja | null>(null)
const loading = ref(false)
const erro = ref<string | null>(null)

onMounted(async () => {
  try {
    lojas.value = await fetchLojas()
    if (lojas.value.length > 0) {
      selectedStore.value = lojas.value[0].idLoja
    }
  } catch (e: any) {
    erro.value = e?.message ?? 'Falha ao carregar lojas'
  }
})

watch(selectedStore, async (id) => {
  if (!id) return
  loading.value = true
  erro.value = null
  try {
    dados.value = await fetchDashboardLoja(id)
  } catch (e: any) {
    erro.value = e?.message ?? 'Falha ao carregar dashboard da loja'
  } finally {
    loading.value = false
  }
})

const mensais = computed(() => mensaisOrdenados(dados.value?.vendasMensais))
const categorias = computed(() => topCategorias(dados.value?.vendasPorCategoria))
</script>

<template>
  <div class="flex flex-col min-h-screen bg-[#f8fafc]">
    <main class="p-10 pt-8 space-y-8">
      <Breadcrumb :items="['Supervisão', 'Dashboard de Loja']" class="mb-0" />

      <header class="flex items-start justify-between">
        <div>
          <h1 class="text-[28px] font-bold text-[#0f172a] leading-tight">Dashboard de Loja</h1>
          <p class="text-sm text-[#64748b] font-medium">Métricas detalhadas e histórico de atividade</p>
        </div>
        <div class="w-72">
          <Select v-model="selectedStore">
            <SelectTrigger class="w-full bg-white h-11 border-slate-200 text-sm font-medium">
              <SelectValue placeholder="Selecionar Loja" />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                <SelectItem v-for="l in lojas" :key="l.idLoja" :value="l.idLoja">{{ l.nomeLoja }}</SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>
      </header>

      <p v-if="erro" class="text-sm text-red-600">{{ erro }}</p>
      <p v-else-if="loading" class="text-sm text-slate-500">A carregar…</p>

      <template v-else-if="dados">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Vendas Totais</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold text-[#0f172a]">{{ formatEuro(dados.vendasTotais) }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Transações</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold text-[#0f172a]">{{ formatInt(dados.totalTransacoes) }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Ticket Médio</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold text-[#0f172a]">{{ formatEuro(dados.ticketMedio) }}</div>
            </CardContent>
          </Card>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Devoluções</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold text-[#0f172a]">{{ formatEuro(dados.totalDevolucoes) }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Despesas (Remessas)</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-2xl font-bold text-[#0f172a]">{{ formatEuro(dados.totalDespesas) }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Lucro Líquido</CardTitle>
            </CardHeader>
            <CardContent>
              <div
                class="text-2xl font-bold"
                :class="dados.lucroLiquido >= 0 ? 'text-[#16a34a]' : 'text-[#dc2626]'"
              >{{ formatEuro(dados.lucroLiquido) }}</div>
              <p class="text-[11px] text-[#94a3b8] font-medium mt-1">Vendas − Devoluções − Despesas</p>
            </CardContent>
          </Card>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 pt-2">
          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader>
              <CardTitle class="text-base font-bold text-[#0f172a]">Evolução Mensal de Vendas</CardTitle>
            </CardHeader>
            <CardContent>
              <SalesChart
                :store1Data="mensais.values"
                :store2Data="[]"
                :labels="mensais.labels"
                :store1Label="dados.nomeLoja"
                store2Label=""
              />
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader>
              <CardTitle class="text-base font-bold text-[#0f172a]">Top Categorias</CardTitle>
            </CardHeader>
            <CardContent class="flex items-center justify-center pt-4">
              <DoughnutChart :data="categorias.values" :labels="categorias.labels" />
            </CardContent>
          </Card>
        </div>

        <div class="space-y-4 pt-2">
          <h2 class="text-lg font-bold text-[#0f172a] bg-[#f1f5f9] w-fit px-2 py-0.5 rounded">Histórico de Fechos</h2>
          <div class="border rounded-lg bg-white shadow-none overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow class="bg-[#f4f4f5] hover:bg-[#f4f4f5] border-b border-slate-200">
                  <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Data Fecho</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Data Receção</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Estado Fecho</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow
                  v-for="f in dados.historicoFechos"
                  :key="f.id"
                  class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12"
                >
                  <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatData(f.dataFecho) }}</TableCell>
                  <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatDataHora(f.dataRecepcao) }}</TableCell>
                  <TableCell>
                    <Badge class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-2 py-0.5 border-none shadow-none">Recebido</Badge>
                  </TableCell>
                </TableRow>
                <TableRow v-if="dados.historicoFechos.length === 0">
                  <TableCell colspan="3" class="text-sm text-slate-400 text-center py-6">Sem fechos</TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </div>
        </div>

        <div class="space-y-4 pt-2">
          <h2 class="text-lg font-bold text-[#0f172a] bg-[#f1f5f9] w-fit px-2 py-0.5 rounded">Logs de Auditoria</h2>
          <div class="border rounded-lg bg-white shadow-none overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow class="bg-[#f4f4f5] hover:bg-[#f4f4f5] border-b border-slate-200">
                  <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Data / Hora</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Utilizador</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Ação</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow
                  v-for="log in dados.logsAuditoria"
                  :key="log.id"
                  class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12"
                >
                  <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatDataHora(log.dataHora) }}</TableCell>
                  <TableCell class="text-sm font-medium text-[#0f172a]">{{ log.nomeUtilizador }}</TableCell>
                  <TableCell>
                    <Badge variant="outline" class="bg-slate-50 text-slate-600 text-[11px] font-medium border-slate-200">
                      {{ log.acao }}
                    </Badge>
                  </TableCell>
                </TableRow>
                <TableRow v-if="dados.logsAuditoria.length === 0">
                  <TableCell colspan="3" class="text-sm text-slate-400 text-center py-6">Sem logs</TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>
