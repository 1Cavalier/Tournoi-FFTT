package fr.pingmanager.gestion_tournois_FFTT.common.exception;

public enum ErrorCode {

    // ========================================================================
    // REF DATA / ORGANIZATION (référentiel)
    // ========================================================================

    // -------- REGION --------
    REGION_CODE_REQUIRED,
    REGION_NAME_REQUIRED,

    // -------- DEPARTEMENT --------
    DEPARTEMENT_CODE_REQUIRED,
    DEPARTEMENT_NAME_REQUIRED,
    DEPARTEMENT_REGION_REQUIRED,

    // -------- CLUB --------
    CLUB_NUMBER_REQUIRED,
    CLUB_NAME_REQUIRED,
    CLUB_CITY_REQUIRED,
    CLUB_DEPARTEMENT_REQUIRED,
    CLUB_GEO_COORDINATES_INCOMPLETE,
    CLUB_GEO_COORDINATES_INVALID,

    // ========================================================================
    // IDENTITY (personnes / qualifications)
    // ========================================================================

    // -------- OFFICIAL / QUALIFICATION --------
    OFFICIAL_ROLE_REQUIRED,
    OFFICIAL_GRADE_REQUIRED,
    OFFICIAL_INCONSISTENT_GRADE,

    // -------- PLAYER --------
    PLAYER_REQUIRED,
    PLAYER_LICENSE_REQUIRED,
    PLAYER_FIRST_NAME_REQUIRED,
    PLAYER_LAST_NAME_REQUIRED,
    PLAYER_GENDER_REQUIRED,
    PLAYER_NATIONALITY_REQUIRED,
    PLAYER_CLUB_REQUIRED,
    PLAYER_AGE_CATEGORY_REQUIRED,
    PLAYER_LICENSE_TYPE_REQUIRED,
    PLAYER_MEDICAL_CERT_REQUIRED,
    PLAYER_POINTS_NEGATIVE,
    PLAYER_QUALIFICATION_REQUIRED,

    // -------- PARTICIPANT (abstraction Player/Foreign/Guest) --------
    PARTICIPANT_REQUIRED,
    PARTICIPANT_ID_REQUIRED,
    PARTICIPANT_NAME_REQUIRED,
    PARTICIPANT_GENDER_REQUIRED,
    PARTICIPANT_NATIONALITY_REQUIRED,
    PARTICIPANT_AGE_CATEGORY_REQUIRED,
    PARTICIPANT_MEDICAL_CERT_REQUIRED,
    PARTICIPANT_POINTS_NEGATIVE,
    PARTICIPANT_FOREIGN_FEDERATION_REQUIRED,

    // ========================================================================
    // COMPETITION - TABLEAU (définition / paramétrage)
    // ========================================================================

    // -------- TABLEAU (DEFINITION) --------
    TABLEAU_CODE_REQUIRED,
    TABLEAU_DESIGNATION_REQUIRED,
    TABLEAU_DATE_REQUIRED,
    TABLEAU_GENDER_POLICY_REQUIRED,
    TABLEAU_POINTS_RULE_TYPE_REQUIRED,

    // ---- points min/max ----
    TABLEAU_MIN_POINTS_REQUIRED,
    TABLEAU_MAX_POINTS_REQUIRED,
    TABLEAU_MIN_POINTS_NEGATIVE,
    TABLEAU_MAX_POINTS_NEGATIVE,
    TABLEAU_MIN_GREATER_THAN_MAX,
    TABLEAU_POINTS_RULE_INCONSISTENT,

    // ---- frais / dotations ----
    TABLEAU_FEE_REQUIRED,
    TABLEAU_FEE_NEGATIVE,
    TABLEAU_PRIZE_REQUIRED,
    TABLEAU_PRIZE_NEGATIVE,
    TABLEAU_PRIZE_TIER_INVALID,
    TABLEAU_PRIZE_INCONSISTENT,

    // ---- horaires ----
    TABLEAU_CHECKIN_TIME_REQUIRED,
    TABLEAU_START_TIME_REQUIRED,
    TABLEAU_CHECKIN_AFTER_START,

