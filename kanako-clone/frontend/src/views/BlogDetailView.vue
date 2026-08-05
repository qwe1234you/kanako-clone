<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { blogApi } from '../api/modules'
import BlogRenderer from '../components/BlogRenderer.vue'
import { countWords, renderMarkdown } from '../utils/markdown'
import dayjs from 'dayjs'

const route = useRoute()
const slug = computed(() => route.params.slug)

const blog = ref(null)
const articleHtml = ref('')
const toc = ref([])
const comments = ref([])
const loading = ref(true)
const loadErr = ref(false)
const notFound = ref(false)

const contentRef = ref(null)
const activeSec = ref('')

const readingMin = computed(() =>
  blog.value ? Math.max(1, Math.round(countWords(blog.value.contentMd) / 400)) : 0
)

const liked = ref(false)

async function loadDetail() {
  loading.value = true
  loadErr.value = false
  notFound.value = false
  try {
    const data = await blogApi.detailBySlug(slug.value)
    if (!data) {
      notFound.value = true
      return
    }
    blog.value = data
    const rendered = renderMarkdown(data.contentMd)
    articleHtml.value = rendered.html
    toc.value = rendered.toc
    liked.value = typeof window !== 'undefined' && !!localStorage.getItem(`blog-liked-${data.id}`)
    await nextTick()
    observeHeadings()
  } catch (e) {
    if (e?.response?.status === 404) notFound.value = true
    else loadErr.value = true
  } finally {
    loading.value = false
  }
}

let headingObserver = null
function observeHeadings() {
  headingObserver?.disconnect()
  const nodes = contentRef.value?.querySelectorAll('h2, h3')
  if (!nodes?.length) return
  headingObserver = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) activeSec.value = entry.target.id
      })
    },
    { rootMargin: '-90px 0px -70% 0px' }
  )
  nodes.forEach((n) => headingObserver.observe(n))
  activeSec.value = nodes[0].id
}

function jumpTo(id) {
  const el = document.getElementById(id)
  el?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function fmt(d) {
  return dayjs(d).format('YYYY-MM-DD')
}

async function doLike() {
  if (!blog.value || liked.value) return
  try {
    await blogApi.like(blog.value.id)
    blog.value.likeCount++
    liked.value = true
    localStorage.setItem(`blog-liked-${blog.value.id}`, '1')
  } catch {
    /* 静默 */
  }
}

const form = ref({ nickname: '', email: '', content: '' })
const replyTo = ref(null)
const submitting = ref(false)

async function loadComments() {
  if (!blog.value) return
  try {
    comments.value = (await blogApi.comments(blog.value.id)) || []
  } catch {
    comments.value = []
  }
}

function startReply(c) {
  replyTo.value = replyTo.value === c.id ? null : c.id
}

async function submitComment() {
  if (!form.value.content.trim()) return
  submitting.value = true
  try {
    await blogApi.addComment(blog.value.id, {
      nickname: form.value.nickname,
      email: form.value.email || undefined,
      content: form.value.content,
      parentId: replyTo.value || undefined
    })
    form.value.content = ''
    replyTo.value = null
    await loadComments()
  } catch (e) {
    alert(e?.message || '发表失败')
  } finally {
    submitting.value = false
  }
}

function avatarOf(name, email) {
  const emailHash = String(email || `${name}@local`)
  const seed = emailHash
  const hue = [...seed].reduce((a, c) => a + c.charCodeAt(0), 0) % 360
  return {
    bg: `hsl(${hue}, 38%, 26%)`,
    text: name ? name.charAt(0).toUpperCase() : '匿'
  }
}

watch(slug, async () => {
  loadDetail()
})

onMounted(() => {
  loadDetail()
  document.title = '博客详情'
})

onBeforeUnmount(() => headingObserver?.disconnect())
</script>

<template>
  <div class="page-container detail-page">
    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="notFound" class="state">
      <p>文章不存在或已被删除。</p>
      <RouterLink to="/blogs">← 返回博客列表</RouterLink>
    </div>
    <div v-else-if="loadErr" class="state">
      <p>加载失败，请稍后重试。</p>
      <RouterLink to="/blogs">← 返回博客列表</RouterLink>
    </div>
    <template v-else-if="blog">
      <div class="detail-main">
        <article class="article">
          <RouterLink to="/blogs" class="back">← 返回博客列表</RouterLink>
          <header class="article-head fade-up">
            <h1 class="title">{{ blog.title }}</h1>
            <div class="meta">
              <span v-if="blog.category" class="cat">{{ blog.category.name }}</span>
              <span>{{ fmt(blog.createdAt) }}</span>
              <span>{{ readingMin }} 分钟</span>
              <span>{{ blog.viewCount ?? 0 }} 阅读</span>
            </div>
            <div v-if="blog.tags?.length" class="tags">
              <span v-for="t in blog.tags" :key="t.id" class="tag"># {{ t.name }}</span>
            </div>
          </header>

          <div ref="contentRef" class="content fade-up">
            <BlogRenderer :html="articleHtml" />
          </div>

          <div class="actions">
            <button class="like-btn" :class="{ liked }" @click="doLike">
              <span v-if="!liked">♥</span>
              <span v-else>♥</span>
              <span class="like-num">{{ blog.likeCount ?? 0 }} 点赞</span>
            </button>
          </div>
        </article>

        <aside v-if="toc.length" class="toc">
          <p class="toc-title">目录</p>
          <ul>
            <li
              v-for="(item, i) in toc"
              :key="i"
              :class="{ sub: item.level === 3, active: activeSec === item.id }"
            >
              <a href="#" @click.prevent="jumpTo(item.id)">{{ item.text }}</a>
            </li>
          </ul>
        </aside>
      </div>

      <section class="comments glass-blur">
        <h2 class="sec-title">评论 · {{ comments.length }}</h2>

        <div class="comment-form">
          <div class="row">
            <input v-model="form.nickname" placeholder="昵称（必填？匿名亦可）" />
            <input v-model="form.email" placeholder="邮箱（用于头像，可不填）" />
          </div>
          <textarea
            v-model="form.content"
            rows="3"
            placeholder="友善交流，理性表达。"
          ></textarea>
          <div class="form-foot">
            <span v-if="replyTo" class="reply-hint">
              正在回复 {{ replyTo }} 的评论
              <a href="#" @click.prevent="replyTo = null">取消</a>
            </span>
            <el-button type="primary" :loading="submitting" @click="submitComment">发表评论</el-button>
          </div>
        </div>

        <div v-if="comments.length === 0" class="no-comment">还没有评论，来抢沙发！</div>
        <ul v-else class="comment-list">
          <li v-for="c in comments" :key="c.id" class="comment-item">
            <div
              class="avatar"
              :style="{ background: avatarOf(c.nickname, c.email).bg }"
            >{{ avatarOf(c.nickname, c.email).text }}</div>
            <div class="body">
              <div class="head">
                <span class="name">
                  {{ c.nickname }}
                  <span v-if="c.isAdmin" class="admin-badge">博主</span>
                </span>
                <span class="time">{{ fmt(c.createdAt) }}</span>
              </div>
              <p class="text">{{ c.content }}</p>
              <a class="reply" href="#" @click.prevent="startReply(c)">回复</a>
            </div>
          </li>
        </ul>
      </section>
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 1080px;
}

