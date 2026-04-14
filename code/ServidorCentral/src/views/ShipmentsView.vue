<script setup lang="ts">
import { ref } from 'vue'
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
import { 
  Select, 
  SelectContent, 
  SelectGroup, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from '@/components/ui/select'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Calendar } from 'lucide-vue-next'

const selectedStore = ref('all')
const selectedStatus = ref('all')
</script>

<template>
  <div class="flex flex-col min-h-screen bg-[#f8fafc]">
    <main class="p-10 pt-8 space-y-8">
      <Breadcrumb :items="['Supervisão', 'Remessas da Rede']" class="mb-0" />

      <header class="flex flex-col gap-1">
        <h1 class="text-[28px] font-bold text-[#0f172a] leading-tight">Remessas da Rede</h1>
        <p class="text-sm text-[#64748b] font-medium">Consulte e filtre as remessas recebidas em todas as lojas da rede</p>
      </header>

      <!-- Filter Card -->
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
                    <SelectItem value="1">Loja 1</SelectItem>
                    <SelectItem value="2">Loja 2</SelectItem>
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
                    <SelectItem value="received">Validado</SelectItem>
                    <SelectItem value="pending">Pendente</SelectItem>
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>

            <div class="flex-1 space-y-2">
              <label class="text-[13px] font-medium text-[#0f172a]">Período</label>
              <div class="flex items-center gap-2 px-3 h-10 border border-slate-200 rounded-md bg-white text-xs font-semibold text-[#0f172a] shadow-none">
                <Calendar :size="14" class="text-[#64748b]" />
                <span>01 Mar - 31 Mar</span>
                <ChevronDown :size="14" class="text-[#64748b] ml-auto" />
              </div>
            </div>

            <button class="h-10 px-6 border border-slate-200 text-[#0f172a] rounded-md hover:bg-slate-50 transition-colors text-xs font-bold shadow-sm">
              Aplicar Filtros
            </button>
          </div>
        </CardContent>
      </Card>

      <!-- Table Container -->
      <div class="border rounded-lg bg-white shadow-none overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow class="bg-[#f4f4f5] hover:bg-[#f4f4f5] border-b border-slate-200">
              <TableHead class="text-[12px] font-medium text-[#64748b] py-3 h-10">Loja</TableHead>
              <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Nº Doc / Fornecedor</TableHead>
              <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Data Receção</TableHead>
              <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Valor Total</TableHead>
              <TableHead class="text-[12px] font-medium text-[#64748b] h-10">Estado Pagamento</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-14">
              <TableCell class="text-sm font-medium text-[#0f172a]">Loja 1</TableCell>
              <TableCell class="text-sm font-medium text-[#0f172a]">FT 2026/452 - Staples Portugal</TableCell>
              <TableCell class="text-sm font-medium text-[#0f172a]">30/03/2026</TableCell>
              <TableCell class="text-sm font-bold text-[#0f172a]">1.245,50 €</TableCell>
              <TableCell>
                <Badge class="bg-[#dcfce7] text-[#16a34a] text-[11px] font-bold px-2 py-0.5 border-none shadow-none">Validado</Badge>
              </TableCell>
            </TableRow>
            <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-14">
              <TableCell class="text-sm font-medium text-[#0f172a]">Loja 2</TableCell>
              <TableCell class="text-sm font-medium text-[#0f172a]">GR 882/26 - Porto Editora</TableCell>
              <TableCell class="text-sm font-medium text-[#0f172a]">29/03/2026</TableCell>
              <TableCell class="text-sm font-bold text-[#0f172a]">850,00 €</TableCell>
              <TableCell>
                <Badge class="bg-[#ffedd5] text-[#f59e0b] text-[11px] font-bold px-2 py-0.5 border-none shadow-none">Pendente</Badge>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </div>
    </main>
  </div>
</template>
