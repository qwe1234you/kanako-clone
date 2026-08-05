<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { treeholeApi } from '../api/modules'
import dayjs from 'dayjs'

const route = useRoute()
const id = Number(route.params.id)

const hole = ref(null)
const comments = ref([])
const loading = ref(true)
const notFound = ref(false)

const form = ref({ nickname: '', content: '' })
const submitting = ref(false)
const liked = ref(false)

const moods = {
  happy: '😄',
  sad: '😢',
  angry: '😤',
  tired: '😴',
  thinking: '🤔',
  love: '❤️',
  rain: '🌧️'
}

async function load() {
  loading.value = true
  try {
    const [h, cs] = await Promise.all([
      treeholeApi.detail(id),
      treeholeApi.comments(id)
    ])
    if (!h) {
      notFound.value = true
      return
    }
    hole.value = h
    comments.value = cs || []
    liked.value = !!localStorage.getItem(`hole-liked-${h.id}`)
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

async function like() {
  if (!hole.value || liked.value) return
  try {
    hole.value.likeCount = await treeholeApi.like(hole.value.id)
    liked.value = true
    localStorage.setItem(`hole-liked-${hole.value.id}`, '1')
  } catch {
    /* 静默 */
  }
}

async function submit() {
  if (!form.value.content.trim()) return
  submitting.value = true
  try {
    await treeholeApi.comment(hole.value.id, {
      nickname: form.value.nickname,
      content: form.value.content
    })
    form.value.content = ''
    comments.value = (await treeholeApi.comments(hole.value.id)) || []
  } catch (e) {
    alert(e?.message || '评论失败')
  } finally {
    submitting.value = false
  }
}

function fmt(d) {
  return dayjs(d).format('YYYY-MM-DD HH:mm')
}

onMounted(load)
</script>

<template>
  <div class="page-container detail">
    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="notFound" class="state">
      <p>这条树洞不存在或已消失。</p>
      <RouterLink to="/tree-hole">← 返回树洞</RouterLink>
    </div>
    <template v-else-if="hole">
      <article class="hole-card glass-blur fade-up" :style="{ borderTopColor: hole.color || 'transparent' }">
        <div class="head">
          <span class="mood">{{ moods[hole.mood] || '🌱' }}</span>
          <span class="nick">{{ hole.nickname || '匿名' }}</span>
          <span class="time">{{ fmt(hole.createdAt) }}</span>
        </div>
        <p class="content">{{ hole.content }}</p>
        <div class="foot">
          <button class="act" :class="{ liked }" @click="like">♥ {{ hole.likeCount ?? 0 }}</button>
          <span class="views">👁 {{ hole.viewCount ?? 0 }} 次浏览</span>
        </div>
      </article>

      <section class="comments glass-blur">
        <h2 class="sec-title">回应 · {{ comments.length }}</h2>
        <div class="comment-form">
          <div class="row">
            <input v-model="form.nickname" placeholder="昵称（可留空）" maxlength="20" />
            <el-button type="primary" :loading="submitting" @click="submit">回应</el-button>
          </div>
          <textarea v-model="form.content" rows="3" maxlength="500" placeholder="说点什么…"></textarea>
        </div>
        <div v-if="comments.length === 0" class="no-comment">还没有回应。</div>
        <ul v-else class="list">
          <li v-for="c in comments" :key="c.id" class="item">
            <span class="c-nick">{{ c.nickname || '匿名' }}</span>
            <span class="c-time">{{ fmt(c.createdAt) }}</span>
            <p class="c-text">{{ c.content }}</p>
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>

<style scoped>
.detail {
  max-width: 720px;
}

.state {
  text-align: center;
  color: var(--text-muted);
  padding: 100px 0;
}

.hole-card {
  padding: 24px 28px;
  border-top: 3px solid var(--border);
}

.head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.mood {
  font-size: 22px;
}

.nick {
  font-size: 14.5px;
  font-weight: 600;
}

.time {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-muted);
}

.content {
  margin: 0 0 18px;
  font-size: 15.5px;
  line-height: 1.85;
  white-space: pre-wrap;
  word-break: break-word;
}

.foot {
  display: flex;
  align-items: center;
  gap: 16px;
}

.act {
  border: none;
  background: transparent;
  color: var(--text-muted);
  font-size: 14px;
  cursor: pointer;
  padding: 6px 14px;
  border: 1px solid var(--border);
  border-radius: 999px;
  transition: all 0.2s;
}

.act:hover,
.act.liked {
  color: var(--accent);
  border-color: var(--accent);
}

.views {
  margin-left: auto;
  font-size: 12.5px;
  color: var(--text-muted);
}

.comments {
  margin-top: 24px;
  padding: 24px 28px;
}

.sec-title {
  margin: 0 0 16px;
  font-size: 17px;
}

.comment-form .row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-form input,
.comment-form textarea {
  flex: 1;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 9px 14px;
  color: var(--text);
  font-size: 14px;
  font-family: inherit;
  outline: none;
}

.comment-form textarea {
  display: block;
  width: 100%;
  resize: vertical;
}

.comment-form input:focus,
.comment-form textarea:focus {
  border-color: var(--accent);
}

.no-comment {
  text-align: center;
  color: var(--text-muted);
  padding: 20px 0;
  font-size: 13.5px;
}

.list {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;
}

.item {
  padding: 14px 0;
  border-top: 1px solid var(--border);
}

.c-nick {
  font-size: 13.5px;
  font-weight: 600;
  margin-right: 10px;
}

.c-time {
  font-size: 12px;
  color: var(--text-muted);
}

.c-text {
  margin: 6px 0 0;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}
</style>
