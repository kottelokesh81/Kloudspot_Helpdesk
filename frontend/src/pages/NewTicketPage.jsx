import { ArrowLeft, ArrowRight, LifeBuoy } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api'

export default function NewTicketPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ subject: '', description: '', priority: 'MEDIUM' })
  const [error, setError] = useState(''); const [saving, setSaving] = useState(false)
  const change = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))
  async function submit(e) {
    e.preventDefault(); setSaving(true); setError('')
    try { const ticket = await api('/api/tickets', { method: 'POST', body: JSON.stringify(form) }); navigate(`/tickets/${ticket.id}`) }
    catch (err) { setError(err.message) } finally { setSaving(false) }
  }
  return <div className="form-page">
    <Link className="back-link" to="/tickets"><ArrowLeft size={17} /> Back to tickets</Link>
    <div className="form-layout"><div className="form-intro"><div className="big-icon"><LifeBuoy size={30} /></div><span className="overline">New request</span><h1>How can we help?</h1><p>Share the details and the right person from your support team will pick it up.</p><ul><li>Use a clear, specific subject</li><li>Describe what you expected to happen</li><li>Include any steps that help us reproduce it</li></ul></div>
      <form className="panel create-form" onSubmit={submit}>{error && <div className="alert error">{error}</div>}
        <label>Subject<span>Briefly summarize your request</span><input name="subject" maxLength="160" value={form.subject} onChange={change} placeholder="e.g. Dashboard does not load" required autoFocus /></label>
        <label>Description<span>Tell us what happened and what you have already tried</span><textarea name="description" maxLength="5000" rows="8" value={form.description} onChange={change} placeholder="Add all the useful context here…" required /></label>
        <fieldset><legend>Priority</legend><p>Choose the impact this issue is having.</p><div className="priority-options">{['LOW','MEDIUM','HIGH','URGENT'].map(p => <label key={p} className={form.priority === p ? 'selected' : ''}><input type="radio" name="priority" value={p} checked={form.priority === p} onChange={change} /><i className={`priority-dot ${p.toLowerCase()}`} /><span><strong>{p[0] + p.slice(1).toLowerCase()}</strong><small>{{LOW:'General question',MEDIUM:'Some work affected',HIGH:'Major impact',URGENT:'Work is blocked'}[p]}</small></span></label>)}</div></fieldset>
        <div className="form-actions"><Link className="button ghost" to="/tickets">Cancel</Link><button className="button primary" disabled={saving}>{saving ? 'Creating…' : <>Create ticket <ArrowRight size={17} /></>}</button></div>
      </form>
    </div>
  </div>
}
