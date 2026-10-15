import { Inbox } from 'lucide-react'

export default function EmptyState({ title = 'No tickets found', copy = 'Try changing your filters or create a new ticket.' }) {
  return <div className="empty"><div className="empty-icon"><Inbox size={25} /></div><h3>{title}</h3><p>{copy}</p></div>
}
