PRAGMA foreign_keys = ON;

-- =========================
-- ORGANIZER ACCOUNT
-- =========================
CREATE TABLE IF NOT EXISTS organizer_account (
  id            TEXT PRIMARY KEY,
  club_name     TEXT NOT NULL,
  email         TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  created_at    TEXT NOT NULL,
  updated_at    TEXT NOT NULL
);

-- =========================
-- CLUB PROFILE
-- =========================
CREATE TABLE IF NOT EXISTS club_profile (
  organizer_id         TEXT PRIMARY KEY,
  club_number          TEXT,
  club_name            TEXT,
  departement_code     TEXT,
  city                 TEXT,
  address1             TEXT,
  address2             TEXT,
  latitude             REAL,
  longitude            REAL,
  contact_first_name   TEXT,
  contact_last_name    TEXT,
  logo_path            TEXT,
  updated_at           TEXT NOT NULL,
  FOREIGN KEY (organizer_id) REFERENCES organizer_account(id)
);

-- =========================
-- TOURNAMENT
-- =========================
CREATE TABLE IF NOT EXISTS tournament (
  id TEXT PRIMARY KEY,
  organizer_id TEXT NOT NULL,
  name TEXT NOT NULL,
  level TEXT NOT NULL,

  -- IMPORTANT: RankingPhase est stocké en String (ex: "PHASE_2")
  phase TEXT NOT NULL,

  start_date TEXT NOT NULL,
  end_date   TEXT NOT NULL,

  status TEXT NOT NULL,

  max_tableaux_per_day INTEGER NOT NULL DEFAULT 2,
  max_total_tableaux   INTEGER NOT NULL DEFAULT 4,

  female_extra_rule    TEXT NOT NULL DEFAULT 'NONE',
  female_extra_code    TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (organizer_id) REFERENCES organizer_account(id)
);

-- =========================
-- APP STATE
-- =========================
CREATE TABLE IF NOT EXISTS app_state (
  id INTEGER PRIMARY KEY CHECK (id = 1),
  current_tournament_id TEXT NULL,
  FOREIGN KEY (current_tournament_id) REFERENCES tournament(id)
);

INSERT OR IGNORE INTO app_state(id, current_tournament_id) VALUES (1, NULL);

-- =========================
-- TABLEAU
-- =========================
CREATE TABLE IF NOT EXISTS tableau (
  id            TEXT PRIMARY KEY,
  tournament_id TEXT NOT NULL,

  code          TEXT NOT NULL,
  label         TEXT NOT NULL,

  price_cents   INTEGER NOT NULL,
  capacity      INTEGER NOT NULL,

  created_at    TEXT NOT NULL,
  updated_at    TEXT NOT NULL,

  FOREIGN KEY (tournament_id) REFERENCES tournament(id),
  UNIQUE (tournament_id, code)
);