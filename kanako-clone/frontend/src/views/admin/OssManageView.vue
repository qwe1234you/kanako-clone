<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ossApi } from '../../api/modules'

const path = ref('')
const dirs = ref([])
const files = ref([])
const loading = ref(false)

const uploadFileRef = ref(null)
const uploading = ref(false)
const mkdirVisible = ref(false)
const mkdirName = ref('')
const renaming = ref(null)
const renameValue = ref('')

async function load() {
  loading.value = true
  try {
    const data = await ossApi.list({ path: path.value })
    dirs.value = data?.dirs || []
    files.value = data?.files || []
  } catch (e) {
    ElMessage.error(e?.message || '无法读取目录')
  } finally {
    loading.value = false
  }
}

function enterDir(name) {
  path.value = path.value ? `${path.value}/${name}` : name
  load()
}

function goUp() {
  if (!path.value) return
  const idx = path.value.lastIndexOf('/')
  path.value = idx > 0 ? path.value.slice(0, idx) : ''
  load()
}

function fmtSize(bytes) {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = Math.floor(Math.log(bytes) / Math.log(1024))
  return `${(bytes / 1024 ** i).toFixed(1)} ${units[i]}`
}

function fmtTime(ms) {
  return new Date(ms).toLocaleString()
}

function pickFiles() {
  uploadFileRef.value?.click()
}

async function upload(filesList) {
  if (!filesList?.length) return
  uploading.value = true
  try {
    const urls = await ossApi.upload(filesList, path.value)
    ElMessage.success(`已上传 ${urls.length} 个文件`)
    load()
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function mkdir() {
  if (!mkdirName.value.trim()) return
  try {
    await ossApi.mkdir(path.value, mkdirName.value.trim())
    mkdirName.value = ''
    mkdirVisible.value = false
    ElMessage.success('已创建')
    load()
  } catch (e) {
    ElMessage.error(e?.message || '创建失败')
  }
}

function startRename(item) {
  renaming.value = item
  renameValue.value = item.name
}

async function doRename(item) {
  const newName = renameValue.value.trim()
  if (!newName || newName === item.name) {
    renaming.value = null
    return
  }
  const fullPath = path.value ? `${path.value}/${item.name}` : item.name
  try {
    await ossApi.rename(fullPath, newName)
    renaming.value = null
    ElMessage.success('已重命名')
    load()
  } catch (e) {
    ElMessage.error(e?.message || '重命名失败')
  }
}

async function remove(item) {
  const fullPath = path.value ? `${path.value}/${item.name}` : item.name
  try {
    await ElMessageBox.confirm(
      item.isDir ? `删除目录「${item.name}」？目录必须为空。` : `删除文件「${item.name}」？`,
      '确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await ossApi.remove(fullPath)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

function copyUrl(name) {
  const url = path.value ? `/${path.value}/${name}` : `/${name}`
  navigator.clipboard.writeText(url).then(() => ElMessage.success('路径已复制'))
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <p class="eyebrow">Admin</p>
      <h1>文件管理</h1>
    </header>

    <div class="toolbar glass-blur">
      <div class="crumb">
        <a href="#" @click.prevent="path = ''; load()">根目录</a>
        <template v-for="(seg, i) in path.split('/')" :key="i">
          <span> / </span>
          <a href="#" @click.prevent="path = path.split('/').slice(0, i + 1).join('/'); load()">{{ seg }}</a>
        </template>
      </div>
      <div class="btns">
        <el-button @click="mkdirVisible = true">新建目录</el-button>
        <input
          ref="uploadFileRef"
          type="file"
          multiple
          style="display: none"
          @change="upload([...$event.target.files]); $event.target.value = ''"
        />
        <el-button type="primary" :loading="uploading" @click="pickFiles">上传文件</el-button>
      </div>
    </div>

    <div v-loading="loading" class="panel glass-blur">
      <div v-if="path" class="up-row">
        <el-button size="small" link @click="goUp">⬆ 返回上级</el-button>
      </div>

      <div v-if="dirs.length" class="sec">
        <p class="sec-title">目录</p>
        <div class="entries">
          <div v-for="d in dirs" :key="d.name" class="entry dir" @dblclick="enterDir(d.name)">
            <span class="icon">📁</span>
            <span class="name">{{ d.name }}</span>
            <div class="ops">
              <el-button size="small" link @click="enterDir(d.name)">进入</el-button>
              <el-button size="small" link type="danger" @click="remove(d)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="files.length" class="sec">
        <p class="sec-title">文件</p>
        <div class="entries">
          <div v-for="f in files" :key="f.name" class="entry">
            <span class="icon">📄</span>
            <template v-if="renaming === f">
              <input v-model="renameValue" class="rename-ipt" @keyup.enter="doRename(f)" @blur="doRename(f)" />
              <div class="ops">
                <el-button size="small" link type="primary" @click="doRename(f)">确定</el-button>
              </div>
            </template>
            <template v-else>
              <span class="name">{{ f.name }}</span>
              <span class="meta">{{ fmtSize(f.size) }} · {{ fmtTime(f.lastModified) }}</span>
              <div class="ops">
                <el-button size="small" link @click="copyUrl(f.name)">复制路径</el-button>
                <el-button size="small" link @click="startRename(f)">重命名</el-button>
                <el-button size="small" link type="danger" @click="remove(f)">删除</el-button>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div v-if="!dirs.length && !files.length && !loading" class="empty">此目录为空。</div>
    </div>

    <el-dialog v-model="mkdirVisible" title="新建目录" width="360px">
      <el-input v-model="mkdirName" placeholder="目录名" @keyup.enter="mkdir" />
      <template #footer>
        <el-button @click="mkdirVisible = false">取消</el-button>
        <el-button type="primary" @click="mkdir">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.crumb {
  font-size: 13.5px;
  color: var(--text-muted);
  word-break: break-all;
  flex: 1;
  min-width: 200px;
}

.crumb a {
  color: var(--text-secondary);
}

.crumb a:hover {
  color: var(--accent);
}

.btns {
  display: flex;
  gap: 8px;
}

.panel {
  padding: 18px 22px;
  min-height: 200px;
}

.up-row {
  margin-bottom: 8px;
}

.sec {
  margin-bottom: 14px;
}

.sec-title {
  font-size: 12.5px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin: 0 0 8px;
}

.entries {
  display: flex;
  flex-direction: column;
}

.entry {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 10px;
  border-radius: 8px;
  transition: background 0.15s;
}

.entry:hover {
  background: var(--bg-soft);
}

.icon {
  font-size: 15px;
  flex-shrink: 0;
}

.name {
  font-size: 13.5px;
  color: var(--text);
  word-break: break-all;
}

.meta {
  font-size: 11.5px;
  color: var(--text-muted);
  white-space: nowrap;
}

.ops {
  margin-left: auto;
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}

.rename-ipt {
  flex: 1;
  background: var(--bg-soft);
  border: 1px solid var(--accent);
  border-radius: 8px;
  padding: 6px 10px;
  color: var(--text);
  font-size: 13.5px;
  outline: none;
}

.empty {
  text-align: center;
  color: var(--text-muted);
  padding: 40px 0;
  font-size: 13.5px;
}
</style>