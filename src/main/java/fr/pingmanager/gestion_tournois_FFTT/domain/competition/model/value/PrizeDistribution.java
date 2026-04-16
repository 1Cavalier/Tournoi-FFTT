package fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;

public final class PrizeDistribution {

    private final List<PrizeTier> tiers;

    public PrizeDistribution(List<PrizeTier> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            throw new BusinessException(ErrorCode.TABLEAU_PRIZE_REQUIRED);
        }

        List<PrizeTier> copy = new ArrayList<>(tiers);

        if (copy.stream().anyMatch(t -> t == null)) {
            throw new BusinessException(ErrorCode.TABLEAU_PRIZE_INCONSISTENT);
        }

        copy.sort(Comparator.comparingInt(PrizeTier::fromRank));

        for (int i = 1; i < copy.size(); i++) {
            PrizeTier prev = copy.get(i - 1);
            PrizeTier current = copy.get(i);

            // chevauchement si prev.toRank >= current.fromRank
            if (prev.toRank() >= current.fromRank()) {
                throw new BusinessException(ErrorCode.TABLEAU_PRIZE_INCONSISTENT);
            }
        }

        this.tiers = List.copyOf(copy);
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

    @Override
    public String toString() {
        return tiers.toString();
    }
}