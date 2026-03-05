package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.FemaleExtraRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Participant;

import java.util.List;

public final class TournamentRegistrationPolicy {

    private final int maxTableauxPerDay;
    private final int maxTotalTableaux;
    private final ParticipantEligibilityPolicy participantEligibilityPolicy;

    private final FemaleExtraRuleType femaleExtraRuleType;
    private final String femaleExtraTableauCode; // utilisé si SPECIFIC_TABLEAU_CODE

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

        if (femaleExtraRuleType == FemaleExtraRuleType.SPECIFIC_TABLEAU_CODE) {
            if (femaleExtraTableauCode == null || femaleExtraTableauCode.isBlank()) {
                throw new BusinessException(ErrorCode.TOURNAMENT_FEMALE_EXTRA_TABLEAU_CODE_REQUIRED);
            }

        }

        this.maxTableauxPerDay = maxTableauxPerDay;
        this.maxTotalTableaux = maxTotalTableaux;
        this.femaleExtraRuleType = femaleExtraRuleType;
        this.participantEligibilityPolicy = participantEligibilityPolicy;
        this.femaleExtraTableauCode = normalizeCode(femaleExtraTableauCode);
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

    /**
     * Nombre autorisé de tableaux pour ce jour (max/jour + bonus féminin éventuel).
     */
    public int allowedTableauxPerDay(Participant participant, List<Tableau> selectedForThatDay) {
        return maxTableauxPerDay + femaleDailyBonus(participant, selectedForThatDay);
    }

    public int femaleDailyBonus(Participant participant, List<Tableau> selectedForThatDay) {
        if (participant == null || !participant.isFemale())
            return 0;

        return switch (femaleExtraRuleType) {
            case NONE -> 0;
            case ANY_TABLEAU -> 1;
            case SPECIFIC_TABLEAU_CODE -> hasSpecificTableau(selectedForThatDay) ? 1 : 0;
        };
    }

    private boolean hasSpecificTableau(List<Tableau> selectedForThatDay) {
        if (femaleExtraTableauCode == null)
            return false;
        if (selectedForThatDay == null || selectedForThatDay.isEmpty())
            return false;

        for (Tableau t : selectedForThatDay) {
            if (t != null && femaleExtraTableauCode.equalsIgnoreCase(t.code())) {
                return true;
            }
        }
        return false;
    }

    private String normalizeCode(String code) {
        if (code == null)
            return null;
        String t = code.trim().toUpperCase();
        return t.isEmpty() ? null : t;
    }
}