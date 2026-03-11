package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto;

/**
 * Représente une ligne de la table tournament.
 * Utilisé par la couche UI / persistence.
 */

public record TournamentRow(
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
                String status,
                String createdAt,
                String updatedAt) {
}