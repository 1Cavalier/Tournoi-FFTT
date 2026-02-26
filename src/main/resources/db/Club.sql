PRAGMA foreign_keys = ON;

-- =========================
-- CLUB (un club peut avoir plusieurs emails/comptes)
-- =========================
CREATE TABLE IF NOT EXISTS club (
  id TEXT PRIMARY KEY,

  club_number TEXT,
  club_name   TEXT,

  departement_code TEXT,
  city            TEXT,
  address1         TEXT,
  address2         TEXT,

  latitude  REAL,
  longitude REAL,

  contact_first_name TEXT,
  contact_last_name  TEXT,

  logo_path TEXT,

  updated_at TEXT NOT NULL
);

-- =========================
-- ORGANIZER ACCOUNT (compte organisme rattaché à un club)
-- =========================
CREATE TABLE IF NOT EXISTS organizer_account (
  id TEXT PRIMARY KEY,

  club_id TEXT NOT NULL,

  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,

  -- vérification à l'inscription
  email_verified INTEGER NOT NULL DEFAULT 0,
  email_verification_code TEXT,
  email_verification_expires_at TEXT,

  -- OTP à chaque connexion
  login_otp_code TEXT,
  login_otp_expires_at TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (club_id) REFERENCES club(id)
);