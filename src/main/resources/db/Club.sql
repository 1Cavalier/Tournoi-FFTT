-- =============================================================================
-- BASE : club.db
-- Fichier : Club.sql
-- Description : Clubs FFTT, comptes organisateurs et accès associés.
--
-- Ordre de création (respecter les FK) :
--   1. club
--   2. organizer_account  (FK → club)
--   3. club_access        (FK → club)
--
-- Connexions vers d'autres bases :
--   ← competition.db/tournament référence club_id (pas de FK SQLite cross-db,
--     cohérence garantie par l'application)
--   ← club.db/player référence club_number (via la colonne club_number,
--     pas de FK directe car player stocke le numéro FFTT, pas l'UUID)
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : club
-- Référentiel des clubs de tennis de table.
-- Alimenté manuellement ou par import API FFTT.
-- Sert de référence pour rattacher organisateurs, joueurs et accès.
-- =============================================================================

CREATE TABLE IF NOT EXISTS club (

  -- Identifiant interne UUID généré côté application
  id TEXT PRIMARY KEY,

  -- Numéro officiel FFTT, ex : "08911132" (unique, utilisé comme clé métier)
  club_number TEXT NOT NULL,

  -- Dénomination officielle du club
  club_name   TEXT NOT NULL,

  -- Département FFTT (ex : "91")
  departement_code TEXT NOT NULL,

  -- Ville du club
  city     TEXT NOT NULL,

  -- Adresse postale (lignes 1 et 2, optionnelles)
  address1 TEXT,
  address2 TEXT,

  -- Coordonnées GPS pour affichage cartographique (optionnelles)
  latitude  REAL,
  longitude REAL,

  -- Contact principal du club (référent organisateur)
  contact_first_name     TEXT,
  contact_last_name      TEXT,
  official_contact_email TEXT,

  -- Chemin relatif vers le logo du club (optionnel)
  logo_path TEXT,

  -- Horodatages ISO-8601 gérés par l'application
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

-- Accès rapide par numéro FFTT (recherche principale)
CREATE UNIQUE INDEX IF NOT EXISTS idx_club_club_number
ON club(club_number);

-- Recherche par nom (autocomplétion)
CREATE INDEX IF NOT EXISTS idx_club_club_name
ON club(club_name);


-- =============================================================================
-- TABLE : organizer_account
-- Compte organisateur rattaché à un club.
-- Gère l'authentification email + OTP (One-Time Password).
--
-- Connexions :
--   → club(id) : l'organisateur appartient à un club
--   ← competition.db/tournament : organizer_id référence cet id
-- =============================================================================

CREATE TABLE IF NOT EXISTS organizer_account (

  id TEXT PRIMARY KEY,

  -- Club propriétaire du compte (suppression cascade non voulue ici :
  -- on ne supprime pas le compte si le club est modifié)
  club_id TEXT NOT NULL,

  -- Identité de l'organisateur
  first_name TEXT,
  last_name  TEXT,

  -- Email principal (identifiant de connexion)
  email         TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,

  -- Vérification de l'email :
  --   0 = non vérifié, 1 = vérifié
  email_verified              INTEGER NOT NULL DEFAULT 0,
  email_verification_code     TEXT,
  email_verification_expires_at TEXT,

  -- OTP de connexion (code à usage unique, TTL géré par l'application)
  login_otp_code       TEXT,
  login_otp_expires_at TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (club_id) REFERENCES club(id)
);

-- Recherche des comptes d'un club
CREATE INDEX IF NOT EXISTS idx_organizer_account_club_id
ON organizer_account(club_id);

-- Recherche par email (login)
CREATE INDEX IF NOT EXISTS idx_organizer_account_email
ON organizer_account(email);


-- =============================================================================
-- TABLE : club_access
-- Liste des accès identifiés liés à un club.
-- Permet de suivre qui a accès aux données du club (multi-organisateurs).
-- Distinct de organizer_account : un accès peut exister sans compte actif.
--
-- Connexions :
--   → club(id) ON DELETE CASCADE : supprimé si le club est supprimé
-- =============================================================================

CREATE TABLE IF NOT EXISTS club_access (

  id TEXT PRIMARY KEY,

  club_id TEXT NOT NULL,

  -- Email de la personne ayant accès
  email      TEXT NOT NULL,
  first_name TEXT,
  last_name  TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE CASCADE
);

-- Unicité : une adresse email ne peut avoir qu'un accès par club
CREATE UNIQUE INDEX IF NOT EXISTS idx_club_access_club_email
ON club_access(club_id, email);

-- Recherche de tous les accès d'un club
CREATE INDEX IF NOT EXISTS idx_club_access_club_id
ON club_access(club_id);