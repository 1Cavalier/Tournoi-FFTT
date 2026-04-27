-- =============================================================================
-- BASE : competition.db
-- Fichier : tournament.sql
-- =============================================================================

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS tournament (

  id TEXT PRIMARY KEY,

  club_id      TEXT NOT NULL,
  organizer_id TEXT NOT NULL,

  name TEXT NOT NULL,
  address1   TEXT,
  address2   TEXT,
  city       TEXT NOT NULL,
  department TEXT NOT NULL,

  -- DEPARTEMENTAL | REGIONAL | NATIONAL | INTERNATIONAL
  level TEXT NOT NULL,

  -- PHASE_1 | PHASE_2 | HORS_PHASE
  phase TEXT NOT NULL,

  start_date TEXT NOT NULL,
  end_date   TEXT NOT NULL,

  homologation_number TEXT,

  -- DRAFT | PUBLISHED | IN_PROGRESS | COMPLETED | CANCELLED
  status TEXT NOT NULL,

  -- Algorithme de tirage des poules pour TOUS les tableaux du tournoi
  -- SNAKE (serpent FFTT, défaut) | RANDOM
  draw_algorithm_type TEXT NOT NULL DEFAULT 'SNAKE',

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