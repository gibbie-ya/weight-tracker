import { useState } from 'react';
import { useLogs } from './hooks/useLogs';
import { getPhaseIndex, PHASE_META } from './data/phases';
import Header from './components/Header';
import WeekPicker from './components/WeekPicker';
import PhaseBanner from './components/PhaseBanner';
import SessionPicker from './components/SessionPicker';
import SessionLogger from './components/SessionLogger';
import ProgressView from './components/ProgressView';
import ScheduleView from './components/ScheduleView';
import PhasesView from './components/PhasesView';
import PrinciplesView from './components/PrinciplesView';
import SettingsView from './components/SettingsView';

const TABS = [
  { id: 'log',        label: 'Log',        icon: '📋' },
  { id: 'progress',   label: 'Progress',   icon: '📈' },
  { id: 'schedule',   label: 'Schedule',   icon: '📅' },
  { id: 'phases',     label: 'Phases',     icon: '🎯' },
  { id: 'principles', label: 'Principles', icon: '📖' },
];

export default function App() {
  const [tab, setTab] = useState('log');
  const [activeWeek, setActiveWeek] = useState(1);
  const [activeDay, setActiveDay] = useState(null);
  const [saved, setSaved] = useState(false);
  const [showSettings, setShowSettings] = useState(false);

  const { logs, getLog, setLog, sessionHasData, bestForExercise, exportLogs, importLogs } = useLogs();

  const phaseIndex = getPhaseIndex(activeWeek);
  const phaseMeta = PHASE_META[phaseIndex];

  function handleWeekChange(w) {
    setActiveWeek(w);
    setActiveDay(null);
    setSaved(false);
  }

  function handleSave() {
    setSaved(true);
  }

  if (showSettings) {
    return (
      <div style={{ background: '#0d0d0d', minHeight: '100dvh', fontFamily: 'system-ui, -apple-system, sans-serif', color: '#f0f0f0' }}>
        <Header onSettings={() => setShowSettings(false)} />
        <div style={{ paddingBottom: 80 }}>
          <SettingsView exportLogs={exportLogs} importLogs={importLogs} onClose={() => setShowSettings(false)} />
        </div>
      </div>
    );
  }

  return (
    <div style={{ background: '#0d0d0d', minHeight: '100dvh', fontFamily: 'system-ui, -apple-system, sans-serif', color: '#f0f0f0' }}>
      <Header onSettings={() => setShowSettings(true)} />

      <div style={{ paddingBottom: 80 }}>
        {tab === 'log' && (
          activeDay ? (
            <SessionLogger
              dayId={activeDay}
              week={activeWeek}
              phase={phaseIndex}
              phaseMeta={phaseMeta}
              getLog={getLog}
              setLog={setLog}
              onBack={() => { setActiveDay(null); setSaved(false); }}
              onSave={handleSave}
              saved={saved}
            />
          ) : (
            <>
              <WeekPicker
                activeWeek={activeWeek}
                onWeekChange={handleWeekChange}
                sessionHasData={sessionHasData}
              />
              <PhaseBanner phaseMeta={phaseMeta} />
              <SessionPicker
                week={activeWeek}
                activeDay={activeDay}
                onDaySelect={id => { setActiveDay(id); setSaved(false); }}
                sessionHasData={sessionHasData}
              />
            </>
          )
        )}

        {tab === 'progress' && <ProgressView bestForExercise={bestForExercise} />}
        {tab === 'schedule' && <ScheduleView />}
        {tab === 'phases' && <PhasesView />}
        {tab === 'principles' && <PrinciplesView />}
      </div>

      <div style={{
        position: 'fixed', bottom: 0, left: 0, right: 0,
        height: 60, background: '#111', borderTop: '1px solid #222',
        display: 'flex', zIndex: 200,
      }}>
        {TABS.map(t => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            style={{
              flex: 1, background: 'none', border: 'none', cursor: 'pointer',
              display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
              gap: 2, color: tab === t.id ? '#f0f0f0' : '#444',
              fontSize: 10, paddingBottom: 4,
            }}
          >
            <span style={{ fontSize: 18 }}>{t.icon}</span>
            <span>{t.label}</span>
          </button>
        ))}
      </div>
    </div>
  );
}
