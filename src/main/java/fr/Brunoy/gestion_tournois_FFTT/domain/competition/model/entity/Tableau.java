package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TableauPointsRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeDistribution;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.RegistrationFee;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public final class Tableau {

    private final String code;
    private final String designation;
    private final LocalDate date;

    private final GenderPolicy genderPolicy;

    private final TableauPointsRuleType pointsRuleType;
    private final Integer minPoints;
    private final Integer maxPoints;

    private final int maxPlayers;

    // NEW : capacité file d'attente (ex: 20). 0 = pas de file d'attente
    private final int waitlistCapacity;

    private final RegistrationFee fee;
    private final LocalTime checkInEnd;
    private final LocalTime startTime;

    private final PrizeDistribution prizes;

    public Tableau(
            String code,
            String designation,
            LocalDate date,
            GenderPolicy genderPolicy,
            TableauPointsRuleType pointsRuleType,
            Integer minPoints,
            Integer maxPoints,
            int maxPlayers,
            int waitlistCapacity, // NEW
            RegistrationFee fee,
            LocalTime checkInEnd,
            LocalTime startTime,
            PrizeDistribution prizes) {

        this.code = requireText(code, ErrorCode.TABLEAU_CODE_REQUIRED).toUpperCase();
        this.designation = requireText(designation, ErrorCode.TABLEAU_DESIGNATION_REQUIRED);
        this.date = requireNonNull(date, ErrorCode.TABLEAU_DATE_REQUIRED);

        this.genderPolicy = requireNonNull(genderPolicy, ErrorCode.TABLEAU_GENDER_POLICY_REQUIRED);

        this.pointsRuleType = requireNonNull(pointsRuleType, ErrorCode.TABLEAU_POINTS_RULE_TYPE_REQUIRED);
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;

        if (maxPlayers <= 0) {
            throw new BusinessException(ErrorCode.TABLEAU_MAX_PLAYERS_INVALID);
        }
        this.maxPlayers = maxPlayers;

        // NEW
        if (waitlistCapacity < 0) {
            throw new BusinessException(ErrorCode.TABLEAU_WAITLIST_CAPACITY_INVALID);
        }
        this.waitlistCapacity = waitlistCapacity;

        this.fee = requireNonNull(fee, ErrorCode.TABLEAU_FEE_REQUIRED);

        this.checkInEnd = requireNonNull(checkInEnd, ErrorCode.TABLEAU_CHECKIN_TIME_REQUIRED);
        this.startTime = requireNonNull(startTime, ErrorCode.TABLEAU_START_TIME_REQUIRED);

        this.prizes = requireNonNull(prizes, ErrorCode.TABLEAU_PRIZE_REQUIRED);

        validatePointsRule();
        validateTimes();
    }

    // -------------------------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------------------------

    private void validatePointsRule() {

        switch (pointsRuleType) {

            case TOUTES_SERIES -> {
                if (minPoints != null || maxPoints != null) {
                    throw new BusinessException(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT);
                }
            }

            case MAX_ONLY -> {
                if (maxPoints == null)
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_REQUIRED);

                if (maxPoints < 0)
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_NEGATIVE);

                if (minPoints != null)
                    throw new BusinessException(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT);
            }

            case RANGE_MIN_MAX -> {

                if (minPoints == null)
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_POINTS_REQUIRED);

                if (maxPoints == null)
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_REQUIRED);

                if (minPoints < 0)
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_POINTS_NEGATIVE);

                if (maxPoints < 0)
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_NEGATIVE);

                if (minPoints > maxPoints)
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_GREATER_THAN_MAX);
            }
        }
    }

    private void validateTimes() {
        if (!checkInEnd.isBefore(startTime)) {
            throw new BusinessException(ErrorCode.TABLEAU_CHECKIN_AFTER_START);
        }
    }

    // -------------------------------------------------------------------------
    // ELIGIBILITY
    // -------------------------------------------------------------------------

    /**
     * Vérifie si un joueur est éligible au tableau
     * (genre + points).
     *
     * La capacité maximale (et la file d'attente) est gérée par l'aggregate
     * Tournament.
     */
    public boolean accepts(int playerPoints, boolean isFemale) {

        if (playerPoints < 0)
            return false;

        if (genderPolicy == GenderPolicy.FEMININ_ONLY && !isFemale)
            return false;

        return switch (pointsRuleType) {

            case TOUTES_SERIES -> true;

            case MAX_ONLY ->
                playerPoints <= maxPoints;

            case RANGE_MIN_MAX ->
                playerPoints >= minPoints && playerPoints <= maxPoints;
        };
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String code() {
        return code;
    }

    public String designation() {
        return designation;
    }

    public LocalDate date() {
        return date;
    }

    public GenderPolicy genderPolicy() {
        return genderPolicy;
    }

    public TableauPointsRuleType pointsRuleType() {
        return pointsRuleType;
    }

    public Integer minPoints() {
        return minPoints;
    }

    public Integer maxPoints() {
        return maxPoints;
    }

    public int maxPlayers() {
        return maxPlayers;
    }

    // NEW
    public int waitlistCapacity() {
        return waitlistCapacity;
    }

    public RegistrationFee fee() {
        return fee;
    }

    public LocalTime checkInEnd() {
        return checkInEnd;
    }

    public LocalTime startTime() {
        return startTime;
    }

    public PrizeDistribution prizes() {
        return prizes;
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private static String requireText(String value, ErrorCode error) {
        if (value == null || value.isBlank())
            throw new BusinessException(error);
        return value.trim();
    }

    private static <T> T requireNonNull(T value, ErrorCode error) {
        if (Objects.isNull(value))
            throw new BusinessException(error);
        return value;
    }
}