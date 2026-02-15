PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS organizer_account (
  id            TEXT PRIMARY KEY,
  club_name     TEXT NOT NULL,
  email         TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  created_at    TEXT NOT NULL,
  updated_at    TEXT NOT NULL
);