    // ---- capacité / file d'attente ----
    TABLEAU_MAX_PLAYERS_INVALID,
    TABLEAU_FULL,
    TABLEAU_WAITLIST_CAPACITY_INVALID,
    TABLEAU_WAITLIST_FULL,

    // ---- catégories d'âge ----
    TABLEAU_AGE_POLICY_INVALID,

    // ========================================================================
    // COMPETITION - TOURNAMENT (agrégat principal)
    // ========================================================================

    // -------- TOURNAMENT (base) --------
    TOURNAMENT_REQUIRED,
    TOURNAMENT_NAME_REQUIRED,
    TOURNAMENT_ORGANIZING_CLUB_REQUIRED,
    TOURNAMENT_LEVEL_REQUIRED,
    TOURNAMENT_RANKING_PHASE_REQUIRED,
    TOURNAMENT_DAYS_REQUIRED,

    // -------- TOURNAMENT (tableaux) --------
    TOURNAMENT_TABLEAU_REQUIRED,
    TOURNAMENT_TABLEAU_CODE_DUPLICATE,
    TOURNAMENT_TABLEAU_DATE_NOT_IN_TOURNAMENT_DAYS,

    // -------- TOURNAMENT (règle féminine) --------
    TOURNAMENT_FEMALE_EXTRA_RULE_REQUIRED,
    TOURNAMENT_FEMALE_EXTRA_TABLEAU_CODE_REQUIRED,

    // -------- TOURNAMENT (règles d’inscription) --------
    TOURNAMENT_REGISTRATION_POLICY_REQUIRED,
    TOURNAMENT_MAX_TABLEAUX_PER_DAY_INVALID,
    TOURNAMENT_MAX_TOTAL_TABLEAUX_INVALID,
    TOURNAMENT_MAX_TOTAL_TABLEAUX_TOO_LOW,

    // -------- TOURNAMENT (policy participants) --------
    TOURNAMENT_PARTICIPANT_POLICY_REQUIRED,

    // ========================================================================
    // COMPETITION - REGISTRATION (inscriptions)
    // ========================================================================

    // -------- REGISTRATION (base) --------
    REGISTRATION_REQUIRED,
    REGISTRATION_INVALID,
    REGISTRATION_TABLEAU_NOT_FOUND,
    REGISTRATION_ALREADY_REGISTERED,
    REGISTRATION_NOT_ELIGIBLE,

    // -------- REGISTRATION (multi / limites) --------
    REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED,
    REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED,
    REGISTRATION_TOO_MANY_FEMALE_ONLY_TABLEAUX_PER_DAY,

    // -------- REGISTRATION (santé) --------
    REGISTRATION_MEDICAL_CERT_INVALID,

    // -------- REGISTRATION (paiement / statut / batch) --------
    REGISTRATION_PAYMENT_MODE_REQUIRED,
    REGISTRATION_STATUS_REQUIRED,
    REGISTRATION_BATCH_ID_REQUIRED,
    REGISTRATION_BATCH_NOT_FOUND,
    REGISTRATION_RESERVATION_EXPIRED,

    // -------- REGISTRATION (restrictions par niveau) --------
    REGISTRATION_PLAYER_NOT_IN_DEPARTEMENT,
    REGISTRATION_PLAYER_NOT_IN_REGION,
    REGISTRATION_LEVEL_INTERNATIONAL_NOT_SUPPORTED,

    // -------- REGISTRATION (eligibility / policy) --------
    REGISTRATION_GUEST_NOT_ALLOWED,
    REGISTRATION_FOREIGN_NOT_ALLOWED,
    REGISTRATION_FOREIGN_COUNTRY_NOT_ALLOWED,

    // ========================================================================
    // REGULATION / PUBLICATION (règlement officiel)
    // ========================================================================

    TOURNAMENT_REGULATION_INFO_REQUIRED,
    TOURNAMENT_HOMOLOGATION_REQUIRED_FOR_PUBLICATION,

