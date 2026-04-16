package fr.Brunoy.gestion_tournois_FFTT.domain.organization;

import org.junit.jupiter.api.Test;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Region;

import static org.junit.jupiter.api.Assertions.*;

class RegionTest {

    @Test
    void shouldRequireCode() {
        BusinessException ex1 = assertThrows(BusinessException.class, () -> new Region(null, "Île-de-France"));
        assertEquals(ErrorCode.REGION_CODE_REQUIRED, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class, () -> new Region("   ", "Île-de-France"));
        assertEquals(ErrorCode.REGION_CODE_REQUIRED, ex2.getCode());
    }

    @Test
    void shouldRequireName() {
        BusinessException ex1 = assertThrows(BusinessException.class, () -> new Region("IDF", null));
        assertEquals(ErrorCode.REGION_NAME_REQUIRED, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class, () -> new Region("IDF", "   "));
        assertEquals(ErrorCode.REGION_NAME_REQUIRED, ex2.getCode());
    }

    @Test
    void shouldTrimFields() {
        Region r = new Region("  IDF  ", "  Île-de-France  ");
        assertEquals("IDF", r.getCode());
        assertEquals("Île-de-France", r.getName());
    }

    @Test
    void equalsAndHashCodeShouldBeBasedOnCode() {
        Region r1 = new Region("IDF", "Île-de-France");
        Region r2 = new Region("IDF", "Autre nom (doit être ignoré pour equals)");
        Region r3 = new Region("NOR", "Normandie");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}