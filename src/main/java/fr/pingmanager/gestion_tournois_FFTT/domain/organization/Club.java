package fr.pingmanager.gestion_tournois_FFTT.domain.organization;

import java.util.Objects;

import fr.pingmanager.gestion_tournois_FFTT.common.exception.BusinessException;
import fr.pingmanager.gestion_tournois_FFTT.common.exception.ErrorCode;

public final class Club {

    private final String number; // ex: "08911132"
    private final String name; // ex: "Brunoy CTT"
    private final Departement departement;

    private final String city; // ex: "Brunoy"

    private final String address1; // optionnel
    private final String address2; // optionnel

    private final Double latitude; // optionnel
    private final Double longitude; // optionnel

    public Club(
            String number,
            String name,
            Departement departement,
            String city,
            String address1,
            String address2,
            Double latitude,
            Double longitude) {

        String n = normalizeRequired(number, ErrorCode.CLUB_NUMBER_REQUIRED);
        if (!looksLikeClubNumber(n)) {
            // Option A (pro) : créer CLUB_NUMBER_INVALID
            throw new BusinessException(ErrorCode.CLUB_NUMBER_REQUIRED);
        }

        this.number = n;
        this.name = normalizeRequiredKeepCase(name, ErrorCode.CLUB_NAME_REQUIRED);

        if (departement == null)
            throw new BusinessException(ErrorCode.CLUB_DEPARTEMENT_REQUIRED);
        this.departement = departement;

        this.city = normalizeRequiredKeepCase(city, ErrorCode.CLUB_CITY_REQUIRED);

        // lat/lon : soit les deux null, soit les deux renseignés
        boolean hasLat = latitude != null;
        boolean hasLon = longitude != null;
        if (hasLat ^ hasLon) {
            throw new BusinessException(ErrorCode.CLUB_GEO_COORDINATES_INCOMPLETE);
        }

        if (latitude != null) {
            boolean latInvalid = latitude < -90.0 || latitude > 90.0;
            boolean lonInvalid = longitude < -180.0 || longitude > 180.0;
            if (latInvalid || lonInvalid) {
                throw new BusinessException(ErrorCode.CLUB_GEO_COORDINATES_INVALID);
            }
        }

        this.address1 = normalizeOptional(address1);
        this.address2 = normalizeOptional(address2);

        this.latitude = latitude;
        this.longitude = longitude;
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

    // ---------------- UTIL ----------------

    private static boolean looksLikeClubNumber(String number) {
        // FFTT: souvent 8 chiffres. On reste permissif: 6..10 chiffres.
        if (number.length() < 6 || number.length() > 10)
            return false;
        for (int i = 0; i < number.length(); i++) {
            if (!Character.isDigit(number.charAt(i)))
                return false;
        }
        return true;
    }

    private static String normalizeRequired(String s, ErrorCode errorIfBlank) {
        if (s == null || s.isBlank())
            throw new BusinessException(errorIfBlank);
        return s.trim();
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