package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForeignFederationInfoTest {

    @Test
    void shouldNormalizeCountryCodeAndOptionalLicense() {
        ForeignFederationInfo info = ForeignFederationInfo.of(" be ", "AFTT", "  123 ");
        assertEquals("BE", info.countryCode());
        assertEquals("AFTT", info.federationName());
        assertEquals("123", info.foreignLicenseId());
    }

    @Test
    void blankOptionalLicenseShouldBecomeNull() {
        ForeignFederationInfo info = ForeignFederationInfo.of("be", "AFTT", "   ");
        assertNull(info.foreignLicenseId());
    }

    @Test
    void requiredFieldsShouldThrow() {
        assertCode(ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED, () -> ForeignFederationInfo.of(" ", "AFTT", null));
        assertCode(ErrorCode.PARTICIPANT_FOREIGN_FEDERATION_REQUIRED, () -> ForeignFederationInfo.of("BE", " ", null));
    }

    private static void assertCode(ErrorCode code, Runnable r) {
        BusinessException ex = assertThrows(BusinessException.class, r::run);
        assertEquals(code, ex.getCode());
    }
}