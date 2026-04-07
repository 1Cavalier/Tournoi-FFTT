PRAGMA foreign_keys = ON;
 
-- =============================================================================
-- PLAYER
-- Base locale des joueurs licenciés FFTT connus de l'application.
-- Alimentée manuellement ou par import (API FFTT à venir).
-- Sert de référence pour désigner des JA / arbitres sur un tournoi.
-- =============================================================================
 
CREATE TABLE IF NOT EXISTS player (
  id TEXT PRIMARY KEY,
 
  -- Identité FFTT
  license_number TEXT NOT NULL,
  first_name     TEXT NOT NULL,
  last_name      TEXT NOT NULL,
 
  -- Genre (MALE / FEMALE)
  gender TEXT NOT NULL,
 
  -- Catégorie d'âge FFTT (ex : SENIOR, VETERAN_40, JUNIOR_1...)
  age_category TEXT NOT NULL,
 
  -- Club d'appartenance (numéro FFTT, ex : "08911132")
  club_number TEXT NOT NULL,
  club_name   TEXT NOT NULL,
 
  -- Département du club (ex : "91")
  departement_code TEXT NOT NULL,
 
  -- Points de classement
  phase1_start_points    INTEGER NOT NULL DEFAULT 0,
  phase2_official_points INTEGER NOT NULL DEFAULT 0,
 
  -- Certificat médical (VALIDE / NON_VALIDE / NON_PRESENT)
  medical_certificate_status TEXT NOT NULL DEFAULT 'NON_PRESENT',
 
  -- Type de licence (COMPETITION / LOISIR / DIRIGEANT / ...)
  license_type TEXT NOT NULL DEFAULT 'COMPETITION',
 
  -- Mutation en cours (0 = non, 1 = oui)
  mutated INTEGER NOT NULL DEFAULT 0,
 
  -- Nationalité ISO-2 (ex : "FR", "BE")
  nationality TEXT NOT NULL DEFAULT 'FR',
 
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);
 
CREATE UNIQUE INDEX IF NOT EXISTS idx_player_license_number
ON player(license_number);
 
CREATE INDEX IF NOT EXISTS idx_player_last_name
ON player(last_name);
 
CREATE INDEX IF NOT EXISTS idx_player_club_number
ON player(club_number);
 
CREATE INDEX IF NOT EXISTS idx_player_departement_code
ON player(departement_code);
 
 
-- =============================================================================
-- OFFICIAL_QUALIFICATION
-- Qualifications officielles d'un joueur (JA, arbitre, technique).
-- Un joueur peut avoir plusieurs qualifications de rôles différents.
-- =============================================================================
 
CREATE TABLE IF NOT EXISTS official_qualification (
  id TEXT PRIMARY KEY,
 
  player_id TEXT NOT NULL,
 
  -- Rôle (JUGE_ARBITRE / ARBITRE / TECHNIQUE)
  role_type TEXT NOT NULL,
 
  -- Grade selon le rôle :
  --   JUGE_ARBITRE -> judge_referee_grade (JA1/JA2/JA3/JAN/JAI)
  --   ARBITRE      -> referee_grade (CLUB/REGIONAL/NATIONAL/INTERNATIONAL/INTERNATIONAL_BLUE_BADGE)
  --   TECHNIQUE    -> technical_grade (IC/AF/EF/CQP/BPJEPS/DEJEPS/BEES1)
  -- Un seul des trois est renseigné selon le rôle.
  judge_referee_grade TEXT,
  referee_grade       TEXT,
  technical_grade     TEXT,
 
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
 
  FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE,
 
  -- Un joueur ne peut avoir qu'une seule qualification par rôle
  UNIQUE (player_id, role_type)
);
 
CREATE INDEX IF NOT EXISTS idx_official_qualification_player_id
ON official_qualification(player_id);
 
CREATE INDEX IF NOT EXISTS idx_official_qualification_role_type
ON official_qualification(role_type);
 
-- Index utile pour la recherche des JA disponibles par grade
CREATE INDEX IF NOT EXISTS idx_official_qualification_ja_grade
ON official_qualification(judge_referee_grade)
WHERE role_type = 'JUGE_ARBITRE';
 
-- Index utile pour la recherche des arbitres par grade
CREATE INDEX IF NOT EXISTS idx_official_qualification_ref_grade
ON official_qualification(referee_grade)
WHERE role_type = 'ARBITRE';