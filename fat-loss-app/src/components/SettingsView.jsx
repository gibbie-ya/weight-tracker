export default function SettingsView({ exportLogs, importLogs, onClose }) {
  return (
    <div style={{ padding: '16px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 20 }}>
        <div style={{ color: '#f0f0f0', fontSize: 16, fontWeight: 700 }}>Settings</div>
        <button
          onClick={onClose}
          style={{
            background: '#1c1c1c', border: '1px solid #333', borderRadius: 8,
            color: '#aaa', padding: '8px 14px', fontSize: 13, cursor: 'pointer', minHeight: 44,
          }}
        >✕ Close</button>
      </div>

      <div style={{ background: '#161616', borderRadius: 10, border: '1px solid #222', padding: '16px', marginBottom: 16 }}>
        <div style={{ color: '#aaa', fontSize: 13, fontWeight: 600, marginBottom: 12 }}>Export / Import</div>

        <button
          onClick={exportLogs}
          style={{
            width: '100%', background: '#22c55e22', border: '1px solid #22c55e44',
            borderRadius: 8, color: '#22c55e', padding: '12px', fontSize: 14,
            cursor: 'pointer', minHeight: 48, marginBottom: 12,
          }}
        >⬇ Export training-logs.json</button>

        <div style={{ color: '#555', fontSize: 11, marginBottom: 8 }}>
          ⚠ Importing will overwrite your current logs.
        </div>
        <label style={{
          display: 'block', width: '100%', background: '#4a9eff22',
          border: '1px solid #4a9eff44', borderRadius: 8, color: '#4a9eff',
          padding: '12px', fontSize: 14, cursor: 'pointer', minHeight: 48,
          textAlign: 'center', boxSizing: 'border-box',
        }}>
          ⬆ Import logs
          <input
            type="file"
            accept=".json"
            style={{ display: 'none' }}
            onChange={e => { if (e.target.files[0]) importLogs(e.target.files[0]); }}
          />
        </label>
      </div>

      <div style={{ background: '#161616', borderRadius: 10, border: '1px solid #222', padding: '14px' }}>
        <div style={{ color: '#aaa', fontSize: 12, lineHeight: 1.6 }}>
          <div style={{ color: '#f0f0f0', fontSize: 13, fontWeight: 600, marginBottom: 6 }}>About</div>
          <div>Fat Loss Training — 12-Week Programme</div>
          <div style={{ color: '#444', marginTop: 4 }}>All data stored locally on your device. No account required.</div>
        </div>
      </div>
    </div>
  );
}
