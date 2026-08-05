import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/modules'
import { getToken, setToken } from '../api/client'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const user = ref(null)

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function _setAuth(t, u) {
    token.value = t
    setToken(t)
    user.value = u
  }

  async function tryRestore() {
    if (!token.value) return
    try {
      user.value = await authApi.me()
    } catch {
      logout()
    }
  }

  async function login(username, password) {
    const data = await authApi.login({ username, password })
    _setAuth(data.token, data.user)
    return data.user
  }

  function logout() {
    token.value = null
    setToken(null)
    user.value = null
  }

  return { token, user, isLogin, isAdmin, tryRestore, login, logout, _setAuth }
})