package fr.Brunoy.gestion_tournois_FFTT.domain.organization;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

import java.util.Locale;
import java.util.Objects;

public final class Region {

    private final String code; // ex: "IDF"
    private final String name; // ex: "Île-de-France"

    public Region(String code, String name) {
        this.code = normalizeRequiredUpper(code, ErrorCode.REGION_CODE_REQUIRED);
        this.name = normalizeRequiredKeepCase(name, ErrorCode.REGION_NAME_REQUIRED);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Region r))
            return false;
        return code.equals(r.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
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