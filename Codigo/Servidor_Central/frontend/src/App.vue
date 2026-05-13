<script setup lang="ts">
import Sidebar from '@/components/layout/Sidebar.vue'
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'
import { isAuthenticated } from '@/api/authService'

const route = useRoute()
const mostrarSidebar = computed(() => !route.meta.publica && isAuthenticated())
</script>

<template>
  <div class="flex min-h-screen bg-background font-sans antialiased text-foreground">
    <Sidebar v-if="mostrarSidebar" />
    <main class="flex-1 overflow-y-auto">
      <RouterView />
    </main>
  </div>
</template>

<style>
:root {
  font-family: Inter, system-ui, Avenir, Helvetica, Arial, sans-serif;
}
</style>
