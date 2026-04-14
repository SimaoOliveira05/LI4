<script setup lang="ts">
import { ref } from 'vue'
import SalesChart from '@/components/features/dashboard/SalesChart.vue'
import DoughnutChart from '@/components/features/dashboard/DoughnutChart.vue'
import Breadcrumb from '@/components/layout/Breadcrumb.vue'
import { 
  Card, 
  CardContent, 
  CardHeader, 
  CardTitle 
} from '@/components/ui/card'
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '@/components/ui/table'
import { Calendar, ChevronDown } from 'lucide-vue-next'

// Dados simulados baseados na print (escala anual Jan-Dez)
const store1Data = ref([18000, 20000, 22000, 28000, 25000, 24000, 22000, 32000, 28000, 35000, 38000, 48000])
const store2Data = ref([15000, 17000, 19000, 21000, 18000, 17000, 16000, 22000, 20000, 25000, 32000, 42000])
const categoryData = ref([45, 30, 25])
</script>

<template>
  <div class="flex flex-col min-h-screen bg-[#f8fafc]">
    <main class="p-10 pt-8 space-y-8">
      <!-- Breadcrumb -->
      <Breadcrumb :items="['Supervisão', 'Dashboard Global']" class="mb-0" />
      
      <!-- Page Header -->
      <header class="flex items-start justify-between">
        <div>
          <h1 class="text-[28px] font-bold text-[#0f172a] leading-tight">Bem-vinda, Cátia</h1>
          <p class="text-sm text-[#64748b] font-medium">Painel Estratégico de Toda a Rede (2 Lojas)</p>
        </div>
        <div class="flex items-center gap-6">
          <div class="flex flex-col items-end">
            <span class="text-[10px] font-bold text-[#94a3b8] uppercase tracking-wider">Lojas Sincronizadas</span>
            <span class="text-sm font-bold text-[#0f172a]">2 / 2</span>
          </div>
          <div class="flex items-center gap-2">
            <div class="flex items-center gap-2 px-3 py-2 border border-slate-200 rounded-md bg-white text-xs font-semibold shadow-sm text-[#0f172a]">
              <Calendar :size="14" class="text-[#64748b]" />
              <span>01 Abr 2026</span>
            </div>
            <div class="flex items-center gap-2 px-3 py-2 border border-slate-200 rounded-md bg-white text-xs font-semibold shadow-sm text-[#0f172a] cursor-pointer hover:bg-slate-50">
              <Calendar :size="14" class="text-[#64748b]" />
              <span>Últimos 30 dias</span>
              <ChevronDown :size="14" class="text-[#64748b] ml-1" />
            </div>
          </div>
        </div>
      </header>

      <!-- KPI Row 1 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Vendas Totais (Rede)</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-[28px] font-bold text-[#0f172a]">34.500,00 €</div>
            <div class="flex items-center gap-2 mt-2">
              <span class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-1.5 py-0.5 rounded">+5.2%</span>
              <span class="text-[11px] text-[#94a3b8] font-medium">vs. per. anterior</span>
            </div>
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Total Transações</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-[28px] font-bold text-[#0f172a]">1.432</div>
            <div class="flex items-center gap-2 mt-2">
              <span class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-1.5 py-0.5 rounded">+5.1%</span>
              <span class="text-[11px] text-[#94a3b8] font-medium">vs. per. anterior</span>
            </div>
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Ticket Médio</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-[28px] font-bold text-[#0f172a]">24,09 €</div>
            <div class="flex items-center gap-2 mt-2">
              <span class="bg-[#fee2e2] text-[#ef4444] text-[11px] font-bold px-1.5 py-0.5 rounded">-1.2%</span>
              <span class="text-[11px] text-[#94a3b8] font-medium">vs. per. anterior</span>
            </div>
          </CardContent>
        </Card>
      </div>

      <!-- KPI Row 2 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Total Devoluções</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-[28px] font-bold text-[#0f172a]">1.250,00 €</div>
            <div class="flex items-center gap-2 mt-2">
              <span class="bg-[#fee2e2] text-[#ef4444] text-[11px] font-bold px-1.5 py-0.5 rounded">+2.1%</span>
              <span class="text-[11px] text-[#94a3b8] font-medium">vs. per. anterior</span>
            </div>
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Numerário vs MB</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-[28px] font-bold text-[#0f172a]">35% / 65%</div>
            <p class="text-[11px] text-[#94a3b8] font-medium mt-2">12.075€ / 22.425€</p>
          </CardContent>
        </Card>
      </div>

      <!-- Charts Row -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 pt-2">
        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader>
            <CardTitle class="text-base font-bold text-[#0f172a]">Evolução de Vendas por Loja</CardTitle>
          </CardHeader>
          <CardContent>
            <SalesChart :store1Data="store1Data" :store2Data="store2Data" />
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader>
            <CardTitle class="text-base font-bold text-[#0f172a]">Top Categorias</CardTitle>
          </CardHeader>
          <CardContent class="flex items-center justify-center pt-4">
            <DoughnutChart :data="categoryData" />
          </CardContent>
        </Card>
      </div>

      <!-- Decomposition Table -->
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
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12">
                <TableCell class="text-sm font-bold text-[#0f172a]">Loja 1</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">15.200,00 €</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">650</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">200,00 €</TableCell>
              </TableRow>
              <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12">
                <TableCell class="text-sm font-bold text-[#0f172a]">Loja 2</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">19.300,00 €</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">782</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">1.050,00 €</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </div>
    </main>
  </div>
</template>
