import { PHASE_META } from '../data/phases';
import { DAYS } from '../data/days';

export default function PhasesView() {
  return (
    <div style={{ padding: '16px' }}>
      {PHASE_META.map((phase, pi) => (
        <div key={pi} style={{
          background: '#161616', borderRadius: 12,
          border: '1px solid #222', marginBottom: 16,
          borderLeft: `3px solid ${phase.accent}`,
          overflow: 'hidden',
        }}>
          <div style={{ padding: '14px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
              <span style={{ color: phase.accent, fontWeight: 700, fontSize: 15 }}>{phase.label}</span>
              <span style={{ color: '#aaa', fontSize: 12 }}>{phase.weeks}</span>
              <span style={{
                marginLeft: 'auto', background: phase.accent + '22',
                color: phase.accent, fontSize: 10, padding: '2px 8px',
                borderRadius: 10, border: `1px solid ${phase.accent}44`,
              }}>{phase.rpe}</span>
            </div>
            <div style={{ color: '#f0f0f0', fontSize: 13, marginBottom: 4 }}>{phase.keyFocus}</div>
            <div style={{ color: '#555', fontSize: 11 }}>Rest: {phase.rest} · Tempo: {phase.tempo}</div>
          </div>

          {DAYS.map(day => (
            <div key={day.id} style={{ borderTop: '1px solid #1e1e1e', padding: '10px 14px' }}>
              <div style={{ color: day.accent, fontSize: 12, fontWeight: 700, marginBottom: 6 }}>
                {day.label} — {day.type}
              </div>
              {day.exercises.map(ex => {
                const p = ex.phases[pi];
                return (
                  <div key={ex.id} style={{
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    padding: '4px 0', borderBottom: '1px solid #1e1e1e',
                  }}>
                    <span style={{ color: '#aaa', fontSize: 11 }}>{ex.name}</span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ color: '#555', fontSize: 11 }}>{p.sets}×{p.reps}</span>
                      {p.dropSet && (
                        <span style={{ color: '#f59e0b', fontSize: 9, padding: '1px 4px', borderRadius: 4, border: '1px solid #f59e0b44' }}>drop</span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}
