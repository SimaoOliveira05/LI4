<script setup lang="ts">
import { ChevronRight } from 'lucide-vue-next'

defineProps<{
  items: string[]
}>()
</script>

<template>
  <nav class="flex items-center gap-2 text-xs font-medium text-muted-foreground mb-4">
    <template v-for="(item, index) in items" :key="item">
      <span>{{ item }}</span>
      <ChevronRight v-if="index < items.length - 1" :size="12" />
    </template>
  </nav>
</template>
