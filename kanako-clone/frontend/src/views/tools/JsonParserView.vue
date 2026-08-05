<script setup>
import { ref } from 'vue'

const input = ref('')
const output = ref('')
const error = ref('')
const indent = ref(2)

function format() {
  error.value = ''
  try {
    const obj = JSON.parse(input.value)
    output.value = JSON.stringify(obj, null, indent.value)
  } catch (e) {
    error.value = e.message
    output.value = ''
  }
}

function minify() {
  error.value = ''
  try {
    output.value = JSON.stringify(JSON.parse(input.value))
  } catch (e) {
    error.value = e.message
    output.value = ''
  }
}

function validate() {
  error.value = ''
  try {
    JSON.parse(input.value)
    alert('JSON 有效 ✓')
  } catch (e) {
    error.value = e.message
  }
}

async function copy() {
  try {
    await navigator.clipboard.writeText(output.value)
  } catch {
    /* 静默 */
  }
}
</script>

<template>
  <div class="page-container tool">
    <header class="page-header">
      <p class="eyebrow">Tools</p>
      <h1>JSON 格式化</h1>
    </header>

    <div class="tool-box glass-blur">
      <div class="row">
        <el-button type="primary" @click="format">格式化</el-button>
        <el-button @click="minify">压缩</el-button>
        <el-button @click="validate">校验</el-button>
        <el-select v-model="indent" style="width: 110px" size="default">
          <el-option label="2 空格" :value="2" />
          <el-option label="4 空格" :value="4" />
          <el-option label="Tab" :value="'\t'" />
        </el-select>
      </div>
      <div class="panes">
        <textarea v-model="input" placeholder="粘贴 JSON 文本…" class="area"></textarea>
        <div class="area out">
          <button class="copy-btn" @click="copy">复制</button>
          <pre>{{ output || (error ? '' : '结果将显示在这里') }}</pre>
        </div>
      </div>
      <p v-if="error" class="err">{{ error }}</p>
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

.row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 14px;
  flex-wrap: wrap;
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
  min-height: 380px;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 14px;
  color: var(--text);
  font-family: var(--font-mono);
  font-size: 13px;
  line-height: 1.6;
  outline: none;
  resize: vertical;
}

.area:focus {
  border-color: var(--accent);
}

.out {
  position: relative;
  overflow: auto;
}

.out pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  color: var(--text-secondary);
}

.copy-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  border: 1px solid var(--border);
  background: var(--bg-card);
  color: var(--text-muted);
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  cursor: pointer;
  z-index: 2;
}

.copy-btn:hover {
  color: var(--accent);
}

.err {
  color: var(--danger);
  font-size: 13px;
  margin: 12px 0 0;
  font-family: var(--font-mono);
  word-break: break-all;
}
</style>
