<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Breadcrumb from '@/components/layout/Breadcrumb.vue'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Card, CardContent } from '@/components/ui/card'
import { Download, Play } from 'lucide-vue-next'
import { fetchLojas, fetchRelatorio } from '@/api/networkService'
import type { RawLoja, RelatorioLinha } from '@/api/types'
import { formatEuro, formatData } from '@/lib/format'

const lojas = ref<RawLoja[]>([])
const selectedStore = ref('all')
const selectedCategory = ref('all')
const dataInicio = ref('')
const dataFim = ref('')
const linhas = ref<RelatorioLinha[]>([])
const loading = ref(false)
const erro = ref<string | null>(null)

const categorias = ref<string[]>([])

onMounted(async () => {
  try {
    lojas.value = await fetchLojas()
  } catch (e: any) {
    erro.value = e?.message ?? 'Falha ao carregar lojas'
  }
  await aplicar()
})

async function aplicar() {
  loading.value = true
  erro.value = null
  try {
    linhas.value = await fetchRelatorio(
      selectedStore.value === 'all' ? undefined : selectedStore.value,
      dataInicio.value || undefined,
      dataFim.value || undefined,
      selectedCategory.value === 'all' ? undefined : selectedCategory.value,
    )
    const set = new Set<string>()
    linhas.value.forEach((l) => set.add(l.categoria))
    if (categorias.value.length === 0) categorias.value = Array.from(set).sort()
  } catch (e: any) {
    erro.value = e?.message ?? 'Falha ao gerar relatório'
  } finally {
    loading.value = false
  }
}

function exportarCSV() {
  const header = 'Data,Loja,Categoria,Vendas\n'
  const rows = linhas.value.map((l) => `${l.data},${l.nomeLoja},${l.categoria},${l.vendas}`).join('\n')
  const blob = new Blob([header + rows], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'relatorio.csv'
  a.click()
  URL.revokeObjectURL(url)
}
</script>

<template>
  <div class="flex flex-col min-h-screen bg-[#f8fafc]">
    <main class="p-10 pt-8 space-y-8">
      <Breadcrumb :items="['Supervisão', 'Relatórios Analíticos']" class="mb-0" />

      <header class="flex items-start justify-between">
        <div>
          <h1 class="text-[28px] font-bold text-[#0f172a] leading-tight">Relatórios Analíticos</h1>
          <p class="text-sm text-[#64748b] font-medium">Filtre e exporte os dados consolidados da rede</p>
        </div>
        <div class="flex items-center gap-3">
          <button
            @click="exportarCSV"
            :disabled="linhas.length === 0"
            class="inline-flex items-center gap-2 px-4 py-2 bg-[#0f172a] text-white rounded-md hover:bg-slate-800 disabled:opacity-50 transition-colors shadow-sm text-xs font-semibold"
          >
            <Download :size="14" />
            Exportar Relatório
          </button>
          <button
            @click="aplicar"
            class="inline-flex items-center gap-2 px-4 py-2 bg-white border border-slate-200 text-[#0f172a] rounded-md hover:bg-slate-50 transition-colors shadow-sm text-xs font-bold"
          >
            <Play :size="14" />
            Gerar Relatório
          </button>
        </div>
      </header>

      <Card class="shadow-none border-slate-200 bg-white">
        <CardContent class="p-6">
          <div class="flex flex-col md:flex-row gap-6 items-end">
            <div class="flex-1 space-y-2">
              <label class="text-[13px] font-medium text-[#0f172a]">Loja(s)</label>
              <Select v-model="selectedStore">
                <SelectTrigger class="w-full bg-white h-10 border-slate-200">
                  <SelectValue placeholder="Todas as Lojas" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem value="all">Todas as Lojas</SelectItem>
                    <SelectItem v-for="l in lojas" :key="l.idLoja" :value="l.idLoja">{{ l.nomeLoja }}</SelectItem>
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>

            <div class="flex-1 space-y-2">
              <label class="text-[13px] font-medium text-[#0f172a]">Data Início</label>
              <input
                type="date"
                v-model="dataInicio"
                class="w-full h-10 px-3 border border-slate-200 rounded-md bg-white text-xs font-semibold text-[#0f172a]"
              />
            </div>

            <div class="flex-1 space-y-2">
              <label class="text-[13px] font-medium text-[#0f172a]">Data Fim</label>
              <input
                type="date"
                v-model="dataFim"
                class="w-full h-10 px-3 border border-slate-200 rounded-md bg-white text-xs font-semibold text-[#0f172a]"
              />
            </div>

            <div class="flex-1 space-y-2">
              <label class="text-[13px] font-medium text-[#0f172a]">Categoria</label>
              <Select v-model="selectedCategory">
                <SelectTrigger class="w-full bg-white h-10 border-slate-200">
                  <SelectValue placeholder="Todas as Categorias" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem value="all">Todas as Categorias</SelectItem>
                    <SelectItem v-for="c in categorias" :key="c" :value="c">{{ c }}</SelectItem>
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>

            <button
              @click="aplicar"
              class="h-10 px-6 border border-slate-200 text-[#0f172a] rounded-md hover:bg-slate-50 transition-colors text-xs font-bold shadow-sm"
            >
              Aplicar Filtros
            </button>
          </div>
        </CardContent>
      </Card>

      <p v-if="erro" class="text-sm text-red-600">{{ erro }}</p>

      <div class="space-y-4">
        <h2 class="text-lg font-bold text-[#0f172a]">Resultados do Relatório</h2>
        <div class="border rounded-lg bg-white shadow-none overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow class="bg-[#f4f4f5] hover:bg-[#f4f4f5] border-b border-slate-200">
                <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Data</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Loja</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Categoria</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Vendas</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow
                v-for="(l, i) in linhas"
                :key="i"
                class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-14"
              >
                <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatData(l.data) }}</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">{{ l.nomeLoja }}</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">{{ l.categoria }}</TableCell>
                <TableCell class="text-sm font-bold text-[#0f172a]">{{ formatEuro(l.vendas) }}</TableCell>
              </TableRow>
              <TableRow v-if="!loading && linhas.length === 0">
                <TableCell colspan="4" class="text-sm text-slate-400 text-center py-6">Sem resultados</TableCell>
              </TableRow>
              <TableRow v-if="loading">
                <TableCell colspan="4" class="text-sm text-slate-500 text-center py-6">A carregar…</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </div>
    </main>
  </div>
</template>
