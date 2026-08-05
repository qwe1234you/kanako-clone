<script setup>
import { onBeforeMount } from 'vue'
import { useSiteStore } from './stores/site'

const site = useSiteStore()

onBeforeMount(() => {
  site.loadConfig()
})
</script>

<template>
  <RouterView />
</template>