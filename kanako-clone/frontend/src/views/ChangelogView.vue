<script setup>
import { onMounted, ref } from 'vue'
import { siteApi } from '../api/modules'
import BlogRenderer from '../components/BlogRenderer.vue'
import { renderMarkdown } from '../utils/markdown'

const logs = ref([])
const loading = ref(true)
const htmlCache = new Map()

function mdHtml(md) {
  if (!htmlCache.has(md)) htmlCache.set(md, renderMarkdown(md).html)
  return htmlCache.get(md)
}

onMounted(async () => {
  try {
    logs.value = (await siteApi.changelog()) || []
  } catch {
    logs.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container">
    <header class="page-header fade-up">
      <p class="eyebrow">Changelog</p>
      <h1>更新日志</h1>
    </header>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="logs.length === 0" class="state">暂无更新记录。</div>
    <div v-else class="timeline">
      <div v-for="(log, i) in logs" :key="log.id" class="tl-item fade-up"
        :style="{ animationDelay: `${Math.min(i, 6) * 50}ms` }">
        <div class="tl-dot"></div>
        <div class="tl-card glass-blur">
          <div class="tl-head">
            <span class="version">{{ log.version }}</span>
            <span class="date">{{ log.date }}</span>
          </div>
          <BlogRenderer :html="mdHtml(log.contentMd)" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.state {
  text-align: center;
  color: var(--text-muted);
  padding: 80px 0;
}

.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 0;
  bottom: 0;
  width: 1px;
  background: var(--border);
}

.tl-item {
  position: relative;
  margin-bottom: 20px;
}

.tl-dot {
  position: absolute;
  left: -24px;
  top: 20px;
  width: 13px;
  height: 13px;
  border-radius: 50%;
  background: var(--accent);
  border: 3px solid var(--bg);
  box-shadow: 0 0 0 1px var(--accent);
}

.tl-card {
  padding: 18px 22px;
  font-size: 14.5px;
  line-height: 1.75;
}

.tl-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--border);
}

.version {
  font-weight: 700;
  font-size: 15.5px;
  color: var(--accent);
  font-family: var(--font-mono);
}

.date {
  font-size: 12.5px;
  color: var(--text-muted);
}
</style>
