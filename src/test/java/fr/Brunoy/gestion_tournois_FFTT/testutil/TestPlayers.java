package fr.Brunoy.gestion_tournois_FFTT.testutil;

import fr.Brunoy.gestion_tournois_FFTT.domain.identity.Player;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.Club;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.Departement;
import fr.Brunoy.gestion_tournois_FFTT.domain.organization.Region;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.*;

public final class TestPlayers {

    private TestPlayers() {
    }

    // ---- Ref org ----
    public static Region regionIdf() {
        return new Region("IDF", "Île-de-France");
    }

    public static Departement dept91() {
        return new Departement("91", "Essonne", regionIdf());
    }

    public static Club clubBrunoy() {
        return new Club("08911132", "CTTB Brunoy", dept91(), "Brunoy", "Gymnase", null, null, null);
    }

    // ---- Players ----

    public static Player maleFr(String licence, int p1, int p2) {
        return new Player(
                licence,
                "Jean",
                "Dupont",
                Gender.MALE,
                "FR",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                p1,
                p2);
    }

    public static Player femaleFr(String licence, int p1, int p2) {
        return new Player(
                licence,
                "Marie",
                "Durand",
                Gender.FEMALE,
                "FR",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                p1,
                p2);
    }

    public static Player maleBe(String licence, int p1, int p2) {
        return new Player(
                licence,
                "Louis",
                "Van Damme",
                Gender.MALE,
                "BE",
                clubBrunoy(),
                AgeCategory.SENIOR,
                LicenseType.COMPETITION,
                false,
                MedicalCertificateStatus.VALIDE,
                p1,
                p2);
    }
}