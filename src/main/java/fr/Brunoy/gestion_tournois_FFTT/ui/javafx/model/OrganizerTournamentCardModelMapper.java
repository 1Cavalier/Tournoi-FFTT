package fr.Brunoy.gestion_tournois_FFTT.ui.javafx.model;

import fr.Brunoy.gestion_tournois_FFTT.ui.javafx.infra.repo.SqliteClubRepository;

import java.util.List;
import java.util.Optional;

public final class OrganizerTournamentCardModelMapper {

    private OrganizerTournamentCardModelMapper() {
    }

    public static OrganizerTournamentCardModel map(
            TournamentRow tournament,
            Optional<SqliteClubRepository.ClubRow> clubOpt,
            List<TableauRow> tableaux) {

        String city = clubOpt.map(SqliteClubRepository.ClubRow::city).orElse(null);
        String dep = clubOpt.map(SqliteClubRepository.ClubRow::departementCode).orElse(null);

        Integer tableauCount = tableaux == null ? 0 : tableaux.size();

        String selectionByLabel = buildSelectionLabel(tableaux);
        String totalRewardLabel = null; // pas encore stocké côté UI
        String homologationNumber = null; // pas encore stocké côté UI

        boolean hasJudgeReferee = false; // pas encore stocké côté UI
        boolean hasReferee = false; // pas encore stocké côté UI

        boolean canManageRegistrations = "OPEN".equalsIgnoreCase(tournament.status())
                || "RUNNING".equalsIgnoreCase(tournament.status());

        boolean canPublish = tableauCount != null
                && tableauCount > 0
                && tournament.maxTableauxPerDay() != null
                && tournament.maxTableauxPerDay() > 0;

        return new OrganizerTournamentCardModel(
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
                homologationNumber,
                null,
                hasJudgeReferee,
                hasReferee,
                tournament.maxTableauxPerDay(),
                femaleRuleLabel(tournament.femaleExtraRule(), tournament.femaleExtraCode()),
                tableauCount,
                selectionByLabel,
                totalRewardLabel,
                canManageRegistrations,
                canPublish);
    }

    private static String buildSelectionLabel(List<TableauRow> tableaux) {
        if (tableaux == null || tableaux.isEmpty()) {
            return null;
        }
        return "Code / date / capacité";
    }

    private static String femaleRuleLabel(String rule, String code) {
        if (rule == null || rule.isBlank()) {
            return null;
        }

        return switch (rule.trim().toUpperCase()) {
            case "NONE" -> "Aucune";
            case "EXTRA_ANY_ONCE" -> "Bonus féminin une fois";
            case "EXTRA_ANY_PER_DAY" -> "Bonus féminin par jour";
            case "SPECIFIC_TABLEAU_ONCE" -> code == null || code.isBlank()
                    ? "Tableau spécifique une fois"
                    : "Tableau " + code + " une fois";
            case "SPECIFIC_TABLEAU_PER_DAY" -> code == null || code.isBlank()
                    ? "Tableau spécifique par jour"
                    : "Tableau " + code + " par jour";
            default -> rule;
        };
    }
}