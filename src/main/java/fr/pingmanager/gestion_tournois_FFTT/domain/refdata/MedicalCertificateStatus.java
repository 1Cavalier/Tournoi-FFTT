package fr.pingmanager.gestion_tournois_FFTT.domain.refdata;

public enum MedicalCertificateStatus {
    VALIDE,
    NON_VALIDE,
    NON_PRESENT;

    public boolean isValid() {
        return this == VALIDE;
    }
}