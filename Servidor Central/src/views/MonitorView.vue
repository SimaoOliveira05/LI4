<script setup lang="ts">
import { onMounted } from 'vue'
import { useNetworkStore } from '@/stores/network'
import Breadcrumb from '@/components/layout/Breadcrumb.vue'
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '@/components/ui/table'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Calendar, ChevronDown } from 'lucide-vue-next'

const networkStore = useNetworkStore()

onMounted(() => {
  networkStore.loadStores()
})
</script>

<template>
  <div class="flex flex-col min-h-screen bg-[#f8fafc]">
    <main class="p-10 pt-8 space-y-8">
      <Breadcrumb :items="['Supervisão', 'Receção de Dados']" class="mb-0" />

      <header class="flex items-start justify-between">
        <div>
          <h1 class="text-[28px] font-bold text-[#0f172a] leading-tight">Monitor de Receção de Dados</h1>
          <p class="text-sm text-[#64748b] font-medium">Estado da sincronização diária de 2 lojas</p>
        </div>
        <div class="flex items-center gap-2">
          <div class="flex items-center gap-2 px-3 py-2 border border-slate-200 rounded-md bg-white text-xs font-semibold shadow-sm text-[#0f172a] cursor-pointer hover:bg-slate-50">
            <Calendar :size="14" class="text-[#64748b]" />
            <span>02 Abr 2026</span>
            <ChevronDown :size="14" class="text-[#64748b] ml-1" />
          </div>
        </div>
      </header>

      <!-- KPI Grid -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Lojas Esperadas</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-[32px] font-bold text-[#0f172a]">2</div>
            <div class="mt-2">
              <span class="bg-[#dcfce7] text-[#16a34a] text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-tight">Foco Reduzido</span>
            </div>
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Recebidas com Sucesso</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-[32px] font-bold text-[#0f172a]">1</div>
            <div class="mt-2">
              <span class="bg-[#dcfce7] text-[#16a34a] text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-tight">50%</span>
            </div>
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Em Falta / Erro</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-[32px] font-bold text-[#0f172a]">1</div>
            <div class="mt-2">
              <span class="bg-[#fee2e2] text-[#ef4444] text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-tight">Requer Atenção</span>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- Table -->
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
            <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12">
              <TableCell class="text-sm font-bold text-[#0f172a]">Loja 1</TableCell>
              <TableCell class="text-sm font-medium text-[#0f172a]">22:05</TableCell>
              <TableCell>
                <Badge class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-2 py-0.5 border-none shadow-none">Recebido</Badge>
              </TableCell>
            </TableRow>
            <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12">
              <TableCell class="text-sm font-bold text-[#0f172a]">Loja 2</TableCell>
              <TableCell class="text-sm font-medium text-[#0f172a]">--:--</TableCell>
              <TableCell>
                <Badge class="bg-[#fee2e2] text-[#ef4444] text-[11px] font-bold px-2 py-0.5 border-none shadow-none">Pendente</Badge>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </main>
  </div>
</template>
