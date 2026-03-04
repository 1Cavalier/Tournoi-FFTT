package fr.Brunoy.gestion_tournois_FFTT.domain.organization;

import java.util.Objects;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

public class Region {

    private final String code; // ex: "IDF"
    private final String name; // ex: "Île-de-France"

    public Region(String code, String name) {
        if (code == null || code.isBlank())
            throw new BusinessException(ErrorCode.REGION_CODE_REQUIRED);
        if (name == null || name.isBlank())
            throw new BusinessException(ErrorCode.REGION_NAME_REQUIRED);

        this.code = code.trim();
        this.name = name.trim();
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
}
