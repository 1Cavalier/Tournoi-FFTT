-- =============================================================================
-- BASE : competition.db
-- Fichier : app_state.sql
-- Description : État local de l'application (singleton).
--               Une seule ligne, toujours id = 1.
--
-- Connexions :
--   current_tournament_id → tournament(id) (référence douce, pas de FK
--   pour éviter de bloquer la suppression d'un tournoi)
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : app_state
-- Stocke l'état de session de l'application entre les lancements.
-- Conçu comme un singleton : la contrainte CHECK (id = 1) garantit
-- qu'une seule ligne peut exister.
-- =============================================================================

CREATE TABLE IF NOT EXISTS app_state (

  -- Toujours égal à 1 (singleton)
  id INTEGER PRIMARY KEY CHECK (id = 1),

  -- Tournoi actif affiché dans l'interface (NULL = aucun tournoi sélectionné)
  -- Référence douce : si le tournoi est supprimé, l'application gère
  -- le cas NULL côté code sans contrainte de FK.
  current_tournament_id TEXT NULL
);

-- Insérer la ligne singleton si elle n'existe pas encore
INSERT OR IGNORE INTO app_state(id, current_tournament_id)
VALUES (1, NULL);