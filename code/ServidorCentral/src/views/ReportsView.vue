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
import { 
  Select, 
  SelectContent, 
  SelectGroup, 
  SelectItem, 
  SelectTrigger, 
  SelectValue 
} from '@/components/ui/select'
import { Card, CardContent } from '@/components/ui/card'
import { Calendar, Download, Play, ChevronRight, ChevronDown } from 'lucide-vue-next'

const selectedStore = ref('all')
const selectedCategory = ref('all')
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
          <button class="inline-flex items-center gap-2 px-4 py-2 bg-[#0f172a] text-white rounded-md hover:bg-slate-800 transition-colors shadow-sm text-xs font-semibold">
            <Download :size="14" />
            Exportar Relatório
          </button>
          <button class="inline-flex items-center gap-2 px-4 py-2 bg-white border border-slate-200 text-[#0f172a] rounded-md hover:bg-slate-50 transition-colors shadow-sm text-xs font-bold">
            <Play :size="14" />
            Gerar Relatório
          </button>
        </div>
      </header>

      <!-- Filter Card -->
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
                    <SelectItem value="1">Loja 1</SelectItem>
                    <SelectItem value="2">Loja 2</SelectItem>
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

            <div class="flex-1 space-y-2">
              <label class="text-[13px] font-medium text-[#0f172a]">Categoria</label>
              <Select v-model="selectedCategory">
                <SelectTrigger class="w-full bg-white h-10 border-slate-200">
                  <SelectValue placeholder="Todas as Categorias" />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem value="all">Todas as Categorias</SelectItem>
                    <SelectItem value="papelaria">Papelaria</SelectItem>
                    <SelectItem value="merch">Merchandising</SelectItem>
                  </SelectGroup>
                </SelectContent>
              </Select>
            </div>

            <button class="h-10 px-6 border border-slate-200 text-[#0f172a] rounded-md hover:bg-slate-50 transition-colors text-xs font-bold shadow-sm">
              Aplicar Filtros
            </button>
          </div>
        </CardContent>
      </Card>

      <!-- Results Section -->
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
                <TableHead class="w-10 h-10"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              <TableRow class="hover:bg-slate-50 border-b border-slate-100 last:border-0 h-14">
                <TableCell class="text-sm font-medium text-[#0f172a]">30/03/2026</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">Loja 1</TableCell>
                <TableCell class="text-sm font-medium text-[#0f172a]">Papelaria</TableCell>
                <TableCell class="text-sm font-bold text-[#0f172a]">4.500,00 €</TableCell>
                <TableCell>
                  <ChevronRight :size="16" class="text-slate-400" />
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </div>
      </div>
    </main>
  </div>
</template>
