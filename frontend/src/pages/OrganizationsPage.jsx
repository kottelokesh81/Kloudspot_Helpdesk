import { Building2, KeyRound, Plus, ShieldCheck, Ticket, UsersRound, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { api, shortDate } from '../api'

const emptyForm = { organizationName: '', tenantKey: '', adminName: '', adminEmail: '', adminPassword: '' }

function slugify(value) {
  return value.toLowerCase().trim().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '').slice(0, 30)
}

export default function OrganizationsPage() {
  const [organizations, setOrganizations] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [keyTouched, setKeyTouched] = useState(false)
  const [saving, setSaving] = useState(false)
  const [formError, setFormError] = useState('')
  const [success, setSuccess] = useState('')

  async function load() {
    setLoadError('')
    try { setOrganizations(await api('/api/organizations')) }
    catch (err) { setLoadError(err.message) }
    finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  function update(field, value) {
    setForm(current => ({
      ...current,
      [field]: value,
      ...(field === 'organizationName' && !keyTouched ? { tenantKey: slugify(value) } : {}),
    }))
  }

  function close() {
    if (saving) return
    setShowCreate(false)
    setForm(emptyForm)
    setKeyTouched(false)
    setFormError('')
  }

  async function submit(event) {
    event.preventDefault()
    setFormError('')
    setSaving(true)
    try {
      const created = await api('/api/organizations', { method: 'POST', body: JSON.stringify(form) })
      setOrganizations(current => [created, ...current])
      setSuccess(`${created.name} is ready. ${created.adminEmail} can sign in now.`)
      setShowCreate(false)
      setForm(emptyForm)
      setKeyTouched(false)
    } catch (err) {
      setFormError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const users = organizations.reduce((total, organization) => total + organization.userCount, 0)
  const tickets = organizations.reduce((total, organization) => total + organization.ticketCount, 0)

  return <>
    <div className="page-heading">
      <div><span className="overline">Platform administration</span><h1>Organizations</h1><p>Create and oversee isolated customer workspaces.</p></div>
      <button className="button primary" onClick={() => { setSuccess(''); setShowCreate(true) }}><Plus size={17} /> Create organization</button>
    </div>

    {success && <div className="alert success organization-alert"><ShieldCheck size={17} /> {success}</div>}
    {loadError && <div className="alert error organization-alert">{loadError}</div>}

    <div className="organization-metrics">
      <div className="metric-card"><div className="metric-icon coral"><Building2 size={19} /></div><div><span>Organizations</span><strong>{organizations.length}</strong></div><small>Active tenant workspaces</small></div>
      <div className="metric-card"><div className="metric-icon green"><UsersRound size={19} /></div><div><span>Users</span><strong>{users}</strong></div><small>Across all organizations</small></div>
      <div className="metric-card"><div className="metric-icon blue"><Ticket size={19} /></div><div><span>Tickets</span><strong>{tickets}</strong></div><small>Tenant-isolated records</small></div>
    </div>

    <section className="panel organizations-panel">
      <div className="section-heading"><div><Building2 size={17} /><h2>Tenant directory</h2></div><span>{organizations.length} total</span></div>
      {loading ? <div className="loading-list"><i /><i /><i /></div> : organizations.length === 0 ? <div className="empty"><div className="empty-icon"><Building2 /></div><h3>No organizations yet</h3><p>Create the first tenant workspace to get started.</p></div> :
        <div className="organization-table-wrap"><table className="organization-table"><thead><tr><th>Organization</th><th>Tenant key</th><th>Initial admin</th><th>Users</th><th>Tickets</th><th>Created</th></tr></thead><tbody>
          {organizations.map(organization => <tr key={organization.id}>
            <td><span className="organization-mark">{organization.name[0]}</span><strong>{organization.name}</strong></td>
            <td><code>{organization.id}</code></td>
            <td><strong>{organization.adminName || '—'}</strong><small>{organization.adminEmail || 'No admin assigned'}</small></td>
            <td>{organization.userCount}</td><td>{organization.ticketCount}</td><td>{shortDate(organization.createdAt)}</td>
          </tr>)}
        </tbody></table></div>}
    </section>

    {showCreate && <div className="modal-backdrop" role="presentation" onMouseDown={event => event.target === event.currentTarget && close()}>
      <section className="create-organization-modal" role="dialog" aria-modal="true" aria-labelledby="create-organization-title">
        <div className="modal-heading"><div className="big-icon"><Building2 size={26} /></div><div><span className="overline">New tenant</span><h2 id="create-organization-title">Create organization</h2><p>Set up an isolated workspace and its first administrator.</p></div><button type="button" className="icon-button" onClick={close} aria-label="Close"><X size={18} /></button></div>
        <form onSubmit={submit}>
          {formError && <div className="alert error">{formError}</div>}
          <div className="field-grid">
            <label>Organization name<input value={form.organizationName} onChange={event => update('organizationName', event.target.value)} placeholder="Northstar Labs" maxLength="120" required autoFocus /></label>
            <label>Tenant key<div className="input-icon"><KeyRound size={16} /><input value={form.tenantKey} onChange={event => { setKeyTouched(true); update('tenantKey', event.target.value.toLowerCase()) }} placeholder="northstar-labs" pattern="[a-z0-9][a-z0-9-]{2,29}" maxLength="30" required /></div><small>3–30 lowercase letters, numbers, and hyphens. Cannot be changed later.</small></label>
          </div>
          <div className="form-section-title"><ShieldCheck size={16} /><div><strong>Initial organization admin</strong><span>This person can sign in immediately. No email will be sent.</span></div></div>
          <div className="field-grid">
            <label>Full name<input value={form.adminName} onChange={event => update('adminName', event.target.value)} placeholder="Avery Morgan" maxLength="100" required /></label>
            <label>Email address<input type="email" value={form.adminEmail} onChange={event => update('adminEmail', event.target.value)} placeholder="admin@northstar.test" required /></label>
          </div>
          <label>Temporary password<input type="password" value={form.adminPassword} onChange={event => update('adminPassword', event.target.value)} minLength="8" maxLength="100" autoComplete="new-password" required /><small>Share this securely with the administrator. It is stored only as a password hash.</small></label>
          <div className="form-actions"><button type="button" className="button ghost" onClick={close}>Cancel</button><button className="button primary" disabled={saving}>{saving ? 'Creating…' : 'Create organization'}</button></div>
        </form>
      </section>
    </div>}
  </>
}
