PRAGMA foreign_keys = ON;

-- =====================================================
-- TOURNAMENT REGULATION
-- Informations nécessaires au règlement officiel
-- =====================================================

CREATE TABLE IF NOT EXISTS tournament_regulation (

  tournament_id TEXT PRIMARY KEY,

  organizer_contact_name TEXT,
  organizer_email        TEXT,
  organizer_phone        TEXT,

  venue_name   TEXT,
  venue_street TEXT,
  venue_zip    TEXT,
  venue_city   TEXT,

  number_of_tables INTEGER,

  playing_area_preset        TEXT,
  playing_area_info_text     TEXT,
  playing_area_length_meters INTEGER,
  playing_area_width_meters  INTEGER,
  playing_area_compliant     INTEGER,

  ball_brand_and_type   TEXT,
  ball_provision_policy TEXT,

  registration_deadline TEXT,
  check_in_deadline     TEXT,
  first_matches_start   TEXT,
  expected_end_time     TEXT,

  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,

  FOREIGN KEY (tournament_id)
    REFERENCES tournament(id)
    ON DELETE CASCADE
);