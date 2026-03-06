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
  NULL,
  NULL,
  NULL,
  'Quentin',
  'Soumet',
  'soumet.quentin@gmail.com',
  NULL,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
)
ON CONFLICT(club_number) DO NOTHING;