import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/checkin', component: () => import('../views/h5/CheckinH5.vue'), meta: { h5: true } },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('../views/DashboardView.vue') },
      { path: 'fence/list', component: () => import('../views/fence/FenceList.vue'), meta: { perm: 'fence:list' } },
      { path: 'fence/edit/:id?', component: () => import('../views/fence/FenceEdit.vue'), meta: { perm: 'fence:add' } },
      { path: 'rule/list', component: () => import('../views/rule/RuleList.vue'), meta: { perm: 'rule:list' } },
      { path: 'rule/edit/:id?', component: () => import('../views/rule/RuleEdit.vue'), meta: { perm: 'rule:add' } },
      { path: 'record/my', component: () => import('../views/record/MyRecords.vue') },
      { path: 'record/list', component: () => import('../views/record/RecordList.vue'), meta: { perm: 'record:list' } },
      { path: 'report/daily', component: () => import('../views/report/DailyReport.vue'), meta: { perm: 'report:daily' } },
      { path: 'system/user', component: () => import('../views/system/UserManage.vue'), meta: { perm: 'sys:user:list' } },
      { path: 'system/group', component: () => import('../views/system/GroupManage.vue'), meta: { perm: 'sys:group:list' } },
      { path: 'audit/log', component: () => import('../views/audit/AuditLog.vue'), meta: { perm: 'audit:list' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public || to.meta.h5) {
    return true
  }
  if (!auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.perm && !auth.hasPerm(to.meta.perm)) {
    return { path: '/dashboard' }
  }
  return true
})

export default router
