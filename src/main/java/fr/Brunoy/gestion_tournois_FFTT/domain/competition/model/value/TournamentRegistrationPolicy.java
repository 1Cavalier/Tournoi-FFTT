package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

public final class TournamentRegistrationPolicy {

    private final int maxTableauxPerDay;
    private final int maxTotalTableaux;

    private final FemaleExtraRuleType femaleExtraRuleType;
    private final String femaleExtraTableauCode; // utilisé si SPECIFIC_TABLEAU_CODE

    public TournamentRegistrationPolicy(
            int maxTableauxPerDay,
            int maxTotalTableaux,
            FemaleExtraRuleType femaleExtraRuleType,
            String femaleExtraTableauCode) {
        if (maxTableauxPerDay <= 0)
            throw new BusinessException(ErrorCode.TOURNAMENT_MAX_TABLEAUX_PER_DAY_INVALID);

        if (maxTotalTableaux <= 0)
            throw new BusinessException(ErrorCode.TOURNAMENT_MAX_TOTAL_TABLEAUX_INVALID);

        if (maxTotalTableaux < maxTableauxPerDay)
            throw new BusinessException(ErrorCode.TOURNAMENT_MAX_TOTAL_TABLEAUX_TOO_LOW);

        if (femaleExtraRuleType == null) {
            throw new BusinessException(ErrorCode.TOURNAMENT_FEMALE_EXTRA_RULE_REQUIRED);
        }

        if (femaleExtraRuleType == FemaleExtraRuleType.SPECIFIC_TABLEAU_CODE) {
            if (femaleExtraTableauCode == null || femaleExtraTableauCode.isBlank()) {
                throw new BusinessException(ErrorCode.TOURNAMENT_FEMALE_EXTRA_TABLEAU_CODE_REQUIRED);
            }
        }

        this.maxTableauxPerDay = maxTableauxPerDay;
        this.maxTotalTableaux = maxTotalTableaux;
        this.femaleExtraRuleType = femaleExtraRuleType;
        this.femaleExtraTableauCode = femaleExtraTableauCode == null ? null
                : femaleExtraTableauCode.trim().toUpperCase();
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
}
