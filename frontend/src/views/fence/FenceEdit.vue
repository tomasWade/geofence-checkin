<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="12">
        <div class="page-card">
          <h3>{{ form.id ? '编辑围栏' : '新增围栏' }}</h3>
          <el-form :model="form" label-width="90px" style="margin-top: 12px">
            <el-form-item label="名称">
              <el-input v-model="form.name" placeholder="如 总部办公区" />
            </el-form-item>
            <el-form-item label="圆心坐标">
              <el-input v-model="form.lng" placeholder="经度，点击左侧地图取点" style="margin-bottom: 8px" />
              <el-input v-model="form.lat" placeholder="纬度" />
            </el-form-item>
            <el-form-item label="半径(米)">
              <el-input-number v-model="form.radiusM" :min="30" :max="5000" />
              <span style="margin-left: 8px; color: #999; font-size: 12px">建议 ≥150</span>
            </el-form-item>
            <el-form-item label="状态">
              <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="save">保存</el-button>
              <el-button @click="$router.back()">返回</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page-card">
          <h3>地图选点<el-tag v-if="!hasKey" type="warning" size="small" style="margin-left: 8px">未配置高德 key，仅可手填坐标</el-tag></h3>
          <div id="map-container" style="width: 100%; height: 460px; margin-top: 12px; border-radius: 6px"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFence, createFence, updateFence } from '../../api'
import { loadAMap, hasAmapKey } from '../../utils/amap'

const route = useRoute()
const router = useRouter()
const hasKey = hasAmapKey()
const saving = ref(false)

const form = reactive({
  id: null, name: '', lng: '', lat: '', radiusM: 200, status: 1,
})

let map = null
let circle = null
let marker = null
let AMap = null

function drawFence() {
  if (!map || !AMap || !form.lng || !form.lat) return
  const center = [Number(form.lng), Number(form.lat)]
  map.setCenter(center)
  if (!circle) {
    circle = new AMap.Circle({
      center, radius: form.radiusM, strokeColor: '#409eff', strokeWeight: 2,
      fillColor: '#409eff', fillOpacity: 0.15,
    })
    map.add(circle)
  } else {
    circle.setCenter(center)
    circle.setRadius(form.radiusM)
  }
  if (!marker) {
    marker = new AMap.Marker({ position: center })
    map.add(marker)
  } else {
    marker.setPosition(center)
  }
}

async function initMap() {
  if (!hasKey) return
  try {
    AMap = await loadAMap()
    map = new AMap.Map('map-container', { zoom: 15 })
    map.on('click', (e) => {
      form.lng = e.lnglat.getLng().toFixed(6)
      form.lat = e.lnglat.getLat().toFixed(6)
      drawFence()
    })
    drawFence()
  } catch (e) {
    ElMessage.warning('地图加载失败：' + (e.message || e))
  }
}

function save() {
  if (!form.name || !form.lng || !form.lat) {
    ElMessage.warning('请填写名称与圆心坐标')
    return
  }
  saving.value = true
  const payload = { ...form, lng: Number(form.lng), lat: Number(form.lat) }
  const action = form.id ? updateFence(payload) : createFence(payload)
  action.then(() => {
    ElMessage.success('已保存')
    router.push('/fence/list')
  }).finally(() => (saving.value = false))
}

onMounted(async () => {
  if (route.params.id) {
    const f = await getFence(route.params.id)
    Object.assign(form, { id: f.id, name: f.name, lng: String(f.lng), lat: String(f.lat), radiusM: f.radiusM, status: f.status })
  }
  initMap()
})

onBeforeUnmount(() => {
  if (map) map.destroy()
})
</script>
