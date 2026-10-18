<template>
  <div class="page-card" style="max-width: 760px">
    <h3>{{ form.id ? '编辑规则' : '新增规则' }}</h3>
    <el-form :model="form" label-width="110px" style="margin-top: 12px">
      <el-form-item label="规则名称">
        <el-input v-model="form.name" placeholder="如 标准班" />
      </el-form-item>
      <el-form-item label="绑定围栏">
        <el-select v-model="form.fenceId" placeholder="选择打卡围栏" style="width: 100%">
          <el-option v-for="f in fences" :key="f.id" :value="f.id" :label="f.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="上班 / 下班">
        <el-time-picker v-model="startHM" format="HH:mm" placeholder="上班时间" />
        <span style="margin: 0 8px">~</span>
        <el-time-picker v-model="endHM" format="HH:mm" placeholder="下班时间" />
      </el-form-item>
      <el-form-item label="迟到宽限(分)">
        <el-input-number v-model="lateMin" :min="0" :max="60" />
      </el-form-item>
      <el-form-item label="生效日">
        <el-checkbox-group v-model="days">
          <el-checkbox v-for="(d, i) in ['周一','周二','周三','周四','周五','周六','周日']" :key="i" :value="i">{{ d }}</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="绑定方式">
        <el-radio-group v-model="form.bindType">
          <el-radio value="ALL">全员</el-radio>
          <el-radio value="GROUP">按分组</el-radio>
          <el-radio value="ROLE">按角色</el-radio>
          <el-radio value="USER">按人员</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.bindType === 'GROUP'" label="选择分组">
        <el-select v-model="form.groupIds" multiple style="width: 100%">
          <el-option v-for="g in groups" :key="g.id" :value="g.id" :label="g.name" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.bindType === 'ROLE'" label="选择角色">
        <el-select v-model="form.roleIds" multiple style="width: 100%">
          <el-option v-for="r in roles" :key="r.id" :value="r.id" :label="r.roleName" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.bindType === 'USER'" label="选择人员">
        <el-select v-model="form.userIds" multiple filterable style="width: 100%">
          <el-option v-for="u in users" :key="u.id" :value="u.id" :label="u.realName || u.username" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="$router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRule, createRule, updateRule, listFences, listGroups, listRoles, pageUsers } from '../../api'

const route = useRoute()
const router = useRouter()
const saving = ref(false)

const form = reactive({
  id: null, name: '', fenceId: null, workStartSec: 32400, workEndSec: 64800,
  lateToleranceSec: 300, effectiveDays: 31, bindType: 'ALL', status: 1,
  roleIds: [], groupIds: [], userIds: [],
})
const startHM = ref(new Date(2000, 0, 1, 9, 0))
const endHM = ref(new Date(2000, 0, 1, 18, 0))
const lateMin = ref(5)
const days = ref([0, 1, 2, 3, 4])

const fences = ref([])
const groups = ref([])
const roles = ref([])
const users = ref([])

const effectiveBits = computed(() => days.value.reduce((acc, d) => acc | (1 << d), 0))

function hm2sec(d) {
  if (!d) return -1
  return d.getHours() * 3600 + d.getMinutes() * 60
}

function sec2date(sec) {
  return new Date(2000, 0, 1, Math.floor(sec / 3600), Math.floor((sec % 3600) / 60))
}

async function save() {
  if (!form.name || !form.fenceId) {
    ElMessage.warning('请填写名称并选择围栏')
    return
  }
  form.workStartSec = hm2sec(startHM.value)
  form.workEndSec = hm2sec(endHM.value)
  if (form.workStartSec < 0 || form.workEndSec < 0) {
    ElMessage.warning('请选择上班与下班时间')
    return
  }
  form.lateToleranceSec = lateMin.value * 60
  form.effectiveDays = effectiveBits.value
  if (form.workEndSec <= form.workStartSec) {
    ElMessage.error('下班时间必须晚于上班时间')
    return
  }
  saving.value = true
  const action = form.id ? updateRule(form) : createRule(form)
  action.then(() => {
    ElMessage.success('已保存')
    router.push('/rule/list')
  }).finally(() => (saving.value = false))
}

onMounted(async () => {
  fences.value = await listFences().catch(() => [])
  roles.value = await listRoles().catch(() => [])
  groups.value = await listGroups().catch(() => [])
  pageUsers({ page: 1, size: 200 }).then((d) => (users.value = d.records)).catch(() => {})
  if (route.params.id) {
    const r = await getRule(route.params.id)
    Object.assign(form, {
      id: r.id, name: r.name, fenceId: r.fenceId, workStartSec: r.workStartSec, workEndSec: r.workEndSec,
      lateToleranceSec: r.lateToleranceSec, effectiveDays: r.effectiveDays, bindType: r.bindType, status: r.status,
    })
    form.roleIds = (r.scopes || []).filter((s) => s.roleId).map((s) => s.roleId)
    form.groupIds = (r.scopes || []).filter((s) => s.groupId).map((s) => s.groupId)
    form.userIds = (r.scopes || []).filter((s) => s.userId).map((s) => s.userId)
    startHM.value = sec2date(r.workStartSec)
    endHM.value = sec2date(r.workEndSec)
    lateMin.value = Math.floor((r.lateToleranceSec || 0) / 60)
    days.value = [...Array(7).keys()].filter((i) => (r.effectiveDays & (1 << i)) !== 0)
  }
})
</script>
