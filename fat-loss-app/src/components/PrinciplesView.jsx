import { PRINCIPLES } from '../data/principles';

const ACCENTS = ['#e94560', '#4a9eff', '#a855f7', '#22c55e', '#f59e0b'];

export default function PrinciplesView() {
  return (
    <div style={{ padding: '16px' }}>
      {PRINCIPLES.map((p, i) => (
        <div key={i} style={{
          background: '#161616', borderRadius: 10,
          border: '1px solid #222', borderLeft: `3px solid ${ACCENTS[i]}`,
          padding: '14px', marginBottom: 12,
        }}>
          <div style={{ color: ACCENTS[i], fontSize: 13, fontWeight: 700, marginBottom: 6 }}>{p.title}</div>
          <div style={{ color: '#aaa', fontSize: 13, lineHeight: 1.5 }}>{p.body}</div>
        </div>
      ))}
    </div>
  );
}
