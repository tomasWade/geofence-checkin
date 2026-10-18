<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="规则名称" style="width: 220px" clearable @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button v-perm="'rule:add'" type="success" @click="$router.push('/rule/edit')">新增规则</el-button>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column label="时段" width="150">
        <template #default="{ row }">{{ sec2hm(row.workStartSec) }} ~ {{ sec2hm(row.workEndSec) }}</template>
      </el-table-column>
      <el-table-column label="生效日" width="130">
        <template #default="{ row }">{{ daysText(row.effectiveDays) }}</template>
      </el-table-column>
      <el-table-column label="绑定围栏" width="120">
        <template #default="{ row }">{{ fenceName(row.fenceId) }}</template>
      </el-table-column>
      <el-table-column label="绑定方式" width="100">
        <template #default="{ row }">{{ row.bindType }}</template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-perm="'rule:edit'" link type="primary" @click="$router.push(`/rule/edit/${row.id}`)">编辑</el-button>
          <el-button v-perm="'rule:delete'" link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="total" v-model:current-page="page"
                     :page-size="20" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRules, deleteRule, listFences } from '../../api'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const fences = ref([])

function sec2hm(sec) {
  const h = Math.floor(sec / 3600)
  const m = Math.floor((sec % 3600) / 60)
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`
}

function daysText(bits) {
  const names = ['一', '二', '三', '四', '五', '六', '日']
  const hit = names.filter((_, i) => (bits & (1 << i)) !== 0)
  if (hit.length === 7) return '每天'
  return '周' + hit.join('/')
}

function fenceName(id) {
  return fences.value.find((f) => f.id === id)?.name || id
}

async function load() {
  const data = await pageRules({ page: page.value, size: 20, keyword: keyword.value })
  rows.value = data.records
  total.value = data.total
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除规则「${row.name}」？`, '提示', { type: 'warning' }).catch(() => Promise.reject('cancel'))
  await deleteRule(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  fences.value = await listFences().catch(() => [])
  load()
})
</script>
