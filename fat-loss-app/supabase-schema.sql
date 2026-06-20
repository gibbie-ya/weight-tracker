-- Run this in your Supabase project: SQL Editor → New query → paste → Run

CREATE TABLE IF NOT EXISTS workout_logs (
  id            UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  user_id       UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
  session_key   TEXT NOT NULL,   -- e.g. "W1-A"
  exercise_id   TEXT NOT NULL,   -- e.g. "A1"
  set_index     INTEGER NOT NULL,
  weight        TEXT DEFAULT '',
  reps          TEXT DEFAULT '',
  created_at    TIMESTAMPTZ DEFAULT NOW(),
  updated_at    TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE(user_id, session_key, exercise_id, set_index)
);

-- Row-level security: users can only access their own rows
ALTER TABLE workout_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users manage own logs"
  ON workout_logs FOR ALL
  USING (auth.uid() = user_id)
  WITH CHECK (auth.uid() = user_id);

-- Auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER workout_logs_updated_at
  BEFORE UPDATE ON workout_logs
  FOR EACH ROW EXECUTE FUNCTION update_updated_at();
