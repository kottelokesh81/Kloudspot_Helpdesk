const API_URL = import.meta.env.VITE_API_URL || ''

export async function api(path, options = {}) {
  const token = localStorage.getItem('helpdesk_token')
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  if (response.status === 401 && !path.includes('/login')) {
    localStorage.removeItem('helpdesk_token')
    localStorage.removeItem('helpdesk_user')
    window.location.assign('/login')
  }
  const body = response.status === 204 ? null : await response.json().catch(() => ({}))
  if (!response.ok) throw new Error(body?.message || 'Something went wrong')
  return body
}

export const pretty = value => value ? value.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase()) : '—'
export const shortDate = value => value ? new Intl.DateTimeFormat('en', { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(value)) : '—'
export const dateTime = value => value ? new Intl.DateTimeFormat('en', { month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit' }).format(new Date(value)) : '—'

