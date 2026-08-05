import { defineStore } from 'pinia'
import { ref } from 'vue'
import { siteApi } from '../api/modules'

export const useSiteStore = defineStore('site', () => {
  const config = ref({})
  const loaded = ref(false)

  async function loadConfig(force = false) {
    if (loaded.value && !force) return config.value
    try {
      config.value = await siteApi.config()
      loaded.value = true
    } catch {
      /* 后端未就绪时静默 */
    }
    return config.value
  }

  return { config, loaded, loadConfig }
})