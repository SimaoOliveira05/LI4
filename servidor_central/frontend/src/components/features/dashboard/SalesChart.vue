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

const props = withDefaults(defineProps<{
  store1Data: number[]
  store2Data?: number[]
  labels?: string[]
  store1Label?: string
  store2Label?: string
}>(), {
  store2Data: () => [],
  labels: () => [],
  store1Label: 'Loja 1',
  store2Label: 'Loja 2',
})

const fallback = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']

const chartData = computed(() => {
  const datasets = [
    {
      label: props.store1Label,
      borderColor: '#2563eb',
      backgroundColor: '#2563eb',
      data: props.store1Data,
      borderWidth: 2,
      pointRadius: 4,
      pointBackgroundColor: '#2563eb',
      showLine: true,
      tension: 0.4
    },
  ]
  if (props.store2Data && props.store2Data.length > 0) {
    datasets.push({
      label: props.store2Label,
      borderColor: '#64748b',
      backgroundColor: '#64748b',
      data: props.store2Data,
      borderWidth: 2,
      pointRadius: 4,
      pointBackgroundColor: '#64748b',
      showLine: true,
      tension: 0.4
    })
  }
  return {
    labels: props.labels.length > 0 ? props.labels : fallback,
    datasets,
  }
})

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
      ticks: {
        callback: (value: number | string) => (Number(value) >= 1000 ? Number(value) / 1000 + 'k' : value),
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
