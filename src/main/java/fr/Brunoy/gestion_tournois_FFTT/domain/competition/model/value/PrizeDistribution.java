package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class PrizeDistribution {

    private final List<PrizeTier> tiers;

    public PrizeDistribution(List<PrizeTier> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new BusinessException(ErrorCode.TABLEAU_PRIZE_REQUIRED);
        }

        // copie défensive + tri
        List<PrizeTier> copy = new ArrayList<>(tiers);
        copy.sort(Comparator.comparingInt(PrizeTier::fromRank));

        // vérifier chevauchements / incohérences
        for (int i = 0; i < copy.size(); i++) {
            PrizeTier current = copy.get(i);

            // (montant négatif et rangs invalides déjà gérés dans PrizeTier)
            if (i > 0) {
                PrizeTier prev = copy.get(i - 1);
                // chevauchement si prev.toRank >= current.fromRank
                if (prev.toRank() >= current.fromRank()) {
                    throw new BusinessException(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT);
                    // idéalement créer: TABLEAU_PRIZE_INCONSISTENT
                }
            }
        }

        this.tiers = Collections.unmodifiableList(copy);
    }

    public List<PrizeTier> tiers() {
        return tiers;
    }

    /** Montant pour une place donnée (1 = vainqueur). 0 si non primé. */
    public int amountForRank(int rank) {
        if (rank <= 0)
            return 0;
        for (PrizeTier tier : tiers) {
            if (tier.covers(rank))
                return tier.amount();
        }
        return 0;
    }
}
