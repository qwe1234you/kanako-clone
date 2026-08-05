<script setup>
defineProps({
  title: { type: String, default: '开发中' },
  desc: { type: String, default: '该功能正在开发中，敬请期待。' }
})
</script>

<template>
  <div class="page-container">
    <div class="placeholder glass-blur">
      <el-icon :size="40"><Tools /></el-icon>
      <h1>{{ title }}</h1>
      <p>{{ desc }}</p>
    </div>
  </div>
</template>

<style scoped>
.placeholder {
  margin-top: 80px;
  padding: 60px 24px;
  text-align: center;
  color: var(--text-secondary);
}

.placeholder h1 {
  margin: 16px 0 8px;
  color: var(--text);
  font-size: 22px;
}

.placeholder p {
  margin: 0;
  font-size: 14px;
}
</style>