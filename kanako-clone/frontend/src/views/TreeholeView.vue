<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { treeholeApi } from '../api/modules'
import dayjs from 'dayjs'

const router = useRouter()

const holes = ref([])
const total = ref(0)
const page = ref(1)
const size = 12
const loading = ref(false)
const loadingMore = ref(false)

const form = ref({ nickname: '', content: '', mood: '' })
const moods = [
  { key: '', icon: '🌱', label: '平静' },
  { key: 'happy', icon: '😄', label: '开心' },
  { key: 'sad', icon: '😢', label: '难过' },
  { key: 'angry', icon: '😤', label: '生气' },
  { key: 'tired', icon: '😴', label: '疲惫' },
  { key: 'thinking', icon: '🤔', label: '思考' },
  { key: 'love', icon: '❤️', label: '心动' },
  { key: 'rain', icon: '🌧️', label: '雨天' }
]

function moodIcon(key) {
  return moods.find((m) => m.key === key)?.icon || '🌱'
}

const likedSet = ref(new Set())

async function loadHoles(append = false) {
  if (append) loadingMore.value = true
  else loading.value = true
  try {
    const data = await treeholeApi.page({ page: page.value, size })
    const records = data?.records || []
    holes.value = append ? [...holes.value, ...records] : records
    total.value = data?.total || 0
    const ids = holes.value.map((h) => h.id).filter((id) => localStorage.getItem(`hole-liked-${id}`))
    likedSet.value = new Set(ids)
  } catch {
    holes.value = []
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function submit() {
  if (!form.value.content.trim()) return
  try {
    const created = await treeholeApi.create({
      nickname: form.value.nickname,
      content: form.value.content,
      mood: form.value.mood
    })
    form.value.content = ''
    form.value.mood = ''
    page.value = 1
    await loadHoles()
    if (created) {
      const el = document.getElementById(`hole-${created}`)
      el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  } catch (e) {
    alert(e?.message || '发布失败')
  }
}

async function like(h) {
  if (likedSet.value.has(h.id)) return
  try {
    const n = await treeholeApi.like(h.id)
    h.likeCount = n
    likedSet.value.add(h.id)
    localStorage.setItem(`hole-liked-${h.id}`, '1')
  } catch {
    /* 静默 */
  }
}

function loadMore() {
  page.value += 1
  loadHoles(true)
}

function fmt(d) {
  return dayjs(d).format('MM-DD HH:mm')
}

onMounted(() => loadHoles())
</script>

<template>
  <div class="page-container treehole">
    <header class="page-header fade-up">
      <p class="eyebrow">Tree Hole</p>
      <h1>树洞</h1>
      <p class="sub">把想说的话丢进树洞，随风飘散。</p>
    </header>

    <div class="publish glass-blur fade-up">
      <div class="pub-row">
        <input v-model="form.nickname" class="nick" placeholder="昵称（可留空，默认匿名）" maxlength="20" />
        <div class="mood-pick">
          <button
            v-for="m in moods"
            :key="m.key"
            class="mood"
            :class="{ active: form.mood === m.key }"
            :title="m.label"
            @click="form.mood = m.key"
          >{{ m.icon }}</button>
        </div>
      </div>
      <textarea
        v-model="form.content"
        rows="3"
        maxlength="1000"
        placeholder="此刻的心情，或想说的话…"
      ></textarea>
      <div class="pub-foot">
        <span class="len">{{ form.content.length }}/1000</span>
        <button class="publish-btn" @click="submit">丢进树洞</button>
      </div>
    </div>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="holes.length === 0" class="state">树洞还空着，来丢第一句话吧。</div>
    <div v-else class="masonry">
      <article
        v-for="(h, i) in holes"
        :id="`hole-${h.id}`"
        :key="h.id"
        class="hole-card glass-blur fade-up"
        :style="{ animationDelay: `${Math.min(i % size, 6) * 40}ms`, borderTopColor: h.color || 'transparent' }"
      >
        <div class="hole-head">
          <span class="mood">{{ moodIcon(h.mood) }}</span>
          <span class="nick">{{ h.nickname || '匿名' }}</span>
          <span class="time">{{ fmt(h.createdAt) }}</span>
        </div>
        <p class="hole-content">{{ h.content }}</p>
        <div class="hole-foot">
          <button class="act" :class="{ liked: likedSet.has(h.id) }" @click="like(h)">
            ♥ {{ h.likeCount ?? 0 }}
          </button>
          <RouterLink :to="{ name: 'tree-hole-detail', params: { id: h.id } }" class="act">
            💬 评论
          </RouterLink>
          <span class="views">👁 {{ h.viewCount ?? 0 }}</span>
        </div>
      </article>
    </div>

    <div v-if="holes.length < total" class="more">
      <button :disabled="loadingMore" @click="loadMore">
        {{ loadingMore ? '加载中…' : '加载更多' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.treehole {
  max-width: 1080px;
}

.sub {
  color: var(--text-muted);
  font-size: 14px;
  margin: 8px 0 0;
}

.publish {
  padding: 20px 24px;
  margin-bottom: 28px;
  border-top: 3px solid var(--accent);
}

.pub-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.nick {
  flex: 1;
  min-width: 160px;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 9px 14px;
  color: var(--text);
  font-size: 14px;
  outline: none;
}

.nick:focus,
.publish textarea:focus {
  border-color: var(--accent);
}

.mood-pick {
  display: flex;
  gap: 6px;
}

.mood {
  border: none;
  background: transparent;
  font-size: 20px;
  padding: 4px;
  cursor: pointer;
  border-radius: 8px;
  opacity: 0.55;
  transition: all 0.15s;
}

.mood:hover {
  opacity: 1;
}

.mood.active {
  opacity: 1;
  background: var(--accent-soft);
}

.publish textarea {
  width: 100%;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 12px 14px;
  color: var(--text);
  font-size: 14.5px;
  line-height: 1.7;
  font-family: inherit;
  resize: vertical;
  outline: none;
}

.pub-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.len {
  font-size: 12px;
  color: var(--text-muted);
}

.publish-btn {
  background: var(--accent);
  color: #0c150e;
  border: none;
  padding: 8px 22px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.publish-btn:hover {
  opacity: 0.88;
}

.state {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 0;
}

.masonry {
  columns: 3 280px;
  column-gap: 16px;
}

.hole-card {
  break-inside: avoid;
  margin-bottom: 16px;
  padding: 18px 20px;
  border-top: 3px solid var(--border);
  transition: transform 0.2s, border-color 0.2s;
}

.hole-card:hover {
  transform: translateY(-2px);
}

.hole-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.mood {
  font-size: 16px;
}

.nick {
  font-size: 13px;
  font-weight: 600;
}

.time {
  margin-left: auto;
  font-size: 11.5px;
  color: var(--text-muted);
}

.hole-content {
  margin: 0 0 14px;
  font-size: 14px;
  line-height: 1.75;
  word-break: break-word;
  white-space: pre-wrap;
}

.hole-foot {
  display: flex;
  align-items: center;
  gap: 14px;
}

.act {
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  padding: 2px 0;
}

.act:hover,
.act.liked {
  color: var(--accent);
}

.views {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-muted);
}

.more {
  text-align: center;
  margin-top: 12px;
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
