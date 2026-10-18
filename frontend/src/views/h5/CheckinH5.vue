<template>
  <div class="h5-wrap">
    <div v-if="!auth.isLoggedIn" class="h5-login">
      <h2>围栏打卡</h2>
      <el-input v-model="loginForm.username" placeholder="用户名" style="margin-bottom: 12px" />
      <el-input v-model="loginForm.password" type="password" placeholder="密码" show-password style="margin-bottom: 16px"
                @keyup.enter="doLogin" />
      <el-button type="primary" style="width: 100%" :loading="loading" @click="doLogin">登 录</el-button>
    </div>

    <div v-else class="h5-main">
      <div class="h5-header">
        <span>{{ auth.userInfo?.realName || auth.userInfo?.username }}</span>
        <el-button link type="danger" size="small" @click="auth.clear()">退出</el-button>
      </div>

      <div class="h5-status">
        <div v-if="locStatus === 'idle'" class="status-text">点击下方按钮开始定位打卡</div>
        <div v-else-if="locStatus === 'locating'" class="status-text">正在定位…</div>
        <div v-else-if="locStatus === 'manual'" class="status-text">
          <div v-if="isProd">定位服务未配置（缺少地图 key），请联系管理员</div>
          <template v-else>
            <div style="margin-bottom: 6px">未配置地图 key，手动模式（仅开发）</div>
            <div style="display: flex; gap: 8px">
              <el-input v-model="manualLng" placeholder="经度" />
              <el-input v-model="manualLat" placeholder="纬度" />
            </div>
          </template>
        </div>
        <div v-else class="status-text">定位成功：{{ Number(lng).toFixed(5) }}, {{ Number(lat).toFixed(5) }}
          <span v-if="accuracy">(±{{ accuracy.toFixed(0) }}m)</span>
        </div>
      </div>

      <div class="h5-result" v-if="result">
        <div :class="['result-card', result.ok ? 'ok' : 'fail']">
          <div class="result-title">{{ result.ok ? '打卡成功' : '打卡失败' }}</div>
          <div class="result-detail">{{ result.msg }}</div>
        </div>
      </div>

      <div class="h5-btn-area">
        <button class="checkin-btn" :disabled="busy" @click="tryCheckin(false)">{{ busy ? '处理中…' : '打 卡' }}</button>
      </div>

      <div class="h5-today" v-if="todayRecords.length">
        <h4>今日记录</h4>
        <div v-for="r in todayRecords" :key="r.id" class="today-item">
          <span>{{ fmtTime(r.checkinTime) }}</span>
          <span>{{ r.fenceName }} · {{ r.distanceM?.toFixed(0) }}m</span>
        </div>
      </div>

      <div class="h5-dev" v-if="hasKey && !isProd">
        <el-button size="small" @click="tryCheckin(true)">模拟圈外测试</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { login, doCheckin, fetchToday } from '../../api'
import { useAuthStore } from '../../stores/auth'
import { loadAMap, hasAmapKey } from '../../utils/amap'

const auth = useAuthStore()
const hasKey = hasAmapKey()
const isProd = import.meta.env.PROD
const loginForm = reactive({ username: '', password: '' })
const loading = ref(false)
const busy = ref(false)
const locStatus = ref('idle')
const lng = ref(null)
const lat = ref(null)
const accuracy = ref(null)
const manualLng = ref('104.065735')
const manualLat = ref('30.657008')
const result = ref(null)
const todayRecords = ref([])

function fmtTime(t) {
  return t ? String(t).replace('T', ' ').slice(11, 19) : ''
}

async function doLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入账号密码')
    return
  }
  loading.value = true
  try {
    const resp = await login(loginForm)
    auth.setLogin(resp)
    loadToday()
  } catch (e) {
    /* toast 已由拦截器弹出 */
  } finally {
    loading.value = false
  }
}

async function loadToday() {
  try {
    todayRecords.value = await fetchToday()
  } catch (e) {
    todayRecords.value = []
  }
}

