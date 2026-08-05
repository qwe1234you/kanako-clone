import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/DefaultLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('../views/HomeView.vue') },
      { path: 'blogs', name: 'blogs', component: () => import('../views/BlogsView.vue') },
      { path: 'blogs/:slug', name: 'blog-detail', component: () => import('../views/BlogDetailView.vue') },
      { path: 'search', name: 'search', component: () => import('../views/SearchView.vue') },
      { path: 'archive/categories', name: 'archive-categories', component: () => import('../views/ArchiveCategoriesView.vue') },
      { path: 'archive/tags', name: 'archive-tags', component: () => import('../views/ArchiveTagsView.vue') },
      { path: 'tree-hole', name: 'tree-hole', component: () => import('../views/TreeholeView.vue') },
      { path: 'tree-hole/:id', name: 'tree-hole-detail', component: () => import('../views/TreeholeDetailView.vue') },
      { path: 'friend-links', name: 'friend-links', component: () => import('../views/FriendLinksView.vue') },
      { path: 'friend-circle', name: 'friend-circle', component: () => import('../views/FriendCircleView.vue') },
      { path: 'chat', name: 'chat', component: () => import('../views/ChatView.vue') },
      { path: 'about', name: 'about', component: () => import('../views/AboutView.vue') },
      { path: 'tools/json', name: 'tools-json', component: () => import('../views/tools/JsonParserView.vue') },
      { path: 'tools/regex', name: 'tools-regex', component: () => import('../views/tools/RegexTesterView.vue') },
      { path: 'tools/timestamp', name: 'tools-timestamp', component: () => import('../views/tools/TimestampConverterView.vue') },
      { path: 'changelog', name: 'changelog', component: () => import('../views/ChangelogView.vue') },
      // 管理页
      { path: 'blog/write', name: 'blog-write', component: () => import('../views/admin/BlogEditorView.vue'), meta: { admin: true } },
      { path: 'blog/edit/:id', name: 'blog-edit', component: () => import('../views/admin/BlogEditorView.vue'), meta: { admin: true } },
      { path: 'admin/site', name: 'site-settings', component: () => import('../views/admin/SiteSettingsView.vue'), meta: { admin: true } },
      { path: 'admin/oss', name: 'oss-manage', component: () => import('../views/admin/OssManageView.vue'), meta: { admin: true } },
      { path: 'admin/friend-links', name: 'friend-link-manage', component: () => import('../views/admin/FriendLinkManageView.vue'), meta: { admin: true } }
    ]
  },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/auth/github/callback', name: 'github-auth-callback', component: () => import('../views/GithubAuthCallbackView.vue') },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.admin) {
    auth.tryRestore()
    if (!auth.isAdmin) {
      return { name: 'login', query: { redirect: to.fullPath } }
    }
  }
  document.title = to.meta.title ? `${to.meta.title} · Kanako` : 'Kanako · Space'
})

export default router