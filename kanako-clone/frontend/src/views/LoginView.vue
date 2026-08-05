<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { authApi } from '../api/modules'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const form = ref({ username: '', password: '' })
const loading = ref(false)
const ghLoading = ref(false)
const error = ref('')

async function submit() {
  if (!form.value.username || !form.value.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    await auth.login(form.value.username, form.value.password)
    router.push(route.query.redirect || '/')
  } catch (e) {
    error.value = e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}

async function githubLogin() {
  ghLoading.value = true
  try {
    const res = await authApi.githubStart(window.location.origin + '/auth/github/callback')
    window.location.href = res.url
  } catch (e) {
    ElMessage.warning(e?.message || 'GitHub 登录不可用')
    ghLoading.value = false
  }
}

onMounted(() => {
  if (auth.isLogin) router.replace(route.query.redirect || '/')
})
</script>

<template>
  <div class="login-wrap">
    <div class="login-card glass-blur fade-up">
      <h1>登录</h1>
      <p class="sub">欢迎回来</p>
      <form @submit.prevent="submit">
        <input
          v-model="form.username"
          placeholder="用户名"
          autocomplete="username"
        />
        <input
          v-model="form.password"
          type="password"
          placeholder="密码"
          autocomplete="current-password"
        />
        <p v-if="error" class="err">{{ error }}</p>
        <button class="primary" :disabled="loading" type="submit">
          {{ loading ? '登录中…' : '登 录' }}
        </button>
      </form>
      <div class="divider"><span>或</span></div>
      <button class="gh" :disabled="ghLoading" @click="githubLogin">
        <svg viewBox="0 0 16 16" width="16" height="16" fill="currentColor" aria-hidden="true">
          <path d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.01 8.01 0 0 0 16 8c0-4.42-3.58-8-8-8z" />
        </svg>
        {{ ghLoading ? '跳转中…' : '使用 GitHub 登录' }}
      </button>
      <RouterLink to="/" class="back">← 返回首页</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 36px 36px 28px;
  text-align: center;
}

h1 {
  margin: 0 0 4px;
  font-size: 26px;
}

.sub {
  color: var(--text-muted);
  font-size: 13.5px;
  margin: 0 0 26px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

input {
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 11px 14px;
  color: var(--text);
  font-size: 14px;
  outline: none;
}

input:focus {
  border-color: var(--accent);
}

.err {
  color: var(--danger);
  font-size: 13px;
  margin: 0;
  text-align: left;
}

button {
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.primary {
  background: var(--accent);
  color: #0c150e;
  border: none;
  border-radius: 10px;
  padding: 11px;
  font-size: 15px;
  font-weight: 600;
}

.primary:hover {
  opacity: 0.9;
}

.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0 14px;
  color: var(--text-muted);
  font-size: 12px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}

.gh {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text);
  border-radius: 10px;
  padding: 11px;
  font-size: 14px;
}

.gh:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.back {
  display: inline-block;
  margin-top: 20px;
  font-size: 13px;
  color: var(--text-muted);
}
</style>
