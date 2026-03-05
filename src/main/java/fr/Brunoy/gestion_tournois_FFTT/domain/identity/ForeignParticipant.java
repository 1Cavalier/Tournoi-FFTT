package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;

import java.util.Objects;

public final class ForeignParticipant implements Participant {

    private final String foreignId; // ex: "FOREIGN-0001"
    private final String fullName;
    private final Gender gender;
    private final AgeCategory ageCategory;

    private final MedicalCertificateStatus medicalCertificateStatus;

    private final ForeignFederationInfo federationInfo;

    /** Points FFTT utilisables pour l’éligibilité (déjà convertis/estimés). */
    private final int convertedPoints;

    public ForeignParticipant(
            String foreignId,
            String fullName,
            Gender gender,
            AgeCategory ageCategory,
            MedicalCertificateStatus medicalCertificateStatus,
            ForeignFederationInfo federationInfo,
            int convertedPoints) {

        if (foreignId == null || foreignId.isBlank())
            throw new BusinessException(ErrorCode.PARTICIPANT_ID_REQUIRED);
        if (fullName == null || fullName.isBlank())
            throw new BusinessException(ErrorCode.PARTICIPANT_NAME_REQUIRED);
        if (gender == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_GENDER_REQUIRED);
        if (ageCategory == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_AGE_CATEGORY_REQUIRED);
        if (medicalCertificateStatus == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_MEDICAL_CERT_REQUIRED);
        if (federationInfo == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_FOREIGN_FEDERATION_REQUIRED);
        if (convertedPoints < 0)
            throw new BusinessException(ErrorCode.PARTICIPANT_POINTS_NEGATIVE);

        this.foreignId = foreignId.trim().toUpperCase();
        this.fullName = fullName.trim();
        this.gender = gender;
        this.ageCategory = ageCategory;
        this.medicalCertificateStatus = medicalCertificateStatus;
        this.federationInfo = federationInfo;
        this.convertedPoints = convertedPoints;
    }

    @Override
    public String participantId() {
        return foreignId;
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
        return federationInfo.countryCode();
    }

    @Override
    public AgeCategory ageCategory() {
        return ageCategory;
    }

    @Override
    public MedicalCertificateStatus medicalCertificateStatus() {
        return medicalCertificateStatus;
    }

    @Override
    public int pointsFor(RankingPhase phase) {
        return convertedPoints; // V1 : mêmes points quel que soit la phase
    }

    @Override
    public boolean isFfttLicensed() {
        return false;
    }

    public ForeignFederationInfo federationInfo() {
        return federationInfo;
    }

    public int convertedPoints() {
        return convertedPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ForeignParticipant that))
            return false;
        return foreignId.equals(that.foreignId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreignId);
    }
}