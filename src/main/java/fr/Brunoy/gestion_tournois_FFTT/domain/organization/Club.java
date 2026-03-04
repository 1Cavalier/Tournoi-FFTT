package fr.Brunoy.gestion_tournois_FFTT.domain.organization;

import fr.Brunoy.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.Brunoy.gestion_tournois_FFTT.common.exception.ErrorCode;

import java.util.Objects;

public class Club {

    private final String number; // ex: "08911132"
    private final String name; // ex: "Brunoy CTT"
    private final Departement departement;

    private final String city; // ex: "Brunoy"

    // Ancien champ "address" remplacé par address1/address2
    private final String address1; // ex: "Gymnase Dupont" / "12 rue ..."
    private final String address2; // ex: "Bâtiment B, entrée arrière" (optionnel)

    // Optionnel
    private final Double latitude; // ex: 48.695
    private final Double longitude; // ex: 2.492

    public Club(
            String number,
            String name,
            Departement departement,
            String city,
            String address1,
            String address2,
            Double latitude,
            Double longitude) {
        if (number == null || number.isBlank())
            throw new BusinessException(ErrorCode.CLUB_NUMBER_REQUIRED);

        if (name == null || name.isBlank())
            throw new BusinessException(ErrorCode.CLUB_NAME_REQUIRED);

        if (departement == null)
            throw new BusinessException(ErrorCode.CLUB_DEPARTEMENT_REQUIRED);

        if (city == null || city.isBlank())
            throw new BusinessException(ErrorCode.CLUB_CITY_REQUIRED);

        // lat/lon : soit les deux null, soit les deux renseignés
        boolean hasLat = latitude != null;
        boolean hasLon = longitude != null;
        if (hasLat ^ hasLon) {
            throw new BusinessException(ErrorCode.CLUB_GEO_COORDINATES_INCOMPLETE);
        }

        // bornes classiques
        if (latitude != null) {
            boolean latInvalid = latitude < -90.0 || latitude > 90.0;
            boolean lonInvalid = longitude < -180.0 || longitude > 180.0;
            if (latInvalid || lonInvalid) {
                throw new BusinessException(ErrorCode.CLUB_GEO_COORDINATES_INVALID);
            }
        }

        this.number = number.trim();
        this.name = name.trim();
        this.departement = departement;
        this.city = city.trim();

        this.address1 = normalizeOptional(address1);
        this.address2 = normalizeOptional(address2);

        this.latitude = latitude;
        this.longitude = longitude;
    }

    private String normalizeOptional(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
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

    public String getAddress1() {
        return address1;
    }

    public String getAddress2() {
        return address2;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
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