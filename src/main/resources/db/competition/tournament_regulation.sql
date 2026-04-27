-- =============================================================================
-- BASE : competition.db
-- Fichier : tournament_regulation.sql
-- Description : Informations réglementaires d'un tournoi (règlement officiel).
--               Relation 1-1 avec tournament.
--
-- Connexions :
--   → tournament(id) ON DELETE CASCADE
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : tournament_regulation
-- Contient toutes les informations nécessaires à la rédaction du règlement
-- officiel FFTT d'un tournoi (lieu, horaires, officiels, surface de jeu…).
-- Une seule ligne par tournament_id (clé primaire = FK).
-- =============================================================================

CREATE TABLE IF NOT EXISTS tournament_regulation (

  -- Même id que le tournoi (relation 1-1)
  tournament_id TEXT PRIMARY KEY,

  -- Contact organisateur (peut différer du compte organizer_account)
  organizer_contact_name TEXT,
  organizer_email        TEXT,
  organizer_phone        TEXT,

  -- Lieu précis de la compétition
  venue_name   TEXT,
  venue_street TEXT,
  venue_zip    TEXT,
  venue_city   TEXT,

  -- Nombre de tables disponibles
  number_of_tables INTEGER,

  -- Configuration de la salle de jeu :
  --   playing_area_preset : STANDARD | PERSONNALISE | INCONNU
  --   playing_area_info_text : description libre si PERSONNALISE
  --   playing_area_length_meters / width_meters : dimensions en mètres
  --   playing_area_compliant : 0 = non conforme FFTT, 1 = conforme
  playing_area_preset        TEXT,
  playing_area_info_text     TEXT,
  playing_area_length_meters INTEGER,
  playing_area_width_meters  INTEGER,
  playing_area_compliant     INTEGER,

  -- Balle utilisée (marque + type, ex : "DHS D40+")
  ball_brand_and_type   TEXT,

  -- Politique de fourniture des balles :
  --   CLUB_FOURNIT | JOUEUR_FOURNIT | PARTAGE
  ball_provision_policy TEXT,

  -- Horaires du tournoi (format "HH:MM")
  registration_open_time TEXT,   -- Ouverture des pointages
  registration_deadline  TEXT,   -- Clôture des inscriptions
  gym_open_time          TEXT,   -- Ouverture de la salle

  -- Exigences en officiels :
  --   required_judge_grade        : grade minimum du JA principal (ex : "JA3")
  --   recommended_judge_count     : nombre de JA recommandés
  --   recommended_referee_grade   : grade des arbitres (ex : "REGIONAL")
  --   recommended_referee_count   : nombre d'arbitres recommandés
  required_judge_grade        TEXT,
  recommended_judge_count     INTEGER,
  recommended_referee_grade   TEXT,
  recommended_referee_count   INTEGER,

  -- Liste des officiels désignés (JSON : [{id, name, role, grade}])
  assigned_officials_json TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE
);