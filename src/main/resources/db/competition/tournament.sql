PRAGMA foreign_keys = ON;

-- =====================================================
-- TOURNAMENT
-- Bloc général du tournoi
-- =====================================================

CREATE TABLE IF NOT EXISTS tournament (
  id TEXT PRIMARY KEY,

  club_id      TEXT NOT NULL,
  organizer_id TEXT NOT NULL,

  name       TEXT NOT NULL,
  address1   TEXT,
  address2   TEXT,
  city       TEXT NOT NULL,
  department TEXT NOT NULL,

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