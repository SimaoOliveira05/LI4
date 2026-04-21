<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import SalesChart from '@/components/features/dashboard/SalesChart.vue'
import DoughnutChart from '@/components/features/dashboard/DoughnutChart.vue'
import Breadcrumb from '@/components/layout/Breadcrumb.vue'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { fetchDashboardGlobal } from '@/api/networkService'
import { utilizadorAtual } from '@/api/authService'
import type { DashboardGlobal } from '@/api/types'
import { formatEuro, formatInt, formatPercent, mensaisOrdenados, topCategorias } from '@/lib/format'

const dados = ref<DashboardGlobal | null>(null)
const loading = ref(true)
const erro = ref<string | null>(null)

const utilizador = utilizadorAtual() ?? 'Utilizador'

onMounted(async () => {
  try {
    dados.value = await fetchDashboardGlobal()
  } catch (e: any) {
    erro.value = e?.message ?? 'Falha ao carregar dashboard'
  } finally {
    loading.value = false
  }
})

const valorNumerario = computed(() => {
  if (!dados.value) return 0
  return dados.value.vendasTotais * (dados.value.percentagemNumerario / 100)
})
const valorMultibanco = computed(() => {
  if (!dados.value) return 0
  return dados.value.vendasTotais * (dados.value.percentagemMultibanco / 100)
})

const loja1 = computed(() => mensaisOrdenados(dados.value?.vendasMensaisLoja1))
const loja2 = computed(() => mensaisOrdenados(dados.value?.vendasMensaisLoja2))
const labelsMeses = computed(() => loja1.value.labels.length > 0 ? loja1.value.labels : loja2.value.labels)

const categorias = computed(() => topCategorias(dados.value?.vendasPorCategoria))

const decomposicao = computed(() => dados.value?.decomposicaoPorLoja ?? [])

const nomeLoja1 = computed(() => decomposicao.value[0]?.nomeLoja ?? 'Loja 1')
const nomeLoja2 = computed(() => decomposicao.value[1]?.nomeLoja ?? 'Loja 2')
</script>

<template>
  <div class="flex flex-col min-h-screen bg-[#f8fafc]">
    <main class="p-10 pt-8 space-y-8">
      <Breadcrumb :items="['Supervisão', 'Dashboard Global']" class="mb-0" />

      <header class="flex items-start justify-between">
        <div>
          <h1 class="text-[28px] font-bold text-[#0f172a] leading-tight">Bem-vindo(a), {{ utilizador }}</h1>
          <p class="text-sm text-[#64748b] font-medium">
            Painel Estratégico de Toda a Rede ({{ dados?.lojasEsperadas ?? 0 }} Lojas)
          </p>
        </div>
        <div class="flex items-center gap-6">
          <div class="flex flex-col items-end">
            <span class="text-[10px] font-bold text-[#94a3b8] uppercase tracking-wider">Lojas Sincronizadas</span>
            <span class="text-sm font-bold text-[#0f172a]">
              {{ dados?.lojasSincronizadas ?? 0 }} / {{ dados?.lojasEsperadas ?? 0 }}
            </span>
          </div>
        </div>
      </header>

      <p v-if="erro" class="text-sm text-red-600">{{ erro }}</p>
      <p v-else-if="loading" class="text-sm text-slate-500">A carregar…</p>

      <template v-else-if="dados">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Vendas Totais (Rede)</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[28px] font-bold text-[#0f172a]">{{ formatEuro(dados.vendasTotais) }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Total Transações</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[28px] font-bold text-[#0f172a]">{{ formatInt(dados.totalTransacoes) }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Ticket Médio</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[28px] font-bold text-[#0f172a]">{{ formatEuro(dados.ticketMedio) }}</div>
            </CardContent>
          </Card>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Total Devoluções</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[28px] font-bold text-[#0f172a]">{{ formatEuro(dados.totalDevolucoes) }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Despesas (Remessas)</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[28px] font-bold text-[#0f172a]">{{ formatEuro(dados.totalDespesas) }}</div>
            </CardContent>
          </Card>

          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Lucro Líquido (Rede)</CardTitle>
            </CardHeader>
            <CardContent>
              <div
                class="text-[28px] font-bold"
                :class="dados.lucroLiquido >= 0 ? 'text-[#16a34a]' : 'text-[#dc2626]'"
              >{{ formatEuro(dados.lucroLiquido) }}</div>
              <p class="text-[11px] text-[#94a3b8] font-medium mt-1">Vendas − Devoluções − Despesas</p>
            </CardContent>
          </Card>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-1 gap-6">
          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader class="pb-2">
              <CardTitle class="text-[13px] font-medium text-[#64748b]">Numerário vs MB</CardTitle>
            </CardHeader>
            <CardContent>
              <div class="text-[28px] font-bold text-[#0f172a]">
                {{ formatPercent(dados.percentagemNumerario) }} / {{ formatPercent(dados.percentagemMultibanco) }}
              </div>
              <p class="text-[11px] text-[#94a3b8] font-medium mt-2">
                {{ formatEuro(valorNumerario) }} / {{ formatEuro(valorMultibanco) }}
              </p>
            </CardContent>
          </Card>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 pt-2">
          <Card class="shadow-none border-slate-200 bg-white">
            <CardHeader>
              <CardTitle class="text-base font-bold text-[#0f172a]">Evolução de Vendas por Loja</CardTitle>
            </CardHeader>
            <CardContent>
              <SalesChart
                :store1Data="loja1.values"
                :store2Data="loja2.values"
                :labels="labelsMeses"
                :store1Label="nomeLoja1"
                :store2Label="nomeLoja2"
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
          <h2 class="text-lg font-bold text-[#0f172a]">Decomposição por Loja</h2>
          <div class="border rounded-lg bg-white shadow-none overflow-hidden">
            <Table>
              <TableHeader>
                <TableRow class="bg-[#f8fafc] hover:bg-[#f8fafc] border-b border-slate-200">
                  <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Loja</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Vendas</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Transações</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Devoluções</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Despesas</TableHead>
                  <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Lucro</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow
                  v-for="l in decomposicao"
                  :key="l.idLoja"
                  class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12"
                >
                  <TableCell class="text-sm font-bold text-[#0f172a]">{{ l.nomeLoja }}</TableCell>
                  <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatEuro(l.vendas) }}</TableCell>
                  <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatInt(l.transacoes) }}</TableCell>
                  <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatEuro(l.devolucoes) }}</TableCell>
                  <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatEuro(l.despesas) }}</TableCell>
                  <TableCell class="text-sm font-bold" :class="l.lucro >= 0 ? 'text-[#16a34a]' : 'text-[#dc2626]'">{{ formatEuro(l.lucro) }}</TableCell>
                </TableRow>
                <TableRow v-if="decomposicao.length === 0">
                  <TableCell colspan="6" class="text-sm text-slate-400 text-center py-6">Sem dados</TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>
