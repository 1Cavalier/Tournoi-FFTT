-- =============================================================================
-- BASE : competition.db
-- Fichier : pool_phase.sql
-- Description : Phase de poules et tableau KO d'un tournoi FFTT.
--               Crée toutes les tables nécessaires au déroulement complet
--               d'un tableau : poules → KO → matchs de classement.
--
-- Ordre de création (respecter les FK) :
--   1. guest_participant          (FK → tournament)
--   2. poule                      (FK → tournament)
--   3. pool_slot                  (FK → poule)
--   4. pool_match                 (FK → poule)
--   5. pool_match_set             (FK → pool_match)
--   6. ko_bracket                 (FK → tournament)
--   7. ko_match                   (FK → ko_bracket)
--   8. ko_match_set               (FK → ko_match)
--   9. classification_bracket     (FK → tournament)
--  10. classification_match       (FK → classification_bracket)
--  11. classification_match_set   (FK → classification_match)
--
-- Connexions avec les autres fichiers :
--   → tournament(id)   pour toutes les tables racine
--   → tableau(code + tournament_id) : référencé via tableau_code + tournament_id
--     (pas de FK directe car le tableau est identifié par son code métier)
--   → player(license_number) ou guest_participant(guest_id)
--     via participant_id : résolution assurée par PlayerParticipantResolver
--
-- Modèle de participant_id :
--   - Joueur FFTT  : license_number (ex : "08911132A")
--   - Invité       : guest_id (ex : "GUEST-abc123")
--   - Étranger     : préfixé par la fédération (ex : "ETTRM-001")
--
-- Tous les statuts suivent le cycle :
--   PENDING → IN_PROGRESS → COMPLETED | WALKOVER
--   PENDING → BYE  (pour ko_match uniquement)
--
-- Tous les scores sont stockés manche par manche dans les tables *_set.
-- La validation des règles ITTF (11 pts, déuce, best-of-5) est assurée
-- par le domaine Java, pas par la base de données.
-- =============================================================================

PRAGMA foreign_keys = ON;


-- =============================================================================
-- TABLE : guest_participant
-- Participants invités ou licenciés d'une autre fédération,
-- non présents dans club.db/player.
-- Liés à un tournoi (pas à un tableau spécifique) pour permettre
-- la participation à plusieurs tableaux du même tournoi.
--
-- Connexions :
--   → tournament(id) ON DELETE CASCADE
--   ← pool_slot.participant_id (référence douce)
--   ← pool_match.slot1_participant_id / slot2_participant_id (référence douce)
--   ← ko_match.player1_id / player2_id (référence douce)
--   ← classification_match.player1_id / player2_id (référence douce)
-- =============================================================================

CREATE TABLE IF NOT EXISTS guest_participant (

  -- Identifiant stable utilisé dans toutes les tables de match
  -- Format recommandé : "GUEST-<uuid>" ou "ETTRM-<numero>"
  guest_id TEXT PRIMARY KEY,

  -- Nom complet affiché (prénom + nom, ou nom de compétition)
  full_name TEXT NOT NULL,

  -- Genre : MALE | FEMALE
  gender TEXT NOT NULL,

  -- Nationalité ISO-3166-1 alpha-2 (ex : "FR", "BE")
  nationality TEXT NOT NULL,

  -- Catégorie d'âge FFTT (utilisée pour vérifier l'éligibilité au tableau)
  age_category TEXT NOT NULL,

  -- Statut du certificat médical : VALIDE | NON_VALIDE | NON_PRESENT
  medical_cert_status TEXT NOT NULL DEFAULT 'VALIDE',

  -- Tournoi auquel cet invité est rattaché
  tournament_id TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE
);

-- Recherche des invités d'un tournoi
CREATE INDEX IF NOT EXISTS idx_guest_participant_tournament
ON guest_participant(tournament_id);


-- =============================================================================
-- TABLE : poule
-- Groupe de joueurs d'un même tableau formant une poule de phase de groupes.
-- Générée par PoolPhaseService.drawPools() via l'algorithme snake FFTT.
-- Les matchs sont dans pool_match, les joueurs dans pool_slot.
--
-- Connexions :
--   → tournament(id) ON DELETE CASCADE
--   ← pool_slot(poule_id)
--   ← pool_match(poule_id)
-- =============================================================================

