import { useState, useEffect, useRef, useCallback } from 'react'
import { supabase } from '../lib/supabase'

const LS_KEY = 'fat_loss_logs_v2'

function readLS() {
  try { return JSON.parse(localStorage.getItem(LS_KEY)) || {} } catch { return {} }
}
function writeLS(logs) {
  try { localStorage.setItem(LS_KEY, JSON.stringify(logs)) } catch {}
}

// Flatten nested log object → array of DB rows
function toRows(logs, userId) {
  const rows = []
  for (const [sk, exercises] of Object.entries(logs)) {
    for (const [exId, sets] of Object.entries(exercises)) {
      sets.forEach((s, idx) => {
        if (s && (s.weight || s.reps)) {
          rows.push({
            user_id: userId,
            session_key: sk,
            exercise_id: exId,
            set_index: idx,
            weight: s.weight ?? '',
            reps: s.reps ?? '',
          })
        }
      })
    }
  }
  return rows
}

// Inflate DB rows → nested log object
function fromRows(rows) {
  const logs = {}
  for (const r of rows) {
    if (!logs[r.session_key]) logs[r.session_key] = {}
    if (!logs[r.session_key][r.exercise_id]) logs[r.session_key][r.exercise_id] = []
    logs[r.session_key][r.exercise_id][r.set_index] = { weight: r.weight, reps: r.reps }
  }
  return logs
}

export function useLogs(user) {
  const [logs, setLogsState] = useState(readLS)
  const [syncing, setSyncing] = useState(false)
  const pendingUpserts = useRef([]) // queue of rows to upsert
  const flushTimer = useRef(null)

  // On login: pull all logs from Supabase and merge into state
  useEffect(() => {
    if (!user || !supabase) return
    setSyncing(true)
    supabase
      .from('workout_logs')
      .select('*')
      .eq('user_id', user.id)
      .then(({ data, error }) => {
        setSyncing(false)
        if (error || !data) return
        const remote = fromRows(data)
        setLogsState(prev => {
          // Remote wins — it's the source of truth
          const merged = { ...prev, ...remote }
          writeLS(merged)
          return merged
        })
      })
  }, [user?.id])

  // Flush pending upserts to Supabase (debounced 800ms)
  const scheduleFlush = useCallback(() => {
    if (!supabase || !user) return
    clearTimeout(flushTimer.current)
    flushTimer.current = setTimeout(async () => {
      const rows = [...pendingUpserts.current]
      pendingUpserts.current = []
      if (!rows.length) return
      await supabase.from('workout_logs').upsert(rows, {
        onConflict: 'user_id,session_key,exercise_id,set_index',
      })
    }, 800)
  }, [user?.id])

  function setLogs(updater) {
    setLogsState(prev => {
      const next = typeof updater === 'function' ? updater(prev) : updater
      writeLS(next)
      return next
    })
  }

  function sessionKey(week, dayId) { return `W${week}-${dayId}` }

  function getLog(week, dayId, exId, setIdx, field) {
    return logs[sessionKey(week, dayId)]?.[exId]?.[setIdx]?.[field] ?? ''
  }

  function setLog(week, dayId, exId, setIdx, field, value) {
    const sk = sessionKey(week, dayId)
    setLogs(prev => {
      const n = { ...prev, [sk]: { ...prev[sk] } }
      if (!n[sk][exId]) n[sk][exId] = []
      n[sk][exId] = [...n[sk][exId]]
      n[sk][exId][setIdx] = { ...(n[sk][exId][setIdx] || {}), [field]: value }

      // Queue upsert
      if (user && supabase) {
        const set = n[sk][exId][setIdx]
        pendingUpserts.current = pendingUpserts.current.filter(
          r => !(r.session_key === sk && r.exercise_id === exId && r.set_index === setIdx)
        )
        pendingUpserts.current.push({
          user_id: user.id, session_key: sk, exercise_id: exId,
          set_index: setIdx, weight: set.weight ?? '', reps: set.reps ?? '',
        })
        scheduleFlush()
      }

      return n
    })
  }

  function sessionHasData(week, dayId) {
    return Object.values(logs[sessionKey(week, dayId)] || {})
      .some(sets => sets.some(s => s?.weight || s?.reps))
  }

  function bestForExercise(exId) {
    const dayId = exId[0]
    const records = []
    for (let w = 1; w <= 12; w++) {
      ;(logs[sessionKey(w, dayId)]?.[exId] ?? []).forEach((s, si) => {
        const wt = parseFloat(s?.weight)
        if (!isNaN(wt) && wt > 0)
          records.push({ week: w, set: si + 1, weight: wt, reps: parseFloat(s?.reps) || 0 })
      })
    }
    return records
  }

  function exportLogs() {
    const blob = new Blob([JSON.stringify(logs, null, 2)], { type: 'application/json' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = 'training-logs.json'
    a.click()
  }

  function importLogs(file) {
    const reader = new FileReader()
    reader.onload = async e => {
      try {
        const imported = JSON.parse(e.target.result)
        setLogs(imported)
        // Push all imported rows to Supabase
        if (user && supabase) {
          const rows = toRows(imported, user.id)
          if (rows.length) {
            await supabase.from('workout_logs').upsert(rows, {
              onConflict: 'user_id,session_key,exercise_id,set_index',
            })
          }
        }
      } catch { alert('Invalid file.') }
    }
    reader.readAsText(file)
  }

  return { logs, getLog, setLog, sessionHasData, bestForExercise, exportLogs, importLogs, syncing }
}
