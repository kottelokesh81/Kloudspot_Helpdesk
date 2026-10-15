import { ArrowRight, Check, LockKeyhole, ShieldCheck, Sparkles } from 'lucide-react'
import { useState } from 'react'
import { useAuth } from '../AuthContext'
import Logo from '../components/Logo'

const demos = [
  { label: 'Super Admin', email: 'superadmin@tandem.test', tone: 'coral' },
  { label: 'Admin', email: 'admin@acme.test', tone: 'mint' },
  { label: 'Agent', email: 'agent@acme.test', tone: 'blue' },
  { label: 'Customer', email: 'customer@acme.test', tone: 'sand' },
]

export default function LoginPage() {
  const { login } = useAuth()
  const [email, setEmail] = useState('agent@acme.test')
  const [password, setPassword] = useState('Password123!')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(e) {
    e.preventDefault(); setError(''); setLoading(true)
    try { await login(email, password) } catch (err) { setError(err.message) } finally { setLoading(false) }
  }

  return <div className="login-page">
    <section className="login-story">
      <Logo light />
      <div className="story-content">
        <span className="eyebrow"><Sparkles size={14} /> Calm support, at scale</span>
        <h1>Every customer<br />conversation, <em>in sync.</em></h1>
        <p>One secure workspace for your team to resolve issues, share context, and keep every customer moving.</p>
        <div className="story-points">
          <span><Check size={15} /> Tenant-isolated by design</span>
          <span><Check size={15} /> Clear ownership and history</span>
        </div>
      </div>
      <div className="story-quote">“The best support feels like a partnership.”</div>
      <div className="orb orb-one" /><div className="orb orb-two" />
    </section>
    <section className="login-form-wrap">
      <form className="login-card" onSubmit={submit}>
        <div className="login-icon"><ShieldCheck size={26} /></div>
        <span className="overline">Welcome back</span>
        <h2>Sign in to your workspace</h2>
        <p className="muted">Use your organization credentials to continue.</p>
        {error && <div className="alert error">{error}</div>}
        <label>Email address<input type="email" value={email} onChange={e => setEmail(e.target.value)} required autoFocus /></label>
        <label>Password<div className="input-icon"><LockKeyhole size={16} /><input type="password" value={password} onChange={e => setPassword(e.target.value)} required /></div></label>
        <button className="button primary wide" disabled={loading}>{loading ? 'Signing in…' : <>Sign in <ArrowRight size={17} /></>}</button>
        <div className="demo-divider"><span>Demo accounts</span></div>
        <div className="demo-users">{demos.map(d => <button type="button" key={d.email} onClick={() => { setEmail(d.email); setPassword('Password123!') }}><i className={d.tone}>{d.label[0]}</i><span><strong>{d.label}</strong><small>{d.email}</small></span></button>)}</div>
        <small className="demo-password">All demo accounts use <strong>Password123!</strong></small>
      </form>
    </section>
  </div>
}

