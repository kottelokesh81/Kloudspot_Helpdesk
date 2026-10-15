import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './AuthContext'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import TicketListPage from './pages/TicketListPage'
import TicketPage from './pages/TicketPage'
import NewTicketPage from './pages/NewTicketPage'
import OrganizationsPage from './pages/OrganizationsPage'

function Protected({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/login" replace />
}

function TenantOnly({ children }) {
  const { user } = useAuth()
  return user?.role === 'SUPER_ADMIN' ? <Navigate to="/organizations" replace /> : children
}

function SuperAdminOnly({ children }) {
  const { user } = useAuth()
  return user?.role === 'SUPER_ADMIN' ? children : <Navigate to="/tickets" replace />
}

export default function App() {
  const { user } = useAuth()
  const home = user?.role === 'SUPER_ADMIN' ? '/organizations' : '/tickets'
  return <Routes>
    <Route path="/login" element={user ? <Navigate to={home} replace /> : <LoginPage />} />
    <Route path="/" element={<Protected><Layout /></Protected>}>
      <Route index element={<Navigate to={home} replace />} />
      <Route path="organizations" element={<SuperAdminOnly><OrganizationsPage /></SuperAdminOnly>} />
      <Route path="tickets" element={<TenantOnly><TicketListPage /></TenantOnly>} />
      <Route path="tickets/new" element={<TenantOnly><NewTicketPage /></TenantOnly>} />
      <Route path="tickets/:id" element={<TenantOnly><TicketPage /></TenantOnly>} />
    </Route>
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
}

