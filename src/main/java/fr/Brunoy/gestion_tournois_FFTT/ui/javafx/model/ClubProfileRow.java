package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model;

public record ClubProfileRow(
        String organizerId,
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
        String logoPath) {
}
