package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto;

/**
 * DTO UI : représentation d'un club utilisable partout (UI, services, repos).
 * Ne dépend pas de SQLite.
 */
public record ClubDto(
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
                String officialContactEmail,
                String logoPath,
                String updatedAt) {
}