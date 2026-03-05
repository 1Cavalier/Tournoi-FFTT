package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.MedicalCertificateStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;
import fr.Brunoy.gestion_tournois_FFTT.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void shouldNormalizeStringsAndComputeFullName() {
        Player p = new Player(
                "  08911132X ",
                "  QUENTIN ",
                " SOUMET  ",
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.Gender.MALE,
                " fr ",
                TestDataFactory.clubBrunoy(),
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.AgeCategory.SENIOR,
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                100,
                200);

        assertEquals("08911132X", p.getLicenseNumber());
        assertEquals("QUENTIN", p.getFirstName());
        assertEquals("SOUMET", p.getLastName());
        assertEquals("QUENTIN SOUMET", p.getFullName());
        assertEquals("FR", p.getNationality());
    }

    @Test
    void pointsForShouldUsePhase() {
        Player p = TestDataFactory.maleSeniorBrunoy();
        assertEquals(p.getPhase1StartPoints(), p.pointsFor(RankingPhase.PHASE_1));
        assertEquals(p.getPhase2OfficialPoints(), p.pointsFor(RankingPhase.PHASE_2));
    }

    @Test
    void pointsForNullPhaseShouldFallbackToOfficialPoints() {
        Player p = TestDataFactory.maleSeniorBrunoy();
        assertEquals(p.getPhase2OfficialPoints(), p.pointsFor(null));
    }

    @Test
    void shouldExposeMedicalCertificateLogic() {
        Player p = TestDataFactory.maleSeniorBrunoy();
        assertTrue(p.hasValidMedicalCertificate());

        p.updateMedicalCertificateStatus(MedicalCertificateStatus.NON_VALIDE);
        assertFalse(p.hasValidMedicalCertificate());
    }

    @Test
    void updateMedicalCertificateStatusNullShouldThrow() {
        Player p = TestDataFactory.maleSeniorBrunoy();
        BusinessException ex = assertThrows(BusinessException.class, () -> p.updateMedicalCertificateStatus(null));
        assertEquals(ErrorCode.PLAYER_MEDICAL_CERT_REQUIRED, ex.getCode());
    }

    @Test
    void equalsAndHashCodeShouldBeBasedOnLicense() {
        Player p1 = TestDataFactory.maleSeniorBrunoy();
        Player p2 = new Player(
                p1.getLicenseNumber(),
                "Other",
                "Name",
                p1.getGender(),
                p1.getNationality(),
                p1.getClub(),
                p1.getAgeCategory(),
                p1.getLicenseType(),
                p1.isMutated(),
                p1.getMedicalCertificateStatus(),
                p1.getPhase1StartPoints(),
                p1.getPhase2OfficialPoints());

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void requiredFieldsShouldThrowBusinessExceptionWithProperCode() {
        assertCode(ErrorCode.PLAYER_LICENSE_REQUIRED, () -> new Player(
                " ", "A", "B",
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.Gender.MALE,
                "FR", TestDataFactory.clubBrunoy(),
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.AgeCategory.SENIOR,
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                10, 10));

        assertCode(ErrorCode.PLAYER_FIRST_NAME_REQUIRED, () -> new Player(
                "X", " ", "B",
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.Gender.MALE,
                "FR", TestDataFactory.clubBrunoy(),
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.AgeCategory.SENIOR,
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                10, 10));

        assertCode(ErrorCode.PLAYER_LAST_NAME_REQUIRED, () -> new Player(
                "X", "A", " ",
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.Gender.MALE,
                "FR", TestDataFactory.clubBrunoy(),
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.AgeCategory.SENIOR,
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                10, 10));

        assertCode(ErrorCode.PLAYER_POINTS_NEGATIVE, () -> new Player(
                "X", "A", "B",
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.Gender.MALE,
                "FR", TestDataFactory.clubBrunoy(),
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.AgeCategory.SENIOR,
                fr.Brunoy.gestion_tournois_FFTT.domain.refdata.LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                -1, 10));
    }

    private static void assertCode(ErrorCode code, Runnable r) {
        BusinessException ex = assertThrows(BusinessException.class, r::run);
        assertEquals(code, ex.getCode());
    }
}