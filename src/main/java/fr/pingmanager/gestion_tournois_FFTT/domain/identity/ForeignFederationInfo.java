package fr.pingmanager.gestion_tournois_FFTT.domain.identity;

import java.util.Locale;
import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;

public final class ForeignFederationInfo {

    private final String countryCode; // ISO-2 ex: "BE"
    private final String federationName; // ex: "AFTT"
    private final String foreignLicenseId; // optionnel

    private ForeignFederationInfo(String countryCode, String federationName, String foreignLicenseId) {
        String cc = normalizeRequired(countryCode, ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED);
        if (cc.length() != 2) {
            throw new BusinessException(ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED);
        }

        String fed = normalizeRequiredKeepCase(federationName, ErrorCode.PARTICIPANT_FOREIGN_FEDERATION_REQUIRED);

        this.countryCode = cc;
        this.federationName = fed;
        this.foreignLicenseId = normalizeOptional(foreignLicenseId);
    }

    public static ForeignFederationInfo of(String countryCode, String federationName, String foreignLicenseId) {
        return new ForeignFederationInfo(countryCode, federationName, foreignLicenseId);
    }

    public String countryCode() {
        return countryCode;
    }

    public String federationName() {
        return federationName;
    }

    public String foreignLicenseId() {
        return foreignLicenseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ForeignFederationInfo that))
            return false;
        return countryCode.equals(that.countryCode)
                && federationName.equals(that.federationName)
                && Objects.equals(foreignLicenseId, that.foreignLicenseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, federationName, foreignLicenseId);
    }

    @Override
    public String toString() {
        return federationName + " (" + countryCode + ")"
                + (foreignLicenseId == null ? "" : " - licence=" + foreignLicenseId);
    }

    // ---------------- UTIL ----------------

    private static String normalizeRequired(String s, ErrorCode errorIfBlank) {
        if (s == null || s.isBlank())
            throw new BusinessException(errorIfBlank);
        return s.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeRequiredKeepCase(String s, ErrorCode errorIfBlank) {
        if (s == null || s.isBlank())
            throw new BusinessException(errorIfBlank);
        return s.trim();
    }

    private static String normalizeOptional(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}