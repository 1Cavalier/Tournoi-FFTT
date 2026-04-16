package fr.pingmanager.gestion_tournois_FFTT.domain.identity;

import java.util.Locale;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.*;

public final class ForeignParticipant implements Participant {

    private static final String ID_PREFIX = "FOREIGN-";

    private final String foreignId; // ex: "FOREIGN-0001"
    private final String fullName;
    private final Gender gender;
    private final AgeCategory ageCategory;
    private final MedicalCertificateStatus medicalCertificateStatus;
    private final ForeignFederationInfo federationInfo;
    private final int convertedPoints;

    public ForeignParticipant(
            String foreignId,
            String fullName,
            Gender gender,
            AgeCategory ageCategory,
            MedicalCertificateStatus medicalCertificateStatus,
            ForeignFederationInfo federationInfo,
            int convertedPoints) {

        String id = normalizeRequiredUpper(foreignId, ErrorCode.PARTICIPANT_ID_REQUIRED);
        if (!id.startsWith(ID_PREFIX)) {
            // Option A (recommandé) : créer PARTICIPANT_ID_INVALID
            throw new BusinessException(ErrorCode.PARTICIPANT_ID_REQUIRED);
        }

        String name = normalizeRequiredKeepCase(fullName, ErrorCode.PARTICIPANT_NAME_REQUIRED);

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

        this.foreignId = id;
        this.fullName = name;
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
        return convertedPoints;
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

    @Override
    public String toString() {
        return fullName + " (" + foreignId + ", " + nationalityCode() + ", pts=" + convertedPoints + ")";
    }

    // ---------------- UTIL ----------------

    private static String normalizeRequiredUpper(String s, ErrorCode errorIfBlank) {
        if (s == null || s.isBlank())
            throw new BusinessException(errorIfBlank);
        return s.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeRequiredKeepCase(String s, ErrorCode errorIfBlank) {
        if (s == null || s.isBlank())
            throw new BusinessException(errorIfBlank);
        return s.trim();
    }
}