<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Breadcrumb from '@/components/layout/Breadcrumb.vue'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { fetchLojas, fetchShipments } from '@/api/networkService'
import type { RawLoja, Shipment } from '@/api/types'
import { formatEuro, formatData } from '@/lib/format'

const lojas = ref<RawLoja[]>([])
const selectedStore = ref('all')
const selectedStatus = ref('all')
const remessas = ref<Shipment[]>([])
const loading = ref(false)
const erro = ref<string | null>(null)

const STATUS_PARAM: Record<string, string | undefined> = {
  all: undefined,
  received: 'PAGA',
  pending: 'PENDENTE_PAGAMENTO',
}

async function carregar() {
  loading.value = true
  erro.value = null
  try {
    remessas.value = await fetchShipments(
      selectedStore.value === 'all' ? undefined : selectedStore.value,
      STATUS_PARAM[selectedStatus.value],
    )
  } catch (e: unknown) {
    erro.value = e instanceof Error ? e.message : 'Falha ao carregar remessas'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    lojas.value = await fetchLojas()
  } catch (e: unknown) {
    erro.value = e instanceof Error ? e.message : 'Falha ao carregar lojas'
  }
  await carregar()
})
</script>

<template>
  <div class="flex flex-col min-h-screen bg-[#f8fafc]">
    <main class="p-10 pt-8 space-y-8">
      <Breadcrumb :items="['Supervisão', 'Remessas da Rede']" class="mb-0" />

      <header class="flex flex-col gap-1">
        <h1 class="text-[28px] font-bold text-[#0f172a] leading-tight">Remessas da Rede</h1>
        <p class="text-sm text-[#64748b] font-medium">Consulte e filtre as remessas recebidas em todas as lojas da rede</p>
      </header>

      <Card class="shadow-none border-slate-200 bg-white">
        <CardHeader class="pb-3 border-b border-slate-100">
          <CardTitle class="text-base font-semibold text-[#0f172a]">Filtros de Pesquisa</CardTitle>
        </CardHeader>
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
              <label class="text-[13px] font-medium text-[#0f172a]">Estado Pagamento</label>
              <Select v-model="selectedStatus">
                <SelectTrigger class="w-full bg-white h-10 border-slate-200">
                  <SelectValue placeholder="Todos os Estados" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem value="all">Todos os Estados</SelectItem>
                    <SelectItem value="received">Paga</SelectItem>
                    <SelectItem value="pending">Pendente</SelectItem>
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>

            <button
              @click="carregar"
              class="h-10 px-6 border border-slate-200 text-[#0f172a] rounded-md hover:bg-slate-50 transition-colors text-xs font-bold shadow-sm"
            >
              Aplicar Filtros
            </button>
          </div>
        </CardContent>
      </Card>

      <p v-if="erro" class="text-sm text-red-600">{{ erro }}</p>

      <div class="border rounded-lg bg-white shadow-none overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow class="bg-[#f4f4f5] hover:bg-[#f4f4f5] border-b border-slate-200">
              <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Loja</TableHead>
              <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Referência / Fornecedor</TableHead>
              <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Data Receção</TableHead>
              <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Valor Total</TableHead>
              <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Estado Pagamento</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow
              v-for="r in remessas"
              :key="r.id"
              class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-14"
            >
              <TableCell class="text-sm font-medium text-[#0f172a]">{{ r.storeName }}</TableCell>
              <TableCell class="text-sm font-medium text-[#0f172a]">{{ r.id }} - {{ r.supplier }}</TableCell>
              <TableCell class="text-sm font-medium text-[#0f172a]">{{ formatData(r.date) }}</TableCell>
              <TableCell class="text-sm font-bold text-[#0f172a]">{{ formatEuro(r.totalValue) }}</TableCell>
              <TableCell>
                <Badge
                  v-if="r.status === 'received'"
                  class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-2 py-0.5 border-none shadow-none"
                >
                  Paga
                </Badge>
                <Badge
                  v-else
                  class="bg-[#ffedd5] text-[#f59e0b] text-[11px] font-bold px-2 py-0.5 border-none shadow-none"
                >
                  Pendente
                </Badge>
              </TableCell>
            </TableRow>
            <TableRow v-if="!loading && remessas.length === 0">
              <TableCell colspan="5" class="text-sm text-slate-400 text-center py-6">Sem remessas</TableCell>
            </TableRow>
            <TableRow v-if="loading">
              <TableCell colspan="5" class="text-sm text-slate-500 text-center py-6">A carregar…</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </main>
  </div>
</template>
