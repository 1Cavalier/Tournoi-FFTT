PRAGMA foreign_keys = ON;

-- =====================================================
-- TOURNAMENT POLICY
-- Règles d'inscription globales
-- =====================================================

CREATE TABLE IF NOT EXISTS tournament_policy (

  tournament_id TEXT PRIMARY KEY,

  max_tableaux_per_day INTEGER NOT NULL,
  max_total_tableaux   INTEGER NOT NULL,

  female_extra_rule_type    TEXT NOT NULL,
  female_extra_tableau_code TEXT,

  participant_policy_type TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE
);