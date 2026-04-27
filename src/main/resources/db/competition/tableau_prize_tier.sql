-- =============================================================================
-- BASE : competition.db
-- Fichier : tableau_prize_tier.sql
-- Description : Paliers de dotations d'un tableau (prix, remises, récompenses).
--               Relation 1-N avec tableau.
--
-- Connexions :
--   → tableau(id) ON DELETE CASCADE
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : tableau_prize_tier
-- Définit les dotations par palier de classement d'un tableau.
-- Exemples :
--   1er → 50€ cash
--   2ème → 30€ cash
--   3ème-4ème → remise d'inscription de 50%
--
-- Plusieurs lignes par tableau (une par palier de classement).
-- =============================================================================

CREATE TABLE IF NOT EXISTS tableau_prize_tier (

  -- Identifiant auto-incrémenté (pas de UUID ici, clé de commodité)
  id INTEGER PRIMARY KEY AUTOINCREMENT,

  -- Tableau concerné
  tableau_id TEXT NOT NULL,

  -- Plage de rangs couverte par ce palier :
  --   from_rank = to_rank → un seul rang (ex : 1er uniquement)
  --   from_rank < to_rank → groupe ex-aequo (ex : 3ème-4ème)
  from_rank INTEGER,
  to_rank   INTEGER,

  -- Type de récompense :
  --   CASH                    → espèces (cash_amount en centimes)
  --   REGISTRATION_DISCOUNT   → remise sur l'inscription suivante
  --   TROPHY                  → trophée physique (pas de valeur monétaire)
  --   NONE                    → pas de récompense pour ce palier
  reward_type TEXT,

  -- Montant en espèces (centimes d'euro, NULL si reward_type != CASH)
  cash_amount INTEGER,

  -- Pourcentage de remise (0-100, NULL si reward_type != REGISTRATION_DISCOUNT)
  registration_discount_percent INTEGER,

  FOREIGN KEY (tableau_id)
    REFERENCES tableau(id)
    ON DELETE CASCADE
);

-- Récupération de tous les paliers d'un tableau (lecture groupée)
CREATE INDEX IF NOT EXISTS idx_tableau_prize_tier_tableau_id
ON tableau_prize_tier(tableau_id);