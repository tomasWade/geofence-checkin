import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    perms: JSON.parse(localStorage.getItem('perms') || '[]'),
  }),
  getters: {
    isLoggedIn: (s) => !!s.accessToken,
    hasPerm: (s) => (code) => s.perms.includes(code),
  },
  actions: {
    setLogin(resp) {
      this.accessToken = resp.accessToken
      this.refreshToken = resp.refreshToken
      this.perms = resp.perms || []
      this.userInfo = { userId: resp.userId, username: resp.username, realName: resp.realName, roles: resp.roles }
      localStorage.setItem('accessToken', resp.accessToken)
      localStorage.setItem('refreshToken', resp.refreshToken)
      localStorage.setItem('perms', JSON.stringify(this.perms))
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    },
    refreshPerms() {
      const info = this.userInfo
      if (!info) return Promise.resolve()
      return import('../api').then(({ fetchMe }) =>
        fetchMe().then((me) => {
          this.perms = me.perms || []
          localStorage.setItem('perms', JSON.stringify(this.perms))
        }),
      )
    },
    clear() {
      this.accessToken = ''
      this.refreshToken = ''
      this.userInfo = null
      this.perms = []
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('perms')
      localStorage.removeItem('userInfo')
    },
  },
})
