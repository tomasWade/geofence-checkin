<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button v-perm="'sys:group:add'" type="success" @click="openEdit()">新增分组</el-button>
    </div>
    <el-table :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="分组名称" min-width="140" />
      <el-table-column prop="managerName" label="组长" width="110" />
      <el-table-column label="成员数" width="90">
        <template #default="{ row }">{{ row.memberUserIds?.length || 0 }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-perm="'sys:group:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-perm="'sys:group:delete'" link type="danger" @click="del(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑分组' : '新增分组'" width="520px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="分组名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="组长">
          <el-select v-model="editForm.managerUserId" filterable clearable style="width: 100%">
            <el-option v-for="u in users" :key="u.id" :value="u.id" :label="u.realName || u.username" />
          </el-select>
        </el-form-item>
        <el-form-item label="成员">
          <el-select v-model="editForm.memberUserIds" multiple filterable style="width: 100%">
            <el-option v-for="u in users" :key="u.id" :value="u.id" :label="u.realName || u.username" />
          </el-select>
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listGroups, createGroup, updateGroup, deleteGroup, pageUsers } from '../../api'

const rows = ref([])
const users = ref([])
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({ id: null, name: '', managerUserId: null, memberUserIds: [] })

async function load() {
  rows.value = await listGroups()
}

function openEdit(row) {
  Object.assign(editForm, row
    ? { id: row.id, name: row.name, managerUserId: row.managerUserId, memberUserIds: [...(row.memberUserIds || [])] }
    : { id: null, name: '', managerUserId: null, memberUserIds: [] })
  editVisible.value = true
}

async function save() {
  if (!editForm.name) {
    ElMessage.warning('请填写分组名称')
    return
  }
  saving.value = true
  try {
    const action = editForm.id ? updateGroup(editForm) : createGroup(editForm)
    await action
    ElMessage.success('已保存')
    editVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

async function del(row) {
  await ElMessageBox.confirm(`确认删除分组「${row.name}」？`, '提示', { type: 'warning' }).catch(() => Promise.reject('cancel'))
  await deleteGroup(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(async () => {
  load()
  pageUsers({ page: 1, size: 200 }).then((d) => (users.value = d.records)).catch(() => {})
})
</script>
