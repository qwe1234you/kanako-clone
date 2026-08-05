<script setup>
import { onMounted, ref } from 'vue'
import { friendApi } from '../api/modules'
import dayjs from 'dayjs'

const items = ref([])
const page = ref(1)
const size = 10
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)

async function load(append = false) {
  if (append) loadingMore.value = true
  else loading.value = true
  try {
    const list = (await friendApi.circle({ page: page.value, size })) || []
    items.value = append ? [...items.value, ...list] : list
    hasMore.value = list.length >= size
  } catch {
    hasMore.value = false
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function imagesOf(item) {
  return (item.images || '').split(',').map((s) => s.trim()).filter(Boolean)
}

function loadMore() {
  page.value += 1
  load(true)
}

function fmt(d) {
  return dayjs(d).format('YYYY-MM-DD HH:mm')
}

onMounted(() => load())
</script>

<template>
  <div class="page-container circle-page">
    <header class="page-header fade-up">
      <p class="eyebrow">Circle</p>
      <h1>朋友圈</h1>
      <p class="sub">友站的近况动态。</p>
    </header>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="items.length === 0" class="state">还没有动态。</div>
    <div v-else class="feed">
      <article v-for="(item, i) in items" :key="item.id" class="feed-item glass-blur fade-up"
        :style="{ animationDelay: `${Math.min(i, 6) * 50}ms` }">
        <div class="feed-head">
          <div class="avatar">
            <img v-if="item.linkAvatar" :src="item.linkAvatar" alt="" />
            <span v-else>{{ (item.linkName || '?').charAt(0) }}</span>
          </div>
          <div class="who">
            <a v-if="item.linkUrl" :href="item.linkUrl" target="_blank" rel="noopener noreferrer" class="name">
              {{ item.linkName || '友人' }}
            </a>
            <span v-else class="name">{{ item.linkName || '友人' }}</span>
            <span class="time">{{ fmt(item.createdAt) }}</span>
          </div>
        </div>
        <p class="content">{{ item.content }}</p>
        <div v-if="imagesOf(item).length" class="imgs" :class="{ multi: imagesOf(item).length > 1 }">
          <img v-for="(src, j) in imagesOf(item)" :key="j" :src="src" alt="" loading="lazy" />
        </div>
      </article>
    </div>

    <div v-if="hasMore" class="more">
      <button :disabled="loadingMore" @click="loadMore">
        {{ loadingMore ? '加载中…' : '加载更多' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.circle-page {
  max-width: 640px;
}

.sub {
  color: var(--text-muted);
  font-size: 14px;
  margin: 8px 0 0;
}

.state {
  text-align: center;
  color: var(--text-muted);
  padding: 80px 0;
}

.feed {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feed-item {
  padding: 18px 22px;
}

.feed-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  background: var(--accent-soft);
  color: var(--accent);
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.who {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.name:hover {
  color: var(--accent);
}

.time {
  font-size: 12px;
  color: var(--text-muted);
}

.content {
  margin: 0 0 12px;
  font-size: 14.5px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
}

.imgs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.imgs.multi img {
  width: calc(33.33% - 6px);
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 10px;
}

.imgs img {
  max-width: 100%;
  max-height: 320px;
  border-radius: 10px;
}

.more {
  text-align: center;
  margin-top: 18px;
}

.more button {
  background: transparent;
  border: 1px solid var(--border);
  color: var(--text-secondary);
  padding: 8px 26px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 13.5px;
}

.more button:hover {
  border-color: var(--accent);
  color: var(--accent);
}
</style>