CREATE TABLE IF NOT EXISTS poule (

  -- Identifiant interne UUID
  id TEXT PRIMARY KEY,

  -- Code du tableau auquel appartient cette poule (ex : "HTS")
  -- Référence souple vers tableau(code + tournament_id)
  tableau_code TEXT NOT NULL,

  -- Tournoi auquel appartient cette poule
  tournament_id TEXT NOT NULL,

  -- Numéro d'ordre de la poule dans le tableau (commence à 1)
  -- Utilisé pour l'affichage (ex : "Poule A" = pool_number 1)
  pool_number INTEGER NOT NULL,

  created_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE,

  -- Une poule est unique dans son tableau
  UNIQUE (tournament_id, tableau_code, pool_number)
);

-- Recherche de toutes les poules d'un tableau
CREATE INDEX IF NOT EXISTS idx_poule_tournament_tableau
ON poule(tournament_id, tableau_code);


-- =============================================================================
-- TABLE : pool_slot
-- Place d'un participant dans une poule.
-- seed_rank    : rang de tirage au sort global (1 = meilleur joueur du tableau)
-- position_in_pool : position dans la poule (1, 2 ou 3)
--
-- Connexions :
--   → poule(id) ON DELETE CASCADE
--   participant_id → player(license_number) ou guest_participant(guest_id)
--                    (résolution par PlayerParticipantResolver)
-- =============================================================================

CREATE TABLE IF NOT EXISTS pool_slot (

  id TEXT PRIMARY KEY,

  -- Poule à laquelle appartient ce slot
  poule_id TEXT NOT NULL,

  -- Identifiant du participant (licence FFTT ou guest_id)
  participant_id TEXT NOT NULL,

  -- Rang serpent global du joueur dans le tirage (1 = tête de série)
  seed_rank INTEGER NOT NULL,

  -- Position dans la poule (1, 2 ou 3)
  -- Détermine l'ordre des matchs FFTT : 1v3, 1v2, 2v3
  position_in_pool INTEGER NOT NULL,

  FOREIGN KEY (poule_id)
    REFERENCES poule(id)
    ON DELETE CASCADE,

  -- Un participant ne peut apparaître qu'une fois par poule
  UNIQUE (poule_id, participant_id),

  -- Chaque position est unique dans la poule
  UNIQUE (poule_id, position_in_pool)
);

-- Récupération de tous les slots d'une poule
CREATE INDEX IF NOT EXISTS idx_pool_slot_poule
ON pool_slot(poule_id);


-- =============================================================================
-- TABLE : pool_match
-- Match entre deux participants d'une poule.
-- L'ordre des matchs FFTT est : match_order 1 = pos1 vs pos3,
--                                match_order 2 = pos1 vs pos2,
--                                match_order 3 = pos2 vs pos3.
-- Le score détaillé (manche par manche) est dans pool_match_set.
--
-- Statuts possibles : PENDING | IN_PROGRESS | COMPLETED | WALKOVER
--
-- Connexions :
--   → poule(id) ON DELETE CASCADE
--   ← pool_match_set(pool_match_id)
-- =============================================================================

