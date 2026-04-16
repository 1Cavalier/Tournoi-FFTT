package fr.pingmanager.gestion_tournois_FFTT.domain.identity;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;
import fr.pingmanager.gestion_tournois_FFTT.domain.organization.Club;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.*;

public final class Player {

    private final String licenseNumber; // unique FFTT (clé métier)
    private final String firstName;
    private final String lastName;
    private final Gender gender;
    private final String nationality; // ISO-2 conseillé ("FR"...)

    private final Club club;
    private final AgeCategory ageCategory;
    private final LicenseType licenseType;
    private final boolean mutated;

    private MedicalCertificateStatus medicalCertificateStatus;

    private final int phase1StartPoints;
    private final int phase2OfficialPoints;

    private final Set<OfficialQualification> qualifications = new HashSet<>();

    public Player(
            String licenseNumber,
            String firstName,
            String lastName,
            Gender gender,
            String nationality,
            Club club,
            AgeCategory ageCategory,
            LicenseType licenseType,
            boolean mutated,
            MedicalCertificateStatus medicalCertificateStatus,
            int phase1StartPoints,
            int phase2OfficialPoints) {

        this.licenseNumber = normalizeRequired(licenseNumber, ErrorCode.PLAYER_LICENSE_REQUIRED);
        this.firstName = normalizeRequired(firstName, ErrorCode.PLAYER_FIRST_NAME_REQUIRED);
        this.lastName = normalizeRequired(lastName, ErrorCode.PLAYER_LAST_NAME_REQUIRED);

        if (gender == null)
            throw new BusinessException(ErrorCode.PLAYER_GENDER_REQUIRED);
        this.gender = gender;

        this.nationality = normalizeRequired(nationality, ErrorCode.PLAYER_NATIONALITY_REQUIRED);

        if (club == null)
            throw new BusinessException(ErrorCode.PLAYER_CLUB_REQUIRED);
        this.club = club;

        if (ageCategory == null)
            throw new BusinessException(ErrorCode.PLAYER_AGE_CATEGORY_REQUIRED);
        this.ageCategory = ageCategory;

        if (licenseType == null)
            throw new BusinessException(ErrorCode.PLAYER_LICENSE_TYPE_REQUIRED);
        this.licenseType = licenseType;

        this.mutated = mutated;

        if (medicalCertificateStatus == null)
            throw new BusinessException(ErrorCode.PLAYER_MEDICAL_CERT_REQUIRED);
        this.medicalCertificateStatus = medicalCertificateStatus;

        if (phase1StartPoints < 0 || phase2OfficialPoints < 0) {
            throw new BusinessException(ErrorCode.PLAYER_POINTS_NEGATIVE);
        }
        this.phase1StartPoints = phase1StartPoints;
        this.phase2OfficialPoints = phase2OfficialPoints;
    }

    // -------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Gender getGender() {
        return gender;
    }

    public boolean isFemale() {
        return gender == Gender.FEMALE;
    }

    public String getNationality() {
        return nationality;
    }

    public Club getClub() {
        return club;
    }

    public AgeCategory getAgeCategory() {
        return ageCategory;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public boolean isMutated() {
        return mutated;
    }

    public MedicalCertificateStatus getMedicalCertificateStatus() {
        return medicalCertificateStatus;
    }

    public int getPhase1StartPoints() {
        return phase1StartPoints;
    }

    public int getPhase2OfficialPoints() {
        return phase2OfficialPoints;
    }

    // -------------------------------------------------------------------------
    // METIER
    // -------------------------------------------------------------------------

    /** Points à utiliser selon la phase du tournoi. */
    public int pointsFor(RankingPhase phase) {
        if (phase == null)
            return phase2OfficialPoints; // safe default
        return switch (phase) {
            case PHASE_1 -> phase1StartPoints;
            case PHASE_2 -> phase2OfficialPoints;
        };
    }

    public boolean hasValidMedicalCertificate() {
        return medicalCertificateStatus == MedicalCertificateStatus.VALIDE;
    }

    public void updateMedicalCertificateStatus(MedicalCertificateStatus newStatus) {
        if (newStatus == null)
            throw new BusinessException(ErrorCode.PLAYER_MEDICAL_CERT_REQUIRED);
        this.medicalCertificateStatus = newStatus;
    }

    public void addQualification(OfficialQualification qualification) {
        if (qualification == null)
            throw new BusinessException(ErrorCode.PLAYER_QUALIFICATION_REQUIRED);
        qualifications.add(qualification);
    }

    public Set<OfficialQualification> getQualifications() {
        return Set.copyOf(qualifications);
    }

    public boolean hasQualification(OfficialRoleType roleType) {
        if (roleType == null)
            throw new BusinessException(ErrorCode.OFFICIAL_ROLE_REQUIRED);
        return qualifications.stream().anyMatch(q -> q.getRoleType() == roleType);
    }

    public OfficialQualification requireQualification(OfficialRoleType roleType) {
        if (roleType == null)
            throw new BusinessException(ErrorCode.OFFICIAL_ROLE_REQUIRED);
        return qualifications.stream()
                .filter(q -> q.getRoleType() == roleType)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PLAYER_QUALIFICATION_REQUIRED));
    }

    public FfttParticipant asParticipant() {
        return new FfttParticipant(this);
    }

    // -------------------------------------------------------------------------
    // VALUE IDENTITY
    // -------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Player p))
            return false;
        return licenseNumber.equals(p.licenseNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenseNumber);
    }

    @Override
    public String toString() {
        String clubName = safe(() -> club.getName());
        String depCode = safe(() -> club.getDepartment().getCode());
        String regionName = safe(() -> club.getDepartment().getRegion().getName());

        return getFullName()
                + " [licence=" + licenseNumber
                + ", gender=" + gender
                + ", nat=" + nationality
                + ", club=" + clubName
                + ", dep=" + depCode
                + ", region=" + regionName
                + ", age=" + ageCategory
                + ", type=" + licenseType
                + ", mutation=" + mutated
                + ", certif=" + medicalCertificateStatus
                + ", P1=" + phase1StartPoints
                + ", P2=" + phase2OfficialPoints
                + "]";
    }

    // -------------------------------------------------------------------------
    // HELPERS
    // -------------------------------------------------------------------------

    private static String normalizeRequired(String s, ErrorCode errorIfBlank) {
        if (s == null || s.isBlank())
            throw new BusinessException(errorIfBlank);
        // licence / nat en uppercase stable
        return s.trim().toUpperCase(Locale.ROOT);
    }

    private static String safe(SupplierString getter) {
        try {
            String v = getter.get();
            return (v == null) ? "?" : v;
        } catch (Exception e) {
            return "?";
        }
    }

    @FunctionalInterface
    private interface SupplierString {
        String get();
    }
}