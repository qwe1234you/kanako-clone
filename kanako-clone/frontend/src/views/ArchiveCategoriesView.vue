<script setup>
import { onMounted, ref } from 'vue'
import { blogApi } from '../api/modules'

const categories = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const list = await blogApi.categories()
    categories.value = (list || []).slice().sort((a, b) => (b.count || 0) - (a.count || 0))
  } catch {
    categories.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container">
    <header class="page-header fade-up">
      <p class="eyebrow">Archive</p>
      <h1>分类归档</h1>
    </header>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="categories.length === 0" class="state">暂无分类。</div>
    <div v-else class="grid">
      <RouterLink
        v-for="(c, i) in categories"
        :key="c.id"
        :to="{ path: '/blogs', query: { category: c.slug } }"
        class="cat-card glass-blur fade-up"
        :style="{ animationDelay: `${Math.min(i, 6) * 50}ms` }"
      >
        <div class="card-head">
          <h2>{{ c.name }}</h2>
          <span class="count">{{ c.count }} 篇</span>
        </div>
        <p v-if="c.description" class="desc">{{ c.description }}</p>
        <p v-else class="desc muted">暂无描述</p>
        <span class="go">查看文章 →</span>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.state {
  text-align: center;
  color: var(--text-muted);
  padding: 80px 0;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.cat-card {
  display: block;
  padding: 22px 24px;
  color: var(--text);
  transition: transform 0.2s, border-color 0.2s;
}

.cat-card:hover {
  transform: translateY(-2px);
  border-color: rgba(111, 207, 151, 0.35);
}

.card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.card-head h2 {
  margin: 0;
  font-size: 19px;
}

.cat-card:hover h2 {
  color: var(--accent);
}

.count {
  font-size: 12.5px;
  color: var(--accent);
  white-space: nowrap;
}

.desc {
  margin: 10px 0 14px;
  font-size: 13.5px;
  color: var(--text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.desc.muted {
  color: var(--text-muted);
}

.go {
  font-size: 13px;
}
</style>
