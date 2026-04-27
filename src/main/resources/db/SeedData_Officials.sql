-- =============================================================================
-- BASE : club.db
-- Fichier : SeedData_Officials.sql
-- Description : Données de test — officiels fictifs représentatifs
--               d'un tournoi départemental 91 (Essonne).
--
-- Toutes les licences sont fictives (format FFTT : 8 chiffres).
-- ON CONFLICT DO NOTHING garantit l'idempotence.
--
-- Profils insérés :
--   JA  : Michel LAMBERT (JA3), Isabelle MOREAU (JAN), Bernard DUPUIS (JA2)
--   ARB : Sophie RENARD (REGIONAL), Luc FONTAINE (NATIONAL), Thomas GIRARD (CLUB)
-- =============================================================================


-- =============================================================================
-- JOUEURS (profils officiels)
-- =============================================================================

INSERT INTO player (
  id, license_number, first_name, last_name,
  gender, age_category,
  club_number, club_name, departement_code,
  phase1_start_points, phase2_official_points,
  medical_certificate_status, license_type, mutated, nationality,
  created_at, updated_at
)
VALUES

  -- Michel LAMBERT — JA3, Brunoy CTT
  -- Juge-arbitre départemental, joueur senior 1200 pts
  (
    'player-ja-001', '09100001',
    'Michel', 'LAMBERT',
    'MALE', 'SENIOR',
    '08911132', 'Brunoy CTT', '91',
    1200, 1185,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Isabelle MOREAU — JAN, ASEPTT Évry
  -- Juge-arbitre national, senior 1650 pts
  (
    'player-ja-002', '09100002',
    'Isabelle', 'MOREAU',
    'FEMALE', 'SENIOR',
    '08911245', 'ASEPTT Évry', '91',
    1650, 1640,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Bernard DUPUIS — JA2, TT Corbeil
  -- Juge-arbitre régional, vétéran 900 pts
  (
    'player-ja-003', '09100003',
    'Bernard', 'DUPUIS',
    'MALE', 'VETERAN_50',
    '08911310', 'TT Corbeil', '91',
    900, 885,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Sophie RENARD — Arbitre REGIONAL, Brunoy CTT
  -- Arbitre régionale, senior 1450 pts
  (
    'player-arb-001', '09100004',
    'Sophie', 'RENARD',
    'FEMALE', 'SENIOR',
    '08911132', 'Brunoy CTT', '91',
    1450, 1430,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Luc FONTAINE — Arbitre NATIONAL, TT Massy
  -- Arbitre national, senior 1800 pts
  (
    'player-arb-002', '09100005',
    'Luc', 'FONTAINE',
    'MALE', 'SENIOR',
    '08911410', 'TT Massy', '91',
    1800, 1790,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Thomas GIRARD — Arbitre CLUB, Brunoy CTT
  -- Jeune arbitre en formation, junior 750 pts
  (
    'player-arb-003', '09100006',
    'Thomas', 'GIRARD',
    'MALE', 'JUNIOR_1',
    '08911132', 'Brunoy CTT', '91',
    750, 730,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  )

ON CONFLICT(license_number) DO NOTHING;


-- =============================================================================
-- QUALIFICATIONS OFFICIELLES
-- =============================================================================

INSERT INTO official_qualification (
  id, player_id, role_type,
  judge_referee_grade, referee_grade, technical_grade,
  created_at, updated_at
)
VALUES

  -- Michel LAMBERT → JUGE_ARBITRE JA3
  (
    'qual-001', 'player-ja-001', 'JUGE_ARBITRE',
    'JA3', NULL, NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Isabelle MOREAU → JUGE_ARBITRE JAN
  (
    'qual-002', 'player-ja-002', 'JUGE_ARBITRE',
    'JAN', NULL, NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Bernard DUPUIS → JUGE_ARBITRE JA2
  -- (corrigé : était JA3 dans SeedData_Officials.sql original, le commentaire disait JA3 mais les données disaient JA2)
  (
    'qual-003', 'player-ja-003', 'JUGE_ARBITRE',
    'JA2', NULL, NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Sophie RENARD → ARBITRE REGIONAL
  (
    'qual-004', 'player-arb-001', 'ARBITRE',
    NULL, 'REGIONAL', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Luc FONTAINE → ARBITRE NATIONAL
  (
    'qual-005', 'player-arb-002', 'ARBITRE',
    NULL, 'NATIONAL', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),

  -- Thomas GIRARD → ARBITRE CLUB
  (
    'qual-006', 'player-arb-003', 'ARBITRE',
    NULL, 'CLUB', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  )

ON CONFLICT(player_id, role_type) DO NOTHING;