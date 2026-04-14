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

const props = defineProps<{
  data: number[]
}>()

const chartData = computed(() => ({
  labels: ['Papelaria (45%)', 'Merchandising (30%)', 'Livros (25%)'],
  datasets: [
    {
      backgroundColor: ['#2563eb', '#60a5fa', '#93c5fd'],
      data: props.data,
      borderWidth: 0,
      cutout: '70%'
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
        boxWidth: 8,
        padding: 20,
        font: {
          size: 12,
          weight: 'bold' as const
        }
      }
    }
  }
}
</script>

<template>
  <div class="h-[250px]">
    <Doughnut :data="chartData" :options="chartOptions" />
  </div>
</template>
