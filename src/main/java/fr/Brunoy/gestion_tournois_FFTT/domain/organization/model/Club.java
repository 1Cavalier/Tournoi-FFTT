package fr.Brunoy.gestion_tournois_FFTT.domain.organization.model;

import java.util.Objects;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

public class Club {

    private final String number; // ex: "08911132"
    private final String name; // ex: "Brunoy CTT"
    private final Departement departement;

    private final String city; // ex: "Brunoy"
    private final String address; // ex: "Gymnase Dupont" (optionnel)

    public Club(
            String number,
            String name,
            Departement departement,
            String city,
            String address) {
        if (number == null || number.isBlank())
            throw new BusinessException(ErrorCode.CLUB_NUMBER_REQUIRED);

        if (name == null || name.isBlank())
            throw new BusinessException(ErrorCode.CLUB_NAME_REQUIRED);

        if (departement == null)
            throw new BusinessException(ErrorCode.CLUB_DEPARTEMENT_REQUIRED);

        if (city == null || city.isBlank())
            throw new BusinessException(ErrorCode.CLUB_CITY_REQUIRED);

        this.number = number.trim();
        this.name = name.trim();
        this.departement = departement;
        this.city = city.trim();
        this.address = (address == null || address.isBlank()) ? null : address.trim();
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public Departement getDepartment() {
        return departement;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return name + " (" + number + ", " + city + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Club c))
            return false;
        return number.equals(c.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
