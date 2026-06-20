import { getPhaseIndex, PHASE_META } from '../data/phases';

export default function WeekPicker({ activeWeek, onWeekChange, sessionHasData }) {
  return (
    <div style={{ padding: '12px 16px', overflowX: 'auto' }}>
      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
        {Array.from({ length: 12 }, (_, i) => i + 1).map(w => {
          const isActive = w === activeWeek;
          const accent = PHASE_META[getPhaseIndex(w)].accent;
          const hasData = sessionHasData ? ['A','B','C','D'].some(d => sessionHasData(w, d)) : false;
          return (
            <button
              key={w}
              onClick={() => onWeekChange(w)}
              style={{
                position: 'relative',
                minWidth: 44, minHeight: 36,
                padding: '6px 10px',
                borderRadius: 20,
                border: isActive ? `2px solid ${accent}` : '1px solid #333',
                background: isActive ? accent + '22' : '#161616',
                color: isActive ? accent : '#aaa',
                fontSize: 12, fontWeight: isActive ? 700 : 400,
                cursor: 'pointer',
              }}
            >
              W{w}
              {hasData && (
                <span style={{
                  position: 'absolute', top: 2, right: 2,
                  width: 6, height: 6, borderRadius: '50%',
                  background: '#22c55e',
                }} />
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
}
