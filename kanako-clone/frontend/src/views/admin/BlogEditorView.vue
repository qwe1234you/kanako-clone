<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { blogApi } from '../../api/modules'
import BlogRenderer from '../../components/BlogRenderer.vue'
import { renderMarkdown } from '../../utils/markdown'

const route = useRoute()
const router = useRouter()
const editingId = ref(null)

const form = ref({
  title: '',
  slug: '',
  summary: '',
  cover: '',
  contentMd: '',
  categoryId: null,
  tagIds: [],
  status: 'PUBLISHED',
  isTop: 0
})

const categories = ref([])
const tags = ref([])
const loading = ref(false)
const saving = ref(false)

// 预览
const previewHtml = ref('')
const previewToc = ref([])
const tab = ref('write')

// 分类/标签管理弹窗
const catDialog = ref(false)
const catForm = ref({ id: null, name: '', slug: '', description: '', sort: 0 })
const tagDialog = ref(false)
const tagForm = ref({ id: null, name: '', slug: '' })

function autoSlug(text) {
  const s = text
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5-]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')
  return s || ''
}

watch(
  () => form.value.title,
  (t) => {
    if (!form.value.slug || form.value.slug === 'auto-pending') {
      form.value.slug = autoSlug(t)
    }
  }
)

function renderPreview() {
  const { html, toc } = renderMarkdown(form.value.contentMd)
  previewHtml.value = html
  previewToc.value = toc
}

function switchTab(name) {
  tab.value = name
  if (name === 'preview') renderPreview()
}

async function loadMeta() {
  const [cs, ts] = await Promise.all([
    blogApi.categories().catch(() => []),
    blogApi.tags().catch(() => [])
  ])
  categories.value = cs || []
  tags.value = ts || []
}

