import { ChevronLeft, ChevronRight, CircleCheck, Clock3, Plus, Search, SlidersHorizontal, TicketCheck } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api, shortDate } from '../api'
import { useAuth } from '../AuthContext'
import Badge from '../components/Badge'
import EmptyState from '../components/EmptyState'

const blank = { content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 }

export default function TicketListPage() {
  const { user, isStaff } = useAuth()
  const navigate = useNavigate()
  const [data, setData] = useState(blank)
  const [agents, setAgents] = useState([])
  const [filters, setFilters] = useState({ search: '', status: '', priority: '', assignedAgentId: '', page: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => { if (isStaff) api('/api/users/agents').then(setAgents).catch(() => {}) }, [isStaff])
  useEffect(() => {
    const timer = setTimeout(async () => {
      setLoading(true); setError('')
      const query = new URLSearchParams({ page: filters.page, size: 10 })
      Object.entries(filters).forEach(([k, v]) => { if (k !== 'page' && v) query.set(k, v) })
      try { setData(await api(`/api/tickets?${query}`)) } catch (err) { setError(err.message) } finally { setLoading(false) }
    }, filters.search ? 280 : 0)
    return () => clearTimeout(timer)
  }, [filters])

  const metrics = useMemo(() => ({
    open: data.content.filter(t => ['OPEN', 'IN_PROGRESS'].includes(t.status)).length,
    waiting: data.content.filter(t => t.status === 'WAITING_ON_CUSTOMER').length,
    closed: data.content.filter(t => ['RESOLVED', 'CLOSED'].includes(t.status)).length,
  }), [data])
  const setFilter = (key, value) => setFilters(f => ({ ...f, [key]: value, page: key === 'page' ? value : 0 }))

  return <>
    <div className="page-heading"><div><span className="overline">{user.organizationName}</span><h1>{isStaff ? 'Support queue' : 'My tickets'}</h1><p>{isStaff ? 'Review, assign, and resolve customer requests.' : 'Track your requests and talk with the support team.'}</p></div><Link className="button primary heading-button" to="/tickets/new"><Plus size={18} /> New ticket</Link></div>
    <div className="metric-grid">
      <div className="metric-card"><div className="metric-icon coral"><TicketCheck size={20} /></div><div><span>Visible tickets</span><strong>{data.totalElements}</strong></div><small>Current queue</small></div>
      <div className="metric-card"><div className="metric-icon blue"><Clock3 size={20} /></div><div><span>Active on page</span><strong>{metrics.open}</strong></div><small>Open or in progress</small></div>
      <div className="metric-card"><div className="metric-icon gold"><SlidersHorizontal size={20} /></div><div><span>Waiting</span><strong>{metrics.waiting}</strong></div><small>Customer response</small></div>
      <div className="metric-card"><div className="metric-icon green"><CircleCheck size={20} /></div><div><span>Done on page</span><strong>{metrics.closed}</strong></div><small>Resolved or closed</small></div>
    </div>
    <section className="panel ticket-panel">
      <div className="panel-toolbar">
        <div className="search-box"><Search size={18} /><input aria-label="Search tickets" placeholder="Search subject or description…" value={filters.search} onChange={e => setFilter('search', e.target.value)} /></div>
        <select aria-label="Filter by status" value={filters.status} onChange={e => setFilter('status', e.target.value)}><option value="">All statuses</option><option value="OPEN">Open</option><option value="IN_PROGRESS">In progress</option><option value="WAITING_ON_CUSTOMER">Waiting on customer</option><option value="RESOLVED">Resolved</option><option value="CLOSED">Closed</option></select>
        <select aria-label="Filter by priority" value={filters.priority} onChange={e => setFilter('priority', e.target.value)}><option value="">All priorities</option><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">High</option><option value="URGENT">Urgent</option></select>
        {isStaff && <select aria-label="Filter by agent" value={filters.assignedAgentId} onChange={e => setFilter('assignedAgentId', e.target.value)}><option value="">All agents</option>{agents.map(a => <option key={a.id} value={a.id}>{a.name}</option>)}</select>}
      </div>
      {error && <div className="alert error panel-alert">{error}</div>}
      <div className="ticket-table-wrap">
        <table className="ticket-table"><thead><tr><th>Ticket</th><th>Status</th><th>Priority</th>{isStaff && <th>Requester</th>}<th>Assigned to</th><th>Updated</th></tr></thead>
          <tbody>{!loading && data.content.map(ticket => <tr key={ticket.id} onClick={() => navigate(`/tickets/${ticket.id}`)}>
            <td><span className="ticket-id">#{ticket.id.slice(-6).toUpperCase()}</span><strong>{ticket.subject}</strong><small>{ticket.description}</small></td>
            <td><Badge value={ticket.status} /></td><td><Badge value={ticket.priority} type="priority" /></td>
            {isStaff && <td><span className="person"><i>{ticket.requesterName?.[0]}</i>{ticket.requesterName}</span></td>}
            <td>{ticket.assignedAgentName ? <span className="person"><i className="agent">{ticket.assignedAgentName[0]}</i>{ticket.assignedAgentName}</span> : <span className="unassigned">Unassigned</span>}</td>
            <td className="date-cell">{shortDate(ticket.updatedAt)}</td>
          </tr>)}</tbody></table>
        {loading && <div className="loading-list">{[1,2,3,4].map(x => <i key={x} />)}</div>}
        {!loading && !data.content.length && <EmptyState />}
      </div>
      <div className="pagination"><span>Showing {data.totalElements ? data.page * data.size + 1 : 0}–{Math.min((data.page + 1) * data.size, data.totalElements)} of {data.totalElements}</span><div><button className="icon-button" disabled={data.page === 0} onClick={() => setFilter('page', data.page - 1)}><ChevronLeft size={18} /></button><span>Page {data.totalPages ? data.page + 1 : 0} of {data.totalPages}</span><button className="icon-button" disabled={data.page + 1 >= data.totalPages} onClick={() => setFilter('page', data.page + 1)}><ChevronRight size={18} /></button></div></div>
    </section>
  </>
}

