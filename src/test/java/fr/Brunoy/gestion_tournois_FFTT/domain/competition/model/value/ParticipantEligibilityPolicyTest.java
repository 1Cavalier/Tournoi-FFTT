package fr.Brunoy.gestion_tournois_FFTT.domain.competition.model.value;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;
import fr.Brunoy.gestion_tournois_FFTT.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantEligibilityPolicyTest {

    @Test
    void ffttParticipantsShouldAlwaysBeEligible() {
        ParticipantEligibilityPolicy p = TestDataFactory.policyNoGuestNoForeign();
        Participant fftt = new FfttParticipant(TestDataFactory.maleSeniorBrunoy());

        assertDoesNotThrow(() -> p.assertEligible(fftt));
    }

    @Test
    void guestsShouldBeRejectedIfNotAllowed() {
        ParticipantEligibilityPolicy p = TestDataFactory.policyNoGuestNoForeign();
        GuestParticipant g = TestDataFactory.guest("G1", "Guest One", Gender.MALE, "FR", AgeCategory.SENIOR,
                MedicalCertificateStatus.VALIDE);

        BusinessException ex = assertThrows(BusinessException.class, () -> p.assertEligible(g));
        assertEquals(ErrorCode.REGISTRATION_GUEST_NOT_ALLOWED, ex.getCode());
    }

    @Test
    void foreignShouldBeRejectedIfNotAllowed() {
        ParticipantEligibilityPolicy p = TestDataFactory.policyNoGuestNoForeign();
        ForeignFederationInfo info = TestDataFactory.foreignFederationInfo("BE", "AFTT", "BE-1");

        ForeignParticipant fp = TestDataFactory.foreignParticipant(
                "FOREIGN-0001", "Foreign One", Gender.MALE, AgeCategory.SENIOR,
                MedicalCertificateStatus.VALIDE, info, 1200);

        BusinessException ex = assertThrows(BusinessException.class, () -> p.assertEligible(fp));
        assertEquals(ErrorCode.REGISTRATION_FOREIGN_NOT_ALLOWED, ex.getCode());
    }

    @Test
    void foreignWhitelistShouldRejectNonAllowedCountry() {
        ParticipantEligibilityPolicy p = TestDataFactory.policyForeignOnlyWhitelist(Set.of("BE", "CH"));
        ForeignFederationInfo info = TestDataFactory.foreignFederationInfo("ES", "RFETM", "ES-1");

        ForeignParticipant fp = TestDataFactory.foreignParticipant(
                "FOREIGN-0002", "Foreign ES", Gender.MALE, AgeCategory.SENIOR,
                MedicalCertificateStatus.VALIDE, info, 1100);

        BusinessException ex = assertThrows(BusinessException.class, () -> p.assertEligible(fp));
        assertEquals(ErrorCode.REGISTRATION_FOREIGN_COUNTRY_NOT_ALLOWED, ex.getCode());
    }

    @Test
    void foreignWhitelistShouldAcceptAllowedCountry() {
        ParticipantEligibilityPolicy p = TestDataFactory.policyForeignOnlyWhitelist(Set.of("BE", "CH"));
        ForeignFederationInfo info = TestDataFactory.foreignFederationInfo("BE", "AFTT", "BE-OK");

        ForeignParticipant fp = TestDataFactory.foreignParticipant(
                "FOREIGN-0003", "Foreign BE", Gender.MALE, AgeCategory.SENIOR,
                MedicalCertificateStatus.VALIDE, info, 1300);

        assertDoesNotThrow(() -> p.assertEligible(fp));
    }

    @Test
    void nullParticipantShouldThrow() {
        ParticipantEligibilityPolicy p = TestDataFactory.policyAllAllowed();
        BusinessException ex = assertThrows(BusinessException.class, () -> p.assertEligible(null));
        assertEquals(ErrorCode.PARTICIPANT_REQUIRED, ex.getCode());
    }
}