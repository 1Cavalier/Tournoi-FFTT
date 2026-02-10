package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;

public final class PrizeTier {

    private final int fromRank; // inclus
    private final int toRank; // inclus
    private final int amount; // montant pour chaque joueur dans cette plage

    public PrizeTier(int fromRank, int toRank, int amount) {
        if (fromRank <= 0 || toRank <= 0 || fromRank > toRank) {
            throw new BusinessException(ErrorCode.TABLEAU_PRIZE_NEGATIVE); // ou crée un code PRIZE_TIER_INVALID si tu
                                                                           // veux être plus précis
        }
        if (amount < 0) {
            throw new BusinessException(ErrorCode.TABLEAU_PRIZE_NEGATIVE);
        }
        this.fromRank = fromRank;
        this.toRank = toRank;
        this.amount = amount;
    }

    public int fromRank() {
        return fromRank;
    }

    public int toRank() {
        return toRank;
    }

    public int amount() {
        return amount;
    }

    public boolean covers(int rank) {
        return rank >= fromRank && rank <= toRank;
    }
}
