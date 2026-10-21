<template>
  <el-container style="height: 100%">
    <el-aside width="210px" style="background: #001529">
      <div class="logo">围栏打卡</div>
      <el-menu :default-active="$route.path" router background-color="#001529" text-color="#bfcbd9"
               active-text-color="#409eff" style="border-right: none">
        <el-menu-item v-for="m in visibleMenus" :key="m.path" :index="m.path">
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display: flex; align-items: center; justify-content: flex-end; background: #fff">
        <span style="margin-right: 16px">{{ auth.userInfo?.realName || auth.userInfo?.username }}</span>
        <el-button link type="primary" @click="$router.push('/checkin')">H5 打卡</el-button>
        <el-button link type="danger" @click="doLogout">退出</el-button>
      </el-header>
      <el-main style="background: #f0f2f5">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { logout } from '../api'

const auth = useAuthStore()
const router = useRouter()

const menus = [
  { path: '/dashboard', label: '首页概览' },
  { path: '/fence/list', label: '围栏管理', perm: 'fence:list' },
  { path: '/rule/list', label: '打卡规则', perm: 'rule:list' },
  { path: '/record/my', label: '我的打卡' },
  { path: '/record/list', label: '记录查询', perm: 'record:list' },
  { path: '/report/daily', label: '考勤日报', perm: 'report:daily' },
  { path: '/system/user', label: '用户管理', perm: 'sys:user:list' },
  { path: '/system/group', label: '分组管理', perm: 'sys:group:list' },
  { path: '/audit/log', label: '审计日志', perm: 'audit:list' },
]

const visibleMenus = computed(() => menus.filter((m) => !m.perm || auth.hasPerm(m.perm)))

async function doLogout() {
  try {
    await logout({ refreshToken: auth.refreshToken })
  } catch (e) {
    /* ignore */
  }
  auth.clear()
  router.push('/login')
}
</script>

<style scoped>
.logo {
  height: 56px;
  line-height: 56px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  letter-spacing: 4px;
}
</style>
