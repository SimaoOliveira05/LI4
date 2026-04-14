<script setup lang="ts">
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement
} from 'chart.js'
import { Pie } from 'vue-chartjs'
import { computed } from 'vue'

ChartJS.register(ArcElement, Title, Tooltip, Legend)

const props = defineProps<{
  data: number[]
}>()

const chartData = computed(() => ({
  labels: ['Papelaria', 'Merchandising', 'Cafetaria', 'Outros'],
  datasets: [
    {
      backgroundColor: ['#0f172a', '#2563eb', '#64748b', '#cbd5e1'],
      data: props.data
    }
  ]
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'right' as const,
      labels: {
        usePointStyle: true,
        font: {
          size: 11
        }
      }
    }
  }
}
</script>

<template>
  <div class="h-[250px]">
    <Pie :data="chartData" :options="chartOptions" />
  </div>
</template>
