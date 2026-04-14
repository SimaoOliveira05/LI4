<script setup lang="ts">
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  PointElement,
  LineElement,
  CategoryScale,
  LinearScale
} from 'chart.js'
import { Line } from 'vue-chartjs'
import { computed } from 'vue'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend)

const props = defineProps<{
  store1Data: number[],
  store2Data: number[]
}>()

const chartData = computed(() => ({
  labels: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
  datasets: [
    {
      label: 'Loja 1',
      borderColor: '#2563eb',
      backgroundColor: '#2563eb',
      data: props.store1Data,
      borderWidth: 2,
      pointRadius: 4,
      pointBackgroundColor: '#2563eb',
      showLine: true,
      tension: 0.4
    },
    {
      label: 'Loja 2',
      borderColor: '#64748b',
      backgroundColor: '#64748b',
      data: props.store2Data,
      borderWidth: 2,
      pointRadius: 4,
      pointBackgroundColor: '#64748b',
      showLine: true,
      tension: 0.4
    }
  ]
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'bottom' as const,
      align: 'start' as const,
      labels: {
        usePointStyle: true,
        boxWidth: 6,
        font: { size: 11 }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      min: 0,
      max: 50000,
      ticks: {
        stepSize: 10000,
        callback: (value: any) => value / 1000 + 'k',
        font: { size: 10 }
      },
      grid: { color: '#f1f5f9' }
    },
    x: {
      grid: { display: false },
      ticks: { font: { size: 10 } }
    }
  }
}
</script>

<template>
  <div class="h-[300px]">
    <Line :data="chartData" :options="chartOptions" />
  </div>
</template>
