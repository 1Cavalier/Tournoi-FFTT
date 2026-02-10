package fr.Brunoy.gestion_tournois_FFTT.domain.model.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.identity.model.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.model.*;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.enums.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerCreationTest {

    // ---------- Méthodes utilitaires (objets valides) ----------

    private Region regionIdf() {
        return new Region("IDF", "Ile-de-France");
    }

    private Departement departementEssonne() {
        return new Departement("91", "Essonne", regionIdf());
    }

    private Club clubBrunoy() {
        return new Club("08911132", "Brunoy CTT", departementEssonne(), "Brunoy", "157 route de Brie");
    }

    private Player joueurValide() {
        return new Player(
                "1234567A",
                "Alice",
                "Martin",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820);
    }

    private void verifierErreurMetier(BusinessException ex, ErrorCode codeAttendu) {
        assertEquals(codeAttendu, ex.getCode());
    }

    // ---------- Tests OK ----------

    @Test
    void creationJoueur_valide_quandTousLesChampsSontCorrects() {
        Player joueur = joueurValide();

        assertEquals("1234567A", joueur.getLicenseNumber());
        assertEquals("Alice", joueur.getFirstName());
        assertEquals("Martin", joueur.getLastName());
        assertEquals("Alice Martin", joueur.getFullName());
        assertEquals("FRA", joueur.getNationality());

        assertEquals("Brunoy CTT", joueur.getClub().getName());
        assertEquals("91", joueur.getClub().getDepartment().getCode());
        assertEquals("Ile-de-France", joueur.getClub().getDepartment().getRegion().getName());

        assertEquals(AgeCategory.SENIOR, joueur.getAgeCategory());
        assertEquals(LicenseType.COMPETITION, joueur.getLicenseType());
        assertFalse(joueur.isMutated());

        assertEquals(MedicalCertificateStatus.VALIDE, joueur.getMedicalCertificateStatus());
        assertTrue(joueur.hasValidMedicalCertificate());

        assertEquals(800, joueur.getPhase1StartPoints());
        assertEquals(820, joueur.getPhase2OfficialPoints());
    }

    @Test
    void joueur_peutNePasAvoirDeQualification() {
        Player joueur = joueurValide();
        assertTrue(joueur.getQualifications().isEmpty());
    }

    @Test
    void nationalite_estMiseEnMajuscules() {
        Player joueur = new Player(
                "1234567A",
                "Alice",
                "Martin",
                Gender.MALE,
                "fra",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820);

        assertEquals("FRA", joueur.getNationality());
    }

    @Test
    void tousLesStatutsDeCertificatMedicalSontAcceptes() {
        for (MedicalCertificateStatus statut : MedicalCertificateStatus.values()) {
            Player joueur = new Player(
                    "LIC-" + statut.name(),
                    "Alice",
                    "Martin",
                    Gender.MALE,
                    "FRA",
                    clubBrunoy(),
                    AgeCategory.SENIOR,
                    LicenseType.COMPETITION,
                    false,
                    statut,
                    500,
                    520);

            assertEquals(statut, joueur.getMedicalCertificateStatus());
        }
    }

    @Test
    void certificatMedicalValide_uniquementQuandStatutEstValide() {
        Player joueurOk = new Player(
                "LIC-OK",
                "A",
                "B",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                0,
                0);

        Player joueurKo1 = new Player(
                "LIC-KO1",
                "A",
                "B",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.NON_VALIDE,
                0,
                0);

        Player joueurKo2 = new Player(
                "LIC-KO2",
                "A",
                "B",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.NON_PRESENTE,
                0,
                0);

        assertTrue(joueurOk.hasValidMedicalCertificate());
        assertFalse(joueurKo1.hasValidMedicalCertificate());
        assertFalse(joueurKo2.hasValidMedicalCertificate());
    }

    @Test
    void ajoutDuneQualification_estPossible() {
        Player joueur = joueurValide();
        joueur.addQualification(OfficialQualification.referee(RefereeGrade.CLUB));

        assertEquals(1, joueur.getQualifications().size());
    }

    @Test
    void miseAJourDuStatutDuCertificatMedical_estPossible() {
        Player joueur = joueurValide();
        joueur.updateMedicalCertificateStatus(MedicalCertificateStatus.NON_PRESENTE);

        assertEquals(MedicalCertificateStatus.NON_PRESENTE, joueur.getMedicalCertificateStatus());
        assertFalse(joueur.hasValidMedicalCertificate());
    }

    // ---------- Tests KO (erreurs joueur) ----------

    @Test
    void erreur_siNumeroDeLicenceVide() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "   ",
                "Alice",
                "Martin",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_LICENSE_REQUIRED);
    }

    @Test
    void erreur_siPrenomVide() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "   ",
                "Martin",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_FIRST_NAME_REQUIRED);
    }

    @Test
    void erreur_siNomVide() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "Alice",
                "   ",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_LAST_NAME_REQUIRED);
    }

    @Test
    void erreur_siNationaliteVide() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "Alice",
                "Martin",
                Gender.MALE,
                "   ",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_NATIONALITY_REQUIRED);
    }

    @Test
    void erreur_siClubNull() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "Alice",
                "Martin",
                Gender.MALE,
                "FRA",
                null,
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_CLUB_REQUIRED);
    }

    @Test
    void erreur_siCategorieAgeNull() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "Alice",
                "Martin",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                null,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_AGE_CATEGORY_REQUIRED);
    }

    @Test
    void erreur_siTypeDeLicenceNull() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "Alice",
                "Martin",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                null,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_LICENSE_TYPE_REQUIRED);
    }

    @Test
    void erreur_siStatutCertificatMedicalNull() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "Alice",
                "Martin",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                null,
                800,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_MEDICAL_CERT_REQUIRED);
    }

    // ---------- Tests KO (points) ----------

    @Test
    void erreur_siPointsPhase1Negatifs() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "Alice",
                "Martin",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                -1,
                820));

        verifierErreurMetier(ex, ErrorCode.PLAYER_POINTS_NEGATIVE);
    }

    @Test
    void erreur_siPointsPhase2Negatifs() {
        BusinessException ex = assertThrows(BusinessException.class, () -> new Player(
                "123",
                "Alice",
                "Martin",
                Gender.MALE,
                "FRA",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                800,
                -5));

        verifierErreurMetier(ex, ErrorCode.PLAYER_POINTS_NEGATIVE);
    }

    // ---------- Tests KO (API Player) ----------

    @Test
    void erreur_siAjoutQualificationNull() {
        Player joueur = joueurValide();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> joueur.addQualification(null));

        verifierErreurMetier(ex, ErrorCode.PLAYER_QUALIFICATION_REQUIRED);
    }

    @Test
    void erreur_siMiseAJourCertificatMedicalNull() {
        Player joueur = joueurValide();

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> joueur.updateMedicalCertificateStatus(null));

        verifierErreurMetier(ex, ErrorCode.PLAYER_MEDICAL_CERT_REQUIRED);
    }
}
