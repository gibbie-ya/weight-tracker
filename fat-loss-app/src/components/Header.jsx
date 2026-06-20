export default function Header({ onSettings, syncing }) {
  return (
    <div style={{
      position: 'sticky', top: 0, zIndex: 100,
      background: '#0d0d0d', borderBottom: '1px solid #222',
      padding: '12px 16px',
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    }}>
      <div>
        <div style={{ fontSize: 18, fontWeight: 700, color: '#f0f0f0' }}>Fat Loss Training</div>
        <div style={{ fontSize: 11, color: '#555' }}>12-Week Programme</div>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
        {syncing && (
          <span style={{ color: '#4a9eff', fontSize: 10 }}>syncing…</span>
        )}
        <button
          onClick={onSettings}
          style={{
            background: 'none', border: '1px solid #333', borderRadius: 8,
            color: '#aaa', padding: '8px 10px', fontSize: 16, cursor: 'pointer',
            minHeight: 44, minWidth: 44, display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}
        >⚙️</button>
      </div>
    </div>
  )
}
