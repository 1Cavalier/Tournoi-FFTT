package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ForeignParticipantTest {

    @Test
    void shouldExposeConvertedPointsAndFederationCountryCode() {
        ForeignFederationInfo info = ForeignFederationInfo.of("be", "AFTT", "BE-123");
        ForeignParticipant fp = new ForeignParticipant(
                " foreign-0001 ",
                "  John Doe ",
                Gender.MALE,
                AgeCategory.SENIOR,
                MedicalCertificateStatus.VALIDE,
                info,
                1450);

        assertEquals("FOREIGN-0001", fp.participantId());
        assertEquals("John Doe", fp.fullName());
        assertEquals("BE", fp.nationalityCode());
        assertEquals(1450, fp.pointsFor(RankingPhase.PHASE_1));
        assertFalse(fp.isFfttLicensed());
        assertEquals(info, fp.federationInfo());
    }

    @Test
    void negativeConvertedPointsShouldThrow() {
        ForeignFederationInfo info = ForeignFederationInfo.of("BE", "AFTT", null);

        assertCode(ErrorCode.PARTICIPANT_POINTS_NEGATIVE, () -> new ForeignParticipant(
                "FOREIGN-0001",
                "X",
                Gender.MALE,
                AgeCategory.SENIOR,
                MedicalCertificateStatus.VALIDE,
                info,
                -1));
    }

    private static void assertCode(ErrorCode code, Runnable r) {
        BusinessException ex = assertThrows(BusinessException.class, r::run);
        assertEquals(code, ex.getCode());
    }
}