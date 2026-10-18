import AMapLoader from '@amap/amap-jsapi-loader'

let amapPromise = null

export function loadAMap() {
  if (!amapPromise) {
    amapPromise = AMapLoader.load({
      key: import.meta.env.VITE_AMAP_KEY || '',
      version: '2.0',
      plugins: ['AMap.Geolocation', 'AMap.Geocoder'],
    }).catch((err) => {
      amapPromise = null
      throw err
    })
  }
  return amapPromise
}

export function hasAmapKey() {
  return !!(import.meta.env.VITE_AMAP_KEY && import.meta.env.VITE_AMAP_KEY.trim())
}
