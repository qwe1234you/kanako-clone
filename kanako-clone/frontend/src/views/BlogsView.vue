<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { blogApi } from '../api/modules'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const blogs = ref([])
const total = ref(0)
const categories = ref([])
const tags = ref([])
const page = ref(1)
const size = 10
const loading = ref(false)

const activeCategory = ref(route.query.category || '')
const activeTag = ref(route.query.tag || '')

function loadCategories() {
  blogApi.categories().then((list) => {
    categories.value = list || []
  }).catch(() => {})
}

function loadTags() {
  blogApi.tags().then((list) => {
    tags.value = list || []
  }).catch(() => {})
}

async function loadBlogs() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (activeCategory.value) params.category = activeCategory.value
    if (activeTag.value) params.tag = activeTag.value
    const data = await blogApi.page(params)
    blogs.value = data?.records || []
    total.value = data?.total || 0
  } catch {
    blogs.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function pickCategory(slug) {
  activeCategory.value = slug
  activeTag.value = ''
  page.value = 1
  syncQuery()
}

function pickTag(slug) {
  activeTag.value = slug
  activeCategory.value = ''
  page.value = 1
  syncQuery()
}

function syncQuery() {
  const q = {}
  if (activeCategory.value) q.category = activeCategory.value
  if (activeTag.value) q.tag = activeTag.value
  router.replace({ query: q })
}

function changePage(p) {
  page.value = p
  loadBlogs()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function fmt(d) {
  return dayjs(d).format('YYYY-MM-DD')
}

watch(activeCategory, loadBlogs)
watch(activeTag, loadBlogs)

onMounted(() => {
  loadCategories()
  loadTags()
  loadBlogs()
})
</script>

<template>
  <div class="page-container blogs-page">
    <header class="page-header fade-up">
      <p class="eyebrow">Blog</p>
      <h1>博客文章</h1>
    </header>

    <div v-if="categories.length" class="cats fade-up">
      <button
        class="cat-chip"
        :class="{ active: activeCategory === '' && !activeTag }"
        @click="pickCategory('')"
      >全部</button>
      <button
        v-for="c in categories"
        :key="c.id"
        class="cat-chip"
        :class="{ active: activeCategory === c.slug }"
        @click="pickCategory(c.slug)"
      >{{ c.name }} <span class="cnt">{{ c.count }}</span></button>
    </div>

    <div v-if="tags.length" class="cats fade-up">
      <button
        v-for="t in tags"
        :key="t.id"
        class="cat-chip"
        :class="{ active: activeTag === t.slug }"
        @click="pickTag(t.slug)"
      >{{ t.name }} <span class="cnt">{{ t.count }}</span></button>
    </div>

    <section class="list">
      <div v-if="loading" class="state">加载中…</div>
      <div v-else-if="blogs.length === 0" class="state">该分类下暂无文章。</div>
      <article
        v-for="(blog, i) in blogs"
        :key="blog.id"
        class="post-card glass-blur fade-up"
        :style="{ animationDelay: `${Math.min(i, 6) * 50}ms` }"
      >
        <RouterLink :to="{ name: 'blog-detail', params: { slug: blog.slug || blog.id } }" class="post-link">
          <div class="post-meta">
            <span class="date">{{ fmt(blog.createdAt) }}</span>
            <span v-if="blog.category" class="cat">{{ blog.category.name }}</span>
            <span v-if="blog.isTop" class="top">置顶</span>
          </div>
          <h2 class="post-title">{{ blog.title }}</h2>
          <p v-if="blog.summary" class="post-summary">{{ blog.summary }}</p>
        </RouterLink>
        <div v-if="blog.tags?.length" class="post-tags">
          <span v-for="t in blog.tags" :key="t.id" class="tag"># {{ t.name }}</span>
        </div>
      </article>
    </section>

    <div v-if="total > size" class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="changePage"
      />
    </div>
  </div>
</template>

<style scoped>
.blogs-page {
  max-width: 860px;
}

.cats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 24px;
}

.cat-chip {
  padding: 6px 14px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-secondary);
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.2s;
}

.cat-chip:hover {
  color: var(--accent);
  border-color: var(--accent);
}

.cat-chip.active {
  background: var(--accent-soft);
  color: var(--accent);
  border-color: var(--accent);
}

.cnt {
  font-size: 11.5px;
  color: var(--text-muted);
}

.list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.state {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 0;
}

.post-card {
  padding: 22px 26px;
  transition: transform 0.2s, border-color 0.2s;
}

.post-card:hover {
  transform: translateY(-2px);
  border-color: rgba(111, 207, 151, 0.35);
}

.post-link {
  display: block;
  color: var(--text);
}

.post-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 12.5px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.cat {
  color: var(--accent);
}

.top {
  background: var(--accent-soft);
  color: var(--accent);
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 11.5px;
}

.post-title {
  margin: 0 0 8px;
  font-size: 20px;
  transition: color 0.2s;
}

.post-card:hover .post-title {
  color: var(--accent);
}

.post-summary {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-tags {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.tag {
  font-size: 12px;
  color: var(--text-muted);
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
