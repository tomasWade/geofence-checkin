<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="围栏名称" style="width: 220px" clearable @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button v-perm="'fence:add'" type="success" @click="$router.push('/fence/edit')">新增围栏</el-button>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column label="圆心" width="200">
        <template #default="{ row }">{{ Number(row.lng).toFixed(6) }}, {{ Number(row.lat).toFixed(6) }}</template>
      </el-table-column>
      <el-table-column prop="radiusM" label="半径(米)" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-perm="'fence:edit'" link type="primary" @click="$router.push(`/fence/edit/${row.id}`)">编辑</el-button>
          <el-button v-perm="'fence:delete'" link type="danger" @click="del(row)">删除</el-button>
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
import { pageFences, deleteFence } from '../../api'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')

async function load() {
  const data = await pageFences({ page: page.value, size: 20, keyword: keyword.value })
  rows.value = data.records
  total.value = data.total
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除围栏「${row.name}」？`, '提示', { type: 'warning' }).catch(() => Promise.reject('cancel'))
  await deleteFence(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>
