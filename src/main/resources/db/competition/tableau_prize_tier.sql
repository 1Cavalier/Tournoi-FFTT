PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS tableau_prize_tier (

  id INTEGER PRIMARY KEY AUTOINCREMENT,

  tableau_id TEXT NOT NULL,

  from_rank INTEGER,
  to_rank INTEGER,

  reward_type TEXT,
  cash_amount INTEGER,
  registration_discount_percent INTEGER,

  FOREIGN KEY (tableau_id)
    REFERENCES tableau(id)
    ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tableau_prize_tier_tableau_id
ON tableau_prize_tier(tableau_id);