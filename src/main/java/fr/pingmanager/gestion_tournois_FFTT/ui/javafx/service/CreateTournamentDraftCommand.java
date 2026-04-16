package fr.pingmanager.gestion_tournois_FFTT.ui.javafx.service;

import java.time.LocalDate;

import fr.pingmanager.gestion_tournois_FFTT.domain.competition.model.enums.TournamentLevel;
import fr.pingmanager.gestion_tournois_FFTT.domain.refdata.RankingPhase;

public record CreateTournamentDraftCommand(
                String clubId,
                String organizerId,
                String name,
                String address1,
                String address2,
                String city,
                String department,
                TournamentLevel level,
                RankingPhase phase,
                LocalDate startDate,
                LocalDate endDate) {
}