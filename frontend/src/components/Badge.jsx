import { pretty } from '../api'

export default function Badge({ value, type = 'status' }) {
  return <span className={`badge ${type}-${String(value).toLowerCase()}`}><i />{pretty(value)}</span>
}

