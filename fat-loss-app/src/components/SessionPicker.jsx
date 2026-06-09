import { DAYS } from '../data/days';

export default function SessionPicker({ week, activeDay, onDaySelect, sessionHasData }) {
  return (
    <div style={{ padding: '0 16px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
      {DAYS.map(day => {
        const isActive = activeDay === day.id;
        const hasData = sessionHasData(week, day.id);
        return (
          <button
            key={day.id}
            onClick={() => onDaySelect(day.id)}
            style={{
              background: isActive ? day.accent + '22' : '#161616',
              border: isActive ? `2px solid ${day.accent}` : '1px solid #222',
              borderRadius: 12,
              padding: '14px 12px',
              cursor: 'pointer',
              textAlign: 'left',
              position: 'relative',
              minHeight: 90,
            }}
          >
            <div style={{ color: day.accent, fontSize: 16, fontWeight: 700, marginBottom: 2 }}>{day.label}</div>
            <div style={{ color: '#f0f0f0', fontSize: 12 }}>{day.type}</div>
            <div style={{ color: '#aaa', fontSize: 11 }}>{day.tag}</div>
            <div style={{ color: '#555', fontSize: 10, marginTop: 4 }}>{day.exercises.length} exercises</div>
            {hasData && (
              <span style={{
                position: 'absolute', top: 8, right: 8,
                background: '#22c55e22', color: '#22c55e',
                fontSize: 9, padding: '2px 6px', borderRadius: 10,
                border: '1px solid #22c55e44',
              }}>logged</span>
            )}
          </button>
        );
      })}
    </div>
  );
}
