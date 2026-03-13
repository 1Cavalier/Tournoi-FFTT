PRAGMA foreign_keys = ON;

-- =====================================================
-- TABLEAU
-- Tableaux d'un tournoi
-- =====================================================

CREATE TABLE IF NOT EXISTS tableau (

  id TEXT PRIMARY KEY,

  tournament_id TEXT NOT NULL,

  code        TEXT NOT NULL,
  designation TEXT NOT NULL,
  date        TEXT NOT NULL,

  gender_policy TEXT NOT NULL,

  points_rule_type TEXT NOT NULL,
  min_points       INTEGER,
  max_points       INTEGER,

  age_category_policy_type   TEXT,
  age_category_policy_values TEXT,

  max_players       INTEGER NOT NULL,
  waitlist_capacity INTEGER NOT NULL,

  fee_amount_cents INTEGER,
  fee_label        TEXT,

  check_in_end TEXT NOT NULL,
  start_time   TEXT NOT NULL,

  prize_summary TEXT,

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