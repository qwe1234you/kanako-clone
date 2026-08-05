<script setup>
import { ref, onMounted } from 'vue'
import { blogApi, siteApi } from '../api/modules'
import dayjs from 'dayjs'

const recentBlogs = ref([])
const stats = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const [pageRes, statsRes] = await Promise.all([
      blogApi.page({ page: 1, size: 5 }),
      siteApi.stats().catch(() => null)
    ])
    recentBlogs.value = pageRes?.records || []
    stats.value = statsRes
  } catch {
    /* 接口未就绪时静默 */
  } finally {
    loading.value = false
  }
})

function fmt(d) {
  return dayjs(d).format('YYYY-MM-DD')
}
</script>

<template>
  <div class="home">
    <section class="hero">
      <h1 class="hero-title">宁静致远，厚积薄发</h1>
      <p class="hero-sub">记录代码、思考与生活 · Kanako Clone</p>
      <div class="hero-stats" v-if="stats">
        <div class="stat">
          <span class="num">{{ stats.blogCount ?? 0 }}</span>
          <span class="label">文章</span>
        </div>
        <div class="stat">
          <span class="num">{{ stats.commentCount ?? 0 }}</span>
          <span class="label">评论</span>
        </div>
        <div class="stat">
          <span class="num">{{ stats.viewCount ?? 0 }}</span>
          <span class="label">浏览</span>
        </div>
      </div>
    </section>

    <section class="recent glass-blur">
      <div class="recent-header">
        <span class="eyebrow">Blog</span>
        <h2>最近文章</h2>
      </div>
      <div v-if="loading" class="loading">加载中…</div>
      <div v-else-if="recentBlogs.length === 0" class="empty">还没有发布博客。</div>
      <RouterLink
        v-for="(blog, i) in recentBlogs"
        :key="blog.id"
        :to="{ name: 'blog-detail', params: { slug: blog.slug || blog.id } }"
        class="blog-item fade-up"
        :style="{ animationDelay: `${i * 60}ms` }"
      >
        <div class="blog-meta">
          <span class="date">{{ fmt(blog.createdAt) }}</span>
          <span v-if="blog.category" class="cat">{{ blog.category.name }}</span>
        </div>
        <h3 class="blog-title">{{ blog.title }}</h3>
        <p class="blog-summary">{{ blog.summary || '' }}</p>
      </RouterLink>
      <RouterLink to="/blogs" class="more">查看全部 →</RouterLink>
    </section>
  </div>
</template>

<style scoped>
.home {
  max-width: 820px;
  margin: 0 auto;
  padding: 60px 20px 80px;
}

.hero {
  text-align: center;
  padding: 60px 0 48px;
}

.hero-title {
  font-size: 40px;
  margin: 0 0 14px;
  background: linear-gradient(120deg, var(--accent), #a8e6c3);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-sub {
  color: var(--text-secondary);
  font-size: 16px;
  margin: 0 0 32px;
}

.hero-stats {
  display: inline-flex;
  gap: 40px;
  padding: 18px 44px;
  border-radius: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border);
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.num {
  font-size: 26px;
  font-weight: 700;
  color: var(--accent);
}

.label {
  font-size: 12px;
  color: var(--text-muted);
}

.recent {
  padding: 28px 32px;
}

.recent-header h2 {
  margin: 4px 0 20px;
  font-size: 22px;
}

.loading,
.empty {
  color: var(--text-muted);
  padding: 24px 0;
  text-align: center;
}

.blog-item {
  display: block;
  padding: 16px 0;
  border-bottom: 1px dashed var(--border);
  color: var(--text);
}

.blog-item:hover .blog-title {
  color: var(--accent);
}

.blog-meta {
  display: flex;
  gap: 12px;
  font-size: 12.5px;
  color: var(--text-muted);
  margin-bottom: 6px;
}

.cat {
  color: var(--accent);
}

.blog-title {
  margin: 0 0 6px;
  font-size: 18px;
  transition: color 0.2s;
}

.blog-summary {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.more {
  display: inline-block;
  margin-top: 18px;
  font-size: 14px;
}
</style>