async function loadForEdit(id) {
  loading.value = true
  try {
    const data = await blogApi.adminDetail(id)
    form.value = {
      title: data.title || '',
      slug: data.slug || '',
      summary: data.summary || '',
      cover: data.cover || '',
      contentMd: data.contentMd || '',
      categoryId: data.categoryId ?? null,
      tagIds: (data.tags || []).map((t) => t.id),
      status: data.status || 'PUBLISHED',
      isTop: data.isTop ?? 0
    }
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.value.title.trim()) return ElMessage.warning('请输入标题')
  if (!form.value.contentMd.trim()) return ElMessage.warning('请输入正文')
  if (!form.value.slug.trim()) return ElMessage.warning('请填写 slug（文章链接标识）')
  saving.value = true
  try {
    const payload = { ...form.value, status: form.value.status || 'DRAFT' }
    if (editingId.value) {
      payload.id = editingId.value
      await blogApi.update(editingId.value, payload)
      ElMessage.success('已更新')
    } else {
      await blogApi.create(payload)
      ElMessage.success('已发布')
    }
    router.push('/blogs')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// ---- 分类管理 ----
async function openCatDialog() {
  catDialog.value = true
  catForm.value = { id: null, name: '', slug: '', description: '', sort: categories.value.length }
}

async function submitCategory() {
  if (!catForm.value.name || !catForm.value.slug) return ElMessage.warning('名称和 slug 必填')
  await blogApi.saveCategory(catForm.value)
  ElMessage.success('已保存')
  catDialog.value = false
  await loadMeta()
}

async function removeCategory(c) {
  try {
    await ElMessageBox.confirm(`删除分类「${c.name}」？其下文章将变为未分类。`, '确认', { type: 'warning' })
  } catch {
    return
  }
  await blogApi.deleteCategory(c.id)
  ElMessage.success('已删除')
  await loadMeta()
}

// ---- 标签管理 ----
async function openTagDialog() {
  tagDialog.value = true
  tagForm.value = { id: null, name: '', slug: '' }
}

async function submitTag() {
  if (!tagForm.value.name || !tagForm.value.slug) return ElMessage.warning('名称和 slug 必填')
  await blogApi.saveTag(tagForm.value)
  ElMessage.success('已保存')
  tagDialog.value = false
  await loadMeta()
}

async function removeTag(t) {
  try {
    await ElMessageBox.confirm(`删除标签「${t.name}」？`, '确认', { type: 'warning' })
  } catch {
    return
  }
  await blogApi.deleteTag(t.id)
  ElMessage.success('已删除')
  await loadMeta()
}

onMounted(async () => {
  await loadMeta()
  if (route.params.id) {
    editingId.value = Number(route.params.id)
    await loadForEdit(editingId.value)
  }
})
</script>

<template>
  <div class="page-container editor">
    <header class="page-header">
      <p class="eyebrow">Admin</p>
      <h1>{{ editingId ? '编辑文章' : '写文章' }}</h1>
    </header>

    <div v-loading="loading" class="editor-body">
      <el-form label-position="top">
        <div class="grid">
          <el-form-item label="标题">
            <el-input v-model="form.title" placeholder="文章标题" />
          </el-form-item>
          <el-form-item label="Slug（链接标识）">
            <el-input v-model="form.slug" placeholder="如 hello-world" />
          </el-form-item>
        </div>

        <div class="grid">
          <el-form-item label="分类">
            <el-select
              v-model="form.categoryId"
              placeholder="选择分类"
              clearable
              style="width: 100%"
            >
              <el-option
                v-for="c in categories"
                :key="c.id"
                :label="c.name"
                :value="c.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select
              v-model="form.tagIds"
              multiple
              collapse-tags
              placeholder="选择标签"
              style="width: 100%"
            >
              <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="列表页显示的摘要（可留空自动截取）"
          />
        </el-form-item>

        <el-form-item label="封面图 URL">
          <el-input v-model="form.cover" placeholder="https://... 图片地址，可留空" />
        </el-form-item>

        <div class="editor-area">
          <div class="toolbar">
            <div class="tabs">
              <button :class="{ active: tab === 'write' }" @click="switchTab('write')">编辑</button>
              <button :class="{ active: tab === 'preview' }" @click="switchTab('preview')">预览</button>
            </div>
          </div>
          <textarea
            v-if="tab === 'write'"
            v-model="form.contentMd"
            class="md-area"
            placeholder="支持 Markdown 语法，如 # 标题、```js 代码块、**加粗** 等"
          ></textarea>
          <div v-else class="preview-pane">
            <BlogRenderer :html="previewHtml" />
            <div v-if="previewToc.length" class="preview-toc">
              <p>目录</p>
              <ul>
                <li v-for="(item, i) in previewToc" :key="i" :class="{ sub: item.level === 3 }">
                  {{ item.text }}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div class="ops">
          <div class="switches">
            <el-switch v-model="form.isTop" :active-value="1" :inactive-value="0" active-text="置顶" />
            <el-radio-group v-model="form.status">
              <el-radio-button value="PUBLISHED">发布</el-radio-button>
              <el-radio-button value="DRAFT">草稿</el-radio-button>
            </el-radio-group>
          </div>
          <div class="btns">
            <el-button @click="catDialog = true">管理分类</el-button>
            <el-button @click="tagDialog = true">管理标签</el-button>
            <el-button type="primary" :loading="saving" @click="save">{{ editingId ? '保存' : '发布' }}</el-button>
          </div>
        </div>
      </el-form>
    </div>

    <!-- 分类管理弹窗 -->
    <el-dialog v-model="catDialog" title="管理分类" width="480px">
      <div class="manage-row">
        <el-input v-model="catForm.name" placeholder="分类名称" />
        <el-input v-model="catForm.slug" placeholder="slug" />
      </div>
      <el-button type="primary" size="small" @click="submitCategory">新增分类</el-button>
      <div class="manage-list">
        <div v-for="c in categories" :key="c.id" class="manage-item">
          <span>{{ c.name }}({{ c.count }})</span>
          <el-button link type="danger" @click="removeCategory(c)">删除</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 标签管理弹窗 -->
    <el-dialog v-model="tagDialog" title="管理标签" width="480px">
      <div class="manage-row">
        <el-input v-model="tagForm.name" placeholder="标签名称" />
        <el-input v-model="tagForm.slug" placeholder="slug" />
      </div>
      <el-button type="primary" size="small" @click="submitTag">新增标签</el-button>
      <div class="manage-list">
        <div v-for="t in tags" :key="t.id" class="manage-item">
          <span>{{ t.name }}({{ t.count }})</span>
          <el-button link type="danger" @click="removeTag(t)">删除</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.editor {
  max-width: 920px;
}

.editor-body {
  min-height: 300px;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.editor-area {
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 18px;
  background: var(--bg-soft);
}

.toolbar {
  border-bottom: 1px solid var(--border);
  padding: 8px 14px;
}

.tabs {
  display: flex;
  gap: 4px;
}

.tabs button {
  border: none;
  background: transparent;
  color: var(--text-muted);
  padding: 6px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13.5px;
}

.tabs button.active {
  background: var(--accent-soft);
  color: var(--accent);
}

.md-area {
  width: 100%;
  min-height: 420px;
  border: none;
  outline: none;
  background: transparent;
  color: var(--text);
  font-family: var(--font-mono);
  font-size: 14px;
  line-height: 1.7;
  padding: 16px;
  resize: vertical;
}

.preview-pane {
  padding: 18px 22px;
  max-height: 560px;
  overflow-y: auto;
  font-size: 15px;
  line-height: 1.8;
}

.preview-toc {
  margin-top: 24px;
  border-top: 1px dashed var(--border);
  padding-top: 12px;
  font-size: 13px;
  color: var(--text-muted);
}

.preview-toc ul {
  margin: 6px 0 0;
  padding-left: 18px;
}

.preview-toc .sub {
  list-style: circle;
}

.ops {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
}

.switches {
  display: flex;
  align-items: center;
  gap: 20px;
}

.manage-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.manage-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.manage-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 10px;
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 13.5px;
}
</style>