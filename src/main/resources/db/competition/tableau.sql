-- =============================================================================
-- BASE : competition.db
-- Fichier : tableau.sql
-- Description : Tableaux d'un tournoi (ex : "Hommes Toutes Séries",
--               "Femmes -1000 pts", "Vétérans 40").
--
-- Connexions :
--   → tournament(id) ON DELETE CASCADE
--   ← tableau_prize_tier(tableau_id)
--   ← poule(tableau_code + tournament_id)
--   ← ko_bracket(tableau_code + tournament_id)
--   ← classification_bracket(tableau_code + tournament_id)
--
-- Note : poule, ko_bracket et classification_bracket référencent le tableau
--   via (tableau_code, tournament_id) plutôt que l'UUID du tableau,
--   car le code est stable et lisible (ex : "HTS", "F800").
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : tableau
-- Un tableau est une épreuve d'un tournoi avec ses propres règles
-- de participation, son mode de tirage et son format de classement.
-- =============================================================================

CREATE TABLE IF NOT EXISTS tableau (

  -- Identifiant interne UUID
  id TEXT PRIMARY KEY,

  -- Tournoi auquel appartient ce tableau
  tournament_id TEXT NOT NULL,

  -- Code court unique dans le tournoi (ex : "HTS", "F800", "VET40")
  -- Utilisé comme clé de liaison vers poule/ko_bracket/classification_bracket
  code TEXT,

  -- Libellé complet affiché dans l'UI (ex : "Hommes Toutes Séries")
  designation TEXT,

  -- Date du tableau (format "YYYY-MM-DD") — peut différer des dates du tournoi
  date TEXT,

  -- Politique de genre : MASCULIN | FEMININ | MIXTE
  gender_policy TEXT,

  -- Politique de catégorie d'âge :
  --   age_policy_type    : ANY | RANGE | EXACT | LIST
  --   age_min_category   : catégorie minimale (si RANGE)
  --   age_max_category   : catégorie maximale (si RANGE)
  --   allowed_age_categories : liste JSON (si LIST, ex : '["VETERAN_40","VETERAN_50"]')
  age_policy_type        TEXT,
  age_min_category       TEXT,
  age_max_category       TEXT,
  allowed_age_categories TEXT,

  -- Règle de points :
  --   TOUTES_SERIES → pas de restriction de points
  --   MIN_POINTS    → uniquement min_points renseigné
  --   MAX_POINTS    → uniquement max_points renseigné
  --   RANGE_POINTS  → min_points et max_points renseignés
  points_rule_type TEXT,
  min_points       INTEGER,  -- seuil inférieur (NULL si non applicable)
  max_points       INTEGER,  -- seuil supérieur (NULL si non applicable)

  -- Capacités du tableau
  max_players        INTEGER,  -- nombre maximum d'inscrits
  waitlist_capacity  INTEGER,  -- capacité de la liste d'attente (0 = pas de file)

  -- Frais d'inscription en centimes d'euro
  prepaid_fee  INTEGER,  -- tarif prépayé (en ligne)
  on_site_fee  INTEGER,  -- tarif sur place

  -- Horaires (format "HH:MM")
  check_in_end TEXT,  -- fin du pointage (heure limite pour pointer)
  start_time   TEXT,  -- heure de début du tableau

  -- Algorithme de tirage des poules :
  --   SNAKE  → méthode du serpent FFTT (défaut)
  --   RANDOM → tirage aléatoire
  draw_algorithm_type TEXT NOT NULL DEFAULT 'SNAKE',

  -- Mode de classement final :
  --   NONE        → aucun match de classement, ex-aequo par vague (défaut)
  --   THIRD_PLACE → petite finale uniquement (3ème/4ème)
  --   TOP_8       → matchs 3/4 + 5/6 + 7/8
  --   FULL        → tous les matchs de classement
  classification_mode TEXT NOT NULL DEFAULT 'NONE',

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE,

  -- Un code de tableau est unique au sein d'un tournoi
  UNIQUE (tournament_id, code)
);

-- Recherche de tous les tableaux d'un tournoi
CREATE INDEX IF NOT EXISTS idx_tableau_tournament_id
ON tableau(tournament_id);

-- Tri par date (affichage du programme journalier)
CREATE INDEX IF NOT EXISTS idx_tableau_tournament_date
ON tableau(tournament_id, date);