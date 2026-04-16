package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.AgeCategory;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AgeCategoryPolicyTest {

    @Test
    void anyShouldAcceptAllNonNullCategories() {
        AgeCategoryPolicy p = AgeCategoryPolicy.any();

        for (AgeCategory c : AgeCategory.values()) {
            assertTrue(p.accepts(c), "Doit accepter " + c.label());
        }
        assertFalse(p.accepts(null));
    }

    @Test
    void allowedSetShouldAcceptOnlyWhitelistedCategories() {
        AgeCategoryPolicy p = AgeCategoryPolicy.allowed(
                EnumSet.of(AgeCategory.SENIOR, AgeCategory.VETERAN_45));

        assertTrue(p.accepts(AgeCategory.SENIOR));
        assertTrue(p.accepts(AgeCategory.VETERAN_45));
        assertFalse(p.accepts(AgeCategory.JUNIOR_2));
        assertFalse(p.accepts(null));
    }

    @Test
    void rangeShouldAcceptBetweenMinAndMaxInclusive() {
        // Intervalle Cadet 1 → Junior 2
        AgeCategoryPolicy p = AgeCategoryPolicy.range(AgeCategory.CADET_1, AgeCategory.JUNIOR_2);

        assertFalse(p.accepts(AgeCategory.MINIME_2)); // avant CADET_1
        assertTrue(p.accepts(AgeCategory.CADET_1));
        assertTrue(p.accepts(AgeCategory.CADET_2));
        assertTrue(p.accepts(AgeCategory.JUNIOR_1));
        assertTrue(p.accepts(AgeCategory.JUNIOR_2));
        assertFalse(p.accepts(AgeCategory.JUNIOR_3)); // après JUNIOR_2
        assertFalse(p.accepts(AgeCategory.SENIOR));
        assertFalse(p.accepts(null));
    }

    @Test
    void allowedSetWithEmptyShouldThrow() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> AgeCategoryPolicy.allowed(Set.of()));
        assertEquals(ErrorCode.TABLEAU_AGE_POLICY_INVALID, ex.getCode());
    }

    @Test
    void rangeWithNullsOrInvertedBoundsShouldThrow() {
        BusinessException ex1 = assertThrows(BusinessException.class,
                () -> AgeCategoryPolicy.range(null, AgeCategory.SENIOR));
        assertEquals(ErrorCode.TABLEAU_AGE_POLICY_INVALID, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> AgeCategoryPolicy.range(AgeCategory.SENIOR, null));
        assertEquals(ErrorCode.TABLEAU_AGE_POLICY_INVALID, ex2.getCode());

        // Bornes inversées : SENIOR > CADET_1
        BusinessException ex3 = assertThrows(BusinessException.class,
                () -> AgeCategoryPolicy.range(AgeCategory.SENIOR, AgeCategory.CADET_1));
        assertEquals(ErrorCode.TABLEAU_AGE_POLICY_INVALID, ex3.getCode());
    }
}