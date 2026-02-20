PRAGMA foreign_keys = ON;

CREATE TABLE organizer_account (
  id TEXT PRIMARY KEY,
  club_name TEXT NOT NULL,
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  email_verified INTEGER NOT NULL DEFAULT 0,
  email_verification_code TEXT,
  email_verification_expires_at TEXT,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE TABLE club_profile (
  organizer_id         TEXT PRIMARY KEY,
  club_number          TEXT,
  club_name            TEXT,
  departement_code     TEXT,
  city                 TEXT,
  address1             TEXT,
  address2             TEXT,
  latitude             REAL,
  longitude            REAL,
  contact_first_name   TEXT,
  contact_last_name    TEXT,
  logo_path            TEXT,
  updated_at           TEXT NOT NULL,
  FOREIGN KEY (organizer_id) REFERENCES organizer_account(id)
);