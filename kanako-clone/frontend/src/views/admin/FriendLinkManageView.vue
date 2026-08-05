<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { friendApi } from '../../api/modules'
import dayjs from 'dayjs'

const tab = ref('PENDING')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const loading = ref(false)

const circleForm = ref({ linkId: null, content: '', images: '' })
const publishing = ref(false)

const statusLabel = {
  PENDING: '待审核',
  SHOW: '已展示',
  HIDE: '已隐藏'
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size }
    if (tab.value !== 'ALL') params.status = tab.value
    const data = await friendApi.adminLinks(params)
    list.value = data?.records || []
    total.value = data?.total || 0
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function switchTab(t) {
  tab.value = t
  page.value = 1
  load()
}

async function setStatus(link, status) {
  await friendApi.setLinkStatus(link.id, status)
  ElMessage.success(status === 'SHOW' ? '已通过展示' : status === 'HIDE' ? '已隐藏' : '已恢复')
  load()
}

async function remove(link) {
  try {
    await ElMessageBox.confirm(`删除友链「${link.name}」？`, '确认', { type: 'warning' })
  } catch {
    return
  }
  await friendApi.deleteLink(link.id)
  ElMessage.success('已删除')
  load()
}

async function publish() {
  if (!circleForm.value.content.trim()) return ElMessage.warning('请输入动态内容')
  publishing.value = true
  try {
    await friendApi.createCircle({
      linkId: circleForm.value.linkId || undefined,
      content: circleForm.value.content,
      images: circleForm.value.images
    })
    ElMessage.success('已发布')
    circleForm.value = { linkId: null, content: '', images: '' }
  } catch (e) {
    ElMessage.error(e?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}

function fmt(d) {
  return dayjs(d).format('YYYY-MM-DD HH:mm')
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <p class="eyebrow">Admin</p>
      <h1>友链管理</h1>
    </header>

    <div class="cols">
      <section class="links-pane glass-blur">
        <el-tabs v-model="tab" @tab-change="switchTab">
          <el-tab-pane label="待审核" name="PENDING" />
          <el-tab-pane label="展示中" name="SHOW" />
          <el-tab-pane label="已隐藏" name="HIDE" />
          <el-tab-pane label="全部" name="ALL" />
        </el-tabs>

        <div v-if="loading" class="state">加载中…</div>
        <div v-else-if="list.length === 0" class="state">暂无记录。</div>
        <ul v-else class="link-list">
          <li v-for="l in list" :key="l.id" class="link-item">
            <div class="info">
              <a :href="l.url" target="_blank" rel="noopener noreferrer" class="name">{{ l.name }}</a>
              <span class="desc">{{ l.description || l.url }}</span>
              <span class="meta">{{ fmt(l.createdAt) }} · {{ statusLabel[l.status] }}</span>
            </div>
            <div class="ops">
              <el-button v-if="l.status !== 'SHOW'" size="small" type="primary" @click="setStatus(l, 'SHOW')">通过</el-button>
              <el-button v-if="l.status === 'SHOW'" size="small" @click="setStatus(l, 'HIDE')">隐藏</el-button>
              <el-button size="small" type="danger" link @click="remove(l)">删除</el-button>
            </div>
          </li>
        </ul>
        <div v-if="total > size" class="pager">
          <el-pagination
            background
            layout="prev, pager, next"
            :total="total"
            :page-size="size"
            :current-page="page"
            @current-change="(p) => { page = p; load() }"
          />
        </div>
      </section>

      <section class="circle-pane glass-blur">
        <h2 class="pane-title">发布朋友圈</h2>
        <el-input v-model="circleForm.linkId" placeholder="关联友链 ID（可留空）" />
        <el-input v-model="circleForm.content" type="textarea" :rows="4" placeholder="动态内容 *" />
        <el-input v-model="circleForm.images" placeholder="图片 URL，多个用英文逗号分隔" />
        <el-button type="primary" style="margin-top: 10px" :loading="publishing" @click="publish">发布</el-button>
      </section>
    </div>
  </div>
</template>

<style scoped>
.cols {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 18px;
  align-items: start;
}

@media (max-width: 900px) {
  .cols {
    grid-template-columns: 1fr;
  }
}

.links-pane,
.circle-pane {
  padding: 20px 24px;
}

.state {
  text-align: center;
  color: var(--text-muted);
  padding: 40px 0;
  font-size: 13.5px;
}

.link-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.link-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
}

.info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.name {
  font-size: 14.5px;
  font-weight: 600;
  color: var(--text);
}

.name:hover {
  color: var(--accent);
}

.desc {
  font-size: 12.5px;
  color: var(--text-muted);
  word-break: break-all;
}

.meta {
  font-size: 11.5px;
  color: var(--text-muted);
}

.ops {
  display: flex;
  gap: 6px;
  align-items: center;
}

.pane-title {
  margin: 0 0 14px;
  font-size: 16px;
}

.circle-pane :deep(.el-input) {
  margin-bottom: 10px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
