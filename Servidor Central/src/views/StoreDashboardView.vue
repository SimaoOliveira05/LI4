<script setup lang="ts">
import { ref } from 'vue'
import Breadcrumb from '@/components/layout/Breadcrumb.vue'
import SalesChart from '@/components/features/dashboard/SalesChart.vue'
import DoughnutChart from '@/components/features/dashboard/DoughnutChart.vue'
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '@/components/ui/table'
import { 
  Select, 
  SelectContent, 
  SelectGroup, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from '@/components/ui/select'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'

const selectedStore = ref('1')
const store1Data = ref([12000, 14000, 15000, 18000, 16000, 15000, 14000, 19000, 17000, 21000, 23000, 28000])
const categoryData = ref([45, 30, 25])
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
        <div class="w-48">
          <Select v-model="selectedStore">
            <SelectTrigger class="w-full bg-white h-10 border-slate-200">
              <SelectValue placeholder="Selecionar Loja" />
            </SelectTrigger>
            <SelectContent>
              <SelectGroup>
                <SelectItem value="1">Loja 1</SelectItem>
                <SelectItem value="2">Loja 2</SelectItem>
              </SelectGroup>
            </SelectContent>
          </Select>
        </div>
      </header>

      <!-- KPI Grid -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Vendas Totais</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold text-[#0f172a]">15.200,00 €</div>
            <p class="text-[11px] text-green-600 font-bold mt-1">+5.2% vs. ontem</p>
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Transações</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold text-[#0f172a]">650</div>
            <p class="text-[11px] text-green-600 font-bold mt-1">+12 transações</p>
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Devoluções</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold text-[#0f172a]">200,00 €</div>
            <p class="text-[11px] text-red-600 font-bold mt-1">-2.1% vs. ontem</p>
          </CardContent>
        </Card>

        <Card class="shadow-none border-slate-200 bg-white">
          <CardHeader class="pb-2">
            <CardTitle class="text-[13px] font-medium text-[#64748b]">Ticket Médio</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-bold text-[#0f172a]">23,38 €</div>
            <p class="text-[11px] text-red-600 font-bold mt-1">-0.5% vs. ontem</p>
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
            <SalesChart :store1Data="store1Data" :store2Data="[]" />
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

      <!-- Histórico de Fechos -->
      <div class="space-y-4 pt-2">
        <h2 class="text-lg font-bold text-[#0f172a] bg-[#f1f5f9] w-fit px-2 py-0.5 rounded">Histórico de Fechos</h2>
        <div class="border rounded-lg bg-white shadow-none overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow class="bg-[#f4f4f5] hover:bg-[#f4f4f5] border-b border-slate-200">
                <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Data</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Vendas</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Transações</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Estado Fecho</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12">
                <TableCell class="text-sm font-medium text-[#0f172a]">30/03/2026</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">4.850,00 €</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">120</TableCell>
                <TableCell>
                  <Badge class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-2 py-0.5 border-none shadow-none">Recebido</Badge>
                </TableCell>
              </TableRow>
              <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12">
                <TableCell class="text-sm font-medium text-[#0f172a]">29/03/2026</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">5.100,00 €</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">135</TableCell>
                <TableCell>
                  <Badge class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-2 py-0.5 border-none shadow-none">Recebido</Badge>
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </div>

      <!-- Logs de Auditoria -->
      <div class="space-y-4 pt-2">
        <h2 class="text-lg font-bold text-[#0f172a] bg-[#f1f5f9] w-fit px-2 py-0.5 rounded">Logs de Auditoria</h2>
        <div class="border rounded-lg bg-white shadow-none overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow class="bg-[#f4f4f5] hover:bg-[#f4f4f5] border-b border-slate-200">
                <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Data / Hora</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Utilizador</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Tipo de Ação</TableHead>
                <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Entidade Afetada</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12">
                <TableCell class="text-sm font-medium text-[#0f172a]">01/04/2026 14:32</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">Cátia (CEO)</TableCell>
                <TableCell>
                  <Badge variant="outline" class="bg-slate-50 text-slate-600 text-[11px] font-medium border-slate-200">Atualização Preço</Badge>
                </TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">Produto SKU: 10452</TableCell>
              </TableRow>
              <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-12">
                <TableCell class="text-sm font-medium text-[#0f172a]">01/04/2026 15:10</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">Ricardo Santos</TableCell>
                <TableCell>
                  <Badge variant="outline" class="bg-slate-50 text-slate-600 text-[11px] font-medium border-slate-200">Venda Anulada</Badge>
                </TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">Fatura: FS 2026/143</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </div>
    </main>
  </div>
</template>