function locate() {
  return new Promise((resolve, reject) => {
    if (!hasKey) {
      locStatus.value = 'manual'
      if (isProd) {
        reject(new Error('定位服务未配置，请联系管理员'))
        return
      }
      if (!manualLng.value || !manualLat.value) {
        reject(new Error('请填写经纬度'))
        return
      }
      lng.value = Number(manualLng.value)
      lat.value = Number(manualLat.value)
      accuracy.value = null
      resolve()
      return
    }
    locStatus.value = 'locating'
    loadAMap().then((AMap) => {
      AMap.plugin('AMap.Geolocation', () => {
        const geolocation = new AMap.Geolocation({ enableHighAccuracy: true, timeout: 12000 })
        geolocation.getCurrentPosition((status, position) => {
          if (status === 'complete' && position) {
            lng.value = position.position.lng
            lat.value = position.position.lat
            accuracy.value = position.accuracy
            locStatus.value = 'ok'
            resolve()
          } else {
            locStatus.value = 'idle'
            reject(new Error('定位失败，请检查定位授权'))
          }
        })
      })
    }).catch(reject)
  })
}

async function tryCheckin(mockOutside) {
  result.value = null
  busy.value = true
  try {
    if (locStatus.value !== 'manual') {
      await locate()
    }
    const useLng = locStatus.value === 'manual' ? Number(manualLng.value) : lng.value
    const useLat = locStatus.value === 'manual' ? Number(manualLat.value) : lat.value
    const resp = await doCheckin({
      lng: useLng,
      lat: useLat,
      accuracyM: accuracy.value,
      clientTime: new Date().toLocaleString('sv-SE').replace(' ', 'T'),
      mockOutside: mockOutside || undefined,
    })
    result.value = { ok: true, msg: `${resp.fenceName} · 距圆心 ${resp.distanceM?.toFixed(0)} 米 · ${resp.message}` }
    loadToday()
  } catch (e) {
    const msg = e?.response?.data?.msg || e?.message || '打卡失败'
    result.value = { ok: false, msg }
    if (locStatus.value === 'manual') locStatus.value = 'manual'
  } finally {
    busy.value = false
  }
}

onMounted(() => {
  if (auth.isLoggedIn) {
    loadToday()
    if (!hasKey) locStatus.value = 'manual'
  }
})
</script>

<style scoped>
.h5-wrap {
  height: 100%;
  max-width: 480px;
  margin: 0 auto;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.h5-login {
  margin: auto;
  width: 80%;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.h5-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.h5-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 4px;
}

.h5-status {
  background: #fff;
  border-radius: 10px;
  padding: 18px 14px;
  margin-top: 12px;
}

.status-text {
  color: #555;
  font-size: 14px;
  line-height: 1.6;
}

.h5-result {
  margin-top: 14px;
}

.result-card {
  border-radius: 10px;
  padding: 16px;
  color: #fff;
}

.result-card.ok {
  background: #67c23a;
}

.result-card.fail {
  background: #f56c6c;
}

.result-title {
  font-size: 18px;
  font-weight: 600;
}

.result-detail {
  font-size: 13px;
  margin-top: 6px;
  opacity: 0.9;
}

.h5-btn-area {
  display: flex;
  justify-content: center;
  margin: 28px 0;
}

.checkin-btn {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  border: none;
  background: radial-gradient(circle at 35% 35%, #79bbff, #409eff);
  color: #fff;
  font-size: 24px;
  letter-spacing: 6px;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.45);
}

.checkin-btn:disabled {
  opacity: 0.6;
}

.h5-today {
  background: #fff;
  border-radius: 10px;
  padding: 12px 14px;
}

.h5-today h4 {
  margin: 4px 0 10px;
}

.today-item {
  display: flex;
  justify-content: space-between;
  color: #666;
  font-size: 13px;
  padding: 6px 0;
  border-bottom: 1px dashed #eee;
}

.h5-dev {
  text-align: center;
  margin-top: 10px;
}
</style>
