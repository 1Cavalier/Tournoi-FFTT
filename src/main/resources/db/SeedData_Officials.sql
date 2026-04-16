-- =============================================================================
-- SEED DATA — OFFICIELS (Juges-arbitres et arbitres)
-- Données de test représentatives d'un tournoi départemental 91.
-- Toutes les licences sont fictives (format FFTT : 8 chiffres).
-- =============================================================================
 
-- -----------------------------------------------------------------------------
-- JOUEURS (profils officiels)
-- -----------------------------------------------------------------------------
 
INSERT INTO player (
  id, license_number, first_name, last_name,
  gender, age_category, club_number, club_name, departement_code,
  phase1_start_points, phase2_official_points,
  medical_certificate_status, license_type, mutated, nationality,
  created_at, updated_at
) VALUES
 
  -- JA3 — Juge-arbitre départemental, senior, Brunoy CTT
  (
    'player-ja-001',
    '09100001',
    'Michel', 'LAMBERT',
    'MALE', 'SENIOR',
    '08911132', 'Brunoy CTT', '91',
    1200, 1185,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- JAN — Juge-arbitre national, senior, club Évry
  (
    'player-ja-002',
    '09100002',
    'Isabelle', 'MOREAU',
    'FEMALE', 'SENIOR',
    '08911245', 'ASEPTT Évry', '91',
    1650, 1640,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- JA3 — Second JA disponible, vétéran
  (
    'player-ja-003',
    '09100003',
    'Bernard', 'DUPUIS',
    'MALE', 'VETERAN_50',
    '08911310', 'TT Corbeil', '91',
    900, 885,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- Arbitre REGIONAL — Arbitre expérimenté, senior
  (
    'player-arb-001',
    '09100004',
    'Sophie', 'RENARD',
    'FEMALE', 'SENIOR',
    '08911132', 'Brunoy CTT', '91',
    1450, 1430,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- Arbitre NATIONAL — Arbitre national, senior
  (
    'player-arb-002',
    '09100005',
    'Luc', 'FONTAINE',
    'MALE', 'SENIOR',
    '08911410', 'TT Massy', '91',
    1800, 1790,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- Arbitre CLUB — Arbitre débutant local
  (
    'player-arb-003',
    '09100006',
    'Thomas', 'GIRARD',
    'MALE', 'JUNIOR_1',
    '08911132', 'Brunoy CTT', '91',
    750, 730,
    'VALIDE', 'COMPETITION', 0, 'FR',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  )
 
ON CONFLICT(license_number) DO NOTHING;
 
 
-- -----------------------------------------------------------------------------
-- QUALIFICATIONS OFFICIELLES
-- -----------------------------------------------------------------------------
 
INSERT INTO official_qualification (
  id, player_id, role_type,
  judge_referee_grade, referee_grade, technical_grade,
  created_at, updated_at
) VALUES
 
  -- Michel LAMBERT -> JA3
  (
    'qual-001', 'player-ja-001', 'JUGE_ARBITRE',
    'JA3', NULL, NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- Isabelle MOREAU -> JAN
  (
    'qual-002', 'player-ja-002', 'JUGE_ARBITRE',
    'JAN', NULL, NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- Bernard DUPUIS -> JA3
  (
    'qual-003', 'player-ja-003', 'JUGE_ARBITRE',
    'JA2', NULL, NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- Sophie RENARD -> Arbitre REGIONAL
  (
    'qual-004', 'player-arb-001', 'ARBITRE',
    NULL, 'REGIONAL', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- Luc FONTAINE -> Arbitre NATIONAL
  (
    'qual-005', 'player-arb-002', 'ARBITRE',
    NULL, 'NATIONAL', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  ),
 
  -- Thomas GIRARD -> Arbitre CLUB
  (
    'qual-006', 'player-arb-003', 'ARBITRE',
    NULL, 'CLUB', NULL,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
  )
 
ON CONFLICT(player_id, role_type) DO NOTHING;