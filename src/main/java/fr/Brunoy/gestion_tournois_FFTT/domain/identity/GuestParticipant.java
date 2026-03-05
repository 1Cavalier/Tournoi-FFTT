package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;

import java.util.Objects;

public final class GuestParticipant implements Participant {

    private final String guestId; // ex: "GUEST-0001" (généré côté UI/infra)
    private final String fullName;
    private final Gender gender;
    private final String nationalityCode; // ex: "FR", "BE"...
    private final AgeCategory ageCategory;

    private final MedicalCertificateStatus medicalCertificateStatus;

    public GuestParticipant(
            String guestId,
            String fullName,
            Gender gender,
            String nationalityCode,
            AgeCategory ageCategory,
            MedicalCertificateStatus medicalCertificateStatus) {

        if (guestId == null || guestId.isBlank())
            throw new BusinessException(ErrorCode.PARTICIPANT_ID_REQUIRED);
        if (fullName == null || fullName.isBlank())
            throw new BusinessException(ErrorCode.PARTICIPANT_NAME_REQUIRED);
        if (gender == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_GENDER_REQUIRED);
        if (nationalityCode == null || nationalityCode.isBlank())
            throw new BusinessException(ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED);
        if (ageCategory == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_AGE_CATEGORY_REQUIRED);
        if (medicalCertificateStatus == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_MEDICAL_CERT_REQUIRED);

        this.guestId = guestId.trim().toUpperCase();
        this.fullName = fullName.trim();
        this.gender = gender;
        this.nationalityCode = nationalityCode.trim().toUpperCase();
        this.ageCategory = ageCategory;
        this.medicalCertificateStatus = medicalCertificateStatus;
    }

    @Override
    public String participantId() {
        return guestId;
    }

    @Override
    public String fullName() {
        return fullName;
    }

    @Override
    public Gender gender() {
        return gender;
    }

    @Override
    public String nationalityCode() {
        return nationalityCode;
    }

    @Override
    public AgeCategory ageCategory() {
        return ageCategory;
    }

    @Override
    public int pointsFor(RankingPhase phase) {
        // invités : pas de classement FFTT -> 0
        return 0;
    }

    @Override
    public MedicalCertificateStatus medicalCertificateStatus() {
        return medicalCertificateStatus;
    }

    @Override
    public boolean isFfttLicensed() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GuestParticipant that))
            return false;
        return guestId.equals(that.guestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guestId);
    }
}