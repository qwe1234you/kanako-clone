<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { blogApi } from '../api/modules'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const keyword = ref(route.query.q || '')
const results = ref([])
const searched = ref(false)
const loading = ref(false)

function highlight(text) {
  const q = keyword.value.trim()
  if (!q || !text) return text
  const esc = q.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const parts = String(text).split(new RegExp(`(${esc})`, 'ig'))
  return parts.map((p, i) =>
    p.toLowerCase() === q.toLowerCase() ? `<mark>${p}</mark>` : p
  ).join('')
}

async function doSearch() {
  const q = keyword.value.trim()
  router.replace({ query: q ? { q } : {} })
  if (!q) {
    results.value = []
    searched.value = false
    return
  }
  loading.value = true
  try {
    const data = await blogApi.search({ q })
    results.value = data?.blogs || []
    searched.value = true
  } catch {
    results.value = []
    searched.value = true
  } finally {
    loading.value = false
  }
}

function fmt(d) {
  return dayjs(d).format('YYYY-MM-DD')
}

let timer = null
watch(keyword, () => {
  clearTimeout(timer)
  timer = setTimeout(doSearch, 350)
})

onMounted(() => {
  if (keyword.value) doSearch()
})
</script>

<template>
  <div class="page-container search-page">
    <header class="page-header fade-up">
      <p class="eyebrow">Search</p>
      <h1>搜索</h1>
    </header>

    <div class="search-box fade-up">
      <el-input
        v-model="keyword"
        size="large"
        placeholder="输入关键词，搜索标题与正文…"
        clearable
        @keyup.enter="doSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="doSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <div v-if="loading" class="state">搜索中…</div>
    <div v-else-if="searched && results.length === 0" class="state">
      未找到与「{{ keyword }}」相关的文章。
    </div>
    <section v-else-if="results.length" class="results">
      <div v-for="(b, i) in results" :key="b.id" class="result-item glass-blur fade-up"
        :style="{ animationDelay: `${Math.min(i, 6) * 50}ms` }">
        <RouterLink :to="{ name: 'blog-detail', params: { slug: b.slug || b.id } }" class="result-link">
          <div class="meta">
            <span class="date">{{ fmt(b.createdAt) }}</span>
          </div>
          <h3 class="title" v-html="highlight(b.title)"></h3>
          <p v-if="b.summary" class="summary" v-html="highlight(b.summary)"></p>
        </RouterLink>
      </div>
    </section>
  </div>
</template>

<style scoped>
.search-page {
  max-width: 780px;
}

.search-box {
  margin-bottom: 28px;
}

.state {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 0;
}

.results {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.result-item {
  padding: 20px 24px;
  transition: transform 0.2s, border-color 0.2s;
}

.result-item:hover {
  transform: translateY(-2px);
  border-color: rgba(111, 207, 151, 0.35);
}

.result-link {
  color: var(--text);
}

.meta {
  font-size: 12.5px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.title {
  margin: 0 0 8px;
  font-size: 17.5px;
}

.result-item:hover .title {
  color: var(--accent);
}

.summary {
  margin: 0;
  font-size: 13.5px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

:deep(mark) {
  background: var(--accent-soft);
  color: var(--accent);
  border-radius: 3px;
  padding: 0 2px;
}
</style>
