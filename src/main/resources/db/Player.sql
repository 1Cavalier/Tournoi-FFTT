-- =============================================================================
-- BASE : club.db
-- Fichier : Player.sql
-- Description : Joueurs licenciés FFTT et leurs qualifications officielles.
--
-- Ordre de création :
--   1. player
--   2. official_qualification  (FK → player)
--
-- Connexions vers d'autres bases :
--   ← competition.db/pool_slot.participant_id peut être une license_number
--   ← competition.db/pool_match.slot1_participant_id / slot2_participant_id
--   ← competition.db/ko_match.player1_id / player2_id
--   (cohérence assurée par PlayerParticipantResolver côté application)
--
-- Note : club_number référence club(club_number) sans FK SQLite
--   car la table club est dans la même base mais la relation est métier,
--   pas structurelle (un joueur peut avoir un club non encore importé).
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : player
-- Base locale des joueurs licenciés FFTT connus de l'application.
-- Alimentée manuellement ou par import (API FFTT à venir).
-- Sert de référence pour :
--   - désigner des JA / arbitres sur un tournoi
--   - retrouver un participant depuis son numéro de licence dans les poules/KO
-- =============================================================================

CREATE TABLE IF NOT EXISTS player (

  -- Identifiant interne UUID (clé primaire applicative)
  id TEXT PRIMARY KEY,

  -- Numéro de licence FFTT (ex : "08911132A"), unique et stable
  license_number TEXT NOT NULL,

  -- Identité civile
  first_name TEXT NOT NULL,
  last_name  TEXT NOT NULL,

  -- Genre : MALE | FEMALE
  gender TEXT NOT NULL,

  -- Catégorie d'âge FFTT
  -- Valeurs : BENJAMIN | MINIME | CADET | JUNIOR_1 | JUNIOR_2 |
  --           SENIOR | VETERAN_40 | VETERAN_50 | VETERAN_60 | VETERAN_70 | VETERAN_80
  age_category TEXT NOT NULL,

  -- Club d'appartenance (numéro FFTT, ex : "08911132")
  -- Dénomination stockée en dur pour éviter une jointure cross-db
  club_number TEXT NOT NULL,
  club_name   TEXT NOT NULL,

  -- Département du club (ex : "91" pour l'Essonne)
  departement_code TEXT NOT NULL,

  -- Points de classement FFTT
  -- phase1_start_points    : points officiels en début de phase 1 (détermine le tableau)
  -- phase2_official_points : points officiels de phase 2 (classement final)
  phase1_start_points    INTEGER NOT NULL DEFAULT 0,
  phase2_official_points INTEGER NOT NULL DEFAULT 0,

  -- Statut du certificat médical : VALIDE | NON_VALIDE | NON_PRESENT
  medical_certificate_status TEXT NOT NULL DEFAULT 'NON_PRESENT',

  -- Type de licence : COMPETITION | LOISIR | DIRIGEANT | ARBITRE | JEUNE
  license_type TEXT NOT NULL DEFAULT 'COMPETITION',

  -- Mutation en cours : 0 = non, 1 = oui
  -- Un joueur en mutation ne peut pas participer à certains tournois
  mutated INTEGER NOT NULL DEFAULT 0,

  -- Nationalité ISO-3166-1 alpha-2 (ex : "FR", "BE", "DE")
  nationality TEXT NOT NULL DEFAULT 'FR',

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

-- Accès rapide par numéro de licence (clé métier principale)
CREATE UNIQUE INDEX IF NOT EXISTS idx_player_license_number
ON player(license_number);

-- Recherche par nom (autocomplétion)
CREATE INDEX IF NOT EXISTS idx_player_last_name
ON player(last_name);

-- Filtrage par club (liste des joueurs d'un club)
CREATE INDEX IF NOT EXISTS idx_player_club_number
ON player(club_number);

-- Filtrage par département (tournois régionaux)
CREATE INDEX IF NOT EXISTS idx_player_departement_code
ON player(departement_code);


-- =============================================================================
-- TABLE : official_qualification
-- Qualifications officielles d'un joueur en tant qu'officiel de tournoi.
-- Un joueur peut avoir plusieurs qualifications de rôles différents,
-- mais un seul grade par rôle (contrainte UNIQUE sur player_id + role_type).
--
-- Règle des grades selon le rôle :
--   JUGE_ARBITRE → judge_referee_grade : JA1 | JA2 | JA3 | JAN | JAI
--   ARBITRE      → referee_grade       : CLUB | REGIONAL | NATIONAL | INTERNATIONAL | INTERNATIONAL_BLUE_BADGE
--   TECHNIQUE    → technical_grade     : IC | AF | EF | CQP | BPJEPS | DEJEPS | BEES1
--   (seule la colonne correspondant au rôle est renseignée, les deux autres sont NULL)
--
-- Connexions :
--   → player(id) ON DELETE CASCADE : supprimé si le joueur est supprimé
-- =============================================================================

CREATE TABLE IF NOT EXISTS official_qualification (

  id TEXT PRIMARY KEY,

  player_id TEXT NOT NULL,

  -- Type de rôle officiel : JUGE_ARBITRE | ARBITRE | TECHNIQUE
  role_type TEXT NOT NULL,

  -- Grade selon le rôle (une seule colonne renseignée par ligne)
  judge_referee_grade TEXT,  -- pour JUGE_ARBITRE
  referee_grade       TEXT,  -- pour ARBITRE
  technical_grade     TEXT,  -- pour TECHNIQUE

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE,

  -- Un joueur ne peut détenir qu'un seul grade pour chaque rôle
  UNIQUE (player_id, role_type)
);

-- Recherche des qualifications d'un joueur
CREATE INDEX IF NOT EXISTS idx_official_qualification_player_id
ON official_qualification(player_id);

-- Recherche par type de rôle (lister tous les JA disponibles)
CREATE INDEX IF NOT EXISTS idx_official_qualification_role_type
ON official_qualification(role_type);

-- Recherche des JA par grade (pour affecter le bon grade requis par le tournoi)
CREATE INDEX IF NOT EXISTS idx_official_qualification_ja_grade
ON official_qualification(judge_referee_grade)
WHERE role_type = 'JUGE_ARBITRE';

-- Recherche des arbitres par grade
CREATE INDEX IF NOT EXISTS idx_official_qualification_ref_grade
ON official_qualification(referee_grade)
WHERE role_type = 'ARBITRE';