    TOURNAMENT_ORGANIZER_CONTACT_REQUIRED,
    TOURNAMENT_VENUE_REQUIRED,
    TOURNAMENT_TABLE_COUNT_INVALID,

    TOURNAMENT_PLAYING_AREA_REQUIRED,
    TOURNAMENT_PLAYING_AREA_INCOMPATIBLE_LEVEL,
    TOURNAMENT_PLAYING_AREA_NOT_COMPLIANT,
    TOURNAMENT_PLAYING_AREA_CUSTOM_INFO_REQUIRED,
    TOURNAMENT_PLAYING_AREA_DIMENSIONS_INCOMPLETE,
    TOURNAMENT_PLAYING_AREA_DIMENSIONS_INVALID,

    TOURNAMENT_BALL_INFO_REQUIRED,

    TOURNAMENT_REGISTRATION_DEADLINE_REQUIRED,
    TOURNAMENT_CHECKIN_DEADLINE_REQUIRED,
    TOURNAMENT_FIRST_MATCH_START_REQUIRED,
    TOURNAMENT_TIMELINE_INCONSISTENT,

    TOURNAMENT_EXPECTED_END_TIME_REQUIRED,

    // ========================================================================
    // JA / ARBITRAGE
    // ========================================================================

    TOURNAMENT_JA_REQUIRED,
    TOURNAMENT_JA_DUPLICATE,
    TOURNAMENT_JA_GRADE_REQUIRED,
    TOURNAMENT_JA_GRADE_TOO_LOW_FOR_LEVEL,

    // ========================================================================
    // POULE (phase de groupes)
    // ========================================================================

    // ---- structure ----
    POOL_INVALID_SIZE,
    POOL_DUPLICATE_PARTICIPANT,
    POOL_NOT_ALL_MATCHES_FINISHED,

    // ---- match ----
    POOL_MATCH_NOT_FOUND,
    POOL_MATCH_SAME_PARTICIPANT,
    POOL_MATCH_ALREADY_FINISHED,
    POOL_MATCH_INVALID_TRANSITION,
    POOL_MATCH_PARTICIPANT_NOT_IN_MATCH,

    // ---- score ----
    POOL_MATCH_SCORE_EMPTY,
    POOL_MATCH_SCORE_INVALID,
    POOL_MATCH_SCORE_INVALID_SET,
    POOL_MATCH_SCORE_NEGATIVE_POINTS,
    POOL_MATCH_SCORE_SET_NOT_FINISHED,
    POOL_MATCH_SCORE_SET_INVALID_DEUCE,
    POOL_MATCH_SCORE_TOO_MANY_SETS,
    POOL_MATCH_SCORE_NOT_FINISHED,

    // ========================================================================
    // DRAW (algorithme de tirage des poules)
    // ========================================================================

    DRAW_NOT_ENOUGH_PLAYERS,
    DRAW_ALGORITHM_REQUIRED,

    // ========================================================================
    // BRACKET KO (tableau à élimination directe)
    // ========================================================================

    BRACKET_NO_QUALIFIED_PLAYERS,
    BRACKET_MATCH_NOT_FOUND,
    BRACKET_MATCH_ALREADY_FINISHED,
    BRACKET_MATCH_INVALID_TRANSITION,
    BRACKET_MATCH_PARTICIPANT_NOT_IN_MATCH,
    TABLEAU_POOL_SIZE_INVALID,
    TABLEAU_QUALIFIED_PER_POOL_INVALID,

    BRACKET_MATCH_SCORE_REQUIRED,

    // ========================================================================
    // CLASSIFICATION (matchs de classement)
    // ========================================================================

    CLASSIFICATION_KO_NOT_COMPLETE,
    CLASSIFICATION_MATCH_NOT_FOUND,
    CLASSIFICATION_MATCH_SAME_PARTICIPANT,
    CLASSIFICATION_MATCH_ALREADY_FINISHED,
    CLASSIFICATION_MATCH_INVALID_TRANSITION,
    CLASSIFICATION_MATCH_PARTICIPANT_NOT_IN_MATCH
}