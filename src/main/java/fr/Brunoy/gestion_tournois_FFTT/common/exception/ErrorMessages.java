package fr.Brunoy.gestion_tournois_FFTT.common.exception;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    public static String message(ErrorCode code) {
        return switch (code) {

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

            // -------- TABLEAU --------
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
            case TABLEAU_FEE_REQUIRED ->
                "Les droits d'inscription du tableau sont obligatoires";
            case TABLEAU_FEE_NEGATIVE ->
                "Les droits d'inscription ne peuvent pas être négatifs";
            case TABLEAU_CHECKIN_TIME_REQUIRED ->
                "L'heure de fin de pointage est obligatoire";
            case TABLEAU_START_TIME_REQUIRED ->
                "L'heure de début du tableau est obligatoire";
            case TABLEAU_CHECKIN_AFTER_START ->
                "La fin du pointage doit être antérieure au début du tableau";
            case TABLEAU_PRIZE_REQUIRED ->
                "La répartition des récompenses est obligatoire";
            case TABLEAU_PRIZE_NEGATIVE ->
                "Les récompenses ne peuvent pas être négatives";

            // -------- TABLEAU (CAPACITE / INSCRIPTION) --------
            case TABLEAU_MAX_PLAYERS_INVALID ->
                "Le nombre maximum de joueurs du tableau doit être strictement positif";
            case TABLEAU_FULL ->
                "Le tableau est complet";

            // -------- TOURNAMENT --------
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
            case TOURNAMENT_TABLEAU_REQUIRED ->
                "Un tableau est obligatoire pour l'ajouter au tournoi";
            case TOURNAMENT_TABLEAU_CODE_DUPLICATE ->
                "Un tableau avec ce code existe déjà dans le tournoi";
            case TOURNAMENT_TABLEAU_DATE_NOT_IN_TOURNAMENT_DAYS ->
                "La date du tableau ne fait pas partie des jours du tournoi";
            case TOURNAMENT_FEMALE_EXTRA_RULE_REQUIRED ->
                "La règle d'inscription supplémentaire féminine du tournoi est obligatoire";
            case TOURNAMENT_FEMALE_EXTRA_TABLEAU_CODE_REQUIRED ->
                "Le code du tableau féminin supplémentaire est obligatoire lorsque la règle est spécifique";

            // -------- TOURNAMENT (REGLES D’INSCRIPTION) --------
            case TOURNAMENT_REGISTRATION_POLICY_REQUIRED ->
                "La politique d'inscription du tournoi est obligatoire";
            case TOURNAMENT_MAX_TABLEAUX_PER_DAY_INVALID ->
                "Le nombre maximum de tableaux par jour doit être strictement positif";
            case TOURNAMENT_MAX_TOTAL_TABLEAUX_INVALID ->
                "Le nombre maximum total de tableaux doit être strictement positif";
            case TOURNAMENT_MAX_TOTAL_TABLEAUX_TOO_LOW ->
                "Le nombre maximum total de tableaux doit être supérieur ou égal au maximum par jour";

            // -------- REGISTRATION --------
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

            // -------- REGISTRATION (MULTI) --------
            case REGISTRATION_MAX_TOTAL_TABLEAUX_EXCEEDED ->
                "Le nombre total de tableaux sélectionnés dépasse la limite autorisée par le tournoi";

            case REGISTRATION_MAX_TABLEAUX_PER_DAY_EXCEEDED ->
                "Le nombre de tableaux sélectionnés sur un même jour dépasse la limite autorisée par le tournoi";

            case REGISTRATION_TOO_MANY_FEMALE_ONLY_TABLEAUX_PER_DAY ->
                "Un seul tableau féminin supplémentaire est autorisé par jour";

            case REGISTRATION_MEDICAL_CERT_INVALID ->
                "Le certificat médical du joueur n'est pas valide";

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

            case REGISTRATION_PLAYER_NOT_IN_DEPARTEMENT ->
                "Inscription impossible : tournoi départemental réservé aux joueurs du même département que le club organisateur.";

            case REGISTRATION_PLAYER_NOT_IN_REGION ->
                "Inscription impossible : tournoi régional réservé aux joueurs de la même région que le club organisateur.";

            case REGISTRATION_LEVEL_INTERNATIONAL_NOT_SUPPORTED ->
                "Inscription impossible : la gestion des tournois internationaux n'est pas encore supportée.";

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

            case TOURNAMENT_REGULATION_INFO_REQUIRED ->
                "Les informations de règlement du tournoi sont obligatoires.";
        };
    }
}
