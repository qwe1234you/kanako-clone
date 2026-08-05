<script setup>
import { nextTick, onMounted, ref } from 'vue'
import { chatApi } from '../api/modules'
import BlogRenderer from '../components/BlogRenderer.vue'
import { renderMarkdown } from '../utils/markdown'
import dayjs from 'dayjs'

const SESSION_KEY = 'kanako-chat-sid'

const messages = ref([])
const input = ref('')
const sending = ref(false)
const loading = ref(true)
const listRef = ref(null)

const sessionId = ref('')

function mdHtml(text) {
  return renderMarkdown(text).html
}

function fmt(d) {
  return dayjs(d).format('HH:mm')
}

async function ensureSession() {
  sessionId.value = localStorage.getItem(SESSION_KEY) || ''
  if (!sessionId.value) {
    sessionId.value = await chatApi.chatNew()
    localStorage.setItem(SESSION_KEY, sessionId.value)
  }
}

async function loadHistory() {
  try {
    const list = (await chatApi.chatHistory(sessionId.value)) || []
    messages.value = list.map((m) => ({
      role: m.role === 'AI' ? 'ai' : 'user',
      content: m.content,
      time: fmt(m.createdAt)
    }))
  } catch {
    messages.value = []
  } finally {
    loading.value = false
  }
}

async function scrollBottom() {
  await nextTick()
  listRef.value?.scrollTo({ top: listRef.value.scrollHeight })
}

async function send() {
  const msg = input.value.trim()
  if (!msg || sending.value) return
  messages.value.push({ role: 'user', content: msg, time: fmt(new Date()) })
  input.value = ''
  sending.value = true
  scrollBottom()
  try {
    const res = await chatApi.chatSend(sessionId.value, msg)
    if (res.sessionId && res.sessionId !== sessionId.value) {
      sessionId.value = res.sessionId
      localStorage.setItem(SESSION_KEY, res.sessionId)
    }
    messages.value.push({ role: 'ai', content: res.reply || '（无回复）', time: fmt(new Date()) })
  } catch (e) {
    messages.value.push({ role: 'ai', content: `请求失败：${e?.message || ''}`, time: fmt(new Date()) })
  } finally {
    sending.value = false
    scrollBottom()
  }
}

async function newChat() {
  try {
    sessionId.value = await chatApi.chatNew()
    localStorage.setItem(SESSION_KEY, sessionId.value)
  } catch {
    /* 静默 */
  }
  messages.value = []
  input.value = ''
}

function onEnter() {
  send()
}

onMounted(async () => {
  await ensureSession()
  await loadHistory()
  scrollBottom()
})
</script>

<template>
  <div class="chat-page">
    <header class="chat-head">
      <div>
        <p class="eyebrow">AI Chat</p>
        <h1>AI 助手</h1>
      </div>
      <el-button size="small" @click="newChat">新对话</el-button>
    </header>

    <div ref="listRef" class="chat-list">
      <div v-if="loading" class="state">加载中…</div>
      <div v-else-if="messages.length === 0" class="state">
        <p>和 AI 聊聊代码、生活或任何想法。</p>
      </div>
      <template v-else>
        <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
          <div class="avatar">{{ m.role === 'ai' ? 'AI' : '你' }}</div>
          <div class="bubble">
            <BlogRenderer v-if="m.role === 'ai'" :html="mdHtml(m.content)" />
            <pre v-else class="user-text">{{ m.content }}</pre>
            <span class="time">{{ m.time }}</span>
          </div>
        </div>
      </template>
    </div>

    <div class="chat-input">
      <textarea
        v-model="input"
        rows="1"
        placeholder="输入消息，Enter 发送（可换行用 Shift+Enter）"
        @keydown.enter.exact.prevent="onEnter"
      ></textarea>
      <button :disabled="sending || !input.trim()" @click="send">
        {{ sending ? '思考中…' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  min-height: 520px;
}

.chat-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.chat-head h1 {
  margin: 0;
  font-size: 26px;
}

.state {
  text-align: center;
  color: var(--text-muted);
  padding: 60px 0;
  font-size: 14px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 2px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.msg {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.msg.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.msg.user .avatar {
  background: var(--accent);
  color: #0c150e;
}

.msg.ai .avatar {
  background: var(--bg-card);
  border: 1px solid var(--border);
  color: var(--accent);
}

.bubble {
  max-width: 78%;
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 14.5px;
  line-height: 1.7;
  position: relative;
}

.msg.user .bubble {
  background: var(--accent-soft);
  border: 1px solid rgba(111, 207, 151, 0.25);
}

.msg.ai .bubble {
  background: var(--bg-card);
  border: 1px solid var(--border);
}

.user-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.bubble.v-html-fallback,
.bubble :deep(.blog-prose) {
  font-size: 14.5px;
}

.bubble :deep(.blog-prose p) {
  margin: 0 0 8px;
}

.bubble :deep(.blog-prose p:last-child) {
  margin-bottom: 0;
}

.time {
  display: block;
  margin-top: 6px;
  font-size: 11px;
  color: var(--text-muted);
  text-align: right;
}

.chat-input {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  align-items: flex-end;
  border: 1px solid var(--border);
  background: var(--bg-card);
  border-radius: 14px;
  padding: 10px 14px;
}

.chat-input textarea {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: var(--text);
  font-size: 14.5px;
  font-family: inherit;
  line-height: 1.6;
  resize: none;
  max-height: 160px;
}

.chat-input button {
  background: var(--accent);
  color: #0c150e;
  border: none;
  padding: 9px 22px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}

.chat-input button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>