import { useState, useEffect } from 'react';

export function useLogs() {
  const [logs, setLogs] = useState(() => {
    try { return JSON.parse(localStorage.getItem('fat_loss_logs_v2')) || {}; }
    catch { return {}; }
  });

  useEffect(() => {
    try { localStorage.setItem('fat_loss_logs_v2', JSON.stringify(logs)); }
    catch {}
  }, [logs]);

  function sessionKey(week, dayId) { return `W${week}-${dayId}`; }

  function getLog(week, dayId, exId, setIdx, field) {
    return logs[sessionKey(week, dayId)]?.[exId]?.[setIdx]?.[field] ?? "";
  }

  function setLog(week, dayId, exId, setIdx, field, value) {
    const sk = sessionKey(week, dayId);
    setLogs(prev => {
      const n = { ...prev, [sk]: { ...prev[sk] } };
      if (!n[sk][exId]) n[sk][exId] = [];
      n[sk][exId] = [...n[sk][exId]];
      n[sk][exId][setIdx] = { ...(n[sk][exId][setIdx] || {}), [field]: value };
      return n;
    });
  }

  function sessionHasData(week, dayId) {
    return Object.values(logs[sessionKey(week, dayId)] || {})
      .some(sets => sets.some(s => s?.weight || s?.reps));
  }

  function bestForExercise(exId) {
    const dayId = exId[0];
    const records = [];
    for (let w = 1; w <= 12; w++) {
      (logs[sessionKey(w, dayId)]?.[exId] ?? []).forEach((s, si) => {
        const wt = parseFloat(s?.weight);
        if (!isNaN(wt) && wt > 0)
          records.push({ week: w, set: si + 1, weight: wt, reps: parseFloat(s?.reps) || 0 });
      });
    }
    return records;
  }

  function exportLogs() {
    const blob = new Blob([JSON.stringify(logs, null, 2)], { type: 'application/json' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'training-logs.json';
    a.click();
  }

  function importLogs(file) {
    const reader = new FileReader();
    reader.onload = e => {
      try { setLogs(JSON.parse(e.target.result)); }
      catch { alert('Invalid file.'); }
    };
    reader.readAsText(file);
  }

  return { logs, getLog, setLog, sessionHasData, bestForExercise, exportLogs, importLogs };
}
