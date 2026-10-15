import { ArrowLeft, Check, Clock3, LockKeyhole, MessageSquare, Pencil, Send, UserRound } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, dateTime, pretty, shortDate } from '../api'
import { useAuth } from '../AuthContext'
import Badge from '../components/Badge'

export default function TicketPage() {
  const { id } = useParams(); const { user, isStaff } = useAuth()
  const [ticket, setTicket] = useState(null); const [agents, setAgents] = useState([])
  const [comment, setComment] = useState(''); const [internal, setInternal] = useState(false)
  const [editing, setEditing] = useState(false); const [edit, setEdit] = useState(null)
  const [error, setError] = useState(''); const [busy, setBusy] = useState(false)

  useEffect(() => { api(`/api/tickets/${id}`).then(t => { setTicket(t); setEdit({ subject: t.subject, description: t.description, priority: t.priority }) }).catch(e => setError(e.message)); if (isStaff) api('/api/users/agents').then(setAgents) }, [id, isStaff])
  async function mutate(path, body, method = 'PATCH') {
    setBusy(true); setError('')
    try { setTicket(await api(`/api/tickets/${id}${path}`, { method, body: JSON.stringify(body) })) }
    catch (e) { setError(e.message) } finally { setBusy(false) }
  }
  async function addComment(e) {
    e.preventDefault(); if (!comment.trim()) return
    await mutate('/comments', { body: comment, internal }, 'POST'); setComment(''); setInternal(false)
  }
  async function saveDetails(e) {
    e.preventDefault(); await mutate('', edit, 'PUT'); setEditing(false)
  }
  if (error && !ticket) return <div className="state-page"><h2>Could not open this ticket</h2><p>{error}</p><Link className="button ghost" to="/tickets">Back to tickets</Link></div>
  if (!ticket) return <div className="detail-loading"><i /><i /><i /></div>

  return <div className="ticket-detail-page">
    <Link className="back-link" to="/tickets"><ArrowLeft size={17} /> Back to tickets</Link>
    {error && <div className="alert error">{error}</div>}
    <div className="detail-heading"><div><div className="detail-kicker"><span>#{ticket.id.slice(-6).toUpperCase()}</span><Badge value={ticket.status} /></div><h1>{ticket.subject}</h1><p>Opened by {ticket.requesterName} on {shortDate(ticket.createdAt)}</p></div>{isStaff && <button className="button ghost compact" onClick={() => setEditing(!editing)}><Pencil size={16} /> Edit details</button>}</div>
    <div className="detail-grid">
      <div className="detail-main">
        {editing ? <form className="panel edit-panel" onSubmit={saveDetails}><label>Subject<input value={edit.subject} onChange={e => setEdit({ ...edit, subject: e.target.value })} required /></label><label>Description<textarea rows="6" value={edit.description} onChange={e => setEdit({ ...edit, description: e.target.value })} required /></label><label>Priority<select value={edit.priority} onChange={e => setEdit({ ...edit, priority: e.target.value })}>{['LOW','MEDIUM','HIGH','URGENT'].map(x => <option key={x}>{x}</option>)}</select></label><div className="form-actions"><button type="button" className="button ghost" onClick={() => setEditing(false)}>Cancel</button><button className="button primary" disabled={busy}>Save changes</button></div></form>
        : <section className="panel description-panel"><span className="section-label">Description</span><p>{ticket.description}</p></section>}
        <section className="panel conversation-panel"><div className="section-heading"><div><MessageSquare size={18} /><h2>Conversation</h2></div><span>{ticket.comments.length} {ticket.comments.length === 1 ? 'reply' : 'replies'}</span></div>
          <div className="comment-list">{ticket.comments.length === 0 && <div className="no-comments">No replies yet. Start the conversation below.</div>}{ticket.comments.map(c => <article className={`comment ${c.internal ? 'internal' : ''}`} key={c.id}><div className="comment-avatar">{c.authorName?.[0]}</div><div><div className="comment-head"><strong>{c.authorName}</strong><span>{pretty(c.authorRole)}</span>{c.internal && <mark><LockKeyhole size={12} /> Internal note</mark>}<time>{dateTime(c.createdAt)}</time></div><p>{c.body}</p></div></article>)}</div>
          <form className="comment-form" onSubmit={addComment}><textarea value={comment} onChange={e => setComment(e.target.value)} rows="4" placeholder={internal ? 'Write a note only staff can see…' : 'Write a reply…'} required />
            <div>{isStaff ? <label className="internal-toggle"><input type="checkbox" checked={internal} onChange={e => setInternal(e.target.checked)} /><span><LockKeyhole size={14} /> Internal note</span></label> : <span className="reply-help">Your reply will be visible to support staff.</span>}<button className="button primary compact" disabled={busy || !comment.trim()}><Send size={16} /> {internal ? 'Add note' : 'Send reply'}</button></div>
          </form>
        </section>
      </div>
      <aside className="detail-side">
        <section className="panel details-panel"><h3>Ticket details</h3><div className="detail-field"><span>Status</span>{isStaff ? <select value={ticket.status} disabled={busy} onChange={e => mutate('/status', { status: e.target.value })}>{['OPEN','IN_PROGRESS','WAITING_ON_CUSTOMER','RESOLVED','CLOSED'].map(s => <option key={s} value={s}>{pretty(s)}</option>)}</select> : <Badge value={ticket.status} />}</div>
          <div className="detail-field"><span>Priority</span><Badge value={ticket.priority} type="priority" /></div>
          <div className="detail-field"><span>Assigned to</span>{isStaff ? <select value={ticket.assignedAgentId || ''} disabled={busy} onChange={e => mutate('/assignee', { agentId: e.target.value || null })}><option value="">Unassigned</option>{agents.map(a => <option value={a.id} key={a.id}>{a.name}</option>)}</select> : <strong>{ticket.assignedAgentName || 'Unassigned'}</strong>}</div>
          <div className="detail-field"><span>Requester</span><strong className="inline-person"><UserRound size={15} />{ticket.requesterName}</strong></div>
          <div className="detail-field"><span>Last updated</span><strong>{dateTime(ticket.updatedAt)}</strong></div>
        </section>
        <section className="panel history-panel"><h3><Clock3 size={17} /> Activity</h3><div className="timeline">{ticket.history.slice().reverse().map(h => <div className="timeline-item" key={h.id}><i>{h.field === 'status' ? <Check size={11} /> : <UserRound size={11} />}</i><div><p><strong>{h.actorName}</strong> changed {h.field}</p><span>{h.fromValue ? `${pretty(h.fromValue)} → ` : ''}{pretty(h.toValue)}</span><time>{dateTime(h.createdAt)}</time></div></div>)}</div></section>
      </aside>
    </div>
  </div>
}
