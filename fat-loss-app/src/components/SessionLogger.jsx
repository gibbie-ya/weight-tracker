import { DAYS } from '../data/days';
import ExerciseRow from './ExerciseRow';

export default function SessionLogger({ dayId, week, phase, phaseMeta, getLog, setLog, onBack, onSave, saved }) {
  const day = DAYS.find(d => d.id === dayId);
  if (!day) return null;
  const finisher = day.phaseFinisher[phase];

  return (
    <div>
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        padding: '12px 16px', gap: 10,
      }}>
        <button
          onClick={onBack}
          style={{
            background: '#1c1c1c', border: '1px solid #333', borderRadius: 8,
            color: '#aaa', padding: '8px 14px', fontSize: 13, cursor: 'pointer',
            minHeight: 44,
          }}
        >← Back</button>
        <div style={{ flex: 1, textAlign: 'center' }}>
          <span style={{ color: day.accent, fontWeight: 700, fontSize: 14 }}>{day.label}</span>
          <span style={{ color: '#555', fontSize: 12 }}> · W{week} · {phaseMeta.label}</span>
        </div>
        <button
          onClick={onSave}
          style={{
            background: saved ? '#22c55e22' : day.accent + '22',
            border: `1px solid ${saved ? '#22c55e' : day.accent}`,
            borderRadius: 8,
            color: saved ? '#22c55e' : day.accent,
            padding: '8px 14px', fontSize: 13, cursor: 'pointer',
            minHeight: 44,
          }}
        >{saved ? '✓ Saved' : 'Save'}</button>
      </div>

      <div style={{ padding: '0 16px 16px' }}>
        <div style={{ color: '#555', fontSize: 11, marginBottom: 12 }}>
          {day.type} · {day.tag} · {phaseMeta.rpe} · Tempo {phaseMeta.tempo}
        </div>

        {day.exercises.map(ex => (
          <ExerciseRow
            key={ex.id}
            ex={ex}
            phase={phase}
            week={week}
            dayId={dayId}
            accent={day.accent}
            getLog={getLog}
            setLog={setLog}
          />
        ))}

        {finisher && (
          <div style={{
            marginTop: 16, padding: '14px', background: '#1c1c1c',
            borderRadius: 10, border: '1px solid #333',
          }}>
            <div style={{ color: '#f59e0b', fontSize: 13, fontWeight: 700, marginBottom: 8 }}>
              🔥 {finisher.label}
            </div>
            {finisher.items.map((item, i) => (
              <div key={i} style={{ color: '#f0f0f0', fontSize: 12, padding: '4px 0' }}>• {item}</div>
            ))}
            <div style={{ color: '#aaa', fontSize: 11, marginTop: 8, lineHeight: 1.4 }}>{finisher.rest}</div>
          </div>
        )}

        <div style={{
          marginTop: 16, padding: '12px 14px', background: '#161616',
          borderRadius: 10, border: '1px solid #1e1e1e',
        }}>
          <div style={{ color: '#444', fontSize: 11, lineHeight: 1.6 }}>
            <div>⏱ Rest: {phaseMeta.rest}</div>
            <div>🎯 Tempo: {phaseMeta.tempo} (down-pause-up)</div>
            <div>📈 Overload: +2.5-5 kg when all reps clean for 2 sessions</div>
          </div>
        </div>
      </div>
    </div>
  );
}
