import { useState } from 'react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ReferenceLine, ResponsiveContainer
} from 'recharts';
import { DAYS } from '../data/days';

export default function ProgressView({ bestForExercise }) {
  const [selectedDay, setSelectedDay] = useState('A');
  const [selectedEx, setSelectedEx] = useState(null);

  const day = DAYS.find(d => d.id === selectedDay);

  const records = selectedEx ? bestForExercise(selectedEx) : [];
  const byWeek = {};
  records.forEach(r => {
    if (!byWeek[r.week] || r.weight > byWeek[r.week]) byWeek[r.week] = r.weight;
  });
  const chartData = Object.entries(byWeek).map(([w, weight]) => ({ week: parseInt(w), weight }));

  const weeks = records.map(r => r.week);
  const weights = records.map(r => r.weight);
  const firstLog = weeks.length ? `W${Math.min(...weeks)}` : '—';
  const bestSet = weights.length ? `${Math.max(...weights)} kg` : '—';
  const weeksLogged = new Set(weeks).size;

  return (
    <div style={{ padding: '16px' }}>
      <div style={{ display: 'flex', gap: 8, marginBottom: 16, flexWrap: 'wrap' }}>
        {DAYS.map(d => (
          <button
            key={d.id}
            onClick={() => { setSelectedDay(d.id); setSelectedEx(null); }}
            style={{
              padding: '8px 16px', borderRadius: 20, minHeight: 44,
              border: selectedDay === d.id ? `2px solid ${d.accent}` : '1px solid #333',
              background: selectedDay === d.id ? d.accent + '22' : '#161616',
              color: selectedDay === d.id ? d.accent : '#aaa',
              fontSize: 13, fontWeight: selectedDay === d.id ? 700 : 400, cursor: 'pointer',
            }}
          >{d.label}</button>
        ))}
      </div>

      <div style={{ marginBottom: 16 }}>
        {day.exercises.map(ex => {
          const recs = bestForExercise(ex.id);
          const latest = recs.length ? recs[recs.length - 1] : null;
          return (
            <button
              key={ex.id}
              onClick={() => setSelectedEx(ex.id)}
              style={{
                width: '100%', background: selectedEx === ex.id ? '#1c1c1c' : '#161616',
                border: selectedEx === ex.id ? `1px solid ${day.accent}` : '1px solid #222',
                borderRadius: 8, padding: '10px 12px', cursor: 'pointer',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                marginBottom: 6, minHeight: 48,
              }}
            >
              <div style={{ textAlign: 'left' }}>
                <span style={{ color: day.accent, fontSize: 11, marginRight: 6 }}>{ex.id}</span>
                <span style={{ color: '#f0f0f0', fontSize: 13 }}>{ex.name}</span>
              </div>
              {latest && (
                <span style={{ color: '#aaa', fontSize: 12 }}>{latest.weight} kg</span>
              )}
            </button>
          );
        })}
      </div>

      {selectedEx && (
        <div>
          <div style={{ color: '#aaa', fontSize: 13, marginBottom: 12 }}>
            {day.exercises.find(e => e.id === selectedEx)?.name}
          </div>

          {chartData.length > 0 ? (
            <div style={{ background: '#161616', borderRadius: 10, padding: '16px 4px 8px', marginBottom: 16 }}>
              <ResponsiveContainer width="100%" height={180}>
                <LineChart data={chartData} margin={{ left: -10, right: 10 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#222" />
                  <XAxis dataKey="week" tickFormatter={w => `W${w}`} tick={{ fill: '#555', fontSize: 10 }} />
                  <YAxis tick={{ fill: '#555', fontSize: 10 }} />
                  <Tooltip
                    contentStyle={{ background: '#1c1c1c', border: '1px solid #333', borderRadius: 6 }}
                    labelFormatter={w => `Week ${w}`}
                    formatter={v => [`${v} kg`, 'Weight']}
                    labelStyle={{ color: '#aaa' }}
                    itemStyle={{ color: '#f0f0f0' }}
                  />
                  <ReferenceLine x={5} stroke="#4a9eff44" label={{ value: 'P2', fill: '#4a9eff', fontSize: 10 }} />
                  <ReferenceLine x={9} stroke="#22c55e44" label={{ value: 'P3', fill: '#22c55e', fontSize: 10 }} />
                  <Line type="monotone" dataKey="weight" stroke={day.accent} strokeWidth={2} dot={{ fill: day.accent, r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div style={{ color: '#444', fontSize: 13, textAlign: 'center', padding: '24px', marginBottom: 16 }}>
              No data logged yet
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 8, marginBottom: 16 }}>
            {[
              { label: 'First log', value: firstLog },
              { label: 'Best set', value: bestSet },
              { label: 'Weeks logged', value: weeksLogged || '—' },
            ].map(stat => (
              <div key={stat.label} style={{
                background: '#161616', borderRadius: 8, padding: '10px 8px', textAlign: 'center',
              }}>
                <div style={{ color: '#f0f0f0', fontSize: 14, fontWeight: 700 }}>{stat.value}</div>
                <div style={{ color: '#555', fontSize: 10 }}>{stat.label}</div>
              </div>
            ))}
          </div>

          {records.length > 0 && (
            <div style={{ background: '#161616', borderRadius: 10, overflow: 'hidden' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', padding: '8px 12px', borderBottom: '1px solid #222' }}>
                {['Week', 'Set', 'kg', 'Reps'].map(h => (
                  <div key={h} style={{ color: '#444', fontSize: 10 }}>{h}</div>
                ))}
              </div>
              {records.map((r, i) => (
                <div key={i} style={{
                  display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr',
                  padding: '7px 12px', borderBottom: '1px solid #1e1e1e',
                }}>
                  <div style={{ color: '#aaa', fontSize: 12 }}>W{r.week}</div>
                  <div style={{ color: '#aaa', fontSize: 12 }}>S{r.set}</div>
                  <div style={{ color: '#f0f0f0', fontSize: 12 }}>{r.weight}</div>
                  <div style={{ color: '#aaa', fontSize: 12 }}>{r.reps || '—'}</div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
