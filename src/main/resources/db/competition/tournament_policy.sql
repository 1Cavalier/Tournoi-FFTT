-- =============================================================================
-- BASE : competition.db
-- Fichier : tournament_policy.sql
-- Description : Règles globales d'inscription et de participation au tournoi.
--               Relation 1-1 avec tournament.
--
-- Connexions :
--   → tournament(id) ON DELETE CASCADE
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : tournament_policy
-- Règles d'organisation globales du tournoi :
--   - Nombre de tableaux par joueur et par jour
--   - Règle spéciale pour les tableaux féminins
--   - Politique d'éligibilité des participants
-- =============================================================================

CREATE TABLE IF NOT EXISTS tournament_policy (

  -- Même id que le tournoi (relation 1-1)
  tournament_id TEXT PRIMARY KEY,

  -- Nombre maximum de tableaux auxquels un joueur peut s'inscrire par jour
  max_tableaux_per_day INTEGER NOT NULL,

  -- Nombre maximum de tableaux auxquels un joueur peut s'inscrire au total
  max_total_tableaux INTEGER NOT NULL,

  -- Règle spéciale pour les féminines :
  --   female_extra_rule_type :
  --     NONE                → pas de règle spéciale
  --     FREE_EXTRA_TABLEAU  → les féminines peuvent s'inscrire à un tableau
  --                           supplémentaire gratuitement
  --     MANDATORY_FEMALE    → les féminines doivent s'inscrire au tableau féminin
  female_extra_rule_type    TEXT NOT NULL,

  -- Code du tableau féminin concerné par la règle (NULL si NONE)
  female_extra_tableau_code TEXT,

  -- Politique d'éligibilité des participants :
  --   FFTT_ONLY     → uniquement des licenciés FFTT
  --   OPEN          → ouvert aux licenciés d'autres fédérations
  --   GUEST_ALLOWED → invités sans licence acceptés
  --   NULL          → aucune restriction définie (défaut)
  participant_policy_type TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE
);