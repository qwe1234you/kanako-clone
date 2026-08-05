<script setup>
import { computed, ref } from 'vue'

const pattern = ref('')
const flags = ref('g')
const text = ref('')
const result = ref(null)
const error = ref('')
const matchIndex = ref(0)

const matches = computed(() => {
  if (!pattern.value || error.value || !text.value) return []
  const re = new RegExp(pattern.value, flags.value)
  const arr = []
  let m
  const t = text.value
  while ((m = re.exec(t)) !== null) {
    const start = m.index
    const end = start + m[0].length
    const lines = t.slice(0, start).split('\n')
    arr.push({
      text: m[0],
      start,
      end,
      line: lines.length,
      col: lines[lines.length - 1].length + 1,
      groups: m.slice(1)
    })
    if (m[0] === '') re.lastIndex++
  }
  return arr
})

const highlighted = computed(() => {
  if (!matches.value.length) return text.value
  let out = ''
  let last = 0
  for (const m of matches.value) {
    out += escapeHtml(text.value.slice(last, m.start))
    out += `<mark>${escapeHtml(m.text)}</mark>`
    last = m.end
  }
  out += escapeHtml(text.value.slice(last))
  return out
})

function escapeHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

function run() {
  error.value = ''
  try {
    new RegExp(pattern.value, flags.value)
  } catch (e) {
    error.value = e.message
  }
}

function pickMatch(i) {
  matchIndex.value = i
  const m = matches.value[i]
  const el = document.querySelectorAll('.hl')[i]
  el?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}
</script>

<template>
  <div class="page-container tool">
    <header class="page-header">
      <p class="eyebrow">Tools</p>
      <h1>正则测试</h1>
    </header>

    <div class="tool-box glass-blur">
      <div class="pat-row">
        <input v-model="pattern" class="pat" placeholder="/ 正则表达式 /" @input="run" />
        <input v-model="flags" class="flags" placeholder="flags (gim)" @input="run" />
        <el-button @click="run">测试</el-button>
      </div>
      <p v-if="error" class="err">{{ error }}</p>

      <div class="panes">
        <textarea v-model="text" placeholder="待匹配文本…" class="area" @input="run"></textarea>
        <div class="area out">
          <div class="out-head">
            <span>匹配 {{ matches.length }} 处</span>
            <span class="muted">{{ matchIndex >= 0 && matches[matchIndex] ? `当前: 第 ${matches[matchIndex].line} 行，第 ${matches[matchIndex].col} 列` : '' }}</span>
          </div>
          <pre class="hl-text" v-html="highlighted"></pre>
        </div>
      </div>

      <div v-if="matches.length" class="m-list">
        <button
          v-for="(m, i) in matches"
          :key="i"
          class="m-item"
          :class="{ active: matchIndex === i }"
          @click="pickMatch(i)"
        >
          <span class="m-idx">{{ i + 1 }}</span>
          <code>{{ m.text }}</code>
          <span class="m-pos">L{{ m.line }}:{{ m.col }}</span>
          <span v-if="m.groups.length" class="m-groups">{{ m.groups.map((g, gi) => `$${gi + 1}=${g}`).join(' ') }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tool {
  max-width: 980px;
}

.tool-box {
  padding: 20px;
}

.pat-row {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.pat {
  flex: 1;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px 14px;
  color: var(--text);
  font-family: var(--font-mono);
  font-size: 14px;
  outline: none;
}

.flags {
  width: 100px;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px 14px;
  color: var(--text);
  font-family: var(--font-mono);
  outline: none;
}

.pat:focus,
.flags:focus {
  border-color: var(--accent);
}

.err {
  color: var(--danger);
  font-size: 12.5px;
  font-family: var(--font-mono);
  margin: 0 0 10px;
}

.panes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

@media (max-width: 760px) {
  .panes {
    grid-template-columns: 1fr;
  }
}

.area {
  min-height: 320px;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 14px;
  color: var(--text);
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.7;
  outline: none;
  resize: vertical;
}

.area:focus {
  border-color: var(--accent);
}

.out {
  overflow: auto;
}

.out-head {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text);
  margin-bottom: 8px;
}

.muted {
  color: var(--text-muted);
}

.hl-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
}

.hl-text :deep(mark) {
  background: var(--accent-soft);
  color: var(--accent);
  border-radius: 3px;
}

.m-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 240px;
  overflow-y: auto;
}

.m-item {
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  color: var(--text);
  font-size: 12.5px;
}

.m-item.active {
  border-color: var(--accent);
}

.m-item:hover {
  border-color: var(--accent);
}

.m-idx {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: var(--accent-soft);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  flex-shrink: 0;
}

.m-item code {
  font-family: var(--font-mono);
  color: var(--accent);
  word-break: break-all;
}

.m-pos {
  color: var(--text-muted);
  font-size: 11.5px;
  white-space: nowrap;
}

.m-groups {
  color: var(--text-secondary);
  font-size: 11.5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