.detail-main {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.article {
  flex: 1;
  min-width: 0;
  max-width: 760px;
}

.back {
  font-size: 13.5px;
  color: var(--text-muted);
}

.back:hover {
  color: var(--accent);
}

.article-head {
  padding: 20px 0 24px;
  border-bottom: 1px dashed var(--border);
  margin-bottom: 28px;
}

.title {
  font-size: 32px;
  line-height: 1.35;
  margin: 8px 0 14px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  font-size: 13px;
  color: var(--text-muted);
}

.cat {
  color: var(--accent);
}

.tags {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  font-size: 12.5px;
  color: var(--text-muted);
  border: 1px solid var(--border);
  padding: 2px 10px;
  border-radius: 999px;
}

.content {
  font-size: 15.5px;
  line-height: 1.85;
  color: var(--text);
}

.content :deep(.blog-prose :is(h2, h3)) {
  scroll-margin-top: 90px;
}

.actions {
  margin: 36px 0;
  display: flex;
  justify-content: center;
}

.like-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 22px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: var(--bg-card);
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.like-btn:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.like-btn.liked {
  color: var(--accent);
  border-color: var(--accent);
  background: var(--accent-soft);
}

.toc {
  position: sticky;
  top: 90px;
  width: 240px;
  flex-shrink: 0;
  max-height: calc(100vh - 140px);
  overflow-y: auto;
  display: none;
}

.toc-title {
  font-size: 13px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin: 0 0 10px;
}

.toc ul {
  list-style: none;
  margin: 0;
  padding: 0;
  border-left: 1px solid var(--border);
}

.toc li {
  margin: 0;
}

.toc li a {
  display: block;
  padding: 5px 0 5px 14px;
  margin-left: -1px;
  border-left: 1px solid transparent;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
  transition: all 0.15s;
}

.toc li.sub a {
  padding-left: 26px;
}

.toc li a:hover {
  color: var(--accent);
}

.toc li.active a {
  color: var(--accent);
  border-left-color: var(--accent);
}

.comments {
  margin-top: 40px;
  padding: 26px 30px;
}

.sec-title {
  margin: 0 0 18px;
  font-size: 18px;
}

.comment-form .row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-form input,
.comment-form textarea {
  flex: 1;
  background: var(--bg-soft);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px 14px;
  color: var(--text);
  font-family: inherit;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
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

.form-foot {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
  margin-top: 12px;
}

.reply-hint {
  font-size: 13px;
  color: var(--text-muted);
}

.no-comment {
  color: var(--text-muted);
  text-align: center;
  padding: 24px 0;
  font-size: 13.5px;
}

.comment-list {
  list-style: none;
  padding: 0;
  margin: 16px 0 0;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 16px 0;
  border-top: 1px solid var(--border);
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text);
  font-size: 15px;
  font-weight: 600;
}

.body {
  flex: 1;
  min-width: 0;
}

.head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.name {
  font-size: 14px;
  font-weight: 600;
}

.admin-badge {
  font-size: 11px;
  background: var(--accent-soft);
  color: var(--accent);
  padding: 1px 7px;
  border-radius: 4px;
  margin-left: 4px;
}

.time {
  font-size: 12px;
  color: var(--text-muted);
}

.text {
  margin: 0 0 4px;
  font-size: 14.5px;
  line-height: 1.7;
  word-break: break-word;
}

.reply {
  font-size: 12.5px;
  color: var(--text-muted);
}

.reply:hover {
  color: var(--accent);
}

.state {
  text-align: center;
  color: var(--text-muted);
  padding: 100px 0;
}

@media (min-width: 1100px) {
  .toc {
    display: block;
  }
}
</style>