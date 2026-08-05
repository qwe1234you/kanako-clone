<script setup>
import { onMounted, ref } from 'vue'
import { friendApi } from '../api/modules'

const links = ref([])
const loading = ref(true)
const showApply = ref(false)
const applyForm = ref({ name: '', url: '', description: '' })
const submitting = ref(false)

async function load() {
  try {
    links.value = (await friendApi.links()) || []
  } catch {
    links.value = []
  } finally {
    loading.value = false
  }
}

async function submitApply() {
  if (!applyForm.value.name || !applyForm.value.url) return
  submitting.value = true
  try {
    await friendApi.apply(applyForm.value)
    applyForm.value = { name: '', url: '', description: '' }
    showApply.value = false
    alert('申请已提交，等待博主审核～')
  } catch (e) {
    alert(e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <header class="page-header fade-up">
      <p class="eyebrow">Friends</p>
      <h1>友情链接</h1>
      <p class="sub">交换链接，一起玩。</p>
    </header>

    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="links.length === 0" class="state">暂无友链。</div>
    <div v-else class="grid">
      <a
        v-for="(l, i) in links"
        :key="l.id"
        :href="l.url"
        target="_blank"
        rel="noopener noreferrer"
        class="link-card glass-blur fade-up"
        :style="{ animationDelay: `${Math.min(i, 6) * 50}ms` }"
      >
        <div class="avatar">
          <img v-if="l.avatar" :src="l.avatar" alt="" />
          <span v-else>{{ (l.name || '?').charAt(0) }}</span>
        </div>
        <div class="info">
          <h3>{{ l.name }}</h3>
          <p v-if="l.description">{{ l.description }}</p>
          <p v-else class="muted">{{ l.url }}</p>
        </div>
      </a>
    </div>

    <div class="apply-zone glass-blur fade-up">
      <template v-if="!showApply">
        <p class="apply-tip">想交换友链？把你的博客丢过来。</p>
        <button class="apply-btn" @click="showApply = true">申请友链</button>
      </template>
      <template v-else>
        <h3 class="apply-title">申请友链</h3>
        <input v-model="applyForm.name" placeholder="网站名称 *" />
        <input v-model="applyForm.url" placeholder="网站地址（http(s)://）*" />
        <input v-model="applyForm.description" placeholder="一句话介绍（可选）" />
        <div class="apply-foot">
          <el-button @click="showApply = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="submitApply">提交申请</el-button>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
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

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.link-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  color: var(--text);
  transition: transform 0.2s, border-color 0.2s;
}

.link-card:hover {
  transform: translateY(-2px);
  border-color: rgba(111, 207, 151, 0.35);
}

.avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  flex-shrink: 0;
  background: var(--accent-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
  overflow: hidden;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.info {
  min-width: 0;
}

.info h3 {
  margin: 0 0 4px;
  font-size: 15.5px;
}

.link-card:hover h3 {
  color: var(--accent);
}

.info p {
  margin: 0;
  font-size: 12.5px;
  color: var(--text-muted);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-all;
}

.apply-zone {
  margin-top: 32px;
  padding: 26px 30px;
  text-align: center;
}

.apply-tip {
  color: var(--text-muted);
  margin: 0 0 14px;
  font-size: 14px;
}

.apply-btn {
  background: transparent;
  border: 1px solid var(--accent);
  color: var(--accent);
  padding: 8px 28px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.apply-btn:hover {
  background: var(--accent-soft);
}

.apply-title {
  margin: 0 0 16px;
  font-size: 16px;
}

.apply-zone input {
  display: block;
  width: 100%;
  max-width: 420px;
  margin: 0 auto 12px;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px 14px;
  color: var(--text);
  font-size: 14px;
  outline: none;
}

.apply-zone input:focus {
  border-color: var(--accent);
}

.apply-foot {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 6px;
}
</style>
