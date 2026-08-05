<script setup>
import { ref, watch } from 'vue'
import dayjs from 'dayjs'

const input = ref('')
const result = ref('')
const dateInput = ref('')
const dateResult = ref('')
const now = ref(dayjs())

function parseTimestamp() {
  const s = input.value.trim()
  if (!s) {
    result.value = ''
    return
  }
  let ts = Number(s)
  if (isNaN(ts)) {
    result.value = '无法解析'
    return
  }
  const ms = String(s).length <= 10 ? ts * 1000 : ts
  const d = dayjs(ms)
  if (!d.isValid()) {
    result.value = '无效时间戳'
    return
  }
  result.value = `${d.format('YYYY-MM-DD HH:mm:ss')}  ${d.format('YYYY-MM-DD HH:mm:ss.SSS')}  ${d.format('ddd')}  (${d.format('YYYY年M月D日')})`
}

function parseDate() {
  const s = dateInput.value.trim()
  if (!s) {
    dateResult.value = ''
    return
  }
  const d = dayjs(s)
  if (!d.isValid()) {
    dateResult.value = '无法解析（支持 2026-08-05、2026-08-05 12:00:00 等格式）'
    return
  }
  dateResult.value = `秒级: ${Math.floor(d.valueOf() / 1000)}    毫秒级: ${d.valueOf()}`
}

watch(input, parseTimestamp)
watch(dateInput, parseDate)

function fillNow() {
  now.value = dayjs()
  input.value = String(Math.floor(now.value.valueOf() / 1000))
  parseTimestamp()
}

function toNow() {
  now.value = dayjs()
  dateInput.value = now.value.format('YYYY-MM-DD HH:mm:ss')
  parseDate()
}
</script>

<template>
  <div class="page-container tool">
    <header class="page-header">
      <p class="eyebrow">Tools</p>
      <h1>时间戳转换</h1>
    </header>

    <div class="tool-box glass-blur">
      <section class="part">
        <h2>时间戳 → 日期</h2>
        <div class="row">
          <input v-model="input" class="ipt" placeholder="输入秒级或毫秒级时间戳" />
          <el-button @click="fillNow">填入当前时间</el-button>
        </div>
        <p v-if="result" class="out-text">{{ result }}</p>
      </section>

      <section class="part">
        <h2>日期 → 时间戳</h2>
        <div class="row">
          <input v-model="dateInput" class="ipt" placeholder="如 2026-08-05 12:00:00" />
          <el-button @click="toNow">填入当前时间</el-button>
        </div>
        <p v-if="dateResult" class="out-text">{{ dateResult }}</p>
      </section>

      <section class="part now-box">
        <p>当前时间：<b>{{ now.format('YYYY-MM-DD HH:mm:ss') }}</b></p>
        <p class="muted">秒级时间戳：{{ Math.floor(now.valueOf() / 1000) }}</p>
        <p class="muted">毫秒级时间戳：{{ now.valueOf() }}</p>
      </section>
    </div>
  </div>
</template>

<style scoped>
.tool {
  max-width: 680px;
}

.tool-box {
  padding: 26px 30px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.part h2 {
  font-size: 15px;
  margin: 0 0 12px;
}

.row {
  display: flex;
  gap: 10px;
}

.ipt {
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

.ipt:focus {
  border-color: var(--accent);
}

.out-text {
  margin: 10px 0 0;
  color: var(--accent);
  font-family: var(--font-mono);
  font-size: 13.5px;
  word-break: break-all;
}

.now-box {
  border-top: 1px dashed var(--border);
  padding-top: 18px;
}

.now-box p {
  margin: 0 0 6px;
  font-size: 13.5px;
}

.muted {
  color: var(--text-muted);
  font-family: var(--font-mono);
  font-size: 12.5px;
}
</style>
