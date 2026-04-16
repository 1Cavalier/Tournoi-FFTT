package fr.pingmanager.gestion_tournois_FFTT.domain.competition.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.BallProvisionPolicy;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value.PlayingAreaSpec;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value.TournamentRegulationInfo;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Club;

/**
 * Génère un TournamentRegulationInfo par défaut à partir du club organisateur.
 * Objectif : créer un DRAFT avec des infos plausibles, puis compléter avant
 * publication.
 */
public final class TournamentRegulationInfoDefaults {

    private TournamentRegulationInfoDefaults() {
    }

    /**
     * Version "safe" : ne devine pas d'horaires, ne met pas de valeurs arbitraires.
     * Remplit :
     * - salle / ville depuis Club
     * - aire de jeu standard selon niveau
     * - policy balles par défaut (modifiable)
     *
     * L'email/téléphone et d'autres champs restent null si non présents dans Club.
     */
    public static TournamentRegulationInfo fromClub(Club club, TournamentLevel level) {
        Objects.requireNonNull(club, "club obligatoire");
        Objects.requireNonNull(level, "level obligatoire");

        String venueName = firstNonBlank(club.getAddress1(), club.getName());
        String venueStreet = club.getAddress2(); // souvent complément (entrée, bâtiment...)
        String venueCity = club.getCity();

        // Pas de zip dans Club : on laisse null
        String venueZip = null;

        // Contact : Club ne contient pas email/tel -> null (à compléter avant
        // publication)
        String organizerContactName = club.getName();
        String organizerEmail = null;
        String organizerPhone = null;

        // Defaults raisonnables (draft) : 0 force le renseignement avant règlement
        // officiel
        int numberOfTables = 0;

        PlayingAreaSpec playingArea = PlayingAreaSpec.standardFor(level);

        // Balles : club ne stocke pas la marque -> null, mais policy par défaut
        String ballBrandAndType = null;
        BallProvisionPolicy ballProvisionPolicy = BallProvisionPolicy.PROVIDED_BY_CLUB;

        // Dates : on ne devine pas
        LocalDateTime registrationDeadline = null;
        LocalDateTime checkInDeadline = null;
        LocalDateTime firstMatchesStart = null;

        // Fin prévisionnelle : on ne devine pas
        LocalTime expectedEndTime = null;

        return TournamentRegulationInfo.draft(
                null, // homologationNumber (null tant que pas validé)
                organizerContactName,
                organizerEmail,
                organizerPhone,
                venueName,
                venueStreet,
                venueZip,
                venueCity,
                numberOfTables,
                playingArea,
                ballBrandAndType,
                ballProvisionPolicy,
                registrationDeadline,
                checkInDeadline,
                firstMatchesStart,
                expectedEndTime);
    }

    /**
     * Variante : tu fournis des horaires par défaut (si tu veux auto-remplir dès la
     * création).
     * Utile si tu veux un "wizard" qui pré-remplit.
     */
    public static TournamentRegulationInfo fromClubWithDefaultTimes(
            Club club,
            TournamentLevel level,
            LocalDateTime registrationDeadline,
            LocalDateTime checkInDeadline,
            LocalDateTime firstMatchesStart,
            LocalTime expectedEndTime,
            int numberOfTables) {
        Objects.requireNonNull(club, "club obligatoire");
        Objects.requireNonNull(level, "level obligatoire");

        String venueName = firstNonBlank(club.getAddress1(), club.getName());
        String venueStreet = club.getAddress2();
        String venueCity = club.getCity();
        String venueZip = null;

        String organizerContactName = club.getName();

        PlayingAreaSpec playingArea = PlayingAreaSpec.standardFor(level);

        return TournamentRegulationInfo.draft(
                null,
                organizerContactName,
                null,
                null,
                venueName,
                venueStreet,
                venueZip,
                venueCity,
                Math.max(0, numberOfTables),
                playingArea,
                null,
                BallProvisionPolicy.PROVIDED_BY_CLUB,
                registrationDeadline,
                checkInDeadline,
                firstMatchesStart,
                expectedEndTime);
    }

    private static String firstNonBlank(String a, String b) {
        String na = normalize(a);
        return na != null ? na : normalize(b);
    }

    private static String normalize(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}