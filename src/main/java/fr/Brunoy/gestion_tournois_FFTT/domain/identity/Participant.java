package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.AgeCategory;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.Gender;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.MedicalCertificateStatus;
import fr.Brunoy.gestion_tournois_FFTT.domain.refdata.RankingPhase;

public interface Participant {

    /**
     * Identifiant stable dans le tournoi.
     * - FFTT : numéro de licence
     * - Guest : id généré
     * - Foreign : id fédération étrangère / passeport / etc.
     *
     * Recommandation : trim + upper-case pour servir de clé.
     */
    String participantId();
    String fullName();
    Gender gender();

    default boolean isFemale() {
        return gender() == Gender.FEMALE;
    }

    /** Code pays conseillé (ISO-2 "FR", "BE", ...). */
    String nationalityCode();
    AgeCategory ageCategory();

    /** Points si dispo (FFTT), sinon 0 par défaut (invités). */
    int pointsFor(RankingPhase phase);
    MedicalCertificateStatus medicalCertificateStatus();

    default boolean hasValidMedicalCertificate() {
        return medicalCertificateStatus() == MedicalCertificateStatus.VALIDE;
    }

    /** vrai si licencié FFTT (Player) */
    boolean isFfttLicensed();
}