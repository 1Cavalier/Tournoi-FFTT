package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;
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
            int maxPlayers, // ✅ nouveau
            RegistrationFee fee,
            LocalTime checkInEnd,
            LocalTime startTime,
            PrizeDistribution prizes) {
        this.code = requireText(code, ErrorCode.TABLEAU_CODE_REQUIRED);
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

        this.fee = requireNonNull(fee, ErrorCode.TABLEAU_FEE_REQUIRED);

        this.checkInEnd = requireNonNull(checkInEnd, ErrorCode.TABLEAU_CHECKIN_TIME_REQUIRED);
        this.startTime = requireNonNull(startTime, ErrorCode.TABLEAU_START_TIME_REQUIRED);

        this.prizes = requireNonNull(prizes, ErrorCode.TABLEAU_PRIZE_REQUIRED);

        validatePointsRule();
        validateTimes();
    }

    private void validatePointsRule() {
        switch (pointsRuleType) {
            case TOUTES_SERIES -> {
                if (minPoints != null || maxPoints != null) {
                    throw new BusinessException(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT);
                }
            }
            case MAX_ONLY -> {
                if (maxPoints == null) {
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_REQUIRED);
                }
                if (maxPoints < 0) {
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_NEGATIVE);
                }
                if (minPoints != null) {
                    throw new BusinessException(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT);
                }
            }
            case RANGE_MIN_MAX -> {
                if (minPoints == null) {
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_POINTS_REQUIRED);
                }
                if (maxPoints == null) {
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_REQUIRED);
                }
                if (minPoints < 0) {
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_POINTS_NEGATIVE);
                }
                if (maxPoints < 0) {
                    throw new BusinessException(ErrorCode.TABLEAU_MAX_POINTS_NEGATIVE);
                }
                if (minPoints > maxPoints) {
                    throw new BusinessException(ErrorCode.TABLEAU_MIN_GREATER_THAN_MAX);
                }
            }
        }
    }

    private void validateTimes() {
        if (!checkInEnd.isBefore(startTime)) {
            throw new BusinessException(ErrorCode.TABLEAU_CHECKIN_AFTER_START);
        }
    }

    private static String requireText(String value, ErrorCode codeIfInvalid) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(codeIfInvalid);
        }
        return value.trim();
    }

    private static <T> T requireNonNull(T value, ErrorCode codeIfNull) {
        if (Objects.isNull(value)) {
            throw new BusinessException(codeIfNull);
        }
        return value;
    }

    /**
     * Eligibilité simple (points + sexe). La capacité max est gérée dans le service
     * d'inscription.
     */
    public boolean accepts(int playerPoints, boolean isFemale) {
        if (playerPoints < 0)
            return false;

        if (genderPolicy == GenderPolicy.FEMININ_ONLY && !isFemale) {
            return false;
        }

        return switch (pointsRuleType) {
            case TOUTES_SERIES -> true;
            case MAX_ONLY -> playerPoints <= maxPoints;
            case RANGE_MIN_MAX -> playerPoints >= minPoints && playerPoints <= maxPoints;
        };
    }

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
    } // ✅ nouveau

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
}
