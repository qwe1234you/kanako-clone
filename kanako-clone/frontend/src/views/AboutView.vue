<script setup>
import { onMounted, ref } from 'vue'
import { siteApi, blogApi } from '../api/modules'

const config = ref(null)
const stats = ref(null)
const tagCount = ref(0)
const year = new Date().getFullYear()

onMounted(async () => {
  try {
    config.value = await siteApi.config()
  } catch {
    config.value = {}
  }
  try {
    stats.value = await siteApi.stats()
  } catch {
    stats.value = null
  }
  try {
    const tags = await blogApi.tags()
    tagCount.value = tags?.length || 0
  } catch {
    tagCount.value = 0
  }
})
</script>

<template>
  <div class="page-container about">
    <header class="page-header fade-up">
      <p class="eyebrow">About</p>
      <h1>关于本站</h1>
    </header>

    <section class="intro glass-blur fade-up">
      <h2>{{ config?.site_name || 'Kanako · Space' }}</h2>
      <p>{{ config?.site_description || '宁静致远，厚积薄发。' }}</p>
      <p class="muted">一个自建的博客与树洞空间，记录代码、思考与生活。</p>
    </section>

    <section class="stats fade-up">
      <div class="stat glass-blur">
        <span class="num">{{ stats?.blogCount ?? 0 }}</span>
        <span class="label">文章</span>
      </div>
      <div class="stat glass-blur">
        <span class="num">{{ stats?.commentCount ?? 0 }}</span>
        <span class="label">评论</span>
      </div>
      <div class="stat glass-blur">
        <span class="num">{{ stats?.viewCount ?? 0 }}</span>
        <span class="label">总浏览</span>
      </div>
      <div class="stat glass-blur">
        <span class="num">{{ tagCount }}</span>
        <span class="label">标签</span>
      </div>
    </section>

    <section class="tech fade-up">
      <h2>技术栈</h2>
      <ul class="tech-list">
        <li><b>前端</b> Vue 3 · Vite · Element Plus · Pinia · Markdown-it</li>
        <li><b>后端</b> Spring Boot 3 · MyBatis-Plus · JWT · H2/MySQL</li>
        <li><b>部署</b> Nginx · 对象存储（本地/OSS）· 反向代理</li>
      </ul>
      <p class="muted footer">© {{ year }} {{ config?.site_name || 'Kanako · Space' }} · {{ config?.site_footer || '' }}</p>
    </section>
  </div>
</template>

<style scoped>
.about {
  max-width: 820px;
}

.intro {
  padding: 28px 32px;
  margin-bottom: 20px;
}

.intro h2 {
  margin: 0 0 12px;
  font-size: 22px;
}

.intro p {
  margin: 0 0 8px;
  font-size: 15px;
  line-height: 1.8;
  color: var(--text);
}

.intro .muted {
  color: var(--text-muted);
  font-size: 13.5px;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 14px;
  margin-bottom: 28px;
}

.stat {
  text-align: center;
  padding: 20px 10px;
}

.num {
  display: block;
  font-size: 26px;
  font-weight: 700;
  color: var(--accent);
}

.label {
  font-size: 12.5px;
  color: var(--text-muted);
}

.tech {
  padding: 8px 6px;
}

.tech h2 {
  font-size: 18px;
  margin: 0 0 12px;
}

.tech-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tech-list li {
  font-size: 14px;
  color: var(--text-secondary);
}

.tech-list b {
  color: var(--text);
  margin-right: 8px;
}

.footer {
  margin-top: 28px;
  color: var(--text-muted);
  font-size: 12.5px;
  text-align: center;
}
</style>
