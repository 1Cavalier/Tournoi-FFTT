PRAGMA foreign_keys = ON;

-- =========================
-- CLUB
-- Base locale de clubs de référence.
-- Un club peut être rattaché à plusieurs comptes organisateurs.
-- =========================
CREATE TABLE IF NOT EXISTS club (
  id TEXT PRIMARY KEY,

  club_number TEXT NOT NULL,
  club_name   TEXT NOT NULL,

  departement_code TEXT NOT NULL,
  city             TEXT NOT NULL,
  address1         TEXT,
  address2         TEXT,

  latitude  REAL,
  longitude REAL,

  contact_first_name     TEXT,
  contact_last_name      TEXT,
  official_contact_email TEXT,

  logo_path TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_club_club_number
ON club(club_number);

CREATE INDEX IF NOT EXISTS idx_club_club_name
ON club(club_name);

-- =========================
-- ORGANIZER ACCOUNT
-- Compte organisateur rattaché à un club existant.
-- =========================
CREATE TABLE IF NOT EXISTS organizer_account (
  id TEXT PRIMARY KEY,

  club_id TEXT NOT NULL,

  first_name TEXT,
  last_name TEXT,

  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,

  email_verified INTEGER NOT NULL DEFAULT 0,
  email_verification_code TEXT,
  email_verification_expires_at TEXT,

  login_otp_code TEXT,
  login_otp_expires_at TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (club_id) REFERENCES club(id)
);

CREATE INDEX IF NOT EXISTS idx_organizer_account_club_id
ON organizer_account(club_id);

CREATE INDEX IF NOT EXISTS idx_organizer_account_email
ON organizer_account(email);

-- =========================
-- CLUB ACCESS
-- Liste des accès identifiés liés à un club.
-- Permet d'afficher qui a accès au club et de préparer le multi-accès.
-- =========================
CREATE TABLE IF NOT EXISTS club_access (
  id TEXT PRIMARY KEY,

  club_id TEXT NOT NULL,

  email TEXT NOT NULL,
  first_name TEXT,
  last_name  TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (club_id) REFERENCES club(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_club_access_club_email
ON club_access(club_id, email);

CREATE INDEX IF NOT EXISTS idx_club_access_club_id
ON club_access(club_id);