package fr.Brunoy.gestion_tournois_FFTT.domain.identity.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.model.Club;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.AgeCategory;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.Gender;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.LicenseType;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.MedicalCertificateStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.RankingPhase;

public class Player {

    private final String licenseNumber; // unique FFTT
    private final String firstName;
    private final String lastName;
    private final Gender gender; 
    private final String nationality;

    private final Club club;
    private final AgeCategory ageCategory;
    private final LicenseType licenseType;
    private final boolean mutated;

    // certificat médical : statut seulement (pas de date)
    private MedicalCertificateStatus medicalCertificateStatus;

    private final int phase1StartPoints;
    private final int phase2OfficialPoints;

    // peut être vide : c'est normal
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

        if (licenseNumber == null || licenseNumber.isBlank())
            throw new BusinessException(ErrorCode.PLAYER_LICENSE_REQUIRED);
        if (firstName == null || firstName.isBlank())
            throw new BusinessException(ErrorCode.PLAYER_FIRST_NAME_REQUIRED);
        if (lastName == null || lastName.isBlank())
            throw new BusinessException(ErrorCode.PLAYER_LAST_NAME_REQUIRED);
        if (gender == null)
            throw new BusinessException(ErrorCode.PLAYER_GENDER_REQUIRED);
        if (nationality == null || nationality.isBlank())
            throw new BusinessException(ErrorCode.PLAYER_NATIONALITY_REQUIRED);
        if (club == null)
            throw new BusinessException(ErrorCode.PLAYER_CLUB_REQUIRED);
        if (ageCategory == null)
            throw new BusinessException(ErrorCode.PLAYER_AGE_CATEGORY_REQUIRED);
        if (licenseType == null)
            throw new BusinessException(ErrorCode.PLAYER_LICENSE_TYPE_REQUIRED);
        if (medicalCertificateStatus == null)
            throw new BusinessException(ErrorCode.PLAYER_MEDICAL_CERT_REQUIRED);
        if (phase1StartPoints < 0 || phase2OfficialPoints < 0)
            throw new BusinessException(ErrorCode.PLAYER_POINTS_NEGATIVE);

        this.licenseNumber = licenseNumber.trim();
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.gender = gender; 
        this.nationality = nationality.trim().toUpperCase();
        this.club = club;
        this.ageCategory = ageCategory;
        this.licenseType = licenseType;
        this.mutated = mutated;
        this.medicalCertificateStatus = medicalCertificateStatus;
        this.phase1StartPoints = phase1StartPoints;
        this.phase2OfficialPoints = phase2OfficialPoints;
    }

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

    /** Points à utiliser selon la phase du tournoi. */
    public int pointsFor(RankingPhase phase) {
        if (phase == null) {
            // on peut aussi throw BusinessException, mais ici on est safe
            return phase2OfficialPoints;
        }
        return switch (phase) {
            case PHASE_1 -> phase1StartPoints;
            case PHASE_2 -> phase2OfficialPoints;
        };
    }

    /** Pratique pour l’inscription à un tournoi. */
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

    @Override
    public String toString() {
        return getFullName()
                + " [licence=" + licenseNumber
                + ", gender=" + gender
                + ", nat=" + nationality
                + ", club=" + club.getName()
                + ", dep=" + club.getDepartment().getCode()
                + ", region=" + club.getDepartment().getRegion().getName()
                + ", age=" + ageCategory
                + ", type=" + licenseType
                + ", mutation=" + mutated
                + ", certif=" + medicalCertificateStatus
                + ", P1=" + phase1StartPoints
                + ", P2=" + phase2OfficialPoints
                + "]";
    }

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
}
