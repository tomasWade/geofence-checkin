<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="用户名/姓名" style="width: 220px" clearable @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button v-perm="'sys:user:add'" type="success" @click="openEdit()">新增用户</el-button>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column prop="mobile" label="手机号" width="140" />
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">{{ (row.roleCodes || []).join(', ') }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230">
        <template #default="{ row }">
          <el-button v-perm="'sys:user:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-perm="'sys:user:assignRole'" link type="primary" @click="openRole(row)">分配角色</el-button>
          <el-button v-perm="'sys:user:delete'" link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination background layout="total, prev, pager, next" :total="total" v-model:current-page="page"
                     :page-size="20" @current-change="load" />
    </div>

    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑用户' : '新增用户'" width="480px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" :disabled="!!editForm.id" />
        </el-form-item>
        <el-form-item v-if="!editForm.id" label="初始密码">
          <el-input v-model="editForm.password" type="password" />
        </el-form-item>
        <el-form-item v-if="editForm.id" label="重置密码">
          <el-input v-model="editForm.password" type="password" placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.mobile" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog v-model="roleVisible" title="分配角色" width="420px">
      <el-select v-model="roleIds" multiple style="width: 100%">
        <el-option v-for="r in roles" :key="r.id" :value="r.id" :label="r.roleName" />
      </el-select>
      <el-button type="primary" style="margin-top: 16px" :loading="saving" @click="saveRoles">保存</el-button>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageUsers, createUser, updateUser, deleteUser, assignRoles, listRoles } from '../../api'

const rows = ref([])
const total = ref(0)
const page = ref(1)
const keyword = ref('')
const roles = ref([])
const editVisible = ref(false)
const roleVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ id: null, username: '', password: '', realName: '', mobile: '' })
const roleIds = ref([])
const roleUserId = ref(null)

async function load() {
  const data = await pageUsers({ page: page.value, size: 20, keyword: keyword.value })
  rows.value = data.records
  total.value = data.total
}

function openEdit(row) {
  Object.assign(editForm, row
    ? { id: row.id, username: row.username, password: '', realName: row.realName, mobile: row.mobile }
    : { id: null, username: '', password: '', realName: '', mobile: '' })
  editVisible.value = true
}

async function save() {
  if (!editForm.id && (!editForm.username || !editForm.password)) {
    ElMessage.warning('用户名与初始密码必填')
    return
  }
  saving.value = true
  try {
    if (editForm.id) {
      await updateUser({ id: editForm.id, realName: editForm.realName, mobile: editForm.mobile,
        password: editForm.password || null })
    } else {
      await createUser(editForm)
    }
    ElMessage.success('已保存')
    editVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

function openRole(row) {
  roleUserId.value = row.id
  roleIds.value = [...(row.roleIds || [])]
  roleVisible.value = true
}

async function saveRoles() {
  saving.value = true
  try {
    await assignRoles({ userId: roleUserId.value, roleIds: roleIds.value })
    ElMessage.success('已保存')
    roleVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { type: 'warning' }).catch(() => Promise.reject('cancel'))
  await deleteUser(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  load()
  roles.value = await listRoles().catch(() => [])
})
</script>
