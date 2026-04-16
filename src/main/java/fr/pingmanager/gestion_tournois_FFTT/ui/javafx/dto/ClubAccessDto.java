package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto;

public record ClubAccessDto(
        String id,
        String clubId,
        String email,
        String firstName,
        String lastName,
        String updatedAt) {
}