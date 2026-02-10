package fr.Brunoy.gestion_tournois_FFTT.domain.model.competition;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.entity.Tableau;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.GenderPolicy;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.enums.TableauPointsRuleType;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeDistribution;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.PrizeTier;
import fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value.RegistrationFee;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableauTest {

    private static PrizeDistribution defaultPrizes() {
        return new PrizeDistribution(List.of(
                new PrizeTier(1, 1, 80),
                new PrizeTier(2, 2, 50),
                new PrizeTier(3, 4, 25)));
    }

    private static Tableau baseTableau(
            GenderPolicy genderPolicy,
            TableauPointsRuleType ruleType,
            Integer min,
            Integer max) {
        return new Tableau(
                "A",
                "Tableau test",
                LocalDate.of(2026, 2, 7),
                genderPolicy,
                ruleType,
                min,
                max,
                12, // ✅ maxPlayers (exemple)
                new RegistrationFee(8, 10),
                LocalTime.of(9, 15),
                LocalTime.of(9, 45),
                defaultPrizes());
    }

    @Test
    void shouldCreateTableau_whenToutesSeries() {
        Tableau t = baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.TOUTES_SERIES, null, null);

        assertEquals("A", t.code());
        assertEquals(TableauPointsRuleType.TOUTES_SERIES, t.pointsRuleType());
        assertNull(t.minPoints());
        assertNull(t.maxPoints());
    }

    @Test
    void shouldThrow_whenToutesSeriesDefinesMinOrMax() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.TOUTES_SERIES, 500, null));

        assertEquals(ErrorCode.TABLEAU_POINTS_RULE_INCONSISTENT, ex.getCode());
    }

    @Test
    void shouldCreateTableau_whenMaxOnly() {
        Tableau t = baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.MAX_ONLY, null, 799);

        assertNull(t.minPoints());
        assertEquals(799, t.maxPoints());
    }

    @Test
    void shouldThrow_whenMaxOnlyWithoutMax() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.MAX_ONLY, null, null));

        assertEquals(ErrorCode.TABLEAU_MAX_POINTS_REQUIRED, ex.getCode());
    }

    @Test
    void shouldCreateTableau_whenRangeMinMax() {
        Tableau t = baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.RANGE_MIN_MAX, 600, 899);

        assertEquals(600, t.minPoints());
        assertEquals(899, t.maxPoints());
    }

    @Test
    void shouldThrow_whenRangeMinMaxMissingMin() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.RANGE_MIN_MAX, null, 899));

        assertEquals(ErrorCode.TABLEAU_MIN_POINTS_REQUIRED, ex.getCode());
    }

    @Test
    void shouldThrow_whenRangeMinMaxMissingMax() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.RANGE_MIN_MAX, 600, null));

        assertEquals(ErrorCode.TABLEAU_MAX_POINTS_REQUIRED, ex.getCode());
    }

    @Test
    void shouldThrow_whenMinGreaterThanMax() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.RANGE_MIN_MAX, 900, 899));

        assertEquals(ErrorCode.TABLEAU_MIN_GREATER_THAN_MAX, ex.getCode());
    }

    @Test
    void shouldThrow_whenCheckInEndIsAfterOrEqualStart() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Tableau(
                "A",
                "Tableau test",
                LocalDate.of(2026, 2, 7),
                GenderPolicy.MIXTE,
                TableauPointsRuleType.TOUTES_SERIES,
                null,
                null,
                12, // ✅ maxPlayers
                new RegistrationFee(8, 10),
                LocalTime.of(9, 45),
                LocalTime.of(9, 45),
                defaultPrizes()));

        assertEquals(ErrorCode.TABLEAU_CHECKIN_AFTER_START, ex.getCode());
    }

    @Test
    void accepts_shouldRespectGenderPolicy_femininOnly() {
        Tableau t = baseTableau(GenderPolicy.FEMININ_ONLY, TableauPointsRuleType.TOUTES_SERIES, null, null);

        assertTrue(t.accepts(1200, true)); // femme OK
        assertFalse(t.accepts(1200, false)); // homme refusé
    }

    @Test
    void accepts_shouldRespectMaxOnly() {
        Tableau t = baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.MAX_ONLY, null, 799);

        assertTrue(t.accepts(500, true));
        assertTrue(t.accepts(799, false));
        assertFalse(t.accepts(800, true));
    }

    @Test
    void accepts_shouldRespectRangeMinMax() {
        Tableau t = baseTableau(GenderPolicy.MIXTE, TableauPointsRuleType.RANGE_MIN_MAX, 600, 899);

        assertFalse(t.accepts(599, true));
        assertTrue(t.accepts(600, true));
        assertTrue(t.accepts(750, false));
        assertTrue(t.accepts(899, true));
        assertFalse(t.accepts(900, false));
    }
}
