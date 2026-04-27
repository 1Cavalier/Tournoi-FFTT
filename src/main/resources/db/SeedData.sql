-- =============================================================================
-- BASE : club.db
-- Fichier : SeedData.sql
-- Description : Données initiales — club organisateur par défaut (Brunoy CTT).
--
-- Exécuté une seule fois à l'initialisation de club.db.
-- ON CONFLICT DO NOTHING garantit l'idempotence si le script est rejoué.
-- =============================================================================

INSERT INTO club (
  id,
  club_number,
  club_name,
  departement_code,
  city,
  address1,
  address2,
  latitude,
  longitude,
  contact_first_name,
  contact_last_name,
  official_contact_email,
  logo_path,
  created_at,
  updated_at
)
VALUES (
  'club-brunoy',
  '08911132',
  'Brunoy CTT',
  '91',
  'Brunoy',
  'Salle municipale',
  NULL,   -- pas de ligne d'adresse 2
  NULL,   -- latitude non renseignée
  NULL,   -- longitude non renseignée
  'Serge',
  'BOULIER',
  'serge.boulier@gmail.com',
  NULL,   -- logo non renseigné
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
)
ON CONFLICT(club_number) DO NOTHING;