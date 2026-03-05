package fr.Brunoy.gestion_tournois_FFTT.domain.organization;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

import java.util.Locale;
import java.util.Objects;

public final class Departement {

    private final String code; // ex: "91", "2A", "971"
    private final String name; // ex: "Essonne"
    private final Region region;

    public Departement(String code, String name, Region region) {
        String c = normalizeRequiredUpper(code, ErrorCode.DEPARTEMENT_CODE_REQUIRED);
        if (!looksLikeDeptCode(c)) {
            throw new BusinessException(ErrorCode.DEPARTEMENT_CODE_REQUIRED);
        }

        this.code = c;
        this.name = normalizeRequiredKeepCase(name, ErrorCode.DEPARTEMENT_NAME_REQUIRED);

        if (region == null)
            throw new BusinessException(ErrorCode.DEPARTEMENT_REGION_REQUIRED);
        this.region = region;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Region getRegion() {
        return region;
    }

    @Override
    public String toString() {
        return name + " (" + code + ", " + region.getCode() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Departement d))
            return false;
        return code.equals(d.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    // ---------------- UTIL ----------------

    private static boolean looksLikeDeptCode(String code) {
        // "2A"/"2B"
        if (code.length() == 2 && code.charAt(0) == '2' && (code.charAt(1) == 'A' || code.charAt(1) == 'B')) {
            return true;
        }
        // chiffres 2 ou 3 (métropole + DOM/TOM usuels)
        if (code.length() == 2 || code.length() == 3) {
            for (int i = 0; i < code.length(); i++) {
                if (!Character.isDigit(code.charAt(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

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