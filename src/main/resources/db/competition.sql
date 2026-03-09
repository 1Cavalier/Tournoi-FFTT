PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS tournament (
  id TEXT PRIMARY KEY,

  club_id TEXT NOT NULL,
  organizer_id TEXT NOT NULL,     -- créateur initial

  name TEXT NOT NULL,
  level TEXT NOT NULL,
  phase TEXT NOT NULL,
  start_date TEXT NOT NULL,
  end_date   TEXT NOT NULL,
  status TEXT NOT NULL,

  max_tableaux_per_day INTEGER NOT NULL DEFAULT 2,
  max_total_tableaux   INTEGER NOT NULL DEFAULT 4,
  female_extra_rule    TEXT NOT NULL DEFAULT 'NONE',
  female_extra_code    TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tournament_club_id
ON tournament(club_id);

CREATE INDEX IF NOT EXISTS idx_tournament_organizer_id
ON tournament(organizer_id);

CREATE TABLE IF NOT EXISTS app_state (
  id INTEGER PRIMARY KEY CHECK (id = 1),
  current_tournament_id TEXT NULL
);

INSERT OR IGNORE INTO app_state(id, current_tournament_id) VALUES (1, NULL);

CREATE TABLE IF NOT EXISTS tableau (
  id            TEXT PRIMARY KEY,
  tournament_id TEXT NOT NULL,
  code          TEXT NOT NULL,
  label         TEXT NOT NULL,
  date          TEXT NOT NULL,
  prepaid_cents INTEGER NOT NULL,
  onsite_cents  INTEGER NOT NULL,
  capacity      INTEGER NOT NULL,
  created_at    TEXT NOT NULL,
  updated_at    TEXT NOT NULL,
  FOREIGN KEY (tournament_id) REFERENCES tournament(id),
  UNIQUE (tournament_id, code)
);