export default function PhaseBanner({ phaseMeta }) {
  if (!phaseMeta) return null;
  return (
    <div style={{
      margin: '0 16px 12px',
      padding: '12px 14px',
      background: '#161616',
      borderRadius: 10,
      borderLeft: `3px solid ${phaseMeta.accent}`,
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
        <span style={{ color: phaseMeta.accent, fontSize: 13, fontWeight: 700 }}>{phaseMeta.label}</span>
        <span style={{ color: '#555', fontSize: 11 }}>·</span>
        <span style={{ color: '#aaa', fontSize: 11 }}>{phaseMeta.weeks}</span>
        <span style={{ color: '#555', fontSize: 11 }}>·</span>
        <span style={{ color: '#aaa', fontSize: 11 }}>{phaseMeta.rpe}</span>
      </div>
      <div style={{ color: '#f0f0f0', fontSize: 12, marginBottom: 4 }}>{phaseMeta.keyFocus}</div>
      <div style={{ color: '#555', fontSize: 11 }}>Rest: {phaseMeta.rest} · Tempo: {phaseMeta.tempo}</div>
    </div>
  );
}
