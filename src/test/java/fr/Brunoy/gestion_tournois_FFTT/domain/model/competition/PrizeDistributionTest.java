package fr.Brunoy.gestion_tournois_FFTT.domain.model.competition;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeDistribution;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeTier;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrizeDistributionTest {

    @Test
    void shouldThrow_whenTiersIsNull() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PrizeDistribution(null));

        assertEquals(ErrorCode.TABLEAU_PRIZE_REQUIRED, ex.getCode());
    }

    @Test
    void shouldThrow_whenTiersIsEmpty() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new PrizeDistribution(List.of()));

        assertEquals(ErrorCode.TABLEAU_PRIZE_REQUIRED, ex.getCode());
    }

    @Test
    void shouldCreate_andReturnAmountsByRank() {
        PrizeDistribution prizes = new PrizeDistribution(List.of(
                new PrizeTier(1, 1, 80),
                new PrizeTier(2, 2, 50),
                new PrizeTier(3, 4, 25),
                new PrizeTier(5, 8, 10)));

        assertEquals(80, prizes.amountForRank(1));
        assertEquals(50, prizes.amountForRank(2));
        assertEquals(25, prizes.amountForRank(3));
        assertEquals(25, prizes.amountForRank(4));
        assertEquals(10, prizes.amountForRank(5));
        assertEquals(10, prizes.amountForRank(8));

        assertEquals(0, prizes.amountForRank(9)); // non primé
        assertEquals(0, prizes.amountForRank(0)); // rang invalide
        assertEquals(0, prizes.amountForRank(-1)); // rang invalide
    }

    @Test
    void shouldThrow_whenTiersOverlap() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new PrizeDistribution(List.of(
                new PrizeTier(1, 2, 50),
                new PrizeTier(2, 4, 25) // chevauchement sur "2"
        )));

        // Dans ton code actuel, tu réutilises TABLEAU_POINTS_RULE_INCONSISTENT
        assertEquals(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT, ex.getCode());
    }
}
