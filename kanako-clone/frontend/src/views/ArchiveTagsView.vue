<script setup>
import { onMounted, ref } from 'vue'
import { blogApi } from '../api/modules'

const tags = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const list = await blogApi.tags()
    tags.value = (list || []).slice().sort((a, b) => (b.count || 0) - (a.count || 0))
  } catch {
    tags.value = []
  } finally {
    loading.value = false
  }
})

function sizeOf(count) {
  const n = count || 0
  if (n >= 8) return 'xl'
  if (n >= 4) return 'lg'
  if (n >= 2) return 'md'
  return 'sm'
}
</script>

<template>
  <div class="page-container">
    <header class="page-header fade-up">
      <p class="eyebrow">Archive</p>
      <h1>标签归档</h1>
    </header>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="tags.length === 0" class="state">暂无标签。</div>
    <div v-else class="cloud fade-up">
      <RouterLink
        v-for="t in tags"
        :key="t.id"
        :to="{ path: '/blogs', query: { tag: t.slug } }"
        class="tag-item"
        :class="`sz-${sizeOf(t.count)}`"
      >
        # {{ t.name }} <span class="cnt">{{ t.count }}</span>
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

.cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 14px;
  padding: 20px 0;
}

.tag-item {
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  padding: 6px 16px;
  border: 1px solid var(--border);
  border-radius: 999px;
  color: var(--text-secondary);
  font-size: 14px;
  transition: all 0.2s;
}

.tag-item:hover {
  color: var(--accent);
  border-color: var(--accent);
  transform: translateY(-2px);
}

.cnt {
  font-size: 11.5px;
  color: var(--text-muted);
}

.sz-sm {
  font-size: 13px;
}

.sz-md {
  font-size: 14.5px;
}

.sz-lg {
  font-size: 17px;
  color: var(--text);
}

.sz-xl {
  font-size: 20px;
  color: var(--accent);
  border-color: rgba(111, 207, 151, 0.4);
}
</style>
