<template>
  <div class="page-card">
    <div class="toolbar">
      <el-date-picker v-model="begin" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
      <span>~</span>
      <el-date-picker v-model="end" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button v-perm="'report:rebuild'" type="warning" @click="rebuild">重算日报</el-button>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="checkinDate" label="日期" width="120" />
      <el-table-column prop="userName" label="员工" width="110" />
      <el-table-column label="首卡" width="100">
        <template #default="{ row }">{{ fmtHM(row.firstInTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="末卡" width="100">
        <template #default="{ row }">{{ fmtHM(row.lastOutTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="工时(分)" width="100">
        <template #default="{ row }">{{ row.workMinutes ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" min-width="140">
        <template #default="{ row }">
          <el-tag v-if="row.statusEnum === 0" type="success">正常</el-tag>
          <el-tag v-else type="danger">{{ row.statusText }}</el-tag>
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
import { pageDaily, rebuildReport } from '../../api'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const today = new Date().toLocaleDateString('sv-SE')
const begin = ref(today)
const end = ref(today)

function fmtHM(t) {
  return t ? String(t).replace('T', ' ').slice(11, 16) : ''
}

async function load() {
  const data = await pageDaily({ page: page.value, size: 20, begin: begin.value, end: end.value })
  rows.value = data.records
  total.value = data.total
}

async function rebuild() {
  const b = begin.value || today
  const e = end.value || today
  await ElMessageBox.confirm(`将删除并重算 ${b} ~ ${e} 的全部日报，确认？`, '重算确认', { type: 'warning' }).catch(() => Promise.reject('cancel'))
  const data = await rebuildReport({ begin: b, end: e })
  ElMessage.success(`已重算 ${data.rows} 条`)
  load()
}

onMounted(load)
</script>
