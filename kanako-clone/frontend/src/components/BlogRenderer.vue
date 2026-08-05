<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { renderMarkdown } from '../utils/markdown'

const props = defineProps({
  content: { type: String, default: '' },
  html: { type: String, default: '' }
})

const root = ref(null)
const rendered = computed(() =>
  props.html || renderMarkdown(props.content).html
)

function wireCopy() {
  if (!root.value) return
  root.value.querySelectorAll('pre.hljs').forEach((pre) => {
    if (pre.querySelector('.copy-btn')) return
    const btn = document.createElement('button')
    btn.className = 'copy-btn'
    btn.type = 'button'
    btn.textContent = '复制'
    btn.addEventListener('click', async () => {
      const code = pre.querySelector('code')?.innerText || ''
      try {
        await navigator.clipboard.writeText(code)
        btn.textContent = '已复制'
      } catch {
        btn.textContent = '失败'
      }
      setTimeout(() => (btn.textContent = '复制'), 1500)
    })
    pre.appendChild(btn)
  })
}

watch(rendered, () => nextTick(wireCopy))
onMounted(wireCopy)
</script>

<template>
  <div ref="root" class="blog-prose" v-html="rendered" />
</template>

<style scoped>
.blog-prose {
  position: relative;
}

.blog-prose :deep(pre.hljs) {
  position: relative;
}

.copy-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 3px 10px;
  font-size: 12px;
  border-radius: 6px;
  border: 1px solid var(--border);
  background: var(--bg-soft);
  color: var(--text-muted);
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s;
}

.copy-btn:hover {
  color: var(--accent);
}

.blog-prose :deep(pre.hljs:hover .copy-btn) {
  opacity: 1;
}

.blog-prose :deep(pre.hljs::after) {
  content: attr(data-lang);
  position: absolute;
  top: 8px;
  right: 10px;
  font-size: 11px;
  color: var(--text-muted);
  pointer-events: none;
  transition: opacity 0.2s;
}

.blog-prose :deep(pre.hljs:hover::after) {
  opacity: 0;
}
</style>
