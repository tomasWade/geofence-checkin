<template>
  <div class="page-card">
    <div class="toolbar">
      <el-date-picker v-model="date" type="date" value-format="YYYY-MM-DD" placeholder="按日期过滤" clearable @change="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button v-perm="'record:manualFix'" type="warning" @click="manualVisible = true">代打卡</el-button>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="userName" label="员工" width="110" />
      <el-table-column label="打卡时间" width="170">
        <template #default="{ row }">{{ fmtTime(row.checkinTime) }}</template>
      </el-table-column>
      <el-table-column prop="fenceName" label="围栏" min-width="140" />
      <el-table-column label="距圆心(米)" width="100">
        <template #default="{ row }">{{ row.distanceM?.toFixed(0) }}</template>
      </el-table-column>
      <el-table-column label="可疑" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.suspicious === 1" type="danger" size="small">可疑</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="source" label="来源" width="90" />
      <el-table-column label="操作" width="90">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="total" v-model:current-page="page"
                     :page-size="20" @current-change="load" />
    </div>

    <el-dialog v-model="detailVisible" title="打卡详情" width="560px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="员工">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="围栏">{{ detail.fenceName }}</el-descriptions-item>
        <el-descriptions-item label="打卡时间">{{ fmtTime(detail.checkinTime) }}</el-descriptions-item>
        <el-descriptions-item label="距圆心">{{ detail.distanceM?.toFixed(1) }} 米</el-descriptions-item>
        <el-descriptions-item label="坐标">{{ Number(detail.lng).toFixed(6) }}, {{ Number(detail.lat).toFixed(6) }}</el-descriptions-item>
        <el-descriptions-item label="定位精度">{{ detail.accuracyM ? detail.accuracyM.toFixed(0) + ' 米' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="代打卡人" :span="2">{{ detail.manualByName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="manualVisible" title="代打卡/补录" width="520px">
      <el-form :model="manualForm" label-width="90px">
        <el-form-item label="员工">
          <el-select v-model="manualForm.userId" filterable style="width: 100%">
            <el-option v-for="u in users" :key="u.id" :value="u.id" :label="u.realName || u.username" />
          </el-select>
        </el-form-item>
        <el-form-item label="围栏">
          <el-select v-model="manualForm.fenceId" style="width: 100%">
            <el-option v-for="f in fences" :key="f.id" :value="f.id" :label="f.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="打卡时间">
          <el-date-picker v-model="manualForm.checkinTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss"
                          placeholder="选择时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注(必填)">
          <el-input v-model="manualForm.remark" type="textarea" placeholder="补录原因，将记入审计" />
        </el-form-item>
        <el-button type="primary" :loading="manualSaving" @click="submitManual">提交</el-button>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageRecords, pageUsers, listFences, manualCheckin } from '../../api'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const date = ref(null)
const users = ref([])
const fences = ref([])
const detailVisible = ref(false)
const detail = ref({})
const manualVisible = ref(false)
const manualSaving = ref(false)
const manualForm = reactive({ userId: null, fenceId: null, checkinTime: '', remark: '' })

function fmtTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}

async function load() {
  const data = await pageRecords({ page: page.value, size: 20, date: date.value || undefined })
  rows.value = data.records
  total.value = data.total
}

function showDetail(row) {
  detail.value = row
  detailVisible.value = true
}

async function submitManual() {
  if (!manualForm.userId || !manualForm.fenceId || !manualForm.checkinTime || !manualForm.remark) {
    ElMessage.warning('请完整填写代打卡信息（备注必填）')
    return
  }
  manualSaving.value = true
  try {
    await manualCheckin(manualForm)
    ElMessage.success('已补录')
    manualVisible.value = false
    manualForm.remark = ''
    load()
  } finally {
    manualSaving.value = false
  }
}

onMounted(async () => {
  load()
  pageUsers({ page: 1, size: 200 }).then((d) => (users.value = d.records)).catch(() => {})
  listFences().then((d) => (fences.value = d)).catch(() => {})
})
</script>
