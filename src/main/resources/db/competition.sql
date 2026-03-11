PRAGMA foreign_keys = ON;

-- =========================
-- TOURNAMENT
-- Tournoi créé dès la première sauvegarde du bloc général.
-- Le tournoi existe en DRAFT puis est enrichi ensuite.
-- =========================
CREATE TABLE IF NOT EXISTS tournament (
  id TEXT PRIMARY KEY,

  club_id      TEXT NOT NULL,
  organizer_id TEXT NOT NULL,

  name        TEXT NOT NULL,
  address1    TEXT,
  address2    TEXT,
  city        TEXT NOT NULL,
  department  TEXT NOT NULL,

  level TEXT NOT NULL,
  phase TEXT NOT NULL,

  start_date TEXT NOT NULL,
  end_date   TEXT NOT NULL,

  homologation_number TEXT,
  status TEXT NOT NULL,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tournament_club_id
ON tournament(club_id);

CREATE INDEX IF NOT EXISTS idx_tournament_organizer_id
ON tournament(organizer_id);

CREATE INDEX IF NOT EXISTS idx_tournament_status
ON tournament(status);

CREATE INDEX IF NOT EXISTS idx_tournament_club_status
ON tournament(club_id, status);

CREATE INDEX IF NOT EXISTS idx_tournament_club_start_date
ON tournament(club_id, start_date);

-- =========================
-- TABLEAU
-- Tableaux rattachés à un tournoi.
-- Recréés plus tard avec plus de règles métier si besoin.
-- =========================
CREATE TABLE IF NOT EXISTS tableau (
  id TEXT PRIMARY KEY,

  tournament_id TEXT NOT NULL,
  code          TEXT NOT NULL,
  label         TEXT NOT NULL,
  date          TEXT NOT NULL,

  prepaid_cents INTEGER NOT NULL,
  onsite_cents  INTEGER NOT NULL,
  capacity      INTEGER NOT NULL,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE,
  UNIQUE (tournament_id, code)
);

CREATE INDEX IF NOT EXISTS idx_tableau_tournament_id
ON tableau(tournament_id);

CREATE INDEX IF NOT EXISTS idx_tableau_tournament_date
ON tableau(tournament_id, date);

-- =========================
-- APP STATE
-- Etat local simple de l'application.
-- Optionnel mais utile pour retrouver un tournoi en cours d'édition.
-- =========================
CREATE TABLE IF NOT EXISTS app_state (
  id INTEGER PRIMARY KEY CHECK (id = 1),
  current_tournament_id TEXT NULL
);

INSERT OR IGNORE INTO app_state(id, current_tournament_id)
VALUES (1, NULL);