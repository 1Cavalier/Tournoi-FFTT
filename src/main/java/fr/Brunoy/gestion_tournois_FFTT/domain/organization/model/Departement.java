package fr.Brunoy.gestion_tournois_FFTT.domain.organization.model;

import java.util.Objects;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

public class Departement {

    private final String code; // ex: "91"
    private final String name; // ex: "Essonne"
    private final Region region;

    public Departement(String code, String name, Region region) {
        if (code == null || code.isBlank())
            throw new BusinessException(ErrorCode.DEPARTEMENT_CODE_REQUIRED);
        if (name == null || name.isBlank())
            throw new BusinessException(ErrorCode.DEPARTEMENT_NAME_REQUIRED);
        if (region == null)
            throw new BusinessException(ErrorCode.DEPARTEMENT_REGION_REQUIRED);

        this.code = code.trim();
        this.name = name.trim();
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
        return name + " (" + code + ", " + region + ")";
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
}
