<script setup lang="ts">
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement
} from 'chart.js'
import { Doughnut } from 'vue-chartjs'
import { computed } from 'vue'

ChartJS.register(ArcElement, Title, Tooltip, Legend)

const props = withDefaults(defineProps<{
  data: number[]
  labels?: string[]
}>(), { labels: () => [] })

const PALETA = ['#2563eb', '#60a5fa', '#93c5fd', '#bfdbfe', '#dbeafe']

const chartData = computed(() => {
  const total = props.data.reduce((a, b) => a + b, 0) || 1
  const labels = props.labels.length === props.data.length
    ? props.labels.map((l, i) => [l, `${Math.round((props.data[i] / total) * 100)}%`])
    : props.data.map((_, i) => [`Categoria ${i + 1}`, `${Math.round((props.data[i] / total) * 100)}%`])
  return {
    labels,
    datasets: [
      {
        backgroundColor: PALETA.slice(0, props.data.length),
        data: props.data,
        borderWidth: 0,
        cutout: '70%'
      }
    ]
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'right' as const,
      labels: {
        usePointStyle: true,
        pointStyle: 'circle' as const,
        boxWidth: 7,
        padding: 16,
        font: {
          size: 11,
          weight: 'bold' as const
        }
      }
    }
  }
}
</script>

<template>
  <div class="h-[250px]">
    <Doughnut v-if="data.length > 0" :data="chartData" :options="chartOptions" />
    <div v-else class="h-full flex items-center justify-center text-sm text-slate-400">Sem dados</div>
  </div>
</template>
