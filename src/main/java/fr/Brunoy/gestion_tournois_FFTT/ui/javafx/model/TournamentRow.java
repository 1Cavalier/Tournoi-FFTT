package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model;

public record TournamentRow(
                String id,
                String organizerId,
                String name,
                String level,
                String phase,
                String startDate,
                String endDate,
                String status) {
}
