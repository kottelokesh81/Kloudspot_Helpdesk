import { createContext, useContext, useMemo, useState } from 'react'
import { api } from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('helpdesk_user')) } catch { return null }
  })

  async function login(email, password) {
    const result = await api('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })
    localStorage.setItem('helpdesk_token', result.token)
    localStorage.setItem('helpdesk_user', JSON.stringify(result.user))
    setUser(result.user)
  }

  function logout() {
    localStorage.removeItem('helpdesk_token')
    localStorage.removeItem('helpdesk_user')
    setUser(null)
  }

  const value = useMemo(() => ({ user, login, logout, isStaff: ['ORGANIZATION_ADMIN', 'SUPPORT_AGENT'].includes(user?.role) }), [user])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)

