export default function Logo({ light = false }) {
  return <div className={`logo ${light ? 'logo-light' : ''}`} aria-label="Tandem Helpdesk">
    <span className="logo-mark"><i /><i /></span>
    <span>Tandem</span>
  </div>
}

