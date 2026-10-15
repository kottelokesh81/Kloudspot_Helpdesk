import { Bell, Building2, ChevronDown, LogOut, Plus, Ticket } from 'lucide-react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../AuthContext'
import { pretty } from '../api'
import Logo from './Logo'

export default function Layout() {
  const { user, logout } = useAuth()
  const isSuperAdmin = user.role === 'SUPER_ADMIN'
  const workspaceName = isSuperAdmin ? 'Tandem Platform' : user.organizationName
  return <div className="app-shell">
    <aside className="sidebar">
      <Logo light />
      <div className="workspace-label">Workspace</div>
      <div className="workspace"><span>{workspaceName?.[0]}</span><div><strong>{workspaceName}</strong><small>{isSuperAdmin ? 'Platform administration' : 'Organization'}</small></div>{!isSuperAdmin && <ChevronDown size={16} />}</div>
      <nav>
        {isSuperAdmin
          ? <NavLink to="/organizations"><Building2 size={18} /> Organizations</NavLink>
          : <NavLink to="/tickets"><Ticket size={18} /> Tickets</NavLink>}
      </nav>
      <div className="sidebar-footer"><div className="avatar">{user.name.split(' ').map(x => x[0]).join('').slice(0, 2)}</div><div className="user-meta"><strong>{user.name}</strong><small>{pretty(user.role)}</small></div><button className="icon-button dark" onClick={logout} title="Sign out"><LogOut size={17} /></button></div>
    </aside>
    <main className="main">
      <header className="topbar"><div className="mobile-logo"><Logo /></div><div className="topbar-actions"><button className="icon-button" aria-label="Notifications"><Bell size={19} /></button>{!isSuperAdmin && <Link className="button primary compact" to="/tickets/new"><Plus size={17} /> New ticket</Link>}</div></header>
      <div className="page"><Outlet /></div>
    </main>
  </div>
}

