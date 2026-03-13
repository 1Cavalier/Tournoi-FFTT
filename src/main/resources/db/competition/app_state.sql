PRAGMA foreign_keys = ON;

-- =====================================================
-- APP STATE
-- Etat local de l'application
-- =====================================================

CREATE TABLE IF NOT EXISTS app_state (
  id INTEGER PRIMARY KEY CHECK (id = 1),
  current_tournament_id TEXT NULL
);

INSERT OR IGNORE INTO app_state(id, current_tournament_id)
VALUES (1, NULL);