import { SCHEDULE } from '../data/schedule';

export default function ScheduleView() {
  return (
    <div style={{ padding: '16px' }}>
      <div style={{ color: '#aaa', fontSize: 13, marginBottom: 16 }}>Weekly template</div>
      {SCHEDULE.map(s => (
        <div
          key={s.day}
          style={{
            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
            padding: '12px 14px', background: '#161616', borderRadius: 10,
            border: '1px solid #222', marginBottom: 8,
            opacity: s.type === 'rest' ? 0.4 : 1,
          }}
        >
          <div>
            <div style={{ color: '#f0f0f0', fontSize: 14, fontWeight: s.type !== 'rest' ? 600 : 400 }}>{s.day}</div>
            <div style={{ color: '#aaa', fontSize: 12 }}>{s.session}</div>
          </div>
          {s.type !== 'rest' && (
            <span style={{
              background: s.type === 'Upper' ? '#a855f722' : '#22c55e22',
              color: s.type === 'Upper' ? '#a855f7' : '#22c55e',
              fontSize: 10, padding: '3px 8px', borderRadius: 10,
              border: `1px solid ${s.type === 'Upper' ? '#a855f733' : '#22c55e33'}`,
            }}>{s.type}</span>
          )}
        </div>
      ))}
    </div>
  );
}
