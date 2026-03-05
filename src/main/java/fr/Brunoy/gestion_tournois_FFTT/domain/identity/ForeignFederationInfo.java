package fr.Brunoy.gestion_tournois_FFTT.domain.identity;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

import java.util.Objects;

public final class ForeignFederationInfo {

    private final String countryCode; // ex: "BE"
    private final String federationName; // ex: "AFTT"
    private final String foreignLicenseId; // ex: licence du pays (optionnel mais conseillé)

    private ForeignFederationInfo(String countryCode, String federationName, String foreignLicenseId) {
        if (countryCode == null || countryCode.isBlank())
            throw new BusinessException(ErrorCode.PARTICIPANT_NATIONALITY_REQUIRED);
        if (federationName == null || federationName.isBlank())
            throw new BusinessException(ErrorCode.PARTICIPANT_FOREIGN_FEDERATION_REQUIRED);

        this.countryCode = countryCode.trim().toUpperCase();
        this.federationName = federationName.trim();
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

    private static String normalizeOptional(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
}