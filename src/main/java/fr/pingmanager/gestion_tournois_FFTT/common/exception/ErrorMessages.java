package fr.pingmanager.gestion_tournois_FFTT.common.exception;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String message(ErrorCode code) {
        return switch (code) {

            // ========================================================================
            // REF DATA / ORGANIZATION
            // ========================================================================

            // -------- REGION --------
            case REGION_CODE_REQUIRED ->
                "Le code de la région est obligatoire";
            case REGION_NAME_REQUIRED ->
                "Le nom de la région est obligatoire";

            // -------- DEPARTEMENT --------
            case DEPARTEMENT_CODE_REQUIRED ->
                "Le code du département est obligatoire";
            case DEPARTEMENT_NAME_REQUIRED ->
                "Le nom du département est obligatoire";
            case DEPARTEMENT_REGION_REQUIRED ->
                "Le département doit être rattaché à une région";

            // -------- CLUB --------
            case CLUB_NUMBER_REQUIRED ->
                "Le numéro du club est obligatoire";
            case CLUB_NAME_REQUIRED ->
                "Le nom du club est obligatoire";
            case CLUB_CITY_REQUIRED ->
                "La ville du club est obligatoire";
            case CLUB_DEPARTEMENT_REQUIRED ->
                "Le club doit être rattaché à un département";
            case CLUB_GEO_COORDINATES_INCOMPLETE ->
                "Latitude et longitude doivent être renseignées ensemble (ou toutes les deux null).";
            case CLUB_GEO_COORDINATES_INVALID ->
                "Coordonnées géographiques invalides : latitude doit être entre -90 et 90, longitude entre -180 et 180.";

            // ========================================================================
            // IDENTITY
            // ========================================================================

            // -------- OFFICIAL / QUALIFICATION --------
            case OFFICIAL_ROLE_REQUIRED ->
                "Le rôle officiel est obligatoire";
            case OFFICIAL_GRADE_REQUIRED ->
                "Le grade officiel est obligatoire";
            case OFFICIAL_INCONSISTENT_GRADE ->
                "Le grade fourni n'est pas compatible avec le rôle officiel";

            // -------- PLAYER --------
            case PLAYER_REQUIRED ->
                "Le joueur est obligatoire";
            case PLAYER_LICENSE_REQUIRED ->
                "Le numéro de licence est obligatoire";
            case PLAYER_FIRST_NAME_REQUIRED ->
                "Le prénom est obligatoire";
            case PLAYER_LAST_NAME_REQUIRED ->
                "Le nom est obligatoire";
            case PLAYER_GENDER_REQUIRED ->
                "Le genre du joueur est obligatoire";
            case PLAYER_NATIONALITY_REQUIRED ->
                "La nationalité est obligatoire";
            case PLAYER_CLUB_REQUIRED ->
                "Le club du joueur est obligatoire";
            case PLAYER_AGE_CATEGORY_REQUIRED ->
                "La catégorie d'âge est obligatoire";
            case PLAYER_LICENSE_TYPE_REQUIRED ->
                "Le type de licence est obligatoire";
            case PLAYER_MEDICAL_CERT_REQUIRED ->
                "Le statut du certificat médical est obligatoire";
            case PLAYER_POINTS_NEGATIVE ->
                "Les points du joueur ne peuvent pas être négatifs";
            case PLAYER_QUALIFICATION_REQUIRED ->
                "La qualification officielle du joueur est obligatoire";

            // -------- PARTICIPANT --------
            case PARTICIPANT_REQUIRED ->
                "Le participant est obligatoire";
            case PARTICIPANT_ID_REQUIRED ->
                "L'identifiant du participant est obligatoire";
            case PARTICIPANT_NAME_REQUIRED ->
                "Le nom complet du participant est obligatoire";
            case PARTICIPANT_GENDER_REQUIRED ->
                "Le genre du participant est obligatoire";
            case PARTICIPANT_NATIONALITY_REQUIRED ->
                "La nationalité du participant est obligatoire";
            case PARTICIPANT_AGE_CATEGORY_REQUIRED ->
                "La catégorie d'âge du participant est obligatoire";
            case PARTICIPANT_MEDICAL_CERT_REQUIRED ->
                "Le statut du certificat médical du participant est obligatoire";
            case PARTICIPANT_POINTS_NEGATIVE ->
                "Les points du participant ne peuvent pas être négatifs";
            case PARTICIPANT_FOREIGN_FEDERATION_REQUIRED ->
                "Les informations de fédération étrangère sont obligatoires";

            // ========================================================================
            // TABLEAU
            // ========================================================================

            // -------- TABLEAU (DEFINITION) --------
            case TABLEAU_CODE_REQUIRED ->
                "Le code du tableau est obligatoire";
            case TABLEAU_DESIGNATION_REQUIRED ->
                "La désignation du tableau est obligatoire";
            case TABLEAU_DATE_REQUIRED ->
                "La date du tableau est obligatoire";
            case TABLEAU_GENDER_POLICY_REQUIRED ->
                "La règle de genre du tableau est obligatoire";
            case TABLEAU_POINTS_RULE_TYPE_REQUIRED ->
                "Le type de règle de points du tableau est obligatoire";

            // ---- points min/max ----
            case TABLEAU_MIN_POINTS_REQUIRED ->
                "Le nombre minimum de points est obligatoire";
            case TABLEAU_MAX_POINTS_REQUIRED ->
                "Le nombre maximum de points est obligatoire";
            case TABLEAU_MIN_POINTS_NEGATIVE ->
                "Le nombre minimum de points ne peut pas être négatif";
            case TABLEAU_MAX_POINTS_NEGATIVE ->
                "Le nombre maximum de points ne peut pas être négatif";
            case TABLEAU_MIN_GREATER_THAN_MAX ->
                "Le nombre minimum de points ne peut pas être supérieur au maximum";
            case TABLEAU_POINTS_RULE_INCONSISTENT ->
                "Les règles de points du tableau sont incohérentes";

            // ---- frais / dotations ----
            case TABLEAU_FEE_REQUIRED ->
                "Les droits d'inscription du tableau sont obligatoires";
            case TABLEAU_FEE_NEGATIVE ->
                "Les droits d'inscription ne peuvent pas être négatifs";
            case TABLEAU_PRIZE_REQUIRED ->
                "La répartition des récompenses est obligatoire";
            case TABLEAU_PRIZE_NEGATIVE ->
                "Les récompenses ne peuvent pas être négatives";
            case TABLEAU_PRIZE_TIER_INVALID ->
                "Répartition des lots invalide : la plage de classement est incorrecte (ex : 1-2, 3-4, etc.).";
            case TABLEAU_PRIZE_INCONSISTENT ->
                "Répartition des lots incohérente : les plages se chevauchent ou contiennent une valeur invalide.";

            // ---- horaires ----
            case TABLEAU_CHECKIN_TIME_REQUIRED ->
                "L'heure de fin de pointage est obligatoire";
            case TABLEAU_START_TIME_REQUIRED ->
                "L'heure de début du tableau est obligatoire";
            case TABLEAU_CHECKIN_AFTER_START ->
                "La fin du pointage doit être antérieure au début du tableau";

            // ---- capacité / file d'attente ----
            case TABLEAU_MAX_PLAYERS_INVALID ->
                "Le nombre maximum de joueurs du tableau doit être strictement positif";
            case TABLEAU_FULL ->
                "Le tableau est complet";
            case TABLEAU_WAITLIST_CAPACITY_INVALID ->
                "La capacité de la file d'attente du tableau ne peut pas être négative";
            case TABLEAU_WAITLIST_FULL ->
                "Le tableau est complet et la file d'attente est pleine";

            // ---- catégories d'âge ----
            case TABLEAU_AGE_POLICY_INVALID ->
                "La restriction de catégorie d'âge du tableau est invalide";

            // ========================================================================
            // TOURNAMENT
            // ========================================================================

            // -------- TOURNAMENT (base) --------
            case TOURNAMENT_REQUIRED ->
                "Le tournoi est obligatoire";
            case TOURNAMENT_NAME_REQUIRED ->
                "Le nom du tournoi est obligatoire";
            case TOURNAMENT_ORGANIZING_CLUB_REQUIRED ->
                "Le club organisateur du tournoi est obligatoire";
            case TOURNAMENT_LEVEL_REQUIRED ->
                "Le niveau du tournoi est obligatoire";
            case TOURNAMENT_RANKING_PHASE_REQUIRED ->
                "La phase de classement du tournoi est obligatoire";
            case TOURNAMENT_DAYS_REQUIRED ->
                "Le tournoi doit se dérouler sur au moins un jour";

            // -------- TOURNAMENT (tableaux) --------
            case TOURNAMENT_TABLEAU_REQUIRED ->
                "Un tableau est obligatoire pour l'ajouter au tournoi";
            case TOURNAMENT_TABLEAU_CODE_DUPLICATE ->
                "Un tableau avec ce code existe déjà dans le tournoi";
            case TOURNAMENT_TABLEAU_DATE_NOT_IN_TOURNAMENT_DAYS ->
                "La date du tableau ne fait pas partie des jours du tournoi";

            // -------- TOURNAMENT (règle féminine) --------
            case TOURNAMENT_FEMALE_EXTRA_RULE_REQUIRED ->
                "La règle d'inscription supplémentaire féminine du tournoi est obligatoire";
            case TOURNAMENT_FEMALE_EXTRA_TABLEAU_CODE_REQUIRED ->
                "Le code du tableau féminin supplémentaire est obligatoire lorsque la règle est spécifique";

            // -------- TOURNAMENT (règles d’inscription) --------
            case TOURNAMENT_REGISTRATION_POLICY_REQUIRED ->
                "La politique d'inscription du tournoi est obligatoire";
            case TOURNAMENT_MAX_TABLEAUX_PER_DAY_INVALID ->
                "Le nombre maximum de tableaux par jour doit être strictement positif";
            case TOURNAMENT_MAX_TOTAL_TABLEAUX_INVALID ->
                "Le nombre maximum total de tableaux doit être strictement positif";
            case TOURNAMENT_MAX_TOTAL_TABLEAUX_TOO_LOW ->
                "Le nombre maximum total de tableaux doit être supérieur ou égal au maximum par jour";

            // -------- TOURNAMENT (policy participants) --------
            case TOURNAMENT_PARTICIPANT_POLICY_REQUIRED ->
                "La politique de participants du tournoi est obligatoire";

            // ========================================================================
            // REGISTRATION
            // ========================================================================

            // -------- REGISTRATION (base) --------
            case REGISTRATION_REQUIRED ->
                "La demande d'inscription est obligatoire";
            case REGISTRATION_INVALID ->
                "La demande d'inscription n'est pas valide";
            case REGISTRATION_TABLEAU_NOT_FOUND ->
                "Le tableau demandé est introuvable";
            case REGISTRATION_ALREADY_REGISTERED ->
                "Le joueur est déjà inscrit à ce tableau";
            case REGISTRATION_NOT_ELIGIBLE ->
                "Le joueur n'est pas éligible pour ce tableau";

            // -------- REGISTRATION (multi / limites) --------
            case REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED ->
                "Le nombre total de tableaux sélectionnés dépasse la limite autorisée par le tournoi";
            case REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED ->
                "Le nombre de tableaux sélectionnés sur un même jour dépasse la limite autorisée par le tournoi";
            case REGISTRATION_TOO_MANY_FEMALE_ONLY_TABLEAUX_PER_DAY ->
                "Un seul tableau féminin supplémentaire est autorisé par jour";

            // -------- REGISTRATION (santé) --------
            case REGISTRATION_MEDICAL_CERT_INVALID ->
                "Le certificat médical du joueur n'est pas valide";

            // -------- REGISTRATION (paiement / statut / batch) --------
            case REGISTRATION_PAYMENT_MODE_REQUIRED ->
                "Le mode de paiement est obligatoire";
            case REGISTRATION_STATUS_REQUIRED ->
                "Le statut d'inscription est obligatoire";
            case REGISTRATION_BATCH_ID_REQUIRED ->
                "L'identifiant d'inscription est obligatoire";
            case REGISTRATION_BATCH_NOT_FOUND ->
                "Aucune inscription correspondante n'a été trouvée";
            case REGISTRATION_RESERVATION_EXPIRED ->
                "Le délai de paiement a expiré : l'inscription a été annulée, veuillez recommencer";

            // -------- REGISTRATION (restrictions par niveau) --------
            case REGISTRATION_PLAYER_NOT_IN_DEPARTEMENT ->
                "Inscription impossible : tournoi départemental réservé aux joueurs du même département que le club organisateur.";
            case REGISTRATION_PLAYER_NOT_IN_REGION ->
                "Inscription impossible : tournoi régional réservé aux joueurs de la même région que le club organisateur.";
            case REGISTRATION_LEVEL_INTERNATIONAL_NOT_SUPPORTED ->
                "Inscription impossible : la gestion des tournois internationaux n'est pas encore supportée.";

            // -------- REGISTRATION (eligibility / policy) --------
            case REGISTRATION_GUEST_NOT_ALLOWED ->
                "Inscription impossible : les non-licenciés ne sont pas autorisés pour ce tournoi";
            case REGISTRATION_FOREIGN_NOT_ALLOWED ->
                "Inscription impossible : les joueurs étrangers ne sont pas autorisés pour ce tournoi";
            case REGISTRATION_FOREIGN_COUNTRY_NOT_ALLOWED ->
                "Inscription impossible : ce pays n'est pas autorisé pour les joueurs étrangers sur ce tournoi";

            // ========================================================================
            // REGULATION / PUBLICATION
            // ========================================================================

            case TOURNAMENT_REGULATION_INFO_REQUIRED ->
                "Les informations de règlement du tournoi sont obligatoires.";
            case TOURNAMENT_HOMOLOGATION_REQUIRED_FOR_PUBLICATION ->
                "Impossible de publier / générer un règlement conforme : le numéro d'homologation est manquant";

            case TOURNAMENT_ORGANIZER_CONTACT_REQUIRED ->
                "Les coordonnées de l'organisateur (au minimum l'email) sont obligatoires pour le règlement";
            case TOURNAMENT_VENUE_REQUIRED ->
                "La salle (nom du gymnase) est obligatoire pour le règlement";
            case TOURNAMENT_TABLE_COUNT_INVALID ->
                "Le nombre de tables doit être strictement positif";

            case TOURNAMENT_PLAYING_AREA_REQUIRED ->
                "Les informations d'aires de jeu sont obligatoires pour le règlement";
            case TOURNAMENT_PLAYING_AREA_INCOMPATIBLE_LEVEL ->
                "L'aire de jeu choisie n'est pas compatible avec le niveau du tournoi";
            case TOURNAMENT_PLAYING_AREA_CUSTOM_INFO_REQUIRED ->
                "En configuration personnalisée, la description des aires de jeu est obligatoire";
            case TOURNAMENT_PLAYING_AREA_DIMENSIONS_INCOMPLETE ->
                "Les dimensions d'aire de jeu doivent être renseignées complètement (longueur et largeur)";
            case TOURNAMENT_PLAYING_AREA_DIMENSIONS_INVALID ->
                "Les dimensions d'aire de jeu doivent être strictement positives";
            case TOURNAMENT_PLAYING_AREA_NOT_COMPLIANT ->
                "Impossible de publier : les aires de jeu sont déclarées non conformes à la réglementation.";

            case TOURNAMENT_BALL_INFO_REQUIRED ->
                "Les informations sur les balles (marque/type + fourniture) sont obligatoires pour le règlement";

            case TOURNAMENT_REGISTRATION_DEADLINE_REQUIRED ->
                "La date limite d'engagement est obligatoire";
            case TOURNAMENT_CHECKIN_DEADLINE_REQUIRED ->
                "La fin du pointage est obligatoire";
            case TOURNAMENT_FIRST_MATCH_START_REQUIRED ->
                "L'heure de début des matchs est obligatoire";
            case TOURNAMENT_TIMELINE_INCONSISTENT ->
                "Les dates/horaires du règlement sont incohérents (deadline, pointage, début)";

            case TOURNAMENT_EXPECTED_END_TIME_REQUIRED ->
                "L'horaire de fin prévisionnel est obligatoire";

            // ========================================================================
            // JA / ARBITRAGE
            // ========================================================================

            case TOURNAMENT_JA_REQUIRED ->
                "Au moins un juge-arbitre (JA) doit être désigné pour publier officiellement le tournoi";
            case TOURNAMENT_JA_DUPLICATE ->
                "Ce juge-arbitre (JA) est déjà affecté au tournoi";
            case TOURNAMENT_JA_GRADE_REQUIRED ->
                "Le grade du juge-arbitre (JA) est obligatoire";
            case TOURNAMENT_JA_GRADE_TOO_LOW_FOR_LEVEL ->
                "Aucun juge-arbitre désigné n'a le grade requis pour le niveau du tournoi";

            // ========================================================================
            // POULE (phase de groupes)
            // ========================================================================

            // ---- structure ----
            case POOL_INVALID_SIZE ->
                "Une poule doit contenir 2 ou 3 joueurs";
            case POOL_DUPLICATE_PARTICIPANT ->
                "Un participant ne peut pas figurer deux fois dans la même poule";
            case POOL_NOT_ALL_MATCHES_FINISHED ->
                "Impossible de calculer le classement : tous les matchs de la poule ne sont pas terminés";

            // ---- match ----
            case POOL_MATCH_NOT_FOUND ->
                "Le match demandé est introuvable dans cette poule";
            case POOL_MATCH_SAME_PARTICIPANT ->
                "Un joueur ne peut pas s'affronter lui-même";
            case POOL_MATCH_ALREADY_FINISHED ->
                "Ce match est déjà terminé, le score ne peut plus être modifié";
            case POOL_MATCH_INVALID_TRANSITION ->
                "Transition de statut invalide pour ce match";
            case POOL_MATCH_PARTICIPANT_NOT_IN_MATCH ->
                "Ce participant ne fait pas partie de ce match";

            // ---- score ----
            case POOL_MATCH_SCORE_EMPTY ->
                "Le score doit contenir au moins une manche";
            case POOL_MATCH_SCORE_INVALID ->
                "Le score du match est invalide";
            case POOL_MATCH_SCORE_INVALID_SET ->
                "Une manche doit contenir exactement deux valeurs (points joueur 1, points joueur 2)";
            case POOL_MATCH_SCORE_NEGATIVE_POINTS ->
                "Les points d'une manche ne peuvent pas être négatifs";
            case POOL_MATCH_SCORE_SET_NOT_FINISHED ->
                "Une manche doit être jouée jusqu'à au moins 11 points";
            case POOL_MATCH_SCORE_SET_INVALID_DEUCE ->
                "En cas d'égalité à 10-10, la manche se joue avec 2 points d'écart exact";
            case POOL_MATCH_SCORE_TOO_MANY_SETS ->
                "Un match ne peut pas dépasser 5 manches (best of 5)";
            case POOL_MATCH_SCORE_NOT_FINISHED ->
                "Le match n'est pas terminé : aucun des deux joueurs n'a atteint 3 manches gagnées";

            // ========================================================================
            // DRAW (algorithme de tirage des poules)
            // ========================================================================

            case DRAW_NOT_ENOUGH_PLAYERS ->
                "Impossible de constituer les poules : pas assez de joueurs inscrits";
            case DRAW_ALGORITHM_REQUIRED ->
                "L'algorithme de tirage des poules est obligatoire";

            // ========================================================================
            // BRACKET KO (tableau à élimination directe)
            // ========================================================================

            case BRACKET_NO_QUALIFIED_PLAYERS ->
                "Impossible de construire le tableau KO : aucun joueur qualifié";
            case BRACKET_MATCH_NOT_FOUND ->
                "Le match demandé est introuvable dans le tableau";
            case BRACKET_MATCH_ALREADY_FINISHED ->
                "Ce match KO est déjà terminé, le score ne peut plus être modifié";
            case BRACKET_MATCH_INVALID_TRANSITION ->
                "Transition de statut invalide pour ce match KO";
            case BRACKET_MATCH_PARTICIPANT_NOT_IN_MATCH ->
                "Ce participant ne fait pas partie de ce match KO";
            case BRACKET_MATCH_SCORE_REQUIRED ->
                "Le score est obligatoire pour clôturer ce match KO";
        };
    }
}