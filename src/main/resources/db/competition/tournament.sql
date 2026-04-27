-- =============================================================================
-- BASE : competition.db
-- Fichier : tournament.sql
-- Description : Tournoi — entité racine de competition.db.
--               Toutes les autres tables en dépendent via FK ON DELETE CASCADE.
--
-- Connexions :
--   → club.db/club(id)               via club_id (pas de FK SQLite cross-db,
--                                     cohérence assurée par l'application)
--   → club.db/organizer_account(id)  via organizer_id (même remarque)
--   ← tournament_regulation(tournament_id)
--   ← tournament_policy(tournament_id)
--   ← tableau(tournament_id)
--   ← guest_participant(tournament_id)
--   ← poule(tournament_id)
--   ← ko_bracket(tournament_id)
--   ← classification_bracket(tournament_id)
--   ← app_state(current_tournament_id)  (référence douce, pas de FK)
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : tournament
-- Entité racine représentant un tournoi officiel FFTT.
-- =============================================================================

CREATE TABLE IF NOT EXISTS tournament (

  -- Identifiant interne UUID
  id TEXT PRIMARY KEY,

  -- Références cross-db (cohérence assurée par l'application)
  club_id      TEXT NOT NULL,  -- → club.db/club(id)
  organizer_id TEXT NOT NULL,  -- → club.db/organizer_account(id)

  -- Informations générales
  name TEXT NOT NULL,

  -- Lieu du tournoi (peut différer du siège du club)
  address1   TEXT,
  address2   TEXT,
  city       TEXT NOT NULL,
  department TEXT NOT NULL,  -- ex : "91"

  -- Niveau FFTT : DEPARTEMENTAL | REGIONAL | NATIONAL | INTERNATIONAL
  level TEXT NOT NULL,

  -- Phase du calendrier FFTT : PHASE_1 | PHASE_2 | HORS_PHASE
  phase TEXT NOT NULL,

  -- Dates du tournoi (format ISO-8601 : "YYYY-MM-DD")
  start_date TEXT NOT NULL,
  end_date   TEXT NOT NULL,

  -- Numéro d'homologation officiel FFTT (optionnel, attribué après validation)
  homologation_number TEXT,

  -- Statut du tournoi :
  --   DRAFT       → en cours de configuration
  --   PUBLISHED   → inscriptions ouvertes
  --   IN_PROGRESS → tournoi en cours
  --   COMPLETED   → terminé
  --   CANCELLED   → annulé
  status TEXT NOT NULL,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

-- Recherche des tournois d'un club
CREATE INDEX IF NOT EXISTS idx_tournament_club_id
ON tournament(club_id);

-- Recherche des tournois d'un organisateur
CREATE INDEX IF NOT EXISTS idx_tournament_organizer_id
ON tournament(organizer_id);

-- Filtrage par statut (ex : tous les tournois PUBLISHED)
CREATE INDEX IF NOT EXISTS idx_tournament_status
ON tournament(status);

-- Filtrage par club + statut (dashboard principal)
CREATE INDEX IF NOT EXISTS idx_tournament_club_status
ON tournament(club_id, status);

-- Tri chronologique par club (liste des tournois d'un club)
CREATE INDEX IF NOT EXISTS idx_tournament_club_start_date
ON tournament(club_id, start_date);