import request from './request'

export const login = (data) => request.post('/auth/login', data)
export const logout = (data) => request.post('/auth/logout', data)
export const fetchMe = () => request.get('/system/me')
export const listRoles = () => request.get('/system/roles')

export const pageUsers = (params) => request.get('/system/users', { params })
export const createUser = (data) => request.post('/system/users', data)
export const updateUser = (data) => request.put('/system/users', data)
export const deleteUser = (id) => request.delete(`/system/users/${id}`)
export const assignRoles = (data) => request.put('/system/users/roles', data)

export const listGroups = () => request.get('/system/groups')
export const createGroup = (data) => request.post('/system/groups', data)
export const updateGroup = (data) => request.put('/system/groups', data)
export const deleteGroup = (id) => request.delete(`/system/groups/${id}`)

export const pageFences = (params) => request.get('/geo/fences', { params })
export const listFences = () => request.get('/geo/fences/all')
export const getFence = (id) => request.get(`/geo/fences/${id}`)
export const createFence = (data) => request.post('/geo/fences', data)
export const updateFence = (data) => request.put('/geo/fences', data)
export const deleteFence = (id) => request.delete(`/geo/fences/${id}`)

export const pageRules = (params) => request.get('/rules', { params })
export const getRule = (id) => request.get(`/rules/${id}`)
export const createRule = (data) => request.post('/rules', data)
export const updateRule = (data) => request.put('/rules', data)
export const deleteRule = (id) => request.delete(`/rules/${id}`)

export const doCheckin = (data) => request.post('/checkin', data)
export const fetchToday = () => request.get('/checkin/today')
export const pageRecords = (params) => request.get('/checkin/records', { params })
export const getRecord = (id) => request.get(`/checkin/records/${id}`)
export const manualCheckin = (data) => request.post('/checkin/records/manual', data)

export const pageDaily = (params) => request.get('/report/daily', { params })
export const rebuildReport = (params) => request.post('/report/rebuild', null, { params })

export const pageAudit = (params) => request.get('/audit/logs', { params })
