package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

import java.util.Objects;

public final class PrizeTier {

    private final int fromRank; // inclus
    private final int toRank; // inclus
    private final int amount; // montant pour chaque joueur dans cette plage

    public PrizeTier(int fromRank, int toRank, int amount) {
        if (fromRank <= 0 || toRank <= 0 || fromRank > toRank) {
            throw new BusinessException(ErrorCode.TABLEAU_PRIZE_TIER_INVALID);
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PrizeTier that))
            return false;
        return fromRank == that.fromRank && toRank == that.toRank && amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromRank, toRank, amount);
    }

    @Override
    public String toString() {
        return fromRank + "-" + toRank + " => " + amount + "€";
    }
}