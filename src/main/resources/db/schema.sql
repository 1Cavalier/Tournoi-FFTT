PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS organizer_account (
  id            TEXT PRIMARY KEY,
  club_name     TEXT NOT NULL,
  email         TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  created_at    TEXT NOT NULL,
  updated_at    TEXT NOT NULL
);

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


CREATE TABLE IF NOT EXISTS tournament (
  id            TEXT PRIMARY KEY,
  organizer_id  TEXT NOT NULL,
  name          TEXT NOT NULL,
  level         TEXT NOT NULL,
  phase         INTEGER NOT NULL,
  start_date    TEXT,
  end_date      TEXT,
  status        TEXT NOT NULL CHECK (status IN ('DRAFT','OPEN','RUNNING','FINISHED')),
  created_at    TEXT NOT NULL,
  updated_at    TEXT NOT NULL,
  FOREIGN KEY (organizer_id) REFERENCES organizer_account(id)
);

CREATE TABLE IF NOT EXISTS app_state (
  id INTEGER PRIMARY KEY CHECK (id = 1),
  current_tournament_id TEXT NULL,
  FOREIGN KEY (current_tournament_id) REFERENCES tournament(id)
);

INSERT OR IGNORE INTO app_state(id, current_tournament_id) VALUES (1, NULL);

CREATE TABLE IF NOT EXISTS tableau (
  id           TEXT PRIMARY KEY,
  tournament_id TEXT NOT NULL,
  code         TEXT NOT NULL,
  label        TEXT NOT NULL,
  price_cents  INTEGER NOT NULL,
  capacity     INTEGER NOT NULL,
  created_at   TEXT NOT NULL,
  updated_at   TEXT NOT NULL,
  FOREIGN KEY (tournament_id) REFERENCES tournament(id),
  UNIQUE (tournament_id, code)
);

