<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="load">刷新</el-button>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="时间" width="170">
        <template #default="{ row }">{{ fmtTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column prop="action" label="动作" width="150" />
      <el-table-column prop="resource" label="位置" min-width="200" />
      <el-table-column prop="ip" label="IP" width="130" />
    </el-table>
    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="total" v-model:current-page="page"
                     :page-size="20" @current-change="load" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { pageAudit } from '../../api'

const rows = ref([])
const total = ref(0)
const page = ref(1)

function fmtTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}

async function load() {
  const data = await pageAudit({ page: page.value, size: 20 })
  rows.value = data.records
  total.value = data.total
}

onMounted(load)
</script>
