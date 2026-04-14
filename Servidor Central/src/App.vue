<script setup lang="ts">
import Sidebar from '@/components/layout/Sidebar.vue'
import { RouterView } from 'vue-router'
import { onMounted } from 'vue'
import { useNetworkStore } from '@/stores/network'

const networkStore = useNetworkStore()

onMounted(() => {
  networkStore.loadStores()
})
</script>

<template>
  <div class="flex min-h-screen bg-background font-sans antialiased text-foreground">
    <Sidebar />
    <main class="flex-1 overflow-y-auto">
      <RouterView />
    </main>
  </div>
</template>

<style>
/* Font Inter/Geist would be better but standard sans is fine for mockup */
:root {
  font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;
}
</style>
