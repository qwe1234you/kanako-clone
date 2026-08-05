<script setup>
import { ref } from 'vue'
import { onBeforeMount } from 'vue'
import { useRoute } from 'vue-router'
import { useSiteStore } from '../stores/site'
import { useAuthStore } from '../stores/auth'
import { setToken } from '../api/client'

const site = useSiteStore()
const auth = useAuthStore()
const route = useRoute()

const navItems = [
  { name: 'home', label: '首页', path: '/' },
  { name: 'blogs', label: '博客', path: '/blogs' },
  { name: 'search', label: '搜索', path: '/search' },
  { name: 'archive-categories', label: '归档', path: '/archive/categories' },
  { name: 'tree-hole', label: '树洞', path: '/tree-hole' },
  { name: 'friend-links', label: '友链', path: '/friend-links' },
  { name: 'friend-circle', label: '朋友圈', path: '/friend-circle' },
  { name: 'chat', label: '聊天', path: '/chat' },
  { name: 'changelog', label: '日志', path: '/changelog' },
  { name: 'about', label: '关于', path: '/about' }
]

const mobileMenu = ref(false)

onBeforeMount(() => {
  site.loadConfig()
  auth.tryRestore()
  window.addEventListener('auth:expired', () => auth.logout())
})

function isActive(n) {
  return route.name === n.name || (n.path !== '/' && route.path.startsWith(n.path))
}

function handleLogout() {
  auth.logout()
}
</script>

<template>
  <div class="layout">
    <header class="nav glass-blur">
      <div class="nav-inner">
        <RouterLink class="brand" to="/">
          <span class="brand-dot" />
          <span>{{ site.config.site_name || 'Kanako · Space' }}</span>
        </RouterLink>
        <nav class="nav-links">
          <RouterLink
            v-for="item in navItems"
            :key="item.name"
            :to="item.path"
            class="nav-link"
            :class="{ active: isActive(item) }"
          >
            {{ item.label }}
          </RouterLink>
          <RouterLink
            v-if="auth.isAdmin"
            to="/blog/write"
            class="nav-link admin"
          >
            写博客
          </RouterLink>
          <RouterLink
            v-if="auth.isAdmin"
            to="/admin/site"
            class="nav-link admin"
          >
            管理
          </RouterLink>
        </nav>
        <div class="nav-actions">
          <template v-if="auth.isLogin">
            <el-dropdown trigger="click">
              <span class="user-chip">
                <el-avatar :size="28" :src="auth.user?.avatar" />
                <span class="user-name">{{ auth.user?.nickname || auth.user?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <RouterLink v-else to="/login" class="login-link">登录</RouterLink>
        </div>
        <button class="burger" @click="mobileMenu = !mobileMenu">
          <el-icon :size="22"><Menu /></el-icon>
        </button>
      </div>
      <div v-if="mobileMenu" class="mobile-menu">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          :to="item.path"
          class="mobile-link"
          @click="mobileMenu = false"
        >
          {{ item.label }}
        </RouterLink>
      </div>
    </header>

    <main class="content">
      <RouterView v-slot="{ Component }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </RouterView>
    </main>

    <footer class="footer">
      <span>{{ site.config.site_footer || '© Kanako Clone' }}</span>
      <span v-if="site.config.site_icp" class="icp">{{ site.config.site_icp }}</span>
    </footer>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.nav {
  position: sticky;
  top: 14px;
  z-index: 100;
  margin: 14px auto 0;
  max-width: 1120px;
  width: calc(100% - 32px);
}

.nav-inner {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 10px 18px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 17px;
  color: var(--text);
  white-space: nowrap;
}

.brand-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 12px var(--accent);
}

.nav-links {
  display: flex;
  gap: 4px;
  flex: 1;
  overflow-x: auto;
  scrollbar-width: none;
}

.nav-links::-webkit-scrollbar {
  display: none;
}

.nav-link {
  padding: 7px 12px;
  border-radius: 9px;
  color: var(--text-secondary);
  font-size: 14px;
  white-space: nowrap;
  transition: all 0.2s;
}

.nav-link:hover {
  color: var(--text);
  background: var(--accent-soft);
}

.nav-link.active {
  color: var(--accent);
  background: var(--accent-soft);
}

.nav-link.admin {
  color: var(--warning);
}

.nav-actions {
  display: flex;
  align-items: center;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 3px 10px 3px 3px;
  border-radius: 20px;
  border: 1px solid var(--border);
  cursor: pointer;
}

.user-name {
  font-size: 13px;
  color: var(--text);
}

.login-link {
  font-size: 14px;
}

.burger {
  display: none;
  background: none;
  border: none;
  color: var(--text);
  cursor: pointer;
}

.mobile-menu {
  display: none;
  flex-direction: column;
  padding: 6px 16px 14px;
  gap: 4px;
}

.mobile-link {
  padding: 10px 8px;
  border-radius: 8px;
  color: var(--text-secondary);
}

.mobile-link:hover {
  background: var(--accent-soft);
  color: var(--text);
}

@media (max-width: 900px) {
  .nav-links,
  .nav-actions {
    display: none;
  }
  .burger {
    display: block;
    margin-left: auto;
  }
  .mobile-menu {
    display: flex;
  }
}

.content {
  flex: 1;
}

.footer {
  text-align: center;
  padding: 32px 20px 40px;
  color: var(--text-muted);
  font-size: 13px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.page-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.page-leave-to {
  opacity: 0;
}
</style>