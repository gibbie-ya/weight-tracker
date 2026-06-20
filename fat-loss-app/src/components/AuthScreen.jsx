import { useState } from 'react'

export default function AuthScreen({ signIn, signUp }) {
  const [mode, setMode] = useState('signin') // 'signin' | 'signup'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)

    const err = mode === 'signin'
      ? await signIn(email, password)
      : await signUp(email, password)

    setLoading(false)
    if (err) {
      setError(err.message)
    } else if (mode === 'signup') {
      setSuccess('Check your email to confirm your account, then sign in.')
      setMode('signin')
    }
  }

  return (
    <div style={{
      minHeight: '100dvh', background: '#0d0d0d',
      display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center',
      padding: '24px 16px',
      fontFamily: 'system-ui, -apple-system, sans-serif',
    }}>
      <div style={{ marginBottom: 32, textAlign: 'center' }}>
        <div style={{ fontSize: 36, marginBottom: 8 }}>🏋️</div>
        <div style={{ fontSize: 22, fontWeight: 700, color: '#f0f0f0' }}>Fat Loss Training</div>
        <div style={{ fontSize: 13, color: '#555', marginTop: 4 }}>12-Week Programme</div>
      </div>

      <div style={{
        width: '100%', maxWidth: 360,
        background: '#161616', borderRadius: 14,
        border: '1px solid #222', padding: '24px 20px',
      }}>
        <div style={{ display: 'flex', marginBottom: 24, background: '#1c1c1c', borderRadius: 8, padding: 3 }}>
          {['signin', 'signup'].map(m => (
            <button
              key={m}
              onClick={() => { setMode(m); setError(''); setSuccess('') }}
              style={{
                flex: 1, padding: '8px', borderRadius: 6, border: 'none',
                background: mode === m ? '#e94560' : 'none',
                color: mode === m ? '#fff' : '#555',
                fontSize: 13, fontWeight: mode === m ? 700 : 400,
                cursor: 'pointer', minHeight: 36,
              }}
            >{m === 'signin' ? 'Sign in' : 'Sign up'}</button>
          ))}
        </div>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 12 }}>
            <label style={{ display: 'block', color: '#aaa', fontSize: 12, marginBottom: 6 }}>Email</label>
            <input
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              autoComplete="email"
              style={{
                width: '100%', boxSizing: 'border-box',
                background: '#1c1c1c', border: '1px solid #333',
                borderRadius: 8, color: '#f0f0f0', fontSize: 16,
                padding: '12px 14px', minHeight: 48,
              }}
            />
          </div>
          <div style={{ marginBottom: 20 }}>
            <label style={{ display: 'block', color: '#aaa', fontSize: 12, marginBottom: 6 }}>Password</label>
            <input
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              autoComplete={mode === 'signin' ? 'current-password' : 'new-password'}
              minLength={6}
              style={{
                width: '100%', boxSizing: 'border-box',
                background: '#1c1c1c', border: '1px solid #333',
                borderRadius: 8, color: '#f0f0f0', fontSize: 16,
                padding: '12px 14px', minHeight: 48,
              }}
            />
          </div>

          {error && (
            <div style={{
              marginBottom: 14, padding: '10px 12px',
              background: '#e9456022', border: '1px solid #e9456044',
              borderRadius: 8, color: '#e94560', fontSize: 13,
            }}>{error}</div>
          )}
          {success && (
            <div style={{
              marginBottom: 14, padding: '10px 12px',
              background: '#22c55e22', border: '1px solid #22c55e44',
              borderRadius: 8, color: '#22c55e', fontSize: 13,
            }}>{success}</div>
          )}

          <button
            type="submit"
            disabled={loading}
            style={{
              width: '100%', minHeight: 50,
              background: loading ? '#333' : '#e94560',
              border: 'none', borderRadius: 10,
              color: '#fff', fontSize: 15, fontWeight: 700,
              cursor: loading ? 'default' : 'pointer',
            }}
          >{loading ? '…' : mode === 'signin' ? 'Sign in' : 'Create account'}</button>
        </form>
      </div>

      <div style={{ color: '#333', fontSize: 11, marginTop: 24, textAlign: 'center' }}>
        Your logs are private and synced to your account.
      </div>
    </div>
  )
}
