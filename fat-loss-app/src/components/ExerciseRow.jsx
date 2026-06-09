import { useState } from 'react';

export default function ExerciseRow({ ex, phase, week, dayId, accent, getLog, setLog }) {
  const [open, setOpen] = useState(false);
  const phaseData = ex.phases[phase];
  const hasLog = Array.from({ length: phaseData.sets }, (_, i) => i)
    .some(i => getLog(week, dayId, ex.id, i, 'weight') || getLog(week, dayId, ex.id, i, 'reps'));

  return (
    <div style={{
      background: '#161616',
      borderRadius: 10,
      border: '1px solid #222',
      overflow: 'hidden',
      marginBottom: 8,
    }}>
      <button
        onClick={() => setOpen(o => !o)}
        style={{
          width: '100%', background: 'none', border: 'none',
          padding: '12px 14px', cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          minHeight: 56,
        }}
      >
        <div style={{ textAlign: 'left', flex: 1 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
            <span style={{ color: accent, fontSize: 11, fontWeight: 700 }}>{ex.id}</span>
            <span style={{ color: '#f0f0f0', fontSize: 13, fontWeight: 600 }}>{ex.name}</span>
            {hasLog && (
              <span style={{
                background: '#22c55e22', color: '#22c55e', fontSize: 9,
                padding: '1px 5px', borderRadius: 8, border: '1px solid #22c55e33',
              }}>✓</span>
            )}
            {phaseData.dropSet && (
              <span style={{
                background: '#f59e0b22', color: '#f59e0b', fontSize: 9,
                padding: '1px 5px', borderRadius: 8, border: '1px solid #f59e0b33',
              }}>drop</span>
            )}
          </div>
          <div style={{ color: '#555', fontSize: 11, marginTop: 2 }}>
            {phaseData.sets} sets × {phaseData.reps}
          </div>
        </div>
        <span style={{ color: '#444', fontSize: 16, marginLeft: 8 }}>{open ? '▲' : '▼'}</span>
      </button>

      {open && (
        <div style={{ padding: '0 14px 14px' }}>
          <div style={{ color: '#aaa', fontSize: 11, marginBottom: 10, lineHeight: 1.4 }}>
            {phaseData.note}
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '32px 1fr 1fr', gap: 6, marginBottom: 4 }}>
            <div style={{ color: '#444', fontSize: 10, textAlign: 'center' }}>Set</div>
            <div style={{ color: '#444', fontSize: 10, textAlign: 'center' }}>kg</div>
            <div style={{ color: '#444', fontSize: 10, textAlign: 'center' }}>Reps</div>
          </div>

          {Array.from({ length: phaseData.sets }, (_, i) => (
            <div key={i} style={{ display: 'grid', gridTemplateColumns: '32px 1fr 1fr', gap: 6, marginBottom: 6 }}>
              <div style={{
                color: '#555', fontSize: 12, display: 'flex',
                alignItems: 'center', justifyContent: 'center',
                background: '#1c1c1c', borderRadius: 6, minHeight: 44,
              }}>{i + 1}</div>
              <input
                type="number"
                inputMode="decimal"
                placeholder="0"
                value={getLog(week, dayId, ex.id, i, 'weight')}
                onChange={e => setLog(week, dayId, ex.id, i, 'weight', e.target.value)}
                style={{
                  background: '#1c1c1c', border: '1px solid #333', borderRadius: 6,
                  color: '#f0f0f0', fontSize: 16, padding: '8px 10px',
                  textAlign: 'center', minHeight: 44, width: '100%', boxSizing: 'border-box',
                }}
              />
              <input
                type="number"
                inputMode="decimal"
                placeholder="0"
                value={getLog(week, dayId, ex.id, i, 'reps')}
                onChange={e => setLog(week, dayId, ex.id, i, 'reps', e.target.value)}
                style={{
                  background: '#1c1c1c', border: '1px solid #333', borderRadius: 6,
                  color: '#f0f0f0', fontSize: 16, padding: '8px 10px',
                  textAlign: 'center', minHeight: 44, width: '100%', boxSizing: 'border-box',
                }}
              />
            </div>
          ))}

          {phaseData.dropSet && (
            <div style={{
              marginTop: 8, padding: '8px 10px', background: '#f59e0b11',
              borderRadius: 6, borderLeft: '2px solid #f59e0b',
            }}>
              <span style={{ color: '#f59e0b', fontSize: 11 }}>
                Drop set: immediately reduce weight 20-30% and continue to failure.
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