CREATE TABLE IF NOT EXISTS pool_match (

  id TEXT PRIMARY KEY,

  -- Poule à laquelle appartient ce match
  poule_id TEXT NOT NULL,

  -- Numéro d'ordre du match dans la poule (1, 2 ou 3)
  match_order INTEGER NOT NULL,

  -- Identifiants des deux participants (dans l'ordre FFTT de la poule)
  slot1_participant_id TEXT NOT NULL,  -- participant en position 1 ou 2
  slot2_participant_id TEXT NOT NULL,  -- participant en position 2 ou 3

  -- Statut du match
  status TEXT NOT NULL DEFAULT 'PENDING',

  -- Participant forfait (NULL sauf si status = WALKOVER)
  -- Le gagnant est l'autre participant
  walkover_participant_id TEXT,

  FOREIGN KEY (poule_id)
    REFERENCES poule(id)
    ON DELETE CASCADE,

  -- Un seul match par ordre dans la poule
  UNIQUE (poule_id, match_order)
);

-- Récupération de tous les matchs d'une poule
CREATE INDEX IF NOT EXISTS idx_pool_match_poule
ON pool_match(poule_id);


-- =============================================================================
-- TABLE : pool_match_set
-- Score manche par manche d'un match de poule.
-- Format ITTF : best-of-5, 11 points, règle du déuce (2 points d'écart min).
-- La validation des règles est faite par PoolMatchScore dans le domaine Java.
--
-- Connexions :
--   → pool_match(id) ON DELETE CASCADE
-- =============================================================================

CREATE TABLE IF NOT EXISTS pool_match_set (

  id TEXT PRIMARY KEY,

  -- Match auquel appartient cette manche
  pool_match_id TEXT NOT NULL,

  -- Numéro de manche (1 à 5)
  set_order INTEGER NOT NULL,

  -- Points marqués par chaque participant dans cette manche
  points_p1 INTEGER NOT NULL,  -- participant slot1
  points_p2 INTEGER NOT NULL,  -- participant slot2

  FOREIGN KEY (pool_match_id)
    REFERENCES pool_match(id)
    ON DELETE CASCADE,

  -- Une seule entrée par manche par match
  UNIQUE (pool_match_id, set_order)
);

-- Récupération de toutes les manches d'un match
CREATE INDEX IF NOT EXISTS idx_pool_match_set_match
ON pool_match_set(pool_match_id);


-- =============================================================================
-- TABLE : ko_bracket
-- Tableau KO généré par BracketBuilder à partir des qualifiés des poules.
-- Taille toujours une puissance de 2 (4, 8, 16, 32…).
-- Les matchs sont dans ko_match.
--
-- Connexions :
--   → tournament(id) ON DELETE CASCADE
--   ← ko_match(ko_bracket_id)
-- =============================================================================

CREATE TABLE IF NOT EXISTS ko_bracket (

  id TEXT PRIMARY KEY,

  -- Code du tableau auquel appartient ce bracket (ex : "HTS")
  tableau_code TEXT NOT NULL,

  -- Tournoi auquel appartient ce bracket
  tournament_id TEXT NOT NULL,

  -- Taille du tableau (puissance de 2 : 4, 8, 16, 32…)
  bracket_size INTEGER NOT NULL,

  -- Nombre total de tours (log2(bracket_size))
  -- Ex : bracket_size=8 → total_rounds=3 (QF, SF, Finale)
  total_rounds INTEGER NOT NULL,

  created_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE,

  -- Un seul bracket KO par tableau dans un tournoi
  UNIQUE (tournament_id, tableau_code)
);

-- Recherche du bracket d'un tableau
CREATE INDEX IF NOT EXISTS idx_ko_bracket_tournament_tableau
ON ko_bracket(tournament_id, tableau_code);


-- =============================================================================
-- TABLE : ko_match
-- Match du tableau KO, identifié par son tour et sa position.
-- Exemples pour bracket_size=8 :
--   round=1, position=1..4 : quarts de finale
--   round=2, position=1..2 : demi-finales
--   round=3, position=1    : finale
--
-- Statuts :
--   PENDING     → joueurs non encore connus ou match pas commencé
--   IN_PROGRESS → match en cours
--   COMPLETED   → score enregistré
--   WALKOVER    → forfait déclaré
--   BYE         → joueur exempt (passe directement au tour suivant)
--
-- Connexions :
--   → ko_bracket(id) ON DELETE CASCADE
--   ← ko_match_set(ko_match_id)
-- =============================================================================

CREATE TABLE IF NOT EXISTS ko_match (

  id TEXT PRIMARY KEY,

  -- Bracket KO auquel appartient ce match
  ko_bracket_id TEXT NOT NULL,

  -- Tour du tableau (1 = premier tour)
  round    INTEGER NOT NULL,

  -- Position dans le tour (1 = haut du tableau)
  position INTEGER NOT NULL,

  -- Identifiants des joueurs (NULL si pas encore connus = propagation en cours)
  player1_id TEXT,  -- joueur du haut (seed le plus faible)
  player2_id TEXT,  -- joueur du bas

  -- Statut du match
  status TEXT NOT NULL DEFAULT 'PENDING',

  -- Participant forfait (NULL sauf si status = WALKOVER)
  walkover_id TEXT,

  FOREIGN KEY (ko_bracket_id)
    REFERENCES ko_bracket(id)
    ON DELETE CASCADE,

  -- Position unique par tour dans le bracket
  UNIQUE (ko_bracket_id, round, position)
);

-- Récupération de tous les matchs d'un bracket (affichage complet)
CREATE INDEX IF NOT EXISTS idx_ko_match_bracket
ON ko_match(ko_bracket_id);


-- =============================================================================
-- TABLE : ko_match_set
-- Score manche par manche d'un match KO.
-- Même format que pool_match_set (best-of-5, règles ITTF).
--
-- Connexions :
--   → ko_match(id) ON DELETE CASCADE
-- =============================================================================

CREATE TABLE IF NOT EXISTS ko_match_set (

  id TEXT PRIMARY KEY,

  -- Match KO auquel appartient cette manche
  ko_match_id TEXT NOT NULL,

  -- Numéro de manche (1 à 5)
  set_order INTEGER NOT NULL,

  -- Points par participant
  points_p1 INTEGER NOT NULL,  -- player1
  points_p2 INTEGER NOT NULL,  -- player2

  FOREIGN KEY (ko_match_id)
    REFERENCES ko_match(id)
    ON DELETE CASCADE,

  UNIQUE (ko_match_id, set_order)
);

-- Récupération de toutes les manches d'un match KO
CREATE INDEX IF NOT EXISTS idx_ko_match_set_match
ON ko_match_set(ko_match_id);


-- =============================================================================
-- TABLE : classification_bracket
-- Conteneur des matchs de classement générés après la fin du tableau KO.
-- Le mode détermine quels rangs sont individuellement déterminés :
--   NONE        → aucun match, ex-aequo par vague d'élimination
--   THIRD_PLACE → 1 match (3ème/4ème place)
--   TOP_8       → 3 matchs (3/4 + 5/6 + 7/8)
--   FULL        → tous les perdants de chaque tour jouent un match
--
-- Connexions :
--   → tournament(id) ON DELETE CASCADE
--   ← classification_match(classification_bracket_id)
-- =============================================================================

CREATE TABLE IF NOT EXISTS classification_bracket (

  id TEXT PRIMARY KEY,

  -- Code du tableau auquel appartient ce bracket de classement
  tableau_code TEXT NOT NULL,

  -- Tournoi auquel appartient ce bracket
  tournament_id TEXT NOT NULL,

  -- Mode de classement : NONE | THIRD_PLACE | TOP_8 | FULL
  mode TEXT NOT NULL,

  -- Taille du tableau KO associé (pour calculer les ex-aequo)
  bracket_size INTEGER NOT NULL,

  created_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE,

  -- Un seul bracket de classement par tableau
  UNIQUE (tournament_id, tableau_code)
);


-- =============================================================================
-- TABLE : classification_match
-- Match de classement entre deux participants éliminés au même tour du KO.
-- winner_rank : rang attribué au vainqueur (ex : 3 pour la 3ème place)
-- loser_rank  : rang attribué au perdant (ex : 4 pour la 3ème place)
--
-- Statuts : PENDING | IN_PROGRESS | COMPLETED | WALKOVER
-- (pas de BYE : les deux joueurs sont toujours connus)
--
-- Connexions :
--   → classification_bracket(id) ON DELETE CASCADE
--   ← classification_match_set(classification_match_id)
-- =============================================================================

CREATE TABLE IF NOT EXISTS classification_match (

  id TEXT PRIMARY KEY,

  -- Bracket de classement auquel appartient ce match
  classification_bracket_id TEXT NOT NULL,

  -- Rangs attribués selon le résultat du match
  winner_rank INTEGER NOT NULL,  -- rang du vainqueur (ex : 3)
  loser_rank  INTEGER NOT NULL,  -- rang du perdant (ex : 4), toujours > winner_rank

  -- Les deux participants (toujours connus avant de créer le match)
  player1_id TEXT NOT NULL,
  player2_id TEXT NOT NULL,

  -- Statut du match
  status TEXT NOT NULL DEFAULT 'PENDING',

  -- Participant forfait (NULL sauf si status = WALKOVER)
  walkover_id TEXT,

  FOREIGN KEY (classification_bracket_id)
    REFERENCES classification_bracket(id)
    ON DELETE CASCADE
);

-- Récupération de tous les matchs de classement d'un bracket
CREATE INDEX IF NOT EXISTS idx_classification_match_bracket
ON classification_match(classification_bracket_id);


-- =============================================================================
-- TABLE : classification_match_set
-- Score manche par manche d'un match de classement.
-- Même format que pool_match_set et ko_match_set.
--
-- Connexions :
--   → classification_match(id) ON DELETE CASCADE
-- =============================================================================

CREATE TABLE IF NOT EXISTS classification_match_set (

  id TEXT PRIMARY KEY,

  -- Match de classement auquel appartient cette manche
  classification_match_id TEXT NOT NULL,

  -- Numéro de manche (1 à 5)
  set_order INTEGER NOT NULL,

  -- Points par participant
  points_p1 INTEGER NOT NULL,  -- player1
  points_p2 INTEGER NOT NULL,  -- player2

  FOREIGN KEY (classification_match_id)
    REFERENCES classification_match(id)
    ON DELETE CASCADE,

  UNIQUE (classification_match_id, set_order)
);

-- Récupération de toutes les manches d'un match de classement
CREATE INDEX IF NOT EXISTS idx_classification_match_set_match
ON classification_match_set(classification_match_id);