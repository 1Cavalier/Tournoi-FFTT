package fr.Brunoy.gestion_tournois_FFTT.domain.organization;

import org.junit.jupiter.api.Test;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Club;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Departement;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Region;

import static org.junit.jupiter.api.Assertions.*;

class ClubTest {

    private static Region idf() {
        return new Region("IDF", "Île-de-France");
    }

    private static Departement essonne91() {
        return new Departement("91", "Essonne", idf());
    }

    @Test
    void shouldRequireNumber() {
        BusinessException ex1 = assertThrows(BusinessException.class,
                () -> new Club(null, "Brunoy CTT", essonne91(), "Brunoy", "Gymnase", null, null, null));
        assertEquals(ErrorCode.CLUB_NUMBER_REQUIRED, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> new Club("   ", "Brunoy CTT", essonne91(), "Brunoy", "Gymnase", null, null, null));
        assertEquals(ErrorCode.CLUB_NUMBER_REQUIRED, ex2.getCode());
    }

    @Test
    void shouldRequireName() {
        BusinessException ex1 = assertThrows(BusinessException.class,
                () -> new Club("08911132", null, essonne91(), "Brunoy", "Gymnase", null, null, null));
        assertEquals(ErrorCode.CLUB_NAME_REQUIRED, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> new Club("08911132", "   ", essonne91(), "Brunoy", "Gymnase", null, null, null));
        assertEquals(ErrorCode.CLUB_NAME_REQUIRED, ex2.getCode());
    }

    @Test
    void shouldRequireDepartement() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> new Club("08911132", "Brunoy CTT", null, "Brunoy", "Gymnase", null, null, null));
        assertEquals(ErrorCode.CLUB_DEPARTEMENT_REQUIRED, ex.getCode());
    }

    @Test
    void shouldRequireCity() {
        BusinessException ex1 = assertThrows(BusinessException.class,
                () -> new Club("08911132", "Brunoy CTT", essonne91(), null, "Gymnase", null, null, null));
        assertEquals(ErrorCode.CLUB_CITY_REQUIRED, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> new Club("08911132", "Brunoy CTT", essonne91(), "   ", "Gymnase", null, null, null));
        assertEquals(ErrorCode.CLUB_CITY_REQUIRED, ex2.getCode());
    }

    @Test
    void shouldAllowNullGeoCoordinates() {
        Club c = new Club("08911132", "Brunoy CTT", essonne91(), "Brunoy", "Gymnase Dupont", null, null, null);
        assertNull(c.getLatitude());
        assertNull(c.getLongitude());
    }

    @Test
    void shouldRejectIncompleteGeoCoordinates() {
        BusinessException ex1 = assertThrows(BusinessException.class,
                () -> new Club("08911132", "Brunoy CTT", essonne91(), "Brunoy", "Gymnase", null, 48.695, null));
        assertEquals(ErrorCode.CLUB_GEO_COORDINATES_INCOMPLETE, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> new Club("08911132", "Brunoy CTT", essonne91(), "Brunoy", "Gymnase", null, null, 2.492));
        assertEquals(ErrorCode.CLUB_GEO_COORDINATES_INCOMPLETE, ex2.getCode());
    }

    @Test
    void shouldRejectOutOfRangeGeoCoordinates() {
        BusinessException ex1 = assertThrows(BusinessException.class,
                () -> new Club("08911132", "Brunoy CTT", essonne91(), "Brunoy", "Gymnase", null, 120.0, 2.0));
        assertEquals(ErrorCode.CLUB_GEO_COORDINATES_INVALID, ex1.getCode());

        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> new Club("08911132", "Brunoy CTT", essonne91(), "Brunoy", "Gymnase", null, 48.0, 200.0));
        assertEquals(ErrorCode.CLUB_GEO_COORDINATES_INVALID, ex2.getCode());
    }

    @Test
    void shouldTrimBasicFieldsAndNormalizeOptionalAddresses() {
        Club c = new Club(
                " 08911132 ",
                " Brunoy CTT ",
                essonne91(),
                " Brunoy ",
                "  Gymnase Dupont  ",
                "   ", // address2 -> null
                null,
                null);

        assertEquals("08911132", c.getNumber());
        assertEquals("Brunoy CTT", c.getName());
        assertEquals("Brunoy", c.getCity());
        assertEquals("Gymnase Dupont", c.getAddress1());
        assertNull(c.getAddress2());
    }

    @Test
    void equalsAndHashCodeShouldBeBasedOnNumber() {
        Club c1 = new Club("08911132", "Brunoy CTT", essonne91(), "Brunoy", "Gymnase", null, null, null);
        Club c2 = new Club("08911132", "Autre nom", essonne91(), "Autre ville", null, null, null, null);
        Club c3 = new Club("08780329", "Versailles", new Departement("78", "Yvelines", idf()), "Versailles", null, null,
                null, null);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }
}