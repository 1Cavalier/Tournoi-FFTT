package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GuestParticipantTest {

    @Test
    void shouldNormalizeIdAndNationalityAndReturnZeroPoints() {
        GuestParticipant g = new GuestParticipant(
                " guest-0001 ",
                "  Alice Guest ",
                Gender.FEMALE,
                " fr ",
                AgeCategory.SENIOR,
                MedicalCertificateStatus.VALIDE);

        assertEquals("GUEST-0001", g.participantId());
        assertEquals("Alice Guest", g.fullName());
        assertEquals("FR", g.nationalityCode());
        assertEquals(0, g.pointsFor(RankingPhase.PHASE_1));
        assertFalse(g.isFfttLicensed());
    }

    @Test
    void shouldThrowIdRequiredWhenIdBlank() {
        assertCode(ErrorCode.PARTICIPANT_ID_REQUIRED, () -> new GuestParticipant(
                " ", "X", Gender.MALE, "FR", AgeCategory.SENIOR, MedicalCertificateStatus.VALIDE));
    }

    @Test
    void shouldThrowIdRequiredWhenIdDoesNotStartWithPrefix() {
        // Ici on teste la règle de format (pro)
        assertCode(ErrorCode.PARTICIPANT_ID_REQUIRED, () -> new GuestParticipant(
                "G1", "X", Gender.MALE, "FR", AgeCategory.SENIOR, MedicalCertificateStatus.VALIDE));
    }

    @Test
    void shouldThrowNameRequiredWhenNameBlank() {
        assertCode(ErrorCode.PARTICIPANT_NAME_REQUIRED, () -> new GuestParticipant(
                "GUEST-0001", " ", Gender.MALE, "FR", AgeCategory.SENIOR, MedicalCertificateStatus.VALIDE));
    }

    @Test
    void shouldThrowNationalityRequiredWhenNationalityBlank() {
        assertCode(ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED, () -> new GuestParticipant(
                "GUEST-0001", "X", Gender.MALE, " ", AgeCategory.SENIOR, MedicalCertificateStatus.VALIDE));
    }

    @Test
    void shouldThrowNationalityRequiredWhenNationalityNotIso2() {
        assertCode(ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED, () -> new GuestParticipant(
                "GUEST-0001", "X", Gender.MALE, "FRA", AgeCategory.SENIOR, MedicalCertificateStatus.VALIDE));
    }

    @Test
    void shouldThrowGenderRequiredWhenGenderNull() {
        assertCode(ErrorCode.PARTICIPANT_GENDER_REQUIRED, () -> new GuestParticipant(
                "GUEST-0001", "X", null, "FR", AgeCategory.SENIOR, MedicalCertificateStatus.VALIDE));
    }

    @Test
    void shouldThrowAgeCategoryRequiredWhenAgeCategoryNull() {
        assertCode(ErrorCode.PARTICIPANT_AGE_CATEGORY_REQUIRED, () -> new GuestParticipant(
                "GUEST-0001", "X", Gender.MALE, "FR", null, MedicalCertificateStatus.VALIDE));
    }

    @Test
    void shouldThrowMedicalCertRequiredWhenMedicalCertNull() {
        assertCode(ErrorCode.PARTICIPANT_MEDICAL_CERT_REQUIRED, () -> new GuestParticipant(
                "GUEST-0001", "X", Gender.MALE, "FR", AgeCategory.SENIOR, null));
    }

    private static void assertCode(ErrorCode code, Runnable r) {
        BusinessException ex = assertThrows(BusinessException.class, r::run);
        assertEquals(code, ex.getCode());
    }
}