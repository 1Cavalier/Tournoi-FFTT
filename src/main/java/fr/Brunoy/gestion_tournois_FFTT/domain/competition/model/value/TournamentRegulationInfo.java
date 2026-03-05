package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.BallProvisionPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Snapshot d'infos nécessaires pour générer un règlement FFTT conforme.
 * Rempli par défaut depuis le club/organisateur, mais modifiable pour ce
 * tournoi.
 *
 * - Draft : validation légère (ne bloque pas tant que le tournoi n'est pas
 * "publiable")
 * - Publication : validation stricte via validateCompleteForRegulation(...)
 */
public final class TournamentRegulationInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    // HOMOLOGATION
    // -------------------------------------------------------------------------

    /** Nullable tant que pas validé / pas attribué. */
    private final String homologationNumber;

    // -------------------------------------------------------------------------
    // ORGANISATEUR (snapshot)
    // -------------------------------------------------------------------------

    private final String organizerContactName; // optionnel
    private final String organizerEmail; // requis pour règlement complet
    private final String organizerPhone; // optionnel

    // -------------------------------------------------------------------------
    // SALLE (snapshot)
    // -------------------------------------------------------------------------

    private final String venueName; // requis pour règlement complet
    private final String venueStreet; // optionnel
    private final String venueZip; // optionnel
    private final String venueCity; // optionnel

    // -------------------------------------------------------------------------
    // MATERIEL / AIRE / BALLES
    // -------------------------------------------------------------------------

    private final int numberOfTables; // requis (>0) pour règlement complet
    private final PlayingAreaSpec playingArea; // requis pour règlement complet

    private final String ballBrandAndType; // requis pour règlement complet
    private final BallProvisionPolicy ballProvisionPolicy; // requis

    // -------------------------------------------------------------------------
    // DATES CLES
    // -------------------------------------------------------------------------

    private final LocalDateTime registrationDeadline; // requis
    private final LocalDateTime checkInDeadline; // requis (fin pointage)
    private final LocalDateTime firstMatchesStart; // requis (début matchs)

    // Fin prévisionnelle
    private final LocalTime expectedEndTime; // requis

    private TournamentRegulationInfo(
            String homologationNumber,
            String organizerContactName,
            String organizerEmail,
            String organizerPhone,
            String venueName,
            String venueStreet,
            String venueZip,
            String venueCity,
            int numberOfTables,
            PlayingAreaSpec playingArea,
            String ballBrandAndType,
            BallProvisionPolicy ballProvisionPolicy,
            LocalDateTime registrationDeadline,
            LocalDateTime checkInDeadline,
            LocalDateTime firstMatchesStart,
            LocalTime expectedEndTime) {

        this.homologationNumber = normalize(homologationNumber);

        this.organizerContactName = normalize(organizerContactName);
        this.organizerEmail = normalize(organizerEmail);
        this.organizerPhone = normalize(organizerPhone);

        this.venueName = normalize(venueName);
        this.venueStreet = normalize(venueStreet);
        this.venueZip = normalize(venueZip);
        this.venueCity = normalize(venueCity);

        this.numberOfTables = numberOfTables;
        this.playingArea = playingArea;

        this.ballBrandAndType = normalize(ballBrandAndType);
        this.ballProvisionPolicy = ballProvisionPolicy;

        this.registrationDeadline = registrationDeadline;
        this.checkInDeadline = checkInDeadline;
        this.firstMatchesStart = firstMatchesStart;

        this.expectedEndTime = expectedEndTime;

        validateDraftRules();
    }

    // -------------------------------------------------------------------------
    // FACTORY
    // -------------------------------------------------------------------------

    public static TournamentRegulationInfo draft(
            String homologationNumber,
            String organizerContactName,
            String organizerEmail,
            String organizerPhone,
            String venueName,
            String venueStreet,
            String venueZip,
            String venueCity,
            int numberOfTables,
            PlayingAreaSpec playingArea,
            String ballBrandAndType,
            BallProvisionPolicy ballProvisionPolicy,
            LocalDateTime registrationDeadline,
            LocalDateTime checkInDeadline,
            LocalDateTime firstMatchesStart,
            LocalTime expectedEndTime) {

        return new TournamentRegulationInfo(
                homologationNumber,
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

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    /**
     * Validation légère (DRAFT) : on évite de tout bloquer tant que le tournoi
     * est en construction.
     */
    private void validateDraftRules() {
        if (numberOfTables < 0) {
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLE_COUNT_INVALID);
        }
        validateTimelineIfPresent();
    }

    /**
     * Vérifie uniquement ce qui est présent (null-safe).
     *
     * Règles (si dates présentes) :
     * - registrationDeadline < checkInDeadline
     * - checkInDeadline < firstMatchesStart
     */
    private void validateTimelineIfPresent() {
        if (registrationDeadline != null && checkInDeadline != null) {
            if (!registrationDeadline.isBefore(checkInDeadline)) {
                throw new BusinessException(ErrorCode.TOURNAMENT_TIMELINE_INCONSISTENT);
            }
        }
        if (checkInDeadline != null && firstMatchesStart != null) {
            if (!checkInDeadline.isBefore(firstMatchesStart)) {
                throw new BusinessException(ErrorCode.TOURNAMENT_TIMELINE_INCONSISTENT);
            }
        }
    }

    /**
     * Validation stricte : à appeler pour générer un règlement conforme / publier.
     *
     * @param homologationRequired true si tu veux interdire publication/export sans
     *                             numéro.
     */
    public void validateCompleteForRegulation(TournamentLevel level, boolean homologationRequired) {
        Objects.requireNonNull(level, "level obligatoire");

        if (homologationRequired && homologationNumber == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_HOMOLOGATION_REQUIRED_FOR_PUBLICATION);
        }

        if (isBlank(organizerEmail)) {
            throw new BusinessException(ErrorCode.TOURNAMENT_ORGANIZER_CONTACT_REQUIRED);
        }
        if (isBlank(venueName)) {
            throw new BusinessException(ErrorCode.TOURNAMENT_VENUE_REQUIRED);
        }
        if (numberOfTables <= 0) {
            throw new BusinessException(ErrorCode.TOURNAMENT_TABLE_COUNT_INVALID);
        }

        if (playingArea == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_PLAYING_AREA_REQUIRED);
        }
        playingArea.validateCompatibleWith(level);

        if (isBlank(ballBrandAndType) || ballProvisionPolicy == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_BALL_INFO_REQUIRED);
        }

        if (registrationDeadline == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_REGISTRATION_DEADLINE_REQUIRED);
        }
        if (checkInDeadline == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_CHECKIN_DEADLINE_REQUIRED);
        }
        if (firstMatchesStart == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_FIRST_MATCH_START_REQUIRED);
        }

        // stricte timeline
        validateTimelineIfPresent();

        if (expectedEndTime == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_EXPECTED_END_TIME_REQUIRED);
        }
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String homologationNumber() {
        return homologationNumber;
    }

    public String organizerContactName() {
        return organizerContactName;
    }

    public String organizerEmail() {
        return organizerEmail;
    }

    public String organizerPhone() {
        return organizerPhone;
    }

    public String venueName() {
        return venueName;
    }

    public String venueStreet() {
        return venueStreet;
    }

    public String venueZip() {
        return venueZip;
    }

    public String venueCity() {
        return venueCity;
    }

    public int numberOfTables() {
        return numberOfTables;
    }

    public PlayingAreaSpec playingArea() {
        return playingArea;
    }

    public String ballBrandAndType() {
        return ballBrandAndType;
    }

    public BallProvisionPolicy ballProvisionPolicy() {
        return ballProvisionPolicy;
    }

    public LocalDateTime registrationDeadline() {
        return registrationDeadline;
    }

    public LocalDateTime checkInDeadline() {
        return checkInDeadline;
    }

    public LocalDateTime firstMatchesStart() {
        return firstMatchesStart;
    }

    public LocalTime expectedEndTime() {
        return expectedEndTime;
    }

    // -------------------------------------------------------------------------
    // VALUE OBJECT
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TournamentRegulationInfo that))
            return false;
        return numberOfTables == that.numberOfTables
                && Objects.equals(homologationNumber, that.homologationNumber)
                && Objects.equals(organizerContactName, that.organizerContactName)
                && Objects.equals(organizerEmail, that.organizerEmail)
                && Objects.equals(organizerPhone, that.organizerPhone)
                && Objects.equals(venueName, that.venueName)
                && Objects.equals(venueStreet, that.venueStreet)
                && Objects.equals(venueZip, that.venueZip)
                && Objects.equals(venueCity, that.venueCity)
                && Objects.equals(playingArea, that.playingArea)
                && Objects.equals(ballBrandAndType, that.ballBrandAndType)
                && ballProvisionPolicy == that.ballProvisionPolicy
                && Objects.equals(registrationDeadline, that.registrationDeadline)
                && Objects.equals(checkInDeadline, that.checkInDeadline)
                && Objects.equals(firstMatchesStart, that.firstMatchesStart)
                && Objects.equals(expectedEndTime, that.expectedEndTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                homologationNumber,
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

    @Override
    public String toString() {
        return "TournamentRegulationInfo{" +
                "homologationNumber='" + homologationNumber + '\'' +
                ", organizerEmail='" + organizerEmail + '\'' +
                ", venueName='" + venueName + '\'' +
                ", numberOfTables=" + numberOfTables +
                ", playingArea=" + playingArea +
                ", ballProvisionPolicy=" + ballProvisionPolicy +
                ", registrationDeadline=" + registrationDeadline +
                ", checkInDeadline=" + checkInDeadline +
                ", firstMatchesStart=" + firstMatchesStart +
                ", expectedEndTime=" + expectedEndTime +
                '}';
    }

    // -------------------------------------------------------------------------
    // UTIL
    // -------------------------------------------------------------------------

    private static String normalize(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}