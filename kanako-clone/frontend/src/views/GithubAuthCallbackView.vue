<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authApi } from '../api/modules'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const error = ref('')

onMounted(async () => {
  const code = route.query.code
  const state = route.query.state
  if (!code) {
    error.value = '缺少授权码'
    return
  }
  try {
    const res = await authApi.githubCallback({
      code,
      redirectUri: window.location.origin + '/auth/github/callback',
      state
    })
    auth._setAuth(res.token, res.user)
    const redirect = route.query.redirect || '/'
    router.replace(redirect)
  } catch (e) {
    error.value = e?.message || 'GitHub 登录失败'
  }
})
</script>

<template>
  <div class="cb-wrap">
    <div class="cb-card glass-blur">
      <p v-if="!error" class="loading">登录中，请稍候…</p>
      <template v-else>
        <p class="err">{{ error }}</p>
        <RouterLink to="/login">← 返回登录页</RouterLink>
      </template>
    </div>
  </div>
</template>

<style scoped>
.cb-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.cb-card {
  padding: 30px 40px;
  text-align: center;
}

.loading {
  color: var(--text-secondary);
}

.err {
  color: var(--danger);
  margin: 0 0 12px;
}
</style>
