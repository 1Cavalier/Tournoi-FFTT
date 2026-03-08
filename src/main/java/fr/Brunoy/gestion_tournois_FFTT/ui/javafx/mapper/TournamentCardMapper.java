package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.mapper;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.dto.*;

import java.util.List;
import java.util.Optional;

public final class TournamentCardMapper {

    private TournamentCardMapper() {
    }

    public static TournamentCardDto map(
            TournamentDto tournament,
            Optional<ClubDto> clubOpt,
            List<TableauDto> tableaux) {

        String city = clubOpt.map(ClubDto::city).orElse(null);
        String dep = clubOpt.map(ClubDto::departementCode).orElse(null);

        int tableauCount = tableaux == null ? 0 : tableaux.size();

        boolean canManageRegistrations = "OPEN".equalsIgnoreCase(tournament.status()) ||
                "RUNNING".equalsIgnoreCase(tournament.status());

        boolean canPublish = tableauCount > 0 &&
                tournament.maxTableauxPerDay() != null &&
                tournament.maxTableauxPerDay() > 0;

        return new TournamentCardDto(
                tournament.id(),
                tournament.organizerId(),
                tournament.name(),
                city,
                dep,
                tournament.level(),
                tournament.phase(),
                tournament.startDate(),
                tournament.endDate(),
                tournament.status(),
                null,
                null,
                false,
                false,
                tournament.maxTableauxPerDay(),
                null,
                tableauCount,
                null,
                null,
                canManageRegistrations,
                canPublish);
    }
}