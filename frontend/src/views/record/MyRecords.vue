<template>
  <div class="page-card">
    <div class="toolbar">
      <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" @change="load" />
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column label="打卡时间" width="170">
        <template #default="{ row }">{{ fmtTime(row.checkinTime) }}</template>
      </el-table-column>
      <el-table-column prop="fenceName" label="围栏" min-width="150" />
      <el-table-column label="距圆心(米)" width="110">
        <template #default="{ row }">{{ row.distanceM?.toFixed(0) }}</template>
      </el-table-column>
      <el-table-column label="来源" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.source === 'MANUAL' ? 'warning' : 'info'">{{ row.source }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="140" />
    </el-table>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { pageRecords } from '../../api'

const rows = ref([])
const date = ref(new Date().toLocaleDateString('sv-SE'))

function fmtTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}

async function load() {
  const data = await pageRecords({ page: 1, size: 100, date: date.value })
  rows.value = data.records
}

onMounted(load)
</script>
