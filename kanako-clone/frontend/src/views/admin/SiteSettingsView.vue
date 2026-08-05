<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { siteApi } from '../../api/modules'
import { useSiteStore } from '../../stores/site'

const site = useSiteStore()
const form = ref({})
const loading = ref(true)
const saving = ref(false)

async function load() {
  try {
    form.value = { ...(await siteApi.config()) }
  } catch {
    form.value = {}
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    await siteApi.updateConfig(form.value)
    site.loadConfig()
    ElMessage.success('已保存')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <header class="page-header">
      <p class="eyebrow">Admin</p>
      <h1>站点设置</h1>
    </header>

    <div v-loading="loading" class="panel glass-blur">
      <el-form label-position="top" style="max-width: 560px">
        <el-form-item label="站点名称">
          <el-input v-model="form.site_name" placeholder="站点名称" />
        </el-form-item>
        <el-form-item label="站点描述">
          <el-input v-model="form.site_description" type="textarea" :rows="2" placeholder="一句话描述站点" />
        </el-form-item>
        <el-form-item label="页脚">
          <el-input v-model="form.site_footer" placeholder="页脚文案，如 © Kanako Clone" />
        </el-form-item>
        <el-form-item label="备案号">
          <el-input v-model="form.site_icp" placeholder="ICP 备案号（可留空）" />
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </el-form>

      <div class="tips">
        <p class="tip-title">说明</p>
        <ul>
          <li>配置保存在数据库 site_config 表，立刻生效。</li>
          <li>首页 Hero、页脚与“关于”页会读取这些字段。</li>
          <li>备案号将显示在页脚。</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>
.panel {
  padding: 28px 30px;
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
}

.tips {
  flex: 1;
  min-width: 220px;
}

.tip-title {
  font-size: 13px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-muted);
  margin: 0 0 10px;
}

.tips ul {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 2;
}
</style>