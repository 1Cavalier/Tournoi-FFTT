package fr.Brunoy.gestion_tournois_FFTT.domain.organization;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepartementTest {

    private static Region idf() {
        return new Region("IDF", "Île-de-France");
    }

    @Test
    void shouldRequireCode() {
        BusinessException ex1 = assertThrows(BusinessException.class, () -> new Departement(null, "Essonne", idf()));
        assertEquals(ErrorCode.DEPARTEMENT_CODE_REQUIRED, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class, () -> new Departement("   ", "Essonne", idf()));
        assertEquals(ErrorCode.DEPARTEMENT_CODE_REQUIRED, ex2.getCode());
    }

    @Test
    void shouldRequireName() {
        BusinessException ex1 = assertThrows(BusinessException.class, () -> new Departement("91", null, idf()));
        assertEquals(ErrorCode.DEPARTEMENT_NAME_REQUIRED, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class, () -> new Departement("91", "   ", idf()));
        assertEquals(ErrorCode.DEPARTEMENT_NAME_REQUIRED, ex2.getCode());
    }

    @Test
    void shouldRequireRegion() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Departement("91", "Essonne", null));
        assertEquals(ErrorCode.DEPARTEMENT_REGION_REQUIRED, ex.getCode());
    }

    @Test
    void shouldTrimFields() {
        Departement d = new Departement("  91  ", "  Essonne  ", idf());
        assertEquals("91", d.getCode());
        assertEquals("Essonne", d.getName());
        assertEquals("IDF", d.getRegion().getCode());
    }

    @Test
    void equalsAndHashCodeShouldBeBasedOnCode() {
        Departement d1 = new Departement("91", "Essonne", idf());
        Departement d2 = new Departement("91", "Autre nom", idf());
        Departement d3 = new Departement("78", "Yvelines", idf());

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertNotEquals(d1, d3);
    }
}