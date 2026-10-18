<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="8">
        <div class="page-card">
          <h3>今日打卡</h3>
          <div v-if="records.length === 0" style="color: #999">今日暂无打卡记录</div>
          <el-timeline v-else style="margin-top: 12px">
            <el-timeline-item v-for="r in records" :key="r.id" :timestamp="fmtTime(r.checkinTime)">
              {{ r.fenceName }} · 距圆心 {{ r.distanceM?.toFixed(0) }} 米
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
      <el-col :span="16">
        <div class="page-card">
          <h3>快捷入口</h3>
          <div style="display: flex; gap: 12px; margin-top: 16px; flex-wrap: wrap">
            <el-button type="primary" @click="$router.push('/checkin')">去打卡（H5）</el-button>
            <el-button v-perm="'fence:list'" @click="$router.push('/fence/list')">围栏管理</el-button>
            <el-button v-perm="'rule:list'" @click="$router.push('/rule/list')">打卡规则</el-button>
            <el-button v-perm="'report:daily'" @click="$router.push('/report/daily')">考勤日报</el-button>
          </div>
          <el-alert style="margin-top: 20px" type="info" :closable="false"
                    title="提示：H5 打卡需要 HTTPS 与定位授权" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchToday } from '../api'

const records = ref([])

function fmtTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}

onMounted(async () => {
  try {
    records.value = await fetchToday()
  } catch (e) {
    records.value = []
  }
})
</script>
