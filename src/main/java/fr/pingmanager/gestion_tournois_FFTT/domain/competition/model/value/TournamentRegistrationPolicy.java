package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value;

import java.util.Locale;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.FemaleExtraRuleType;
import fr.pingmanager.gestion_tournois_FFTT.domain.identity.Participant;

public final class TournamentRegistrationPolicy {

    private final int maxTableauxPerDay;
    private final int maxTotalTableaux;

    private final ParticipantEligibilityPolicy participantEligibilityPolicy;

    private final FemaleExtraRuleType femaleExtraRuleType;
    private final String femaleExtraTableauCode; // requis uniquement pour SPECIFIC_*

    public TournamentRegistrationPolicy(
            int maxTableauxPerDay,
            int maxTotalTableaux,
            FemaleExtraRuleType femaleExtraRuleType,
            String femaleExtraTableauCode,
            ParticipantEligibilityPolicy participantEligibilityPolicy) {

        if (maxTableauxPerDay <= 0)
            throw new BusinessException(ErrorCode.TOURNAMENT_MAX_TABLEAUX_PER_DAY_INVALID);

        if (maxTotalTableaux <= 0)
            throw new BusinessException(ErrorCode.TOURNAMENT_MAX_TOTAL_TABLEAUX_INVALID);

        if (maxTotalTableaux < maxTableauxPerDay)
            throw new BusinessException(ErrorCode.TOURNAMENT_MAX_TOTAL_TABLEAUX_TOO_LOW);

        if (femaleExtraRuleType == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_FEMALE_EXTRA_RULE_REQUIRED);

        if (participantEligibilityPolicy == null)
            throw new BusinessException(ErrorCode.TOURNAMENT_PARTICIPANT_POLICY_REQUIRED);

        String normalized = normalizeCode(femaleExtraTableauCode);

        if (femaleExtraRuleType == FemaleExtraRuleType.SPECIFIC_TABLEAU_ONCE
                || femaleExtraRuleType == FemaleExtraRuleType.SPECIFIC_TABLEAU_PER_DAY) {
            if (normalized == null) {
                throw new BusinessException(ErrorCode.TOURNAMENT_FEMALE_EXTRA_TABLEAU_CODE_REQUIRED);
            }
        }

        this.maxTableauxPerDay = maxTableauxPerDay;
        this.maxTotalTableaux = maxTotalTableaux;
        this.femaleExtraRuleType = femaleExtraRuleType;
        this.femaleExtraTableauCode = normalized;
        this.participantEligibilityPolicy = participantEligibilityPolicy;
    }

    public int maxTableauxPerDay() {
        return maxTableauxPerDay;
    }

    public int maxTotalTableaux() {
        return maxTotalTableaux;
    }

    public FemaleExtraRuleType femaleExtraRuleType() {
        return femaleExtraRuleType;
    }

    public String femaleExtraTableauCode() {
        return femaleExtraTableauCode;
    }

    public ParticipantEligibilityPolicy participantEligibilityPolicy() {
        return participantEligibilityPolicy;
    }

    // -------------------------------------------------------------------------
    // LIMITS (pro) — déterministes
    // -------------------------------------------------------------------------

    public int allowedTableauxPerDay(Participant participant, Tableau targetTableau, boolean onceAlreadyUsed) {
        return maxTableauxPerDay + femaleDailyBonus(participant, targetTableau, onceAlreadyUsed);
    }

    public int allowedTotalTableaux(Participant participant, Tableau targetTableau, int tournamentDaysCount,
            boolean onceAlreadyUsed) {
        return maxTotalTableaux + femaleTotalBonus(participant, targetTableau, tournamentDaysCount, onceAlreadyUsed);
    }

    /**
     * Indique si la règle "féminin" est de type ONCE (une fois sur tout le
     * tournoi).
     */
    public boolean isFemaleExtraOnceRule() {
        return femaleExtraRuleType == FemaleExtraRuleType.EXTRA_ANY_ONCE
                || femaleExtraRuleType == FemaleExtraRuleType.SPECIFIC_TABLEAU_ONCE;
    }

    /**
     * Indique si le tableau cible peut déclencher le bonus (utile pour savoir si on
     * consomme le ONCE).
     */
    public boolean targetQualifiesForFemaleExtra(Participant participant, Tableau targetTableau) {
        if (participant == null || !participant.isFemale())
            return false;
        return switch (femaleExtraRuleType) {
            case NONE -> false;
            case EXTRA_ANY_ONCE, EXTRA_ANY_PER_DAY -> true;
            case SPECIFIC_TABLEAU_ONCE, SPECIFIC_TABLEAU_PER_DAY -> isTargetSpecific(targetTableau);
        };
    }

    // -------------------------------------------------------------------------
    // BONUS
    // -------------------------------------------------------------------------

    private int femaleDailyBonus(Participant participant, Tableau targetTableau, boolean onceAlreadyUsed) {
        if (participant == null || !participant.isFemale())
            return 0;

        return switch (femaleExtraRuleType) {
            case NONE -> 0;

            case EXTRA_ANY_PER_DAY -> 1;

            case EXTRA_ANY_ONCE -> onceAlreadyUsed ? 0 : 1;

            case SPECIFIC_TABLEAU_PER_DAY -> isTargetSpecific(targetTableau) ? 1 : 0;

            case SPECIFIC_TABLEAU_ONCE -> {
                if (!isTargetSpecific(targetTableau))
                    yield 0;
                yield onceAlreadyUsed ? 0 : 1;
            }
        };
    }

    private int femaleTotalBonus(Participant participant, Tableau targetTableau, int tournamentDaysCount,
            boolean onceAlreadyUsed) {
        if (participant == null || !participant.isFemale())
            return 0;

        return switch (femaleExtraRuleType) {
            case NONE -> 0;

            case EXTRA_ANY_ONCE -> onceAlreadyUsed ? 0 : 1;

            case EXTRA_ANY_PER_DAY -> Math.max(0, tournamentDaysCount);

            case SPECIFIC_TABLEAU_ONCE -> {
                if (!isTargetSpecific(targetTableau))
                    yield 0;
                yield onceAlreadyUsed ? 0 : 1;
            }

            case SPECIFIC_TABLEAU_PER_DAY -> {
                if (!isTargetSpecific(targetTableau))
                    yield 0;
                yield Math.max(0, tournamentDaysCount);
            }
        };
    }

    private boolean isTargetSpecific(Tableau target) {
        return target != null
                && femaleExtraTableauCode != null
                && femaleExtraTableauCode.equalsIgnoreCase(target.code());
    }

    private static String normalizeCode(String code) {
        if (code == null)
            return null;
        String t = code.trim().toUpperCase(Locale.ROOT);
        return t.isEmpty() ? null : t;
    }
}