<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="nf-wrap">
    <div class="nf-card fade-up">
      <p class="code">404</p>
      <h1>页面不存在</h1>
      <p class="desc">你访问的页面可能已被移动或删除。</p>
      <div class="ops">
        <el-button type="primary" @click="router.push('/')">返回首页</el-button>
        <el-button @click="router.back()">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.nf-wrap {
  min-height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  text-align: center;
}

.code {
  font-size: 72px;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(120deg, var(--accent), #a8e6c3);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  font-family: var(--font-mono);
  line-height: 1;
}

h1 {
  margin: 16px 0 8px;
  font-size: 22px;
}

.desc {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0 0 26px;
}

.ops {
  display: flex;
  gap: 10px;
  justify-content: center;
}
</style>