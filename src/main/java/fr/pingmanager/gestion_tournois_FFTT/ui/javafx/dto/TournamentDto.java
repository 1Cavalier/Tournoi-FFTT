package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.dto;

/**
 * Représente une ligne de la table tournament.
 * Utilisé par la couche UI / persistence.
 */

public record TournamentDto(
                String id,
                String clubId,
                String organizerId,
                String name,
                String address1,
                String address2,
                String city,
                String department,
                String level,
                String phase,
                String startDate,
                String endDate,
                String homologationNumber,
                String status,
                String drawAlgorithmType,
                String createdAt,
                String updatedAt) {
}