package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.AgeCategory;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.Gender;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.MedicalCertificateStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;
import fr.Brunoy.gestion_tournois_FFTT.testutil.TestDataFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FfttParticipantTest {

    @Test
    void shouldDelegateToPlayer() {
        Player p = TestDataFactory.maleSeniorBrunoy();
        FfttParticipant fp = new FfttParticipant(p);

        assertEquals(p.getLicenseNumber(), fp.participantId());
        assertEquals(p.getFullName(), fp.fullName());
        assertEquals(p.getGender(), fp.gender());
        assertEquals(p.getNationality(), fp.nationalityCode());
        assertEquals(p.getAgeCategory(), fp.ageCategory());
        assertEquals(p.getMedicalCertificateStatus(), fp.medicalCertificateStatus());
        assertEquals(p.pointsFor(RankingPhase.PHASE_2), fp.pointsFor(RankingPhase.PHASE_2));
        assertTrue(fp.isFfttLicensed());
    }

    @Test
    void equalsShouldBeBasedOnParticipantIdAcrossDifferentParticipantTypes() {
        FfttParticipant fp = TestDataFactory.participantFrom(
                TestDataFactory.maleSeniorBrunoy());

        Participant other = new Participant() {
            @Override
            public String participantId() {
                return "08911132A";
            }

            @Override
            public String fullName() {
                return "Quelqu'un";
            }

            @Override
            public Gender gender() {
                return Gender.MALE;
            }

            @Override
            public String nationalityCode() {
                return "FR";
            }

            @Override
            public AgeCategory ageCategory() {
                return AgeCategory.SENIOR;
            }

            @Override
            public int pointsFor(RankingPhase phase) {
                return 500;
            }

            @Override
            public MedicalCertificateStatus medicalCertificateStatus() {
                return MedicalCertificateStatus.VALIDE;
            }

            @Override
            public boolean isFfttLicensed() {
                return false;
            }
        };

        assertEquals(fp.participantId(), other.participantId());
    }
}