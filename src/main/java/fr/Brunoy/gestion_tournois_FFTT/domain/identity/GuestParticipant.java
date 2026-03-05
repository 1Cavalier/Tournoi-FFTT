package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;

import java.util.Locale;
import java.util.Objects;

public final class GuestParticipant implements Participant {

    private static final String ID_PREFIX = "GUEST-";

    private final String guestId; // ex: "GUEST-0001"
    private final String fullName;
    private final Gender gender;
    private final String nationalityCode; // ISO-2
    private final AgeCategory ageCategory;
    private final MedicalCertificateStatus medicalCertificateStatus;

    public GuestParticipant(
            String guestId,
            String fullName,
            Gender gender,
            String nationalityCode,
            AgeCategory ageCategory,
            MedicalCertificateStatus medicalCertificateStatus) {

        String id = normalizeRequiredUpper(guestId, ErrorCode.PARTICIPANT_ID_REQUIRED);
        if (!id.startsWith(ID_PREFIX)) {
            throw new BusinessException(ErrorCode.PARTICIPANT_ID_REQUIRED);
        }

        String name = normalizeRequiredKeepCase(fullName, ErrorCode.PARTICIPANT_NAME_REQUIRED);

        if (gender == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_GENDER_REQUIRED);

        String nat = normalizeRequiredUpper(nationalityCode, ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED);
        if (nat.length() != 2) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED);
        }

        if (ageCategory == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_AGE_CATEGORY_REQUIRED);
        if (medicalCertificateStatus == null)
            throw new BusinessException(ErrorCode.PARTICIPANT_MEDICAL_CERT_REQUIRED);

        this.guestId = id;
        this.fullName = name;
        this.gender = gender;
        this.nationalityCode = nat;
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

    @Override
    public String toString() {
        return fullName + " (" + guestId + ", " + nationalityCode + ")";
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