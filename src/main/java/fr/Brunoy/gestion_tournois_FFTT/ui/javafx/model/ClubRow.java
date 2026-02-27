package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model;

/**
 * DTO UI : représentation d'un club utilisable partout (UI, services, repos).
 * Ne dépend pas de SQLite.
 */
public record ClubRow(
        String id,
        String clubNumber,
        String clubName,
        String departementCode,
        String city,
        String address1,
        String address2,
        Double latitude,
        Double longitude,
        String contactFirstName,
        String contactLastName,
        String logoPath,
        String updatedAt) {
}