PRAGMA foreign_keys = ON;

-- =====================================================
-- TABLEAU
-- Tableaux d'un tournoi
-- =====================================================

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS tableau (

  id TEXT PRIMARY KEY,

  tournament_id TEXT NOT NULL,

  code TEXT,
  designation TEXT,
  date TEXT,

  gender_policy TEXT,

  age_policy_type TEXT,
  age_min_category TEXT,
  age_max_category TEXT,
  allowed_age_categories TEXT,

  points_rule_type TEXT,
  min_points INTEGER,
  max_points INTEGER,

  max_players INTEGER,
  waitlist_capacity INTEGER,

  prepaid_fee INTEGER,
  on_site_fee INTEGER,

  check_in_end TEXT,
  start_time TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE,

  UNIQUE (tournament_id, code)
);

CREATE INDEX IF NOT EXISTS idx_tableau_tournament_id
ON tableau(tournament_id);

CREATE INDEX IF NOT EXISTS idx_tableau_tournament_date
ON tableau(tournament_id, date);